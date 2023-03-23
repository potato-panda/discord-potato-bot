import { Schema, model } from 'mongoose';

export interface TwitterPostRequest {
  postId: string;
  key: string;
  data: Buffer;
  createdAt: Date;
}

const TwitterPostRequestSchema = new Schema<TwitterPostRequest>({
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

export const TwitterPostRequestModel = model<TwitterPostRequest>(
  'TwitterPostRequest',
  TwitterPostRequestSchema,
  'TwitterPostRequests',
);
