import { Container } from 'inversify';
import { env } from 'process';
import { PixivPostRequestListener } from './listeners';
import { BrowserStorageStateService } from './services/BrowserStorageStateService';
import { DownloadService } from './services/DownloadService';
import { PixivScraper } from './services/scraper/PixivScraper';
import { TYPES } from './Types';

const container = new Container();

container
  .bind<BrowserStorageStateService>(TYPES.BrowserStorageStateService)
  .to(BrowserStorageStateService);
container
  .bind<PixivScraper>(TYPES.PixivScraper)
  .to(PixivScraper)
  .inSingletonScope();

container.bind<DownloadService>(TYPES.DownloadService).to(DownloadService);

container
  .bind<string>(TYPES.Username)
  .toConstantValue(env?.PIXIV_ID ?? '')
  .whenTargetNamed('pixiv');
container
  .bind<string>(TYPES.Password)
  .toConstantValue(env?.PIXIV_PW ?? '')
  .whenTargetNamed('pixiv');
container
  .bind<string>(TYPES.Name)
  .toConstantValue('pixiv')
  .whenTargetNamed('pixiv');

container
  .bind<string>(TYPES.Domain)
  .toConstantValue('pixiv.net')
  .whenParentNamed('pixiv');
container
  .bind<PixivScraper>(TYPES.ImageScraper)
  .to(PixivScraper)
  .inSingletonScope();

container
  .bind<PixivPostRequestListener>(TYPES.PixivPostRequestListener)
  .to(PixivPostRequestListener)
  .inSingletonScope();

export { container };
