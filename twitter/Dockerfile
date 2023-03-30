FROM node:18-alpine

WORKDIR /app

ENV NATS_NAME=$NATS_NAME
ENV MONGO_NAME=$MONGO_NAME
ENV REDIS_NAME=$REDIS_NAME

ENV TWITTER_API_KEY=$TWITTER_API_KEY
ENV TWITTER_API_SECRET=$TWITTER_API_SECRET

COPY package*.json tsconfig.json ./

RUN npm install -g typescript
RUN npm install --omit=dev

COPY ./app ./app

RUN npm run build

CMD npm run start