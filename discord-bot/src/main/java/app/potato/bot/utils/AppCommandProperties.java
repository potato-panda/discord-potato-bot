package app.potato.bot.utils;

import javax.annotation.Nonnull;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention( RetentionPolicy.RUNTIME )
@Target( { ElementType.TYPE } )
public
@interface AppCommandProperties {
    @Nonnull
    String commandName();

    String commandDesc() default "no description";

}

