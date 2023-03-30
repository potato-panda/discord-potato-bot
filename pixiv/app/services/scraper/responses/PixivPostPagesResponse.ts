export interface PixivPostPagesResponse<T extends Body_Gif | Body_Image[],> {
  error: boolean;
  message: string;
  body: T;
}

export interface Body_Image {
  urls: Urls;
  width: number;
  height: number;
}

export interface Urls {
  thumb_mini: string;
  small: string;
  regular: string;
  original: string;
}

export interface Body_Gif {
  src: string;
  originalSrc: string;
  mime_type: string;
  frames: Frame[];
}

export interface Frame {
  file: string;
  delay: number;
}
