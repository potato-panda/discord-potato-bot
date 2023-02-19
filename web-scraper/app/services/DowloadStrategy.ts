import { DownloadService } from './DownloadService';

export interface DownloadStrategy {
  location: string;
  service: DownloadService;
  download: (links: string[]) => Promise<string | boolean | void>;
}
