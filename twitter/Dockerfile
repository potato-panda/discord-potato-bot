FROM node:18-alpine as base

WORKDIR /app

# Builds app
FROM base as builder

COPY package*.json tsconfig.json ./

RUN npm install -g typescript
RUN npm install

COPY ./app ./app

RUN npm run build

# Copy build
FROM base as build

COPY package*.json tsconfig.json ./

RUN npm install --omit=dev

COPY --from=builder /app/build ./build

# Serve
FROM build

ENV NATS_NAME=$NATS_NAME
ENV MONGO_NAME=$MONGO_NAME

ENV TWITTER_API_KEY=$TWITTER_API_KEY
ENV TWITTER_API_SECRET=$TWITTER_API_SECRET

CMD npm run start