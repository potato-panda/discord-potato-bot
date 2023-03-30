FROM mcr.microsoft.com/playwright:v1.32.0-focal

WORKDIR /app

ENV NATS_NAME=$NATS_NAME
ENV MONGO_NAME=$MONGO_NAME
ENV REDIS_NAME=$REDIS_NAME

ENV PIXIV_ID=$PIXIV_ID
ENV PIXIV_PW=$PIXIV_PW

COPY package*.json tsconfig.json ./

RUN npm install -g typescript
RUN npm install --omit=dev

COPY ./app ./app

RUN npm run build

CMD npm run start