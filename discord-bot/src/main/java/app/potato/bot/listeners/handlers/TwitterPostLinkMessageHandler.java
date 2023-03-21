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
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Date;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static app.potato.bot.listeners.handlers.MessageHandler.AbstractMessageHandler;
import static app.potato.bot.services.TwitterService.*;
import static app.potato.bot.services.TwitterService.TwitterPostLinkRequestReply.TwitterPostMetadata;
import static app.potato.bot.utils.MessageUtil.getSanitizedContent;
import static net.dv8tion.jda.api.utils.TimeUtil.getDiscordTimestamp;

@MessageHandler
public final
class TwitterPostLinkMessageHandler extends AbstractMessageHandler {

    private static final Logger logger
            = LoggerFactory.getLogger( TwitterPostLinkMessageHandler.class );

    public
    TwitterPostLinkMessageHandler() {
        super( "twitter.com" );
    }

    @Override
    public
    void handle( MessageReceivedEvent event )
    throws IOException, ExecutionException, InterruptedException
    {
        logger.info( "Handled by TwitterPostLinkMessageHandler" );
        // suppress initial embeds
        Message targetMessage = event.getMessage();

        targetMessage.suppressEmbeds( true ).queue();

        String string = getSanitizedContent( event );

        String regex = "https://([vf]x)?twitter.com/\\w+/status/\\d+";

        // TODO Split link url and optional flags
        ArrayList<String> links
                = Arrays.stream( string.split( "\\s(?=\\S*https://)" ) )
                        .filter( s -> s.matches( regex ) )
                        .collect( Collectors.toCollection( ArrayList::new ) );

        List<TwitterPostLinkRequest> requests
                = buildTwitterPostLinkRequest( links );

        for ( TwitterPostLinkRequest request : requests ) {

            TwitterServiceResult twitterServiceResult = requestPost( event,
                                                                     request );

            ArrayList<TwitterModeratedFile> moderatedFiles
                    = twitterServiceResult.moderatedFiles();

            ArrayList<FileUpload> filesToUploads = moderatedFiles.stream()
                                                                 .map( twitterModeratedFile -> {
                                                                     FileUpload
                                                                             fileUpload
                                                                             = FileUpload.fromData( twitterModeratedFile.imageData(),
                                                                                                    twitterModeratedFile.metadata()
                                                                                                                        .getFileNameWithExtension() );
                                                                     if ( twitterModeratedFile.moderation()
                                                                                              .suggestive() || ( twitterModeratedFile.moderation()
                                                                                                                                     .contentModerationResponse()
                                                                                                                                     .isPresent() && twitterModeratedFile.moderation()
                                                                                                                                                                         .contentModerationResponse()
                                                                                                                                                                         .get()
                                                                                                                                                                         .result()
                                                                                                                                                                         .isPresent() && twitterModeratedFile.moderation()
                                                                                                                                                                                                             .contentModerationResponse()
                                                                                                                                                                                                             .get()
                                                                                                                                                                                                             .result()
                                                                                                                                                                                                             .get() ) )
                                                                         return fileUpload.asSpoiler();

                                                                     return fileUpload;
                                                                 } )
                                                                 .collect( Collectors.toCollection( ArrayList::new ) );

            TwitterPostMetadata metadata = twitterServiceResult.metadata();

            EmbedBuilder embedBuilder
                    = new EmbedBuilder().setColor( Color.decode( "#1da0f2" ) )
                                        .setThumbnail( metadata.userThumbnailUrl() )
                                        .setTitle( metadata.userName() + " (@" + metadata.userScreenName() + ")",
                                                   metadata.userUrl() )
                                        .setDescription( metadata.content() )
                                        .addField( "Retweets",
                                                   String.valueOf( metadata.retweets() ),
                                                   true )
                                        .addField( "Likes",
                                                   String.valueOf( metadata.favourites() ),
                                                   true )
                                        .setFooter( "on Bird app • " + getDiscordTimestamp( Date.from( ZonedDateTime.parse( metadata.createdAt() )
                                                                                                                    .toInstant() )
                                                                                                .getTime() ) );

            MessageEmbed embed = embedBuilder.build();

            targetMessage.reply( "" )
                         .mentionRepliedUser( false )
                         .addEmbeds( embed )
                         .queue();

            for ( FileUpload fileUpload : filesToUploads ) {
                targetMessage.reply( "" )
                             .mentionRepliedUser( false )
                             .addFiles( fileUpload )
                             .queue();
            }

        }
    }

    private
    ArrayList<TwitterPostLinkRequest> buildTwitterPostLinkRequest( ArrayList<String> splits )
    {
        return splits.stream()
                     .reduce( new ArrayList<TwitterPostLinkRequest>() {},
                              ( twitterPostLinkRequests, s ) -> {
                                  try {
                                      URL url = new URL( s );
                                      TwitterPostLinkRequest request
                                              = new TwitterPostLinkRequest( url.toString() );
                                      twitterPostLinkRequests.add( request );
                                  }
                                  catch ( MalformedURLException ignored ) {
                                  }
                                  return twitterPostLinkRequests;
                              },
                              ( twitterPostLinkRequests, twitterPostLinkRequests2 ) -> {
                                  twitterPostLinkRequests.addAll( twitterPostLinkRequests2 );
                                  return twitterPostLinkRequests;
                              } );
    }
}
