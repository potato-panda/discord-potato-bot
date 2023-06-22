package app.potato.bot.commands.slash;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.util.concurrent.ExecutionException;

public abstract
class SlashCommand {
    public final String commandName;

    public final String           commandDesc;
    public final SlashCommandData commandData;

    protected
    SlashCommand( String commandName,
                  String commandDesc,
                  SlashCommandData commandData )
    {
        this.commandName = commandName;
        this.commandDesc = commandDesc;
        this.commandData = commandData;
    }

    public abstract
    void execute( SlashCommandInteractionEvent event )
    throws ExecutionException, InterruptedException;

}
