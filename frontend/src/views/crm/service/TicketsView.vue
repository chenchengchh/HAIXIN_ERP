<template>
  <div class="tickets-view">
    <!-- 页面操作栏 -->
    <div class="page-actions">
      <el-input
        v-model="searchForm.keyword"
        placeholder="搜索工单编号、客户名称"
        style="width: 300px; margin-right: 10px;"
        clearable
      >
        <template #append>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-button type="primary" @click="handleSearch">
        <el-icon><Search /></el-icon> 搜索
      </el-button>
      <el-button type="primary" @click="handleCreate">
        <el-icon><Plus /></el-icon> 新建工单
      </el-button>
      <el-button @click="handleExport">
        <el-icon><Download /></el-icon> 导出
      </el-button>
    </div>

    <!-- 搜索条件展开面板 -->
    <el-collapse v-model="activeSearchPanel" style="margin-bottom: 20px;">
      <el-collapse-item title="高级搜索" name="1">
        <div class="advanced-search">
          <el-form :model="searchForm" inline>
            <el-form-item label="工单类型">
              <el-select v-model="searchForm.category" placeholder="选择工单类型" clearable>
                <el-option label="投诉" value="complaint" />
                <el-option label="咨询" value="consultation" />
                <el-option label="维修" value="repair" />
                <el-option label="退货" value="return" />
              </el-select>
            </el-form-item>
            <el-form-item label="优先级">
              <el-select v-model="searchForm.priority" placeholder="选择优先级" clearable>
                <el-option label="低" value="LOW" />
                <el-option label="中" value="MEDIUM" />
                <el-option label="高" value="HIGH" />
                <el-option label="紧急" value="URGENT" />
              </el-select>
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="searchForm.status" placeholder="选择状态" clearable>
                <el-option label="待处理" value="OPEN" />
                <el-option label="已分配" value="ASSIGNED" />
                <el-option label="已回复" value="REPLIED" />
                <el-option label="已关闭" value="CLOSED" />
              </el-select>
            </el-form-item>
            <el-form-item label="工单日期">
              <el-date-picker
                v-model="searchForm.dateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSearch">搜索</el-button>
              <el-button @click="handleReset">重置</el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-collapse-item>
    </el-collapse>

    <!-- 工单列表 -->
    <el-card shadow="never" class="tickets-table-card">
      <el-table
        v-loading="loading"
        :data="ticketsList"
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="ticketNo" label="工单编号" min-width="150" sortable />
        <el-table-column prop="customerName" label="客户名称" min-width="180" />
        <el-table-column prop="category" label="工单类型" width="120">
          <template #default="scope">
            {{ getCategoryLabel(scope.row.category) }}
          </template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="100">
          <template #default="scope">
            <el-tag :type="getPriorityTagType(scope.row.priority)">
              {{ getPriorityLabel(scope.row.priority) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="200" />
        <el-table-column prop="status" label="状态" width="120">
          <template #default="scope">
            <el-tag :type="getStatusTagType(scope.row.status)">
              {{ getStatusLabel(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="提交时间" width="180" sortable>
          <template #default="scope">
            {{ formatTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="contactName" label="联系人" width="120" />
        <el-table-column prop="assigneeName" label="处理人" width="120">
          <template #default="scope">
            {{ scope.row.assigneeName || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="330" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="handleView(scope.row)">查看</el-button>
            <el-button size="small" type="primary" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button v-if="scope.row.status !== 'CLOSED'" size="small" type="success" @click="handleAssign(scope.row)">分配</el-button>
            <el-button v-if="scope.row.status !== 'CLOSED'" size="small" type="warning" @click="handleReply(scope.row)">回复</el-button>
            <el-button v-if="scope.row.status !== 'CLOSED'" size="small" type="danger" @click="handleClose(scope.row)">关闭</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
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
    </el-card>

    <!-- 查看工单详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="工单详情" width="640px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="工单编号">{{ currentTicket?.ticketNo }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusTagType(currentTicket?.status)">{{ getStatusLabel(currentTicket?.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="标题" :span="2">{{ currentTicket?.title }}</el-descriptions-item>
        <el-descriptions-item label="客户名称">{{ currentTicket?.customerName }}</el-descriptions-item>
        <el-descriptions-item label="工单类型">{{ getCategoryLabel(currentTicket?.category) }}</el-descriptions-item>
        <el-descriptions-item label="联系人">{{ currentTicket?.contactName }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ currentTicket?.phone }}</el-descriptions-item>
        <el-descriptions-item label="优先级">
          <el-tag :type="getPriorityTagType(currentTicket?.priority)">{{ getPriorityLabel(currentTicket?.priority) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="处理人">{{ currentTicket?.assigneeName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="提交时间">{{ formatTime(currentTicket?.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="关闭时间">{{ formatTime(currentTicket?.closedAt) || '-' }}</el-descriptions-item>
        <el-descriptions-item label="问题描述" :span="2">{{ currentTicket?.description || '-' }}</el-descriptions-item>
      </el-descriptions>
      <!-- 回复记录 -->
      <div v-if="ticketReplies.length > 0" style="margin-top: 16px;">
        <h4 style="margin-bottom: 10px;">回复记录</h4>
        <el-timeline>
          <el-timeline-item
            v-for="(reply, index) in ticketReplies"
            :key="index"
            :timestamp="reply.time || ''"
            placement="top"
          >
            <p><strong>{{ reply.replyBy || '客服' }}：</strong>{{ reply.content }}</p>
          </el-timeline-item>
        </el-timeline>
      </div>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 新建/编辑工单对话框 -->
    <el-dialog
      v-model="editDialogVisible"
      :title="isEditMode ? '编辑工单' : '新建工单'"
      width="600px"
    >
      <el-form :model="ticketForm" :rules="ticketFormRules" ref="ticketFormRef" label-width="100px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="ticketForm.title" placeholder="请输入工单标题" />
        </el-form-item>
        <el-form-item label="客户名称" prop="customerName">
          <el-input v-model="ticketForm.customerName" placeholder="请输入客户名称" />
        </el-form-item>
        <el-form-item label="联系人">
          <el-input v-model="ticketForm.contactName" placeholder="请输入联系人" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="ticketForm.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="工单类型" prop="category">
          <el-select v-model="ticketForm.category" placeholder="请选择工单类型" style="width: 100%;">
            <el-option label="投诉" value="complaint" />
            <el-option label="咨询" value="consultation" />
            <el-option label="维修" value="repair" />
            <el-option label="退货" value="return" />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级" prop="priority">
          <el-select v-model="ticketForm.priority" placeholder="请选择优先级" style="width: 100%;">
            <el-option label="低" value="LOW" />
            <el-option label="中" value="MEDIUM" />
            <el-option label="高" value="HIGH" />
            <el-option label="紧急" value="URGENT" />
          </el-select>
        </el-form-item>
        <el-form-item label="问题描述">
          <el-input v-model="ticketForm.description" type="textarea" :rows="3" placeholder="请输入问题描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSaveTicket">保存</el-button>
      </template>
    </el-dialog>

    <!-- 分配工单对话框 -->
    <el-dialog v-model="assignDialogVisible" title="分配工单" width="480px">
      <el-form :model="assignForm" :rules="assignFormRules" ref="assignFormRef" label-width="100px">
        <el-form-item label="工单编号">
          <el-input :model-value="currentTicket?.ticketNo" disabled />
        </el-form-item>
        <el-form-item label="处理人ID" prop="assigneeId">
          <el-input-number v-model="assignForm.assigneeId" :min="1" placeholder="请输入处理人ID" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="处理人姓名" prop="assigneeName">
          <el-input v-model="assignForm.assigneeName" placeholder="请输入处理人姓名" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assignDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleConfirmAssign">确定分配</el-button>
      </template>
    </el-dialog>

    <!-- 回复工单对话框 -->
    <el-dialog v-model="replyDialogVisible" title="回复工单" width="520px">
      <el-form :model="replyForm" :rules="replyFormRules" ref="replyFormRef" label-width="100px">
        <el-form-item label="工单编号">
          <el-input :model-value="currentTicket?.ticketNo" disabled />
        </el-form-item>
        <el-form-item label="回复人" prop="replyBy">
          <el-input v-model="replyForm.replyBy" placeholder="请输入回复人姓名" />
        </el-form-item>
        <el-form-item label="回复内容" prop="content">
          <el-input v-model="replyForm.content" type="textarea" :rows="4" placeholder="请输入回复内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="replyDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleConfirmReply">提交回复</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { Search, Plus, Download } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { serviceApi } from '../../../api/crm/service'
import { unwrapPageResponse } from '../../../api'

// 工单列表数据
const ticketsList = ref<any[]>([])

// 加载状态
const loading = ref(false)

// 提交加载状态
const submitLoading = ref(false)

// 分页数据
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 搜索表单
const searchForm = reactive({
  keyword: '',
  category: '',
  priority: '',
  status: '',
  dateRange: [] as string[]
})

// 搜索面板展开状态
const activeSearchPanel = ref<string[]>([])

// 当前操作的工单
const currentTicket = ref<any>(null)

// 详情对话框显示状态
const detailDialogVisible = ref(false)

// 工单回复记录（从 replies JSON 字符串解析）
const ticketReplies = ref<any[]>([])

// 编辑/新建对话框状态
const editDialogVisible = ref(false)
const isEditMode = ref(false)
const ticketFormRef = ref()

// 工单表单
const ticketForm = reactive({
  id: undefined as number | undefined,
  title: '',
  customerName: '',
  contactName: '',
  phone: '',
  category: 'consultation',
  priority: 'MEDIUM',
  description: ''
})

// 工单表单校验规则
const ticketFormRules = reactive({
  title: [{ required: true, message: '请输入工单标题', trigger: 'blur' }],
  customerName: [{ required: true, message: '请输入客户名称', trigger: 'blur' }],
  category: [{ required: true, message: '请选择工单类型', trigger: 'change' }],
  priority: [{ required: true, message: '请选择优先级', trigger: 'change' }]
})

// 分配对话框状态
const assignDialogVisible = ref(false)
const assignFormRef = ref()

// 分配表单
const assignForm = reactive({
  assigneeId: undefined as number | undefined,
  assigneeName: ''
})

// 分配表单校验规则
const assignFormRules = reactive({
  assigneeId: [{ required: true, message: '请输入处理人ID', trigger: 'blur' }],
  assigneeName: [{ required: true, message: '请输入处理人姓名', trigger: 'blur' }]
})

// 回复对话框状态
const replyDialogVisible = ref(false)
const replyFormRef = ref()

// 回复表单
const replyForm = reactive({
  replyBy: '',
  content: ''
})

// 回复表单校验规则
const replyFormRules = reactive({
  replyBy: [{ required: true, message: '请输入回复人姓名', trigger: 'blur' }],
  content: [{ required: true, message: '请输入回复内容', trigger: 'blur' }]
})

/**
 * 获取工单列表（对接后端分页接口 GET /api/v1/crm/tickets/my）
 */
const fetchTicketsList = async () => {
  loading.value = true
  try {
    const params: any = {
      page: pagination.currentPage,
      size: pagination.pageSize
    }
    // 后端仅支持 assigneeName/status 查询参数，状态筛选传给后端
    if (searchForm.status) {
      params.status = searchForm.status
    }
    const response = await serviceApi.getMyTickets(params)
    const { list, total } = unwrapPageResponse<any>(response)
    ticketsList.value = list
    pagination.total = total
  } catch (error) {
    console.error('获取工单列表失败:', error)
    ElMessage.error('获取工单列表失败')
    ticketsList.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

/**
 * 获取工单类型标签
 * @param category 工单分类编码
 */
const getCategoryLabel = (category: string) => {
  const labelMap: Record<string, string> = {
    complaint: '投诉',
    consultation: '咨询',
    repair: '维修',
    return: '退货'
  }
  return labelMap[category] || category || '-'
}

/**
 * 获取优先级标签类型
 * @param priority 优先级（LOW/MEDIUM/HIGH/URGENT）
 */
const getPriorityTagType = (priority: string) => {
  const typeMap: Record<string, string> = {
    LOW: 'info',
    MEDIUM: 'warning',
    HIGH: 'danger',
    URGENT: 'danger'
  }
  return typeMap[priority] || 'info'
}

/**
 * 获取优先级标签
 * @param priority 优先级（LOW/MEDIUM/HIGH/URGENT）
 */
const getPriorityLabel = (priority: string) => {
  const labelMap: Record<string, string> = {
    LOW: '低',
    MEDIUM: '中',
    HIGH: '高',
    URGENT: '紧急'
  }
  return labelMap[priority] || priority || '-'
}

/**
 * 获取状态标签类型
 * @param status 工单状态（OPEN/ASSIGNED/REPLIED/CLOSED）
 */
const getStatusTagType = (status: string) => {
  const typeMap: Record<string, string> = {
    OPEN: 'danger',
    ASSIGNED: 'warning',
    REPLIED: 'primary',
    CLOSED: 'success'
  } as Record<string, string>
  return typeMap[status] || 'info'
}

/**
 * 获取状态标签
 * @param status 工单状态（OPEN/ASSIGNED/REPLIED/CLOSED）
 */
const getStatusLabel = (status: string) => {
  const labelMap: Record<string, string> = {
    OPEN: '待处理',
    ASSIGNED: '已分配',
    REPLIED: '已回复',
    CLOSED: '已关闭'
  }
  return labelMap[status] || status || '-'
}

/**
 * 格式化时间显示
 * @param time 后端返回的时间字符串
 */
const formatTime = (time: string | undefined | null) => {
  if (!time) return ''
  // 将 ISO 格式（2026-07-30T10:30:00）转为常规展示格式
  return String(time).replace('T', ' ').substring(0, 19)
}

/**
 * 处理搜索：重置页码并重新加载列表
 */
const handleSearch = () => {
  pagination.currentPage = 1
  fetchTicketsList()
}

/**
 * 处理重置：清空搜索条件并重新加载列表
 */
const handleReset = () => {
  Object.assign(searchForm, {
    keyword: '',
    category: '',
    priority: '',
    status: '',
    dateRange: []
  })
  pagination.currentPage = 1
  fetchTicketsList()
}

/**
 * 处理新建：打开新建工单对话框并重置表单
 */
const handleCreate = () => {
  isEditMode.value = false
  Object.assign(ticketForm, {
    id: undefined,
    title: '',
    customerName: '',
    contactName: '',
    phone: '',
    category: 'consultation',
    priority: 'MEDIUM',
    description: ''
  })
  editDialogVisible.value = true
}

/**
 * 处理查看：打开详情对话框并解析回复记录
 * @param row 工单行数据
 */
const handleView = (row: any) => {
  currentTicket.value = row
  // 后端 replies 字段为 JSON 数组字符串，此处解析为数组
  try {
    ticketReplies.value = row.replies ? JSON.parse(row.replies) : []
  } catch (e) {
    console.warn('解析工单回复记录失败:', e)
    ticketReplies.value = []
  }
  detailDialogVisible.value = true
}

/**
 * 处理编辑：打开编辑对话框并回填表单数据
 * @param row 工单行数据
 */
const handleEdit = (row: any) => {
  isEditMode.value = true
  Object.assign(ticketForm, {
    id: row.id,
    title: row.title || '',
    customerName: row.customerName || '',
    contactName: row.contactName || '',
    phone: row.phone || '',
    category: row.category || 'consultation',
    priority: row.priority || 'MEDIUM',
    description: row.description || ''
  })
  editDialogVisible.value = true
}

/**
 * 保存工单：新建调用 POST /tickets，编辑调用 PUT /tickets/{id}
 */
const handleSaveTicket = () => {
  ticketFormRef.value.validate(async (valid: boolean) => {
    if (!valid) return
    submitLoading.value = true
    try {
      const data = {
        title: ticketForm.title,
        customerName: ticketForm.customerName,
        contactName: ticketForm.contactName,
        phone: ticketForm.phone,
        category: ticketForm.category,
        priority: ticketForm.priority,
        description: ticketForm.description
      }
      if (isEditMode.value && ticketForm.id !== undefined) {
        await serviceApi.updateTicket(ticketForm.id, data)
        ElMessage.success('工单更新成功')
      } else {
        await serviceApi.createTicket(data)
        ElMessage.success('工单创建成功')
      }
      editDialogVisible.value = false
      fetchTicketsList()
    } catch (error) {
      console.error('保存工单失败:', error)
      ElMessage.error('保存工单失败')
    } finally {
      submitLoading.value = false
    }
  })
}

/**
 * 处理分配：打开分配对话框并回填当前处理人
 * @param row 工单行数据
 */
const handleAssign = (row: any) => {
  currentTicket.value = row
  Object.assign(assignForm, {
    assigneeId: row.assigneeId ?? undefined,
    assigneeName: row.assigneeName || ''
  })
  assignDialogVisible.value = true
}

/**
 * 确认分配：调用 POST /tickets/{id}/assign
 */
const handleConfirmAssign = () => {
  assignFormRef.value.validate(async (valid: boolean) => {
    if (!valid) return
    submitLoading.value = true
    try {
      await serviceApi.assignTicket(currentTicket.value.id, {
        assigneeId: assignForm.assigneeId,
        assigneeName: assignForm.assigneeName
      })
      ElMessage.success('工单分配成功')
      assignDialogVisible.value = false
      fetchTicketsList()
    } catch (error) {
      console.error('分配工单失败:', error)
      ElMessage.error('分配工单失败')
    } finally {
      submitLoading.value = false
    }
  })
}

/**
 * 处理回复：打开回复对话框
 * @param row 工单行数据
 */
const handleReply = (row: any) => {
  currentTicket.value = row
  Object.assign(replyForm, {
    replyBy: '',
    content: ''
  })
  replyDialogVisible.value = true
}

/**
 * 确认回复：调用 POST /tickets/{id}/reply
 */
const handleConfirmReply = () => {
  replyFormRef.value.validate(async (valid: boolean) => {
    if (!valid) return
    submitLoading.value = true
    try {
      await serviceApi.replyTicket(currentTicket.value.id, {
        content: replyForm.content,
        replyBy: replyForm.replyBy
      })
      ElMessage.success('回复提交成功')
      replyDialogVisible.value = false
      fetchTicketsList()
    } catch (error) {
      console.error('回复工单失败:', error)
      ElMessage.error('回复工单失败')
    } finally {
      submitLoading.value = false
    }
  })
}

/**
 * 处理关闭：确认后调用 POST /tickets/{id}/close
 * @param row 工单行数据
 */
const handleClose = (row: any) => {
  ElMessageBox.confirm(`确定要关闭工单「${row.ticketNo}」吗？关闭后不可再分配或回复。`, '关闭确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await serviceApi.closeTicket(row.id)
      ElMessage.success('工单已关闭')
      fetchTicketsList()
    } catch (error) {
      console.error('关闭工单失败:', error)
      ElMessage.error('关闭工单失败')
    }
  }).catch(() => {
    // 取消关闭操作
  })
}

/**
 * 处理导出：后端暂无导出接口，保留提示
 */
const handleExport = () => {
  ElMessage.info('导出工单功能开发中')
}

/**
 * 处理分页大小变化
 * @param size 每页条数
 */
const handleSizeChange = (size: number) => {
  pagination.pageSize = size
  pagination.currentPage = 1
  fetchTicketsList()
}

/**
 * 处理当前页码变化
 * @param current 页码
 */
const handleCurrentChange = (current: number) => {
  pagination.currentPage = current
  fetchTicketsList()
}

/**
 * 处理表格选择变化
 * @param selection 选中行数组
 */
const handleSelectionChange = (selection: any[]) => {
  console.log('选中的行:', selection)
}

// 组件挂载时初始化数据
onMounted(() => {
  fetchTicketsList()
})
</script>

<style scoped>
.tickets-view {
  padding: 20px;
  background-color: #ffffff;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.page-actions {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.advanced-search {
  background-color: #f5f7fa;
  padding: 20px;
  border-radius: 4px;
}

.tickets-table-card {
  margin-bottom: 20px;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>
