<template>
  <el-drawer
    v-model="visible"
    title="实时执行日志"
    direction="btt"
    size="50%"
    destroy-on-close
    @close="handleClose"
  >
    <div class="log-wrapper">
      <div v-if="taskName" class="task-info-bar">
        当前任务: <span class="highlight">{{ taskName }}</span>
      </div>
      <div class="log-container" ref="logContainer">
        <div v-for="(log, index) in logs" :key="index" class="log-item" :class="log.type">
          <span class="time">[{{ log.time }}]</span>
          <span class="msg">{{ log.message }}</span>
        </div>
        <div v-if="logs.length === 0" class="empty-tip">暂无日志数据</div>
      </div>
    </div>
  </el-drawer>
</template>

<script setup lang="ts">
import { ref, watch, nextTick, computed } from 'vue'
import type { LogItem } from '../types'

const props = defineProps<{
  modelValue: boolean
  logs: LogItem[]
  taskName?: string
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', val: boolean): void
}>()

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const logContainer = ref<HTMLElement | null>(null)

watch(
  () => props.logs.length,
  () => {
    if (visible.value) {
      nextTick(() => {
        if (logContainer.value) {
          logContainer.value.scrollTop = logContainer.value.scrollHeight
        }
      })
    }
  }
)
// Also scroll to bottom when drawer opens
watch(visible, (val) => {
  if (val) {
    nextTick(() => {
      if (logContainer.value) {
        logContainer.value.scrollTop = logContainer.value.scrollHeight
      }
    })
  }
})

const handleClose = () => {
  visible.value = false
}
</script>

<style scoped lang="scss">
.log-wrapper {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.task-info-bar {
  padding: 10px 15px;
  background: #f4f4f5;
  border-bottom: 1px solid #e9e9eb;
  font-size: 13px;
  color: #303133;
  font-weight: 500;
  
  .highlight {
    font-weight: 600;
    color: #1d2129;
    margin-left: 5px;
  }
}

.log-container {
  flex: 1;
  overflow-y: auto;
  padding: 15px;
  background: #f5f7fa;
  font-family: 'Consolas', monospace;
  font-size: 12px;
  
  .log-item {
    margin-bottom: 4px;
    line-height: 1.4;
    
    .time { color: #909399; margin-right: 10px; }
    
    &.info .msg { color: #303133; }
    &.success .msg { color: #67c23a; }
    &.warning .msg { color: #e6a23c; }
    &.error .msg { color: #f56c6c; }
  }
  
  .empty-tip {
    color: #909399;
    text-align: center;
    margin-top: 50px;
  }
}
</style>
