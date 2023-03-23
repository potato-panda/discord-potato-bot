import cluster from 'cluster';
import { log } from 'console';

import * as dotenv from 'dotenv';
dotenv.config({ path: `${__dirname}//.env` });

import 'reflect-metadata';

import './constants.config';

import { InversifyExpressServer } from 'inversify-express-utils';
import './Redis';
import './Mongoose'

import './controllers';
import { App } from './App';
import { appPort } from './constants.config';
import { container } from './inversity.config';
import { PixivPostRequestListener } from './listeners';
import { nats } from './Nats';

(async () => {
  container
    .resolve<PixivPostRequestListener>(PixivPostRequestListener)
    .listen(await nats);

  if (!process.env.DEBUG) {
    let server = new InversifyExpressServer(container);
    server.setConfig((app) => {
      new App(app);
    });
    let app = server.build();

    app.listen(appPort, () => {
      log(
        `Worker ${cluster?.worker?.id} (PID:${process.pid}) listening on port ${appPort}`,
      );
    });
  } else {
    log(`Worker ${cluster?.worker?.id || 'null'} (PID:${process.pid}) started`);
  }
})();
