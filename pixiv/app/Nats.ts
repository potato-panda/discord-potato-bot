import { natsServers } from './constants.config';
import { log } from 'console';
import { connect } from 'nats';

export const nats = (async () => {
  const nc = await connect(natsServers);
  log('Connected to Nats');

  // Log when connection is closed
  nc.closed().then((err) => {
    let msg = 'Connection to Nats closed';
    if (err) {
      msg = `${msg} with error ${err!.message}`;
    }
    log(msg);
  });

  return nc;
})();
