package app.potato.bot.utils;

import java.util.Date;
import java.util.Objects;

import static app.potato.bot.utils.TimeConstants.DAY;

public
class TimestampUtil {
    public
    String getDiscordTimestamp( Long timeInSeconds ) {
        Objects.requireNonNull( timeInSeconds );
        long lTime      = new Date( timeInSeconds ).getTime();
        long oneDayAgo  = new Date().getTime() - DAY;
        long twoDaysAgo = new Date().getTime() - DAY * 2;
        if ( lTime <= twoDaysAgo ) {
            return "<t:" + lTime + ":d> <t:" + lTime + ":t>";
        } else if ( lTime <= oneDayAgo )
            return "Yesterday at <t:" + lTime + ":t>";
        else return "Today at <t:" + lTime + ":t>";
    }
}
