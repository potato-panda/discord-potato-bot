export interface PixivPostResponse {
  error: boolean;
  message: string;
  body: Body;
}

export interface Body {
  illustId: string;
  illustTitle: string;
  illustComment: string;
  id: string;
  title: string;
  description: string;
  illustType: number;
  createDate: string;
  uploadDate: string;
  restrict: number;
  xRestrict: number;
  sl: number;
  urls: Urls;
  tags: Tags;
  alt: string;
  storableTags: string[];
  userId: string;
  userName: string;
  userAccount: string;
  userIllusts: { [key: string]: UserIllust | null };
  likeData: boolean;
  width: number;
  height: number;
  pageCount: number;
  bookmarkCount: number;
  likeCount: number;
  commentCount: number;
  responseCount: number;
  viewCount: number;
  bookStyle: string;
  isHowto: boolean;
  isOriginal: boolean;
  imageResponseOutData: unknown[];
  imageResponseData: unknown[];
  imageResponseCount: number;
  pollData: null;
  seriesNavData: null;
  descriptionBoothId: null;
  descriptionYoutubeId: null;
  comicPromotion: null;
  fanboxPromotion: FanboxPromotion;
  contestBanners: unknown[];
  isBookmarkable: boolean;
  bookmarkData: null;
  contestData: null;
  zoneConfig: ZoneConfig;
  extraData: ExtraData;
  titleCaptionTranslation: TitleCaptionTranslation;
  isUnlisted: boolean;
  request: null;
  commentOff: number;
  aiType: number;
}

export interface ExtraData {
  meta: Meta;
}

export interface Meta {
  title: string;
  description: string;
  canonical: string;
  alternateLanguages: AlternateLanguages;
  descriptionHeader: string;
  ogp: Ogp;
  twitter: Ogp;
}

export interface AlternateLanguages {
  ja: string;
  en: string;
}

export interface Ogp {
  description: string;
  image: string;
  title: string;
  type?: string;
  card?: string;
}

export interface FanboxPromotion {
  userName: string;
  userImageUrl: string;
  contentUrl: string;
  description: string;
  imageUrl: string;
  imageUrlMobile: string;
  hasAdultContent: boolean;
}

export interface Tags {
  authorId: string;
  isLocked: boolean;
  tags: Tag[];
  writable: boolean;
}

export interface Tag {
  tag: string;
  locked: boolean;
  deletable: boolean;
  userId?: string;
  userName?: string;
  romaji?: string;
  translation?: Translation;
}

export interface Translation {
  en: string;
}

export interface TitleCaptionTranslation {
  workTitle: null;
  workCaption: null;
}

export interface Urls {
  mini: string;
  thumb: string;
  small: string;
  regular: string;
  original: string;
}

export interface UserIllust {
  id: string;
  title: string;
  illustType: number;
  xRestrict: number;
  restrict: number;
  sl: number;
  url: string;
  description: string;
  tags: string[];
  userId: string;
  userName: string;
  width: number;
  height: number;
  pageCount: number;
  isBookmarkable: boolean;
  bookmarkData: null;
  alt: string;
  titleCaptionTranslation: TitleCaptionTranslation;
  createDate: string;
  updateDate: string;
  isUnlisted: boolean;
  isMasked: boolean;
  aiType: number;
  profileImageUrl?: string;
}

export interface ZoneConfig {
  responsive: The500_X500;
  rectangle: The500_X500;
  '500x500': The500_X500;
  header: The500_X500;
  footer: The500_X500;
  expandedFooter: The500_X500;
  logo: The500_X500;
  relatedworks: The500_X500;
}

export interface The500_X500 {
  url: string;
}
