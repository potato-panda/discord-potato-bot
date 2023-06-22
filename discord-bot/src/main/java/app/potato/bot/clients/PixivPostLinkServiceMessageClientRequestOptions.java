package app.potato.bot.clients;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;

public
class PixivPostLinkServiceMessageClientRequestOptions {
    private String              postId    = null;
    private ImageRequestQuality quality   = null;
    private TreeSet<Integer>    selection = null;
    private boolean             isOmit    = false;

    public
    PixivPostLinkServiceMessageClientRequestOptions() {
    }

    public
    PixivPostLinkServiceMessageClientRequestOptions( String postId,
                                                     ImageRequestQuality quality,
                                                     boolean isOmit,
                                                     Collection<Integer> selection )
    {
        this.setPostId( postId );
        this.setQuality( quality );
        this.setOmit( isOmit );
        this.setSelection( new TreeSet<>( selection ) );
    }

    public
    Boolean getOmit() {
        return isOmit;
    }

    public
    void setOmit( Boolean omit ) {
        isOmit = omit;
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
    ImageRequestQuality getQuality() {
        return quality;
    }

    public
    void setQuality( ImageRequestQuality quality ) {
        this.quality = quality;
    }

    public
    TreeSet<Integer> getSelection() {
        return selection;
    }

    public
    void setSelection( Collection<Integer> selection ) {
        this.selection = new TreeSet<>( selection );
    }

    @Override
    public
    String toString() {
        return "PixivPostLinkServiceMessageClientRequestOptions{" +
                "postId='" + postId + '\'' +
                ", quality=" + quality +
                ", selection=" + selection +
                ", isOmit=" + isOmit +
                '}';
    }

    public
    enum ImageRequestQuality {
        ORIGINAL( "original" ), REGULAR( "regular" );

        private final String value;

        ImageRequestQuality( String value ) {
            this.value = value;
        }

        public
        String getValue() {
            return value;
        }
    }

    public
    enum OptionFlag {
        PICK( "-p" ), OMIT( "-o" ), QUALITY( "-q" );

        public static final
        HashSet<OptionFlag> OPTION_FLAGS
                = new HashSet<>( List.of( PICK,
                                          OMIT,
                                          QUALITY ) );
        ;
        private final String value;

        private
        OptionFlag( String value ) {
            this.value = value;
        }

        public static
        OptionFlag keyOf( String string ) {
            for ( OptionFlag e : OptionFlag.values() ) {
                if ( e.value.equals( string ) ) return e;
            }
            return null;
        }

        public
        String getValue() {
            return value;
        }
    }

}
