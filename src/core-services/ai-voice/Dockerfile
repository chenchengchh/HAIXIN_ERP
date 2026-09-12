FROM python:3.9-slim

LABEL maintainer="hxcoe@example.com"

# 设置环境变量确保UTF-8编码
ENV LANG=C.UTF-8
ENV LC_ALL=C.UTF-8
ENV PYTHONUNBUFFERED=1
ENV PIP_NO_CACHE_DIR=1
ENV PIP_DEFAULT_TIMEOUT=120

# 设置工作目录
WORKDIR /app

# 安装系统依赖
RUN apt-get update -o Acquire::Retries=3 && apt-get install -y --no-install-recommends --fix-missing \
    curl \
    && rm -rf /var/lib/apt/lists/*

# 安装轻量级Python依赖（不依赖FunASR/PyTorch，快速构建）
RUN pip install --upgrade pip && \
    pip install fastapi==0.110.0 uvicorn[standard]==0.28.0

# 复制后端代码
COPY backend/ .

# 暴露端口（与FastAPI配置一致）
EXPOSE 8000

# 启动命令（使用Mock服务，生产环境可替换为funasr_local_server.py）
CMD ["python", "mock_server.py"]
