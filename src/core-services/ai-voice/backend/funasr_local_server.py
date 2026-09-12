import asyncio
import json
import logging
import os
import numpy as np
import websockets
from collections import deque
import socket
import time
import sys
import contextlib
import urllib.parse
import urllib.request
import urllib.error
from fastapi import FastAPI, WebSocket, WebSocketDisconnect
from fastapi.responses import JSONResponse
from fastapi.middleware.cors import CORSMiddleware
import uvicorn

os.environ.setdefault("TQDM_DISABLE", "1")
os.environ.setdefault("DISABLE_TQDM", "1")

from funasr import AutoModel


@contextlib.contextmanager
def suppress_output():
    """临时屏蔽标准输出与错误输出，避免模型推理阶段刷屏日志。"""
    with open(os.devnull, "w") as devnull:
        old_stdout = sys.stdout
        old_stderr = sys.stderr
        old___stdout = getattr(sys, "__stdout__", None)
        old___stderr = getattr(sys, "__stderr__", None)
        sys.stdout = devnull
        sys.stderr = devnull
        if old___stdout is not None:
            sys.__stdout__ = devnull
        if old___stderr is not None:
            sys.__stderr__ = devnull
        try:
            yield
        finally:
            sys.stdout = old_stdout
            sys.stderr = old_stderr
            if old___stdout is not None:
                sys.__stdout__ = old___stdout
            if old___stderr is not None:
                sys.__stderr__ = old___stderr

# 配置日志
logging.basicConfig(level=logging.DEBUG, format='%(asctime)s - %(levelname)s - %(message)s')
logger = logging.getLogger(__name__)
logger.setLevel(logging.DEBUG)

class NacosRegistrar:
    def __init__(self, server_addr, service_name, port, namespace=None, group_name="DEFAULT_GROUP", retry_count=3, timeout=30):
        self.server_addr = server_addr
        self.service_name = service_name
        self.port = port
        self.namespace = namespace
        self.group_name = group_name
        self.retry_count = retry_count
        self.timeout = timeout
        self.base_url = server_addr if server_addr.startswith("http") else f"http://{server_addr}"
        self.registered = False
        self.ip = self._get_local_ip()

    def _get_local_ip(self):
        try:
            # 尝试获取容器的真实IP
            s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
            s.connect(("8.8.8.8", 80))
            ip = s.getsockname()[0]
            s.close()
            return ip
        except Exception:
            return "127.0.0.1"

    def _request(self, method, path, params):
        """向 Nacos 发起 HTTP 请求（不依赖 nacos-sdk-python，避免 SDK 版本兼容问题）。"""
        if self.namespace:
            params = dict(params)
            params["namespaceId"] = self.namespace
        url = f"{self.base_url}{path}?{urllib.parse.urlencode(params)}"
        req = urllib.request.Request(url, method=method)
        with urllib.request.urlopen(req, timeout=self.timeout) as resp:
            return resp.read().decode("utf-8", errors="ignore")

    def register(self):
        success = False
        retry = 0
        
        while retry < self.retry_count and not success:
            retry += 1
            try:
                logger.info(f"=== NACOS注册开始 (第 {retry}/{self.retry_count} 次尝试) ===")
                logger.info(f"NACOS地址: {self.server_addr}")
                logger.info(f"服务名称: {self.service_name}")
                logger.info(f"本地IP: {self.ip}")
                logger.info(f"服务端口: {self.port}")
                logger.info(f"分组名称: {self.group_name}")
                logger.info(f"超时时间: {self.timeout}秒")

                logger.info(f"正在注册服务实例...")
                self._request(
                    "POST",
                    "/nacos/v1/ns/instance",
                    {
                        "serviceName": self.service_name,
                        "ip": self.ip,
                        "port": self.port,
                        "groupName": self.group_name,
                        "ephemeral": "true",
                    },
                )
                logger.info(f"服务已注册到 Nacos: {self.service_name} @ {self.ip}:{self.port}")
                logger.info(f"=== NACOS注册完成 ===")
                self.registered = True
                success = True
                break
            except Exception as e:
                logger.error(f"=== NACOS注册失败 (第 {retry}/{self.retry_count} 次尝试) ===")
                logger.error(f"注册到 Nacos 失败: {e}")
                import traceback
                logger.error(f"详细错误栈: {traceback.format_exc()}")
                
                if retry < self.retry_count:
                    wait_time = 2 ** retry  # 指数退避
                    logger.info(f"将在 {wait_time} 秒后重试...")
                    time.sleep(wait_time)
                else:
                    logger.error(f"已达到最大重试次数 {self.retry_count}，Nacos注册失败")
        
        return success

    def deregister(self):
        if not self.registered:
            return
        try:
            self._request(
                "DELETE",
                "/nacos/v1/ns/instance",
                {
                    "serviceName": self.service_name,
                    "ip": self.ip,
                    "port": self.port,
                    "groupName": self.group_name,
                    "ephemeral": "true",
                },
            )
            self.registered = False
            logger.info("服务已从 Nacos 注销")
        except Exception as e:
            logger.error(f"从 Nacos 注销失败: {e}")
            import traceback
            logger.error(f"详细错误栈: {traceback.format_exc()}")

    def heartbeat(self):
        """发送心跳检测，保持服务注册状态"""
        if not self.registered:
            return
        try:
            beat = json.dumps({"serviceName": self.service_name, "ip": self.ip, "port": self.port})
            self._request(
                "PUT",
                "/nacos/v1/ns/instance/beat",
                {
                    "serviceName": self.service_name,
                    "ip": self.ip,
                    "port": self.port,
                    "groupName": self.group_name,
                    "ephemeral": "true",
                    "beat": beat,
                },
            )
            logger.debug(f"Nacos心跳发送成功: {self.service_name} @ {self.ip}:{self.port}")
        except Exception as e:
            logger.error(f"Nacos心跳发送失败: {e}")
            import traceback
            logger.error(f"详细错误栈: {traceback.format_exc()}")

class FunASRModel:
    def __init__(self, model_path, config=None):
        self.model_path = model_path
        self.config = config or {}
        self.model = None
        self.is_model_loaded = False
        self.model_config = self.config.get('model', {})

    def load_model(self):
        logger.info(f"=== 开始模型加载流程 ===")
        try:
            from pathlib import Path
            
            # 从配置中读取模型路径和下载开关
            download_enabled = self.model_config.get('download', {}).get('enabled', False)
            model_base_path = self.model_path
            
            vad_model_config_path = self.model_config.get('vad_model_path', '/app/models/iic/speech_fsmn_vad_zh-cn-16k-common-pytorch')
            timestamp_model_config_path = self.model_config.get('timestamp_model_path', '/app/models/iic/speech_timestamp_prediction-v1-16k-offline')
            
            logger.info(f"模型配置: 下载开关={download_enabled}, ASR模型路径={model_base_path}, VAD模型路径={vad_model_config_path}, 时间戳模型路径={timestamp_model_config_path}")
            
            final_model_path = None
            mount_model_path = Path(model_base_path)
            
            # 1. 优先检查本地挂载的ASR模型路径
            if mount_model_path.exists() and mount_model_path.is_dir():
                logger.info(f"✓ 本地挂载ASR模型路径存在: {mount_model_path}")
                # 检查模型完整性（简单检查，确保有模型文件）
                model_files = list(mount_model_path.glob("*.pt")) + list(mount_model_path.glob("*.onnx")) + list(mount_model_path.glob("*.bin"))
                if len(model_files) > 0:
                    logger.info(f"✓ 本地ASR模型文件完整，找到 {len(model_files)} 个模型文件")
                    final_model_path = str(mount_model_path)
                else:
                    logger.warning(f"⚠ 本地ASR模型路径存在但缺少模型文件: {mount_model_path}")
            else:
                logger.info(f"ℹ 本地挂载ASR模型路径不存在或不是目录: {mount_model_path}")
                # 2. 检查modelscope缓存路径
                cache_path = Path.home() / ".cache" / "modelscope" / "hub" / "models" / "damo" / "speech_paraformer-large_asr_nat-zh-cn-16k-common-vocab8404-pytorch"
                if cache_path.exists():
                    logger.info(f"✓ 使用modelscope缓存路径: {cache_path}")
                    final_model_path = str(cache_path)
                else:
                    logger.info(f"ℹ modelscope缓存路径不存在: {cache_path}")

            # 检查VAD和时间戳模型路径
            vad_model_path = Path(vad_model_config_path)
            vad_model_final = str(vad_model_path) if vad_model_path.exists() else None
            if vad_model_final:
                logger.info(f"✓ 本地VAD模型路径存在: {vad_model_path}")
            else:
                logger.info(f"ℹ 本地VAD模型路径不存在: {vad_model_path}")
                if not download_enabled:
                    logger.warning(f"⚠ 下载开关关闭，无法从网络获取VAD模型")

            timestamp_model_path = Path(timestamp_model_config_path)
            timestamp_model_final = str(timestamp_model_path) if timestamp_model_path.exists() else None
            if timestamp_model_final:
                logger.info(f"✓ 本地时间戳模型路径存在: {timestamp_model_path}")
            else:
                logger.info(f"ℹ 本地时间戳模型路径不存在: {timestamp_model_path}")
                if not download_enabled:
                    logger.warning(f"⚠ 下载开关关闭，无法从网络获取时间戳模型")

            # 统一加载逻辑
            if final_model_path:
                logger.info(f"=== 开始加载模型 ===")
                logger.info(f"ASR模型路径: {final_model_path}")
                logger.info(f"VAD模型路径: {'本地模型' if vad_model_final else '使用modelscope模型'}")
                logger.info(f"时间戳模型路径: {'本地模型' if timestamp_model_final else '使用modelscope模型'}")

                self.model = AutoModel(
                    model=final_model_path,
                    vad_model=vad_model_final if vad_model_final else ("damo/speech_fsmn_vad_zh-cn-16k-common-pytorch" if download_enabled else None),
                    timestamp_model=timestamp_model_final if timestamp_model_final else ("damo/speech_timestamp_prediction-v1-16k-offline" if download_enabled else None),
                    device="cpu",
                    disable_update=True
                )
                logger.info("✅ 模型加载成功，集成了VAD和时间戳预测功能")
                self.is_model_loaded = True
                return True

            # 3. 最后尝试从网络下载（如果启用了下载开关）
            if download_enabled:
                try:
                    logger.warning(f"ℹ 本地模型未找到，尝试从网络下载...")
                    self.model = AutoModel(
                        model="damo/speech_paraformer-large_asr_nat-zh-cn-16k-common-vocab8404-pytorch",
                        vad_model="damo/speech_fsmn_vad_zh-cn-16k-common-pytorch",
                        timestamp_model="damo/speech_timestamp_prediction-v1-16k-offline",
                        model_revision="v2.0.4",
                        device="cpu",
                        disable_update=True
                    )
                    logger.info("✅ 模型下载并加载成功，集成了VAD和时间戳预测功能")
                    self.is_model_loaded = True
                    return True
                except Exception as download_error:
                    logger.error(f"❌ 模型下载失败: {download_error}")
                    logger.error("在资源有限环境下，建议提前准备模型文件并挂载到指定目录")
                    self.is_model_loaded = False
                    return False
            else:
                logger.error("❌ 下载开关已关闭，无法从网络获取模型")
                logger.error("请确保本地已挂载正确的模型文件，或启用下载开关")
                self.is_model_loaded = False
                return False
        except Exception as e:
            logger.error(f"❌ 加载模型失败: {e}")
            import traceback
            logger.error(f"详细错误栈: {traceback.format_exc()}")
            self.is_model_loaded = False
            return False
        finally:
            logger.info(f"=== 模型加载流程结束，加载状态: {'成功' if self.is_model_loaded else '失败'} ===")

    def recognize(self, audio_data, use_vad=True, use_timestamp=True):
        if self.model is None:
            return {"text": "", "timestamps": []}

        try:
            # 统一预处理：16k 16bit Mono -> Float32 [-1.0, 1.0]
            audio_np = np.frombuffer(audio_data, dtype=np.int16).astype(np.float32) / 32768.0

            with suppress_output():
                res = self.model.generate(
                    input=audio_np,
                    batch_size_s=300,
                    vad=use_vad,
                    timestamp=use_timestamp,
                    output_dir="",
                    device="cpu"
                )

            if res and len(res) > 0:
                raw_text = res[0].get('text', '')
                # 后端去噪与去重复策略
                cleaned_text = raw_text.replace('[unk]', '').strip()
                
                # 简单重复词过滤 (如 "你好你好" -> "你好")
                if len(cleaned_text) > 4:
                    words = list(cleaned_text)
                    if len(words) >= 4 and words[0:2] == words[2:4]:
                         # 针对叠词的简单处理，实际可根据业务需求增强
                         pass 

                timestamps = res[0].get('timestamp', []) if use_timestamp else []
                return {"text": cleaned_text, "timestamps": timestamps}
            return {"text": "", "timestamps": []}
        except Exception as e:
            logger.error(f"识别错误: {e}")
            return {"text": "", "timestamps": []}

class VoiceServer:
    def __init__(self, host='0.0.0.0', port=10095, model_path=None):
        # 加载配置文件
        self.config = self.load_config()
        
        # 服务器配置
        server_conf = self.config.get('server', {})
        self.host = server_conf.get('host', host)
        self.port = server_conf.get('port', port)
        log_level = server_conf.get('log_level', 'info')
        self.health_check_interval = server_conf.get('health_check_interval_s', 30)
        self.max_request_size = server_conf.get('max_request_size_mb', 10) * 1024 * 1024
        
        # 设置日志级别
        self._setup_logger(log_level)
        
        # 模型配置
        model_conf = self.config.get('model', {})
        config_model_path = model_conf.get('base_path')
        self.model_path = model_path or config_model_path or "/app/models/speech_paraformer-small_asr_nat-zh-cn-16k-common-vocab8404"
        inference_conf = model_conf.get('inference', {})
        self.MODEL_USE_VAD = inference_conf.get('use_vad', True)
        self.MODEL_USE_TIMESTAMP = inference_conf.get('use_timestamp', True)
        
        # 初始化模型包装器
        self.model_wrapper = FunASRModel(self.model_path, self.config)
        
        # 系统配置
        system_conf = self.config.get('system', {})
        self.idle_timeout = system_conf.get('idle_timeout', 360)  # 6分钟无活跃连接则释放模型资源
        self.silence_timeout = system_conf.get('silence_timeout_s', 300) # 5分钟无语音检测则断开连接
        self.no_audio_timeout = system_conf.get('no_audio_timeout_s', 60) # 连接后长时间无音频输入则断开连接
        self.max_clients = system_conf.get('max_clients', 15)
        self.request_timeout = system_conf.get('request_timeout_s', 30)
        self.retry_times = system_conf.get('retry_times', 3)
        self.retry_delay = system_conf.get('retry_delay_ms', 1000) / 1000.0
        
        # 音频配置
        audio_conf = self.config.get('audio', {})
        self.MAX_BUFFER_DURATION = audio_conf.get('max_buffer_duration_s', 25.0)
        self.SAMPLE_RATE = audio_conf.get('sample_rate', 16000)
        self.AUDIO_CHANNELS = audio_conf.get('channels', 1)
        self.AUDIO_SAMPLE_WIDTH = audio_conf.get('sample_width', 2)
        self.CHUNK_SIZE = audio_conf.get('chunk_size', 8000)
        
        # VAD配置
        vad_conf = self.config.get('vad', {})
        self.VAD_SILENCE_THRESHOLD = vad_conf.get('silence_threshold_ms', 1.5)
        self.VAD_MAX_DURATION = vad_conf.get('max_sentence_duration_s', 15.0)
        self.VAD_RMS_THRESHOLD = vad_conf.get('silence_rms_threshold', 120)
        self.VAD_SPEECH_RMS_THRESHOLD = vad_conf.get('speech_rms_threshold', 150)
        self.VAD_ENERGY_THRESHOLD = vad_conf.get('vad_energy_threshold', 1000)
        self.VAD_FRAMES_WINDOW = vad_conf.get('vad_frames_window', 5)
        self.VAD_MIN_SPEECH_DURATION = vad_conf.get('min_speech_duration_s', 0.4)
        
        # WebSocket配置
        websocket_conf = self.config.get('websocket', {})
        self.WEBSOCKET_PING_INTERVAL = websocket_conf.get('ping_interval_s', 30)
        self.WEBSOCKET_PONG_TIMEOUT = websocket_conf.get('pong_timeout_s', 10)
        self.WEBSOCKET_MAX_MESSAGE_SIZE = websocket_conf.get('max_message_size_bytes', 1048576)
        
        # 其他初始化
        self.clients = set()
        self.model_loaded = False  # 添加模型加载状态标记
        self.last_active_time = time.time()
        self.idle_check_timer = None
        
        logger.info(f"Init VoiceServer on {self.host}:{self.port} with config version: {self.config.get('version', 'unknown')}")
        logger.debug(f"Full config: {json.dumps(self.config, indent=2)}")
    
    def _setup_logger(self, log_level):
        """设置日志级别"""
        level = getattr(logging, log_level.upper(), logging.INFO)
        logger.setLevel(level)
        
        # 设置所有相关日志器的级别
        for name in logging.root.manager.loggerDict:
            if name.startswith('__main__') or name == 'uvicorn' or name == 'fastapi':
                logging.getLogger(name).setLevel(level)

    def load_config(self):
        """加载配置文件
        优先级：配置文件 > 环境变量 > 默认值
        """
        try:
            config_path = os.path.join(os.path.dirname(__file__), 'config.json')
            config = {}
            
            # 1. 从配置文件加载配置
            if os.path.exists(config_path):
                with open(config_path, 'r', encoding='utf-8') as f:
                    config = json.load(f)
                logger.info(f"Loaded config from {config_path}")
            else:
                logger.warning(f"Config file not found at {config_path}, using default config")
            
            # 2. 从环境变量加载配置，覆盖配置文件
            env_config = self._load_config_from_env()
            config = self._merge_configs(config, env_config)
            
            # 3. 验证配置
            self._validate_config(config)
            
            return config
        except Exception as e:
            logger.error(f"Failed to load config: {e}")
            import traceback
            logger.error(f"Config load error stack: {traceback.format_exc()}")
            return self._get_default_config()
    
    def save_config(self, new_config=None):
        """保存配置到文件
        Args:
            new_config: 新的配置字典，如果为None则保存当前配置
        """
        try:
            config_path = os.path.join(os.path.dirname(__file__), 'config.json')
            config_to_save = new_config if new_config else self.config
            
            # 验证配置
            self._validate_config(config_to_save)
            
            # 保存配置
            with open(config_path, 'w', encoding='utf-8') as f:
                json.dump(config_to_save, f, indent=4, ensure_ascii=False)
            
            # 更新当前配置
            self.config = config_to_save
            logger.info(f"Config saved to {config_path}")
            return True
        except Exception as e:
            logger.error(f"Failed to save config: {e}")
            import traceback
            logger.error(f"Config save error stack: {traceback.format_exc()}")
            return False
    
    def _load_config_from_env(self):
        """从环境变量加载配置"""
        env_config = {}
        
        # 服务器配置
        server_port = os.environ.get('VOICE_SERVER_PORT')
        if server_port:
            env_config.setdefault('server', {})['port'] = int(server_port)
        
        # 模型配置
        model_dir = os.environ.get('MODEL_DIR')
        if model_dir:
            env_config.setdefault('model', {})['base_path'] = model_dir
        
        return env_config
    
    def _merge_configs(self, base_config, override_config):
        """合并配置，用override_config覆盖base_config"""
        def deep_merge(dest, src):
            for key, value in src.items():
                if isinstance(value, dict) and key in dest and isinstance(dest[key], dict):
                    deep_merge(dest[key], value)
                else:
                    dest[key] = value
            return dest
        
        return deep_merge(base_config.copy(), override_config)
    
    def _validate_config(self, config):
        """验证配置的有效性"""
        # 基本配置验证
        assert isinstance(config, dict), "Config must be a dictionary"
        
        # 服务器配置验证
        server_config = config.get('server', {})
        assert isinstance(server_config, dict), "Server config must be a dictionary"
        assert 'port' in server_config, "Server port is required"
        assert isinstance(server_config['port'], int), "Server port must be an integer"
        assert 1 <= server_config['port'] <= 65535, "Server port must be between 1 and 65535"
        
        # VAD配置验证
        vad_config = config.get('vad', {})
        assert isinstance(vad_config, dict), "VAD config must be a dictionary"
        assert 'silence_threshold_ms' in vad_config, "VAD silence threshold is required"
        assert isinstance(vad_config['silence_threshold_ms'], (int, float)), "VAD silence threshold must be a number"
        assert 'max_sentence_duration_s' in vad_config, "VAD max sentence duration is required"
        assert isinstance(vad_config['max_sentence_duration_s'], (int, float)), "VAD max sentence duration must be a number"
        assert 'silence_rms_threshold' in vad_config, "VAD RMS threshold is required"
        assert isinstance(vad_config['silence_rms_threshold'], (int, float)), "VAD RMS threshold must be a number"
        if 'speech_rms_threshold' in vad_config:
            assert isinstance(vad_config['speech_rms_threshold'], (int, float)), "VAD speech rms threshold must be a number"
        if 'vad_frames_window' in vad_config:
            assert isinstance(vad_config['vad_frames_window'], int), "VAD frames window must be an integer"
            assert vad_config['vad_frames_window'] > 0, "VAD frames window must be positive"
        if 'min_speech_duration_s' in vad_config:
            assert isinstance(vad_config['min_speech_duration_s'], (int, float)), "VAD min speech duration must be a number"
            assert vad_config['min_speech_duration_s'] >= 0, "VAD min speech duration must be >= 0"
        
        # 音频配置验证
        audio_config = config.get('audio', {})
        assert isinstance(audio_config, dict), "Audio config must be a dictionary"
        assert 'sample_rate' in audio_config, "Audio sample rate is required"
        assert isinstance(audio_config['sample_rate'], int), "Audio sample rate must be an integer"
        assert audio_config['sample_rate'] > 0, "Audio sample rate must be positive"
        
        # 模型配置验证
        model_config = config.get('model', {})
        assert isinstance(model_config, dict), "Model config must be a dictionary"
        assert 'base_path' in model_config, "Model base path is required"
        assert isinstance(model_config['base_path'], str), "Model base path must be a string"
        
        # 系统配置验证
        system_config = config.get('system', {})
        assert isinstance(system_config, dict), "System config must be a dictionary"
        assert 'idle_timeout' in system_config, "System idle timeout is required"
        assert isinstance(system_config['idle_timeout'], int), "System idle timeout must be an integer"
        assert system_config['idle_timeout'] > 0, "System idle timeout must be positive"
        if 'silence_timeout_s' in system_config:
            assert isinstance(system_config['silence_timeout_s'], int), "System silence_timeout_s must be an integer"
            assert system_config['silence_timeout_s'] >= 0, "System silence_timeout_s must be >= 0"
        if 'no_audio_timeout_s' in system_config:
            assert isinstance(system_config['no_audio_timeout_s'], int), "System no_audio_timeout_s must be an integer"
            assert system_config['no_audio_timeout_s'] >= 0, "System no_audio_timeout_s must be >= 0"
        
        # WebSocket配置验证
        websocket_config = config.get('websocket', {})
        assert isinstance(websocket_config, dict), "WebSocket config must be a dictionary"
        
        # Logging配置验证
        logging_config = config.get('logging', {})
        assert isinstance(logging_config, dict), "Logging config must be a dictionary"
        
        logger.info("Config validation passed")
    
    def _get_default_config(self):
        """获取默认配置"""
        return {
            "version": "2.0.0",
            "server": {
                "host": "0.0.0.0",
                "port": 8000,
                "log_level": "info",
                "health_check_interval_s": 30,
                "max_request_size_mb": 10
            },
            "vad": {
                "silence_threshold_ms": 2.0,
                "max_sentence_duration_s": 15.0,
                "silence_rms_threshold": 300,
                "speech_rms_threshold": 500,
                "vad_energy_threshold": 2000,
                "vad_frames_window": 5,
                "min_speech_duration_s": 0.4
            },
            "audio": {
                "sample_rate": 16000,
                "max_buffer_duration_s": 25.0,
                "channels": 1,
                "sample_width": 2,
                "chunk_size": 8000
            },
            "model": {
                "base_path": "/app/models/speech_paraformer-small_asr_nat-zh-cn-16k-common-vocab8404",
                "vad_model_path": "/app/models/iic/speech_fsmn_vad_zh-cn-16k-common-pytorch",
                "timestamp_model_path": "/app/models/iic/speech_timestamp_prediction-v1-16k-offline",
                "download": {
                    "enabled": False,
                    "modelscope_cache_path": "~/.cache/modelscope/hub/models/damo/",
                    "timeout_s": 600
                },
                "inference": {
                    "beam_size": 15,
                    "ctc_weight": 0.6,
                    "max_len_ratio": 1.2,
                    "min_len_ratio": 0.03,
                    "use_timestamp": True,
                    "use_vad": True
                },
                "optimization": {
                    "lazy_load": True,
                    "cache_enabled": True,
                    "batch_size": 2,
                    "enable_fp16": False,
                    "enable_dynamic_batching": True
                }
            },
            "system": {
                "idle_timeout": 360,
                "silence_timeout_s": 300,
                "no_audio_timeout_s": 60,
                "max_clients": 15,
                "request_timeout_s": 30,
                "retry_times": 3,
                "retry_delay_ms": 1000
            },
            "websocket": {
                "ping_interval_s": 30,
                "pong_timeout_s": 10,
                "max_message_size_bytes": 1048576
            },
            "logging": {
                "level": "info",
                "file_path": "/app/logs/ai-voice-service.log",
                "max_file_size_mb": 100,
                "backup_count": 5,
                "format": "%(asctime)s - %(name)s - %(levelname)s - %(message)s"
            }
        }

    def analyze_audio(self, audio_data):
        if not audio_data:
            logger.debug(f"No audio data, treating as silent")
            return True, False, 0.0
            
        try:
            # 尝试将音频数据转换为numpy数组
            audio_np = np.frombuffer(audio_data, dtype=np.int16)
            
            # 检查音频数据是否为空
            if len(audio_np) == 0:
                logger.debug(f"Audio data is empty, treating as silent")
                return True, False, 0.0
            
            # 检查是否所有样本都是0
            is_all_zero = np.all(audio_np == 0)
            if is_all_zero:
                logger.debug(f"All audio samples are zero, treating as silent")
                return True, False, 0.0
            
            # 计算RMS值，添加保护措施
            # 使用np.nanmean和np.nan_to_num来处理可能的无效值
            squared = np.square(audio_np.astype(float))  # 转换为float避免整数溢出
            mean_square = np.nanmean(squared)
            # 确保mean_square不是nan或inf
            mean_square = np.nan_to_num(mean_square, nan=0.0, posinf=0.0, neginf=0.0)
            
            rms = np.sqrt(mean_square)
            # 确保rms不是nan或inf
            rms = np.nan_to_num(rms, nan=0.0, posinf=0.0, neginf=0.0)
            
            # 检查RMS是否低于阈值
            is_low_rms = rms < self.VAD_RMS_THRESHOLD
            is_speech_rms = rms >= self.VAD_SPEECH_RMS_THRESHOLD
            
            # 自适应阈值：根据环境噪声动态调整阈值
            logger.debug(f"RMS: {rms:.1f}, Threshold: {self.VAD_RMS_THRESHOLD}, AllZero: {is_all_zero}, Length: {len(audio_np)}")
            
            # 如果RMS低于阈值，则认为是静音
            return is_low_rms, is_speech_rms, float(rms)
        except Exception as e:
            # 任何异常都将音频视为静音
            logger.error(f"Error in is_silent_audio: {e}")
            return True, False, 0.0

    def is_silent_audio(self, audio_data):
        is_silence, _, _ = self.analyze_audio(audio_data)
        return is_silence

    async def handle_connection(self, websocket: WebSocket):
        await websocket.accept()
        logger.info(f"Connected: {websocket.client}")
        if len(self.clients) >= self.max_clients:
            logger.warning(f"Too many clients ({len(self.clients)}), rejecting connection")
            await websocket.close(code=1013, reason="Server overloaded")
            return

        self.clients.add(websocket)
        self.last_active_time = time.time()

        loop = asyncio.get_event_loop()
        connection_open_time = loop.time()
        last_audio_time = connection_open_time
        last_speech_time = connection_open_time
        session_started = False

        async def ensure_model_loaded():
            """确保模型已加载；仅在会话真正开始（开始发送音频/显式启动）时触发。"""
            if self.model_loaded:
                return True
            logger.info("Loading model on first audio/session start...")
            self.model_loaded = await loop.run_in_executor(None, lambda: self.model_wrapper.load_model())
            if self.model_loaded:
                logger.info("Model loaded successfully")
                return True
            logger.error("Failed to load model")
            return False
        
        # 启动空闲检查定时器
        if not self.idle_check_timer:
            self.start_idle_check()
        
        main_buffer = bytearray()
        silence_start_time = None
        last_partial_time = 0
        task_queue = asyncio.Queue()
        utterance_id = 0

        async def worker():
            active_utterance_id = None
            total_result = ""
            noise_texts = {'嗯', '嗯嗯', '好的', '好的好的', '对', '对对对', '啊', '哦', '我们不知道', '不可能'}
            while True:
                try:
                    task = await task_queue.get()
                    utter_id, audio_seg, t_type, speech_dur = task
                    if t_type == "partial_result" and task_queue.qsize() > 0:
                        task_queue.task_done()
                        continue
                    if active_utterance_id != utter_id:
                        active_utterance_id = utter_id
                        total_result = ""

                    if t_type == "final_result" and speech_dur < self.VAD_MIN_SPEECH_DURATION:
                        task_queue.task_done()
                        continue
                    start = asyncio.get_event_loop().time()
                    result = await asyncio.get_running_loop().run_in_executor(
                        None, lambda: self.model_wrapper.recognize(
                            audio_seg,
                            use_vad=self.MODEL_USE_VAD,
                            use_timestamp=(t_type == "final_result") and self.MODEL_USE_TIMESTAMP
                        )
                    )
                    current_text = result.get("text", "").replace('[unk]', '').strip()
                    if current_text:
                        if current_text in noise_texts and speech_dur < max(0.8, self.VAD_MIN_SPEECH_DURATION):
                            task_queue.task_done()
                            continue
                        latency = asyncio.get_event_loop().time() - start
                        logger.info(f"[{'FINAL' if t_type=='final_result' else 'LIVE'}] {latency:.2f}s | {current_text}")
                        
                        # 增量结果提取算法
                        response_data = {}
                        if len(current_text) > len(total_result):
                            # 新增文本，推送增量结果
                            new_text = current_text[len(total_result):]
                            total_result = current_text
                            response_data = {
                                "type": "increment",
                                "new_text": new_text,
                                "full_text": total_result,
                                "timestamp": int(asyncio.get_event_loop().time() * 1000)
                            }
                        elif len(current_text) < len(total_result):
                            # 模型回溯修正，推送完整修正结果
                            total_result = current_text
                            response_data = {
                                "type": "correct",
                                "full_text": total_result,
                                "timestamp": int(asyncio.get_event_loop().time() * 1000)
                            }
                        elif current_text != total_result:
                            # 内容变化但长度相同，推送完整修正结果
                            total_result = current_text
                            response_data = {
                                "type": "correct",
                                "full_text": total_result,
                                "timestamp": int(asyncio.get_event_loop().time() * 1000)
                            }
                        
                        # 最终结果直接推送
                        if t_type == "final_result":
                            total_result = current_text
                            response_data = {
                                "type": "final",
                                "full_text": total_result,
                                "timestamps": result.get("timestamps", []),
                                "timestamp": int(asyncio.get_event_loop().time() * 1000)
                            }
                        
                        if response_data:
                            await websocket.send_text(json.dumps(response_data))
                            if t_type == "final_result":
                                total_result = ""
                    task_queue.task_done()
                except asyncio.CancelledError: break
                except Exception as e:
                    logger.error(f"Worker Error: {e}")
                    task_queue.task_done()

        worker_task = asyncio.create_task(worker())

        try:
            await websocket.send_text(json.dumps({"type": "init_success", "message": "Voice Engine V10 (Streaming Optimized)"}))
            speech_detected = False
            speech_duration_s = 0.0
            speech_window = deque(maxlen=max(1, int(self.VAD_FRAMES_WINDOW)))
            current_utterance_id = None
            while True:
                try:
                    try:
                        message = await asyncio.wait_for(websocket.receive(), timeout=1.0)
                    except asyncio.TimeoutError:
                        now = loop.time()
                        if not session_started and (now - connection_open_time) > self.no_audio_timeout:
                            logger.info(f"Connection closed due to no-audio timeout ({self.no_audio_timeout}s)")
                            await websocket.close(code=1000, reason="No audio timeout")
                            break
                        if session_started and (now - last_audio_time) > self.no_audio_timeout:
                            logger.info(f"Connection closed due to audio inactivity timeout ({self.no_audio_timeout}s)")
                            await websocket.close(code=1000, reason="Audio inactivity timeout")
                            break
                        continue

                    if message["type"] == "websocket.disconnect":
                        break
                    
                    if "text" in message and message["text"]:
                        try:
                            data = json.loads(message["text"])
                            if data.get('type') == 'ping':
                                await websocket.send_text(json.dumps({"type": "pong"}))
                                continue
                            if data.get('type') == 'start_session':
                                session_started = True
                                if not await ensure_model_loaded():
                                    await websocket.send_text(json.dumps({"type": "error", "message": "model_load_failed"}))
                                    await websocket.close(code=1011, reason="Model load failed")
                                    break
                                continue
                            elif data.get('type') == 'end_session':
                                # 重置所有状态
                                main_buffer.clear()
                                silence_start_time = None
                                last_partial_time = 0
                                speech_detected = False
                                speech_duration_s = 0.0
                                speech_window.clear()
                                current_utterance_id = None
                                session_started = False
                                logger.info("会话已结束，所有状态已重置")
                                await websocket.close()
                                break
                        except: pass
                        continue
                    
                    if "bytes" in message and message["bytes"]:
                        audio_bytes = message["bytes"]
                        now = loop.time()
                        self.last_active_time = now
                        last_audio_time = now

                        if not session_started:
                            session_started = True
                            if not await ensure_model_loaded():
                                await websocket.send_text(json.dumps({"type": "error", "message": "model_load_failed"}))
                                await websocket.close(code=1011, reason="Model load failed")
                                break

                        is_silence, is_speech, rms = self.analyze_audio(audio_bytes)
                        speech_window.append(1 if is_speech else 0)
                        window_ready = len(speech_window) == speech_window.maxlen
                        window_speech_ratio = (sum(speech_window) / len(speech_window)) if speech_window else 0.0
                        
                        if not speech_detected:
                            if window_ready and window_speech_ratio >= 0.6:
                                speech_detected = True
                                utterance_id += 1
                                current_utterance_id = utterance_id
                                speech_duration_s = 0.0
                            else:
                                continue

                        chunk_dur = len(audio_bytes) / 2 / self.SAMPLE_RATE

                        if not is_silence:
                            last_speech_time = now # 更新最后一次语音检测时间
                            main_buffer.extend(audio_bytes)
                            silence_start_time = None
                            speech_duration_s += chunk_dur
                            
                            dur = len(main_buffer) / 2 / self.SAMPLE_RATE
                            if (now - last_partial_time) > 0.8 and dur > 0.8 and speech_duration_s >= self.VAD_MIN_SPEECH_DURATION and window_speech_ratio >= 0.4:
                                p_len = 10 * self.SAMPLE_RATE * 2
                                task_queue.put_nowait((current_utterance_id, bytes(main_buffer[-p_len:] if dur > 10 else main_buffer), "partial_result", speech_duration_s))
                                last_partial_time = now
                        else:
                            # 检查静音超时
                            if (now - last_speech_time) > self.silence_timeout:
                                logger.info(f"Connection closed due to silence timeout ({self.silence_timeout}s)")
                                await websocket.close(code=1000, reason="Silence timeout")
                                break

                            # 如果是静音数据，检查是否需要断句
                            if silence_start_time is None: silence_start_time = now
                            
                            dur = len(main_buffer) / 2 / self.SAMPLE_RATE
                            if silence_start_time and (now - silence_start_time) > self.VAD_SILENCE_THRESHOLD and dur > 0.6 and speech_duration_s >= self.VAD_MIN_SPEECH_DURATION:
                                # 如果之前有非静音数据，并且静音时间超过阈值，则进行最终识别
                                if main_buffer:  # 只有当缓冲区有数据时才进行识别
                                    task_queue.put_nowait((current_utterance_id, bytes(main_buffer), "final_result", speech_duration_s))
                                    main_buffer.clear()
                                silence_start_time = None
                                last_partial_time = now
                                speech_detected = False
                                speech_duration_s = 0.0
                                speech_window.clear()
                                current_utterance_id = None
                            elif dur >= self.VAD_MAX_DURATION:
                                # 如果缓冲区数据超过最大时长，无论是否静音，都进行最终识别
                                if main_buffer:  # 只有当缓冲区有数据时才进行识别
                                    task_queue.put_nowait((current_utterance_id, bytes(main_buffer), "final_result", speech_duration_s))
                                    main_buffer.clear()
                                silence_start_time = None
                                last_partial_time = now
                                speech_detected = False
                                speech_duration_s = 0.0
                                speech_window.clear()
                                current_utterance_id = None

                except WebSocketDisconnect:
                    logger.info(f"Closed: {websocket.client}")
                    break
                except Exception as e:
                    logger.error(f"Receive error: {e}")
                    import traceback
                    logger.error(traceback.format_exc())
                    break

        except websockets.exceptions.ConnectionClosed:
            logger.info(f"Closed: {websocket.remote_address}")
        except Exception as e:
            logger.error(f"Loop error: {e}")
            import traceback
            logger.error(traceback.format_exc())
        finally:
            worker_task.cancel()
            if websocket in self.clients:
                self.clients.remove(websocket)
            # 如果没有活跃连接，停止空闲检查定时器
            if not self.clients:
                if self.idle_check_timer:
                    self.idle_check_timer.cancel()
                    self.idle_check_timer = None
            logger.info(f"Connection closed, active clients: {len(self.clients)}")

    def start_idle_check(self):
        async def check_idle():
            while True:
                await asyncio.sleep(60)  # 每分钟检查一次
                if not self.clients and self.model_loaded:
                    idle_time = time.time() - self.last_active_time
                    if idle_time > self.idle_timeout:
                        logger.info("Releasing model resources due to inactivity...")
                        self.model_wrapper = FunASRModel(self.model_path)  # 释放模型资源
                        self.model_loaded = False
                        self.idle_check_timer = None
                        break
        
        self.idle_check_timer = asyncio.create_task(check_idle())
    
    async def start(self):
        logger.info(f"正在初始化语音服务器...")
        
        # 配置验证
        logger.info("=== 开始配置验证 ===")
        try:
            # 验证服务器配置
            server_config = self.config.get('server', {})
            port = server_config.get('port', 8000)
            if not isinstance(port, int) or port < 1 or port > 65535:
                logger.error(f"❌ 无效的服务器端口: {port}")
                return False
            
            # 验证模型配置
            model_config = self.config.get('model', {})
            model_base_path = model_config.get('base_path')
            if not model_base_path:
                logger.error("❌ 缺少模型基础路径配置")
                return False
            
            # 验证下载配置
            download_config = model_config.get('download', {})
            download_enabled = download_config.get('enabled', False)
            if not isinstance(download_enabled, bool):
                logger.error(f"❌ 无效的下载开关配置: {download_enabled}")
                return False
            
            # 验证VAD配置
            vad_config = self.config.get('vad', {})
            silence_threshold = vad_config.get('silence_threshold_ms', 1.5)
            max_duration = vad_config.get('max_sentence_duration_s', 12.0)
            
            if not isinstance(silence_threshold, (int, float)) or silence_threshold <= 0:
                logger.error(f"❌ 无效的静音阈值: {silence_threshold}")
                return False
            
            if not isinstance(max_duration, (int, float)) or max_duration <= 0:
                logger.error(f"❌ 无效的最大句子时长: {max_duration}")
                return False
            
            # 验证音频配置
            audio_config = self.config.get('audio', {})
            sample_rate = audio_config.get('sample_rate', 16000)
            if not isinstance(sample_rate, int) or sample_rate <= 0:
                logger.error(f"❌ 无效的采样率: {sample_rate}")
                return False
            
            logger.info("✅ 配置验证通过")
        except Exception as e:
            logger.error(f"❌ 配置验证失败: {e}")
            return False
        
        # 服务器启动时不加载模型，改为懒加载
        logger.info(f"语音服务器初始化完成，模型将在首次请求时加载")
        logger.info(f"服务器配置: host={self.host}, port={self.port}")
        logger.info(f"模型配置: base_path={self.model_path}, download_enabled={self.config.get('model', {}).get('download', {}).get('enabled', False)}")
        logger.info(f"VAD配置: silence_threshold={self.VAD_SILENCE_THRESHOLD}s, max_duration={self.VAD_MAX_DURATION}s")
        logger.info(f"音频配置: sample_rate={self.SAMPLE_RATE}, max_buffer={self.MAX_BUFFER_DURATION}s")
        return True

# 创建FastAPI应用实例
app = FastAPI()
voice_server = None

# 配置CORS
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # 允许所有来源
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# 健康检查接口
@app.get("/health")
async def health_check():
    global voice_server
    return JSONResponse({
        "status": "healthy",
        "service": "funasr-stream-asr",
        "model_status": "loaded" if voice_server and voice_server.model_wrapper.is_model_loaded else "not_loaded",
        "timestamp": int(asyncio.get_event_loop().time() * 1000)
    })

# WebSocket路由
@app.websocket("/ai-voice/ws/stream-asr")
async def websocket_endpoint(websocket: WebSocket):
    global voice_server
    try:
        if voice_server is None:
            logger.error("voice_server is None!")
            await websocket.close(code=1011)
            return
        await voice_server.handle_connection(websocket)
    except Exception as e:
        logger.error(f"WebSocket endpoint error: {e}")
        import traceback
        logger.error(traceback.format_exc())

async def main():
    # 从环境变量获取配置
    port = int(os.environ.get("VOICE_SERVER_PORT", 8000))
    model_dir = os.environ.get("MODEL_DIR", "/app/models/speech_paraformer-small_asr_nat-zh-cn-16k-common-vocab8404")
    nacos_addr = os.environ.get("NACOS_ADDR", "nacos:8848")
    service_name = os.environ.get("SERVICE_NAME", "ai-voice-service")

    logger.info(f"AI语音服务启动配置: 端口={port}, 模型目录={model_dir}, NACOS地址={nacos_addr}, 服务名称={service_name}")

    # Nacos 服务注册
    registrar = None
    try:
        registrar = NacosRegistrar(nacos_addr, service_name, port)
        registrar.register()
        logger.info("Nacos服务注册完成")
        
        # 启动心跳检测
        async def heartbeat_task():
            while True:
                try:
                    registrar.heartbeat()
                except Exception as e:
                    logger.error(f"心跳检测失败: {e}")
                await asyncio.sleep(30)  # 每30秒发送一次心跳
        
        # 启动心跳检测任务
        asyncio.create_task(heartbeat_task())
        logger.info("Nacos心跳检测已启动")
    except Exception as e:
        logger.error(f"Nacos服务注册失败: {e}")
        import traceback
        logger.error(f"详细错误栈: {traceback.format_exc()}")
    
    # 初始化全局voice_server实例
    global voice_server
    voice_server = VoiceServer(model_path=model_dir, port=port)
    
    # 加载模型（懒加载，实际在第一次请求时加载）
    await voice_server.start()
    
    # 启动uvicorn服务器
    config = uvicorn.Config(
        app, 
        host="0.0.0.0", 
        port=port, 
        log_level="info",
        proxy_headers=True,
        forwarded_allow_ips="*"
    )
    server = uvicorn.Server(config)
    await server.serve()
    
    # 服务停止时注销Nacos服务
    if registrar:
        registrar.deregister()

if __name__ == "__main__":
    try:
        asyncio.run(main())
    except KeyboardInterrupt:
        pass
