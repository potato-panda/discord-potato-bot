FROM node:18 as base

WORKDIR /app

# Builds app
FROM base as builder

COPY package*.json tsconfig.json ./

RUN npm install -g typescript
RUN npm install

COPY ./app ./app

RUN npm run build

# Copy build
FROM base as build-puppeteer

# Install latest chrome dev package and fonts to support major charsets (Chinese, Japanese, Arabic, Hebrew, Thai and a few others)
# Note: this installs the necessary libs to make the bundled version of Chrome that Puppeteer
# installs, work.
# RUN apt-get update \
#     && apt-get install -y wget gnupg \
#     && wget -q -O - https://dl-ssl.google.com/linux/linux_signing_key.pub | gpg --dearmor -o /usr/share/keyrings/googlechrome-linux-keyring.gpg \
#     && sh -c 'echo "deb [arch=amd64 signed-by=/usr/share/keyrings/googlechrome-linux-keyring.gpg] http://dl.google.com/linux/chrome/deb/ stable main" >> /etc/apt/sources.list.d/google.list' \
#     && apt-get update \
#     && apt-get install -y google-chrome-stable fonts-ipafont-gothic fonts-wqy-zenhei fonts-thai-tlwg fonts-khmeros fonts-kacst fonts-freefont-ttf libxss1 \
#       --no-install-recommends \
#     && rm -rf /var/lib/apt/lists/*

# Serve
FROM build-puppeteer as build

COPY package*.json tsconfig.json ./

# RUN apk add --no-cache chromium

RUN npm install --omit=dev

COPY --from=builder /app/build ./build

# Serve
FROM build

ENV NATS_NAME=$NATS_NAME
ENV MONGO_NAME=$MONGO_NAME

ENV PIXIV_ID=$PIXIV_ID
ENV PIXIV_PW=$PIXIV_PW

CMD npm run start