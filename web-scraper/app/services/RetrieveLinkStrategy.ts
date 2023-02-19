import { BrowserContext, Response } from 'playwright';

export abstract class RetrieveLinkStrategy {
  constructor(public context: BrowserContext) {}

  async retrieveLinks(uri: string) {
    const page = await this.context.newPage();
    let res = await page.goto(uri);
    await page.close();
    if (res) {
      return await this.processResponse(res);
    } else {
      throw new Error('Response is null');
    }
  }

  protected abstract processResponse(res: Response): Promise<string[]>;
}
