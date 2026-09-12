<template>
  <div class="audio-collector">
    <el-card class="collector-card">
      <template #header>
        <div class="card-header">
          <span>音频采集控制</span>
        </div>
      </template>
      
      <div class="collector-content">
        <div class="control-section">
          <el-button 
            type="primary" 
            :icon="isCollecting ? VideoPause : VideoCamera" 
            @click="toggleCollect"
            :disabled="!isConnected"
            size="large"
          >
            {{ isCollecting ? '停止采集' : '开始采集' }}
          </el-button>
          
          <el-tooltip v-if="!isConnected" content="未连接到语音服务">
            <el-button type="danger" size="large" :icon="CircleClose">
              连接失败
            </el-button>
          </el-tooltip>
          
          <el-tooltip v-else content="已连接到语音服务">
            <el-button type="success" size="large" :icon="CircleCheck">
              连接成功
            </el-button>
          </el-tooltip>
        </div>
        
        <div class="status-section">
          <div class="status-item">
            <span class="label">采集状态：</span>
            <span class="value" :class="isCollecting ? 'collecting' : 'idle'">
              {{ isCollecting ? '正在采集' : '空闲' }}
            </span>
          </div>
          
          <div class="status-item">
            <span class="label">连接状态：</span>
            <span class="value" :class="isConnected ? 'connected' : 'disconnected'">
              {{ isConnected ? '已连接' : '未连接' }}
            </span>
          </div>
        </div>
        
        <div class="visualization-section">
          <div class="waveform-container">
            <canvas ref="waveformCanvas" class="waveform"></canvas>
          </div>
          <div class="volume-meter">
            <div class="volume-bar" :style="{ height: `${volumeLevel}%` }"></div>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { VideoCamera, VideoPause, CircleCheck, CircleClose } from '@element-plus/icons-vue'
import { useWebSocket } from '@/utils/websocket'
import { ElMessage } from 'element-plus'

// 定义组件属性
const props = defineProps<{
  wsUrl: string
}>()

// 定义事件
const emit = defineEmits<{
  'result-update': [result: any]
  'status-change': [status: { isCollecting: boolean; isConnected: boolean }]
}>()

// 状态变量
const isCollecting = ref(false)
const isConnected = ref(false)
const volumeLevel = ref(0)
const waveformCanvas = ref<HTMLCanvasElement | null>(null)
const audioContext = ref<AudioContext | null>(null)
const mediaStream = ref<MediaStream | null>(null)
const analyser = ref<AnalyserNode | null>(null)
const dataArray = ref<Uint8Array | null>(null)
const wsInstance = ref<any>(null)
const audioBuffer = ref<Float32Array>(new Float32Array(0))

// WebSocket配置
const wsEvents = {
  onOpen: () => {
    isConnected.value = true
    emit('status-change', { isCollecting: isCollecting.value, isConnected: isConnected.value })
  },
  onClose: () => {
    isConnected.value = false
    emit('status-change', { isCollecting: isCollecting.value, isConnected: isConnected.value })
  },
  onMessage: (event: MessageEvent) => {
    try {
      const result = JSON.parse(event.data)
      emit('result-update', result)
    } catch (error) {
      console.error('Failed to parse WebSocket message:', error)
    }
  },
  onError: () => {
    isConnected.value = false
    emit('status-change', { isCollecting: isCollecting.value, isConnected: isConnected.value })
  }
}

// 初始化WebSocket连接
const initWebSocket = () => {
  wsInstance.value = useWebSocket(props.wsUrl, wsEvents)
}

// 初始化音频上下文和麦克风
const initAudio = async () => {
  try {
    if (!audioContext.value) {
      audioContext.value = new (window.AudioContext || (window as any).webkitAudioContext)({ sampleRate: 16000 })
    }
    
    // 请求麦克风权限
    mediaStream.value = await navigator.mediaDevices.getUserMedia({ 
      audio: { 
        sampleRate: 16000, 
        channelCount: 1, 
        echoCancellation: true,
        noiseSuppression: true
      } 
    })
    
    // 创建分析器节点，用于音量可视化
    analyser.value = audioContext.value.createAnalyser()
    analyser.value.fftSize = 256
    const bufferLength = analyser.value.frequencyBinCount
    dataArray.value = new Uint8Array(bufferLength)
    
    // 创建音频源和处理器
    const source = audioContext.value.createMediaStreamSource(mediaStream.value)
    const processor = audioContext.value.createScriptProcessor(1024, 1, 1)
    
    // 连接音频节点
    source.connect(analyser.value)
    analyser.value.connect(processor)
    processor.connect(audioContext.value.destination)
    
    // 处理音频数据
    processor.onaudioprocess = (event) => {
      if (!isCollecting.value) return
      
      // 获取音频数据
      const inputData = event.inputBuffer.getChannelData(0)
      
      // 更新音量显示
      updateVolume(inputData)
      
      // 更新波形显示
      updateWaveform()
      
      // 添加到缓冲区
      audioBuffer.value = Float32Array.from([...audioBuffer.value, ...inputData])
      
      // 当缓冲区达到200ms（3200采样点）时，发送数据
      if (audioBuffer.value.length >= 3200) {
        const chunk = audioBuffer.value.slice(0, 3200)
        audioBuffer.value = audioBuffer.value.slice(3200)
        sendAudioData(chunk)
      }
    }
    
    return true
  } catch (error) {
    console.error('Failed to initialize audio:', error)
    return false
  }
}

// 更新音量显示
const updateVolume = (data: Float32Array) => {
  const rms = Math.sqrt(data.reduce((sum, val) => sum + val * val, 0) / data.length)
  volumeLevel.value = Math.min(100, Math.max(0, rms * 500))
}

// 更新波形显示
const updateWaveform = () => {
  if (!analyser.value || !dataArray.value || !waveformCanvas.value) return
  
  // 使用更强制的类型断言解决类型不匹配问题
  const typedArray = dataArray.value as any
  analyser.value.getByteTimeDomainData(typedArray)
  const canvas = waveformCanvas.value
  const ctx = canvas.getContext('2d')
  if (!ctx) return
  
  const width = canvas.width
  const height = canvas.height
  
  ctx.fillStyle = 'rgba(255, 255, 255, 0.1)'
  ctx.fillRect(0, 0, width, height)
  
  ctx.lineWidth = 2
  ctx.strokeStyle = '#409EFF'
  ctx.beginPath()
  
  const sliceWidth = width / typedArray.length
  let x = 0
  
  for (let i = 0; i < typedArray.length; i++) {
    // 使用类型断言确保typedArray[i]不会是undefined
    const v = typedArray[i] as number / 128.0
    const y = v * height / 2
    
    if (i === 0) {
      ctx.moveTo(x, y)
    } else {
      ctx.lineTo(x, y)
    }
    
    x += sliceWidth
  }
  
  ctx.lineTo(canvas.width, canvas.height / 2)
  ctx.stroke()
}

// 将Float32Array转换为16位PCM格式
const convertTo16BitPCM = (input: Float32Array) => {
  const buffer = new ArrayBuffer(input.length * 2)
  const view = new DataView(buffer)
  input.forEach((sample, i) => {
    const value = Math.max(-1, Math.min(1, sample))
    view.setInt16(i * 2, value * 32767, true)
  })
  return buffer
}

// 发送音频数据
const sendAudioData = (audioData: Float32Array) => {
  if (wsInstance.value && wsInstance.value.readyState === WebSocket.OPEN) {
    const pcmData = convertTo16BitPCM(audioData)
    wsInstance.value.send(pcmData)
  }
}

// 开始/停止采集
const toggleCollect = async () => {
  isCollecting.value = !isCollecting.value
  
  if (isCollecting.value) {
    // 开始采集
    const success = await initAudio()
    if (!success) {
      isCollecting.value = false
      ElMessage.error('音频初始化失败，请检查麦克风权限')
    }
  } else {
    // 停止采集
    if (mediaStream.value) {
      mediaStream.value.getTracks().forEach(track => track.stop())
      mediaStream.value = null
    }
    
    // 发送会话结束信号
    if (wsInstance.value && wsInstance.value.readyState === WebSocket.OPEN) {
      wsInstance.value.send(JSON.stringify({ type: 'end_session' }))
    }
  }
  
  // 通知父组件状态变化
  emit('status-change', { isCollecting: isCollecting.value, isConnected: isConnected.value })
}

// 初始化Canvas尺寸
const initCanvas = () => {
  if (!waveformCanvas.value) return
  
  const canvas = waveformCanvas.value
  const container = canvas.parentElement
  if (container) {
    canvas.width = container.clientWidth
    canvas.height = 100
  }
}

// 生命周期钩子
onMounted(() => {
  initWebSocket()
  initCanvas()
  window.addEventListener('resize', initCanvas)
})

onUnmounted(() => {
  if (isCollecting.value) {
    toggleCollect()
  }
  
  if (audioContext.value) {
    audioContext.value.close()
    audioContext.value = null
  }
  
  if (wsInstance.value) {
    wsInstance.value.close()
  }
  
  window.removeEventListener('resize', initCanvas)
})

// 监听连接状态变化
watch(isConnected, (newVal) => {
  if (newVal) {
    ElMessage.success('已连接到语音服务')
  } else {
    ElMessage.error('与语音服务连接断开')
  }
})
</script>

<style scoped lang="scss">
.audio-collector {
  width: 100%;
}

.collector-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.collector-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.control-section {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

.status-section {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 8px;
}

.status-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.status-item .label {
  font-weight: 500;
  color: #606266;
}

.status-item .value {
  font-weight: 600;
  padding: 4px 8px;
  border-radius: 4px;
}

.status-item .value.idle {
  color: #909399;
  background-color: #ecf5ff;
}

.status-item .value.collecting {
  color: #409eff;
  background-color: #ecf5ff;
  animation: pulse 1s infinite;
}

.status-item .value.connected {
  color: #67c23a;
  background-color: #f0f9eb;
}

.status-item .value.disconnected {
  color: #f56c6c;
  background-color: #fef0f0;
}

@keyframes pulse {
  0% {
    opacity: 1;
  }
  50% {
    opacity: 0.7;
  }
  100% {
    opacity: 1;
  }
}

.visualization-section {
  display: flex;
  gap: 20px;
  align-items: center;
}

.waveform-container {
  flex: 1;
  height: 100px;
  background-color: #f5f7fa;
  border-radius: 8px;
  overflow: hidden;
}

.waveform {
  width: 100%;
  height: 100%;
}

.volume-meter {
  width: 20px;
  height: 100px;
  background-color: #f5f7fa;
  border-radius: 10px;
  overflow: hidden;
  position: relative;
}

.volume-bar {
  width: 100%;
  background: linear-gradient(to top, #409eff, #67c23a);
  position: absolute;
  bottom: 0;
  transition: height 0.1s ease;
}
</style>