<template>
  <div class="cross-module-approval-view">
    <!-- 模块概述 -->
    <div class="overview-banner">
      <div class="banner-content">
        <div class="banner-title">
          <el-icon class="banner-icon"><Connection /></el-icon>
          <span>跨模块统一审批中心</span>
        </div>
        <div class="banner-desc">
          OA审批贯穿 CRM / SCM / ERP / WMS / MES / LES / QMS / SRM / EAM / HR 等全业务系统，实现统一入口、统一流转、统一回调的审批闭环
        </div>
      </div>
      <div class="banner-actions">
        <el-button type="primary" :icon="Plus" @click="openSubmitDialog">发起跨模块审批</el-button>
        <el-button :icon="Refresh" @click="refreshAll">刷新</el-button>
      </div>
    </div>

    <!-- 总体统计卡片 -->
    <div class="total-stats" v-loading="statsLoading">
      <el-card class="total-stat-card" shadow="hover">
        <div class="stat-content">
          <div class="stat-icon total">
            <el-icon><Document /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ totalStats.total || 0 }}</div>
            <div class="stat-label">审批总数</div>
          </div>
        </div>
      </el-card>
      <el-card class="total-stat-card" shadow="hover">
        <div class="stat-content">
          <div class="stat-icon pending">
            <el-icon><Clock /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ totalStats.pending || 0 }}</div>
            <div class="stat-label">待审批</div>
          </div>
        </div>
      </el-card>
      <el-card class="total-stat-card" shadow="hover">
        <div class="stat-content">
          <div class="stat-icon approved">
            <el-icon><Check /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ totalStats.approved || 0 }}</div>
            <div class="stat-label">已通过</div>
          </div>
        </div>
      </el-card>
      <el-card class="total-stat-card" shadow="hover">
        <div class="stat-content">
          <div class="stat-icon rejected">
            <el-icon><Close /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ totalStats.rejected || 0 }}</div>
            <div class="stat-label">已拒绝</div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 我的待办审批 -->
    <div class="section-header">
      <h3 class="section-title">
        <el-icon><Bell /></el-icon>
        我的待办审批
      </h3>
      <div class="section-filters">
        <el-button :icon="Refresh" size="small" @click="fetchTodoList">刷新</el-button>
      </div>
    </div>

    <el-table :data="todoList" v-loading="todoLoading" style="width: 100%" stripe>
      <el-table-column label="审批标题" prop="name" min-width="180" show-overflow-tooltip>
        <template #default="{ row }">
          <span>{{ row.name || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="当前节点" prop="nodeName" width="140" show-overflow-tooltip>
        <template #default="{ row }">{{ row.nodeName || '-' }}</template>
      </el-table-column>
      <el-table-column label="要求角色" prop="assigneeRole" width="150" show-overflow-tooltip>
        <template #default="{ row }">
          <el-tag size="small" type="info" v-if="row.assigneeRole">{{ row.assigneeRole }}</el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="分配时间" prop="createTime" width="170">
        <template #default="{ row }">{{ row.createTime || '-' }}</template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button link type="success" size="small" @click="openProcessDialog(row, 'approve')">通过</el-button>
          <el-button link type="danger" size="small" @click="openProcessDialog(row, 'reject')">拒绝</el-button>
        </template>
      </el-table-column>
      <template #empty>
        <el-empty description="暂无待办审批任务" :image-size="80" />
      </template>
    </el-table>

    <!-- 我发起的跨模块审批列表 -->
    <div class="section-header">
      <h3 class="section-title">
        <el-icon><List /></el-icon>
        我发起的跨模块审批
      </h3>
      <div class="section-filters">
        <el-select v-model="filterModule" placeholder="全部模块" size="small" clearable style="width: 160px" @change="fetchInitiatedList">
          <el-option v-for="m in moduleOptions" :key="m.value" :label="m.label" :value="m.value" />
        </el-select>
        <el-button :icon="Refresh" size="small" @click="fetchInitiatedList">刷新</el-button>
      </div>
    </div>

    <el-table :data="initiatedList" v-loading="listLoading" style="width: 100%" stripe>
      <el-table-column label="审批标题" prop="title" min-width="180" show-overflow-tooltip>
        <template #default="{ row }">
          <el-link type="primary" @click="viewDetail(row)">{{ row.title || '-' }}</el-link>
        </template>
      </el-table-column>
      <el-table-column label="来源模块" prop="processCode" width="140">
        <template #default="{ row }">
          <el-tag size="small" :type="getModuleTagType(row.processCode)">{{ getModuleLabel(row.processCode) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="业务单号" prop="businessNo" width="150" show-overflow-tooltip>
        <template #default="{ row }">
          {{ extractBusinessNo(row) || '-' }}
        </template>
      </el-table-column>
      <el-table-column label="状态" prop="status" width="110">
        <template #default="{ row }">
          <el-tag :type="getStatusTagType(row.status)">{{ getStatusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="当前节点" prop="currentNodeName" width="140" show-overflow-tooltip>
        <template #default="{ row }">{{ row.currentNodeName || '-' }}</template>
      </el-table-column>
      <el-table-column label="发起时间" prop="createTime" width="170">
        <template #default="{ row }">{{ row.createTime || row.startTime || '-' }}</template>
      </el-table-column>
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="viewDetail(row)">详情</el-button>
        </template>
      </el-table-column>
      <template #empty>
        <el-empty description="暂无发起的跨模块审批" :image-size="80" />
      </template>
    </el-table>

    <div class="pagination" v-if="pagination.total > 0">
      <el-pagination
        v-model:current-page="pagination.currentPage"
        v-model:page-size="pagination.pageSize"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="pagination.total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 发起跨模块审批对话框 -->
    <el-dialog v-model="submitDialogVisible" title="发起跨模块审批" width="640px" :close-on-click-modal="false">
      <el-form ref="submitFormRef" :model="submitForm" :rules="submitRules" label-width="100px">
        <el-form-item label="来源模块" prop="sourceSystem">
          <el-select v-model="submitForm.sourceSystem" placeholder="请选择来源业务模块" style="width: 100%" @change="onSourceSystemChange">
            <el-option v-for="m in moduleOptions" :key="m.value" :label="m.label" :value="m.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="业务类型" prop="businessType">
          <el-select v-model="submitForm.businessType" placeholder="请选择业务类型" style="width: 100%">
            <el-option
              v-for="bt in currentBusinessTypes"
              :key="bt.value"
              :label="bt.label"
              :value="bt.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="业务单号" prop="businessNo">
          <el-input v-model="submitForm.businessNo" placeholder="请输入业务单据编号（如：SO20260728001）" />
        </el-form-item>
        <el-form-item label="业务ID" prop="businessId">
          <el-input v-model="submitForm.businessId" placeholder="请输入业务单据ID" />
        </el-form-item>
        <el-form-item label="审批标题" prop="title">
          <el-input v-model="submitForm.title" placeholder="请输入审批标题" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="审批描述" prop="description">
          <el-input v-model="submitForm.description" type="textarea" :rows="3" placeholder="请输入审批描述（选填）" maxlength="500" show-word-limit />
        </el-form-item>
        <el-form-item label="回调地址" prop="callbackUrl">
          <el-input v-model="submitForm.callbackUrl" placeholder="审批完成后回调URL（选填，如：http://crm:8086/api/v1/crm/approval/callback）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="submitDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitApproval">提交审批</el-button>
      </template>
    </el-dialog>

    <!-- 审批详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="跨模块审批详情" width="720px">
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
        <el-timeline v-if="groupedTasks.length > 0">
          <el-timeline-item
            v-for="group in groupedTasks"
            :key="group.nodeId"
            :type="getGroupTimelineType(group)"
          >
            <div class="timeline-content">
              <div class="timeline-header">
                <span class="timeline-node">{{ group.nodeName }}</span>
                <el-tag size="small" :type="getGroupTagType(group)">
                  {{ getSignedCount(group) }}/{{ group.tasks.length }} 已签
                </el-tag>
              </div>
              <div class="timeline-assignee" v-for="task in group.tasks" :key="task.id">
                <span>审批人：{{ task.assigneeName }}</span>
                <el-tag size="small" :type="getStatusTagType(task.status)" style="margin-left: 8px">
                  {{ getTaskStatusText(task.status) }}
                </el-tag>
                <span v-if="task.approveTime" class="timeline-time">{{ task.approveTime }}</span>
                <div class="timeline-comment" v-if="task.comment">审批意见：{{ task.comment }}</div>
              </div>
            </div>
          </el-timeline-item>
        </el-timeline>
        <el-empty v-else description="暂无审批流转记录" :image-size="80" />
      </div>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 审批操作对话框（公共组件，携带 approverId 进行权限校验） -->
    <ApprovalProcessDialog
      v-model:visible="processDialogVisible"
      :loading="processLoading"
      :action="processForm.action"
      @confirm="(comment: string) => confirmProcess(comment, onApprovalSuccess)"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import {
  Connection, Plus, Refresh, Document, Clock, Check, Close, List,
  ShoppingCart, Goods, Box, OfficeBuilding, Cpu, DataAnalysis, Files,
  Setting, Tools, User, Bell
} from '@element-plus/icons-vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { unifiedApprovalApi } from '@/api/oa'
import type { UnifiedApprovalRequest, ApprovalTask } from '@/api/oa'
import { unwrapResponseData } from '@/api'
import { useApprovalProcess } from '@/composables/useApprovalProcess'
import ApprovalProcessDialog from '@/components/oa/ApprovalProcessDialog.vue'

// 业务模块定义（OA贯穿的全部业务系统）
const moduleOptions = [
  { value: 'crm', label: 'CRM 客户关系管理', icon: ShoppingCart },
  { value: 'scm', label: 'SCM 供应链管理', icon: Goods },
  { value: 'erp', label: 'ERP 企业资源计划', icon: Box },
  { value: 'wms', label: 'WMS 仓储管理', icon: OfficeBuilding },
  { value: 'mes', label: 'MES 制造执行', icon: Cpu },
  { value: 'les', label: 'LES 物流执行', icon: Goods },
  { value: 'qms', label: 'QMS 质量管理', icon: Check },
  { value: 'srm', label: 'SRM 供应商管理', icon: Files },
  { value: 'eam', label: 'EAM 设备资产', icon: Tools },
  { value: 'hr', label: 'HR 人力资源', icon: User }
]

// 各模块的业务类型映射（用于发起审批时选择）
const businessTypeMap: Record<string, { value: string; label: string }[]> = {
  crm: [
    { value: 'sales_order', label: '销售订单审批' },
    { value: 'contract', label: '销售合同审批' },
    { value: 'customer_visit', label: '客户拜访审批' }
  ],
  scm: [
    { value: 'purchase_order', label: '采购订单审批' },
    { value: 'supplier_contract', label: '供应商合同审批' },
    { value: 'logistics_plan', label: '物流方案审批' }
  ],
  erp: [
    { value: 'production_order', label: '生产订单审批' },
    { value: 'finance_payment', label: '财务付款审批' },
    { value: 'finance_receipt', label: '财务收款审批' }
  ],
  wms: [
    { value: 'warehouse_outbound', label: '出库单审批' },
    { value: 'warehouse_inbound', label: '入库单审批' },
    { value: 'inventory_adjust', label: '库存调整审批' }
  ],
  mes: [
    { value: 'work_order', label: '工单审批' },
    { value: 'process_change', label: '工艺变更审批' },
    { value: 'downtime_report', label: '停机报告审批' }
  ],
  les: [
    { value: 'delivery_order', label: '配送单审批' },
    { value: 'transport_plan', label: '运输计划审批' }
  ],
  qms: [
    { value: 'quality_inspection', label: '质量检验审批' },
    { value: 'nonconformance', label: '不合格品处理审批' }
  ],
  srm: [
    { value: 'supplier_qualification', label: '供应商准入审批' },
    { value: 'supplier_evaluation', label: '供应商评估审批' }
  ],
  eam: [
    { value: 'equipment_maintenance', label: '设备维修审批' },
    { value: 'equipment_purchase', label: '设备采购审批' }
  ],
  hr: [
    { value: 'recruitment', label: '招聘审批' },
    { value: 'leave', label: '请假审批' },
    { value: 'expense', label: '报销审批' }
  ]
}

// 总体统计数据
const totalStats = reactive<Record<string, number>>({
  total: 0,
  pending: 0,
  approved: 0,
  rejected: 0
})

const statsLoading = ref(false)

// 我发起的审批列表
const initiatedList = ref<any[]>([])
const listLoading = ref(false)
const filterModule = ref('')
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 发起审批对话框
const submitDialogVisible = ref(false)
const submitLoading = ref(false)
const submitFormRef = ref<FormInstance>()
const submitForm = reactive<UnifiedApprovalRequest>({
  sourceSystem: '',
  businessType: '',
  businessId: '',
  businessNo: '',
  title: '',
  description: '',
  initiatorId: 0,
  initiatorName: '',
  callbackUrl: ''
})

// 当前来源模块对应的业务类型选项
const currentBusinessTypes = computed(() => {
  return businessTypeMap[submitForm.sourceSystem] || []
})

// 表单校验规则
const submitRules: FormRules = {
  sourceSystem: [{ required: true, message: '请选择来源业务模块', trigger: 'change' }],
  businessType: [{ required: true, message: '请选择业务类型', trigger: 'change' }],
  businessId: [{ required: true, message: '请输入业务ID', trigger: 'blur' }],
  businessNo: [{ required: true, message: '请输入业务单号', trigger: 'blur' }],
  title: [{ required: true, message: '请输入审批标题', trigger: 'blur' }]
}

// 审批详情对话框
const detailDialogVisible = ref(false)
const detailLoading = ref(false)
const approvalDetail = ref<any>(null)

// 我的待办审批列表（按当前登录用户查询待办任务）
const todoList = ref<ApprovalTask[]>([])
const todoLoading = ref(false)

// 接入公共审批操作 composable（通过 / 拒绝 待办任务，自动携带 approverId/approverName 进行权限校验）
const {
  processDialogVisible, processLoading, processForm,
  openProcessDialog, confirmProcess, getCurrentUser
} = useApprovalProcess()

/**
 * 审批详情时间线：按 nodeId 分组、nodeIndex 排序，每组显示"X/Y 已签"
 * <p>会签模式下同一节点可能有多条任务，分组后更直观展示节点流转进度。</p>
 */
const groupedTasks = computed(() => {
  const tasks = approvalDetail.value?.tasks || []
  if (!tasks.length) return []
  const groupMap = new Map<string, { nodeId: string; nodeName: string; nodeIndex: number; tasks: any[] }>()
  for (const task of tasks) {
    const key = task.nodeId || task.nodeName || 'unknown'
    if (!groupMap.has(key)) {
      groupMap.set(key, {
        nodeId: key,
        nodeName: task.nodeName || '-',
        nodeIndex: task.nodeIndex ?? 0,
        tasks: []
      })
    }
    groupMap.get(key)!.tasks.push(task)
  }
  // 按 nodeIndex 升序排序
  return Array.from(groupMap.values()).sort((a, b) => a.nodeIndex - b.nodeIndex)
})

/**
 * 获取审批统计数据（总体统计）
 */
const fetchStatistics = async () => {
  statsLoading.value = true
  try {
    const response = await unifiedApprovalApi.getStatistics()
    const data = unwrapResponseData<any>(response, {})
    // 解析总体统计（注意：使用totalInstances字段，避免与分页数据的total字段混淆）
    totalStats.total = Number(data.totalInstances ?? data.total ?? 0)
    totalStats.pending = Number(data.pending ?? data.pendingTasks ?? 0)
    totalStats.approved = Number(data.approved ?? 0)
    totalStats.rejected = Number(data.rejected ?? 0)
  } catch (error: any) {
    console.error('获取审批统计失败:', error)
    totalStats.total = 0
    totalStats.pending = 0
    totalStats.approved = 0
    totalStats.rejected = 0
    ElMessage.warning('审批统计获取失败，请稍后重试')
  } finally {
    statsLoading.value = false
  }
}

/**
 * 获取我发起的跨模块审批列表
 */
const fetchInitiatedList = async () => {
  listLoading.value = true
  try {
    const user = getCurrentUser()
    const response = await unifiedApprovalApi.getInitiatedList(user.id, {
      page: pagination.currentPage,
      size: pagination.pageSize
    })
    const data = unwrapResponseData<any>(response, {})
    const records = data.records || data.list || []
    // 按模块过滤
    initiatedList.value = filterModule.value
      ? records.filter((item: any) => isModuleMatch(item, filterModule.value))
      : records
    pagination.total = Number(data.total || initiatedList.value.length)
  } catch (error: any) {
    console.error('获取我发起的审批列表失败:', error)
    initiatedList.value = []
    pagination.total = 0
    const status = error?.response?.status ?? error?.data?.code
    if (status === 401) {
      ElMessage.error('登录状态已失效，请重新登录')
    } else {
      ElMessage.error(error?.response?.data?.msg || error?.message || '获取审批列表失败')
    }
  } finally {
    listLoading.value = false
  }
}

/**
 * 判断审批实例是否属于指定模块（通过processCode或来源系统判断）
 */
const isModuleMatch = (item: any, moduleCode: string): boolean => {
  const code = String(item?.processCode || '').toLowerCase()
  return code.startsWith(moduleCode) || code.includes(`_${moduleCode}_`) || code === moduleCode
}

/**
 * 获取我的待办审批列表（按当前登录用户ID查询待办任务）
 */
const fetchTodoList = async () => {
  todoLoading.value = true
  try {
    const user = getCurrentUser()
    const response = await unifiedApprovalApi.getTodoList(user.id, { page: 1, size: 50 })
    const data = unwrapResponseData<any>(response, {})
    todoList.value = data.records || data.list || []
  } catch (error: any) {
    console.error('获取待办审批列表失败:', error)
    todoList.value = []
    const status = error?.response?.status ?? error?.data?.code
    if (status === 401) {
      ElMessage.error('登录状态已失效，请重新登录')
    } else {
      ElMessage.error(error?.response?.data?.msg || error?.message || '获取待办列表失败')
    }
  } finally {
    todoLoading.value = false
  }
}

/**
 * 审批操作成功后的回调（乐观更新 + 并行刷新）
 * <p>立即从待办列表中移除已处理任务，然后并行刷新全部统计数据。
 * 乐观更新可避免等待后端响应期间用户重复点击，提升交互反馈速度。</p>
 */
const onApprovalSuccess = async () => {
  // 乐观更新：立即从待办列表中移除已处理任务
  const processedTaskId = processForm.taskId
  if (processedTaskId) {
    todoList.value = todoList.value.filter((task) => task.id !== processedTaskId)
  }
  // 并行刷新统计数据与列表，确保数据最终一致
  await Promise.all([
    fetchTodoList(),
    fetchStatistics(),
    fetchInitiatedList()
  ])
}

/**
 * 从审批实例中提取业务单号（优先从formData中获取）
 */
const extractBusinessNo = (instance: any): string => {
  if (!instance) return ''
  if (instance.businessNo) return instance.businessNo
  // 尝试从processVariables或formData中提取
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
 * 打开发起审批对话框
 */
const openSubmitDialog = () => {
  const user = getCurrentUser()
  submitForm.sourceSystem = ''
  submitForm.businessType = ''
  submitForm.businessId = ''
  submitForm.businessNo = ''
  submitForm.title = ''
  submitForm.description = ''
  submitForm.callbackUrl = ''
  submitForm.initiatorId = user.id
  submitForm.initiatorName = user.name
  submitDialogVisible.value = true
}

/**
 * 来源模块变化时清空业务类型
 */
const onSourceSystemChange = () => {
  submitForm.businessType = ''
}

/**
 * 提交跨模块审批申请
 */
const submitApproval = async () => {
  if (!submitFormRef.value) return
  await submitFormRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      const user = getCurrentUser()
      const payload: UnifiedApprovalRequest = {
        ...submitForm,
        initiatorId: user.id,
        initiatorName: user.name,
        formData: {
          businessNo: submitForm.businessNo,
          businessId: submitForm.businessId,
          businessType: submitForm.businessType
        }
      }
      const response = await unifiedApprovalApi.submit(payload)
      const result = unwrapResponseData<any>(response, null)
      if (result) {
        ElMessage.success('跨模块审批申请提交成功')
        submitDialogVisible.value = false
        fetchInitiatedList()
        fetchStatistics()
      } else {
        ElMessage.warning('审批申请已提交，但未返回实例信息')
      }
    } catch (error: any) {
      console.error('提交审批申请失败:', error)
      ElMessage.error(error?.response?.data?.msg || error?.message || '提交审批申请失败')
    } finally {
      submitLoading.value = false
    }
  })
}

/**
 * 查看审批详情
 */
const viewDetail = async (row: any) => {
  if (!row?.id) {
    ElMessage.error('无效的审批实例ID')
    return
  }
  detailDialogVisible.value = true
  detailLoading.value = true
  approvalDetail.value = null
  try {
    const response = await unifiedApprovalApi.getDetail(row.id)
    const data = unwrapResponseData<any>(response, {})
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
 * 按模块过滤审批列表
 */
const filterByModule = (moduleCode: string) => {
  filterModule.value = filterModule.value === moduleCode ? '' : moduleCode
  pagination.currentPage = 1
  fetchInitiatedList()
}

/**
 * 刷新全部数据（统计 + 发起列表 + 待办列表，并行执行提升性能）
 */
const refreshAll = async () => {
  await Promise.all([
    fetchStatistics(),
    fetchInitiatedList(),
    fetchTodoList()
  ])
}

/**
 * 分页大小变化
 */
const handleSizeChange = (size: number) => {
  pagination.pageSize = size
  pagination.currentPage = 1
  fetchInitiatedList()
}

/**
 * 页码变化
 */
const handleCurrentChange = (current: number) => {
  pagination.currentPage = current
  fetchInitiatedList()
}

/**
 * 获取模块标签文本
 */
const getModuleLabel = (processCode: string): string => {
  if (!processCode) return '通用'
  const code = String(processCode).toUpperCase()
  const matched = moduleOptions.find((m) => code.startsWith(m.value.toUpperCase()))
  return matched ? (matched.label.split(' ')[0] ?? '通用') : '通用'
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
 * 获取时间线节点类型
 */
const getTimelineType = (status?: string): string => {
  const statusStr = status || ''
  const typeMap: Record<string, string> = {
    approved: 'success', rejected: 'danger',
    pending: 'warning', cancelled: 'info', completed: 'success'
  }
  return typeMap[statusStr] || 'info'
}

/**
 * 统计节点分组中已签（completed/approved）的任务数
 */
const getSignedCount = (group: { tasks: any[] }): number => {
  return group.tasks.filter((t) => t.status === 'completed' || t.status === 'approved').length
}

/**
 * 判断节点分组整体状态：有拒绝→danger，全部已签→success，否则→warning
 */
const getGroupStatus = (group: { tasks: any[] }): string => {
  if (group.tasks.some((t) => t.status === 'rejected')) return 'rejected'
  if (group.tasks.some((t) => t.status === 'cancelled')) return 'cancelled'
  if (group.tasks.every((t) => t.status === 'completed' || t.status === 'approved')) return 'approved'
  return 'pending'
}

/**
 * 获取节点分组的时间线类型
 */
const getGroupTimelineType = (group: { tasks: any[] }): string => {
  const status = getGroupStatus(group)
  const typeMap: Record<string, string> = {
    approved: 'success', rejected: 'danger',
    pending: 'warning', cancelled: 'info'
  }
  return typeMap[status] || 'info'
}

/**
 * 获取节点分组的标签类型
 */
const getGroupTagType = (group: { tasks: any[] }): string => {
  const status = getGroupStatus(group)
  const typeMap: Record<string, string> = {
    approved: 'success', rejected: 'danger',
    pending: 'warning', cancelled: 'info'
  }
  return typeMap[status] || 'info'
}

// 初始化加载数据
onMounted(() => {
  refreshAll()
})
</script>

<style scoped lang="scss">
.cross-module-approval-view {
  padding: 20px;
  box-sizing: border-box;
}

/* 概述横幅 */
.overview-banner {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  padding: 24px;
  border-radius: 12px;
  margin-bottom: 24px;
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.3);

  .banner-content {
    flex: 1;

    .banner-title {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 20px;
      font-weight: 600;
      margin-bottom: 8px;

      .banner-icon {
        font-size: 24px;
      }
    }

    .banner-desc {
      font-size: 13px;
      opacity: 0.9;
      line-height: 1.6;
      max-width: 700px;
    }
  }

  .banner-actions {
    display: flex;
    gap: 12px;
  }
}

/* 总体统计卡片 */
.total-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  margin-bottom: 24px;

  .total-stat-card {
    :deep(.el-card__body) {
      padding: 20px;
    }
  }

  .stat-content {
    display: flex;
    align-items: center;
    gap: 16px;
  }

  .stat-icon {
    width: 48px;
    height: 48px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 24px;

    &.total { background: #ecf5ff; color: #409eff; }
    &.pending { background: #fdf6ec; color: #e6a23c; }
    &.approved { background: #f0f9eb; color: #67c23a; }
    &.rejected { background: #fef0f0; color: #f56c6c; }
  }

  .stat-info {
    .stat-value {
      font-size: 28px;
      font-weight: 700;
      color: #303133;
      line-height: 1.2;
    }

    .stat-label {
      font-size: 13px;
      color: #909399;
      margin-top: 4px;
    }
  }
}

/* 区块标题 */
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  margin-top: 8px;

  .section-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 16px;
    font-weight: 600;
    color: #303133;
    margin: 0;

    .el-icon {
      color: #409eff;
    }
  }

  .section-subtitle {
    font-size: 12px;
    color: #909399;
  }

  .section-filters {
    display: flex;
    gap: 8px;
    align-items: center;
  }
}

/* 分页 */
.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

/* 详情对话框相关 */
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
    margin-bottom: 4px;

    .timeline-node {
      font-weight: 600;
      color: #303133;
    }
  }

  .timeline-assignee {
    font-size: 13px;
    color: #606266;
    margin-bottom: 2px;

    .timeline-time {
      font-size: 12px;
      color: #c0c4cc;
      margin-left: 8px;
    }
  }

  .timeline-comment {
    font-size: 13px;
    color: #909399;
    background: #f5f7fa;
    padding: 6px 10px;
    border-radius: 4px;
    margin-top: 4px;
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .cross-module-approval-view {
    padding: 12px;
  }

  .overview-banner {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
    padding: 16px;

    .banner-desc {
      font-size: 12px;
    }
  }
}
</style>
