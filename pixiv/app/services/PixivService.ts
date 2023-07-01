import { Illust, PixivApi, Utils } from 'pixiv-api-wrapper';
import { PixivPost } from '../events/PixivPostRequest';
import { PixivPostRequestReplyModel } from '../models/PixivPostRequestReply';
import { uploadStream } from '../utils/uploadStream';

export class PixivService {
  expires = this.nextExpiry();
  public constructor(protected client: PixivApi) {}

  private nextExpiry() {
    return Date.now() + 1_000 * 60 * 50;
  }

  async getIllust(illustId: string) {
    const { Auth, Illust } = this.client;

    if (Date.now() >= this.expires) {
      const { accessToken } = Auth.getAuthentication();
      await Auth.refreshAuth()
        .then(async () => {
          const { accessToken: newAccessToken } = Auth.getAuthentication();
          if (accessToken === newAccessToken) {
            console.log('Access Token unchanged');
            throw new Error('Access Token unchanged');
          }
          console.log('Refreshed access token');
          this.expires = this.nextExpiry();
        })
        .catch(() => {
          console.log('Failed to refresh token');
          throw new Error('Failed to refresh token');
        });
    }

    const illustMetadata = await Illust.detail(illustId);

    const {
      illust: {
        xRestrict,
        tags,
        title,
        caption: description,
        user: { name: userName, account: userAccount },
        illustAiType: aiType,
        totalBookmarks: favourites,
        totalView: views,
        createDate: createdAt,
        type: illustType,
      },
    } = illustMetadata;

    return {
      metadata: {
        explicit: Boolean(xRestrict),
        tags: tags.map((tag) => tag.name),
        url: `https://www.pixiv.net/artworks/${illustId}`,
        title,
        description,
        userName,
        userAccount,
        isAi: aiType === 2,
        favourites,
        createdAt,
        illustType,
        views,
      } as PixivPost.Metadata,
      illustMetadata,
    };
  }

  async downloadIllusts(
    illustMetadata: Illust,
    quality?: string,
  ): Promise<PixivPost.FileDownloadResponse[]> {
    const q = quality === 'REGULAR' ? 'large' : 'original';
    const illusts = await Utils.downloadIllusts(illustMetadata, q);

    const result = [];

    for (const [i, illust] of illusts.entries()) {
      if (illust.status === 'fulfilled') {
        const { data, metadata } = illust.value;

        const upload = await uploadStream(metadata, data);

        const key = upload.id.toJSON();

        result.push({
          metadata,
          key,
          message: '',
          success: true,
        });

        // Find stored reply else create new reply
        const entry =
          (await PixivPostRequestReplyModel.findOne({
            postId: `${illustMetadata.id}_p${i + 1}`,
            key,
          })?.exec()) ??
          new PixivPostRequestReplyModel({
            postId: `${illustMetadata.id}_p${i + 1}`,
            key,
          });

        await entry.save();
      }
    }

    return result;
  }

  async downloadUgoira(
    ugoiraId: string,
  ): Promise<PixivPost.FileDownloadResponse[]> {
    const ugoiraMetadata = await this.client.Ugoira.metadata(ugoiraId);
    const { metadata, data } = await Utils.downloadUgoira(ugoiraMetadata);

    const key = metadata.fileName;

    const entry =
      (
        await PixivPostRequestReplyModel.findOne({
          postId: ugoiraId,
          key,
        }).exec()
      )?.$set('data', data) ??
      new PixivPostRequestReplyModel({
        postId: ugoiraId,
        key,
      });

    await entry.save();

    const results: PixivPost.FileDownloadResponse[] = [
      {
        metadata: metadata,
        key,
        success: true,
        message: '',
      },
    ];

    return results;
  }
}
