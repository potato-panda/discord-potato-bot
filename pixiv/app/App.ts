import { json, urlencoded } from 'body-parser';
import express from 'express';

export class App {
  constructor(public app = express.application) {
    app.use(urlencoded({ extended: true }));
    app.use(json());
  }
}
