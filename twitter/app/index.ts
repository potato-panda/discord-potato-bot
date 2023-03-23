import * as dotenv from 'dotenv';
dotenv.config({ path: `${__dirname}//.env` });

import 'reflect-metadata';

import { container } from './inversity.config';

import './Mongoose';

import { nats } from './Nats';
import { TwitterPostRequestListener } from './listeners/TwitterPostRequestListener';

async function main() {
  container
    .resolve<TwitterPostRequestListener>(TwitterPostRequestListener)
    .listen(await nats);
}

main();
