package app.potato.bot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPooled;

import static app.potato.bot.utils.StringUtil.isNullOrBlank;

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
                String redisName
                        = System.getenv( "REDIS_NAME" );

                _connection = !isNullOrBlank( redisName )
                              ? new JedisPooled( redisName,
                                                 6379 )
                              : new JedisPooled();
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
