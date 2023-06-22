export interface Tweet {
  created_at:                    string;
  id:                            number;
  id_str:                        string;
  full_text:                     string;
  truncated:                     boolean;
  display_text_range:            number[];
  entities:                      Entities;
  extended_entities:             ExtendedEntities;
  source:                        string;
  in_reply_to_status_id:         number | null;
  in_reply_to_status_id_str:     string | null;
  in_reply_to_user_id:           number | null;
  in_reply_to_user_id_str:       string | null;
  in_reply_to_screen_name:       string | null;
  user:                          User;
  geo:                           null;
  coordinates:                   Coordinate | null;
  place:                         Place | null;
  contributors:                  null;
  is_quote_status:               boolean;
  retweet_count:                 number;
  favorite_count:                number;
  reply_count:                   number;
  favorited:                     boolean;
  retweeted:                     boolean;
  possibly_sensitive:            boolean;
  possibly_sensitive_appealable: boolean;
  possibly_sensitive_editable:   boolean;
  lang:                          string;
  supplemental_language:         null;
  self_thread?:                  SelfThread;
  quoted_status_id?:             number;
  quoted_status_id_str?:         string;
  quoted_status_permalink?:      QuotedStatusPermalink;
  quoted_status?:                QuotedStatus;
}

export interface Entities {
  hashtags:      any[];
  symbols:       any[];
  user_mentions: UserMention[];
  urls:          URL[];
  media?:        Media[];
}

export interface Media {
  id:                     number;
  id_str:                 string;
  indices:                number[];
  media_url:              string;
  media_url_https:        string;
  url:                    string;
  display_url:            string;
  expanded_url:           string;
  type:                   Type;
  original_info:          OriginalInfo;
  sizes:                  Sizes;
  features:               Features;
  source_status_id?:      number;
  source_status_id_str?:  string;
  source_user_id?:        number;
  source_user_id_str?:    string;
  media_key?:             string;
  video_info?:            VideoInfo;
  additional_media_info?: AdditionalMediaInfo;
}

export interface AdditionalMediaInfo {
  monetizable: boolean;
}

export interface Features {
  large?:  OrigClass;
  orig?:   OrigClass;
  medium?: OrigClass;
  small?:  OrigClass;
}

export interface OrigClass {
  faces: FocusRect[];
}

export interface FocusRect {
  x: number;
  y: number;
  h: number;
  w: number;
}

export interface OriginalInfo {
  width:        number;
  height:       number;
  focus_rects?: FocusRect[];
}

export interface Sizes {
  large:  ThumbClass;
  thumb:  ThumbClass;
  medium: ThumbClass;
  small:  ThumbClass;
}

export interface ThumbClass {
  w:      number;
  h:      number;
  resize: Resize;
}

export type Resize = "fit" | "crop";

export type Type = "animated_gif" | "photo" | "video";

export interface VideoInfo {
  aspect_ratio:     number[];
  duration_millis?: number;
  variants:         Variant[];
}

export interface Variant {
  bitrate?:     number;
  content_type: string;
  url:          string;
}

export interface URL {
  url:          string;
  expanded_url: string;
  display_url:  string;
  indices:      number[];
}

export interface UserMention {
  screen_name: string;
  name:        string;
  id:          number;
  id_str:      string;
  indices:     number[];
}

export interface ExtendedEntities {
  media: Media[];
}

export interface QuotedStatus {
  created_at:                string;
  id:                        number;
  id_str:                    string;
  full_text:                 string;
  truncated:                 boolean;
  display_text_range:        number[];
  entities:                  Entities;
  source:                    string;
  in_reply_to_status_id:     number | null;
  in_reply_to_status_id_str: string | null;
  in_reply_to_user_id:       number | null;
  in_reply_to_user_id_str:   string | null;
  in_reply_to_screen_name:   string | null;
  user:                      User;
  geo:                       null;
  coordinates:               Coordinate;
  place:                     Place;
  contributors:              null;
  is_quote_status:           boolean;
  retweet_count:             number;
  favorite_count:            number;
  reply_count:               number;
  favorited:                 boolean;
  retweeted:                 boolean;
  lang:                      string;
  supplemental_language:     string | null;
}

export interface User {
  id:                                 number;
  id_str:                             string;
  name:                               string;
  screen_name:                        string;
  location:                           string;
  url:                                string | null;
  description:                        string;
  protected:                          boolean;
  followers_count:                    number;
  friends_count:                      number;
  listed_count:                       number;
  created_at:                         string;
  favourites_count:                   number;
  utc_offset:                         string | null;
  time_zone:                          string | null;
  geo_enabled:                        boolean;
  verified:                           boolean;
  statuses_count:                     number;
  media_count:                        number;
  lang:                               string | null;
  contributors_enabled:               boolean;
  is_translator:                      boolean;
  is_translation_enabled:             boolean;
  profile_background_color:           string;
  profile_background_image_url:       string | null;
  profile_background_image_url_https: string | null;
  profile_background_tile:            boolean;
  profile_image_url:                  string;
  profile_image_url_https:            string;
  profile_banner_url:                 string;
  profile_link_color:                 string;
  profile_sidebar_border_color:       string;
  profile_sidebar_fill_color:         string;
  profile_text_color:                 string;
  profile_use_background_image:       boolean;
  has_extended_profile:               boolean;
  default_profile:                    boolean;
  default_profile_image:              boolean;
  has_custom_timelines:               boolean;
  following:                          boolean;
  follow_request_sent:                boolean;
  notifications:                      boolean;
  business_profile_state:             string;
  translator_type:                    string;
  withheld_in_countries:              any[];
  require_some_consent:               boolean;
}

export interface QuotedStatusPermalink {
  url:      string;
  expanded: string;
  display:  string;
}

export interface SelfThread {
  id:     number;
  id_str: string;
}

export interface Coordinate {
  coordinates: number[] | number[][];
  type: string;
}

export interface Place {
  full_name: string;
  id: string;
  url: string;
  country: string;
  country_code: string;
  bounding_box: Coordinate;
  name: string;
  place_type: string;
  contained_within?: Place[];
  geometry?: any;
  polylines?: number[];
  centroid?: number[];
  attributes?: {
    geotagCount: string;
    [geoTagId: string]: string;
  };
}