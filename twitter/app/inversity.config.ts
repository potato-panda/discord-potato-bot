import { Container } from "inversify";
import { env } from "process";
import { TwitterPostRequestListener } from "./listeners/TwitterPostRequestListener";
import { TwitterService } from "./services/TwitterService";
import { DownloadService } from "./services/DownloadService";
import { TYPES } from "./Types";

const container = new Container();

container
	.bind<TwitterPostRequestListener>(TYPES.TwitterPostRequestListener)
	.to(TwitterPostRequestListener)
	.inSingletonScope();

container
	.bind<TwitterService>(TYPES.TwitterService)
	.to(TwitterService)
	.inRequestScope();
container
	.bind<DownloadService>(TYPES.DownloadService)
	.to(DownloadService)
	.inRequestScope();

container
	.bind<string>(TYPES.TwitterApiKey)
	.toConstantValue(env.TWITTER_API_KEY ?? "");
container
	.bind<string>(TYPES.TwitterApiSecret)
	.toConstantValue(env.TWITTER_API_SECRET ?? "");

export { container };
