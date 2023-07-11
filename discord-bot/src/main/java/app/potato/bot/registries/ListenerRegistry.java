package app.potato.bot.registries;

import app.potato.bot.listeners.MessageReceivedListener;
import app.potato.bot.listeners.ReadyListener;
import app.potato.bot.listeners.SlashCommandInteractionListener;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public final
class ListenerRegistry {
    private static final Logger           logger
                                                    = LoggerFactory.getLogger( ListenerRegistry.class );
    private static final ListenerRegistry _instance = new ListenerRegistry();

    private final CopyOnWriteArrayList<ListenerAdapter> listeners;

    private
    ListenerRegistry() {
        listeners = Arrays.stream( new ListenerAdapter[]{
                new MessageReceivedListener(),
                new ReadyListener(),
                new SlashCommandInteractionListener()
        } ).peek( aClass -> {
            logger.info( "{} registered",
                         aClass.getClass().getName() );
        } ).collect( Collectors.toCollection( CopyOnWriteArrayList::new ) );
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
