#!/bin/sh

docker build -t twitter-build .

docker run -d --name twitter-node --env-file ./.env --env-file ../.env twitter-build