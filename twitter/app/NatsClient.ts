import { log } from 'node:console';
import { NatsConnection, connect } from 'nats';

export default class NatsClient {
  private constructor(private _connection: NatsConnection) {}

  get connection() {
    return this._connection;
  }

  static async create() {
    const nats = connect({
      servers: `${process.env.NATS_NAME || 'localhost'}:4222`,
      user: process.env.NATS_USER,
      pass: process.env.NATS_PASS,
    });
    nats.catch(
      (err) => new Error(`Nats connection error: ${err?.message || err}`),
    );
    const nc = await nats;
    log('Connected to Nats');
    nc.closed().then((err) => {
      let msg = 'Connection to Nats closed';
      if (err != null) {
        msg = `${msg} with error ${err.message}`;
      }
      log(msg);
    });

    return new NatsClient(nc);
  }
}
