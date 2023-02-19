import { AuthableWebScraper } from './AuthableWebScraper';
import { RetrieveLinkStrategy } from './RetrieveLinkStrategy';

export class TestScraper implements AuthableWebScraper {
  constructor(readonly strategy: RetrieveLinkStrategy) {}
  login(): Promise<boolean | void> {
    throw new Error('Method not implemented.');
  }
  scrape(uri: string): Promise<String> {
    throw new Error('Method not implemented.');
  }
}
