import { Msg, StringCodec } from 'nats';
import { PixivService } from '../services/PixivService';
import { Listener, ListenerEvent } from './Listener';
import ListenerSubjects from './ListenerSubjects';

interface PixivImageRequestEvent extends ListenerEvent {
  data: {
    url: string;
  };
}

export class PixivImageRequestListener extends Listener<PixivImageRequestEvent> {
  subject = ListenerSubjects.PixivImageRequest;
  constructor(
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
