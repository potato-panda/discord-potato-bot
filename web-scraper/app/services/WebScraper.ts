import { DownloadStrategy } from './DowloadStrategy';
import { RetrieveLinkStrategy } from './RetrieveLinkStrategy';

export interface WebScraper {
  strategy: RetrieveLinkStrategy;
  downloader: DownloadStrategy;
  scrape(uri: string): Promise<String>;
}
