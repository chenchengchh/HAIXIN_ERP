"""
轻量级AI语音服务（Mock模式）

提供与funasr_local_server.py相同的WebSocket接口，不依赖FunASR/PyTorch。
用于开发环境验证语音助手前端功能，避免下载大模型。
生产环境请使用funasr_local_server.py。

接口协议：
  - WebSocket /ai-voice/ws/stream-asr  语音识别流式接口
  - GET /health                         健康检查
  - GET /info                           服务信息
"""
import asyncio
import json
import logging
import os
import socket
import time
import urllib.request
import urllib.error
from fastapi import FastAPI, WebSocket, WebSocketDisconnect
from fastapi.middleware.cors import CORSMiddleware
import uvicorn

# 配置日志
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')
logger = logging.getLogger(__name__)

# 创建FastAPI应用
app = FastAPI(title="AI Voice Service (Mock)", version="1.0.0")

# CORS配置
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


class NacosRegistrar:
    """Nacos服务注册器（轻量级实现，不依赖SDK）。"""

    def __init__(self, server_addr, service_name, port, namespace=None, group_name="DEFAULT_GROUP"):
        self.server_addr = server_addr
        self.service_name = service_name
        self.port = port
        self.namespace = namespace
        self.group_name = group_name
        self.base_url = server_addr if server_addr.startswith("http") else f"http://{server_addr}"
        self.registered = False
        self.ip = self._get_local_ip()

    def _get_local_ip(self):
        """获取容器本地IP。"""
        try:
            s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
            s.connect(("8.8.8.8", 80))
            ip = s.getsockname()[0]
            s.close()
            return ip
        except Exception:
            return "127.0.0.1"

    def register(self):
        """向Nacos注册服务实例。"""
        url = f"{self.base_url}/nacos/v1/ns/instance"
        params = {
            "serviceName": self.service_name,
            "ip": self.ip,
            "port": str(self.port),
            "groupName": self.group_name,
            "healthy": "true",
            "enabled": "true",
        }
        try:
            query = "&".join(f"{k}={v}" for k, v in params.items())
            full_url = f"{url}?{query}"
            req = urllib.request.Request(full_url, method="POST")
            with urllib.request.urlopen(req, timeout=5) as resp:
                if resp.status == 200:
                    self.registered = True
                    logger.info(f"Nacos注册成功: {self.service_name} @ {self.ip}:{self.port}")
                else:
                    logger.warning(f"Nacos注册失败: HTTP {resp.status}")
        except Exception as e:
            logger.warning(f"Nacos注册异常: {e}")

    def deregister(self):
        """从Nacos注销服务实例。"""
        if not self.registered:
            return
        url = f"{self.base_url}/nacos/v1/ns/instance"
        params = {
            "serviceName": self.service_name,
            "ip": self.ip,
            "port": str(self.port),
            "groupName": self.group_name,
        }
        try:
            query = "&".join(f"{k}={v}" for k, v in params.items())
            full_url = f"{url}?{query}"
            req = urllib.request.Request(full_url, method="DELETE")
            with urllib.request.urlopen(req, timeout=5) as resp:
                logger.info(f"Nacos注销完成: {self.service_name}")
        except Exception as e:
            logger.warning(f"Nacos注销异常: {e}")


# 全局变量
registrar = None
connected_clients = set()


@app.on_event("startup")
async def startup_event():
    """服务启动时注册Nacos。"""
    global registrar
    nacos_addr = os.environ.get("NACOS_SERVER_ADDR", "nacos:8848")
    service_name = os.environ.get("NACOS_SERVICE_NAME", "ai-voice-service")
    port = int(os.environ.get("SERVICE_PORT", "8000"))

    registrar = NacosRegistrar(nacos_addr, service_name, port)
    # 异步注册，避免阻塞启动
    registrar.register()
    logger.info(f"AI语音服务(Mock)已启动，监听端口 {port}")


@app.on_event("shutdown")
async def shutdown_event():
    """服务关闭时注销Nacos。"""
    if registrar:
        registrar.deregister()


@app.get("/health")
async def health():
    """健康检查端点。"""
    return {
        "status": "UP",
        "service": "ai-voice-service",
        "mode": "mock",
        "connected_clients": len(connected_clients)
    }


@app.get("/info")
async def info():
    """服务信息端点。"""
    return {
        "service": "ai-voice-service",
        "mode": "mock",
        "description": "轻量级Mock语音服务，不执行真实ASR识别",
        "endpoints": {
            "websocket": "/ai-voice/ws/stream-asr",
            "health": "/health",
            "info": "/info"
        }
    }


@app.websocket("/ai-voice/ws/stream-asr")
async def websocket_endpoint(websocket: WebSocket):
    """
    语音识别WebSocket端点（Mock模式）。

    协议说明：
      - 连接成功后发送 init_success 消息
      - 收到 ping 消息时回复 pong
      - 收到 start_session 消息时开始会话
      - 收到音频二进制数据时返回模拟识别结果
      - 收到 stop_session 消息时结束会话
      - 收到 end_session 消息时关闭连接
    """
    await websocket.accept()
    connected_clients.add(websocket)
    logger.info(f"WebSocket客户端已连接: {websocket.client}, 当前连接数: {len(connected_clients)}")

    session_started = False
    audio_frame_count = 0
    last_result_time = time.time()

    try:
        # 发送初始化成功消息
        await websocket.send_text(json.dumps({
            "type": "init_success",
            "message": "Voice Engine Mock V1.0 (Streaming)"
        }))

        while True:
            try:
                # 接收消息（1秒超时）
                message = await asyncio.wait_for(websocket.receive(), timeout=1.0)
            except asyncio.TimeoutError:
                continue

            if message["type"] == "websocket.disconnect":
                break

            # 处理文本消息（控制指令）
            if "text" in message and message["text"]:
                try:
                    data = json.loads(message["text"])

                    # 心跳ping
                    if data.get("type") == "ping":
                        await websocket.send_text(json.dumps({"type": "pong"}))
                        continue

                    # 开始会话
                    if data.get("type") == "start_session":
                        session_started = True
                        audio_frame_count = 0
                        await websocket.send_text(json.dumps({
                            "type": "session_started",
                            "message": "会话已开始（Mock模式）"
                        }))
                        logger.info(f"会话已开始: {websocket.client}")
                        continue

                    # 结束会话
                    if data.get("type") == "stop_session":
                        if session_started:
                            await websocket.send_text(json.dumps({
                                "type": "final",
                                "full_text": "语音识别服务处于Mock模式，请部署完整FunASR服务以获得真实识别结果。",
                                "timestamps": [],
                                "timestamp": int(time.time() * 1000)
                            }))
                        session_started = False
                        continue

                    # 关闭会话
                    if data.get("type") == "end_session":
                        await websocket.send_text(json.dumps({
                            "type": "session_ended",
                            "message": "会话已结束"
                        }))
                        break

                except json.JSONDecodeError:
                    logger.warning(f"JSON解析失败: {message['text'][:100]}")

            # 处理二进制音频数据
            if "bytes" in message and message["bytes"]:
                if not session_started:
                    continue

                audio_frame_count += 1
                now = time.time()

                # 每50帧（约1.6秒）返回一次模拟partial结果
                if now - last_result_time > 2.0 and audio_frame_count > 10:
                    mock_texts = [
                        "正在聆听",
                        "语音服务已连接",
                        "Mock模式运行中",
                    ]
                    mock_text = mock_texts[audio_frame_count % len(mock_texts)]
                    await websocket.send_text(json.dumps({
                        "type": "partial",
                        "text": mock_text,
                        "timestamp": int(now * 1000)
                    }))
                    last_result_time = now

    except WebSocketDisconnect:
        logger.info(f"WebSocket客户端断开: {websocket.client}")
    except Exception as e:
        logger.error(f"WebSocket端点异常: {e}")
    finally:
        connected_clients.discard(websocket)
        logger.info(f"连接关闭，剩余连接数: {len(connected_clients)}")


if __name__ == "__main__":
    port = int(os.environ.get("SERVICE_PORT", "8000"))
    log_level = os.environ.get("LOG_LEVEL", "info").lower()
    uvicorn.run(app, host="0.0.0.0", port=port, log_level=log_level)
