import { FileDownloadsResponse } from '../../services/responses/FileDownloadResponse';

export type PixivPostMetadata = {
  adult: boolean;
  tags: string[];
  url: string;
  title: string;
  description: string;
  userName: string;
  userAccount: string;
  likes: number;
  hearts: number;
  createdAt: string;
};

export interface PixivPostRequestReply {
  downloadResponses: FileDownloadsResponse[];
  metadata: PixivPostMetadata;
}
