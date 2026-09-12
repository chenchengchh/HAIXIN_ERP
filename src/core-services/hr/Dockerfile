# 使用官方OpenJDK 17作为基础镜像
FROM mcr.microsoft.com/openjdk/jdk:17-ubuntu

# 维护者信�?
LABEL maintainer="hxcoe@example.com"

# 设置环境变量确保UTF-8编码
ENV LANG=C.UTF-8
ENV LC_ALL=C.UTF-8

# 安装curl命令，用于健康检�?
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# 设置工作目录
WORKDIR /app

# 复制jar包到工作目录
COPY target/*.jar app.jar

# 暴露端口
EXPOSE 8083

# 启动命令
ENTRYPOINT ["java", "-Dfile.encoding=UTF-8", "-Dsun.jnu.encoding=UTF-8", "-jar", "app.jar"]