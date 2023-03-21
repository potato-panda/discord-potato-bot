export interface FileDownloadMetadata {
  mimeType: string;
  size: number;
  fileName: string;
  fileExtension: string;
  key: string;
}

export interface FileDownloadsResponse {
  metadata?: FileDownloadMetadata;
  success: boolean;
  message: string;
}

