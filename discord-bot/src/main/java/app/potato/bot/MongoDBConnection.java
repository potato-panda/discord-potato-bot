package app.potato.bot;

import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import dev.morphia.Datastore;
import dev.morphia.Morphia;
import dev.morphia.mapping.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static app.potato.bot.utils.StringUtil.isNullOrBlank;

public
class MongoDBConnection {

    public static final Logger    logger
                                              = LoggerFactory.getLogger( MongoDBConnection.class );
    private static      Datastore _connection = null;

    private static GridFSBucket _bucket = null;

    private
    MongoDBConnection() {}

    public static
    GridFSBucket bucket()
    {
        if ( _bucket == null ) {
            try {
                Datastore instance = instance();

                GridFSBucket bucket
                        = GridFSBuckets.create( instance.getDatabase() );
                _bucket = bucket;
            }
            catch ( Exception e ) {
                logger.info( "Mongo bucket error : {}",
                             e.getMessage() );
            }
        }
        return _bucket;
    }

    public static
    Datastore instance() {
        if ( _connection == null ) {
            try {
                String mongoName
                        = System.getenv( "MONGO_NAME" );
                String mongoUser
                        = System.getenv( "MONGO_INITDB_ROOT_USERNAME" );
                String mongoPass
                        = System.getenv( "MONGO_INITDB_ROOT_PASSWORD" );
                MongoClientSettings.Builder settingsBuilder
                        = MongoClientSettings.builder();
                if ( isNullOrBlank( mongoName ) ) {
                    logger.info( "MONGO_NAME was not set" );
                    settingsBuilder.applyToClusterSettings( builder -> {
                        builder.hosts( List.of( new ServerAddress() ) );
                    } );
                } else {
                    settingsBuilder.applyToClusterSettings( builder -> {
                        builder.hosts( List.of( new ServerAddress( mongoName ) ) );
                    } );
                }
                if ( !isNullOrBlank( mongoUser ) && !isNullOrBlank( mongoPass ) ) {
                    MongoCredential credentials
                            = MongoCredential.createCredential( mongoUser,
                                                                "admin",
                                                                mongoPass
                                                                        .toCharArray() );
                    settingsBuilder.credential( credentials );
                }
                MongoClient mongoClient
                        = MongoClients.create( settingsBuilder.build() );
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
