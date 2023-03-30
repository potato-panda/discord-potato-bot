package app.potato.bot;

import io.nats.client.Connection;
import io.nats.client.Nats;
import io.nats.client.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

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
                Optional<String> natsName
                        = Optional.ofNullable( System.getenv( "NATS_NAME" ) );
                Optional<String> natsUser
                        = Optional.ofNullable( System.getenv( "NATS_USER" ) );
                Optional<String> natsPass
                        = Optional.ofNullable( System.getenv( "NATS_PASS" ) );
                Options.Builder optionsBuilder = new Options.Builder();
                if ( natsName.isEmpty() ) {
                    logger.info( "NATS_NAME was not set" );
                } else {
                    optionsBuilder.server( "nats://" + natsName.get() + ":4222" );
                }
                if ( natsUser.isPresent() && natsPass.isPresent() ) {
                    optionsBuilder.userInfo( natsUser.get(),
                                             natsPass.get() );
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
