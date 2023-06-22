package app.potato.bot.registries;


import app.potato.bot.commands.slash.SlashCommand;
import app.potato.bot.utils.AppCommandProperties;
import app.potato.bot.utils.Disabled;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final
class SlashCommandsRegistry {

    private static final Logger                logger
            = LoggerFactory.getLogger( SlashCommandsRegistry.class );
    private static final SlashCommandsRegistry _instance
            = new SlashCommandsRegistry();
    private final        CopyOnWriteArrayList<SlashCommand>
                                               slashCommands;

    public
    SlashCommandsRegistry() {
        slashCommands
                = new CopyOnWriteArrayList<>(
                new Reflections( "app.potato.bot.commands.slash" )
                        .getSubTypesOf( SlashCommand.class )
                        .stream()
                        .reduce( new ArrayList<SlashCommand>() {},
                                 ( abstractSlashCommands, aClass ) -> {
                                     try {
                                         if ( aClass.isAnnotationPresent( Disabled.class ) )
                                             return abstractSlashCommands;

                                         SlashCommand
                                                 slashCommand
                                                 = createSlashCommand( aClass );
                                         abstractSlashCommands.add( slashCommand );
                                         return abstractSlashCommands;
                                     }
                                     catch ( InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e ) {
                                         return abstractSlashCommands;
                                     }
                                 },
                                 ( abstractSlashCommands, abstractSlashCommands2 ) -> {
                                     abstractSlashCommands.addAll( abstractSlashCommands2 );
                                     return abstractSlashCommands;
                                 } ) );
    }

    private static
    SlashCommand createSlashCommand( Class<? extends SlashCommand> aClass )
    throws
    NoSuchMethodException,
    InvocationTargetException,
    InstantiationException,
    IllegalAccessException
    {
        AppCommandProperties declaredAnnotation
                = aClass.getDeclaredAnnotation( AppCommandProperties.class );

        logger.info( "Registering Slash Command {} – {}",
                     declaredAnnotation.commandName(),
                     declaredAnnotation.commandDesc() );

        return aClass.getDeclaredConstructor( String.class,
                                              String.class )
                     .newInstance( declaredAnnotation.commandName(),
                                   declaredAnnotation.commandDesc() );
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
