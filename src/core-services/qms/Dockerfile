# 使用官方OpenJDK 17作为基础镜像
FROM eclipse-temurin:17-jdk-alpine

LABEL maintainer="hxcoe@example.com"

ENV LANG=C.UTF-8
ENV LC_ALL=C.UTF-8

RUN apk add --no-cache curl

WORKDIR /app

COPY target/*.jar app.jar

EXPOSE 8089

ENTRYPOINT ["java", "-Dfile.encoding=UTF-8", "-Dsun.jnu.encoding=UTF-8", "-jar", "app.jar"]
