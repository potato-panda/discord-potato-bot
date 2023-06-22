package app.potato.bot.listeners;

import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

public
class MessageUpdatedListener extends Listener {
    private static final Logger logger
            = LoggerFactory.getLogger( MessageUpdatedListener.class );

    @Override
    public
    void onMessageUpdate( @Nonnull MessageUpdateEvent event ) {
    }
}
