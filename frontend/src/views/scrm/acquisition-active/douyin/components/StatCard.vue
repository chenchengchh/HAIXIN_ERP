<template>
  <el-card shadow="hover" class="stat-card">
    <div class="stat-content">
      <div class="stat-icon" :style="{ backgroundColor: bgColor }">
        <el-icon :color="iconColor" :size="24">
          <component :is="icon" />
        </el-icon>
      </div>
      <div class="stat-info">
        <div class="stat-label">{{ label }}</div>
        <div class="stat-value">{{ value }}</div>
      </div>
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  label: string
  value: string | number
  icon: any
  type?: 'primary' | 'success' | 'warning' | 'danger'
}>()

const colorMap = {
  primary: { bg: '#ecf5ff', icon: '#409eff' },
  success: { bg: '#f0f9eb', icon: '#67c23a' },
  warning: { bg: '#fdf6ec', icon: '#e6a23c' },
  danger: { bg: '#fef0f0', icon: '#f56c6c' }
}

const bgColor = computed(() => colorMap[props.type || 'primary'].bg)
const iconColor = computed(() => colorMap[props.type || 'primary'].icon)
</script>

<style scoped lang="scss">
.stat-card {
  height: 100%;
  :deep(.el-card__body) {
    padding: 20px;
  }
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 15px;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-info {
  flex: 1;
  .stat-label {
    font-size: 14px;
    color: #909399;
    margin-bottom: 4px;
  }
  .stat-value {
    font-size: 24px;
    font-weight: 600;
    color: #303133;
  }
}
</style>
