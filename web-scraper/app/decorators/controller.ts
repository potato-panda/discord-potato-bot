import 'reflect-metadata';
import { AppRouter } from '../AppRouter';
import { handleValidationResults, Middleware } from './';
import { MetadataKeys, MethodKeys } from './meta/MetadataKeys';

export function Controller(
  routePrefix: string,
  ...controllerMiddlewares: Middleware[]
) {
  return function (target: Function) {
    const router = AppRouter.instance;

    const { prototype } = target;
    const keys = Object.getOwnPropertyNames(prototype);

    for (const key of keys) {
      const method: MethodKeys = Reflect.getMetadata(
        MetadataKeys.METHOD,
        prototype,
        key,
      );
      const path = Reflect.getMetadata(MetadataKeys.PATH, prototype, key);
      const routeCallback: () => void = prototype[key];
      const middlewares =
        Reflect.getMetadata(MetadataKeys.MIDDLEWARE, prototype, key) || [];

      if (
        Object.values(MethodKeys).includes(method) &&
        typeof path === 'string'
      ) {
        const fullPath = `${routePrefix}${path}`;
        router[method](
          fullPath,
          ...controllerMiddlewares,
          ...middlewares,
          handleValidationResults,
          routeCallback,
        );
      }
    }
  };
}
