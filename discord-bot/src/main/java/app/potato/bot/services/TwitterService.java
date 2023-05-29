package app.potato.bot.services;

import app.potato.bot.MongoDBConnection;
import app.potato.bot.NatsConnection;
import app.potato.bot.listeners.handlers.TwitterPostLinkRequest;
import app.potato.bot.models.TwitterPostRequest;
import app.potato.bot.utils.ChannelUtil;
import app.potato.bot.utils.NatsUtil;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import io.nats.client.Connection;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static app.potato.bot.services.ContentModerationService.ContentModerationResponse;
import static app.potato.bot.services.ContentModerationService.requestImageContentModeration;
import static app.potato.bot.services.FileDownloadResponse.FileDownloadMetadata;
import static app.potato.bot.services.TwitterService.TwitterModeratedFile.TwitterModerationData;
import static app.potato.bot.services.TwitterService.TwitterPostLinkRequestReply.TwitterPostMetadata;
import static com.mongodb.client.model.Filters.eq;

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

                                                            MongoCollection<TwitterPostRequest>
                                                                    collection
                                                                    = MongoDBConnection.instance()
                                                                                       .getDatabase()
                                                                                       .getCollection( "TwitterPostRequests",
                                                                                                       TwitterPostRequest.class );
                                                            TwitterPostRequest
                                                                    twitterPostRequest
                                                                    = collection.find()
                                                                                .filter( eq( "key",
                                                                                             fileDownloadResponse.key() ) )
                                                                                .first();

                                                            if ( twitterPostRequest != null ) {

                                                                GridFSBucket
                                                                        bucket
                                                                        = MongoDBConnection.bucket();


                                                                GridFSFile entry
                                                                        = bucket.find( eq( "_id",
                                                                                           new ObjectId( twitterPostRequest.getKey() ) ) )
                                                                                .first();

                                                                if ( entry != null ) {

                                                                    String
                                                                            filename
                                                                            = entry.getFilename();

                                                                    GridFSDownloadStream
                                                                            downloadStream
                                                                            = bucket.openDownloadStream( filename );

                                                                    byte[]
                                                                            imageBytes
                                                                            = downloadStream.readAllBytes();
                                                                    downloadStream.close();


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
                                                            }

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
