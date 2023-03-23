package app.potato.bot;

import com.mongodb.client.MongoClients;
import dev.morphia.Datastore;
import dev.morphia.Morphia;
import dev.morphia.mapping.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
                Datastore datastore
                        = Morphia.createDatastore( MongoClients.create(),
                                                   "bot-app" );
                Mapper mapper = datastore.getMapper();
                mapper.mapPackage( "app.potato.bot.models" );

                datastore.ensureIndexes();

                _connection = datastore;
            }
            catch ( Exception e ) {
                logger.info( "MongoDB connection error : {}",
                             e.getMessage() );
            }
        }
        return _connection;
    }
}
