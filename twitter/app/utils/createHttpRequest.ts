import { request, RequestOptions } from 'node:https';

export function createHttpRequest(url: URL, options?: RequestOptions) {
  return request(url, {
    headers: {
      Accept: '*/*',
      Host: url.host,
      'User-Agent':
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/111.0.0.0 Safari/537.36 Edg/111.0.1661.41',
      ...options?.headers,
    },
    ...options,
  });
}
