package app.potato.bot.commands.slash;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.util.concurrent.ExecutionException;

public abstract
class SlashCommand {
    public final String           commandName;
    public final String           commandDesc;
    public final SlashCommandData commandData;
    public       boolean          enabled;

    protected
    SlashCommand( String commandName,
                  String commandDesc )
    {
        this.enabled     = true;
        this.commandName = commandName;
        this.commandDesc = commandDesc;
        this.commandData = Commands.slash( commandName,
                                           commandDesc );
    }

    protected
    SlashCommand( boolean enabled,
                  String commandName,
                  String commandDesc )
    {
        this.enabled     = enabled;
        this.commandName = commandName;
        this.commandDesc = commandDesc;
        this.commandData = Commands.slash( commandName,
                                           commandDesc );
    }

    public abstract
    void execute( SlashCommandInteractionEvent event )
    throws ExecutionException, InterruptedException;

}
