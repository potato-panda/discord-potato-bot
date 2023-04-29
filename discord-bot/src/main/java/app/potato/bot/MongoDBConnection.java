package app.potato.bot;

import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import dev.morphia.Datastore;
import dev.morphia.Morphia;
import dev.morphia.mapping.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public
class MongoDBConnection {

    public static final Logger    logger
                                              = LoggerFactory.getLogger( MongoDBConnection.class );
    private static      Datastore _connection = null;

    private
    MongoDBConnection() {}

    public static
    Datastore instance() {
        if ( _connection == null ) {
            try {
                Optional<String> mongoName
                        = Optional.ofNullable( System.getenv( "MONGO_NAME" ) );
                Optional<String> mongoUser
                        = Optional.ofNullable( System.getenv( "MONGO_INITDB_ROOT_USERNAME" ) );
                Optional<String> mongoPass
                        = Optional.ofNullable( System.getenv( "MONGO_INITDB_ROOT_PASSWORD" ) );
                MongoClientSettings.Builder settingsBuilder
                        = MongoClientSettings.builder();
                MongoClient mongoClient;
                if ( mongoName.isEmpty() ) {
                    logger.info( "MONGO_NAME was not set" );
                    settingsBuilder.applyToClusterSettings( builder -> {
                        builder.hosts( List.of( new ServerAddress() ) );
                    } );
                } else {
                    settingsBuilder.applyToClusterSettings( builder -> {
                        builder.hosts( List.of( new ServerAddress( mongoName.get() ) ) );
                    } );
                }
                if ( mongoUser.isPresent() && mongoPass.isPresent() ) {
                    MongoCredential credentials
                            = MongoCredential.createCredential( mongoUser.get(),
                                                                "admin",
                                                                mongoPass.get()
                                                                         .toCharArray() );
                    settingsBuilder.credential( credentials );
                }
                mongoClient = MongoClients.create( settingsBuilder.build() );
                Datastore datastore = Morphia.createDatastore( mongoClient,
                                                               "bot-app" );
                Mapper mapper = datastore.getMapper();
                mapper.mapPackage( "app.potato.bot.models" );

                datastore.ensureIndexes();

                _connection = datastore;
            }
            catch ( Exception e ) {
                logger.info( "Mongo connection error : {}",
                             e.getMessage() );
            }
        }
        return _connection;
    }
}
