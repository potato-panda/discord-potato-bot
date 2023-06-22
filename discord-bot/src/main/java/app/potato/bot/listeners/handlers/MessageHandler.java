package app.potato.bot.listeners.handlers;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.io.IOException;
import java.text.ParseException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public abstract
class MessageHandler {

    public final String key;

    protected
    MessageHandler( String key ) {
        this.key = key;
    }

    public abstract
    void handle( MessageReceivedEvent event ) throws
                                              IOException,
                                              InterruptedException,
                                              ExecutionException,
                                              TimeoutException,
                                              ParseException;

}
