import { error, log } from 'console';
import Ffmpeg from 'fluent-ffmpeg';
import { ClientRequest } from 'http';
import { inject, injectable } from 'inversify';
import { Msg, StringCodec } from 'nats';
import { Readable, Writable } from 'stream';
import { TweetExtendedEntitiesV1 } from 'twitter-api-v2';
import { inspect } from 'util';
import { TwitterPostRequestModel } from '../models/TwitterPostRequest';
import {
  DownloadService,
  FileDownloadResponse,
} from '../services/DownloadService';
import { TwitterService } from '../services/TwitterService';
import { TYPES } from '../Types';
import { safelyStringify } from '../utils/String';
import { createHttpRequest } from '../utils/Web';
import { Listener } from './Listener';
import {
  ReplyFileDownloadResponse,
  TwitterPostRequestReply,
} from './reply/TwitterPostRequestReply';
import { TwitterPostRequestEvent } from './TwitterPostRequestEvent';

@injectable()
export class TwitterPostRequestListener extends Listener<TwitterPostRequestEvent> {
  subject = 'twitter.post.request';
  queueName = 'twitter-post-service-queue';

  constructor(
    @inject(TYPES.TwitterService)
    private service: TwitterService,
    @inject(TYPES.DownloadService)
    private downloader: DownloadService,
  ) {
    super();
  }

  async onMessage(msg: Msg, data: { url: string }): Promise<void> {
    const sc = StringCodec();
    try {
      const {
        id_str: postId,
        full_text,
        extended_entities,
        user: {
          id_str: userId,
          name,
          screen_name,
          url,
          profile_image_url_https,
        },
        retweet_count,
        favorite_count,
        possibly_sensitive,
        created_at,
        quote_count,
        reply_count,
      } = await this.service.getOneTweet(data.url);

      const reply: TwitterPostRequestReply = {
        metadata: {
          content: full_text ?? null,
          userName: name,
          userScreenName: screen_name,
          userUrl: url,
          userThumbnailUrl: profile_image_url_https,
          retweets: retweet_count,
          favourites: favorite_count,
          suggestive: possibly_sensitive ?? false,
          createdAt: created_at,
        },
        downloadResponses: await this.getMedia(postId, extended_entities),
      };

      log('Response: ', inspect(reply, false, null, true));

      msg.respond(sc.encode(safelyStringify(reply)));
    } catch (err) {
      error(err);

      msg.respond(
        sc.encode(
          safelyStringify({ success: false, message: safelyStringify(err) }),
        ),
      );
    }
  }

  async getMedia(
    postId: string,
    extendedEntities: TweetExtendedEntitiesV1 | undefined,
  ): Promise<ReplyFileDownloadResponse[]> {
    if (!extendedEntities?.media) return Promise.reject();

    const requests = [];
    for (const [i, media] of extendedEntities.media.entries()) {
      const { type } = media;
      let req: ClientRequest;
      switch (type) {
        case 'animated_gif':
        case 'video': {
          const vidUrl = media.video_info?.variants[0].url as string;

          const url = new URL(vidUrl);
          req = createHttpRequest(url);
        }
        break;
        case 'photo': {
          const imgLink = media.media_url_https;

          const url = new URL(imgLink);
          req = createHttpRequest(url);
        }
        break;
      }

      const response = await this.downloader
        .download(req)
        .then(async (response) => {
          let { data, metadata, success } = response;

          const key = `${i}:${metadata.fileName}`;

          if (type === 'animated_gif') {
            const mp4ReadStream = new Readable({
              read(size) {
                this.push(data);
                this.push(null);
              },
            });

            // rome-ignore lint/suspicious/noExplicitAny: is a Buffer but to expect null
            const gifBinary: any[] = [];

            const toGifPromise = new Promise<void>((resolve, reject) => {
              const gifWriteStream = new Writable({
                write(chunk, encoding, callback) {
                  if (chunk) {
                    gifBinary.push(chunk);
                  } else {
                    gifBinary.push(null);
                  }
                  callback();
                },
              })
                .on('finish', () => {
                  data = Buffer.concat(gifBinary);
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

            await toGifPromise;
          }

          const entry =
            (
              await TwitterPostRequestModel.findOne({
                postId: postId,
                key: key,
              }).exec()
            )?.set('data', data) ??
            new TwitterPostRequestModel({
              postId,
              key,
              data,
            });

          await entry.save();

          log('metadata:', inspect(metadata, false, null, true));

          return Promise.resolve<ReplyFileDownloadResponse>({
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
    }, [] as ReplyFileDownloadResponse[]);
  }
}
