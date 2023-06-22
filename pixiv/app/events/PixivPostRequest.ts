import { FileDownload } from '../services/FileDownload';

export namespace PixivPost {
  export interface Request {
    postId: string;
    quality: 'ORIGINAL' | 'REGULAR';
  }
  
  export interface Metadata {
    explicit: boolean;
    tags: string[];
    url: string;
    title: string;
    description: string;
    userName: string;
    userAccount: string;
    isAi: boolean;
    favourites: number;
    createdAt: string;
    illustType: 'illust' | 'ugoira' | 'manga';
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
