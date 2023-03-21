package app.potato.bot.commands.slash;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

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
    String commandName();

    String commandDesc() default "no description";

    abstract
    class AbstractSlashCommand {
        @Nonnull
        public final String commandName;

        public final String commandDesc;

        protected
        AbstractSlashCommand( @Nonnull String commandName,
                              String commandDesc )
        {
            this.commandName = commandName;
            this.commandDesc = commandDesc;
        }

        @Nonnull
        public abstract
        SlashCommandData commandData();

        public abstract
        void execute( SlashCommandInteractionEvent event );

    }
}

