#!/bin/sh

docker build -t bot-app .

docker run -d --name discord-bot --env-file ./.env --env-file ../.env bot-app