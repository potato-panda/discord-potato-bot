import { error, log } from 'node:console';
import mongoose from 'mongoose';

async function connectMongo() {
  const connection = mongoose.connect(
    `mongodb://${process.env.MONGO_NAME || 'localhost'}:27017`,
    {
      dbName: 'bot-app',
      auth: {
        username: process.env.MONGO_INITDB_ROOT_USERNAME,
        password: process.env.MONGO_INITDB_ROOT_PASSWORD,
      },
    },
  );
  connection.then(() => {
    log('Connected to Mongo');
  });
  connection.catch((err) => {
    error(`Mongo connection error : ${err?.message ?? err}`);
  });
  const client = await connection;
  const bucket = new client.mongo.GridFSBucket(client.connection.db, {
    chunkSizeBytes: 1048576,
  });
  return bucket;
}

export const bucket = connectMongo();
