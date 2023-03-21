package app.potato.bot.utils;

import app.potato.bot.listeners.Listener;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

public
class ListenerUtil {
    private static final Reflections reflections
            = new Reflections( "app.potato.bot.listeners" );

    private static final Logger logger
            = LoggerFactory.getLogger( ListenerUtil.class );

    public static
    Object[] getListenersAsArray() {
        return reflections.getSubTypesOf( ListenerAdapter.class )
                          .stream()
                          .filter( aClass -> Arrays.stream( aClass.getAnnotations() )
                                                   .anyMatch( annotation -> annotation.annotationType()
                                                                                      .equals( Listener.class ) ) )
                          .map( aClass -> {
                              try {
                                  ListenerAdapter inst
                                          = aClass.getDeclaredConstructor()
                                                  .newInstance();
                                  logger.info( "{} registered",
                                               aClass.getName() );
                                  return inst;
                              }
                              catch ( InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e ) {
                                  throw new RuntimeException( e );
                              }
                          } )
                          .distinct()
                          .toArray();
    }

}
