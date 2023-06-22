package app.potato.bot.listeners;

import net.dv8tion.jda.api.events.session.ReadyEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

public
class ReadyListener extends Listener {
    private static final Logger logger
            = LoggerFactory.getLogger( ReadyListener.class );

    @Override
    public
    void onReady( @Nonnull ReadyEvent event ) {
        logger.info( "Bot is READY!" );
    }
}
