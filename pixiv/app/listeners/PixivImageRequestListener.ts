import { TYPES } from '../Types';
import { PixivScraper } from '../services/scraper/PixivScraper';
import { Listener } from './Listener';
import { PixivImageRequestEvent } from './PixivImageRequestEvent';
import { error } from 'console';
import { inject, injectable, named } from 'inversify';
import { Msg, StringCodec } from 'nats';
import { safelyStringify } from '../utils/String';

@injectable()
export class PixivImageRequestListener extends Listener<PixivImageRequestEvent> {
  subject = 'pixiv.image.request';
  queueName= 'pixiv-image-service-queue';
  constructor(
    @inject(TYPES.PixivScraper) @named('pixiv')
    private service: PixivScraper,
    ) {
      super();
    }
    
    async onMessage(msg: Msg, data: { url: string; }) {
      const sc = StringCodec();
    const { url } = data;
    try {

      const fileInfo = await this.service.downloadFromLinks(
        [url]
      );

      msg.respond(sc.encode(safelyStringify(fileInfo)));
    } catch (err) {
      error(err);

      msg.respond(
        sc.encode(safelyStringify({ success: false, error: { message: err } })),
      );
    }
    }
  
}
