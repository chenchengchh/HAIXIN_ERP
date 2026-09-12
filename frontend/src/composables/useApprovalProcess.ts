import { ref, reactive, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { unifiedApprovalApi } from '@/api/oa'
import type { ApprovalActionRequest } from '@/api/oa'

/**
 * 审批操作 Composable。
 * <p>统一封装审批操作（通过/拒绝）的状态机与 API 调用，供 OaView / PendingApprovalView /
 * CrossModuleApprovalView 等多个视图复用，避免重复编写 processDialog 状态与 processTask 调用逻辑。</p>
 *
 * <p>关键点：每次调用 useApprovalProcess() 都会创建独立的 ref 状态，
 * 因此各视图独立调用不会出现对话框串显问题。</p>
 *
 * <p>审批操作通过 unifiedApprovalApi.processTask 提交，自动从 authStore 获取当前用户
 * 的 approverId/approverName，满足后端三层权限校验（角色校验 + 任务归属校验）。</p>
 *
 * @returns 审批操作相关的状态与方法
 */
export function useApprovalProcess() {
  const authStore = useAuthStore()

  /** 审批操作对话框可见性 */
  const processDialogVisible = ref(false)
  /** 审批操作加载中状态 */
  const processLoading = ref(false)
  /** 审批操作表单（taskId + action + comment） */
  const processForm = reactive<{
    taskId: number
    action: 'approve' | 'reject'
    comment: string
  }>({
    taskId: 0,
    action: 'approve',
    comment: ''
  })

  /** 审批操作对话框标题（根据动作切换） */
  const processDialogTitle = computed(() => {
    return processForm.action === 'approve' ? '审批通过' : '审批拒绝'
  })

  /**
   * 获取当前登录用户信息（用于审批操作时填充审批人 approverId/approverName）。
   * <p>id 兜底为 1（admin），避免传 0 导致后端查询异常；
   * name 优先取 name，其次 username，最后兜底"当前用户"。</p>
   *
   * @returns 当前用户 { id, name }
   */
  const getCurrentUser = () => {
    const userInfo = authStore.userInfo as any
    return {
      id: Number(userInfo?.id) || 1,
      name: userInfo?.name || userInfo?.username || '当前用户'
    }
  }

  /**
   * 打开审批操作对话框。
   *
   * @param task   待办任务（需含 id）
   * @param action 审批动作：approve-通过 / reject-拒绝
   */
  const openProcessDialog = (task: { id?: number }, action: 'approve' | 'reject') => {
    processForm.taskId = task.id || 0
    processForm.action = action
    processForm.comment = ''
    processDialogVisible.value = true
  }

  /**
   * 确认审批操作（调用统一审批接口，携带 approverId/approverName 进行权限校验）。
   *
   * @param comment   审批意见（由对话框组件传入）
   * @param onSuccess 审批成功后的回调（如刷新列表/统计），由调用方传入
   */
  const confirmProcess = async (
    comment: string,
    onSuccess?: () => void | Promise<void>
  ) => {
    if (!processForm.taskId) {
      ElMessage.error('无效的审批任务ID')
      return
    }
    processForm.comment = comment
    processLoading.value = true
    try {
      const user = getCurrentUser()
      const payload: ApprovalActionRequest = {
        action: processForm.action,
        comment: processForm.comment,
        approverId: user.id,
        approverName: user.name
      }
      await unifiedApprovalApi.processTask(processForm.taskId, payload)
      ElMessage.success(processForm.action === 'approve' ? '审批通过成功' : '审批拒绝成功')
      processDialogVisible.value = false
      // 执行调用方传入的成功回调（如刷新待办列表、统计等）
      if (onSuccess) {
        await onSuccess()
      }
    } catch (error: any) {
      console.error('审批操作失败:', error)
      ElMessage.error(error?.response?.data?.msg || error?.message || '审批操作失败')
    } finally {
      processLoading.value = false
    }
  }

  /** 关闭审批操作对话框 */
  const closeProcessDialog = () => {
    processDialogVisible.value = false
  }

  return {
    processDialogVisible,
    processLoading,
    processForm,
    processDialogTitle,
    openProcessDialog,
    confirmProcess,
    closeProcessDialog,
    getCurrentUser
  }
}
