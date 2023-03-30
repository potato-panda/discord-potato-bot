#!/bin/sh

docker build -t pixiv-build .

docker run -d --name pixiv-node --env-file ./.env --env-file ../.env pixiv-build