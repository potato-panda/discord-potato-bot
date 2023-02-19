package app.potato.bot.commands.slash;

import javax.annotation.Nonnull;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention( RetentionPolicy.RUNTIME )
@Target( { ElementType.TYPE } )
public
@interface SlashCommand {
    @Nonnull
    String commandName( );

    String commandDesc( ) default "no description";
}

