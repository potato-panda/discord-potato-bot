package app.potato.bot;

import io.nats.client.Connection;
import io.nats.client.Nats;
import io.nats.client.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static app.potato.bot.utils.StringUtil.isNullOrBlank;

public
class NatsConnection {
    private static final Logger     logger
                                                = LoggerFactory.getLogger( NatsConnection.class );
    private static       Connection _connection = null;

    private
    NatsConnection() {}

    public static
    Connection instance()
    {
        if ( _connection == null ) {
            try {
                String natsName
                        = System.getenv( "NATS_NAME" );
                String natsUser
                        = System.getenv( "NATS_USER" );
                String natsPass
                        = System.getenv( "NATS_PASS" );
                Options.Builder optionsBuilder = new Options.Builder();
                if ( isNullOrBlank( natsName ) ) {
                    logger.info( "NATS_NAME was not set" );
                } else {
                    optionsBuilder.server( "nats://" + natsName + ":4222" );
                }
                if ( !isNullOrBlank( natsUser ) && !isNullOrBlank( natsPass ) ) {
                    optionsBuilder.userInfo( natsUser,
                                             natsPass );
                }
                _connection = Nats.connect( optionsBuilder.build() );
                logger.info( "NATS Connected" );
            }
            catch ( Exception e ) {
                logger.error( "Nats connection error : {}",
                              e.getMessage() );
            }
        }
        return _connection;
    }
}
