import { log } from 'console';
import { inject, injectable } from 'inversify';
import { Cookie } from 'playwright';
import { redis } from '../Redis';
import { TYPES } from '../Types';
import { safelyStringify as safelyStringify } from '../utils/String';

type LocalStorage = {
  name: string;
  value: string;
};

type Origin = {
  origin: string;
  localStorage: LocalStorage[];
};

export type StorageState = {
  cookies: Cookie[];
  origins: Origin[];
};

@injectable()
export class BrowserStorageStateService {
  // @inject(TYPES.Domain) protected readonly domain!: string;
  constructor(@inject(TYPES.Domain) protected readonly domain: string) {}

  async retrieveState(): Promise<StorageState> {
    const value = await redis.get(this.domain);
    value && log(`[Storate State:${this.domain}] retrieved`);
    return value ? JSON.parse(value) : null;
  }

  async storeState(state: StorageState): Promise<void> {
    await redis.set(this.domain, safelyStringify(state));
    log(`[Storate State:${this.domain}] stored`);
    return;
  }
}
