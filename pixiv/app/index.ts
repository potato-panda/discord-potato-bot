import cluster from 'cluster';
import { log } from 'console';
import process from 'process';

import 'reflect-metadata';

import { appCPUs } from './constants.config';
import { worker } from './Worker';

log();
if (cluster.isPrimary && !process.env.DEBUG) {
  log(`Primary ${process.pid} is running`);

  for (let i = 0; i < appCPUs; i++) {
    cluster.fork();
  }

  cluster.on('exit', (worker, code, signal) => {
    log(`Worker ${worker.id} (PID:${worker.process.pid}) died`);
  });
} else {
  worker();
}
