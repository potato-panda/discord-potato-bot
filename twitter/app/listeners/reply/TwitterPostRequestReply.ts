import { FileDownloadsResponse } from "../../services/responses/FileDownloadResponse";

export type TwitterPostMetadata = {
	content: string | null;
	userName: string;
	userScreenName: string;
	userUrl: string | null;
	userThumbnailUrl: string;
	retweets: number;
	favourites: number;
	suggestive: boolean;
	createdAt: string;
};

export interface TwitterPostRequestReply {
	downloadResponses: FileDownloadsResponse[];
	metadata: TwitterPostMetadata;
}
