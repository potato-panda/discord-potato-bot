import { Response } from 'express';
import { controller, httpMethod, response } from 'inversify-express-utils';

@controller('*')
export class NotFoundController {
  @httpMethod('all','/')
  async index(@response() res: Response) {
    res.status(404).send({ message: 'Not found' });
  }
}
