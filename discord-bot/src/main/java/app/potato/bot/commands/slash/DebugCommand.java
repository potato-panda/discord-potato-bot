package app.potato.bot.commands.slash;

import com.thedeanda.lorem.LoremIpsum;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;


@SlashCommand( commandName = "debug", commandDesc = "Commands for debugging" )
public final
class DebugCommand extends AbstractSlashCommand {

    public
    DebugCommand( String commandName, String commandDesc ) {
        super(
                commandName,
                commandDesc
        );
    }

    @Override
    public
    SlashCommandData commandData( ) {

        return Commands.slash(
                               commandName,
                               commandDesc
                       )
                       .setDefaultPermissions( DefaultMemberPermissions.DISABLED )
                       .addSubcommands( new SubcommandData(
                               "loremipsum",
                               "create messages for debugging"
                       ).addOption(
                               OptionType.INTEGER,
                               "amount",
                               "generate a number of messages",
                               false
                       ) );
    }

    @Override
    public
    void execute( SlashCommandInteractionEvent event ) {

        Integer amount = event.getOption(
                "amount",
                OptionMapping::getAsInt
        );
        amount = amount < 1 ? amount : 1;

        event.deferReply( )
             .setEphemeral( true )
             .queue( );

        for ( Integer i = 0; i < amount; i++ ) {
            String messageString = LoremIpsum.getInstance( )
                                             .getWords(
                                                     2,
                                                     10
                                             );

            event.getMessageChannel( )
                 .sendMessage( messageString )
                 .complete( );
        }

        event.getHook( )
             .sendMessage( "Executed command" )
             .queue( );

    }
}
