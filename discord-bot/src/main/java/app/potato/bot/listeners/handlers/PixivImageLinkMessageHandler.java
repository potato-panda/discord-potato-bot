package app.potato.bot.listeners.handlers;

import app.potato.bot.services.ContentModeratedService.ModeratedContent;
import app.potato.bot.utils.Disabled;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.utils.FileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Formatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static app.potato.bot.listeners.handlers.MessageHandler.AbstractMessageHandler;
import static app.potato.bot.services.PixivService.PixivServiceResult;
import static app.potato.bot.services.PixivService.requestPost;
import static app.potato.bot.utils.MessageUtil.getSanitizedContent;

@Disabled
@MessageHandler
public final
class PixivImageLinkMessageHandler extends AbstractMessageHandler {

    private static final Logger logger
            = LoggerFactory.getLogger( PixivImageLinkMessageHandler.class );

    PixivImageLinkMessageHandler() {
        super( "i.pximg.net" );
    }

    @Override
    public
    void handle( MessageReceivedEvent event )
    throws IOException, InterruptedException
    {
        // suppress initial embeds
        event.getMessage().suppressEmbeds( true ).queue();
        String string = getSanitizedContent( event );
        // TODO Split link url and optional flags
        String[] splits = string.split( "\\s+" );

        List<String> ids = Arrays.stream( splits )
                                 .reduce( new ArrayList<String>() {},
                                          ( strings, s ) -> {
                                              try {
                                                  URI uri = new URI( s );
                                                  if ( uri.getHost()
                                                          .equals( this.key ) )
                                                  {
                                                      String path
                                                              = uri.getPath();
                                                      Pattern pattern
                                                              = Pattern.compile( "^/(?<imageClass>[\\D&&[^\\/]]+)/img/(\\d{4})/(\\d{2})/(\\d{2})/(\\d{2})/(\\d{2})/(\\d{2})/(?<postId>\\d+)_p(?<page>\\d+)_?(?<quality>.+)?\\.(?<ext>.+)$" );
                                                      Matcher matcher
                                                              = pattern.matcher( path );
                                                      // TODO Create URL matcher
//                                                      if ( matcher.matches() ) {
//                                                          String postId
//                                                                  = matcher.group( "postId" );
//                                                          System.out.printf( "%s has match: id = %s\n",
//                                                                             path,
//                                                                             postId );
//                                                          strings.add( postId );
//                                                      } else System.out.printf( "%s doesn't match\n",
//                                                                                uri );
                                                  }
                                                  return strings;
                                              }
                                              catch ( Exception e ) {
                                                  return strings;
                                              }
                                          },
                                          ( arrayList, arrayList2 ) -> {
                                              arrayList.addAll( arrayList2 );
                                              return arrayList;
                                          } );

        // TODO foreach id, make request (Generally there will only be one link)
        for ( String id : ids ) {
            PixivPostLinkOptions pixivPostLinkOptions
                    = new PixivPostLinkOptions( id );

            PixivPostLinkRequest request
                    = new PixivPostLinkRequest( pixivPostLinkOptions.postId,
                                                pixivPostLinkOptions.quality );

            // TODO receive data, make post
            PixivServiceResult serviceResults = requestPost( event,
                                                             request );

            ArrayList<ModeratedContent> moderatedFile
                    = serviceResults.moderatedContents();

            List<FileUpload> uploadFiles = moderatedFile.stream()
                                                        .map( pixivModeratedFile -> FileUpload.fromData( pixivModeratedFile.imageData(),
                                                                                                         pixivModeratedFile.metadata()
                                                                                                                           .fileName() ) )
                                                        .collect( Collectors.toList() );

            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setTitle( new Formatter().format( "{} images",
                                                           uploadFiles.size() )
                                                  .toString() );
            MessageEmbed embed = embedBuilder.build();

            event.getMessage()
                 .reply( "" )
                 .addEmbeds( new MessageEmbed[]{ embed } )
                 .addFiles( uploadFiles );
        }

    }

    public static
    record PixivImageLinkRequest( String url ) {}
}
