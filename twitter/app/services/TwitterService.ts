import Ffmpeg from 'fluent-ffmpeg';
import { inject, injectable } from 'inversify';
import { log } from 'node:console';
import { env } from 'node:process';
import { Readable, Writable } from 'node:stream';
import { inspect } from 'node:util';
import { TweetExtendedEntitiesV1, TwitterApi } from 'twitter-api-v2';
import TwitterApiv1ReadOnly from 'twitter-api-v2/dist/esm/v1/client.v1.read';
import { TwitterPost } from '../events/TwitterPostRequest';
import { TwitterPostRequestReplyModel } from '../models/TwitterPostRequestReply';
import createHttpRequest from '../utils/createHttpRequest';
import uploadStream from '../utils/uploadStream';
import { DownloadService, FileDownload } from './DownloadService';

@injectable()
export class TwitterService {
  private api: TwitterApiv1ReadOnly;
  constructor(
    @inject(DownloadService)
    private downloader: DownloadService,
  ) {
    this.api = new TwitterApi({
      appKey: env.TWITTER_APP_KEY ?? '',
      appSecret: env.TWITTER_APP_SECRET ?? '',
      accessSecret: env.TWITTER_ACCESS_SECRET ?? '',
      accessToken: env.TWITTER_ACCESS_TOKEN ?? '',
    }).readOnly.v1;
  }

  async getOneTweet(tweetUrl: string) {
    const { pathname } = new URL(tweetUrl);
    const tweetId = pathname.split('/').splice(-1)[0];
    console.log('tweetId', tweetId)
    const tweet = await this.api.singleTweet(tweetId);
    console.log('thistweet', tweet)
    return tweet;
  }

  async downloadMedia(
    postId: string,
    extendedEntities: TweetExtendedEntitiesV1 | undefined,
  ): Promise<TwitterPost.FileDownloadResponse[]> {
    if (!extendedEntities?.media) return Promise.reject();

    const requests = [];
    for (const [i, media] of extendedEntities.media.entries()) {
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

      const response = await this.downloader
        .download(req)
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
              postId: postId,
              key,
            }).exec() ??
            new TwitterPostRequestReplyModel({
              postId,
              key,
            });

          await entry.save();

          log('metadata:', inspect(metadata, false, null, true));

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

      // rome-ignore lint/suspicious/noExplicitAny: is a Buffer but to expect null
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
            log('finish gif');
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
