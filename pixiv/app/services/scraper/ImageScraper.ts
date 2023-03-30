import { injectable } from 'inversify';
import { BrowserContext, chromium, devices } from 'playwright';
import { BrowserStorageStateService } from '../BrowserStorageStateService';

@injectable()
export abstract class ImageScraper {
  protected browserContext!: BrowserContext;
  // protected eventListener: EventEmitter;
  constructor(
    public name: string,
    protected storageStateService: BrowserStorageStateService,
  ) {
    // this.eventListener = new EventEmitter();
  }

  public async initThen(postInitFn?: () => Promise<void>) {
    const browserCtx = (
      await chromium.launch({
        headless: !process.env.DEBUG || !!process.env.HEADLESS,
      })
    ).newContext({
      ...devices['Desktop Chrome'],
      storageState: (await this.storageStateService.retrieveState()) ?? {},
    });
    browserCtx.then(async (ctx) => {
      // console.log('Context used: ', await ctx.storageState());
    });

    this.browserContext = await browserCtx;
    if (postInitFn) {
      await postInitFn();
    }
  }

  abstract login(user: string, password: string): Promise<void>;

  /**
   * Takes uri and provides a json response which is then processed by supplied callback
   * @param uri
   * @param processResponse
   */
  abstract scrape<R, D>(
    uri: string,
    processResponse: (response: R) => D,
  ): Promise<D>;
}
