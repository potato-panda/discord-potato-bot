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
    data: { postId: string; quality: 'ORIGINAL' | 'REGULAR' },
  ): Promise<void> {
    const sc = StringCodec();
    const { postId, quality } = data;
    try {
      const { pagesMetadata, gifMetadata, metadata } =
        await this.service.scrapePost(postId);

      const pagesDownloadResponse = async () =>
        await this.service.downloadByBody(postId, pagesMetadata, quality);

      const gifDownloadResponse = async () =>
        await this.service.downloadGif(postId, pagesMetadata[0], gifMetadata);

      const downloadResponses =
        metadata.illustType === 'gif'
          ? await gifDownloadResponse()
          : await pagesDownloadResponse();

      const reply: PixivPostRequestReply = {
        metadata,
        downloadResponses,
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
