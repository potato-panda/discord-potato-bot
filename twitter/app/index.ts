import 'reflect-metadata';

import { container as resolvedContainer } from './inversity.config';

import './Mongoose';
import nats from './Nats';
import { TwitterPostRequestListener } from './events';

(async function () {
  const container = await resolvedContainer;
  container
    .resolve<TwitterPostRequestListener>(TwitterPostRequestListener)
    .listen(await nats);
})();
