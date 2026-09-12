<template>
  <div v-if="loading" class="loading-container">
    <div class="loading-spinner">
      <el-icon :size="size === 'large' ? 40 : size === 'default' ? 32 : 24" class="is-loading">
        <Loading />
      </el-icon>
      <div v-if="message" class="loading-message">{{ message }}</div>
    </div>
    <div v-if="fullScreen" class="loading-overlay"></div>
  </div>
</template>

<script setup lang="ts">
import { Loading } from '@element-plus/icons-vue'

// 组件属性定义
const props = defineProps({
  /**
   * 加载状态
   */
  loading: {
    type: Boolean,
    default: false
  },
  /**
   * 是否全屏显示
   */
  fullScreen: {
    type: Boolean,
    default: false
  },
  /**
   * 加载提示信息
   */
  message: {
    type: String,
    default: ''
  },
  /**
   * 加载图标大小
   */
  size: {
    type: String,
    default: 'default'
  },
  /**
   * 加载图标类型
   */
  type: {
    type: String,
    default: 'default'
  }
})
</script>

<style scoped>
.loading-container {
  position: relative;
  display: inline-block;
}

.loading-spinner {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.loading-message {
  margin-top: 12px;
  font-size: 14px;
  color: #606266;
}

.loading-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(255, 255, 255, 0.7);
  z-index: 999;
}

/* 全屏加载时居中显示 */
:deep(.fullscreen) {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
}
</style>
