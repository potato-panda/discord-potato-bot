FROM gradle:8.0.2-jdk17-alpine as BASE

WORKDIR /app

COPY gradle gradle
COPY src src
COPY build.gradle settings.gradle gradlew gradlew.bat settings.gradle gradle.properties src ./

RUN gradle shadowJar

FROM gradle:8.0.2-jdk17-alpine

WORKDIR /app

ENV NATS_NAME=$NATS_NAME
ENV MONGO_NAME=$MONGO_NAME
ENV REDIS_NAME=$REDIS_NAME

ENV BOT_TOKEN=$BOT_TOKEN
ENV ENV=$ENV
ENV GUILD_ID=$GUILD_ID
ENV CONTENT_MOD_KEY=$CONTENT_MOD_KEY

ENV ARTIFACT_NAME=discord-bot-1.0-SNAPSHOT-all.jar

COPY --from=BASE /app/build/libs/${ARTIFACT_NAME} /app/

CMD java \
    -DBOT_TOKEN=$BOT_TOKEN \
    -DCONTENT_MOD_KEY=$CONTENT_MOD_KEY \
    -DENV=$ENV \
    -DGUILD_ID=$GUILD_ID \
    -jar $ARTIFACT_NAME