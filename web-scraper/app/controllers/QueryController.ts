import { Controller, GET, Use } from '../decorators';
import { debug } from '../middlewares/debug';
import { Request, Response } from 'express';
import { checkSchema } from 'express-validator';

@Controller("/api/query", debug)
export class QueryController {
  @GET()
  @Use(checkSchema({
        uri: {
			in: ['query','body'],
			notEmpty:true,
			isString: true,
		}
    }))
  query(req: Request, res: Response) {
    const { body, query } = req;
    res.send('OK');
  }
}
