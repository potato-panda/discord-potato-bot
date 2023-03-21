import { inject, injectable } from "inversify";
import { TwitterApi } from "twitter-api-v2";
import TwitterApiv1ReadOnly from "twitter-api-v2/dist/esm/v1/client.v1.read";
import { TYPES } from "../Types";

@injectable()
export class TwitterService {
	api: TwitterApiv1ReadOnly;
	constructor(
		@inject(TYPES.TwitterApiKey) apiKey: string,
		@inject(TYPES.TwitterApiSecret) apiSecret: string,
	) {
		this.api = new TwitterApi({
			appKey: apiKey,
			appSecret: apiSecret,
		}).readOnly.v1;
	}

	async getOneTweet(url: string) {
		const { pathname } = new URL(url);
		const tweetId = pathname.split("/").splice(-1)[0];
		return await this.api.singleTweet(tweetId);
	}
}
