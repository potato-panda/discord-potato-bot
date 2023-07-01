import { Schema, model } from 'mongoose';

export interface TwitterPostRequestReply {
  postId: string;
  key: string;
  createdAt: Date;
}

const TwitterPostRequestSchema = new Schema<TwitterPostRequestReply>({
  postId: {
    type: String,
    required: true,
  },
  key: {
    type: String,
    required: true,
  },
  createdAt: {
    type: Date,
    required: true,
    default: Date.now,
  },
})
  .index({ postId: 1, key: 1 }, { unique: true })
  .index({ createdAt: 1 }, { expireAfterSeconds: 60 * 60 });

export const TwitterPostRequestReplyModel = model<TwitterPostRequestReply>(
  'TwitterPostRequest',
  TwitterPostRequestSchema,
  'TwitterPostRequests',
);