import { log } from 'console';
import { createClient } from 'redis';

export const redis = createClient({
  socket: {
    reconnectStrategy(retries, cause) {
      if (retries > 10) {
        return new Error(`Too many retries. Latest cause: ${cause}`);
      }

      return retries;
    },
  },
});
redis.on('error', (err: Error) =>
  log('Redis Client connection error: ', err.message),
);
redis.on('connect', () => log('Connected to Redis'));

(async () => await redis.connect())();
