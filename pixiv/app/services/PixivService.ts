import { inject, injectable } from 'inversify';
import { Illust, PixivApiClient, Utils } from 'pixiv-api-wrapper';
import { PixivPost } from '../events/PixivPostRequest';
import { PixivPostRequestReplyModel } from '../models/PixivPostRequestReply';
import os from 'node:os';

@injectable()
export class PixivService {
  downloadKey = 'pixiv:illust';
  public constructor(
    @inject(PixivApiClient) protected client: PixivApiClient,
  ) { }

  async getIllust(illustId: string) {
    const illustMetadata = await this.client.Illust.detail(illustId);

    const {
      illust: {
        xRestrict,
        tags,
        title,
        caption: description,
        user: { name: userName, account: userAccount },
        illustAiType: aiType,
        totalBookmarks: bookmarkCount,
        createDate,
        type: illustType,
      },
    } = illustMetadata;

    return {
      metadata: {
        adult: Boolean(xRestrict),
        tags: tags.map((tag) => tag.name),
        url: `https://www.pixiv.net/artworks/${illustId}`,
        title,
        description,
        userName,
        userAccount,
        isAi: aiType === 2,
        favourites: bookmarkCount,
        createdAt: createDate,
        illustType,
      } as PixivPost.Metadata,
      illustMetadata
    }
  }

  async downloadIllusts(
    illustMetadata: Illust.Illust,
    quality?: string,
  ): Promise<PixivPost.FileDownloadResponse[]> {
    const illusts = await Utils.downloadIllusts(illustMetadata);

    const result: PixivPost.FileDownloadResponse[] = [];

    for (const illust of illusts) {
      if (illust.status === 'fulfilled') {
        const { metadata, data } = illust.value;

        const key = metadata.fileName;
        result.push({
          metadata,
          key,
          message: '',
          success: true,
        });

        // Find stored reply else create new reply
        const entry =
          (
            await PixivPostRequestReplyModel.findOne({
              postId: illustMetadata.id,
              key,
            })?.exec()
          )?.$set('data', data) ??
          new PixivPostRequestReplyModel({
            postId: illustMetadata.id,
            key,
            data,
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
    const { metadata, data } = await Utils.downloadUgoira(ugoiraMetadata, { threads: os.cpus().length });

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
