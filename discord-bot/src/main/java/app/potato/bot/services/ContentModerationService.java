package app.potato.bot.services;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;

public
class ContentModerationService {

    private static final Logger logger
            = LoggerFactory.getLogger( ContentModerationService.class );

    private static final String      moderationUrl
                                                 = "https://discordbotcontentmod.cognitiveservices.azure.com/contentmoderator/moderate/v1.0/ProcessImage/Evaluate";
    private static final HttpClient  httpClient  = HttpClient.newHttpClient();
    private static       RateLimiter rateLimiter = null;

    static {
        try {
            rateLimiter = new RateLimiter( 1,
                                           1_000 );
        }
        catch ( InterruptedException e ) {
            logger.info( "Rate Limiter exception : {}",
                         e.getMessage() );
        }
    }

    public static
    Optional<ContentModerationResponse> requestImageContentModeration( MessageReceivedEvent event,
                                                                       String imageUrl )
    throws IOException, InterruptedException
    {
        Objects.requireNonNull( imageUrl,
                                "ImageUrl can not be null" );
        return doRequest( event,
                          null,
                          imageUrl,
                          null );
    }

    private static
    Optional<ContentModerationResponse> doRequest( MessageReceivedEvent event,
                                                   String contentType,
                                                   String imageUrl,
                                                   byte[] imageBytes )
    throws IOException, InterruptedException
    {
        // check if moderatable
        boolean moderatable = !event.getChannel().asTextChannel().isNSFW();
        if ( !moderatable ) return Optional.empty();

        HttpRequest.Builder builder
                = HttpRequest.newBuilder( URI.create( moderationUrl ) )
                             .header( "CacheImage",
                                      "false" )
                             .header( "Ocp-Apim-Subscription-Key",
                                      System.getenv( "CONTENT_MOD_KEY" ) );

        if ( imageUrl != null ) {
            byte[] requestBodyAsBytes = getRequestBodyAsBytes( imageUrl );
            builder.header( "Content-Type",
                            "application/json" );
            builder.POST( HttpRequest.BodyPublishers.ofByteArray( requestBodyAsBytes ) );
        } else if ( imageBytes != null && contentType != null ) {
            builder.header( "Content-Type",
                            contentType );
            builder.POST( HttpRequest.BodyPublishers.ofByteArray( imageBytes ) );
        }

        HttpRequest httpRequest = builder.build();

        rateLimiter.acquire();

        String responseBytes = httpClient.send( httpRequest,
                                                HttpResponse.BodyHandlers.ofString() )
                                         .body();

        ContentModerationResponse responseBody
                = new ObjectMapper().registerModule( new Jdk8Module() )
                                    .readValue( responseBytes,
                                                ContentModerationResponse.class );
        logger.info( "MODERATION RESPONSE: {}",
                     responseBody );

        return Optional.ofNullable( responseBody );
    }

    private static
    byte[] getRequestBodyAsBytes( String imageUrl )
    throws JsonProcessingException
    {
        ContentModerationRequestBodyWithUrl requestBody
                = new ContentModerationRequestBodyWithUrl( imageUrl );

        ObjectWriter objectWriter
                = new ObjectMapper().writer()
                                    .withDefaultPrettyPrinter();

        return objectWriter.writeValueAsBytes( requestBody );
    }

    public static
    Optional<ContentModerationResponse> requestImageContentModeration( MessageReceivedEvent event,
                                                                       String contentType,
                                                                       byte[] imageBytes )
    throws IOException, InterruptedException
    {
        Objects.requireNonNull( contentType,
                                "ContentType can not be null" );
        Objects.requireNonNull( imageBytes,
                                "ImageBytes can not be null" );
        return doRequest( event,
                          contentType,
                          null,
                          imageBytes );
    }

    public
    record ContentModerationResponse(
            @JsonProperty( "AdultClassificationScore" ) Optional<Double> adultClassificationScore,
            @JsonProperty( "IsImageAdultClassified" ) Optional<Boolean> isImageAdultClassified,
            @JsonProperty( "RacyClassificationScore" ) Optional<Double> racyClassificationScore,
            @JsonProperty( "IsImageRacyClassified" ) Optional<Boolean> isImageRacyClassified,
            @JsonProperty( "Result" ) Optional<Boolean> result,
            @JsonProperty( "AdvancedInfo" ) Optional<ArrayList<AdvancedInfo>> advancedInfo,
            @JsonProperty( "Status" ) Optional<Status> status,
            @JsonProperty( "TrackingId" ) Optional<String> trackingId,
            Optional<ContentModerationError> error
    )
    {

        public
        record AdvancedInfo(
                @JsonProperty( "Key" ) String key,
                @JsonProperty( "Value" ) String value
        )
        {}

        public
        record Status(
                @JsonProperty( "Code" ) int code,
                @JsonProperty( "Description" ) String description,
                @JsonProperty( "Exception" ) Object exception
        )
        {}

        public
        record ContentModerationError(
                String code,
                String message
        ) {}
    }

    public static
    class ContentModerationRequestBodyWithUrl {
        @JsonProperty( "DataRepresentation" )
        String dataRepresentation;
        @JsonProperty( "Value" )
        String value;

        public
        ContentModerationRequestBodyWithUrl( String url )
        {
            this( "URL",
                  url );
        }

        private
        ContentModerationRequestBodyWithUrl(
                String dataRepresentation,
                String value
        )
        {
            this.dataRepresentation = dataRepresentation;
            this.value              = value;
        }
    }
}
