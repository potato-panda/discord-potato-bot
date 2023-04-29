import { inject, injectable, named } from 'inversify';
import { Msg, StringCodec } from 'nats';
import { PixivService } from '../services/PixivService';
import { BaseListener, BaseListenerEvent } from './Listener';
import ListenerSubjects from './ListenerSubjects';

interface PixivImageRequestEvent extends BaseListenerEvent {
  data: {
    url: string;
  };
}

@injectable()
export class PixivImageRequestListener extends BaseListener<PixivImageRequestEvent> {
  subject = ListenerSubjects.PixivImageRequest;
  constructor(
    @inject(PixivService) @named('pixiv')
    private service: PixivService,
  ) {
    super();
  }

  async onMessage(msg: Msg, data: { url: string }) {
    const sc = StringCodec();
    const { url } = data;
    // try {

    //   const fileInfo = await this.service.downloadFromLinks(
    //     [url]
    //   );

    //   msg.respond(sc.encode(safelyStringify(fileInfo)));
    // } catch (err) {
    //   error(err);

    //   msg.respond(
    //     sc.encode(safelyStringify({ success: false, error: { message: err } })),
    //   );
    // }
  }
}
