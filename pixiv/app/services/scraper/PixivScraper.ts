import { log } from 'console';
import { BrowserStorageStateService } from '../BrowserStorageStateService';
import { ImageScraper } from './ImageScraper';
import { DownloadStrategy } from './strategies/DownloadStrategy';

import { inject, injectable, named } from 'inversify';
import { Page } from 'playwright';
import { TYPES } from '../../Types';
import {
  Body,
  PixivPostPagesResponse,
} from './responses/PixivPostPagesResponse';
import { PixivPostResponse } from './responses/PixivPostResponse';
import { PixivPostMetadata } from '../../listeners/reply/PixivPostRequestReply';

@injectable()
export class PixivScraper extends ImageScraper {
  @inject(TYPES.Username)  @named('pixiv') private user!: string;
  @inject(TYPES.Password)  @named('pixiv') private password!: string;
  protected loggedIn = false;
  protected loggingIn = false;
  public constructor(
    @inject(TYPES.Name)  @named('pixiv') name: string,
    @inject(TYPES.DownloadToServerStrategy)  @named('pixiv') downloader: DownloadStrategy,
    @inject(TYPES.BrowserStorageStateService) @named('pixiv') BrowserStorageStateService: BrowserStorageStateService,
  ) {
    super(name, downloader, BrowserStorageStateService);
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

  /**
   * Scrape a url for HTML or JSON
   * @type {Response} Response object
   * @type {Target} Transform target object
   * @param uri
   * @param processResponse Custom response processsing
   * @returns A processed response
   */
  async scrape<Response, Target>(
    uri: string,
    processResponse: (response: Response) => Target,
  ): Promise<Target> {
    if (!this.isLoggedIn) {
      log('Not Logged In');
    }
    const ctx = this.browserContext;
    const page = await ctx.newPage();
    // Save state when page closes
    page.on('close', async (page) => {
      await this.storageStateService.storeState(
        await page.context().storageState(),
      );
    });

    const res = await page.goto(uri);
    const json = (await res?.json()) as unknown as Response;

    if (!json) {
      throw Error(`Failed to scrape ${uri}`);
    }
    const links: Target = processResponse(json);

    page.close();
    return links;
  }

  async scrapeImagePost(postId: string) {
    // get Post JSON
    const postUri = `https://www.pixiv.net/ajax/illust/${postId}`;
    // get Post's Pages JSON
    const pagesUri = `${postUri}/pages`;
    return {
      post: await this.scrape<PixivPostResponse, PixivPostMetadata>(
        postUri,
        (res) => {
          const {
            body: {
              xRestrict,
              tags,
              extraData: { meta: { title, description, canonical } },
              userName,
              userAccount,
              aiType,
              likeCount,
              bookmarkCount,
              createDate,
            },
          } = res;

          return {
            adult: Boolean(xRestrict),
            tags: tags.tags.map((tags) => tags.tag),
            url: canonical,
            title,
            description,
            userName,
            userAccount,
            isAi: aiType === 2,
            likes: likeCount,
            hearts: bookmarkCount,
            createdAt: createDate,
          };
        },
      ),
      pages: await this.scrape<PixivPostPagesResponse, Body[]>(
        pagesUri,
        (res) => res.body,
      ),
    };
  }

  async downloadFromLinks(uris: string[]) {
    const state = await this.browserContext.storageState();
    return this.downloader.download(uris, state);
  }
}
