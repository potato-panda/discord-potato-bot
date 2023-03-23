import { error, log, time, timeEnd, timeLog } from 'console';
import { BitmapImage, GifCodec, GifFrame, GifUtil } from 'gifwrap';
import https from 'https';
import { inject, injectable, named } from 'inversify';
import Jimp from 'jimp';
import { Page } from 'playwright';
import { userAgent } from '../../constants.config';
import {
  PixivPostMetadata as PixivPostRequestMetadata,
  ReplyFileDownloadResponse,
} from '../../listeners/reply/PixivPostRequestReply';
import { PixivPostRequestModel } from '../../models/PixivPostRequest';
import { TYPES } from '../../Types';
import { BrowserStorageStateService } from '../BrowserStorageStateService';
import { DownloadService, FileDownloadMetadata } from '../DownloadService';
import { ImageScraper } from './ImageScraper';
import {
  Body_Gif,
  Body_Image,
  PixivPostPagesResponse,
} from './responses/PixivPostPagesResponse';
import { PixivPostResponse } from './responses/PixivPostResponse';

@injectable()
export class PixivScraper extends ImageScraper {
  @inject(TYPES.Username)  @named('pixiv') private user!: string;
  @inject(TYPES.Password)  @named('pixiv') private password!: string;
  downloadKey: string = 'pixiv:illust';
  protected loggedIn = false;
  protected loggingIn = false;
  public constructor(
    @inject(TYPES.Name)  @named('pixiv') name: string,
    @inject(TYPES.DownloadService) readonly service: DownloadService,
    @inject(TYPES.BrowserStorageStateService) @named('pixiv') BrowserStorageStateService: BrowserStorageStateService,
  ) {
    super(name, BrowserStorageStateService);
    this.initThen(async () => this.login(this.user, this.password));
  }

  get isLoggedIn(): boolean {
    return this.loggedIn;
  }

  get isLoggingIn(): boolean {
    return this.loggingIn;
  }

  get status(): {
    status: string;
  } {
    return {
      status: this.isLoggingIn
        ? 'LoggingIn'
        : this.isLoggedIn
        ? 'LoggedIn'
        : 'LoggedOut',
    };
  }

  async checkIfLoggedIn(): Promise<{ loggedIn: boolean; page: Page }> {
    const page = await this.browserContext.newPage();
    await page.goto('https://accounts.pixiv.net/login', {
      waitUntil: 'domcontentloaded',
    });
    this.loggedIn = page.url().match('https://www.pixiv.net/') ? true : false;
    this.loggedIn ? log('Already Logged In') : log('Not Logged In');
    return { loggedIn: this.loggedIn, page: page };
  }

  async login(user = this.user, password = this.password): Promise<void> {
    const { loggedIn, page } = await this.checkIfLoggedIn();
    if (!(user && password)) {
      log(
        'Please provide PIXIV_ID and PIXIV_PW environment variables or set session.',
      );
      return;
    }
    if (!loggedIn) {
      this.loggingIn = true;
      log('Logging in');
      const idInput = page.getByPlaceholder('E-mail address or pixiv ID');
      await idInput.type(user);
      const pwInput = page.getByPlaceholder('Password');
      await pwInput.type(password);
      await page.getByText('Login').click();
      await page.waitForLoadState('domcontentloaded');
      log('URL? ', page.url());
      if (page.url().match('https://www.pixiv.net')) {
        this.loggedIn = true;
        await this.storageStateService.storeState(
          await this.browserContext.storageState(),
        );
        log('Login Successful');
      } else {
        this.loggedIn = false;
        log('Login Failed');
      }
      this.loggingIn = false;
    }
    log(`Current url: ${page.url()}`);
  }

  /**
   * Sets session to browser context
   * @param session String representation of a Cookie array
   */
  async setSession(session: string) {
    await this.browserContext.clearCookies();
    try {
      await this.browserContext.addCookies(JSON.parse(session));
    } catch (error) {
      console.error(error);
    }
    const pages = this.browserContext.pages().map((page) => {
      return Promise.resolve(page.reload());
    });
    await Promise.all(pages).finally(async () => {
      await this.storageStateService.storeState(
        await this.browserContext.storageState(),
      );
    });
  }

  async scrape<Response, Target>(
    uri: string,
    processResponse: (response: Response) => Target,
  ): Promise<Target> {
    if (!this.isLoggedIn) {
      log('Not Logged In');
    }
    // TODO use request over browser
    const ctx = this.browserContext;
    const page = await ctx.newPage();

    const res = await page.goto(uri);
    const json = (await res?.json()) as unknown as Response;

    if (!json) {
      throw Error(`Failed to scrape ${uri}`);
    }
    const links: Target = processResponse(json);

    page.close();
    return links;
  }

  async scrapePost(postId: string) {
    const postUrl = `https://www.pixiv.net/ajax/illust/${postId}`;
    const pagesUrls = `${postUrl}/pages`;
    const framesUrl = `${postUrl}/ugoira_meta`;

    const metadata = await this.scrape<
      PixivPostResponse,
      PixivPostRequestMetadata
    >(postUrl, (res) => {
      const {
        body: {
          xRestrict,
          tags: { tags },
          extraData: { meta: { title, description, canonical } },
          userName,
          userAccount,
          aiType,
          likeCount,
          bookmarkCount,
          createDate,
          illustType,
        },
      } = res;

      return {
        adult: Boolean(xRestrict),
        tags: tags.map((tags) => tags.tag),
        url: canonical,
        title,
        description,
        userName,
        userAccount,
        isAi: aiType === 2,
        likes: likeCount,
        favourites: bookmarkCount,
        createdAt: createDate,
        illustType: illustType === 2 ? 'gif' : 'jpg',
      } as PixivPostRequestMetadata;
    });

    return {
      metadata,
      pagesMetadata: await this.scrape<
        PixivPostPagesResponse<Body_Image[]>,
        Body_Image[]
      >(pagesUrls, (res) => res.body),
      gifMetadata: await this.scrape<
        PixivPostPagesResponse<Body_Gif>,
        Body_Gif
      >(framesUrl, (res) => res.body),
    };
  }

  async downloadByBody(
    postId: string,
    body: Body_Image[],
    quality?: string,
  ): Promise<ReplyFileDownloadResponse[]> {
    return await this.downloadFromBodyLinks(
      postId,
      body.map((page) => {
        switch (quality) {
          case 'ORIGINAL':
            return page.urls.original;
          default:
            return page.urls.regular;
        }
      }),
    );
  }

  private async downloadFromBodyLinks(
    postId: string,
    urls: string[],
  ): Promise<ReplyFileDownloadResponse[]> {
    const cookie = await this.getContextCookie();
    const requests = urls.map(async (s) => {
      const url = new URL(s);
      const request = https.request(url, {
        headers: {
          Accept: '*/*',
          Cookie: cookie,
          Referer: 'https://www.pixiv.net/',
          'User-Agent': userAgent,
        },
      });

      return await this.service.download(request, async (response) => {
        return new Promise<ReplyFileDownloadResponse>((resolve, reject) => {
          const { statusCode, headers } = response;

          const { protocol, host, path } = request;

          const { href, pathname } = new URL(protocol + host + path);

          if (statusCode !== 200) {
            reject(new Error(`(${statusCode}) Failed to fetch ${href}`));
          }

          const contentType = headers['content-type'] as string;
          const contentSize = parseInt(headers['content-length'] as string, 10);

          const fileNameWithExtension = this.fileNameFromPathName(pathname);
          const [fileName, fileExtension] = fileNameWithExtension.split('.');

          const chunks: Buffer[] = [];
          response
            .on('data', (chunk) => {
              chunks.push(Buffer.from(chunk));
            })
            .on('end', async () => {
              const data = Buffer.concat(chunks);
              const entry =
                (
                  await PixivPostRequestModel.findOne({
                    postId: postId,
                    key: fileName,
                  }).exec()
                )?.$set('data', data) ??
                new PixivPostRequestModel({
                  postId,
                  key: fileName,
                  data,
                });

              await entry.save();

              const fileDownloadMetadata: FileDownloadMetadata = {
                mimeType: contentType,
                fileName: fileName,
                fileExtension: fileExtension,
                size: contentSize,
              };

              log('Finished Downloading Page: ', fileDownloadMetadata);

              resolve({
                metadata: fileDownloadMetadata,
                key: fileName,
                success: true,
                message: '',
              });
            })
            .on('error', (err) => {
              error('Error writing chunks: ', err.message);
              reject('Error writing chunks');
            });
        });
      });
    });

    const results = (await Promise.allSettled(requests)).reduce(
      (results, result) => {
        log('page result');
        if (result.status === 'fulfilled') {
          results.push(result.value);
        }
        return results;
      },
      [] as ReplyFileDownloadResponse[],
    );

    console.log('results:', results);

    return results;
  }

  async downloadGif(
    postId: string,
    body: Body_Image,
    data: Body_Gif,
  ): Promise<ReplyFileDownloadResponse[]> {
    const cookie = await this.getContextCookie();

    const originalUrl = new URL(body.urls.original);
    const { href, pathname } = originalUrl;

    const frameRequests = data.frames.map(async ({ delay }, i) => {
      const frameUrl: string = originalUrl
        .toString()
        .replace('ugoira0', `ugoira${i}`);
      const url = new URL(frameUrl);

      const request = https.request(url, {
        headers: {
          Accept: '*/*',
          Cookie: cookie,
          Referer: 'https://www.pixiv.net/',
          'User-Agent': userAgent,
        },
      });

      return await this.service.download(request, async (response) => {
        return new Promise<{
          buffer: Buffer;
          delay: number;
        }>((resolve, reject) => {
          const buffer: Buffer[] = [];

          response
            .on('data', (chunk) => {
              buffer.push(chunk);
            })
            .on('end', () => {
              resolve({
                buffer: Buffer.concat(buffer),
                delay,
              });
            })
            .on('error', (err) => reject(err.message));
        });
      });
    });

    const gifFrameData = await Promise.all(frameRequests);

    const fileNameWithExtension = this.fileNameFromPathName(pathname);

    const [fileName, fileExtension] = fileNameWithExtension.split('.');

    const gifFrames: GifFrame[] = [];

    time('Creating Gif');
    for (const [i, { buffer, delay }] of gifFrameData.entries()) {
      const jimp = await Jimp.read(buffer);
      const jimpBitmap = jimp.bitmap;

      const bitmap = new BitmapImage(jimpBitmap);

      GifUtil.quantizeDekker(bitmap, 256);

      const frame = new GifFrame(bitmap, {
        delayCentisecs: delay / 10,
      });
      timeLog('Creating Gif', `Frame ${i}`);
      gifFrames.push(frame);
    }

    const { buffer } = await new GifCodec().encodeGif(gifFrames, {});
    timeEnd('Creating Gif');

    const size = buffer.length;

    const entry =
      (
        await PixivPostRequestModel.findOne({
          postId: postId,
          key: fileName,
        }).exec()
      )?.$set('data', buffer) ??
      new PixivPostRequestModel({
        postId,
        key: fileName,
        data: buffer,
      });

    await entry.save();

    const fileDownloadMetadata: FileDownloadMetadata = {
      mimeType: 'image/gif',
      fileName: fileName,
      fileExtension: 'gif',
      size,
    };

    log('Finished Gif: ', fileDownloadMetadata);

    const results: ReplyFileDownloadResponse[] = [
      {
        metadata: fileDownloadMetadata,
        key: fileName,
        success: true,
        message: '',
      },
    ];

    console.log('results:', results);

    return results;
  }

  private fileNameFromPathName(path: string) {
    return path.split('/').slice(-1)[0];
  }

  private async getContextCookie() {
    const state = await this.browserContext.storageState();
    let cookie = state?.cookies?.find((c) => c.name === 'PHPSESSID');
    if (!cookie) {
      console.log('Error bad cookie');
      return [];
    }
    const cookieString = `PHPSESSID=${cookie.value}; Path=/; Domain=pixiv.net; Secure; HttpOnly;`;

    return cookieString;
  }
}
