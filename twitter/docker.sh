#!/bin/sh

docker build -t twitter-service .

docker run -d --name twitter-service --env-file ../.env --env-file ./.env twitter-service