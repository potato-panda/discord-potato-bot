package app.potato.bot.commands.slash;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import javax.annotation.Nonnull;

import static app.potato.bot.commands.slash.SlashCommand.AbstractSlashCommand;

@SlashCommand( commandName = "ping", commandDesc = "Pings back" )
public final
class PingCommand extends AbstractSlashCommand {

    public
    PingCommand( String commandName,
                 String commandDesc )
    {
        super(
                commandName,
                commandDesc
        );
    }

    @Nonnull
    @Override
    public
    SlashCommandData commandData() {
        return Commands.slash(
                commandName,
                commandDesc
        );
    }

    @Override
    public
    void execute( SlashCommandInteractionEvent event ) {
        long time = System.currentTimeMillis();
        event.reply( "Pong!" )
             .setEphemeral( true ) // reply or acknowledge
             .flatMap( v -> event.getHook()
                                 .editOriginalFormat(
                                         "Pong: %d ms",
                                         System.currentTimeMillis() - time
                                 )
                       // then edit original
             )
             .queue(); // Queue both reply and edit
    }


}
