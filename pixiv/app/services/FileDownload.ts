
export namespace FileDownload {
  export interface Metadata {
    mimeType: string;
    size: number;
    fileName: string;
    fileExtension: string;
  }

  export interface Response {
    metadata: Metadata;
    data: Buffer;
    success: boolean;
    message: string;
  }
}
