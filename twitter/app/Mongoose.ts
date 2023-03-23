import { log } from 'console';
import mongoose from 'mongoose';

export async function connectMongo() {
  try {
    await mongoose.connect('mongodb://127.0.0.1:27017', {
      dbName: 'bot-app',
    });
    log('Connected to Mongo');
  } catch (err) {
    log('Error connecting to Mongo');
  }
}

connectMongo();
