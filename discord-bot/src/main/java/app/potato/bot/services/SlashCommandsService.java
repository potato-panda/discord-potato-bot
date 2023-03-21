package app.potato.bot.services;


import app.potato.bot.commands.slash.SlashCommand;
import app.potato.bot.utils.Disabled;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static app.potato.bot.commands.slash.SlashCommand.AbstractSlashCommand;

public
class SlashCommandsService {

    private static final Logger logger
            = LoggerFactory.getLogger( SlashCommandsService.class );

    private static final Reflections reflections
            = new Reflections( "app.potato.bot.commands.slash" );
    private static final List<? extends AbstractSlashCommand>
                                     abstractSlashCommandsList
            = initializeSlashCommands();

    public static
    void setGuildSlashCommands( Guild guild ) {
        SlashCommandData[] slashCommandDataArray
                = getSlashCommandsData().toArray( new SlashCommandData[]{} );

        guild.updateCommands().addCommands( slashCommandDataArray ).queue();

        logger.info( "Set Guild Slash Commands for {}: {}",
                     guild.getName(),
                     slashCommandDataArray.length );
    }

    public static
    List<SlashCommandData> getSlashCommandsData() {
        return abstractSlashCommandsList.stream()
                                        .map( AbstractSlashCommand::commandData )
                                        .collect( Collectors.toList() );
    }

    private static
    List<AbstractSlashCommand> initializeSlashCommands() {
        ArrayList<AbstractSlashCommand> slashCommandArrayList
                = reflections.getSubTypesOf( AbstractSlashCommand.class )
                             .stream()
                             .reduce( new ArrayList<AbstractSlashCommand>() {},
                                      ( abstractSlashCommands, aClass ) -> {
                                          try {
                                              if ( aClass.isAnnotationPresent( Disabled.class ) )
                                                  return abstractSlashCommands;

                                              AbstractSlashCommand
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
                                      } );
        return Collections.unmodifiableList( slashCommandArrayList );
    }

    private static
    AbstractSlashCommand createSlashCommand( Class<? extends AbstractSlashCommand> aClass )
    throws
    NoSuchMethodException,
    InvocationTargetException,
    InstantiationException,
    IllegalAccessException
    {
        SlashCommand declaredAnnotation;
        declaredAnnotation = aClass.getDeclaredAnnotation( SlashCommand.class );

        logger.info( "Creating Slash Command {} – {}",
                     declaredAnnotation.commandName(),
                     declaredAnnotation.commandDesc() );

        return aClass.getDeclaredConstructor( String.class,
                                              String.class )
                     .newInstance( declaredAnnotation.commandName(),
                                   declaredAnnotation.commandDesc() );
    }

    public static
    List<? extends AbstractSlashCommand> getSlashCommands() {
        return abstractSlashCommandsList;
    }

    public static
    void setGlobalSlashCommands( JDA jda ) {
        SlashCommandData[] slashCommandDataArray
                = getSlashCommandsData().toArray( new SlashCommandData[]{} );

        jda.updateCommands().addCommands( slashCommandDataArray ).queue();

        logger.info( "Set Global Slash Commands: {}",
                     slashCommandDataArray.length );
    }

}
