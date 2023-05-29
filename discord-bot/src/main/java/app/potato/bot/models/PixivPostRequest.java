package app.potato.bot.models;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import org.bson.types.ObjectId;

import java.util.Date;

@Entity( "PixivPostRequests" )
public
class PixivPostRequest {
    @Id private ObjectId id;
    private     String   postId;
    private     String   key;
    private     Date     createdAt;

    private
    PixivPostRequest() {}

    public
    ObjectId getId() {
        return id;
    }

    public
    void setId( ObjectId id ) {
        this.id = id;
    }

    public
    String getPostId() {
        return postId;
    }

    public
    void setPostId( String postId ) {
        this.postId = postId;
    }

    public
    String getKey() {
        return key;
    }

    public
    void setKey( String key ) {
        this.key = key;
    }

    public
    Date getCreatedAt() {
        return createdAt;
    }

    public
    void setCreatedAt( Date createdAt ) {
        this.createdAt = createdAt;
    }
}
