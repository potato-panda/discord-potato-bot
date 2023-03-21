export interface PixivPostPagesResponse {
    error:   boolean;
    message: string;
    body:    Body[];
}

export interface Body {
    urls:   Urls;
    width:  number;
    height: number;
}

export interface Urls {
    thumb_mini: string;
    small:      string;
    regular:    string;
    original:   string;
}
