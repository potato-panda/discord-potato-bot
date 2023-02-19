import { Router } from 'express';

export class AppRouter {
  private static readonly _instance: Router = Router();

  private constructor() {}

  static get instance(): Router {
    return AppRouter._instance;
  }
}
