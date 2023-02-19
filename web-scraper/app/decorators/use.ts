import { RequestHandler } from 'express';
import { ValidationChain } from 'express-validator';
import { Request } from 'express-validator/src/base';
import { ResultWithContext } from 'express-validator/src/chain';
import { MetadataKeys } from './meta/MetadataKeys';

export type Middleware =
  | RequestHandler
  | (ValidationChain[] & {
      run: (req: Request) => Promise<ResultWithContext[]>;
    });

export function Use(...middlewares: Middleware[]) {
  return function (target: {}, key: string, descriptor: PropertyDescriptor) {
    const setMiddlewares =
      Reflect.getMetadata(MetadataKeys.MIDDLEWARE, target, key) || [];

    Reflect.defineMetadata(
      MetadataKeys.MIDDLEWARE,
      [...setMiddlewares, ...middlewares],
      target,
      key,
    );
  };
}
