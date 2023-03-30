#!/bin/sh

docker build -t bot-build .

docker run -d --name discord-bot --env-file ./.env --env-file ../.env bot-build