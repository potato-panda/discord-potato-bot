package app.potato.bot.utils;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public final
class MessageUtil {

    public static
    String getSanitizedMessageTextContent( MessageReceivedEvent event ) {
        return event.getMessage()
                    .getContentRaw()
                    .trim();
    }
}
