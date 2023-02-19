import { Response } from 'playwright';
import { RetrieveLinkStrategy } from './RetrieveLinkStrategy';

export class LinksFromJsonStrategy extends RetrieveLinkStrategy {
  async processResponse(res: Response) {
    const json: string = await res.json();
    const stringified = JSON.stringify(json);
    const regex = /(["'])((?:(?=(?:\\)*)\\.|.)*?)\1/gm;
    const links = (stringified.match(regex) ?? [])
      .filter((s) => s)
      .map((s) => s.replaceAll('"', '').trim());

    return links;
  }
}
