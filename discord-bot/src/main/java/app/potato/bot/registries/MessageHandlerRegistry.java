package app.potato.bot.registries;

import app.potato.bot.listeners.handlers.MessageHandler;
import app.potato.bot.listeners.handlers.PixivPostLinkMessageHandler;
import app.potato.bot.listeners.handlers.TwitterPostLinkMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;


public final
class MessageHandlerRegistry {
    private static final Logger logger
            = LoggerFactory.getLogger( MessageHandlerRegistry.class );

    private static final MessageHandlerRegistry _instance
            = new MessageHandlerRegistry();

    private final ConcurrentHashMap<String, MessageHandler>
            messageHandlers;

    private
    MessageHandlerRegistry() {
        logger.info( "Registering Message Handlers" );

        messageHandlers
                = new ConcurrentHashMap<>(
                Arrays.stream( new MessageHandler[]{
                              new PixivPostLinkMessageHandler(),
                              new TwitterPostLinkMessageHandler()
                      } )
                      .reduce( new HashMap<>(),
                               ( hashMap, aClass ) -> {
                                   // map handler to matcher
                                   hashMap.put( aClass.key,
                                                aClass );
                                   logger.info( "Message Handler {} registered",
                                                aClass.key );
                                   return hashMap;

                               },
                               ( hashMap, hashMap2 ) -> {
                                   hashMap.putAll( hashMap2 );
                                   return hashMap;
                               } ) );
    }

    public static
    ConcurrentHashMap<String, MessageHandler> getMessageHandlers() {
        return instance().messageHandlers;
    }

    public static synchronized
    MessageHandlerRegistry instance() {
        return _instance;
    }

}
