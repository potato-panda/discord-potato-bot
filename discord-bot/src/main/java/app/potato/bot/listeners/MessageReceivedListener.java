package app.potato.bot.listeners;

import app.potato.bot.services.MessageHandlerService;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Map;

import static app.potato.bot.listeners.Listener.AbstractListener;
import static app.potato.bot.listeners.handlers.MessageHandler.AbstractMessageHandler;

@Listener
public
class MessageReceivedListener extends AbstractListener {

    private static final Logger logger
            = LoggerFactory.getLogger( MessageReceivedListener.class );

    private final Map<String, AbstractMessageHandler> handlers
            = MessageHandlerService.getHandlers();

    @Override
    public
    void onMessageReceived( @NotNull MessageReceivedEvent event ) {

        // ignore if author is this bot
        if ( event.getAuthor().isBot() ) return;

        logger.info( "Message received" );

        Message message = event.getMessage();
        String  content = message.getContentRaw();

        String sanitizedContent = content.toLowerCase().trim();

        if ( sanitizedContent.equals( "!ping" ) ) {
            message.reply( "Pong!" ).queue();
        }

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
