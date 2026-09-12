# 使用官方OpenJDK 17作为基础镜像
FROM mcr.microsoft.com/openjdk/jdk:17-ubuntu

# 维护者信�?
LABEL maintainer="hxcoe@example.com"

# 安装必要的系统依赖和浏览�?
RUN apt-get update --fix-missing && apt-get install -y --no-install-recommends --fix-missing \
    wget \
    curl \
    gnupg \
    unzip \
    fonts-liberation \
    libasound2 \
    libatk-bridge2.0-0 \
    libatk1.0-0 \
    libatspi2.0-0 \
    libcairo2 \
    libcups2 \
    libdbus-1-3 \
    libdrm2 \
    libexpat1 \
    libfontconfig1 \
    libgbm1 \
    libglib2.0-0 \
    libgtk-3-0 \
    libnspr4 \
    libnss3 \
    libpango-1.0-0 \
    libx11-6 \
    libxcb1 \
    libxcomposite1 \
    libxdamage1 \
    libxext6 \
    libxfixes3 \
    libxrandr2 \
    && rm -rf /var/lib/apt/lists/*

# 安装Node.js和npm
RUN curl -fsSL https://deb.nodesource.com/setup_18.x | bash - \
    && apt-get install -y nodejs \
    && rm -rf /var/lib/apt/lists/*

# 设置工作目录
WORKDIR /app

# 创建必要的目�?
RUN mkdir -p /app/.playwright/browsers /app/logs

# 复制jar包到工作目录
COPY target/*.jar app.jar

# 设置Playwright浏览器路径环境变�?
ENV PLAYWRIGHT_BROWSERS_PATH=/app/.playwright/browsers

# 只有在浏览器目录为空时才安装Playwright浏览器二进制文件
RUN if [ -z "$(ls -A /app/.playwright/browsers)" ]; then \
        echo "安装Playwright浏览器二进制文件..."; \
        npx playwright@1.48.0 install chromium --with-deps; \
    else \
        echo "使用现有的Playwright浏览器二进制文件..."; \
    fi

# 暴露端口
EXPOSE 8018

# 启动命令
ENTRYPOINT ["java", "-Dfile.encoding=UTF-8", "-Dsun.jnu.encoding=UTF-8", "-jar", "app.jar"]