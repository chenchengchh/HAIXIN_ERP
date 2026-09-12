<template>
  <div class="session-control">
    <el-card class="control-card">
      <template #header>
        <div class="card-header">
          <span>会话管理</span>
        </div>
      </template>
      
      <div class="control-content">
        <div class="session-info">
          <h3>当前会话</h3>
          <div class="info-grid">
            <div class="info-item">
              <span class="label">会话ID：</span>
              <span class="value">{{ sessionId }}</span>
            </div>
            <div class="info-item">
              <span class="label">开始时间：</span>
              <span class="value">{{ startTime }}</span>
            </div>
            <div class="info-item">
              <span class="label">持续时间：</span>
              <span class="value">{{ duration }}</span>
            </div>
            <div class="info-item">
              <span class="label">状态：</span>
              <span class="value" :class="sessionStatus.toLowerCase()">
                {{ sessionStatusText[sessionStatus] }}
              </span>
            </div>
          </div>
        </div>
        
        <div class="control-panel">
          <h3>会话控制</h3>
          <div class="button-group">
            <el-button 
              type="primary" 
              :icon="VideoPlay" 
              @click="startSession"
              :disabled="sessionStatus === 'active'"
              size="large"
              class="control-btn"
            >
              开始会话
            </el-button>
            
            <el-button 
              type="warning" 
              :icon="VideoPause" 
              @click="pauseSession"
              :disabled="sessionStatus !== 'active'"
              size="large"
              class="control-btn"
            >
              暂停会话
            </el-button>
            
            <el-button 
              type="success" 
              :icon="Check" 
              @click="endSession"
              :disabled="sessionStatus === 'idle'"
              size="large"
              class="control-btn"
            >
              结束会话
            </el-button>
            
            <el-button 
              type="danger" 
              :icon="Refresh" 
              @click="resetSession"
              size="large"
              class="control-btn"
            >
              重置会话
            </el-button>
          </div>
        </div>
        
        <div class="settings-panel">
          <h3>配置设置</h3>
          <el-collapse v-model="activeSettings">
            <el-collapse-item title="音频设置" name="audio">
              <el-form label-position="top" size="small">
                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-form-item label="采样率">
                      <el-select v-model="audioSettings.sampleRate" placeholder="选择采样率">
                        <el-option label="16000 Hz" value="16000" />
                        <el-option label="8000 Hz" value="8000" />
                      </el-select>
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="声道数">
                      <el-select v-model="audioSettings.channelCount" placeholder="选择声道数">
                        <el-option label="单声道" value="1" />
                        <el-option label="立体声" value="2" />
                      </el-select>
                    </el-form-item>
                  </el-col>
                </el-row>
                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-form-item label="回声消除">
                      <el-switch v-model="audioSettings.echoCancellation" />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="噪音抑制">
                      <el-switch v-model="audioSettings.noiseSuppression" />
                    </el-form-item>
                  </el-col>
                </el-row>
              </el-form>
            </el-collapse-item>
            
            <el-collapse-item title="识别设置" name="recognition">
              <el-form label-position="top" size="small">
                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-form-item label="增量识别">
                      <el-switch v-model="recognitionSettings.incremental" />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="时间戳">
                      <el-switch v-model="recognitionSettings.timestamp" />
                    </el-form-item>
                  </el-col>
                </el-row>
                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-form-item label="VAD检测">
                      <el-switch v-model="recognitionSettings.vad" />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="静音时长(ms)">
                      <el-input-number 
                        v-model="recognitionSettings.silenceDuration" 
                        :min="100" 
                        :max="3000" 
                        :step="100"
                      />
                    </el-form-item>
                  </el-col>
                </el-row>
              </el-form>
            </el-collapse-item>
          </el-collapse>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, computed, watch } from 'vue'
import { VideoPlay, VideoPause, Check, Refresh } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

// 定义事件
const emit = defineEmits<{
  'session-start': []
  'session-pause': []
  'session-end': []
  'session-reset': []
  'settings-change': [settings: { audio: any; recognition: any }]
}>()

// 状态变量
const sessionId = ref('')
const startTime = ref('')
const duration = ref('00:00:00')
const sessionStatus = ref('idle') // idle, active, paused, ended
const activeSettings = ref(['audio'])
let timer: number | null = null
let startTimeMs = 0

// 会话状态文本映射
const sessionStatusText: { [key: string]: string } = {
  idle: '空闲',
  active: '活跃',
  paused: '暂停',
  ended: '已结束'
}

// 音频设置
const audioSettings = reactive({
  sampleRate: '16000',
  channelCount: '1',
  echoCancellation: true,
  noiseSuppression: true
})

// 识别设置
const recognitionSettings = reactive({
  incremental: true,
  timestamp: true,
  vad: true,
  silenceDuration: 800
})

// 计算属性：格式化持续时间
const formatDuration = (ms: number) => {
  const seconds = Math.floor(ms / 1000)
  const minutes = Math.floor(seconds / 60)
  const hours = Math.floor(minutes / 60)
  
  return `${hours.toString().padStart(2, '0')}:${(minutes % 60).toString().padStart(2, '0')}:${(seconds % 60).toString().padStart(2, '0')}`
}

// 更新持续时间
const updateDuration = () => {
  if (sessionStatus.value === 'active') {
    const currentTime = Date.now()
    const elapsed = currentTime - startTimeMs
    duration.value = formatDuration(elapsed)
  }
}

// 开始会话
const startSession = () => {
  sessionId.value = `session_${Date.now()}`
  startTimeMs = Date.now()
  startTime.value = new Date().toLocaleString()
  sessionStatus.value = 'active'
  
  // 启动定时器
  timer = window.setInterval(updateDuration, 1000)
  
  // 通知父组件
  emit('session-start')
  
  ElMessage.success('会话已开始')
}

// 暂停会话
const pauseSession = () => {
  sessionStatus.value = 'paused'
  
  // 停止定时器
  if (timer) {
    clearInterval(timer)
    timer = null
  }
  
  // 通知父组件
  emit('session-pause')
  
  ElMessage.info('会话已暂停')
}

// 结束会话
const endSession = () => {
  sessionStatus.value = 'ended'
  
  // 停止定时器
  if (timer) {
    clearInterval(timer)
    timer = null
  }
  
  // 通知父组件
  emit('session-end')
  
  ElMessage.success('会话已结束')
}

// 重置会话
const resetSession = () => {
  // 停止定时器
  if (timer) {
    clearInterval(timer)
    timer = null
  }
  
  // 重置状态
  sessionId.value = ''
  startTime.value = ''
  duration.value = '00:00:00'
  sessionStatus.value = 'idle'
  startTimeMs = 0
  
  // 通知父组件
  emit('session-reset')
  
  ElMessage.info('会话已重置')
}

// 监听设置变化
const watchSettings = () => {
  emit('settings-change', {
    audio: { ...audioSettings },
    recognition: { ...recognitionSettings }
  })
}

// 监听音频设置变化
watch(
  audioSettings,
  () => {
    watchSettings()
  },
  { deep: true }
)

// 监听识别设置变化
watch(
  recognitionSettings,
  () => {
    watchSettings()
  },
  { deep: true }
)

// 生命周期钩子
onMounted(() => {
  // 初始化会话ID
  sessionId.value = `session_${Date.now()}`
})

onUnmounted(() => {
  // 清理定时器
  if (timer) {
    clearInterval(timer)
    timer = null
  }
})
</script>

<style scoped lang="scss">
.session-control {
  width: 100%;
}

.control-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.control-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.session-info {
  background-color: #f5f7fa;
  padding: 20px;
  border-radius: 8px;
}

.session-info h3 {
  margin: 0 0 15px 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 15px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.info-item .label {
  font-size: 13px;
  color: #606266;
  font-weight: 500;
}

.info-item .value {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.info-item .value.idle {
  color: #909399;
}

.info-item .value.active {
  color: #409eff;
}

.info-item .value.paused {
  color: #e6a23c;
}

.info-item .value.ended {
  color: #67c23a;
}

.control-panel {
  background-color: #f5f7fa;
  padding: 20px;
  border-radius: 8px;
}

.control-panel h3 {
  margin: 0 0 15px 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.button-group {
  display: flex;
  gap: 15px;
  flex-wrap: wrap;
}

.control-btn {
  flex: 1;
  min-width: 120px;
}

.settings-panel {
  background-color: #f5f7fa;
  padding: 20px;
  border-radius: 8px;
}

.settings-panel h3 {
  margin: 0 0 15px 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

// 响应式调整
@media (max-width: 768px) {
  .info-grid {
    grid-template-columns: 1fr;
  }
  
  .button-group {
    flex-direction: column;
  }
  
  .control-btn {
    width: 100%;
  }
}
</style>