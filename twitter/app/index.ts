import { config } from 'dotenv';
import path from 'node:path';
import { env } from 'node:process';
import './Mongoose';
import NatsClient from './NatsClient';
import { TwitterPostRequestListener } from './events';
import { TwitterService } from './services/TwitterService';
config({ path: path.resolve(`${__dirname}/../../.env`) });

(async function () {
  try {

    const twitterService = new TwitterService(env.TWITTER_TOKENS);

    const natsClient = await NatsClient.create();

    new TwitterPostRequestListener(twitterService)
      .listen(natsClient.connection);

  } catch (error) {
    console.log('Error:', error);
  }
})();
