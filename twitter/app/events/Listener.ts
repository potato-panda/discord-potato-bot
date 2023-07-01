import { log } from 'node:console';
import { Msg, NatsConnection, StringCodec } from 'nats';

export interface ListenerEvent {
  data: unknown;
}

export abstract class Listener<T extends ListenerEvent> {
  constructor(public readonly subject: string) {}
  protected abstract onMessage(msg: Msg, data: T['data']): Promise<void>;

  public listen(nats: NatsConnection) {
    nats.subscribe(this.subject, {
      callback: (err, msg) => {
        const sc = StringCodec();
        if (err) {
          msg.respond(sc.encode(`Request Error: ${err}`));
          log('Request error:', err);
        } else {
          try {
            const data = msg.json() as T['data'];
            log('Request received:', data);
            this.onMessage(msg, data);
          } catch (err) {
            log('Request Error:', err);
            msg.respond(sc.encode(`Request Error: ${err}`));
          }
        }
      },
    });
    log('Listening subject: ', this.subject);
  }
}
