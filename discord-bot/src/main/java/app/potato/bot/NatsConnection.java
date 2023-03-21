package app.potato.bot;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import io.nats.client.Connection;
import io.nats.client.Message;
import io.nats.client.Nats;
import io.nats.client.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public
class NatsConnection {
    private static final Logger     logger
                                                = LoggerFactory.getLogger( NatsConnection.class );
    private static       Connection _connection = null;

    private
    NatsConnection() {}

    public static
    CompletableFuture<Message> request( String key,
                                        Object object )
    throws IOException, InterruptedException
    {
        ObjectWriter objectWriter
                = new ObjectMapper().writer()
                                    .withDefaultPrettyPrinter();
        byte[] data = objectWriter.writeValueAsBytes( object );
        return instance().request( key,
                                   data );
    }

    public static
    Connection instance() throws IOException, InterruptedException
    {
        if ( _connection == null ) {
            _connection = Nats.connect( Options.DEFAULT_URL );
            logger.info( "NATS Connected" );
        }
        return _connection;
    }
}
