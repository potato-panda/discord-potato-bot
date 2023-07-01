import { Msg, NatsConnection, StringCodec } from 'nats';
import { log } from 'node:console';

export interface ListenerEvent {
  data: unknown;
}

export abstract class Listener<T extends ListenerEvent> {
  constructor(public readonly subject: string) {}
  abstract onMessage(msg: Msg, data: T['data']): Promise<void>;

  public listen(nats: NatsConnection) {
    nats.subscribe(this.subject, {
      callback: (err, msg) => {
        const sc = StringCodec();
        if (err) {
          msg.respond(sc.encode(`Request Error: ${err.message}`));
          log('Request error:', err.message);
        } else {
          try {
            const data = msg.json() as T['data'];
            log(`Request received: ${data}`);
            this.onMessage(msg, data);
          } catch (err: any) {
            log(`Data error: ${err?.message || err}`);
            msg.respond(sc.encode(`Request Error: ${err?.message || err}`));
          }
        }
      },
    });
    log('Listening to subject: ', this.subject);
  }
}
