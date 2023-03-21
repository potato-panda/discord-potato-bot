package app.potato.bot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPooled;

import java.io.IOException;

public
class RedisConnection {
    private static final Logger      logger
                                                 = LoggerFactory.getLogger( RedisConnection.class );
    private static       JedisPooled _connection = null;

    private
    RedisConnection() {}

    public static
    JedisPooled instance() throws IOException, InterruptedException
    {
        if ( _connection == null ) {
            _connection = new JedisPooled();
            logger.info( "Redis Connected" );
        }
        return _connection;
    }
}
