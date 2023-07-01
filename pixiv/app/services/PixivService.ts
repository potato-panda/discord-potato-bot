import { Illust, PixivApi, Utils } from 'pixiv-api-wrapper';
import { PixivPost } from '../events/PixivPostRequest';
import { PixivPostRequestReplyModel } from '../models/PixivPostRequestReply';
import { uploadStream } from '../utils/uploadStream';

export class PixivService {
  public constructor(
    protected client: PixivApi,
  ) { }

  async getIllust(illustId: string) {
    let illustMetadata;

    try {
      illustMetadata = await this.client.Illust.detail(illustId);
    } catch (e) {
      illustMetadata = await this.client.Auth.refreshAuth()
        .then(async () => {
          console.log('Refreshed Token')
          return await this.client.Illust.detail(illustId)
        }).catch(() => {
          console.log('Failed to refresh token');
          throw new Error('Failed to refresh token');
        });
    }


    const {
      illust: {
        xRestrict,
        tags,
        title,
        caption: description,
        user: { name: userName, account: userAccount },
        illustAiType: aiType,
        totalBookmarks: favourites,
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
      } as PixivPost.Metadata,
      illustMetadata,
    };
  }

  async downloadIllusts(
    illustMetadata: Illust.Illust,
    quality?: string,
  ): Promise<PixivPost.FileDownloadResponse[]> {
    const q = quality === 'REGULAR' ? 'large' : 'original';
    const illusts = await Utils.downloadIllusts(illustMetadata, q);

    const result: PixivPost.FileDownloadResponse[] = [];

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
        data,
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
