import { FileDownload } from '../utils/download';

export namespace TwitterPost {
  export interface Request {
    url: string;
  }

  export interface Metadata {
    content: string | null;
    userName: string;
    userScreenName: string;
    userUrl: string | null;
    userThumbnailUrl: string;
    retweets: number;
    favourites: number;
    suggestive: boolean;
    createdAt: string;
    embedColour: string;
  }

  export type FileDownloadResponse = Omit<
    FileDownload.Response & { key: string },
    'data'
  >;

  export interface Reply {
    downloadResponses: FileDownloadResponse[];
    metadata: Metadata;
  }
}
