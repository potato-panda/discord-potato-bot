import { WebScraper } from './WebScraper';

export interface AuthableWebScraper extends WebScraper {
  login(): Promise<boolean | void>;
}
