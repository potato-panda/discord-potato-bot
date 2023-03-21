import { error, log } from "console";
import { inject, injectable } from "inversify";
import { Msg, StringCodec } from "nats";
import { TweetExtendedEntitiesV1 } from "twitter-api-v2";
import { inspect } from "util";
import { DownloadService } from "../services/DownloadService";
import { FileDownloadsResponse } from "../services/responses/FileDownloadResponse";
import { TwitterService } from "../services/TwitterService";
import { TYPES } from "../Types";
import { safelyStringify } from "../utils/String";
import { createHttpRequest } from "../utils/Web";
import { Listener } from "./Listener";
import { TwitterPostRequestReply } from "./reply/TwitterPostRequestReply";
import { TwitterPostRequestEvent } from "./TwitterPostRequestEvent";

@injectable()
export class TwitterPostRequestListener extends Listener<TwitterPostRequestEvent> {
	subject = "twitter.post.request";
	queueName = "twitter-post-service-queue";
	downloadKey = "twitter:media";

	constructor(
		@inject(TYPES.TwitterService)
		private service: TwitterService,
		@inject(TYPES.DownloadService)
		private downloader: DownloadService,
	) {
		super();
	}

	async onMessage(msg: Msg, data: { url: string }): Promise<void> {
		const sc = StringCodec();
		try {
			const {
				full_text,
				extended_entities,
				user: { id_str, name, screen_name, url, profile_image_url_https },
				retweet_count,
				favorite_count,
				possibly_sensitive,
				created_at,
				quote_count,
				reply_count,
			} = await this.service.getOneTweet(data.url);

			const reply: TwitterPostRequestReply = {
				metadata: {
					content: full_text ?? null,
					userName: name,
					userScreenName: screen_name,
					userUrl: url,
					userThumbnailUrl: profile_image_url_https,
					retweets: retweet_count,
					favourites: favorite_count,
					suggestive: possibly_sensitive ?? false,
					createdAt: created_at,
				},
				downloadResponses: await this.getMedia(extended_entities),
			};

			log("Response: ", inspect(reply, false, null, true));

			msg.respond(sc.encode(safelyStringify(reply)));
		} catch (err) {
			error(err);

			msg.respond(
				sc.encode(
					safelyStringify({ success: false, message: safelyStringify(err) }),
				),
			);
		}
	}

	async getMedia(
		extendedEntities: TweetExtendedEntitiesV1 | undefined,
	): Promise<FileDownloadsResponse[]> {
		if (!extendedEntities?.media) return [{ success: false, message: "" }];
		const requests = [];
		for (const media of extendedEntities.media) {
			const imgLink = media.media_url_https;
			const url = new URL(imgLink);

			const req = createHttpRequest(url);
			const response = await this.downloader
				.download(req, this.downloadKey)
				.then((response) => Promise.resolve(response))
				.catch((reason) =>
					Promise.resolve<FileDownloadsResponse>({
						success: false,
						message: safelyStringify(reason),
					}),
				);
			requests.push(response);
		}

		return (await Promise.allSettled(requests)).map((result) => {
			if (result.status === "fulfilled") {
				return result.value;
			}
			return result.reason;
		});
	}
}
