import { env } from 'node:process';
import { PixivApi } from 'pixiv-api-wrapper';
import puppeteer from 'puppeteer-core';
import './Mongoose';
import NatsClient from './NatsClient';
import { PixivPostRequestListener } from './events';
import { PixivService } from './services/PixivService';

(async function () {
  try {
    // const browser = await puppeteer.launch({
    //   headless: 'new',
    //   args: [
    //     '--disable-gpu',
    //     '--disable-setuid-sandbox',
    //     '--no-sandbox',
    //     '--no-zygote'],
    //   executablePath: puppeteer.executablePath('chrome'),
    // });

    if (!env.PIXIV_RT) throw new Error('Refresh Token is not set');

    const client = await PixivApi.create(env.PIXIV_RT);

    const pixivService = new PixivService(client);

    const natsClient = await NatsClient.create();

    new PixivPostRequestListener(pixivService).listen(natsClient.connection);
  } catch (err) {
    console.log(err);
  }
})();
