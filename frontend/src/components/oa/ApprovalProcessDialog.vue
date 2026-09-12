<template>
  <el-dialog
    :model-value="visible"
    :title="title"
    width="500px"
    :close-on-click-modal="false"
    @update:model-value="handleVisibleChange"
  >
    <el-form label-width="80px">
      <el-form-item label="审批意见">
        <el-input
          v-model="comment"
          type="textarea"
          :rows="4"
          placeholder="请输入审批意见（选填）"
          maxlength="500"
          show-word-limit
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="handleVisibleChange(false)">取消</el-button>
      <el-button
        :type="action === 'approve' ? 'success' : 'danger'"
        :loading="loading"
        @click="handleConfirm"
      >
        {{ action === 'approve' ? '确认通过' : '确认拒绝' }}
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'

/**
 * 审批操作对话框组件。
 * <p>统一的审批通过/拒绝对话框，内部管理审批意见（comment）状态，
 * 确认时通过 confirm 事件将 comment 传出，由父组件调用 useApprovalProcess.confirmProcess 完成 API 提交。</p>
 *
 * <p>按钮文案与类型根据 action 自动切换：approve→绿色"确认通过"，reject→红色"确认拒绝"。</p>
 */

const props = defineProps<{
  /** 对话框可见性（v-model:visible） */
  visible: boolean
  /** 提交加载中状态 */
  loading: boolean
  /** 审批动作：approve-通过 / reject-拒绝 */
  action: 'approve' | 'reject'
}>()

const emit = defineEmits<{
  /** 可见性变更 */
  'update:visible': [value: boolean]
  /** 确认审批，传出审批意见 */
  confirm: [comment: string]
}>()

/** 审批意见（对话框内部管理，打开时重置） */
const comment = ref('')

/** 对话框标题 */
const title = computed(() => (props.action === 'approve' ? '审批通过' : '审批拒绝'))

/** 对话框打开时重置审批意见 */
watch(
  () => props.visible,
  (val) => {
    if (val) {
      comment.value = ''
    }
  }
)

/** 处理可见性变更 */
const handleVisibleChange = (val: boolean) => {
  emit('update:visible', val)
}

/** 确认审批，传出审批意见 */
const handleConfirm = () => {
  emit('confirm', comment.value)
}
</script>
