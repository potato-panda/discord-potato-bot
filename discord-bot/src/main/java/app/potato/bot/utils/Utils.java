package app.potato.bot.utils;

import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.reflections.Reflections;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public
class Utils {
    static Reflections
            reflections
            = new Reflections( "app.potato.bot.listeners" );

    public static
    Object[] getListenersAsArray( ) {
        return reflections.getSubTypesOf( ListenerAdapter.class )
                          .stream( )
                          .map( aClass -> {
                              try {
                                  return aClass.getDeclaredConstructor( )
                                               .newInstance( );
                              }
                              catch ( InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e ) {
                                  throw new RuntimeException( e );
                              }
                          } )
                          .distinct( )
                          .toArray( );
    }

}
