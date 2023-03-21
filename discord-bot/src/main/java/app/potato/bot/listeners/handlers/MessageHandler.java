package app.potato.bot.listeners.handlers;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

@Retention( RetentionPolicy.RUNTIME )
@Target( { ElementType.TYPE } )
public
@interface MessageHandler {
    abstract
    class AbstractMessageHandler {

        public String key;

        protected
        AbstractMessageHandler( String key ) {
            this.key = key;
        }

        public abstract
        void handle( MessageReceivedEvent event ) throws
                                                  IOException,
                                                  InterruptedException,
                                                  ExecutionException,
                                                  TimeoutException;
    }
}
