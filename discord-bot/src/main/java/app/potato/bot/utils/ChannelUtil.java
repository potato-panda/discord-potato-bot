package app.potato.bot.utils;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public
class ChannelUtil {
    public static
    boolean isNsfwChannel( MessageReceivedEvent event ) {
        return event.getChannel().asTextChannel().isNSFW();
    }
}
