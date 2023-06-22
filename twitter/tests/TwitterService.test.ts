import { env } from 'node:process';
import { beforeAll, beforeEach, describe, expect, it } from 'vitest';
import { TwitterService } from '../app/services/TwitterService';

describe('twitter service', () => {
  let twitterService!: TwitterService;
  beforeAll(() => {
    expect(env.TWITTER_TOKENS).toBeTruthy();
  })

  beforeEach(() => {
    twitterService = new TwitterService(env.TWITTER_TOKENS);
  })

  it('should get a tweet as guest', async () => {
    const tweet = twitterService
      .getTweet("https://twitter.com/cat_auras/status/1666426672843825156", false);
    expect(tweet).resolves.not.toHaveProperty("errors");
    expect(tweet).resolves.toHaveProperty("id_str");
  })

  it('should fail to get a sensitive tweet as guest', () => {

    twitterService = new TwitterService();
    expect(twitterService.authTokens).toHaveLength(0);

    const tweet = twitterService
      .getTweet("https://twitter.com/kafrizzzle/status/1630287826867609603", false);
    expect(tweet).rejects.toHaveProperty("errors");

  })

  it('should get a tweet as authenticated user', () => {
    twitterService = new TwitterService(env.TWITTER_TOKENS);
    expect(twitterService.authTokens.size).toBeGreaterThan(0);

    const tweet = twitterService
      .getTweet("https://twitter.com/kafrizzzle/status/1630287826867609603", false);
    expect(tweet).resolves.not.toHaveProperty("errors");
    expect(tweet).resolves.toHaveProperty("id_str");
  })

})