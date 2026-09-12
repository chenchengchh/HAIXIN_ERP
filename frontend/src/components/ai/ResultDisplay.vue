<template>
  <div class="result-display">
    <el-card class="result-card">
      <template #header>
        <div class="card-header">
          <span>语音识别结果</span>
          <div class="header-actions">
            <el-button 
              type="primary" 
              :icon="DocumentCopy" 
              @click="copyResult"
              :disabled="!fullText"
            >
              复制结果
            </el-button>
            <el-button 
              type="success" 
              :icon="Download" 
              @click="downloadResult"
              :disabled="!fullText"
            >
              下载结果
            </el-button>
            <el-button 
              type="warning" 
              :icon="Delete" 
              @click="clearResult"
              :disabled="!fullText"
            >
              清空结果
            </el-button>
          </div>
        </div>
      </template>
      
      <div class="result-content">
        <div v-if="!fullText" class="empty-result">
          <el-empty description="暂无识别结果" />
        </div>
        
        <div v-else class="text-result">
          <div class="result-header">
            <div class="result-stats">
              <span class="stat-item">
                <el-icon><Timer /></el-icon>
                <span class="label">响应时间：</span>
                <span class="value">{{ responseTime }}ms</span>
              </span>
              <span class="stat-item">
                <el-icon><Document /></el-icon>
                <span class="label">字数：</span>
                <span class="value">{{ fullText.length }}</span>
              </span>
              <span class="stat-item">
                <el-icon><Check /></el-icon>
                <span class="label">结果类型：</span>
                <span class="value" :class="resultType.toLowerCase()">
                  {{ resultTypeText[resultType] }}
                </span>
              </span>
            </div>
          </div>
          
          <div class="result-body">
            <div class="full-text" ref="resultRef">
              {{ fullText }}
            </div>
          </div>
          
          <div class="result-footer" v-if="newText">
            <div class="new-text-indicator">
              <el-tag type="primary" size="small">新增文本</el-tag>
              <span class="new-text">{{ newText }}</span>
            </div>
          </div>
        </div>
        
        <div class="timeline-section" v-if="timestamps.length > 0">
          <h3>时间戳信息</h3>
          <el-timeline>
            <el-timeline-item 
              v-for="(item, index) in timestamps" 
              :key="index"
              :timestamp="formatTime(item[1])"
              placement="top"
            >
              {{ item[0] }}
            </el-timeline-item>
          </el-timeline>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import { DocumentCopy, Download, Delete, Timer, Document, Check } from '@element-plus/icons-vue'
import { ElMessage, ElEmpty, ElTag, ElTimeline, ElTimelineItem } from 'element-plus'

// 定义组件属性
const props = defineProps<{
  result: {
    type: string
    new_text?: string
    full_text: string
    timestamps?: Array<[string, number]>
    timestamp?: number
  } | null
}>()

// 状态变量
const resultRef = ref<HTMLElement | null>(null)
const fullText = ref('')
const newText = ref('')
const resultType = ref('idle')
const responseTime = ref(0)
const timestamps = ref<Array<[string, number]>>([])
let lastUpdateTime = 0

// 结果类型文本映射
const resultTypeText: { [key: string]: string } = {
  idle: '空闲',
  increment: '增量结果',
  correct: '修正结果',
  final: '最终结果'
}

// 计算属性：根据结果类型返回对应的样式类
const resultTypeClass = computed(() => {
  return resultType.value.toLowerCase()
})

// 监听结果变化
watch(() => props.result, (newResult) => {
  if (!newResult) return
  
  // 计算响应时间
  const currentTime = Date.now()
  if (lastUpdateTime > 0) {
    responseTime.value = currentTime - lastUpdateTime
  }
  lastUpdateTime = currentTime
  
  // 更新结果类型
  resultType.value = newResult.type
  
  // 更新完整文本
  fullText.value = newResult.full_text
  
  // 更新新增文本
  newText.value = newResult.new_text || ''
  
  // 更新时间戳
  if (newResult.timestamps) {
    timestamps.value = newResult.timestamps
  }
  
  // 滚动到底部
  scrollToBottom()
}, { deep: true, immediate: true })

// 滚动到底部
const scrollToBottom = () => {
  if (resultRef.value) {
    const container = resultRef.value.parentElement
    if (container) {
      container.scrollTop = container.scrollHeight
    }
  }
}

// 复制结果
const copyResult = () => {
  if (!fullText.value) return
  
  navigator.clipboard.writeText(fullText.value).then(() => {
    ElMessage.success('结果已复制到剪贴板')
  }).catch(() => {
    ElMessage.error('复制失败，请手动复制')
  })
}

// 下载结果
const downloadResult = () => {
  if (!fullText.value) return
  
  const blob = new Blob([fullText.value], { type: 'text/plain;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `语音识别结果_${new Date().toISOString().slice(0, 19).replace(/:/g, '-')}.txt`
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  URL.revokeObjectURL(url)
  
  ElMessage.success('结果已下载')
}

// 清空结果
const clearResult = () => {
  fullText.value = ''
  newText.value = ''
  resultType.value = 'idle'
  responseTime.value = 0
  timestamps.value = []
  lastUpdateTime = 0
  
  ElMessage.info('结果已清空')
}

// 格式化时间戳
const formatTime = (time: number) => {
  const seconds = Math.floor(time / 1000)
  const milliseconds = time % 1000
  return `${seconds}.${milliseconds.toString().padStart(3, '0')}s`
}
</script>

<style scoped lang="scss">
.result-display {
  width: 100%;
}

.result-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.result-content {
  min-height: 200px;
}

.empty-result {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 200px;
  background-color: #fafafa;
  border-radius: 8px;
}

.text-result {
  background-color: #fafafa;
  border-radius: 8px;
  padding: 20px;
}

.result-header {
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
}

.result-stats {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #606266;
}

.stat-item .label {
  font-weight: 500;
}

.stat-item .value {
  font-weight: 600;
}

.stat-item .value.increment {
  color: #409eff;
}

.stat-item .value.correct {
  color: #f56c6c;
}

.stat-item .value.final {
  color: #67c23a;
}

.result-body {
  margin-bottom: 15px;
  max-height: 400px;
  overflow-y: auto;
  padding: 10px;
  background-color: white;
  border-radius: 6px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.full-text {
  font-size: 16px;
  line-height: 1.8;
  color: #303133;
  word-break: break-all;
  white-space: pre-wrap;
}

.result-footer {
  padding-top: 10px;
  border-top: 1px solid #ebeef5;
}

.new-text-indicator {
  display: flex;
  align-items: center;
  gap: 10px;
  animation: fadeIn 0.5s ease;
}

.new-text {
  font-size: 14px;
  color: #409eff;
  font-weight: 600;
  background-color: rgba(64, 158, 255, 0.1);
  padding: 4px 8px;
  border-radius: 4px;
  animation: pulse 1s infinite;
}

.timeline-section {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

.timeline-section h3 {
  margin: 0 0 15px 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
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

// 自定义滚动条
.result-body::-webkit-scrollbar {
  width: 8px;
}

.result-body::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}

.result-body::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 4px;
}

.result-body::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style>