package app.potato.bot.clients;

import app.potato.bot.clients.ContentModerationServiceClient.ContentModerationServiceEvaluation.ContentModerationServiceEvaluationBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.model.DetectModerationLabelsResponse;
import software.amazon.awssdk.services.rekognition.model.Image;
import software.amazon.awssdk.services.rekognition.model.ModerationLabel;

import java.util.List;
import java.util.Objects;

public
class AWSRekognitionModerationServiceClient extends ContentModerationServiceClient {
    private final RekognitionClient rekognitionClient;

    public
    AWSRekognitionModerationServiceClient()
    {
        super( AWSRekognitionModerationServiceClient.class );
        Region region = Region.US_EAST_2;

        DefaultCredentialsProvider credentialsProvider
                = DefaultCredentialsProvider.create();

        this.rekognitionClient = RekognitionClient.builder()
                                                  .region( region )
                                                  .credentialsProvider( credentialsProvider )
                                                  .build();

    }

    @Override
    public
    ContentModerationServiceEvaluation doRequest( MessageReceivedEvent event,
                                                  String contentType,
                                                  String contentUrl,
                                                  byte[] contentBytes )
    {
        if ( Objects.equals( contentType,
                             "image/png" )
                || Objects.equals( contentType,
                                   "image/jpeg" ) )
        {
            try {
                DetectModerationLabelsResponse
                        result
                        = rekognitionClient.detectModerationLabels( builder -> {

                    SdkBytes sdkBytes = SdkBytes.fromByteArray( contentBytes );
                    builder.image( Image.builder()
                                        .bytes( sdkBytes ).build() )
                           .minConfidence( 95F );

                } );

                List<ModerationLabel>
                        moderationLabels = result.moderationLabels();
                ContentModerationServiceEvaluationBuilder
                        evaluationBuilder
                        = new ContentModerationServiceEvaluationBuilder();
                moderationLabels.forEach( moderationLabel -> {
                    boolean isAdult = Objects.equals( moderationLabel.name(),
                                                      "Explicit Nudity" );
                    if ( isAdult ) {
                        evaluationBuilder.setExplicitScore( moderationLabel.confidence()
                                                                           .doubleValue() );
                        evaluationBuilder.setIsExplicit( true );
                    }
                    boolean isSuggestive
                            = Objects.equals( moderationLabel.name(),
                                              "Suggestive" );
                    if ( isSuggestive ) {
                        evaluationBuilder.setSuggestiveScore( moderationLabel.confidence()
                                                                             .doubleValue() );
                        evaluationBuilder.setIsSuggestive( true );
                    }

                    if ( isAdult || isSuggestive ) {
                        evaluationBuilder.setResult( true );
                    }
                } );

                ContentModerationServiceEvaluation evaluation
                        = evaluationBuilder.build();

                logger.info( "MODERATION EVALUATION: {}",
                             evaluation );

                return evaluation;
            }
            catch ( AwsServiceException | SdkClientException e ) {
                throw new RuntimeException( e );
            }
        }
        return null;
    }
}
