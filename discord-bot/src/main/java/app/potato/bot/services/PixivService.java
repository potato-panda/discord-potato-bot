package app.potato.bot.services;

import app.potato.bot.NatsConnection;
import app.potato.bot.RedisConnection;
import app.potato.bot.listeners.handlers.PixivPostLinkRequest;
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
import static app.potato.bot.services.PixivService.PixivModeratedFile.PixivModerationData;
import static app.potato.bot.services.PixivService.PixivPostLinkRequestReply.PixivPostMetadata;

public
class PixivService {

    private static final Logger logger
            = LoggerFactory.getLogger( PixivService.class );

    public static
    PixivServiceResult requestPost( MessageReceivedEvent event,
                                    PixivPostLinkRequest request )
    throws IOException, InterruptedException, ExecutionException
    {
        byte[] requestBytes = NatsUtil.getObjectBytes( request );

        Connection nc = NatsConnection.instance();

        PixivPostLinkRequestReply pixivPostLinkRequestReply
                = nc.request( "pixiv.post.request",
                              requestBytes ).handle( ( message, throwable ) -> {
            try {
                return NatsUtil.getMessageObject( message,
                                                  PixivPostLinkRequestReply.class );
            }
            catch ( Exception e ) {
                logger.error( "Error transforming response : {}",
                              e.getMessage() );
                return null;
            }
        } ).get();

        ArrayList<FileDownloadResponse> fileDownloadResponses
                = pixivPostLinkRequestReply.downloadResponses();
        PixivPostMetadata pixivPostMetadata
                = pixivPostLinkRequestReply.metadata();

        boolean nsfwChannel = ChannelUtil.isNsfwChannel( event );

        ArrayList<PixivModeratedFile> moderatedFiles
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

                                                            String
                                                                    base64ImageData
                                                                    = RedisConnection.instance()
                                                                                     .get( fileDownloadMetadata.key() );

                                                            byte[]
                                                                    imageBytes
                                                                    = Base64.getDecoder()
                                                                            .decode( base64ImageData );

                                                            PixivModerationData
                                                                    moderationData
                                                                    = new PixivModerationData( pixivPostMetadata.adult(),
                                                                                               // Do moderation if safe for work
                                                                                               !pixivPostMetadata.adult() && !nsfwChannel
                                                                                               ? requestImageContentModeration( event,
                                                                                                                                fileDownloadMetadata.mimeType(),
                                                                                                                                imageBytes )
                                                                                               : Optional.empty() );

                                                            PixivModeratedFile
                                                                    pixivModeratedFile
                                                                    = new PixivModeratedFile( fileDownloadMetadata,
                                                                                              imageBytes,
                                                                                              moderationData );

                                                            pixivServiceResults.add( pixivModeratedFile );
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
                                       moderatedFiles );
    }

    public
    record PixivServiceResult(
            PixivPostMetadata metadata,
            ArrayList<PixivModeratedFile> moderatedFiles
    ) {}

    public
    record PixivModeratedFile(
            FileDownloadMetadata fileMetadata,
            byte[] imageData,
            PixivModerationData moderation
    )
    {
        public
        record PixivModerationData(
                boolean adult,
                Optional<ContentModerationResponse> contentModerationResponse
        ) {}
    }

    public
    record PixivPostLinkRequestReply(
            ArrayList<FileDownloadResponse> downloadResponses,
            PixivPostMetadata metadata
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
                int likes,
                int hearts,
                String createdAt
        ) {}
    }

}
