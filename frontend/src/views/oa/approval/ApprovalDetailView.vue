<template>
  <div class="approval-detail-view">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2 class="page-title">审批详情</h2>
      <div class="header-actions">
        <el-button type="primary" @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
          返回列表
        </el-button>
      </div>
    </div>
    
    <!-- 加载状态 -->
    <el-skeleton :rows="5" animated v-if="loading" style="padding: 20px;"></el-skeleton>
    
    <!-- 审批详情内容 -->
    <div v-else-if="approvalDetail" class="approval-content">
      <!-- 审批基本信息 -->
      <el-card class="detail-card" shadow="hover">
        <div class="card-header">
          <span class="card-title">基本信息</span>
          <el-tag :type="getStatusTagType(approvalDetail.status)">{{ getStatusText(approvalDetail.status) }}</el-tag>
        </div>
        <div class="card-body">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="审批标题">{{ approvalDetail.name }}</el-descriptions-item>
            <el-descriptions-item label="审批编号">{{ approvalDetail.id }}</el-descriptions-item>
            <el-descriptions-item label="发起人">{{ approvalDetail.assigneeName }}</el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ approvalDetail.createTime }}</el-descriptions-item>
            <el-descriptions-item label="截止时间">{{ approvalDetail.dueTime || '无' }}</el-descriptions-item>
            <el-descriptions-item label="当前节点">{{ approvalDetail.nodeName }}</el-descriptions-item>
            <el-descriptions-item label="审批状态">{{ getStatusText(approvalDetail.status) }}</el-descriptions-item>
          </el-descriptions>
        </div>
      </el-card>
      
      <!-- 审批表单内容 -->
      <el-card class="detail-card" shadow="hover" v-if="approvalDetail.description">
        <div class="card-header">
          <span class="card-title">审批内容</span>
        </div>
        <div class="card-body">
          <div class="form-content">
            {{ approvalDetail.description }}
          </div>
        </div>
      </el-card>
      
      <!-- AI 预审意见（S07，AUTO 级附加意见，缺失时整体隐藏） -->
      <el-card class="detail-card" shadow="hover" v-if="preauditSuggestion">
        <div class="card-header">
          <span class="card-title">
            <el-icon class="preaudit-title-icon"><Cpu /></el-icon>
            AI 预审意见
          </span>
          <el-tag v-if="preauditAnalysis?.conclusion" :type="preauditTagType">{{ preauditConclusionText }}</el-tag>
        </div>
        <div class="card-body">
          <!-- 存疑项 -->
          <div class="preaudit-section" v-if="preauditRisks.length">
            <div class="preaudit-section-title">存疑项</div>
            <el-alert
              v-for="(risk, index) in preauditRisks"
              :key="`risk-${index}`"
              type="error"
              :title="risk"
              :closable="false"
              show-icon
              class="preaudit-alert"
            />
          </div>
          <!-- 核查提示 -->
          <div class="preaudit-section" v-if="preauditFindings.length">
            <div class="preaudit-section-title">核查提示</div>
            <el-alert
              v-for="(finding, index) in preauditFindings"
              :key="`finding-${index}`"
              type="info"
              :title="finding"
              :closable="false"
              show-icon
              class="preaudit-alert"
            />
          </div>
          <!-- analysis 解析失败时按纯文本展示 -->
          <div class="preaudit-raw" v-if="preauditRawText">{{ preauditRawText }}</div>
          <!-- 底部证据来源与免责声明 -->
          <div class="preaudit-footer">
            <span v-if="preauditSources.length">证据来源：{{ preauditSources.join('、') }}</span>
            <span>预审意见仅供参考，审批权在审批人</span>
          </div>
        </div>
      </el-card>

      <!-- 审批历史记录 -->
      <el-card class="detail-card" shadow="hover">
        <div class="card-header">
          <span class="card-title">审批历史</span>
        </div>
        <div class="card-body">
          <el-timeline>
            <el-timeline-item
              v-for="(item, index) in approvalHistory"
              :key="index"
              :timestamp="item.time"
              :type="item.type"
              :color="item.color"
              :size="item.size"
              :icon="item.icon"
              placement="top"
            >
              <div class="timeline-content">
                <h4 class="timeline-title">{{ item.title }}</h4>
                <p class="timeline-desc">{{ item.description }}</p>
                <p class="timeline-comment" v-if="item.comment">
                  <span class="comment-label">审批意见：</span>
                  {{ item.comment }}
                </p>
              </div>
            </el-timeline-item>
          </el-timeline>
        </div>
      </el-card>
      
      <!-- 审批操作区 -->
      <el-card class="detail-card" shadow="hover" v-if="canApprove">
        <div class="card-header">
          <span class="card-title">审批操作</span>
        </div>
        <div class="card-body">
          <div class="approval-actions">
            <el-form :model="approvalForm" label-position="top">
              <el-form-item label="审批意见">
                <el-input
                  v-model="approvalForm.comment"
                  type="textarea"
                  :rows="4"
                  placeholder="请输入审批意见（可选）"
                ></el-input>
              </el-form-item>
              <el-form-item>
                <div class="action-buttons">
                  <el-button type="danger" size="large" @click="handleReject">
                    <el-icon><Close /></el-icon>
                    拒绝
                  </el-button>
                  <el-button type="primary" size="large" @click="handleApprove">
                    <el-icon><Check /></el-icon>
                    同意
                  </el-button>
                </div>
              </el-form-item>
            </el-form>
          </div>
        </div>
      </el-card>
    </div>
    
    <!-- 空状态 -->
    <div v-else class="empty-state">
      <el-empty
        description="
          <div class='empty-desc'>
            <span class='empty-title'>未找到审批详情</span>
            <span class='empty-subtitle'>该审批任务不存在或已被删除</span>
          </div>
        "
        :image-size="120"
      >
        <el-button type="primary" @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
          返回列表
        </el-button>
      </el-empty>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, Check, Close, Cpu } from '@element-plus/icons-vue'
import { unwrapPageResponse, unwrapResponseData } from '@/api'
import { approvalTaskApi, unifiedApprovalApi } from '@/api/oa'
import type { ApprovalTask } from '@/api/oa'
import { getPreauditByInstance } from '@/api/ai'
import type { AiSuggestion } from '@/api/ai'
import { useAuthStore } from '@/stores/auth'
import { ElMessage, ElMessageBox } from 'element-plus'

// 路由实例
const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

/**
 * 提交审批操作（统一审批接口，推进流程流转）。
 * <p>旧端点 /api/v1/oa/approval/tasks/{id}/approve 仅更新任务状态不推进流程，
 * 必须使用 unifiedApprovalApi.processTask 才会触发节点流转与回调。</p>
 *
 * @param action  审批动作：approve-通过 / reject-拒绝
 * @param comment 审批意见
 */
const submitApprovalAction = async (action: 'approve' | 'reject', comment: string) => {
  const taskId = approvalDetail.value?.id
  if (!taskId) {
    ElMessage.error('审批任务ID缺失')
    return
  }
  const userInfo = authStore.userInfo as any
  await unifiedApprovalApi.processTask(taskId, {
    action,
    comment,
    approverId: Number(userInfo?.id) || 1,
    approverName: userInfo?.name || userInfo?.username || '当前用户'
  })
}

// 审批详情数据
const approvalDetail = ref<ApprovalTask | null>(null)
const loading = ref(false)
const approvalHistory = ref<any[]>([])

/** AI 预审意见 analysis 字段（JSON 字符串）解析后的结构 */
interface PreauditAnalysis {
  instanceId?: number
  nodeName?: string
  assigneeName?: string
  initiatorName?: string
  risks?: string[]
  findings?: string[]
  conclusion?: string
  evidence?: {
    sources?: string[]
    metrics?: Record<string, any>
  }
}

// AI 预审意见数据（S07）
const preauditSuggestion = ref<AiSuggestion | null>(null)
const preauditAnalysis = ref<PreauditAnalysis | null>(null)
const preauditRawText = ref('')

// 计算属性：预审存疑项列表
const preauditRisks = computed<string[]>(() =>
  Array.isArray(preauditAnalysis.value?.risks) ? preauditAnalysis.value!.risks! : []
)

// 计算属性：预审核查提示列表
const preauditFindings = computed<string[]>(() =>
  Array.isArray(preauditAnalysis.value?.findings) ? preauditAnalysis.value!.findings! : []
)

// 计算属性：预审证据来源列表
const preauditSources = computed<string[]>(() =>
  Array.isArray(preauditAnalysis.value?.evidence?.sources) ? preauditAnalysis.value!.evidence!.sources! : []
)

// 计算属性：预审结论标签类型
const preauditTagType = computed(() =>
  preauditAnalysis.value?.conclusion === 'REVIEW_RECOMMENDED' ? 'danger' : 'success'
)

// 计算属性：预审结论文案
const preauditConclusionText = computed(() =>
  preauditAnalysis.value?.conclusion === 'REVIEW_RECOMMENDED' ? '建议人工复核' : '核查通过（附提示）'
)

/**
 * 加载当前审批实例的 AI 预审意见（S07）。
 * <p>预审为 AUTO 级附加意见：无预审数据（data 为 null）或请求失败（含 404）时
 * 静默降级、整体隐藏卡片，仅 console.debug 记录，不阻塞审批页面。</p>
 *
 * @param instanceId OA 审批实例ID
 */
const fetchPreauditSuggestion = async (instanceId: number) => {
  try {
    const response = await getPreauditByInstance(instanceId)
    const suggestion = unwrapResponseData<AiSuggestion>(response)
    if (!suggestion) {
      console.debug('当前审批实例暂无 AI 预审意见')
      return
    }
    preauditSuggestion.value = suggestion
    if (suggestion.analysis) {
      try {
        preauditAnalysis.value = JSON.parse(suggestion.analysis) as PreauditAnalysis
      } catch {
        // analysis 非合法 JSON 时按纯文本降级展示
        preauditRawText.value = suggestion.analysis
      }
    }
  } catch (error) {
    console.debug('获取 AI 预审意见失败，已静默降级:', error)
  }
}

// 审批表单
const approvalForm = ref({
  comment: ''
})

// 计算属性：是否可以审批
const canApprove = computed(() => {
  return approvalDetail.value?.status === 'pending'
})

// 获取审批历史
const fetchApprovalHistory = async (instanceId: number) => {
  try {
    // 调用API获取同一实例的所有审批任务作为审批历史
    const response = await approvalTaskApi.getByInstanceId(instanceId, { page: 1, size: 100 })

    return unwrapPageResponse<any>(response).list.map((task: any) => ({
      time: task.approveTime || task.createTime,
      type: task.result === 'approved' ? 'success' : task.result === 'rejected' ? 'danger' : 'primary',
      color: '',
      size: '',
      icon: '',
      title: task.result ? `${task.result === 'approved' ? '同意' : '拒绝'}审批` : '待审批',
      description: `${task.assigneeName}${task.result ? `在${task.nodeName}节点${task.result === 'approved' ? '同意' : '拒绝'}了审批` : `在${task.nodeName}节点待审批`}`,
      comment: task.comment || ''
    }))
  } catch (error) {
    console.error('获取审批历史失败:', error)
    return []
  }
}

// 获取审批详情
const fetchApprovalDetail = async () => {
  const id = route.params.id
  if (!id) {
    ElMessage.error('无效的审批ID')
    return
  }
  
  loading.value = true
  try {
    // 调用真实API获取审批详情
    const response = await approvalTaskApi.getById(Number(id))
    approvalDetail.value = unwrapResponseData<ApprovalTask>(response)

    if (!approvalDetail.value) {
      ElMessage.error('获取审批详情失败')
      return
    }

    // 获取审批历史
    if (approvalDetail.value.instanceId) {
      approvalHistory.value = await fetchApprovalHistory(approvalDetail.value.instanceId)
      // 加载 AI 预审意见（静默降级，失败不影响审批主流程）
      await fetchPreauditSuggestion(approvalDetail.value.instanceId)
    }
  } catch (error) {
    console.error('获取审批详情失败:', error)
    ElMessage.error('获取审批详情失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 返回列表
const goBack = () => {
  router.back()
}

// 获取状态标签类型
const getStatusTagType = (status?: string) => {
  const statusStr = status || ''
  const typeMap: Record<string, string> = {
    pending: 'warning',
    approved: 'success',
    rejected: 'danger',
    cancelled: 'info',
    running: 'primary',
    completed: 'success',
    failed: 'danger',
    processing: 'warning',
    waiting: 'info'
  }
  return typeMap[statusStr] || 'info'
}

// 获取状态文本
const getStatusText = (status?: string) => {
  const statusStr = status || ''
  const statusMap: Record<string, string> = {
    pending: '待审批',
    approved: '已同意',
    rejected: '已拒绝',
    cancelled: '已取消',
    running: '运行中',
    completed: '已完成',
    failed: '已失败',
    processing: '处理中',
    waiting: '等待中'
  }
  return statusMap[statusStr] || '未知状态'
}

// 获取流程类型文本
const getProcessTypeText = (type?: string) => {
  const typeStr = type || ''
  const typeMap: Record<string, string> = {
    leave: '请假申请',
    expense: '报销申请',
    purchase: '采购申请',
    contract: '合同审批',
    seal: '用章申请',
    travel: '出差申请'
  }
  return typeMap[typeStr] || typeStr
}

// 同意审批
const handleApprove = async () => {
  if (!approvalDetail.value?.id) {
    ElMessage.error('无效的审批ID')
    return
  }
  
  try {
    await ElMessageBox.confirm('确定要同意该审批吗？', '审批确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    
    // 调用统一审批接口（推进流程流转并触发回调）
    await submitApprovalAction('approve', approvalForm.value.comment)

    ElMessage.success('审批成功')
    fetchApprovalDetail() // 刷新详情
  } catch (error: any) {
    if (error === 'cancel') {
      return
    }
    console.error('审批失败:', error)
    ElMessage.error(error?.msg || '审批失败，请稍后重试')
  }
}

// 拒绝审批
const handleReject = async () => {
  if (!approvalDetail.value?.id) {
    ElMessage.error('无效的审批ID')
    return
  }
  
  try {
    await ElMessageBox.confirm('确定要拒绝该审批吗？', '审批确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 调用统一审批接口（推进流程流转并触发回调）
    await submitApprovalAction('reject', approvalForm.value.comment)

    ElMessage.success('审批成功')
    fetchApprovalDetail() // 刷新详情
  } catch (error: any) {
    if (error === 'cancel') {
      return
    }
    console.error('审批失败:', error)
    ElMessage.error(error?.msg || '审批失败，请稍后重试')
  }
}

// 初始化
onMounted(() => {
  fetchApprovalDetail()
})
</script>

<style scoped>
.approval-detail-view {
  padding: 20px;
  box-sizing: border-box;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-title {
  margin: 0;
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

.approval-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.detail-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.card-title {
  font-size: 18px;
  font-weight: bold;
  color: #303133;
}

.card-body {
  padding: 0;
}

.form-content {
  padding: 16px;
  background-color: #fafafa;
  border-radius: 4px;
  font-size: 14px;
  line-height: 1.6;
}

.timeline-content {
  padding: 8px 0;
}

.timeline-title {
  margin: 0 0 8px 0;
  font-size: 14px;
  font-weight: bold;
  color: #303133;
}

.timeline-desc {
  margin: 0 0 8px 0;
  font-size: 14px;
  color: #606266;
}

.timeline-comment {
  margin: 0;
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

.comment-label {
  font-weight: bold;
  color: #606266;
}

.preaudit-title-icon {
  margin-right: 6px;
  vertical-align: -2px;
}

.preaudit-section {
  margin-bottom: 12px;
}

.preaudit-section-title {
  font-size: 14px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 8px;
}

.preaudit-alert {
  margin-bottom: 8px;
}

.preaudit-raw {
  padding: 12px;
  background-color: #fafafa;
  border-radius: 4px;
  font-size: 14px;
  line-height: 1.6;
  white-space: pre-wrap;
}

.preaudit-footer {
  margin-top: 12px;
  font-size: 12px;
  color: #909399;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.approval-actions {
  padding: 16px 0;
}

.action-buttons {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 16px;
}

.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 60px 20px;
}

.empty-desc {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.empty-title {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.empty-subtitle {
  font-size: 14px;
  color: #909399;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .approval-detail-view {
    padding: 12px;
  }
  
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .page-title {
    font-size: 20px;
  }
  
  .detail-card {
    margin-bottom: 16px;
  }
  
  .action-buttons {
    flex-direction: column;
  }
  
  .action-buttons .el-button {
    width: 100%;
  }
}
</style>
