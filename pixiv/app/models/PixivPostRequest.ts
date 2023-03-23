import { Schema, model } from 'mongoose';

export interface PixivPostRequest {
  postId: string;
  key: string;
  data: Buffer;
  createdAt: Date;
}

const PixivPostRequestSchema = new Schema<PixivPostRequest>({
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

export const PixivPostRequestModel = model<PixivPostRequest>(
  'PixivPostRequest',
  PixivPostRequestSchema,
  'PixivPostRequests',
);
