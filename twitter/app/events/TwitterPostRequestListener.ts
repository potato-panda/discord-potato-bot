import { Msg, StringCodec } from 'nats';
import { error } from 'node:console';
import { TwitterService } from '../services/TwitterService';
import { safeStringify } from '../utils/String';
import { Listener, ListenerEvent } from './Listener';
import ListenerSubjects from './ListenerSubjects';
import { TwitterPost } from './TwitterPostRequest';

export interface TwitterPostRequestEvent extends ListenerEvent {
  data: TwitterPost.Request;
}

export class TwitterPostRequestListener extends Listener<TwitterPostRequestEvent> {
  constructor(
    private service: TwitterService,
  ) {
    super(ListenerSubjects.TwitterPostRequest);
  }

  async onMessage(msg: Msg, data: TwitterPost.Request) {
    const sc = StringCodec();
    try {
      const {
        id_str: postId,
        full_text,
        extended_entities,
        user: {
          id_str: userId,
          name,
          screen_name,
          url,
          profile_image_url_https,
          profile_link_color,
        },
        retweet_count,
        favorite_count,
        possibly_sensitive,
        created_at,
        reply_count,
      } = await this.service.getTweet(data.url);

      const reply: TwitterPost.Reply = {
        metadata: {
          content: full_text ?? null,
          userName: name,
          userScreenName: screen_name,
          userUrl: url,
          userThumbnailUrl: profile_image_url_https,
          retweets: retweet_count,
          favourites: favorite_count,
          suggestive: possibly_sensitive ?? false,
          createdAt: created_at,
          embedColour: profile_link_color
        },
        downloadResponses: await this.service.downloadMedia(
          postId,
          extended_entities,
        ),
      };

      msg.respond(sc.encode(safeStringify(reply)));
    } catch (err) {
      error(err);

      msg.respond(
        sc.encode(
          safeStringify({ success: false, message: safeStringify(err) }),
        ),
      );
    }
  }
}
