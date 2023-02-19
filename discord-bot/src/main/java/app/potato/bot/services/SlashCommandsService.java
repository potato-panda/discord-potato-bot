package app.potato.bot.services;


import app.potato.bot.commands.slash.AbstractSlashCommand;
import app.potato.bot.commands.slash.SlashCommand;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.stream.Collectors;

public
class SlashCommandsService {

    private static final Logger logger =
            LoggerFactory.getLogger( SlashCommandsService.class );

    static               Reflections reflections               =
            new Reflections( "app.potato.bot" );
    private static final List<? extends AbstractSlashCommand>
                                     abstractSlashCommandsList =
            reflections.getSubTypesOf( AbstractSlashCommand.class )
                       .stream( )
                       .map( aClass -> {
                           try {
                               return createSlashCommand( aClass );
                           }
                           catch ( InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e ) {
                               throw new RuntimeException( e );
                           }
                       } )
                       .collect( Collectors.toList( ) );

    public static
    void setGuildSlashCommands( Guild guild ) {
        SlashCommandData[] slashCommandDataArray =
                getSlashCommandDataAsArray( );

        guild.updateCommands( )
             .addCommands( slashCommandDataArray )
             .queue( );

        logger.info(
                "Set Guild Slash Commands for {}: {}",
                guild.getName( ),
                slashCommandDataArray.length
        );
    }

    public static
    SlashCommandData[] getSlashCommandDataAsArray( ) {
        return getSlashCommandDataAsList( ).toArray( new SlashCommandData[]{ } );
    }

    public static
    List<SlashCommandData> getSlashCommandDataAsList( ) {
        return getSlashCommandList( ).stream( )
                                     .map( AbstractSlashCommand::commandData )
                                     .collect( Collectors.toList( ) );
    }

    public static
    List<? extends AbstractSlashCommand> getSlashCommandList( ) {
        return abstractSlashCommandsList;
    }

    public static
    void setGlobalSlashCommands( JDA jda ) {
        SlashCommandData[] slashCommandDataArray =
                getSlashCommandDataAsArray( );

        jda.updateCommands( )
           .addCommands( slashCommandDataArray )
           .queue( );

        logger.info(
                "Set Global Slash Commands: {}",
                slashCommandDataArray.length
        );
    }

    private static
    AbstractSlashCommand createSlashCommand( Class<? extends AbstractSlashCommand> aClass ) throws
                                                                                            NoSuchMethodException,
                                                                                            InvocationTargetException,
                                                                                            InstantiationException,
                                                                                            IllegalAccessException {
        SlashCommand anno = aClass.getDeclaredAnnotation( SlashCommand.class );

        logger.info(
                "Creating Slash Command {} – {}",
                anno.commandName( ),
                anno.commandDesc( )
        );

        return aClass.getDeclaredConstructor(
                             String.class,
                             String.class
                     )
                     .newInstance(
                             anno.commandName( ),
                             anno.commandDesc( )
                     );
    }

}
