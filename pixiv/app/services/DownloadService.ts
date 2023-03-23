import { error } from 'console';
import { ClientRequest, IncomingMessage } from 'http';
import { injectable } from 'inversify';

@injectable()
export class DownloadService {
  async download<T = FileDownloadResponse>(
    request: ClientRequest,
    callback: (response: IncomingMessage) => Promise<T>,
  ) {
    const promise = new Promise<T>((resolve, reject) => {
      request
        .on('response', async (response) => resolve(await callback(response)))
        .on('finish', () => {})
        .on('error', (err) => {
          error('Error on response: ', err.message);
          reject('Error on response');
        });
    });

    request.end();

    return promise;
  }
}

export interface FileDownloadMetadata {
  mimeType: string;
  size: number;
  fileName: string;
  fileExtension: string;
}

export interface FileDownloadResponse {
  metadata: FileDownloadMetadata;
  data: Buffer;
  success: boolean;
  message: string;
}
