import os from 'os';
import { log } from 'console';
import { ConnectionOptions } from 'nats';

export const MAX_WORKERS = 1;
export const CPUs = os.cpus().length;

export const appCPUs = CPUs >= MAX_WORKERS ? MAX_WORKERS : CPUs;

export const appPort = 3000;

export const natsServers: ConnectionOptions = { servers: 'localhost:4222' };

log({ CPUs: CPUs, appCPUs: appCPUs, appPort: appPort });

export const userAgent = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/111.0.0.0 Safari/537.36 Edg/111.0.1661.41';