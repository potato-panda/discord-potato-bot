#!/bin/sh

docker build -t pixiv-service .

docker run --name pixiv-service --env-file ../.env --env-file ./.env pixiv-service