package app.potato.bot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static app.potato.bot.services.SlashCommandsService.setGlobalSlashCommands;
import static app.potato.bot.services.SlashCommandsService.setGuildSlashCommands;
import static app.potato.bot.utils.ListenerUtil.getListenersAsArray;

public
class Bot {
    private static final
    Logger logger = LoggerFactory.getLogger( Bot.class );

    public static
    void main( String[] args ) throws Exception {
        String env = System.getenv( "ENV" );
        if ( !env.equals( "PROD" ) ) {
            env = "DEV";
        }
        String botToken = System.getenv( "BOT_TOKEN" );
        if ( botToken.isEmpty() ) {
            throw new Exception( "BOT_TOKEN env var must be provided" );
        }

        JDABuilder builder = JDABuilder.createDefault( botToken );

        builder.enableIntents( GatewayIntent.MESSAGE_CONTENT,
                               GatewayIntent.GUILD_MESSAGES );

        JDA jda = builder.build();

        String guildId = System.getenv( "GUILD_ID" );

        jda.addEventListener( getListenersAsArray() );

        NatsConnection.instance();

        RedisConnection.instance();

        jda.awaitReady();


        // Register Guild Commands when in Dev Mode
        if ( env.equals( "DEV" ) ) {
            try {
                Guild guild = jda.getGuildById( guildId );

                if ( guild != null ) {
                    logger.info( "Bot is a member of guild: {}",
                                 guildId );

                    setGuildSlashCommands( guild );

                } else {
                    logger.info(
                            "Bot is not a member of guild: {}",
                            guildId
                    );
                }
            }
            catch ( Exception e ) {
                throw new RuntimeException( e );
            }
        }
        // Register Global Commands when not in Dev Mode
        else {
            try {
                setGlobalSlashCommands( jda );
            }
            catch ( Exception e ) {
                throw new RuntimeException( e );
            }
        }

    }


}
