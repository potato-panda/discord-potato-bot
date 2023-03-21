package app.potato.bot.listeners.handlers;

import java.util.ArrayList;

public
class PixivPostLinkOptions extends PixivPostLinkRequest {
    public boolean            pick;
    public ArrayList<Integer> selectPages;

    public
    PixivPostLinkOptions( String postId,
                          ImageRequestQuality quality,
                          boolean pick,
                          ArrayList<Integer> selectPages )
    {
        super( postId,
               quality );
        this.pick        = pick;
        this.selectPages = selectPages;
    }

    public
    PixivPostLinkOptions( String postId ) {
        super( postId,
               ImageRequestQuality.regular );
        this.pick        = true;
        this.selectPages = new ArrayList<>();
    }

}

