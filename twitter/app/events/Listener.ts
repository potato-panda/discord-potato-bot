import { log } from 'node:console';
import { injectable } from 'inversify';
import { Msg, NatsConnection, StringCodec } from 'nats';

export interface BaseListenerEvent {
  subject: string;
  data: unknown;
}

@injectable()
export abstract class BaseListener<T extends BaseListenerEvent, D = T['data']> {
  abstract subject: string;
  abstract onMessage(msg: Msg, data: D): Promise<void>;

  public listen(nats: NatsConnection) {
    log('Listening subject: ', this.subject);
    nats.subscribe(this.subject, {
      callback: (err, msg) => {
        const sc = StringCodec();
        if (err) {
          msg.respond(sc.encode(`Request Error: ${err}`));
          log('Request error:', err);
        } else {
          try {
            const data = JSON.parse(sc.decode(msg.data)) as D;
            log('Request received:', data);
            this.onMessage(msg, data);
          } catch (err) {
            msg.respond(sc.encode(`Request Error: ${err}`));
            log('Data error:', err);
          }
        }
      },
    });
  }
}
