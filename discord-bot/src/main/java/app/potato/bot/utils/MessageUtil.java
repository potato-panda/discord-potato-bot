package app.potato.bot.utils;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public
class MessageUtil {

    public static
    String getSanitizedContent(
            MessageReceivedEvent event
    )
    {
        Message message = event.getMessage();
        String  content = message.getContentRaw();

        return content.toLowerCase().trim();

    }
}
