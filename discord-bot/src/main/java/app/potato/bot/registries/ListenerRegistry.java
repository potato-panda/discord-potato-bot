package app.potato.bot.registries;

import app.potato.bot.listeners.Listener;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final
class ListenerRegistry {
    private static final Logger           logger
                                                    = LoggerFactory.getLogger( ListenerRegistry.class );
    private static final ListenerRegistry _instance = new ListenerRegistry();

    private final CopyOnWriteArrayList<ListenerAdapter> listeners;

    private
    ListenerRegistry() {
        listeners
                = new CopyOnWriteArrayList<>( new Reflections( "app.potato.bot.listeners" ).getSubTypesOf( Listener.class )
                                                                                           .stream()
                                                                                           .map( aClass -> {
                                                                                               try {
                                                                                                   ListenerAdapter
                                                                                                           inst
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
                                                                                           .toList() );
    }

    public static
    List<? extends ListenerAdapter> getListeners() {
        return instance().listeners;
    }

    public static synchronized
    ListenerRegistry instance() {
        return _instance;
    }

}
