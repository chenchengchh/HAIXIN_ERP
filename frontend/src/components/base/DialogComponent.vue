<template>
  <el-dialog
    v-model="visible"
    :title="title"
    :width="width"
    :fullscreen="fullscreen"
    :modal="modal"
    :modal-append-to-body="modalAppendToBody"
    :append-to-body="appendToBody"
    :lock-scroll="lockScroll"
    :custom-class="customClass"
    :close-on-click-modal="closeOnClickModal"
    :close-on-press-escape="closeOnPressEscape"
    :show-close="effectiveShowClose"
    :center="center"
    :draggable="draggable"
    @close="handleClose"
    @open="handleOpen"
    @opened="handleOpened"
    @closed="handleClosed"
  >
    <!-- 对话框内容 -->
    <div class="dialog-content" v-loading="loading">
      <!-- 成功类型 -->
      <div v-if="type === 'success'" class="dialog-type success">
        <el-icon class="success-icon"><CircleCheckFilled /></el-icon>
        <div class="dialog-message">
          {{ message }}
        </div>
      </div>
      
      <!-- 警告类型 -->
      <div v-else-if="type === 'warning'" class="dialog-type warning">
        <el-icon class="warning-icon"><WarningFilled /></el-icon>
        <div class="dialog-message">
          {{ message }}
        </div>
      </div>
      
      <!-- 错误类型 -->
      <div v-else-if="type === 'error'" class="dialog-type error">
        <el-icon class="error-icon"><CircleCloseFilled /></el-icon>
        <div class="dialog-message">
          {{ message }}
        </div>
      </div>
      
      <!-- 信息类型 -->
      <div v-else-if="type === 'info'" class="dialog-type info">
        <el-icon class="info-icon"><InfoFilled /></el-icon>
        <div class="dialog-message">
          {{ message }}
        </div>
      </div>
      
      <!-- 自定义内容 -->
      <div v-else>
        <slot></slot>
      </div>
    </div>
    
    <!-- 对话框底部 -->
    <template #footer>
      <!-- 自定义底部 -->
      <slot v-if="footerSlot" name="footer"></slot>
      
      <!-- 默认底部按钮 -->
      <div v-else-if="showFooter" class="dialog-footer">
        <!-- 取消按钮 -->
        <el-button v-if="effectiveShowCancelButton" @click="handleCancel">
          {{ effectiveCancelText }}
        </el-button>
        
        <!-- 确认按钮 -->
        <el-button v-if="effectiveShowConfirmButton" type="primary" :loading="confirmLoading" @click="handleConfirm">
          {{ effectiveConfirmText }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { CircleCheckFilled, WarningFilled, CircleCloseFilled, InfoFilled } from '@element-plus/icons-vue'

// 组件属性定义
const props = defineProps<{
  // 是否显示对话框
  modelValue: boolean
  // 对话框标题
  title?: string
  // 对话框宽度
  width?: string | number
  // 是否全屏显示
  fullscreen?: boolean
  // 是否显示遮罩
  modal?: boolean
  // 遮罩是否插入到 body 元素下
  modalAppendToBody?: boolean
  // 对话框是否插入到 body 元素下
  appendToBody?: boolean
  // 是否锁定背景滚动
  lockScroll?: boolean
  // 自定义类名
  customClass?: string
  // 点击遮罩是否关闭对话框
  closeOnClickModal?: boolean
  // 按下 ESC 键是否关闭对话框
  closeOnPressEscape?: boolean
  // 是否显示关闭按钮
  showClose?: boolean
  // 是否居中显示
  center?: boolean
  // 对话框类型：success, warning, error, info, custom
  type?: 'success' | 'warning' | 'error' | 'info' | 'custom'
  // 对话框消息内容
  message?: string
  // 是否显示底部
  showFooter?: boolean
  // 是否显示取消按钮
  showCancelButton?: boolean
  // 是否显示确认按钮
  showConfirmButton?: boolean
  // 取消按钮文本
  cancelText?: string
  // 确认按钮文本
  confirmText?: string
  // 确认按钮加载状态
  confirmLoading?: boolean
  // 底部插槽名称
  footerSlot?: boolean
  // 是否显示内容加载状态
  loading?: boolean
  // 是否允许拖拽
  draggable?: boolean
  // 拖拽容器选择器
  dragContainer?: string
}>()

// 组件事件
const emit = defineEmits<{
  // 对话框关闭事件
  close: []
  // 对话框打开事件
  open: []
  // 对话框打开动画结束事件
  opened: []
  // 对话框关闭动画结束事件
  closed: []
  // 取消按钮点击事件
  cancel: []
  // 确认按钮点击事件
  confirm: []
  // 可见性变化事件
  'update:modelValue': [visible: boolean]
}>()

// 响应式数据
// 对话框可见性
const visible = ref(props.modelValue)

const effectiveShowClose = computed(() => props.showClose !== false)

const effectiveShowCancelButton = computed(() => {
  if (props.showFooter && props.showCancelButton === undefined && props.showConfirmButton === undefined) {
    return true
  }
  return !!props.showCancelButton
})

const effectiveShowConfirmButton = computed(() => {
  if (props.showFooter && props.showCancelButton === undefined && props.showConfirmButton === undefined) {
    return true
  }
  return !!props.showConfirmButton
})

const effectiveCancelText = computed(() => props.cancelText || '取消')

const effectiveConfirmText = computed(() => props.confirmText || '确定')

// 监听可见性变化
watch(
  () => props.modelValue,
  (newValue) => {
    visible.value = newValue
  }
)

// 监听内部可见性变化
watch(
  visible,
  (newValue) => {
    emit('update:modelValue', newValue)
  }
)

// 处理关闭
const handleClose = () => {
  emit('close')
}

// 处理打开
const handleOpen = () => {
  emit('open')
}

// 处理打开动画结束
const handleOpened = () => {
  emit('opened')
}

// 处理关闭动画结束
const handleClosed = () => {
  emit('closed')
}

// 处理取消
const handleCancel = () => {
  emit('cancel')
  visible.value = false
}

// 处理确认
const handleConfirm = () => {
  emit('confirm')
}

// 暴露方法给父组件
const expose = {
  // 打开对话框
  open: () => {
    visible.value = true
  },
  // 关闭对话框
  close: () => {
    visible.value = false
  },
  // 获取对话框可见性
  getVisible: () => visible.value
}

defineExpose(expose)
</script>

<style scoped lang="scss">
.dialog-content {
  padding: 20px 0;
  
  .dialog-type {
    display: flex;
    align-items: center;
    gap: 15px;
    padding: 20px;
    
    .success-icon {
      font-size: 48px;
      color: #67c23a;
    }
    
    .warning-icon {
      font-size: 48px;
      color: #e6a23c;
    }
    
    .error-icon {
      font-size: 48px;
      color: #f56c6c;
    }
    
    .info-icon {
      font-size: 48px;
      color: #909399;
    }
    
    .dialog-message {
      font-size: 16px;
      line-height: 1.5;
      color: #303133;
    }
  }
}

.dialog-footer {
  display: flex;
  gap: 10px;
  
  .el-button {
    margin-right: 0;
  }
}

// 响应式设计
@media (max-width: 768px) {
  .dialog-content {
    padding: 15px 0;
    
    .dialog-type {
      flex-direction: column;
      text-align: center;
      gap: 10px;
      
      .success-icon,
      .warning-icon,
      .error-icon,
      .info-icon {
        font-size: 36px;
      }
      
      .dialog-message {
        font-size: 14px;
      }
    }
  }
  
  .dialog-footer {
    flex-direction: column;
    
    .el-button {
      width: 100%;
    }
  }
}
</style>
