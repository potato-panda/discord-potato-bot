import { ConnectionOptions } from 'nats';

export const appPort = 3001;

export const natsServers: ConnectionOptions = {
  servers: `${process.env.NATS_NAME}:4222`,
  user: process.env.NATS_USER,
  pass: process.env.NATS_PASS,
};

export const userAgent =
  'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/111.0.0.0 Safari/537.36 Edg/111.0.1661.41';
