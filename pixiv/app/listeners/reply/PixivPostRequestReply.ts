import { FileDownloadResponse } from '../../services/DownloadService';

export type PixivPostMetadata = {
  adult: boolean;
  tags: string[];
  url: string;
  title: string;
  description: string;
  userName: string;
  userAccount: string;
  isAi: boolean;
  likes: number;
  favourites: number;
  createdAt: string;
  illustType: 'gif' | 'jpg';
};

export type ReplyFileDownloadResponse = Omit<
  FileDownloadResponse & { key: string },
  'data'
>;

export interface PixivPostRequestReply {
  downloadResponses: ReplyFileDownloadResponse[];
  metadata: PixivPostMetadata;
}
