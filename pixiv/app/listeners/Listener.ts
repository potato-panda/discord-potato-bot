import { log } from 'console';
import { injectable } from 'inversify';
import { Msg, NatsConnection, StringCodec } from 'nats';
import { ListenerEvent } from './ListenerEvent';

@injectable()
export abstract class Listener<T extends ListenerEvent, D = T['data']> {
  abstract subject: string;
  abstract onMessage(msg: Msg, data: D): Promise<void>;
  constructor() {}

  public listen(nats: NatsConnection) {
    log('Listening to subject: ', this.subject);
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
