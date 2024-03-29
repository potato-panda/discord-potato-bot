import mongoose from 'mongoose';
import { Readable } from 'node:stream';
import { bucket } from '../Mongoose';
import { FileDownload } from './download';

export async function uploadStream(
  fileMetadata: FileDownload.Metadata,
  data: Buffer | string,
) {
  const b = await bucket;
  return new Promise<mongoose.mongo.GridFSBucketWriteStream>(
    (resolve, reject) => {
      const readableData = new Readable();
      readableData.push(data);
      readableData.push(null);

      const ws = b.openUploadStream(fileMetadata.fileName, {
        contentType: fileMetadata.mimeType,
      });
      const upstream = readableData.pipe(ws);

      upstream.on('finish', () => {
        // console.log('Uploaded: ', {
        //   length: ws.length,
        //   filename: ws.filename
        // });
        resolve(ws);
      });

      upstream.on('error', (err) => {
        reject(err.message);
      });
    },
  );
}
