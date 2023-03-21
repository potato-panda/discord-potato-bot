package app.potato.bot.listeners.handlers;

public
class PixivPostLinkRequest {
    public String              postId;
    public ImageRequestQuality quality;

    public
    PixivPostLinkRequest( String postId,
                          ImageRequestQuality quality )
    {
        this.postId  = postId;
        this.quality = quality;
    }

    public
    PixivPostLinkRequest( String postId )
    {
        this.postId  = postId;
        this.quality = ImageRequestQuality.regular;
    }

    public
    enum ImageRequestQuality {
        original( "original" ), regular( "regular" );

        private final String value;

        ImageRequestQuality( String value ) {
            this.value = value;
        }

        public
        String getValue() {
            return value;
        }
    }
}
