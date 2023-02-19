import { Response } from 'playwright';
import { RetrieveLinkStrategy } from './RetrieveLinkStrategy';

export class LinksFromAtagStrategy extends RetrieveLinkStrategy {
  async processResponse<T>(res: Response): Promise<T[]> {
    const matchingEls = await res.frame().locator('a[href]', {}).all();
    const links = (
      await Promise.all(
        matchingEls.map(async (el) => await el.getAttribute('href')),
      )
    ).filter((link) => link) as T[];
    return links;
  }
}
