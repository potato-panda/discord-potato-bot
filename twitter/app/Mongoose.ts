import { error, log } from 'node:console';
import mongoose from 'mongoose';

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
  const { mongo } = (await c);
  const bucket = new mongo.GridFSBucket(conn.connection.db, { chunkSizeBytes: 1048576, });
  return bucket;
}

export const bucket = connectMongo();
