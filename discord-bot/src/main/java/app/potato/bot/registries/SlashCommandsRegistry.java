package app.potato.bot.registries;


import app.potato.bot.commands.slash.BanCommand;
import app.potato.bot.commands.slash.SlashCommand;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final
class SlashCommandsRegistry {

    private static final Logger                             logger
                                                                      = LoggerFactory.getLogger( SlashCommandsRegistry.class );
    private static final SlashCommandsRegistry              _instance
                                                                      = new SlashCommandsRegistry();
    private final        CopyOnWriteArrayList<SlashCommand> slashCommands;

    public
    SlashCommandsRegistry() {
        slashCommands
                = new CopyOnWriteArrayList<>( Arrays.stream( new SlashCommand[]{
                new BanCommand( false )
        } ).toList() );
    }

    public static
    void setGuildSlashCommands( Guild guild ) {

        guild.updateCommands().addCommands( getSlashCommandsData() ).queue();

        logger.info( "Set Guild Slash Commands for {}: {}",
                     guild.getName(),
                     getSlashCommandsData().size() );
    }

    public static
    List<SlashCommandData> getSlashCommandsData() {
        return getSlashCommands().stream()
                                 .map( slashCommand -> slashCommand.commandData )
                                 .toList();
    }

    public static
    CopyOnWriteArrayList<SlashCommand> getSlashCommands() {
        return instance().slashCommands;
    }

    public static synchronized
    SlashCommandsRegistry instance() {
        return _instance;
    }

    public static
    void setGlobalSlashCommands( JDA jda ) {

        jda.updateCommands().addCommands( getSlashCommandsData() ).queue();

        logger.info( "Set Global Slash Commands: {}",
                     getSlashCommandsData().size() );
    }

}
