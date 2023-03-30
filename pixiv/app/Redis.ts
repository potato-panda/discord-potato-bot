import { log } from 'console';
import { createClient } from 'redis';

const { REDIS_NAME, REDIS_PASS } = process.env;

export const redis = createClient({
  password: REDIS_PASS,
  socket: {
    host: REDIS_NAME,
    reconnectStrategy(retries, cause) {
      if (retries > 10) {
        return new Error(`Too many retries. Latest cause: ${cause}`);
      }

      return retries;
    },
  },
})
  .on('connect', () => log('Connected to Redis'))
  .on('error', (err: Error) => log('Redis connection error :', err.message));

(async () => await redis.connect())();
