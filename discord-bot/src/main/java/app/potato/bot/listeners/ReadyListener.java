package app.potato.bot.listeners;

import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Listener
public
class ReadyListener extends ListenerAdapter {
    private static final Logger
            logger
            = LoggerFactory.getLogger( ReadyListener.class );

    @Override
    public
    void onReady( @NotNull ReadyEvent event ) {
        logger.info( "Bot is READY!" );
    }
}
