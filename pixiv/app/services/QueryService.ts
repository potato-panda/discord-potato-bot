import { TYPES } from '../Types';
import { Body, PixivPostPagesResponse } from './scraper/responses/PixivPostPagesResponse';
import { PixivScraper } from './scraper/PixivScraper';
import { inject, injectable, named } from 'inversify';

@injectable()
export class QueryService {
  constructor(
    @inject(TYPES.PixivScraper) @named('pixiv') private pixivScraper: PixivScraper,
  ) {}

  async query(uri: string) {
    // const isMatch = uri.match(/https:\/\/www\.pixiv\.net\/en\/artworks\/\d+/g);
    // if (isMatch) {
    //   const pixImgId = uri.split('/').at(-1);
    //   const pagesUri = `https://www.pixiv.net/ajax/illust/${pixImgId}/pages`;
    //   const links = await this.pixivScraper.scrape<PixivPagesResponse, Body[]>(
    //     pagesUri,
    //     (res) => res.body,
    //   );
    //   return links;
    // }
  }

  async queryPixiv(uri: string) {
    const isMatch = uri.match(/https:\/\/www\.pixiv\.net\/en\/artworks\/\d+/g);
    if (isMatch) {
      const pixImgId = uri.split('/').at(-1);
      const pagesUri = `https://www.pixiv.net/ajax/illust/${pixImgId}/pages`;
      const links = await this.pixivScraper.scrape<PixivPostPagesResponse, Body[]>(
        pagesUri,
        (res) => res.body,
      );
      return links;
    }
  }

  get statuses() {
    return {
      pixiv: this.pixivScraper?.status ?? 'n/a',
    };
  }
}
