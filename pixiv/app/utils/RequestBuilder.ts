import { ClientRequest } from 'http';
import https, { RequestOptions } from 'https';

export function createRequest(uri: string, headers?: RequestOptions): ClientRequest {
  const url = new URL(uri);
  console.log(`created url: ${url}`)
  return https.request(
    headers ?? {
      host: url.host,
      path: url.pathname,
      method: 'GET',
      headers: {
        Accept: '*/*',
      },
    },
  );
}
