import { NextFunction, Request, Response } from 'express';

export function debug(req: Request, res: Response, next: NextFunction) {
  if (process.env.DEBUG)
    console.log(
      `[debug] ${req.method}`,
      req.originalUrl,
      '\nQuery :',
      req.query,
      '\nBody :',
      req.body,
    );

  next();
}
