package app.potato.bot.listeners.handlers;

import app.potato.bot.clients.ContentModerationServiceClient;
import app.potato.bot.clients.PixivPostLinkServiceMessageClientRequestOptions;
import app.potato.bot.clients.PixivPostLinkServiceMessageClientRequestOptions.OptionFlag;
import app.potato.bot.utils.ExtendedFileUpload;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static app.potato.bot.clients.PixivPostLinkServiceMessageClientRequestOptions.ImageRequestQuality;
import static app.potato.bot.clients.PixivServiceMessageClient.PixivPostLinkRequestReply.PixivPostMetadata;
import static app.potato.bot.clients.PixivServiceMessageClient.PixivServiceResult;
import static app.potato.bot.clients.PixivServiceMessageClient.requestPost;
import static app.potato.bot.utils.MessageUtil.getSanitizedMessageTextContent;

public final
class PixivPostLinkMessageHandler extends MessageHandler {

    private static final Logger logger
            = LoggerFactory.getLogger( PixivPostLinkMessageHandler.class );

    public
    PixivPostLinkMessageHandler() {
        super( "pixiv.net" );
    }

    @Override
    public
    void handle( MessageReceivedEvent event )
    throws IOException, InterruptedException
    {
        Message targetMessage = event.getMessage();
        logger.info( "Handled by PixivPostLinkMessageHandler. Raw: {}",
                     targetMessage.getContentRaw() );

        event.getChannel().asGuildMessageChannel().sendTyping().queue();

        targetMessage.suppressEmbeds( true ).queue();

        String string = getSanitizedMessageTextContent( event );

        // TODO Split link url and optional flags
        ArrayList<String> splits
                = Arrays.stream( string.split( "\\s(?=\\S*https://)" ) )
                        .collect( Collectors.toCollection( ArrayList::new ) );

        ArrayList<PixivPostLinkServiceMessageClientRequestOptions> items
                = buildPixivPostLinkServiceOptions( splits );

        for ( PixivPostLinkServiceMessageClientRequestOptions options : items ) {

            PixivServiceResult pixivServiceResult = requestPost( event,
                                                                 options );

            if ( pixivServiceResult == null ) {
                logger.info( "Did not receive a response" );
                continue;
            } else {
                logger.info( "Message received: {}",
                             pixivServiceResult );
            }

            ArrayList<ContentModerationServiceClient.ModeratedContent>
                    moderatedContents
                    = pixivServiceResult.moderatedContents();

            ArrayList<ExtendedFileUpload> filesToUpload
                    = moderatedContents
                    .stream()
                    .map( moderatedContent -> {
                        FileUpload
                                fileUpload
                                = FileUpload.fromData( moderatedContent.data(),
                                                       moderatedContent.metadata()
                                                                       .getFileNameWithExtension() );

                        if ( moderatedContent.moderationData()
                                             .isExplicit()
                                ||
                                moderatedContent.moderationData()
                                                .contentModerationResponses()
                                        != null )
                        {
                            return new ExtendedFileUpload( moderatedContent.metadata(),
                                                           fileUpload.asSpoiler() );
                        }

                        return new ExtendedFileUpload( moderatedContent.metadata(),
                                                       fileUpload );
                    } )
                    .collect( Collectors.toCollection( ArrayList::new ) );

            PixivPostMetadata metadata = pixivServiceResult.metadata();

            EmbedBuilder embedBuilder
                    = new EmbedBuilder()
                    .setColor( Color.decode( "#1da0f2" ) )
                    .setTitle( metadata.title(),
                               metadata.url() )
                    .setAuthor( metadata.userName() )
                    .setDescription( metadata.description() )
//                                        .addField( "Tags",
//                                                   String.join( ", ",
//                                                                metadata.tags()
//                                                                        .toArray( value -> new String[]{} ) ),
//                                                   false )
                    .addField( "Favourites",
                               String.valueOf( metadata.favourites() ),
                               true )
                    .setFooter( "Pixiv" )
                    .setTimestamp( ZonedDateTime.parse( metadata.createdAt() )
                                                .toInstant() );


            MessageEmbed embed = embedBuilder.build();

            // Send embed
            targetMessage.reply( "" )
                         .mentionRepliedUser( false )
                         .addEmbeds( embed )
                         .queue();

            ArrayList<Integer> pagesToSend = new ArrayList<>();
            for ( int i = 0; i < filesToUpload.size(); i++ ) {
                if ( options.getOmit() && options.getSelection()
                                                 .contains( i + 1 ) )
                {
                    if ( filesToUpload.get( i ) != null ) {
                        pagesToSend.add( i );
                    }
                }
                // Add page if not omitted {1}
                else if ( !options.getOmit() && !options.getSelection()
                                                        .contains( i + 1 ) )
                {
                    if ( filesToUpload.get( i ) != null ) {
                        pagesToSend.add( i );
                    }
                }
            }

            int imageCount = filesToUpload.size();
            int count      = 0;

            // Send images
            if ( options.getSelection().size() > 0 ) {
                int selectCount = pagesToSend.size();
                for ( Integer idx : pagesToSend ) {
                    ExtendedFileUpload fileUpload = filesToUpload.get( idx );
                    if ( filesToUpload.get( idx ) != null ) {

                        String imageString = String
                                .format( "%s/%s (of %s)",
                                         ++count,
                                         selectCount,
                                         imageCount );
                        if ( fileUpload.metadata().size() > 25_000_000 )
                            targetMessage.reply( imageString.concat( " Attachment exceeds Max upload limit" ) )
                                         .mentionRepliedUser( false )
                                         .queue();
                        else {
                            targetMessage.reply( imageString )
                                         .mentionRepliedUser( false )
                                         .addFiles( fileUpload.fileUpload() )
                                         .queue();
                        }
                    }
                }
            } else {
                for ( ExtendedFileUpload fileUpload : filesToUpload ) {

                    String imageString = String.format( "%s/%s",
                                                        ++count,
                                                        imageCount );
                    if ( fileUpload.metadata().size() > 25_000_000 )
                        targetMessage.reply( imageString.concat( " Attachment exceeds Max upload limit" ) )
                                     .mentionRepliedUser( false )
                                     .queue();
                    else {
                        targetMessage.reply( imageString )
                                     .mentionRepliedUser( false )
                                     .addFiles( fileUpload.fileUpload() )
                                     .queue();
                    }
                }
            }

        }
    }

    private
    ArrayList<PixivPostLinkServiceMessageClientRequestOptions> buildPixivPostLinkServiceOptions( ArrayList<String> splits ) {

        return splits
                .stream()
                .reduce( new ArrayList<PixivPostLinkServiceMessageClientRequestOptions>() {},
                         ( pixivPostLinkRequests, messageTextContent ) -> {
                             try {
                                 ArrayList<String> tokens
                                         = new ArrayList<>( Arrays.stream( messageTextContent.split( "([ ,])+" ) )
                                                                  .toList() );
                                 String url = tokens.get( 0 );

                                 // Get post URL
                                 URI uri = new URI( url );
                                 // Remove url array
                                 tokens.remove( 0 );

                                 PixivPostLinkServiceMessageClientRequestOptions
                                         requestOptions
                                         = new PixivPostLinkServiceMessageClientRequestOptions();

                                 if ( uri.getHost()
                                         .contains( this.key ) )
                                 {
                                     String path = uri.getPath();
                                     Pattern pattern
                                             = Pattern.compile( "^(/\\w+)?/artworks/(?<postId>\\d+)(.*)?$" );
                                     Matcher matcher
                                             = pattern.matcher( path );

                                     if ( matcher.matches() ) {
                                         String postId
                                                 = matcher.group( "postId" );

                                         requestOptions.setPostId( postId );

                                     } else {
                                         logger.info( "Bad post link; Invalid post ID. Link: {}",
                                                      uri );
                                         return null;
                                     }
                                 }

                                 TreeSet<Integer> selectPages
                                         = new TreeSet<>() {};

                                 HashSet<OptionFlag> flagOptions
                                         = OptionFlag.OPTION_FLAGS;

                                 // read option flags and values
                                 OptionFlag currentOptionFlag = null;
                                 for ( String s : tokens ) {
                                     String token = s.trim();
                                     // If token is a flag, set flag
                                     OptionFlag flag
                                             = OptionFlag.keyOf( token );
                                     if ( flag != null ) {
                                         currentOptionFlag = flag;
                                     }
                                     // If current flag then get values
                                     else if ( currentOptionFlag != null ) {
                                         switch ( currentOptionFlag ) {
                                             case OMIT, PICK -> {
                                                 if ( requestOptions.getOmit() == null ) {
                                                     requestOptions.setOmit( currentOptionFlag == OptionFlag.OMIT );
                                                     flagOptions.removeAll( List.of( OptionFlag.OMIT,
                                                                                     OptionFlag.PICK ) );
                                                 }
                                                 // If token is a digit or is a range of digits
                                                 if ( token.matches( "\\d+(-)?(\\d+)?" ) ) {
                                                     try {
                                                         String[] range
                                                                 = token.split( "-" );
                                                         int rangeStart
                                                                 = Integer.parseInt( range[0] );
                                                         if ( rangeStart > 0 ) {
                                                             selectPages.add( rangeStart );
                                                         }
                                                         int rangeEnd
                                                                 = Integer.parseInt( range[1] );
                                                         if ( rangeEnd >= rangeStart ) {
                                                             for ( int i
                                                                   = rangeStart + 1; i <= rangeEnd; i++ )
                                                             {
                                                                 selectPages.add( i );
                                                             }
                                                         }
                                                     }
                                                     catch ( NumberFormatException | ArrayIndexOutOfBoundsException ignored ) {
                                                     }
                                                 }
                                             }
                                             case QUALITY -> {
                                                 switch ( token ) {
                                                     case "og", "0", "original" -> requestOptions.setQuality( ImageRequestQuality.ORIGINAL );
                                                     case "rg", "1", "regular" -> requestOptions.setQuality( ImageRequestQuality.REGULAR );
                                                     default -> {
                                                     }
                                                 }
                                                 flagOptions.remove( OptionFlag.QUALITY );
                                                 currentOptionFlag = null;
                                             }
                                             default -> {
                                             }
                                         }
                                     }
                                 }

                                 requestOptions.setSelection( selectPages );

                                 logger.info( "Options: {}",
                                              requestOptions );

                                 pixivPostLinkRequests.add( requestOptions );
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
