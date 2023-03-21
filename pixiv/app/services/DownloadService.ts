import { error, log } from 'console';
import { ClientRequest } from 'http';
import { injectable } from 'inversify';
import { redis } from '../Redis';
import { safelyStringify } from '../utils/String';
import {
  FileDownloadMetadata,
  FileDownloadsResponse,
} from './responses/FileDownloadResponse';

@injectable()
export class DownloadService {
  async download(request: ClientRequest, downloadKey: string) {
    const promise = new Promise<FileDownloadsResponse>((resolve, reject) => {
      request
        .on('response', (res) => {
          const { statusCode, headers } = res;

          const { protocol, host, path } = request;

          const { href, pathname } = new URL(protocol + host + path);

          if (statusCode !== 200) {
            reject(new Error(`(${statusCode}) Failed to fetch ${href}`));
          }

          // check content type and length
          const contentType = headers['content-type'] as string;
          const contentSize = parseInt(headers['content-length'] as string, 10);

          if (!contentType) {
            reject(new Error('Content not downloadable'));
          }

          // determine file name
          const fileNameWithExtension = this.fileNameFromPathName(pathname);

          const [fileName, fileExtension] = fileNameWithExtension.split('.');

          // determine stored file key
          const fileKey = `${downloadKey}:${fileName}`;

          // create info
          const fileDownloadMetadata: FileDownloadMetadata = {
            mimeType: contentType,
            size: contentSize,
            fileName: fileName,
            fileExtension: fileExtension,
            key: fileKey,
          };

          const chunks: Buffer[] = [];
          res
            .on('data', (chunk) => {
              chunks.push(Buffer.from(chunk));
            })
            .on('end', async () => {
              try {
                const base64string = Buffer.concat(chunks).toString('base64');
                await redis.setEx(fileKey, 1_000 * 60 * 60, base64string);
                log('Finished Downloading: ', fileDownloadMetadata);
                resolve({
                  metadata: fileDownloadMetadata,
                  success: true,
                  message: '',
                });
              } catch (err) {
                error('Error storing to Redis: ', err);
                this.resolveError('Error storing to Redis');
              }
            })
            .on('error', (err) => {
              error('Error encoding image: ', err);
              this.resolveError('Error encoding image');
            });
        })
        .on('finish', () => {})
        .on('error', (err) => {
          error('Error on response: ', err);
          this.resolveError('Error on response');
        });
    });

    request.end();

    return promise;
  }

  private fileNameFromPathName(path: string) {
    return path.split('/').slice(-1)[0];
  }

  private resolveError(
    error: Error | string | unknown,
  ): Promise<FileDownloadsResponse> {
    let msg = '';
    switch (error) {
      case typeof Error:
        msg = (error as Error).message;
        break;
      case typeof 'string':
        msg = error as string;
        break;
      default:
        msg = safelyStringify(msg);
        break;
    }
    return Promise.resolve({
      message: msg,
      success: false,
    });
  }
}
