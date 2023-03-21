package app.potato.bot.services;

import app.potato.bot.NatsConnection;
import app.potato.bot.RedisConnection;
import app.potato.bot.listeners.handlers.TwitterPostLinkRequest;
import app.potato.bot.utils.ChannelUtil;
import app.potato.bot.utils.NatsUtil;
import io.nats.client.Connection;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static app.potato.bot.services.ContentModerationService.ContentModerationResponse;
import static app.potato.bot.services.ContentModerationService.requestImageContentModeration;
import static app.potato.bot.services.FileDownloadResponse.FileDownloadMetadata;
import static app.potato.bot.services.TwitterService.TwitterModeratedFile.TwitterModerationData;
import static app.potato.bot.services.TwitterService.TwitterPostLinkRequestReply.TwitterPostMetadata;

public
class TwitterService {

    private static final Logger logger
            = LoggerFactory.getLogger( TwitterService.class );

    public static
    TwitterServiceResult requestPost( MessageReceivedEvent event,
                                      TwitterPostLinkRequest request )
    throws IOException, InterruptedException, ExecutionException
    {
        byte[] requestBytes = NatsUtil.getObjectBytes( request );

        Connection nc = NatsConnection.instance();

        TwitterPostLinkRequestReply twitterPostLinkRequestReply
                = nc.request( "twitter.post.request",
                              requestBytes ).handle( ( message, throwable ) -> {
            try {
                return NatsUtil.getMessageObject( message,
                                                  TwitterPostLinkRequestReply.class );
            }
            catch ( Exception e ) {
                logger.error( "Error transforming response : {}",
                              e.getMessage() );
                return null;
            }
        } ).get();

        ArrayList<FileDownloadResponse> fileDownloadResponses
                = twitterPostLinkRequestReply.downloadResponses();
        TwitterPostMetadata twitterPostMetadata
                = twitterPostLinkRequestReply.metadata();

        boolean nsfwChannel = ChannelUtil.isNsfwChannel( event );

        ArrayList<TwitterModeratedFile> moderatedFiles
                = fileDownloadResponses.stream()
                                       .reduce( new ArrayList<>(),
                                                ( twitterServiceResults, fileDownloadResponse ) -> {
                                                    if ( fileDownloadResponse.success() && fileDownloadResponse.metadata()
                                                                                                               .isPresent() )
                                                    {
                                                        try {
                                                            FileDownloadMetadata
                                                                    fileDownloadMetadata
                                                                    = fileDownloadResponse.metadata()
                                                                                          .get();
                                                            String
                                                                    base64ImageData
                                                                    = RedisConnection.instance()
                                                                                     .get( fileDownloadMetadata.key() );

                                                            byte[] imageBytes
                                                                    = Base64.getDecoder()
                                                                            .decode( base64ImageData );

                                                            TwitterModerationData
                                                                    moderationData
                                                                    = new TwitterModerationData( twitterPostMetadata.suggestive(),
                                                                                                 !twitterPostMetadata.suggestive() && !nsfwChannel
                                                                                                 ? requestImageContentModeration( event,
                                                                                                                                  fileDownloadMetadata.mimeType(),
                                                                                                                                  imageBytes )
                                                                                                 : Optional.empty() );

                                                            TwitterModeratedFile
                                                                    result
                                                                    = new TwitterModeratedFile( fileDownloadMetadata,
                                                                                                imageBytes,
                                                                                                moderationData );

                                                            twitterServiceResults.add( result );

                                                        }
                                                        catch ( Exception e ) {
                                                            logger.info( "Error moderating : {}",
                                                                         e.getMessage() );
                                                        }
                                                    }
                                                    return twitterServiceResults;
                                                },
                                                ( twitterServiceResults, twitterServiceResults2 ) -> {
                                                    twitterServiceResults.addAll( twitterServiceResults2 );
                                                    return twitterServiceResults;
                                                } );

        return new TwitterServiceResult( twitterPostMetadata,
                                         moderatedFiles );
    }

    public
    record TwitterServiceResult(
            TwitterPostMetadata metadata,
            ArrayList<TwitterModeratedFile> moderatedFiles
    ) {}

    public
    record TwitterModeratedFile(
            FileDownloadMetadata metadata,
            byte[] imageData,
            TwitterModerationData moderation
    )
    {
        public
        record TwitterModerationData(
                boolean suggestive,
                Optional<ContentModerationResponse> contentModerationResponse
        ) {}
    }

    public
    record TwitterPostLinkRequestReply(
            ArrayList<FileDownloadResponse> downloadResponses,
            TwitterPostMetadata metadata
    )
    {
        public
        record TwitterPostMetadata(
                String content,
                String userName,
                String userScreenName,
                String userUrl,
                String userThumbnailUrl,
                int retweets,
                int favourites,
                boolean suggestive,
                String createdAt

        ) {}
    }
}
