import { FileDownloadsResponse } from "../../responses/FileDownloadResponse";

export interface DownloadStrategy {
  downloadKey: string;
  download: (
    links: string[],
    _state?: unknown,
  ) => Promise<FileDownloadsResponse[]>;
}
