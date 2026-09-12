# 使用轻量级Alpine基础镜像，加速构建并减小镜像体积
FROM eclipse-temurin:17-jdk-alpine

# 维护者信息
LABEL maintainer="hxcoe@example.com"

# 安装curl命令，用于健康检查
RUN apk add --no-cache curl

# 设置工作目录
WORKDIR /app

# 复制jar包到工作目录
COPY target/*.jar app.jar

# 暴露端口
EXPOSE 8085

# 启动命令
ENTRYPOINT ["java", "-Dfile.encoding=UTF-8", "-Dsun.jnu.encoding=UTF-8", "-jar", "app.jar"]
