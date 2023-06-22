package app.potato.bot.listeners;

import app.potato.bot.listeners.handlers.MessageHandler;
import app.potato.bot.registries.MessageHandlerRegistry;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

public
class MessageReceivedListener extends Listener {

    private static final Logger logger
            = LoggerFactory.getLogger( MessageReceivedListener.class );

    @Override
    public
    void onMessageReceived( @Nonnull MessageReceivedEvent event ) {

        // ignore if author is this bot
        if ( event.getAuthor().isBot() ) return;

        logger.info( "Message received" );

        Message message = event.getMessage();
        String  content = message.getContentRaw();

        String sanitizedContent = content.toLowerCase().trim();

        if ( sanitizedContent.equals( "!ping" ) ) {
            message.reply( "Pong!" ).queue();
        }

        ConcurrentHashMap<String, MessageHandler>
                handlers = MessageHandlerRegistry.getMessageHandlers();

        // if content contains a matching handler
        for ( String key : handlers.keySet() ) {

            // match patterns here
            String firstElement
                    = Arrays.stream( sanitizedContent.split( "\\s+" ) )
                            .findFirst()
                            .orElse( "" );

            // Match Handlers if Https link
            if ( firstElement.contains( "https://" ) ) {
                URI url = null;
                try {
                    url = new URI( firstElement );
                }
                catch ( URISyntaxException e ) {
                    logger.info( "Not a valid url: {}",
                                 firstElement );
                }
                assert url != null;
                if ( url.getHost() != null && url.getHost()
                                                 .contains( key ) )
                {
                    new Thread( () -> {
                        try {
                            handlers.get( key ).handle( event );
                        }
                        catch ( Exception e ) {
                            logger.info( "Error handling {} : {}",
                                         key,
                                         e.getMessage() );
                        }
                    } ).start();
                }
            }

        }
    }

}
