package app.potato.bot.listeners;

import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static app.potato.bot.listeners.Listener.AbstractListener;

@Listener
public
class MessageUpdatedListener extends AbstractListener {
    private static final Logger logger
            = LoggerFactory.getLogger( MessageUpdatedListener.class );

    @Override
    public
    void onMessageUpdate( @NotNull MessageUpdateEvent event ) {
    }
}
