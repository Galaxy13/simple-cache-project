FROM gradle:8.8.0 AS build
LABEL authors="Retroider"
WORKDIR /app

COPY build.gradle.kts .
COPY settings.gradle.kts .
COPY gradle.properties .
COPY server ./server

RUN gradle build

FROM amazoncorretto:21-alpine-jdk
WORKDIR /app

COPY --from=build /app/server/build/libs/server-0.1.3-test.jar /app/server.jar

EXPOSE 8081

ENV PORT="8081"
ENV LOGIN="test"
ENV CAPACITY="1024"
ENV PASSWORD="test"

ENTRYPOINT ["java", "-jar", "/app/server.jar"]