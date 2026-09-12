<template>
  <div v-if="error" class="error-container" :class="{ 'full-screen': fullScreen }">
    <div class="error-content">
      <el-alert
        :title="error.title || '发生错误'"
        :description="error.message"
        :type="error.type || 'error'"
        :closable="true"
        @close="onClose"
        show-icon
      >
        <template #default>
          <div v-if="error.details" class="error-details">
            <el-collapse>
              <el-collapse-item title="错误详情">
                <pre>{{ error.details }}</pre>
              </el-collapse-item>
            </el-collapse>
          </div>
          <div v-if="error.action" class="error-action">
            <el-button type="primary" size="small" @click="error.action">{{ error.actionText || '重试' }}</el-button>
          </div>
        </template>
      </el-alert>
    </div>
    <div v-if="fullScreen" class="error-overlay" @click="onClose"></div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';

interface ErrorProps {
  error: {
    title?: string;
    message: string;
    type?: 'success' | 'warning' | 'info' | 'error';
    details?: string;
    action?: () => void;
    actionText?: string;
  } | null;
  fullScreen?: boolean;
}

interface ErrorEmits {
  (e: 'close'): void;
}

const props = withDefaults(defineProps<ErrorProps>(), {
  error: null,
  fullScreen: false
});

const emit = defineEmits<ErrorEmits>();

const onClose = () => {
  emit('close');
};
</script>

<style scoped>
.error-container {
  position: relative;
  width: 100%;
  z-index: 1000;
}

.error-container.full-screen {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.error-content {
  max-width: 600px;
  width: 100%;
  margin: 16px;
  z-index: 1001;
}

.error-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  z-index: 1000;
}

.error-details {
  margin-top: 16px;
}

.error-details pre {
  background-color: #f5f7fa;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  padding: 12px;
  overflow-x: auto;
  font-size: 12px;
  line-height: 1.5;
  color: #606266;
}

.error-action {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
