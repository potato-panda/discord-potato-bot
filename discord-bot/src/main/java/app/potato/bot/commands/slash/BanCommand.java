package app.potato.bot.commands.slash;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@SlashCommand( commandName = "ban", commandDesc = "Bans a user" )
public final
class BanCommand extends AbstractSlashCommand {

    public
    BanCommand( String commandName, String commandDesc ) {
        super( commandName, commandDesc );
    }

    @NotNull
    @Override
    public
    SlashCommandData commandData( ) {
        return Commands.slash( commandName, commandDesc );
    }

    @Override
    public
    void execute( SlashCommandInteractionEvent event ) {

        // double check permissions, don't trust discord on this!
        if ( !Objects.requireNonNull( event.getMember( ) )
                     .hasPermission( Permission.BAN_MEMBERS ) ) {
            event.reply( "You cannot ban members! Nice try ;)" )
                 .setEphemeral( true )
                 .queue( );
            return;
        }

        User target = event.getOption( "user", OptionMapping::getAsUser );
        // optionally check for member information
        Member member = event.getOption( "user", OptionMapping::getAsMember );
        assert member != null;
        if ( !event.getMember( )
                   .canInteract( member ) ) {
            event.reply( "You cannot ban this user." )
                 .setEphemeral( true )
                 .queue( );
            return;
        }

        // Before starting our ban request, tell the user we received the command
        // This sends a "Bot is thinking..." message which is later edited once we finished
        event.deferReply( )
             .queue( );
        String reason = event.getOption( "reason", OptionMapping::getAsString );
        assert target != null;
        AuditableRestAction<Void> action =
                Objects.requireNonNull( event.getGuild( ) )
                       .ban( target,
                             0,
                             TimeUnit.MILLISECONDS
                       ); // Start building our ban request
        if ( reason != null ) // reason is optional
            action =
                    action.reason( reason ); // set the reason for the ban in the audit logs and ban log
        action.queue( v -> {
            // Edit the thinking message with our response on success
            event.getHook( )
                 .editOriginal( "**" + target.getAsTag( ) + "** was banned by **" + event.getUser( )
                                                                                         .getAsTag( ) + "**!" )
                 .queue( );
        }, error -> {
            // Tell the user we encountered some error
            event.getHook( )
                 .editOriginal( "Some error occurred, try again!" )
                 .queue( );
            error.printStackTrace( );
        } );
    }


}
