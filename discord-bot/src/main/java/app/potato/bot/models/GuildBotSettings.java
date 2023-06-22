package app.potato.bot.models;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.HashMap;

@Entity( "GuildBotSettings" )
public final
class GuildBotSettings {
    private String                   guildId;
    private HashMap<String, Boolean> moderatedChannels;
    private
    HashMap<String, ArrayList<String>> feedRoutes;
    @Id
    private ObjectId id;
    private boolean  moderationLayerEnabled;

    private
    GuildBotSettings() {}

    public
    GuildBotSettings( String guildId ) {
        this.guildId = guildId;
    }

    public
    ObjectId getId() {
        return id;
    }

    public
    void setId( ObjectId id ) {
        this.id = id;
    }

    public
    String getGuildId() {
        return guildId;
    }

    public
    void setGuildId( String guildId ) {
        this.guildId = guildId;
    }

    public
    HashMap<String, Boolean> getModeratedChannels() {
        return moderatedChannels;
    }

    public
    void setModeratedChannels( HashMap<String, Boolean> moderatedChannels ) {
        this.moderatedChannels = moderatedChannels;
    }

    public
    boolean isModerationLayerEnabled() {
        return moderationLayerEnabled;
    }

    public
    void setModerationLayerEnabled( boolean moderationLayerEnabled ) {
        this.moderationLayerEnabled = moderationLayerEnabled;
    }

    public
    HashMap<String, ArrayList<String>> getFeedRoutes() {
        return feedRoutes;
    }

    public
    void setFeedRoutes( HashMap<String, ArrayList<String>> feedRoutes ) {
        this.feedRoutes = feedRoutes;
    }

}
