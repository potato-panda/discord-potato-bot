import { log } from 'node:console';
import { connect } from 'nats';

export default (async () => {
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

  return nc;
})();
