package app.potato.bot;

import app.potato.bot.registries.ContentModerationRegistry;
import app.potato.bot.registries.ListenerRegistry;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

import static app.potato.bot.registries.SlashCommandsRegistry.setGlobalSlashCommands;
import static app.potato.bot.registries.SlashCommandsRegistry.setGuildSlashCommands;
import static app.potato.bot.utils.StringUtil.isNullOrBlank;

public final
class Bot {
    private static final
    Logger logger = LoggerFactory.getLogger( Bot.class );

    public static
    void main( String[] args ) throws Exception {
        String env = System.getenv( "ENV" );
        if ( !Objects.equals( env,
                              "PROD" ) )
        {
            env = "DEV";
        }
        String discordBotToken
                = System.getenv( "DISCORD_BOT_TOKEN" );
        if ( isNullOrBlank( discordBotToken ) ) {
            throw new Exception( "DISCORD_BOT_TOKEN env var must be provided" );
        }

        JDABuilder builder = JDABuilder.createDefault( discordBotToken );

        builder.enableIntents( GatewayIntent.MESSAGE_CONTENT,
                               GatewayIntent.GUILD_MESSAGES );

        JDA jda = builder.build();

        ContentModerationRegistry.instance();

        jda.addEventListener( ListenerRegistry.getListeners().toArray() );

        NatsConnection.instance();

        RedisConnection.instance();

        MongoDBConnection.instance();

        jda.awaitReady();

        // Register Guild Commands when in Dev Mode
        if ( env.equals( "DEV" ) ) {
            try {
                String discordDevGuildId
                        = System.getenv( "DISCORD_DEV_GUILD_ID" );
                if ( !isNullOrBlank( discordDevGuildId ) ) {
                    Guild guild = jda.getGuildById( discordDevGuildId );

                    if ( guild != null ) {
                        logger.info( "Bot is a member of guild: {}",
                                     discordDevGuildId );

                        setGuildSlashCommands( guild );

                    } else {
                        logger.info( "Bot is not a member of guild: {}",
                                     discordDevGuildId );
                    }
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
