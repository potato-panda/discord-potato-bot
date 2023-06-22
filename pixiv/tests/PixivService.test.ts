import { env } from 'node:process';
import { PixivApi } from 'pixiv-api-wrapper';
import { beforeAll, describe, expect, it } from 'vitest';
import { PixivService } from '../app/services/PixivService';

describe('pixiv service', () => {
  let pixivService!: PixivService;
  beforeAll(async () => {
    expect(env.PIXIV_RT).toBeTruthy();

    const pixivClient = await PixivApi.create(env.PIXIV_RT ?? "");

    pixivService = new PixivService(pixivClient);
  })

  it('should get a illust json', async () => {
    const illust = pixivService.getIllust("60895189");
    expect(illust).resolves.toHaveProperty("illustMetadata");
  })

})