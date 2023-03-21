package app.potato.bot.listeners;

import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention( RetentionPolicy.RUNTIME )
@Target( { ElementType.TYPE } )
public
@interface Listener {
    class AbstractListener extends ListenerAdapter {
    }
}
