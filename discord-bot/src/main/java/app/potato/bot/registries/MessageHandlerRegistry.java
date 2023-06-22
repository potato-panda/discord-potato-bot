package app.potato.bot.registries;

import app.potato.bot.listeners.handlers.MessageHandler;
import app.potato.bot.utils.Disabled;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
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
                new Reflections( "app.potato.bot.listeners.handlers" )
                        .getSubTypesOf( MessageHandler.class )
                        .stream()
                        .reduce( new HashMap<>(),
                                 ( hashMap, aClass ) -> {
                                     try {
                                         if ( aClass.isAnnotationPresent( Disabled.class ) )
                                             return hashMap;
                                         // get matcher field
                                         // instantiate handler
                                         MessageHandler
                                                 instance
                                                 = aClass.getDeclaredConstructor()
                                                         .newInstance();
                                         Field
                                                 declaredField
                                                 = aClass.getField( "key" );

                                         String key
                                                 = (String) declaredField.get( instance );

                                         // map handler to matcher
                                         hashMap.put( key,
                                                      instance );
                                         logger.info( "Message Handler {} registered",
                                                      aClass.getName() );
                                         return hashMap;
                                     }
                                     catch ( NoSuchFieldException | InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e ) {
                                         logger.info( "Message Handler {} is invalid: {}",
                                                      aClass.getName(),
                                                      e.getMessage() );
                                         return hashMap;
                                     }
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
