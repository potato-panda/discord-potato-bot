import { error, log } from 'node:console';
import { ClientRequest } from 'node:http';

export async function download(request: ClientRequest)
  : Promise<FileDownload.Response> {
  const downloadPromise = new Promise<FileDownload.Response>(
    (resolve, reject) => {
      request
        .on('response', (response) => {
          const { statusCode, headers } = response;

          const { protocol, host, path } = request;

          const { href, pathname } = new URL(protocol + host + path);

          if (statusCode !== 200) {
            reject(new Error(`(${statusCode}) Failed to fetch ${href}`));
          }

          const contentType = headers['content-type'] as string;
          const contentSize = parseInt(
            headers['content-length'] as string,
            10,
          );

          const fileNameWithExtension = fileNameFromPathName(pathname);

          const [fileName, fileExtension] = fileNameWithExtension.split('.');

          const fileDownloadMetadata: FileDownload.Metadata = {
            mimeType: contentType,
            size: contentSize,
            fileName: fileName,
            fileExtension: fileExtension,
          };

          const chunks: Buffer[] = [];
          response
            .on('data', (chunk) => {
              chunks.push(chunk);
            })
            .on('end', async () => {
              const data = Buffer.concat(chunks);

              log('Finished Downloading: ', fileDownloadMetadata);
              resolve({
                metadata: fileDownloadMetadata,
                data,
                success: true,
                message: '',
              });
            })
            .on('error', (err) => {
              error('Error encoding image: ', err.message);
              reject('Error encoding image');
            });
        })
        .on('finish', () => { })
        .on('error', (err) => {
          error('Error writing chunks: ', err.message);
          reject('Error writing chunks');
        });
    },
  );

  request.end();

  return downloadPromise;
}

function fileNameFromPathName(path: string) {
  return path.split('/').slice(-1)[0];
}


export namespace FileDownload {
  export interface Metadata {
    mimeType: string;
    size: number;
    fileName: string;
    fileExtension: string;
  }

  export interface Response {
    metadata: Metadata;
    data: Buffer;
    success: boolean;
    message: string;
  }
}
