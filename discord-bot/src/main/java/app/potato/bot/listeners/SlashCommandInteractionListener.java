package app.potato.bot.listeners;

import app.potato.bot.commands.slash.SlashCommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static app.potato.bot.services.SlashCommandsService.getSlashCommands;


@Listener
public
class SlashCommandInteractionListener extends ListenerAdapter {

    private static final Logger logger
            = LoggerFactory.getLogger( SlashCommandInteractionListener.class );

    @Override
    public
    void onSlashCommandInteraction( SlashCommandInteractionEvent event ) {
        String commandName = event.getName();
        logger.info( "Slash command {} called",
                     commandName );
        try {
            get( commandName ).execute( event );
        }
        catch ( Exception e ) {
            if ( e instanceof NullPointerException ) {
                logger.info(
                        "Command \"{}\" could not be found",
                        commandName
                );
                event.reply( "Command could not be found." )
                     .setEphemeral( true )
                     .queue();
            } else {
                logger.info(
                        "Error executing the \"{}\" command",
                        commandName
                );
                event.reply( "Error executing command." )
                     .setEphemeral( true )
                     .queue();
            }

        }
    }

    private static
    SlashCommand.AbstractSlashCommand get( String commandName )
    throws NullPointerException
    {
        return getSlashCommands().stream()
                                 .filter( slashCommand -> slashCommand.commandName.equals( commandName ) )
                                 .findFirst()
                                 .orElseThrow( NullPointerException::new );
    }
}
