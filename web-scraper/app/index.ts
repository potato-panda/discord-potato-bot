import express, { json, urlencoded } from 'express';
import { AppRouter } from './AppRouter';
import './controllers';

(async () => {
  const app = express();

  app.use(urlencoded({ extended: true }));
  app.use(json());
  app.use(
    AppRouter.instance.all('*', (req, res) => {
      res.status(404).send('Not found');
    }),
  );

  app.listen(3000, () => {
    console.log('listening on port 3000');
  });
})();
