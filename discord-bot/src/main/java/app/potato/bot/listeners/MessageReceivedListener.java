package app.potato.bot.listeners;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

@Listener
public
class MessageReceivedListener extends ListenerAdapter {
    @Override
    public
    void onMessageReceived( MessageReceivedEvent event ) {
        if ( event.getAuthor( )
                  .isBot( ) ) return;
        Message message = event.getMessage( );
        String  content = message.getContentRaw( );

        String sanitizedContent = content.toLowerCase( )
                                         .trim( );
        if ( sanitizedContent.equals( "!ping" ) ) {
            message.reply( "Pong!" )
                   .queue( );
        }
    }

}
