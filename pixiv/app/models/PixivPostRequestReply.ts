import { Schema, model } from 'mongoose';

export interface PixivPostRequestReply {
  postId: string;
  key: string;
  data: Buffer;
  createdAt: Date;
}

const PixivPostRequestSchema = new Schema<PixivPostRequestReply>({
  postId: {
    type: String,
    required: true,
  },
  key: {
    type: String,
    required: true,
  },
  data: Buffer,
  createdAt: {
    type: Date,
    required: true,
    default: Date.now,
    expires: 60 * 60,
  },
}).index({ postId: 1, key: 1 }, { unique: true });

export const PixivPostRequestReplyModel = model<PixivPostRequestReply>(
  'PixivPostRequest',
  PixivPostRequestSchema,
  'PixivPostRequests',
);
