package app.potato.bot.clients;

import app.potato.bot.clients.ContentModerationServiceClient.ContentModerationServiceEvaluation.ContentModerationServiceEvaluationBuilder;
import app.potato.bot.utils.RateLimiter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;

import static app.potato.bot.registries.ContentModerationRegistry.ContentModerationServiceName.ACS_CONTENT_MODERATOR;

public
class ACSContentModeratorModerationServiceClient extends ContentModerationServiceClient {

    private final String      moderationServiceUrl;
    private final RateLimiter rateLimiter;

    private final int contentSizeBinaryLimit = 4194304;

    public
    ACSContentModeratorModerationServiceClient() {
        super( ACSContentModeratorModerationServiceClient.class );
        this.rateLimiter = new RateLimiter( 1,
                                            1_000 );
        this.name        = ACS_CONTENT_MODERATOR;

        this.moderationServiceUrl
                = "https://discordbotcontentmod.cognitiveservices.azure.com/contentmoderator/moderate/v1.0/ProcessImage/Evaluate";
    }

    @Override
    public
    ContentModerationServiceClient.ContentModerationServiceEvaluation doRequest( MessageReceivedEvent event,
                                                                                 String contentType,
                                                                                 String contentUrl,
                                                                                 byte[] contentBytes )
    throws IOException, InterruptedException
    {
        boolean moderatable = !event.getChannel().asTextChannel().isNSFW();
        if ( !moderatable ) return null;

        String apiKey = System.getenv( "ACS_CONTENT_MODERATOR_API_KEY" );
        HttpRequest.Builder builder
                = HttpRequest.newBuilder( URI.create( moderationServiceUrl ) )
                             .header( "CacheImage",
                                      "false" )
                             .header( "Ocp-Apim-Subscription-Key",
                                      apiKey );

        if ( contentUrl != null ) {
            ContentModerationServiceRequestBodyWithUrl
                    requestBody
                    = new ContentModerationServiceRequestBodyWithUrl( this.moderationServiceUrl );

            ObjectWriter objectWriter = new ObjectMapper().writer()
                                                          .withDefaultPrettyPrinter();
            byte[] requestBodyAsBytes
                    = objectWriter.writeValueAsBytes( requestBody );
            builder.header( "Content-Type",
                            "application/json" );
            logger.info( "MODERATION Content-Type: application/json" );
            builder.POST( HttpRequest.BodyPublishers.ofByteArray( requestBodyAsBytes ) );
        } else if ( contentBytes != null && contentType != null ) {
            builder.header( "Content-Type",
                            contentType );
            logger.info( "MODERATION Content-Type: {}",
                         contentType );
            builder.POST( HttpRequest.BodyPublishers.ofByteArray( contentBytes ) );
        }

        HttpRequest httpRequest = builder.build();

        rateLimiter.acquire();

        try {
            String responseBytes = httpClient.send( httpRequest,
                                                    HttpResponse.BodyHandlers.ofString() )
                                             .body();
            try {

                ContentModerationServiceResponse responseBody
                        = new ObjectMapper().registerModule( new Jdk8Module() )
                                            .readValue( responseBytes,
                                                        ContentModerationServiceResponse.class );

                ContentModerationServiceEvaluation evaluation
                        = new ContentModerationServiceEvaluationBuilder()
                        .setExplicitScore( responseBody.adultClassificationScore() * 100 )
                        .setIsExplicit( responseBody.isImageAdultClassified() )
                        .setSuggestiveScore( responseBody.racyClassificationScore() * 100 )
                        .setIsSuggestive( responseBody.isImageRacyClassified() )
                        .setResult( responseBody.result() )
                        .build();

                logger.info( "MODERATION EVALUATION: {}",
                             evaluation );

                return evaluation;
            }
            catch ( JsonProcessingException e ) {
                try {
                    ContentModerationServiceErrorResponse responseBody
                            = new ObjectMapper().registerModule( new Jdk8Module() )
                                                .readValue( responseBytes,
                                                            ContentModerationServiceErrorResponse.class );
                    logger.info( "MODERATION ERROR RESPONSE: {}",
                                 responseBody );

                    return null;
                }
                catch ( JsonProcessingException ex ) {
                    logger.info( "MODERATION ERROR TRANSFORMING: {}",
                                 ex.getMessage() );
                    return null;
                }
            }
        }
        catch ( IOException | InterruptedException e ) {
            throw new RuntimeException( e );
        }
    }

    public
    record ContentModerationServiceErrorResponse(
            @JsonProperty( "Message" ) String message,
            @JsonProperty( "TrackingId" ) String trackingId,
            @JsonProperty( "Errors" ) ContentModerationServiceErrorResponse.ContentModerationError[] errors
    )
    {

        public
        ContentModerationServiceErrorResponse(
                String message,
                String trackingId,
                ContentModerationError[] errors
        )
        {
            this.message    = message;
            this.trackingId = trackingId;
            this.errors     = errors;
        }

        @Override
        public
        String message() {
            return message;
        }

        @Override
        public
        String trackingId() {
            return trackingId;
        }

        @Override
        public
        ContentModerationError[] errors() {
            return errors;
        }


        public
        record ContentModerationError(
                @JsonProperty( "Title" ) String title,
                @JsonProperty( "Message" ) String message
        ) {}
    }

    public
    record ContentModerationServiceResponse(
            @JsonProperty( "AdultClassificationScore" ) Double adultClassificationScore,
            @JsonProperty( "IsImageAdultClassified" ) Boolean isImageAdultClassified,
            @JsonProperty( "RacyClassificationScore" ) Double racyClassificationScore,
            @JsonProperty( "IsImageRacyClassified" ) Boolean isImageRacyClassified,
            @JsonProperty( "Result" ) Boolean result,
            @JsonProperty( "AdvancedInfo" ) ArrayList<AdvancedInfo> advancedInfo,
            @JsonProperty( "Status" ) ContentModerationServiceResponse.Status status,
            @JsonProperty( "TrackingId" ) String trackingId
    )
    {
        public
        record AdvancedInfo(
                @JsonProperty( "Key" ) String key,
                @JsonProperty( "Value" ) String value
        ) {}

        public
        record Status(
                @JsonProperty( "Code" ) int code,
                @JsonProperty( "Description" ) String description,
                @JsonProperty( "Exception" ) Object exception
        ) {}
    }

    private static
    class ContentModerationServiceRequestBodyWithUrl {
        @JsonProperty( "DataRepresentation" ) String dataRepresentation;
        @JsonProperty( "Value" )              String value;

        public
        ContentModerationServiceRequestBodyWithUrl( String url )
        {
            this( "URL",
                  url );
        }

        private
        ContentModerationServiceRequestBodyWithUrl( String dataRepresentation,
                                                    String value )
        {
            this.dataRepresentation = dataRepresentation;
            this.value              = value;
        }
    }

}
