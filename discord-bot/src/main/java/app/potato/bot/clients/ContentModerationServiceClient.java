package app.potato.bot.clients;

import app.potato.bot.registries.ContentModerationRegistry;
import app.potato.bot.registries.ContentModerationRegistry.ContentModerationServiceName;
import app.potato.bot.utils.FileDownloadResponse.FileDownloadMetadata;
import app.potato.bot.utils.ImageCompressor;
import app.potato.bot.utils.ImageScaler;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.http.HttpClient;
import java.util.ArrayList;
import java.util.Objects;

public abstract
class ContentModerationServiceClient {
    public final HttpClient                   httpClient;
    public final Logger                       logger;
    public       ContentModerationServiceName name;

    public
    ContentModerationServiceClient( Class<?> aClass )
    {
        this.httpClient = HttpClient.newHttpClient();
        this.logger     = LoggerFactory.getLogger( aClass );
    }

    public
    ContentModerationServiceEvaluation requestImageContentModeration( MessageReceivedEvent event,
                                                                      String contentType,
                                                                      byte[] contentBytes )
    throws IOException, InterruptedException
    {
        Objects.requireNonNull( contentType,
                                "Content Type can not be null" );
        Objects.requireNonNull( contentBytes,
                                "Content Bytes can not be null" );
        return doRequest( event,
                          contentType,
                          null,
                          contentBytes );
    }

    public abstract
    ContentModerationServiceEvaluation doRequest( MessageReceivedEvent event,
                                                  String contentType,
                                                  String contentUrl,
                                                  byte[] contentBytes )
    throws IOException, InterruptedException;

    public static final
    class ContentModerationData {

        private final boolean isExplicit;
        private       ArrayList<ContentModerationServiceEvaluation>
                              contentModerationServiceEvaluations;

        public
        ContentModerationData( MessageReceivedEvent event,
                               boolean isExplicit,
                               FileDownloadMetadata
                                       fileDownloadMetadata,
                               byte[] contentBytes ) throws IOException
        {
            Logger logger
                    = LoggerFactory.getLogger( ContentModerationData.class );

            boolean isNsfwChannel = event.getChannel().asTextChannel().isNSFW();
            this.isExplicit = isExplicit;

            logger.info( "contentBytes: {}",
                         contentBytes.length );

            byte[] reducedBytes = getReducedBytes( fileDownloadMetadata,
                                                   contentBytes
            );
            logger.info( "reducedBytes: {}",
                         reducedBytes.length );

            boolean isModerationRequired = !isExplicit && !isNsfwChannel;

            ArrayList<ContentModerationServiceEvaluation> evaluations
                    = new ArrayList<>();

            if ( isModerationRequired ) {
                try {
                    ACSContentModeratorModerationServiceClient
                            azureContentModerationService
                            = (ACSContentModeratorModerationServiceClient) ContentModerationRegistry.getContentModerationServices()
                                                                                                    .get( ContentModerationServiceName.ACS_CONTENT_MODERATOR );
                    ContentModerationServiceEvaluation response;

                    try {
                        response
                                = azureContentModerationService.requestImageContentModeration( event,
                                                                                               fileDownloadMetadata.mimeType(),
                                                                                               reducedBytes );
                    }
                    catch ( IOException | InterruptedException e ) {
                        logger.info( e.getMessage() );
                        response = null;
                    }

                    if ( response != null ) {
                        evaluations.add( response );
                        if ( response.result ) {
                            isModerationRequired = false;
                        }
                    }

                    if ( isModerationRequired ) {
                        AWSRekognitionModerationServiceClient
                                awsRekognitionModerationServiceClient
                                = (AWSRekognitionModerationServiceClient) ContentModerationRegistry.getContentModerationServices()
                                                                                                   .get( ContentModerationServiceName.AWS_REKOGNITION );
                        try {
                            response
                                    = awsRekognitionModerationServiceClient.requestImageContentModeration( event,
                                                                                                           fileDownloadMetadata.mimeType(),
                                                                                                           reducedBytes );
                        }
                        catch ( IOException | InterruptedException e ) {
                            logger.info( e.getMessage() );
                            response = null;
                        }

                        if ( response != null ) {
                            evaluations.add( response );
                        }
                    }
                    this.contentModerationServiceEvaluations = evaluations;
                }
                catch ( Exception e ) {
                    this.contentModerationServiceEvaluations = null;
                }
            } else {
                this.contentModerationServiceEvaluations = null;
            }
        }

        private static
        byte[] getReducedBytes( FileDownloadMetadata fileDownloadMetadata,
                                byte[] contentBytes ) throws IOException
        {
            byte[]    reducedBytes           = contentBytes;
            final int contentSizeBinaryLimit = 4194304;

            if ( fileDownloadMetadata.mimeType().matches( "^image/.*" ) ) {
                if ( reducedBytes.length > contentSizeBinaryLimit ) {
                    byte[] scaledImageBytes = new ImageScaler( reducedBytes,
                                                               fileDownloadMetadata.fileExtension() ).getScaled();
                    if ( scaledImageBytes.length <= contentBytes.length ) {
                        reducedBytes = scaledImageBytes;
                    }
                }
                if ( reducedBytes.length > contentSizeBinaryLimit ) {
                    byte[] compressedImageBytes
                            = new ImageCompressor( reducedBytes,
                                                   fileDownloadMetadata.fileExtension() ).getCompressed();
                    if ( compressedImageBytes.length <= contentBytes.length ) {
                        reducedBytes = compressedImageBytes;
                    }
                }

            }
            return reducedBytes;
        }

        public
        boolean isExplicit() {return isExplicit;}

        public
        ArrayList<ContentModerationServiceEvaluation> contentModerationResponses() {return contentModerationServiceEvaluations;}

    }

    public
    record ModeratedContent(
            FileDownloadMetadata metadata,
            ContentModerationData moderationData,
            byte[] data
    ) {}

    public
    record ContentModerationServiceEvaluation(
            Double explicitScore,
            Boolean isExplicit,
            Double suggestiveScore,
            Boolean isSuggestive,
            Boolean result
    )
    {


        public static
        class ContentModerationServiceEvaluationBuilder {
            private Double  explicitScore;
            private Boolean isExplicit;
            private Double  suggestiveScore;
            private Boolean isSuggestive;
            private Boolean result;

            public
            ContentModerationServiceEvaluationBuilder setExplicitScore( Double explicitScore ) {
                this.explicitScore = explicitScore;
                return this;
            }

            public
            ContentModerationServiceEvaluationBuilder setIsExplicit( Boolean isExplicit ) {
                this.isExplicit = isExplicit;
                return this;
            }

            public
            ContentModerationServiceEvaluationBuilder setSuggestiveScore( Double suggestiveScore ) {
                this.suggestiveScore = suggestiveScore;
                return this;
            }

            public
            ContentModerationServiceEvaluationBuilder setIsSuggestive( Boolean isSuggestive ) {
                this.isSuggestive = isSuggestive;
                return this;
            }

            public
            ContentModerationServiceEvaluationBuilder setResult( Boolean result ) {
                this.result = result;
                return this;
            }

            public
            ContentModerationServiceEvaluation build() {
                return new ContentModerationServiceEvaluation( explicitScore,
                                                               isExplicit,
                                                               suggestiveScore,
                                                               isSuggestive,
                                                               result );
            }
        }
    }
}
