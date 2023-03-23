import { FileDownloadResponse } from '../../services/DownloadService';

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

export type ReplyFileDownloadResponse = Omit<
  FileDownloadResponse & { key: string },
  'data'
>;

export interface TwitterPostRequestReply {
  downloadResponses: ReplyFileDownloadResponse[];
  metadata: TwitterPostMetadata;
}
