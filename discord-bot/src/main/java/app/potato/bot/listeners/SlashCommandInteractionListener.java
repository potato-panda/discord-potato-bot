package app.potato.bot.listeners;

import app.potato.bot.commands.slash.SlashCommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static app.potato.bot.registries.SlashCommandsRegistry.getSlashCommands;


public
class SlashCommandInteractionListener extends Listener {

    private static final Logger logger
            = LoggerFactory.getLogger( SlashCommandInteractionListener.class );

    @Override
    public
    void onSlashCommandInteraction( SlashCommandInteractionEvent event ) {
        String commandName = event.getName();
        logger.info( "Slash command \"{}\" called",
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
                event.getHook().sendMessage( "Command could not be found." )
                     .setEphemeral( true )
                     .queue();
            } else {
                logger.info(
                        "Error executing the \"{}\" command",
                        commandName
                );
                event.getHook().sendMessage( "Error executing command \"{}\"." )
                     .setEphemeral( true )
                     .queue();
            }
        }
    }

    private static
    SlashCommand get( String commandName )
    throws NullPointerException
    {
        return getSlashCommands()
                .stream()
                .filter( slashCommand -> slashCommand.commandName.equals( commandName ) )
                .findFirst()
                .orElseThrow( NullPointerException::new );
    }
}
