package app.potato.bot.commands.slash;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@SlashCommand( commandName = "clear", commandDesc = "Clears messages" )
public
class DeleteMessagesCommand extends AbstractSlashCommand {

    private static final Logger logger = LoggerFactory.getLogger(
            DeleteMessagesCommand.class );

    public
    DeleteMessagesCommand( String commandName, String commandDesc ) {
        super( commandName, commandDesc );
    }

    @Override
    public
    SlashCommandData commandData( ) {
        return Commands.slash( commandName, commandDesc )
                       .setDefaultPermissions( DefaultMemberPermissions.ENABLED )
                       .addOption( OptionType.INTEGER,
                                   "amount",
                                   "number of messages up to last 100",
                                   true
                       )
                       .addOption( OptionType.USER,
                                   "user",
                                   "delete messages of specific user",
                                   false
                       );


    }

    @Override
    public
    void execute( SlashCommandInteractionEvent event ) {

        event.deferReply( )
             .setEphemeral( true )
             .queue( );

        // Check if user has permission to delete messages
        if ( !Objects.requireNonNull( event.getMember( ) )
                     .hasPermission( Permission.MESSAGE_MANAGE ) ) {
            event.getHook( )
                 .sendMessage( "You do not have permission to delete messages." )
                 .setEphemeral( true )
                 .queue( );
            return;
        }

        User target = event.getOption( "user", OptionMapping::getAsUser );
        // Check for optional member info, and if user can affect chosen member
        Member member = event.getOption( "user", OptionMapping::getAsMember );
        if ( member != null && !event.getMember( )
                                     .canInteract( member ) ) {
            event.getHook( )
                 .sendMessage( "You cannot delete messages by this user." )
                 .setEphemeral( true )
                 .queue( );
            return;
        }


        Integer amount = event.getOption( "amount", OptionMapping::getAsInt );

        MessageChannel messageChannel = event.getMessageChannel( );

        List<Message> pastXMessages = collectXMessages( event,
                                                        amount,
                                                        member != null
                                                        ? member.getUser( )
                                                        : null
        );

        CompletableFuture[] futures = messageChannel.purgeMessages(
                                                            pastXMessages )
                                                    .toArray( new CompletableFuture[]{ } );
        CompletableFuture.allOf( futures )
                         .thenAccept( unused -> {
                             String message = "Messages " + ( member != null
                                                              ? "by " + (
                                     member.getNickname( ) != null
                                     ? member.getNickname( )
                                     : member.getEffectiveName( ) ) + " "
                                                              : "" ) + "deleted: " + pastXMessages.size( );

                             event.getHook( )
                                  .sendMessage( message )
                                  .setEphemeral( true )
                                  .queue( );

                             logger.info( message );
                         } );

    }

    private
    List<Message> collectXMessages( SlashCommandInteractionEvent event,
                                    Integer amount,
                                    User user ) {
        List<Message> listMessages = new ArrayList<>( 0 );
        return collectXMessages( event, amount, user, listMessages, false );
    }

    private
    List<Message> collectXMessages( SlashCommandInteractionEvent event,
                                    Integer amount,
                                    User user,
                                    List<Message> pastXMessages,
                                    boolean searchTerminated ) {
        MessageChannel messageChannel = event.getMessageChannel( );
        String messageId = event.getMessageChannel( )
                                .getLatestMessageId( );

        // Check if messages exceeds given amount, limit amount
        if ( pastXMessages.size( ) >= amount ) {
            return pastXMessages.stream( )
                                .limit( amount )
                                .collect( Collectors.toList( ) );
        }
        // Return results if amount reached or search reached termination (no more messages)
        else if ( pastXMessages.size( ) == amount || searchTerminated ) {
            return pastXMessages;
        }
        // Recurse
        else {
            List<Message> resultMessages;
            // If user is provided, filter messages for user
            if ( user != null ) {
                resultMessages = messageChannel.getHistoryBefore( messageId,
                                                                  100
                                               )
                                               .complete( )
                                               .getRetrievedHistory( )
                                               .stream( )
                                               .filter( message -> message.getAuthor( )
                                                                          .getId( )
                                                                          .equals(
                                                                                  user.getId( ) ) )
                                               .collect( Collectors.toList( ) );

            } else {
                resultMessages = messageChannel.getHistory( )
                                               .retrievePast( 100 )
                                               .complete( )
                                               .stream( )
                                               .filter( message -> Objects.requireNonNull(
                                                                                  event.getMember( ) )
                                                                          .canInteract(
                                                                                  Objects.requireNonNull(
                                                                                          message.getMember( ) ) ) )
                                               .collect( Collectors.toList( ) );
            }
            pastXMessages.addAll( resultMessages );

            // If no more message, set to terminate recursion
            searchTerminated = resultMessages.size( ) == 0;

            return collectXMessages( event,
                                     amount,
                                     user,
                                     pastXMessages,
                                     searchTerminated
            );
        }
    }
}
