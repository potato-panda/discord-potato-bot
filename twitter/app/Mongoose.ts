import { error, log } from 'node:console';
import mongoose from 'mongoose';

export async function connectMongo() {
  return await mongoose
    .connect(`mongodb://${process.env.MONGO_NAME || 'localhost'}:27017`, {
      dbName: 'bot-app',
      auth: {
        username: process.env.MONGO_INITDB_ROOT_USERNAME,
        password: process.env.MONGO_INITDB_ROOT_PASSWORD,
      },
    })
    .then(() => {
      log('Connected to Mongo');
    })
    .catch((err) => {
      error(`Mongo connection error : ${err?.message || err}`);
    });
}

connectMongo();
