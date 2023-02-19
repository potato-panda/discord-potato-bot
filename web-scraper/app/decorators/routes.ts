import { MetadataKeys, MethodKeys as Methods } from './meta/MetadataKeys';
import { RequestHandler } from 'express';

interface RouterHandlerDescriptor extends PropertyDescriptor {
  value?: RequestHandler;
}

function routeDecorator(method: string) {
  return function (path: string = '') {
    return function (
      target: {},
      key: string,
      descriptor: RouterHandlerDescriptor,
    ) {
      Reflect.defineMetadata(MetadataKeys.PATH, path, target, key);
      Reflect.defineMetadata(MetadataKeys.METHOD, method, target, key);
    };
  };
}

export const GET = routeDecorator(Methods.GET);
export const PUT = routeDecorator(Methods.PUT);
export const POST = routeDecorator(Methods.POST);
export const DELETE = routeDecorator(Methods.DELETE);
export const PATCH = routeDecorator(Methods.PATCH);
export const OPTIONS = routeDecorator(Methods.OPTIONS);
export const HEAD = routeDecorator(Methods.HEAD);
