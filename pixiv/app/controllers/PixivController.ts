import { TYPES } from '../Types';
import { handleValidationResults } from '../middlewares/validate';
import { PixivScraper } from '../services/scraper/PixivScraper';
import { Response } from 'express';
import { checkSchema } from 'express-validator';
import { inject, named } from 'inversify';
import {
  controller,
  httpGet,
  httpPost,
  requestBody,
  response,
} from 'inversify-express-utils';
import { safelyStringify } from '../utils/String';

// DEBUG ONLY
@controller('/pixiv')
export class PixivController {
  constructor(
    @inject(TYPES.PixivScraper) @named('pixiv') private pixivScraper: PixivScraper,
  ) {}

  @httpGet('/status')
  getStatus(@response() res: Response) {
    res.send(this.pixivScraper.status);
  }

  @httpGet('/login')
  async login(@response() res: Response) {
    await this.pixivScraper.login();
    res.send(this.pixivScraper.status);
  }

  @httpPost('/set-session',
  ...checkSchema({
    session: {
      in: 'body'
    }
  }), handleValidationResults)
  async setSession(
    @response() res: Response,
    @requestBody() // rome-ignore lint/suspicious/noExplicitAny: <explanation>
    body: any,
  ) {
    const actual = safelyStringify(body.session);
    try {
      await this.pixivScraper.setSession(actual);
      res.send('Session Ok');
    } catch (error) {
      res.send({ error });
    }
  }
}
