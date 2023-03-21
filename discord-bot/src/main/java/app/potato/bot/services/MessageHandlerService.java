package app.potato.bot.services;

import app.potato.bot.utils.Disabled;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static app.potato.bot.listeners.handlers.MessageHandler.AbstractMessageHandler;

public
class MessageHandlerService {
    private static final Reflections reflections
            = new Reflections( "app.potato.bot.listeners.handlers" );
    private static final Logger      logger
            = LoggerFactory.getLogger( MessageHandlerService.class );
    private static final Map<String, AbstractMessageHandler>
                                     handlers
            = registerHandlers();

    public static
    Map<String, AbstractMessageHandler> getHandlers() {
        return handlers;
    }

    private static
    Map<String, AbstractMessageHandler> registerHandlers() {
        logger.info( "Registering Message Handlers" );
        HashMap<String, AbstractMessageHandler> handlersMap
                = reflections.getSubTypesOf( AbstractMessageHandler.class )
                             .stream()
                             .reduce( new HashMap<>(),
                                      ( hashMap, aClass ) -> {
                                          try {
                                              if ( aClass.isAnnotationPresent( Disabled.class ) )
                                                  return hashMap;
                                              // get matcher field
                                              // instantiate handler
                                              AbstractMessageHandler
                                                      instance
                                                      = aClass.getDeclaredConstructor()
                                                              .newInstance();
                                              Field declaredField
                                                      = aClass.getField( "key" );
                                              // map handler to matcher
                                              hashMap.put( (String) declaredField.get( instance ),
                                                           instance );
                                              logger.info( "Message Handler {} registered",
                                                           aClass.getName() );
                                              return hashMap;
                                          }
                                          catch ( NoSuchFieldException | InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e ) {
                                              logger.info( "Message Handler {} is an invalid: {}",
                                                           aClass.getName(),
                                                           e.getMessage() );
                                              return hashMap;
                                          }
                                      },
                                      ( hashMap, hashMap2 ) -> {
                                          hashMap.putAll( hashMap2 );
                                          return hashMap;
                                      } );

        return Collections.unmodifiableMap( handlersMap );
    }

}
