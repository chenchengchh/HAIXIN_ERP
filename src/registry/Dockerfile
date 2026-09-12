# 使用官方OpenJDK 17作为基础镜像
FROM mcr.microsoft.com/openjdk/jdk:17-ubuntu

LABEL maintainer="hxcoe@example.com"

ENV LANG=C.UTF-8
ENV LC_ALL=C.UTF-8

WORKDIR /app

RUN apt-get update -o Acquire::Retries=3 && apt-get install -y --no-install-recommends --fix-missing curl && rm -rf /var/lib/apt/lists/*

COPY target/*.jar app.jar

EXPOSE 8761

ENTRYPOINT ["java", "-Dfile.encoding=UTF-8", "-Dsun.jnu.encoding=UTF-8", "-jar", "app.jar"]