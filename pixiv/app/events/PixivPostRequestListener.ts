import { Msg, StringCodec } from 'nats';
import { error } from 'node:console';
import { PixivService } from '../services/PixivService';
import { safeStringify } from '../utils/String';
import { Listener, ListenerEvent } from './Listener';
import ListenerSubjects from './ListenerSubjects';
import { PixivPost } from './PixivPostRequest';

export interface PixivPostRequestEvent extends ListenerEvent {
  data: PixivPost.Request;
}

export class PixivPostRequestListener extends Listener<PixivPostRequestEvent> {
  constructor(
    private service: PixivService,
  ) {
    super(ListenerSubjects.PixivPostRequest);
  }

  async onMessage(msg: Msg, data: PixivPost.Request) {
    const sc = StringCodec();
    const { postId, quality } = data;
    try {
      const { metadata, illustMetadata } = await this.service.getIllust(postId);

      const downloadResponses =
        metadata.illustType === 'ugoira'
          ? await this.service.downloadUgoira(postId)
          : await this.service.downloadIllusts(illustMetadata.illust, quality);

      const reply: PixivPost.Reply = {
        metadata,
        downloadResponses,
      };

      // log('Response: ', inspect(reply, false, null, true));

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
