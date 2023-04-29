import 'reflect-metadata';

import { container as resolvedContainer } from './inversity.config';

import './Mongoose';
import nats from './Nats';
import { PixivPostRequestListener } from './events';

(async function () {
  const container = await resolvedContainer;
  container
    .resolve<PixivPostRequestListener>(PixivPostRequestListener)
    .listen(await nats);
})();
