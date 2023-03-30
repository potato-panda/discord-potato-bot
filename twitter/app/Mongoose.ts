import { error, log } from 'console';
import mongoose from 'mongoose';

export async function connectMongo() {
  await mongoose
    .connect(`mongodb://${process.env.MONGO_NAME || '127.0.0.1'}:27017`, {
      dbName: 'bot-app',
      auth: {
        username: process.env.MONGO_INITDB_ROOT_USERNAME,
        password: process.env.MONGO_INITDB_ROOT_PASSWORD,
      },
    })
    .then((mongoose) => {
      log('Connected to Mongo');
    })
    .catch((err) => {
      error('Mongo connection error :', err);
    });
}

connectMongo();
