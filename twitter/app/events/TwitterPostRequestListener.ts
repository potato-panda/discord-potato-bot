import { inject, injectable } from 'inversify';
import { Msg, StringCodec } from 'nats';
import { error, log } from 'node:console';
import { inspect } from 'node:util';
import { TwitterService } from '../services/TwitterService';
import { safeStringify } from '../utils/String';
import { BaseListener, BaseListenerEvent } from './Listener';
import { TwitterPost } from './TwitterPostRequest';
import ListenerSubjects from './ListenerSubjects';

export interface TwitterPostRequestEvent extends BaseListenerEvent {
  data: TwitterPost.Request;
}

@injectable()
export class TwitterPostRequestListener extends BaseListener<TwitterPostRequestEvent> {
  subject = ListenerSubjects.TwitterPostRequest;

  constructor(
    @inject(TwitterService)
    private service: TwitterService,
  ) {
    super();
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
        },
        retweet_count,
        favorite_count,
        possibly_sensitive,
        created_at,
        quote_count,
        reply_count,
      } = await this.service.getOneTweet(data.url);

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
        },
        downloadResponses: await this.service.downloadMedia(
          postId,
          extended_entities,
        ),
      };

      log('Response: ', inspect(reply, false, null, true));

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
