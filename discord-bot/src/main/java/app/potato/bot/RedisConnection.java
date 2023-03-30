package app.potato.bot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPooled;

import java.util.Optional;

public
class RedisConnection {
    private static final Logger      logger
                                                 = LoggerFactory.getLogger( RedisConnection.class );
    private static       JedisPooled _connection = null;

    private
    RedisConnection() {}

    public static
    JedisPooled instance()
    {
        if ( _connection == null ) {
            try {
                Optional<String> redisName
                        = Optional.ofNullable( System.getenv( "REDIS_NAME" ) );
                _connection = redisName.map( s -> new JedisPooled( s,
                                                                   6379 ) )
                                       .orElseGet( JedisPooled::new );
                logger.info( "Redis Connected" );
            }
            catch ( Exception e ) {
                logger.error( "Redis connection error : {}",
                              e.getMessage() );
            }
        }
        return _connection;
    }
}
