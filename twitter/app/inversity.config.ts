import { Container } from 'inversify';
import { TwitterPostRequestListener } from './events/TwitterPostRequestListener';
import { DownloadService } from './services/DownloadService';
import { TwitterService } from './services/TwitterService';

const container = (async () => {
  const container = new Container();

  container
    .bind<TwitterPostRequestListener>(TwitterPostRequestListener)
    .to(TwitterPostRequestListener)
    .inSingletonScope();

  container
    .bind<TwitterService>(TwitterService)
    .to(TwitterService)
    .inSingletonScope();

  container
    .bind<DownloadService>(DownloadService)
    .to(DownloadService)
    .inRequestScope();

  return container;
})();

export { container };
