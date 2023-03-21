package app.potato.bot.listeners.handlers;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.utils.FileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static app.potato.bot.listeners.handlers.MessageHandler.AbstractMessageHandler;
import static app.potato.bot.listeners.handlers.PixivPostLinkRequest.ImageRequestQuality;
import static app.potato.bot.services.PixivService.*;
import static app.potato.bot.services.PixivService.PixivPostLinkRequestReply.PixivPostMetadata;
import static app.potato.bot.utils.MessageUtil.getSanitizedContent;
import static net.dv8tion.jda.api.utils.TimeUtil.getDiscordTimestamp;

@MessageHandler
public final
class PixivPostLinkMessageHandler extends AbstractMessageHandler {

    private static final Logger logger
            = LoggerFactory.getLogger( PixivPostLinkMessageHandler.class );

    public
    PixivPostLinkMessageHandler() {
        super( "pixiv.net" );
    }

    @Override
    public
    void handle( MessageReceivedEvent event )
    throws IOException, ExecutionException, InterruptedException
    {
        logger.info( "Handled by PixivPostLinkMessageHandler" );
        // suppress initial embeds
        Message targetMessage = event.getMessage();

        targetMessage.suppressEmbeds( true ).queue();

        String string = getSanitizedContent( event );
        // TODO Split link url and optional flags
        ArrayList<String> splits
                = Arrays.stream( string.split( "\\s(?=\\S*https://)" ) )
                        .collect( Collectors.toCollection( ArrayList::new ) );

        ArrayList<PixivPostLinkOptions> options
                = buildPixivPostLinkOptions( splits );

        for ( PixivPostLinkOptions option : options ) {

            PixivPostLinkRequest request
                    = new PixivPostLinkRequest( option.postId,
                                                option.quality );

            PixivServiceResult pixivServiceResult = requestPost( event,
                                                                 request );

            ArrayList<PixivModeratedFile> moderatedFiles
                    = pixivServiceResult.moderatedFiles();

            ArrayList<FileUpload> filesToUpload = moderatedFiles.stream()
                                                                .map( pixivModeratedFile -> {
                                                                    FileUpload
                                                                            fileUpload
                                                                            = FileUpload.fromData( pixivModeratedFile.imageData(),
                                                                                                   pixivModeratedFile.fileMetadata()
                                                                                                                     .getFileNameWithExtension() );

                                                                    if (
                                                                        // If adult is true
                                                                            pixivModeratedFile.moderation()
                                                                                              .adult() ||
                                                                                    // Else check moderation data
                                                                                    ( pixivModeratedFile.moderation()
                                                                                                        .contentModerationResponse()
                                                                                                        .isPresent() && pixivModeratedFile.moderation()
                                                                                                                                          .contentModerationResponse()
                                                                                                                                          .get()
                                                                                                                                          .result()
                                                                                                                                          .isPresent() && pixivModeratedFile.moderation()
                                                                                                                                                                            .contentModerationResponse()
                                                                                                                                                                            .get()
                                                                                                                                                                            .result()
                                                                                                                                                                            .get() ) )

                                                                        return fileUpload.asSpoiler();

                                                                    return fileUpload;
                                                                } )
                                                                .collect( Collectors.toCollection( ArrayList::new ) );

            PixivPostMetadata metadata = pixivServiceResult.metadata();

            EmbedBuilder embedBuilder
                    = new EmbedBuilder().setColor( Color.decode( "#1da0f2" ) )
                                        .setTitle( metadata.title(),
                                                   metadata.url() )
                                        .setAuthor( metadata.userName() )
                                        .setDescription( metadata.description() )
//                                        .addField( "Tags",
//                                                   String.join( ", ",
//                                                                metadata.tags()
//                                                                        .toArray( value -> new String[]{} ) ),
//                                                   false )
                                        .addField( "Likes",
                                                   String.valueOf( metadata.likes() ),
                                                   true )
                                        .addField( "Favourites",
                                                   String.valueOf( metadata.hearts() ),
                                                   true )
                                        .setFooter( "on Bird app • " + getDiscordTimestamp( Date.from( ZonedDateTime.parse( metadata.createdAt() )
                                                                                                                    .toInstant() )
                                                                                                .getTime() ) );


            MessageEmbed embed = embedBuilder.build();

            targetMessage.reply( "" )
                         .mentionRepliedUser( false )
                         .addEmbeds( embed )
                         .queue();

            ArrayList<Integer> pagesToSend = new ArrayList<>();
            for ( int i = 0; i < filesToUpload.size(); i++ ) {
                if ( option.pick && option.selectPages.contains( i + 1 ) ) {
                    if ( filesToUpload.get( i ) != null ) {
                        pagesToSend.add( i );
                    }
                }
                // Add page if not omitted {1}
                else if ( !option.pick && !option.selectPages.contains( i + 1 ) ) {
                    if ( filesToUpload.get( i ) != null ) {
                        pagesToSend.add( i );
                    }
                }
            }

            if ( option.selectPages.size() > 0 ) {
                for ( Integer page : pagesToSend ) {
                    if ( filesToUpload.get( page ) != null ) {
                        targetMessage.reply( "" )
                                     .mentionRepliedUser( false )
                                     .addFiles( filesToUpload.get( page ) )
                                     .queue();
                    }
                }
            } else {
                for ( FileUpload uploadFile : filesToUpload ) {
                    targetMessage.reply( "" )
                                 .mentionRepliedUser( false )
                                 .addFiles( uploadFile )
                                 .queue();
                }
            }
        }
    }

    private
    ArrayList<PixivPostLinkOptions> buildPixivPostLinkOptions( ArrayList<String> splits ) {
        return splits.stream()
                     .reduce( new ArrayList<PixivPostLinkOptions>() {},
                              ( pixivPostLinkRequests, string ) -> {
                                  try {
                                      ArrayList<String> msgOptsSegment
                                              = Arrays.stream( string.split( "([ ,])+" ) )
                                                      .collect( Collectors.toCollection( ArrayList::new ) );
                                      String url = msgOptsSegment.get( 0 );

                                      // Get post URL
                                      URI uri = new URI( url );
                                      // Remove url array
                                      msgOptsSegment.remove( 0 );

                                      // Default quality
                                      ImageRequestQuality quality
                                              = ImageRequestQuality.regular;
                                      // Default pick
                                      boolean pick = true;
                                      // Select pages
                                      ArrayList<Integer> pages
                                              = new ArrayList<>() {};

                                      ArrayList<String> flagOptions
                                              = new ArrayList<>();
                                      Collections.addAll( flagOptions,
                                                          "-p",
                                                          "-o",
                                                          "-q" );

                                      // read option flags and values
                                      String switchFlag = "";
                                      for ( String s : msgOptsSegment ) {

                                          // If next is flag, set as current flag, and remove flag from next flags to check
                                          if ( flagOptions.contains( s ) ) {
                                              switchFlag = s;
                                          }
                                          // If current flag then get values
                                          else {
                                              String optVal = s.trim();

                                              switch ( switchFlag ) {
                                                  case "-o", "-p" -> {
                                                      if ( switchFlag.matches( "-o" ) ) {
                                                          pick = false;
                                                      }
                                                      // if value matches a range
                                                      if ( optVal.matches( "\\d+(-)?(\\d+)?" ) ) {
                                                          List<String> range
                                                                  = Arrays.stream( optVal.split( "-" ) )
                                                                          .limit( 2 )
                                                                          .toList();
                                                          for ( String r : range ) {
                                                              if ( !r.isEmpty() ) {
                                                                  pages.add( Integer.parseInt( r ) );
                                                              }
                                                          }
                                                      }
                                                      flagOptions.removeAll( List.of( new String[]{
                                                              "-o", "-p"
                                                      } ) );
                                                  }
                                                  case "-q" -> {
                                                      switch ( optVal ) {
                                                          case "rg", "regular" ->
                                                          {
                                                              quality
                                                                      = ImageRequestQuality.regular;
                                                          }
                                                          case "og", "original" ->
                                                          {
                                                              quality
                                                                      = ImageRequestQuality.original;
                                                          }
                                                          default -> {
                                                          }
                                                      }
                                                      flagOptions.remove( "-q" );
                                                      switchFlag = "";
                                                  }
                                                  default -> {
                                                  }
                                              }
                                          }
                                      }

                                      if ( uri.getHost()
                                              .contains( this.key ) )
                                      {
                                          String path = uri.getPath();
                                          Pattern pattern
                                                  = Pattern.compile( "^/en/artworks/(?<postId>\\d+)(.*)?$" );
                                          Matcher matcher
                                                  = pattern.matcher( path );

                                          if ( matcher.matches() ) {
                                              String postId
                                                      = matcher.group( "postId" );

                                              PixivPostLinkOptions
                                                      pixivPostLinkOptions
                                                      = new PixivPostLinkOptions( postId,
                                                                                  quality,
                                                                                  pick,
                                                                                  pages );

                                              pixivPostLinkRequests.add( pixivPostLinkOptions );
                                          }
                                      }
                                      return pixivPostLinkRequests;
                                  }
                                  catch ( Exception e ) {
                                      logger.info( "What happened? {}",
                                                   e.getMessage() );
                                      return pixivPostLinkRequests;
                                  }

                              },
                              ( pixivPostLinkRequests, pixivPostLinkRequests2 ) -> {
                                  pixivPostLinkRequests.addAll( pixivPostLinkRequests2 );
                                  return pixivPostLinkRequests;
                              } );
    }

}
