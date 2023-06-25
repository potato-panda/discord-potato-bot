package app.potato.bot.clients;

import app.potato.bot.MongoDBConnection;
import app.potato.bot.NatsConnection;
import app.potato.bot.models.TwitterPostRequest;
import app.potato.bot.utils.FileDownloadResponse;
import app.potato.bot.utils.NatsUtil;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import io.nats.client.Connection;
import io.nats.client.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Objects;

import static app.potato.bot.clients.ContentModerationServiceClient.ContentModerationData;
import static app.potato.bot.clients.ContentModerationServiceClient.ModeratedContent;
import static app.potato.bot.clients.TwitterServiceMessageClient.TwitterPostLinkRequestReply.TwitterPostMetadata;
import static app.potato.bot.utils.FileDownloadResponse.FileDownloadMetadata;
import static com.mongodb.client.model.Filters.eq;

public
class TwitterServiceMessageClient {

    private static final Logger logger
            = LoggerFactory.getLogger( TwitterServiceMessageClient.class );

    public static
    TwitterServiceResult requestPost( MessageReceivedEvent event,
                                      TwitterPostLinkServiceClientRequestOptions request )
    throws IOException, InterruptedException
    {
        byte[] requestBytes = NatsUtil.getObjectBytes( request );

        Connection nc = NatsConnection.instance();

        TwitterPostLinkRequestReply twitterPostLinkRequestReply;
        Message message = nc.request( "twitter.post.request",
                                      requestBytes,
                                      Duration.ofMinutes( 3 ) );

        Objects.requireNonNull( message,
                                "Request Timed out" );

        try {
            twitterPostLinkRequestReply = NatsUtil.getMessageObject( message,
                                                                     TwitterPostLinkRequestReply.class );
        }
        catch ( Exception e ) {
            logger.error( "Error transforming response : {}",
                          e.getMessage() );
            return null;
        }

        ArrayList<FileDownloadResponse> downloadResponses
                = twitterPostLinkRequestReply.downloadResponses();
        TwitterPostMetadata twitterPostMetadata
                = twitterPostLinkRequestReply.metadata();

        ArrayList<ModeratedContent> moderatedContents
                = downloadResponses.stream()
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

                                                            GridFSBucket bucket
                                                                    = MongoDBConnection.bucket();

                                                            ObjectId oid
                                                                    = new ObjectId( twitterPostRequest.getKey() );


                                                            GridFSFile entry
                                                                    = bucket.find( eq( "_id",
                                                                                       oid ) )
                                                                            .first();

                                                            if ( entry != null ) {

                                                                String filename
                                                                        = entry.getFilename();

                                                                GridFSDownloadStream
                                                                        downloadStream
                                                                        = bucket.openDownloadStream( filename );

                                                                byte[]
                                                                        imageBytes
                                                                        = downloadStream.readAllBytes();

                                                                downloadStream.close();

                                                                ContentModerationData
                                                                        moderationData
                                                                        = new ContentModerationData( event,
                                                                                                     twitterPostMetadata.suggestive(),
                                                                                                     fileDownloadMetadata,
                                                                                                     imageBytes );

                                                                ModeratedContent
                                                                        result
                                                                        = new ModeratedContent( fileDownloadMetadata,
                                                                                                moderationData,
                                                                                                imageBytes );

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
                                         moderatedContents );
    }

    public
    record TwitterServiceResult(
            TwitterPostMetadata metadata,
            ArrayList<ModeratedContent> moderatedContents
    ) {}

    public
    record TwitterPostLinkRequestReply(
            TwitterPostMetadata metadata,
            ArrayList<FileDownloadResponse> downloadResponses
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
                String createdAt,
                String embedColour

        ) {}
    }
}
