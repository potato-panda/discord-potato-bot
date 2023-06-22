import mongoose from 'mongoose';
import { error, log } from 'node:console';

async function connectMongo() {
  const c = mongoose
    .connect(`mongodb://${process.env.MONGO_NAME || 'localhost'}:27017`, {
      dbName: 'bot-app',
      auth: {
        username: process.env.MONGO_INITDB_ROOT_USERNAME,
        password: process.env.MONGO_INITDB_ROOT_PASSWORD,
      },
    })
  c.then(() => {
    log('Connected to Mongo');
  })
  c.catch((err) => {
    error(`Mongo connection error : ${err?.message || err}`);
  });
  const conn = await c;
  const bucket = new conn.mongo.GridFSBucket(conn.connection.db, {
    chunkSizeBytes: 1048576,
  });
  return bucket;
}

export const bucket = connectMongo();
