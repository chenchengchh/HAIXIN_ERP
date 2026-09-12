export interface SttResult {
  text: string
  isFinal: boolean
  timestamps?: Array<{start: number; end: number; text: string}>
}

export type SttCallback = (result: SttResult) => void

export class RemoteSttEngine {
  private ws: WebSocket | null = null
  private audioContext: AudioContext | null = null
  private source: MediaStreamAudioSourceNode | null = null
  private processor: ScriptProcessorNode | AudioWorkletNode | null = null
  private isListening = false
  private onResultCallback: SttCallback | null = null
  private wsUrl: string
  private retryCount = 0
  private maxRetries = 3
  private retryDelay = 3000
  private mediaStream: MediaStream | null = null
  private heartbeatTimer: any = null
  private heartbeatInterval = 30000 // 30秒发送一次心跳
  private connectionStatus: 'disconnected' | 'connecting' | 'connected' | 'error' = 'disconnected'
  private wsCloseTimer: any = null
  private wsCloseDelay = 30000 // 30秒后自动关闭WebSocket连接
  private workletBlobUrl: string | null = null // 存储AudioWorklet Blob URL，用于释放资源

  constructor(wsUrl?: string) {
    if (!wsUrl) {
      // 动态生成 WebSocket URL，支持开发环境代理和生产环境部署
      const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
      const host = window.location.host
      // 开发环境下 /ai-voice 会被 Vite 代理转发
      this.wsUrl = `${protocol}//${host}/ai-voice/ws/stream-asr`
    } else {
      this.wsUrl = wsUrl
    }
  }

  /**
   * 预热硬件资源（麦克风和AudioContext）
   * 提前调用可显著降低首次及后续启动延迟
   */
  async ensureAudioInitialized() {
    if (this.audioContext && this.mediaStream && this.mediaStream.active) {
      if (this.audioContext.state === 'suspended') {
        await this.audioContext.resume();
      }
      return;
    }

    try {
      console.log('[RemoteSTT] Initializing hardware resources...')
      this.mediaStream = await navigator.mediaDevices.getUserMedia({ 
        audio: { 
          sampleRate: 16000,
          channelCount: 1,
          echoCancellation: true,
          noiseSuppression: true,
          autoGainControl: true
        } 
      })
      
      const AudioContextClass = (window as any).AudioContext || (window as any).webkitAudioContext
      this.audioContext = new AudioContextClass({ sampleRate: 16000 })
      
      // 使用更兼容的方法创建音频源
      try {
        const audioContext = this.audioContext!
        this.source = audioContext.createMediaStreamSource(this.mediaStream)
        console.log('[RemoteSTT] Audio source created successfully')
      } catch (e) {
        console.error('[RemoteSTT] Failed to create audio source:', e)
        throw e
      }
      
      if ('AudioWorklet' in window) {
        await this.initAudioWorklet()
      } else {
        this.initScriptProcessor()
      }
      console.log('[RemoteSTT] Hardware resources initialized')
    } catch (e) {
      console.error('[RemoteSTT] Hardware initialization failed:', e)
      throw e
    }
  }

  private startHeartbeat() {
    this.stopHeartbeat() // 先停止已有的心跳
    this.heartbeatTimer = setInterval(() => {
      if (this.ws && this.ws.readyState === WebSocket.OPEN) {
        try {
          this.ws.send(JSON.stringify({ type: 'ping' }))
          console.log('[RemoteSTT] Sent ping to server')
        } catch (e) {
          console.error('[RemoteSTT] Failed to send ping:', e)
          // 如果发送失败，尝试重连
          this.handleReconnect()
        }
      }
    }, this.heartbeatInterval)
  }

  private stopHeartbeat() {
    if (this.heartbeatTimer) {
      clearInterval(this.heartbeatTimer)
      this.heartbeatTimer = null
    }
  }

  async start(onResult: SttCallback) {
    if (this.isListening) return
    this.onResultCallback = onResult
    this.retryCount = 0

    try {
      // 1. 清除WebSocket延迟关闭计时器
      if (this.wsCloseTimer) {
        clearTimeout(this.wsCloseTimer)
        this.wsCloseTimer = null
        console.log('[RemoteSTT] Cleared WebSocket close timer')
      }
      
      // 2. 先检查是否需要建立WebSocket连接
      if (!this.ws || this.ws.readyState === WebSocket.CLOSED) {
        console.log('[RemoteSTT] Establishing new WebSocket connection...')
        this.ws = this.createWebSocket()
        await this.connectWebSocket()
      } else {
        console.log('[RemoteSTT] Reusing existing WebSocket connection')
      }

      if (this.ws && this.ws.readyState === WebSocket.OPEN) {
        try {
          this.ws.send(JSON.stringify({ type: 'start_session' }))
        } catch (e) {
          console.error('[RemoteSTT] Failed to send start_session:', e)
        }
      }

      // 3. 初始化音频资源，每次都重新获取麦克风轨道
      // 因为之前的轨道可能已经被停止，无法重新启用
      await this.ensureAudioInitialized()
      
      // 4. 重新创建音频处理链
      // 因为之前的处理器可能已经被停止或断开连接
      if (this.audioContext && this.mediaStream) {
        // 断开并重建处理器
        if (this.processor) {
          this.processor.disconnect()
          if ('port' in this.processor) {
            (this.processor as AudioWorkletNode).port.close()
          }
          this.processor = null
        }
        
        // 断开并重建源
        if (this.source) {
          this.source.disconnect()
          this.source = null
        }
        
        // 重新创建源 - 使用更兼容的方法
        try {
          this.source = this.audioContext.createMediaStreamSource(this.mediaStream)
          console.log('[RemoteSTT] Audio source recreated successfully')
        } catch (e) {
          console.error('[RemoteSTT] Failed to recreate audio source:', e)
          throw e
        }
        
        // 重新创建处理器
        if ('AudioWorklet' in window) {
          await this.initAudioWorklet()
        } else {
          this.initScriptProcessor()
        }
        
        // 连接音频处理链
        if (this.source && this.processor) {
          this.source.connect(this.processor)
          console.log('[RemoteSTT] Audio processing chain reconnected')
        }
      }

      // 5. 启用麦克风轨道
      if (this.mediaStream) {
        this.mediaStream.getAudioTracks().forEach(track => {
          track.enabled = true;
          console.log('[RemoteSTT] Microphone track enabled');
        });
      }

      // 6. 启动心跳机制
      this.startHeartbeat()

      this.isListening = true
      console.log('[RemoteSTT] Started listening')
    } catch (e) {
      console.error('[RemoteSTT] Failed to start:', e)
      this.stop(true) // 出错时强制彻底停止
      throw e
    }
  }

  private createWebSocket(): WebSocket {
    const ws = new WebSocket(this.wsUrl)
    this.connectionStatus = 'connecting'
    
    ws.onopen = () => {
      this.connectionStatus = 'connected'
      console.log('[RemoteSTT] WebSocket connection opened')
    }
    
    ws.onclose = (event) => {
      console.log('[RemoteSTT] WebSocket closed:', event.code, event.reason)
      this.connectionStatus = 'disconnected'
      if (this.isListening && event.code !== 1000) {
        console.log('[RemoteSTT] Connection lost unexpectedly, attempting to reconnect...')
        this.handleReconnect()
      }
    }

    ws.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data)
        console.log('[RemoteSTT] Received WebSocket message:', data)
        
        // 处理不同类型的消息
        if (data.type === 'increment' || data.type === 'correct' || data.type === 'partial_result') {
          // 增量结果或修正结果（非最终）
          if (this.onResultCallback) {
            this.onResultCallback({
              text: data.full_text || data.text || '', // 优先使用 full_text
              isFinal: false,
              timestamps: []
            })
          }
        } else if (data.type === 'final' || data.type === 'final_result') {
          // 最终结果
          if (this.onResultCallback) {
            this.onResultCallback({
              text: data.full_text || data.text || '',
              isFinal: true,
              timestamps: data.timestamps || []
            })
          }
        } else if (data.type === 'init_success') {
          console.log('[RemoteSTT] WebSocket initialized successfully')
        } else if (data.type === 'pong') {
          // 忽略pong消息
        } else {
          console.warn('[RemoteSTT] Unexpected WebSocket message type:', data.type)
        }
      } catch (e) {
        console.error('[RemoteSTT] Error parsing WebSocket message:', e, 'Message:', event.data)
      }
    }

    ws.onerror = (error) => {
      console.error('[RemoteSTT] WebSocket error:', error)
      this.connectionStatus = 'error'
      // 发生错误时，如果正在监听，尝试重连
      if (this.isListening) {
        this.handleReconnect()
      }
    }

    return ws
  }

  private connectWebSocket(): Promise<void> {
    return new Promise((resolve, reject) => {
      if (!this.ws) return reject('WebSocket not initialized')
      
      const timeout = setTimeout(() => {
        reject('WebSocket connection timeout')
      }, 10000) // 10秒超时

      const originalOnOpen = this.ws!.onopen
      const originalOnError = this.ws!.onerror

      this.ws!.onopen = (event) => {
        if (originalOnOpen) originalOnOpen.call(this.ws!, event)
        clearTimeout(timeout)
        resolve()
      }

      this.ws!.onerror = (error) => {
        if (originalOnError) originalOnError.call(this.ws!, error as any)
        clearTimeout(timeout)
        reject(error)
      }
    })
  }


  // 添加静态标志，确保AudioWorklet处理器只被注册一次
  private static audioWorkletRegistered = false;

  private async initAudioWorklet() {
    try {
        // 创建优化的AudioWorklet处理器
        // 关键优化：在Worklet端进行缓冲，减少postMessage频率
        const workletCode = `
          class AudioProcessor extends AudioWorkletProcessor {
            constructor() {
                super();
                // 缓冲区大小 1024 (约64ms @ 16kHz)，进一步减少主线程通信频率和延迟
                this._bufferSize = 1024; 
                this._buffer = new Int16Array(this._bufferSize);
                this._bytesWritten = 0;
                this._isRunning = true;
                
                // 监听停止信号
                this.port.onmessage = (event) => {
                    if (event.data === 'stop') {
                        this._isRunning = false;
                    }
                };
            }

            process(inputs, outputs, parameters) {
              if (!this._isRunning) {
                // 停止处理器
                return false;
              }
              
              const input = inputs[0][0];
              if (input && input.length > 0) {
                for (let i = 0; i < input.length; i++) {
                    const val = input[i];
                    // Float32 -> Int16
                    const s = Math.max(-1, Math.min(1, val));
                    this._buffer[this._bytesWritten++] = s < 0 ? s * 0x8000 : s * 0x7FFF;
                    
                    // 缓冲区满时发送
                    if (this._bytesWritten >= this._bufferSize) {
                        this.port.postMessage(this._buffer.slice(0, this._bufferSize));
                        this._bytesWritten = 0;
                    }
                }
              }
              return this._isRunning;
            }
          }
          
          // 只在处理器未注册时才注册
          if (!AudioWorkletGlobalScope.registeredProcessors) {
            AudioWorkletGlobalScope.registeredProcessors = new Set();
          }
          
          if (!AudioWorkletGlobalScope.registeredProcessors.has('audio-processor')) {
            registerProcessor('audio-processor', AudioProcessor);
            AudioWorkletGlobalScope.registeredProcessors.add('audio-processor');
          }
        `;
        
        // 创建并添加worklet
        const blob = new Blob([workletCode], { type: 'application/javascript' });
        const url = URL.createObjectURL(blob);
        
        // 如果之前已经有worklet URL，先释放
        if (this.workletBlobUrl) {
          URL.revokeObjectURL(this.workletBlobUrl);
        }
        this.workletBlobUrl = url; // 存储URL以便后续释放
        
        await this.audioContext!.audioWorklet.addModule(url);
        // AudioWorkletNode需要2个参数：AudioContext和processor名称
        this.processor = new AudioWorkletNode(this.audioContext!, 'audio-processor');
        
        // 监听worklet消息
        this.processor.port.onmessage = (event) => {
          if (!this.isListening || !this.ws || this.ws.readyState !== WebSocket.OPEN) return;
          
          const pcmData = event.data as Int16Array;
          if (!pcmData || pcmData.length === 0) return;
          
          try {
            if (this.ws && this.ws.readyState === WebSocket.OPEN) {
              this.ws.send(pcmData.buffer);
            }
          } catch (e) {
            console.error('[RemoteSTT] Error sending audio data:', e);
          }
        };
        
        // 只在source和processor都存在时才连接
        if (this.source && this.processor) {
          this.source.connect(this.processor);
          console.log('[RemoteSTT] AudioWorklet initialized successfully');
        }
    } catch (e) {
        console.warn('[RemoteSTT] AudioWorklet initialization failed, falling back to ScriptProcessor:', e);
        this.initScriptProcessor();
    }
  }

  private initScriptProcessor() {
    // 回退到ScriptProcessorNode
    console.warn('[RemoteSTT] Using deprecated ScriptProcessorNode');
    
    // 使用 4096 缓冲区大小
    this.processor = this.audioContext!.createScriptProcessor(4096, 1, 1);

    this.processor!.onaudioprocess = (e) => {
      if (!this.isListening || !this.ws || this.ws.readyState !== WebSocket.OPEN) return;
      
      const inputData = e.inputBuffer.getChannelData(0);
      
      const pcmData = this.float32ToInt16(inputData);
      try {
        this.ws.send(pcmData.buffer);
      } catch (e) {
        console.error('[RemoteSTT] Error sending audio data:', e);
      }
    };

    this.source!.connect(this.processor!);
    this.processor!.connect(this.audioContext!.destination);
  }

  private float32ToInt16(buffer: Float32Array): Int16Array {
    let l = buffer.length;
    const buf = new Int16Array(l);
    while (l--) {
      const val = buffer[l] || 0;
      let s = Math.max(-1, Math.min(1, val));
      buf[l] = s < 0 ? s * 0x8000 : s * 0x7FFF;
    }
    return buf;
  }

  private handleReconnect() {
    if (this.retryCount >= this.maxRetries) {
      console.error('[RemoteSTT] Max retries reached, stopping...');
      this.stop();
      return;
    }

    this.retryCount++;
    const delay = this.retryDelay * Math.pow(2, this.retryCount - 1); // 指数退避
    
    console.log(`[RemoteSTT] Attempting to reconnect (${this.retryCount}/${this.maxRetries}) in ${delay}ms...`);
    
    setTimeout(() => {
      if (this.isListening) {
        this.start(this.onResultCallback!);
      }
    }, delay);
  }

  /**
   * 停止监听
   * @param forceClose 是否强制关闭 WebSocket 和释放硬件资源
   */
  stop(forceClose = false) {
    this.isListening = false;
    
    // 1. 发送会话结束信号给后端，确保后端停止采集和处理
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      try {
        this.ws.send(JSON.stringify({ type: 'end_session' }));
        console.log('[RemoteSTT] Sent end_session signal to backend');
      } catch (e) {
        console.error('[RemoteSTT] Error sending end_session signal:', e);
      }
    }
    
    // 2. 彻底断开音频处理链，防止采集
    if (this.processor) {
      try {
        // 发送停止信号给AudioWorklet处理器
        if ('port' in this.processor) {
          (this.processor as AudioWorkletNode).port.postMessage('stop');
          (this.processor as AudioWorkletNode).port.close();
        }
        
        this.processor.disconnect();
        
        // 对于ScriptProcessorNode，断开到destination的连接
        if (this.audioContext && this.processor instanceof ScriptProcessorNode) {
          try {
            this.processor.disconnect(this.audioContext.destination);
          } catch (e) {}
        }
        
        console.log('[RemoteSTT] Audio processor disconnected');
      } catch (e) {
        console.error('[RemoteSTT] Error disconnecting processor:', e);
      }
    }
    
    if (this.source) {
      try {
        this.source.disconnect();
        console.log('[RemoteSTT] Audio source disconnected');
      } catch (e) {
        console.error('[RemoteSTT] Error disconnecting source:', e);
      }
      this.source = null;
    }

    // 3. 停止麦克风轨道，彻底停止产生音频数据，消除浏览器红点
    if (this.mediaStream) {
      this.mediaStream.getTracks().forEach(track => {
        track.stop();
        track.enabled = false; // 显式禁用
        console.log('[RemoteSTT] Microphone track stopped and disabled');
      });
      this.mediaStream = null;
    }

    if (forceClose) {
      this.closeWebSocket();
      console.log('[RemoteSTT] WebSocket closed (forced)');
    } else {
      // 设置WebSocket延迟关闭计时器
      this.stopHeartbeat();
      // 清除旧的计时器
      if (this.wsCloseTimer) clearTimeout(this.wsCloseTimer);
      
      this.wsCloseTimer = setTimeout(() => {
        this.closeWebSocket();
        console.log('[RemoteSTT] WebSocket closed due to inactivity');
      }, this.wsCloseDelay);
      console.log('[RemoteSTT] Stopped (WebSocket will close in 30s if inactive)');
    }
  }

  private closeWebSocket() {
    this.stopHeartbeat();
    if (this.ws) {
      // 移除监听防止触发重连
      this.ws.onclose = null;
      this.ws.onerror = null;
      this.ws.close();
      this.ws = null;
      this.connectionStatus = 'disconnected';
    }
  }

  /**
   * 彻底释放所有资源
   */
  destroy() {
    this.stop(true);
    
    // 清除WebSocket延迟关闭计时器
    if (this.wsCloseTimer) {
      clearTimeout(this.wsCloseTimer);
      this.wsCloseTimer = null;
    }
    
    if (this.mediaStream) {
      this.mediaStream.getTracks().forEach(track => track.stop());
      this.mediaStream = null;
    }

    if (this.processor) {
      this.processor.disconnect();
      if ('port' in this.processor) {
        (this.processor as AudioWorkletNode).port.close();
      }
      this.processor = null;
    }

    if (this.source) {
      this.source.disconnect();
      this.source = null;
    }

    if (this.audioContext) {
      this.audioContext.close();
      this.audioContext = null;
    }
    
    // 释放AudioWorklet Blob URL，避免内存泄漏
    if (this.workletBlobUrl) {
      URL.revokeObjectURL(this.workletBlobUrl);
      this.workletBlobUrl = null;
      console.log('[RemoteSTT] Released AudioWorklet Blob URL');
    }
    
    console.log('[RemoteSTT] Destroyed');
  }


}

export const remoteStt = new RemoteSttEngine()
