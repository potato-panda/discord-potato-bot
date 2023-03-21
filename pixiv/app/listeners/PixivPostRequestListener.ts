import { error, log } from 'console';
import { inject, injectable, named } from 'inversify';
import { Msg, StringCodec } from 'nats';
import { inspect } from 'util';
import { PixivScraper } from '../services/scraper/PixivScraper';
import { TYPES } from '../Types';
import { safelyStringify } from '../utils/String';
import { Listener } from './Listener';
import { PixivPostRequestEvent } from './PixivPostRequestEvent';
import { PixivPostRequestReply } from './reply/PixivPostRequestReply';

@injectable()
export class PixivPostRequestListener extends Listener<PixivPostRequestEvent> {
  subject = 'pixiv.post.request';
  queueName = 'pixiv-post-service-queue';
  constructor(
    @inject(TYPES.PixivScraper) @named('pixiv')
    private service: PixivScraper,
  ) {
    super();
  }

  async onMessage(
    msg: Msg,
    data: { postId: string; quality: 'original' | 'regular' },
  ): Promise<void> {
    const sc = StringCodec();
    const { postId, quality } = data;
    try {
      const { pages, post } = await this.service.scrapeImagePost(postId);

      const reply: PixivPostRequestReply = {
        metadata: post,
        downloadResponses: await this.service.downloadFromLinks(
          pages.map((page) => {
            switch (quality) {
              case 'original':
                return page.urls.original;
              default:
                return page.urls.regular;
            }
          }),
        ),
      };

      log('Response: ', inspect(reply, false, null, true));

      msg.respond(sc.encode(safelyStringify(reply)));
    } catch (err) {
      error(err);

      msg.respond(
        sc.encode(
          safelyStringify({ success: false, message: safelyStringify(err) }),
        ),
      );
    }
  }
}
