import Ffmpeg = require('fluent-ffmpeg');
import { request as httpsRequest } from 'node:https';
import { Readable, Writable } from 'node:stream';
import { generateCsrfToken, generateGuestToken } from 'twitter-web-auth';
import { bearer, userAgent } from '../constants';
import { TwitterPost } from '../events/TwitterPostRequest';
import { TwitterPostRequestReplyModel } from '../models/TwitterPostRequestReply';
import { Tweet } from '../types/Tweet';
import { FileDownload, createHttpRequest, download, uploadStream, } from '../utils';

// Reminder: Tokens generated with bearer token must be sent alongside that bearer token. Using other bearer tokens will not work.

export class TwitterService {
  readonly authTokens: Set<string> = new Set<string>;
  private csrfToken: string | null = null;
  private guestToken: string | null = null;
  private baseHeaders = {
    "user-agent": userAgent,
    "x-twitter-client-language": "en",
    "origin": "https://twitter.com",
    "Authorization": bearer,
  }

  constructor(twitterTokens?: string) {
    if (twitterTokens) {
      this.authTokens
        = twitterTokens ?
          twitterTokens.split(",").length > 0
            ? new Set(twitterTokens.split(",").filter(s => s))
            :
            new Set([twitterTokens])
          : new Set([]);
    }
  }

  private regenerateCsrfToken() {
    this.csrfToken = generateCsrfToken();
  }

  private async regenerateGuestToken() {
    this.guestToken
      = (await generateGuestToken() as { guestToken: string })['guestToken'];
  }

  async getTweet(tweetUrl: string, fallback = true): Promise<Tweet> {

    const { pathname } = new URL(tweetUrl);
    const tweetId = pathname.split('/').splice(-1)[0];

    if (!this.csrfToken)
      this.regenerateCsrfToken();
    if (!this.guestToken)
      await this.regenerateGuestToken();

    return new Promise<Tweet>((resolve, reject) => {

      const { authTokens, csrfToken, guestToken, baseHeaders } = this;

      try {
        resolve(request());
      }
      catch (e) {
        if (authTokens.size > 0 && fallback) {
          for (const [i, authToken] of authTokens.entries()) {
            try {
              resolve(request(authToken));
            } catch (err) {
              console.log(`Auth Fallback | attempt (${i + 1})`)
            }
          }
          reject('Auth fallbacks rejected')
        }
        reject('No auth tokens available')
      }

      throw new Error('Should not reach here');

      async function request(authToken?: string) {

        const url = `https://api.twitter.com/1.1/statuses/show/${tweetId}.json?tweet_mode=extended&cards_platform=Web-12&include_cards=1&include_reply_count=1&include_user_entities=0`;

        const headers = !authToken ? {
          ...baseHeaders,
          "x-guest-token": guestToken ?? '',
        } : {
          ...baseHeaders,
          "x-twitter-active-user": "yes",
          "x-twitter-auth-type": "OAuth2Session",
          "x-csrf-token": csrfToken ?? '',
          "cookie": `auth_token=${authToken}; ct0=${csrfToken ?? ''}`,
        }

        return new Promise<Tweet>((resolve, reject) => {
          httpsRequest(url, {
            method: 'GET',
            headers,
          }).on('response', response => {

            // console.log('sent headers', req.getHeaders())

            const chunks: Buffer[] = [];
            const { statusCode } = response;

            response
              .on('readable', () => {
                response.read();
              })
              .on('data', data => {
                chunks.push(data);
              })
              .on('end', () => {
                const result: Tweet = JSON.parse(Buffer.concat(chunks)
                  .toString('utf-8'));
                if (statusCode !== 200) {
                  reject(result);
                }
                resolve(result);
              })
              .on('close', () => {
              })
              .on('error', err => {
                reject(err)
              });
          }).end();
        });
      }
    });
  }

  async downloadMedia(
    twid: string,
    extendedEntities: Tweet['extended_entities'],
  ): Promise<TwitterPost.FileDownloadResponse[]> {
    if (!extendedEntities?.media) return Promise.reject();

    const requests = [];
    for (const media of extendedEntities.media) {
      const { type, video_info, media_url_https } = media;
      let stringLink;
      switch (type) {
        case 'animated_gif':
        case 'video': {
          const vidUrl = video_info?.variants[0].url as string;
          stringLink = new URL(vidUrl);
        }
          break;
        case 'photo': {
          const imgLink = media_url_https;
          stringLink = new URL(imgLink);
        }
          break;
      }

      const req = createHttpRequest(stringLink);

      const response = await download(req)
        .then(async (response) => {
          let { data, metadata, success } = response;

          // Convert to gif because media format is mp4 and not a real gif
          if (type === 'animated_gif') {
            data = await convertToGifFormat(data, metadata);
          }

          const upload = await uploadStream(metadata, data);

          const key = upload.id.toJSON();

          // Find stored reply else create new reply
          const entry =
            await TwitterPostRequestReplyModel.findOne({
              postId: twid,
              key,
            }).exec() ??
            new TwitterPostRequestReplyModel({
              postId: twid,
              key,
            });

          await entry.save();

          return Promise.resolve<TwitterPost.FileDownloadResponse>({
            metadata,
            key,
            success,
            message: '',
          });
        });

      requests.push(response);
    }

    return (await Promise.allSettled(requests)).reduce((results, result) => {
      if (result.status === 'fulfilled') {
        results.push(result.value);
      }
      return results;
    }, [] as TwitterPost.FileDownloadResponse[]);

    async function convertToGifFormat(
      data: Buffer,
      metadata: FileDownload.Metadata,
    ) {
      const mp4ReadStream = new Readable({
        read(_size) {
          this.push(data);
          this.push(null);
        },
      });

      const gifBinary: any[] = [];

      await new Promise<void>((resolve, reject) => {
        const gifWriteStream = new Writable({
          write(chunk, _encoding, callback) {
            if (chunk) {
              gifBinary.push(chunk);
            } else {
              gifBinary.push(null);
            }
            callback();
          },
        })
          .on('finish', () => {
            const data = Buffer.concat(gifBinary);
            metadata.mimeType = 'image/gif';
            metadata.fileExtension = 'gif';
            metadata.size = data.length;
            resolve();
          })
          .on('error', (err) => reject(err.message));

        Ffmpeg(mp4ReadStream)
          .inputFormat('mp4')
          .videoCodec('gif')
          .outputFormat('gif')
          .outputOptions([
            '-vf scale=-1:-1:flags=lanczos,split[s0][s1];[s0]palettegen[p];[s1][p]paletteuse',
            /**
             * https://superuser.com/questions/556029/how-do-i-convert-a-video-to-gif-using-ffmpeg-with-reasonable-quality
             */
          ])
          .pipe(gifWriteStream, { end: true });
      });
      return data;
    }
  }

}
