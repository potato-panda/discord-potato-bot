package app.potato.bot.registries;

import app.potato.bot.clients.ACSContentModeratorModerationServiceClient;
import app.potato.bot.clients.AWSRekognitionModerationServiceClient;
import app.potato.bot.clients.ContentModerationServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static app.potato.bot.registries.ContentModerationRegistry.ContentModerationServiceName.ACS_CONTENT_MODERATOR;
import static app.potato.bot.registries.ContentModerationRegistry.ContentModerationServiceName.AWS_REKOGNITION;

public final
class ContentModerationRegistry {
    private static final Logger                    logger
            = LoggerFactory.getLogger( ContentModerationRegistry.class );
    private static final ContentModerationRegistry _instance;

    static {
        try {
            _instance = new ContentModerationRegistry();
        }
        catch ( InterruptedException e ) {
            throw new RuntimeException( e );
        }
    }

    private final ConcurrentHashMap<ContentModerationServiceName, ContentModerationServiceClient>
            contentModerationServices;

    private
    ContentModerationRegistry() throws InterruptedException {
        logger.info( "" );

        contentModerationServices
                = new ConcurrentHashMap<>();

        ACSContentModeratorModerationServiceClient azureContentModerationService
                = new ACSContentModeratorModerationServiceClient();

        contentModerationServices.put( ACS_CONTENT_MODERATOR,
                                       azureContentModerationService );

        AWSRekognitionModerationServiceClient
                awsRekognitionModerationServiceClient
                = new AWSRekognitionModerationServiceClient();

        contentModerationServices.put( AWS_REKOGNITION,
                                       awsRekognitionModerationServiceClient );
    }

    public static
    ConcurrentHashMap<ContentModerationServiceName, ContentModerationServiceClient> getContentModerationServices() {
        return instance().contentModerationServices;
    }

    public static synchronized
    ContentModerationRegistry instance() {
        return _instance;
    }

    public static final
    class
    ContentModerationServiceName {
        public static final ContentModerationServiceName ACS_CONTENT_MODERATOR
                = ContentModerationServiceName.of( "acs_content_moderator" );
        public static final ContentModerationServiceName AWS_REKOGNITION
                = ContentModerationServiceName.of( "aws_rekognition" );

        private static final List<ContentModerationServiceName>
                                    CONTENT_MODERATION_SERVICE_NAMES
                = Collections.unmodifiableList( Arrays.asList( ACS_CONTENT_MODERATOR,
                                                               AWS_REKOGNITION ) );
        private final        String name;

        private
        ContentModerationServiceName( String name ) {
            this.name = name;
        }

        public static
        List<ContentModerationServiceName> names() {return CONTENT_MODERATION_SERVICE_NAMES;}

        public static
        ContentModerationServiceName of( String value ) {
            return ContentModerationServiceNameCache.put( value );
        }

        public
        String name() {
            return name;
        }

        @Override
        public
        String toString() {
            return name;
        }

        private static
        class ContentModerationServiceNameCache {
            private static final
            ConcurrentHashMap<String, ContentModerationServiceName> VALUES
                    = new ConcurrentHashMap<>();

            private
            ContentModerationServiceNameCache() {}

            private static
            ContentModerationServiceName put( String value ) {
                return VALUES.computeIfAbsent( value,
                                               v -> new ContentModerationServiceName( value ) );
            }

        }
    }
}
