import { Container } from 'inversify';
import { env } from 'node:process';
import { PixivApiClient } from 'pixiv-api-wrapper';
import puppeteer from 'puppeteer-core';
import { PixivPostRequestListener } from './events/PixivPostRequestListener';
import { PixivService } from './services/PixivService';

const container = (async () => {
  const container = new Container();

  container
    .bind<PixivPostRequestListener>(PixivPostRequestListener)
    .to(PixivPostRequestListener)
    .inSingletonScope();

  const browser = await puppeteer.launch({
    headless: 'new',
    args: [
      '--disable-gpu',
      '--disable-setuid-sandbox',
      '--no-sandbox',
      '--no-zygote'],
    executablePath: puppeteer.executablePath('chrome'),
  });

  const client = await PixivApiClient.create(
    {
      userId: env.PIXIV_ID || '',
      password: env.PIXIV_PW || '',
    },
    browser,
  );

  container.bind<PixivApiClient>(PixivApiClient).toConstantValue(client);

  container
    .bind<PixivService>(PixivService)
    .to(PixivService)
    .inSingletonScope();

  return container;
})();

export { container };
