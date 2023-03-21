import { log } from 'console';
import https from 'https';
import { inject, injectable } from 'inversify';
import { userAgent } from '../../../constants.config';
import { TYPES } from '../../../Types';
import { StorageState } from '../../BrowserStorageStateService';
import { DownloadService } from '../../DownloadService';
import { FileDownloadsResponse } from '../../responses/FileDownloadResponse';
import { DownloadStrategy } from './DownloadStrategy';

@injectable()
export class PixivDownloadStrategy implements DownloadStrategy {
  readonly downloadKey: string = 'pixiv:illust';
  constructor(
    @inject(TYPES.DownloadService) readonly service: DownloadService,
  ) {}
  async download(
    uris: string[],
    _state?: unknown,
  ): Promise<FileDownloadsResponse[]> {
    // TODO
    let state = _state as StorageState;
    let cookie = state?.cookies?.find((c) => c.name === 'PHPSESSID');
    if (!cookie) {
      console.log('Error bad cookie');
      return [];
    }

    const cookieString = `PHPSESSID=${cookie.value}; Path=/; Domain=pixiv.net; Secure; HttpOnly;`;

    const clientRequests = uris.map((uri) => {
      const url = new URL(uri);
      return https.request(url, {
        headers: {
          Accept: '*/*',
          Cookie: cookieString,
          Referer: 'https://www.pixiv.net',
          'User-Agent': userAgent,
        },
      });
    });

    return (
      await Promise.allSettled(
        clientRequests.map(
          async (request) =>
            await this.service.download(request, this.downloadKey),
        ),
      )
    ).map((result) => {
      if (result.status === 'fulfilled') {
        return result.value;
      }
      return result.reason;
    });
  }
}
