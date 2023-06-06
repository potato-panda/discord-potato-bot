package app.potato.bot.services;

import app.potato.bot.MongoDBConnection;
import app.potato.bot.NatsConnection;
import app.potato.bot.listeners.handlers.PixivPostLinkRequest;
import app.potato.bot.models.PixivPostRequest;
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

import static app.potato.bot.services.ContentModerationService.ContentModerationData;
import static app.potato.bot.services.FileDownloadResponse.FileDownloadMetadata;
import static app.potato.bot.services.PixivService.PixivPostLinkRequestReply.PixivPostMetadata;
import static com.mongodb.client.model.Filters.eq;

public
class PixivService extends ContentModeratedService {

    private static final Logger logger
            = LoggerFactory.getLogger( PixivService.class );

    public static
    PixivServiceResult requestPost( MessageReceivedEvent event,
                                    PixivPostLinkRequest request )
    throws IOException, InterruptedException
    {
        byte[] requestBytes = NatsUtil.getObjectBytes( request );

        Connection nc = NatsConnection.instance();

        PixivPostLinkRequestReply pixivPostLinkRequestReply;
        Message message = nc.request( "pixiv.post.request",
                                      requestBytes,
                                      Duration.ofMinutes( 3 ) );

        Objects.requireNonNull( message,
                                "Request Timed out" );

        try {
            pixivPostLinkRequestReply = NatsUtil.getMessageObject( message,
                                                                   PixivPostLinkRequestReply.class );
        }
        catch ( Exception e ) {
            logger.error( "Error transforming response : {}",
                          e.getMessage() );
            return null;
        }

        ArrayList<FileDownloadResponse> fileDownloadResponses
                = pixivPostLinkRequestReply.downloadResponses();
        PixivPostMetadata pixivPostMetadata
                = pixivPostLinkRequestReply.metadata();

        ArrayList<ModeratedContent> moderatedContents
                = fileDownloadResponses.stream()
                                       .reduce( new ArrayList<>(),
                                                ( pixivServiceResults, fileDownloadResponse ) -> {
                                                    if ( fileDownloadResponse.success() && fileDownloadResponse.metadata()
                                                                                                               .isPresent() )
                                                    {
                                                        try {
                                                            FileDownloadMetadata
                                                                    fileDownloadMetadata
                                                                    = fileDownloadResponse.metadata()
                                                                                          .get();

                                                            MongoCollection<PixivPostRequest>
                                                                    collection
                                                                    = MongoDBConnection.instance()
                                                                                       .getDatabase()
                                                                                       .getCollection( "PixivPostRequests",
                                                                                                       PixivPostRequest.class );

                                                            PixivPostRequest
                                                                    pixivPostRequest
                                                                    = collection.find()
                                                                                .filter( eq( "key",
                                                                                             fileDownloadResponse.key() ) )
                                                                                .first();

                                                            if ( pixivPostRequest != null ) {

                                                                GridFSBucket
                                                                        bucket
                                                                        = MongoDBConnection.bucket();


                                                                ObjectId oid
                                                                        = new ObjectId( pixivPostRequest.getKey() );

                                                                GridFSFile entry
                                                                        = bucket.find( eq( "_id",
                                                                                           oid ) )
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

                                                                    ContentModerationData
                                                                            moderationData
                                                                            = new ContentModerationData( pixivPostMetadata.adult(),
                                                                                                         // Do moderationData if safe for work
                                                                                                         getContentModerationData( event,
                                                                                                                                   pixivPostMetadata.adult(),
                                                                                                                                   fileDownloadMetadata,
                                                                                                                                   imageBytes ) );

                                                                    ModeratedContent
                                                                            pixivModeratedFile
                                                                            = new ModeratedContent( fileDownloadMetadata,
                                                                                                    moderationData,
                                                                                                    imageBytes
                                                                    );

                                                                    pixivServiceResults.add( pixivModeratedFile );
                                                                }
                                                            }
                                                        }
                                                        catch ( Exception e ) {
                                                            logger.info( "Error moderating : {}",
                                                                         e.getMessage() );
                                                        }
                                                    }
                                                    return pixivServiceResults;
                                                },
                                                ( pixivServiceResults, pixivServiceResults2 ) -> {
                                                    pixivServiceResults.addAll( pixivServiceResults2 );
                                                    return pixivServiceResults;
                                                } );

        return new PixivServiceResult( pixivPostMetadata,
                                       moderatedContents );
    }

    public
    record PixivServiceResult(
            PixivPostMetadata metadata,
            ArrayList<ModeratedContent> moderatedContents
    ) {}

    public
    record PixivPostLinkRequestReply(
            PixivPostMetadata metadata,
            ArrayList<FileDownloadResponse> downloadResponses
    )
    {

        public
        record PixivPostMetadata(
                boolean adult,
                ArrayList<String> tags,
                String url,
                String title,
                String description,
                String userName,
                String userAccount,
                boolean isAi,
                int favourites,
                String createdAt,
                IllustrationType illustType
        )
        {
            public
            enum IllustrationType {
                illust( "illust" ), manga( "manga" ), ugoira( "ugoira" );

                private final String value;

                IllustrationType( String value ) {
                    this.value = value;
                }

                public
                String getValue() {
                    return value;
                }
            }
        }
    }

}
