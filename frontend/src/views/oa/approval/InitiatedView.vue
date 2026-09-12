<template>
  <div class="initiated-view">
    <!-- 搜索和筛选区域 -->
    <div class="search-filter">
      <el-form :inline="true" :model="searchParams" class="filter-form">
        <el-form-item label="流程状态">
          <el-select v-model="searchParams.status" placeholder="选择流程状态" size="small" clearable>
            <el-option label="全部" value="" />
            <el-option label="审批中" value="running" />
            <el-option label="已通过" value="approved" />
            <el-option label="已拒绝" value="rejected" />
            <el-option label="已取消" value="cancelled" />
          </el-select>
        </el-form-item>
        <el-form-item label="来源模块">
          <el-select v-model="searchParams.processCode" placeholder="选择来源模块" size="small" clearable>
            <el-option label="全部" value="" />
            <el-option v-for="m in moduleOptions" :key="m.value" :label="m.label" :value="m.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="searchParams.keyword" placeholder="审批标题 / 业务单号" size="small" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="search" size="small">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="reset" size="small">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
          <el-button @click="fetchInitiatedList" size="small">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 我发起的审批列表 -->
    <div class="approval-list">
      <!-- 加载状态 -->
      <el-skeleton :rows="5" animated v-if="loading" />

      <!-- 空状态 -->
      <el-empty v-else-if="filteredList.length === 0" description="暂无发起的审批任务">
        <el-button type="primary" @click="navigateToCrossModule">去发起跨模块审批</el-button>
      </el-empty>

      <!-- 审批卡片列表 -->
      <el-card class="approval-card"
               v-else
               v-for="(item, index) in filteredList"
               :key="item.id || index"
               shadow="hover">
        <div class="card-content">
          <div class="approval-header">
            <div class="approval-title">
              <span class="title-text">{{ item.title || '未命名审批' }}</span>
              <el-tag :type="getStatusTagType(item.status)">{{ getStatusText(item.status) }}</el-tag>
            </div>
            <div class="approval-meta">
              <span class="source-module">
                <el-tag size="small" :type="getModuleTagType(item.processCode)">{{ getModuleLabel(item.processCode) }}</el-tag>
              </span>
              <span class="create-time">发起时间：{{ item.createTime || item.startTime || '-' }}</span>
            </div>
          </div>
          <div class="approval-body">
            <div class="approval-info">
              <div class="info-item">
                <el-icon><Connection /></el-icon>
                <span>当前节点：{{ item.currentNodeName || '-' }}</span>
              </div>
              <div class="info-item">
                <el-icon><Document /></el-icon>
                <span>业务单号：{{ extractBusinessNo(item) || '-' }}</span>
              </div>
              <div class="info-item" v-if="item.description">
                <el-icon><InfoFilled /></el-icon>
                <span>{{ item.description }}</span>
              </div>
            </div>
            <div class="approval-preview">
              <el-descriptions :column="2" size="small">
                <el-descriptions-item label="审批标题">{{ item.title || '-' }}</el-descriptions-item>
                <el-descriptions-item label="流程编码">{{ item.processCode || '-' }}</el-descriptions-item>
                <el-descriptions-item label="当前节点">{{ item.currentNodeName || '-' }}</el-descriptions-item>
                <el-descriptions-item label="发起时间">{{ item.createTime || item.startTime || '-' }}</el-descriptions-item>
                <el-descriptions-item label="审批描述" :span="2">{{ item.description || '-' }}</el-descriptions-item>
              </el-descriptions>
            </div>
          </div>
          <div class="approval-footer">
            <el-button type="primary" size="small" @click="viewDetail(item)">
              <el-icon><View /></el-icon>
              查看详情
            </el-button>
            <el-button v-if="item.status === 'running'" type="warning" size="small" @click="withdrawProcess(item)">
              <el-icon><Delete /></el-icon>
              撤回
            </el-button>
            <el-button type="success" size="small" @click="copyProcess(item)">
              <el-icon><CopyDocument /></el-icon>
              再次发起
            </el-button>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 分页 -->
    <div class="pagination" v-if="!loading && initiatedList.length > 0">
      <el-pagination
        v-model:current-page="pagination.currentPage"
        v-model:page-size="pagination.pageSize"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="pagination.total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 审批详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="审批详情" width="720px">
      <div v-loading="detailLoading">
        <el-descriptions v-if="approvalDetail" :column="2" border>
          <el-descriptions-item label="审批标题">{{ approvalDetail.instance?.title || '-' }}</el-descriptions-item>
          <el-descriptions-item label="来源模块">
            <el-tag size="small" :type="getModuleTagType(approvalDetail.instance?.processCode)">
              {{ getModuleLabel(approvalDetail.instance?.processCode) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="业务单号">{{ extractBusinessNo(approvalDetail.instance) || '-' }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusTagType(approvalDetail.instance?.status)">
              {{ getStatusText(approvalDetail.instance?.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="发起人">{{ approvalDetail.instance?.initiatorName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="发起时间">{{ approvalDetail.instance?.createTime || approvalDetail.instance?.startTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="当前节点" :span="2">{{ approvalDetail.instance?.currentNodeName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="审批描述" :span="2">{{ approvalDetail.instance?.description || '-' }}</el-descriptions-item>
        </el-descriptions>

        <div class="detail-section-title">审批流转记录</div>
        <el-timeline v-if="approvalDetail && (approvalDetail.tasks || []).length > 0">
          <el-timeline-item
            v-for="task in (approvalDetail.tasks || [])"
            :key="task.id"
            :type="getTaskTimelineType(task.status)"
          >
            <div class="timeline-content">
              <div class="timeline-header">
                <span class="timeline-node">{{ task.nodeName }}</span>
                <el-tag size="small" :type="getStatusTagType(task.status)">
                  {{ getTaskStatusText(task.status) }}
                </el-tag>
              </div>
              <div class="timeline-assignee">
                审批人：{{ task.assigneeName }}
                <span v-if="task.approveTime" class="timeline-time">{{ task.approveTime }}</span>
              </div>
              <div class="timeline-comment" v-if="task.comment">审批意见：{{ task.comment }}</div>
            </div>
          </el-timeline-item>
        </el-timeline>
        <el-empty v-else description="暂无审批流转记录" :image-size="80" />
      </div>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import {
  Search, Refresh, View, Delete, CopyDocument, Connection, Document, InfoFilled
} from '@element-plus/icons-vue'
import { unwrapResponseData } from '@/api'
import { unifiedApprovalApi } from '@/api/oa'
import type { ApprovalProcessInstance } from '@/api/oa'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useApprovalProcess } from '@/composables/useApprovalProcess'

/** 业务模块定义（用于来源模块筛选与展示） */
const moduleOptions = [
  { value: 'crm', label: 'CRM' },
  { value: 'scm', label: 'SCM' },
  { value: 'erp', label: 'ERP' },
  { value: 'wms', label: 'WMS' },
  { value: 'mes', label: 'MES' },
  { value: 'les', label: 'LES' },
  { value: 'qms', label: 'QMS' },
  { value: 'srm', label: 'SRM' },
  { value: 'eam', label: 'EAM' },
  { value: 'hr', label: 'HR' }
]

/** 搜索参数（前端过滤） */
const searchParams = reactive({
  status: '',
  processCode: '',
  keyword: ''
})

/** 分页参数 */
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

/** 我发起的审批列表（来自统一审批发起接口，返回审批实例） */
const initiatedList = ref<ApprovalProcessInstance[]>([])
const loading = ref(false)

/** 审批详情对话框 */
const detailDialogVisible = ref(false)
const detailLoading = ref(false)
const approvalDetail = ref<{ instance: any; tasks: any[] } | null>(null)

const router = useRouter()

/** 接入公共审批操作 composable（复用 getCurrentUser） */
const { getCurrentUser } = useApprovalProcess()

/**
 * 前端过滤后的列表（按流程状态 / 来源模块 / 关键词筛选）
 */
const filteredList = computed(() => {
  return initiatedList.value.filter((item) => {
    // 流程状态过滤
    if (searchParams.status) {
      if ((item.status || '') !== searchParams.status) return false
    }
    // 来源模块过滤
    if (searchParams.processCode) {
      const code = String(item.processCode || '').toLowerCase()
      if (!code.startsWith(searchParams.processCode) && code !== searchParams.processCode) return false
    }
    // 关键词过滤：匹配标题 / 业务单号
    if (searchParams.keyword) {
      const keyword = searchParams.keyword.toLowerCase()
      const matchStr = `${item.title || ''} ${extractBusinessNo(item)}`.toLowerCase()
      if (!matchStr.includes(keyword)) return false
    }
    return true
  })
})

/**
 * 获取我发起的审批列表（调用统一审批发起接口，按当前登录用户ID查询审批实例）
 */
const fetchInitiatedList = async () => {
  loading.value = true
  try {
    const userId = getCurrentUser().id
    const response = await unifiedApprovalApi.getInitiatedList(userId, {
      page: pagination.currentPage,
      size: pagination.pageSize
    })
    const data = unwrapResponseData<any>(response, {}) || {}
    initiatedList.value = data.records || data.list || []
    pagination.total = Number(data.total ?? initiatedList.value.length)
  } catch (error: any) {
    console.error('获取我发起的审批列表失败:', error)
    initiatedList.value = []
    pagination.total = 0
    const status = error?.response?.status ?? error?.data?.code
    if (status === 401) {
      ElMessage.error('登录状态已失效，请重新登录')
      return
    }
    ElMessage.error(error?.response?.data?.msg || error?.message || '获取我发起的审批列表失败')
  } finally {
    loading.value = false
  }
}

/** 搜索 */
const search = () => {
  pagination.currentPage = 1
  fetchInitiatedList()
}

/** 重置筛选 */
const reset = () => {
  Object.assign(searchParams, {
    status: '',
    processCode: '',
    keyword: ''
  })
  pagination.currentPage = 1
  fetchInitiatedList()
}

/** 分页大小变化 */
const handleSizeChange = (size: number) => {
  pagination.pageSize = size
  pagination.currentPage = 1
  fetchInitiatedList()
}

/** 页码变化 */
const handleCurrentChange = (current: number) => {
  pagination.currentPage = current
  fetchInitiatedList()
}

/**
 * 从审批实例中提取业务单号（优先从 formData / processVariables 中获取）
 */
const extractBusinessNo = (instance: any): string => {
  if (!instance) return ''
  if (instance.businessNo) return instance.businessNo
  try {
    const vars = typeof instance.processVariables === 'string'
      ? JSON.parse(instance.processVariables)
      : instance.processVariables
    if (vars?.businessNo) return vars.businessNo
    const formData = typeof instance.formData === 'string'
      ? JSON.parse(instance.formData)
      : instance.formData
    if (formData?.businessNo) return formData.businessNo
  } catch {
    // 忽略解析错误
  }
  return ''
}

/**
 * 查看审批详情（调用统一审批详情接口）
 */
const viewDetail = async (item: ApprovalProcessInstance) => {
  if (!item?.id) {
    ElMessage.error('无效的审批实例ID')
    return
  }
  detailDialogVisible.value = true
  detailLoading.value = true
  approvalDetail.value = null
  try {
    const response = await unifiedApprovalApi.getDetail(item.id)
    const data = unwrapResponseData<any>(response, {}) || {}
    approvalDetail.value = {
      instance: data.instance || data.processInstance || data,
      tasks: data.tasks || data.taskList || data.history || []
    }
  } catch (error: any) {
    console.error('获取审批详情失败:', error)
    ElMessage.error(error?.response?.data?.msg || error?.message || '获取审批详情失败')
  } finally {
    detailLoading.value = false
  }
}

/**
 * 撤回流程（提示确认，仅审批中的实例可撤回）
 */
const withdrawProcess = async (item: ApprovalProcessInstance) => {
  try {
    if (!item?.id) {
      ElMessage.error('无效的审批实例ID')
      return
    }
    await ElMessageBox.confirm('确定要撤回该审批流程吗？撤回后流程将终止。', '撤回流程', {
      confirmButtonText: '确定撤回',
      cancelButtonText: '取消',
      type: 'warning'
    })
    // 统一审批接口暂未提供撤回端点，此处提示用户后续支持
    ElMessage.info('撤回功能接入中，当前统一审批接口暂未提供撤回端点')
  } catch (error: any) {
    if (error === 'cancel') return
    console.error('撤回流程失败:', error)
    ElMessage.error(error?.response?.data?.msg || error?.message || '撤回流程失败')
  }
}

/**
 * 再次发起（跳转到跨模块审批中心，带上原始信息）
 */
const copyProcess = (item: ApprovalProcessInstance) => {
  router.push({
    path: '/home/oa/approval',
    query: {
      copyFromId: String(item.id || ''),
      processCode: item.processCode || ''
    }
  })
}

/**
 * 跳转到跨模块审批中心发起审批
 */
const navigateToCrossModule = () => {
  router.push('/home/oa/approval')
}

/**
 * 获取模块标签文本
 */
const getModuleLabel = (processCode: string): string => {
  if (!processCode) return '通用'
  const code = String(processCode).toUpperCase()
  const matched = moduleOptions.find((m) => code.startsWith(m.value.toUpperCase()))
  return matched ? matched.label : '通用'
}

/**
 * 获取模块标签类型
 */
const getModuleTagType = (processCode: string): string => {
  if (!processCode) return 'info'
  const code = String(processCode).toLowerCase()
  const typeMap: Record<string, string> = {
    crm: 'primary', scm: 'success', erp: 'warning', wms: 'danger',
    mes: '', les: 'warning', qms: 'success', srm: 'info', eam: 'warning', hr: ''
  }
  const matched = Object.keys(typeMap).find((k) => code.startsWith(k))
  return matched ? (typeMap[matched] ?? 'info') : 'info'
}

/**
 * 获取实例状态标签类型
 */
const getStatusTagType = (status?: string): string => {
  const statusStr = status || ''
  const typeMap: Record<string, string> = {
    running: 'warning', pending: 'warning',
    approved: 'success', completed: 'success',
    rejected: 'danger', cancelled: 'info'
  }
  return typeMap[statusStr] || 'info'
}

/**
 * 获取实例状态文本
 */
const getStatusText = (status?: string): string => {
  const statusStr = status || ''
  const statusMap: Record<string, string> = {
    running: '审批中', pending: '待审批',
    approved: '已通过', completed: '已完成',
    rejected: '已拒绝', cancelled: '已取消'
  }
  return statusMap[statusStr] || statusStr || '未知'
}

/**
 * 获取任务状态文本
 */
const getTaskStatusText = (status?: string): string => {
  const statusStr = status || ''
  const statusMap: Record<string, string> = {
    pending: '待审批', approved: '已通过',
    rejected: '已拒绝', cancelled: '已取消',
    completed: '已完成', returned: '已退回'
  }
  return statusMap[statusStr] || statusStr || '未知'
}

/**
 * 获取任务时间线节点类型
 */
const getTaskTimelineType = (status?: string): string => {
  const statusStr = status || ''
  const typeMap: Record<string, string> = {
    approved: 'success',
    rejected: 'danger',
    pending: 'warning',
    cancelled: 'info',
    completed: 'success'
  }
  return typeMap[statusStr] || 'info'
}

/** 初始加载 */
onMounted(() => {
  fetchInitiatedList()
})
</script>

<style scoped>
.initiated-view {
  padding: 20px;
  box-sizing: border-box;
}

.search-filter {
  margin-bottom: 20px;
}

.approval-list {
  margin-bottom: 20px;
}

.approval-card {
  margin-bottom: 16px;
  transition: all 0.3s ease;
}

.approval-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.card-content {
  padding: 16px;
}

.approval-header {
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.approval-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.title-text {
  font-size: 16px;
  font-weight: bold;
}

.approval-meta {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  color: #909399;
}

.approval-body {
  margin-bottom: 16px;
}

.approval-info {
  display: flex;
  gap: 20px;
  margin-bottom: 12px;
  font-size: 14px;
  flex-wrap: wrap;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #606266;
}

.approval-preview {
  background-color: #fafafa;
  padding: 12px;
  border-radius: 4px;
  font-size: 14px;
}

.approval-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.detail-section-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin: 20px 0 12px;
  padding-left: 8px;
  border-left: 3px solid #409eff;
}

.timeline-content {
  .timeline-header {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 6px;

    .timeline-node {
      font-weight: 600;
      color: #303133;
    }
  }

  .timeline-assignee {
    font-size: 13px;
    color: #606266;

    .timeline-time {
      margin-left: 8px;
      color: #909399;
    }
  }

  .timeline-comment {
    margin-top: 4px;
    font-size: 13px;
    color: #606266;
    background: #f5f7fa;
    padding: 6px 10px;
    border-radius: 4px;
  }
}

@media (max-width: 768px) {
  .initiated-view {
    padding: 12px;
  }

  .filter-form {
    display: flex;
    flex-direction: column;
    gap: 10px;
  }

  .approval-info {
    flex-direction: column;
    gap: 8px;
  }

  .approval-footer {
    flex-wrap: wrap;
  }

  .approval-footer .el-button {
    flex: 1;
    min-width: 80px;
  }
}
</style>
