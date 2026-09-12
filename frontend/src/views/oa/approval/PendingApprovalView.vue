<template>
  <div class="pending-approval-view">
    <!-- 搜索和筛选区域 -->
    <div class="search-filter">
      <el-form :inline="true" :model="searchParams" class="filter-form">
        <el-form-item label="流程类型">
          <el-select v-model="searchParams.processType" placeholder="选择流程类型" size="small" clearable>
            <el-option label="全部" value="" />
            <el-option label="请假申请" value="leave" />
            <el-option label="报销申请" value="expense" />
            <el-option label="采购申请" value="purchase" />
            <el-option label="合同审批" value="contract" />
            <el-option label="用章申请" value="seal" />
            <el-option label="出差申请" value="travel" />
          </el-select>
        </el-form-item>
        <el-form-item label="发起人">
          <el-input v-model="searchParams.initiator" placeholder="输入发起人姓名" size="small" clearable />
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
          <el-button @click="fetchApprovalList" size="small">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 审批列表 -->
    <div class="approval-list">
      <!-- 加载状态 -->
      <div v-if="loading" class="loading-state">
        <el-skeleton :rows="5" animated />
        <div class="loading-text">正在加载待审批任务...</div>
      </div>

      <!-- 空状态 -->
      <el-empty
        v-else-if="approvalList.length === 0"
        description="暂无待审批任务"
        :image-size="120">
        <template #image>
          <div class="empty-image">
            <el-icon size="120" color="#c0c4cc">
              <Document />
            </el-icon>
          </div>
        </template>
        <el-button type="primary" @click="fetchApprovalList" size="large">
          <el-icon><Refresh /></el-icon>
          刷新列表
        </el-button>
      </el-empty>

      <!-- 审批卡片列表 -->
      <el-card
        v-else
        class="approval-card"
        v-for="(item, index) in filteredList"
        :key="item.id || index"
        shadow="hover"
      >
        <div class="card-content">
          <div class="approval-header">
            <div class="approval-title">
              <span class="title-text">{{ item.name || '未命名任务' }}</span>
              <el-tag type="warning">待审批</el-tag>
            </div>
            <div class="approval-meta">
              <span class="initiator">审批人：{{ item.assigneeName || '-' }}</span>
              <span class="create-time">{{ item.createTime || '-' }}</span>
            </div>
          </div>
          <div class="approval-body">
            <div class="approval-info">
              <div class="info-item">
                <el-icon><Calendar /></el-icon>
                <span>{{ item.description ? String(item.description).trim() : '审批事项' }}</span>
              </div>
              <div class="info-item">
                <el-icon><Clock /></el-icon>
                <span>截止时间：{{ item.dueTime ? String(item.dueTime).trim() : '无' }}</span>
              </div>
              <div class="info-item" v-if="item.assigneeRole">
                <el-icon><User /></el-icon>
                <el-tag size="small" type="info">要求角色：{{ item.assigneeRole }}</el-tag>
              </div>
            </div>
            <div class="approval-preview">
              <el-descriptions :column="2" size="small">
                <el-descriptions-item label="任务名称">{{ item.name || '-' }}</el-descriptions-item>
                <el-descriptions-item label="节点名称">{{ item.nodeName || '-' }}</el-descriptions-item>
                <el-descriptions-item label="节点序号">{{ item.nodeIndex ?? '-' }}</el-descriptions-item>
                <el-descriptions-item label="创建时间">{{ item.createTime || '-' }}</el-descriptions-item>
              </el-descriptions>
            </div>
          </div>
          <div class="approval-footer">
            <el-button type="success" size="small" @click="handleApprove(item)">
              <el-icon><Check /></el-icon>
              同意
            </el-button>
            <el-button type="danger" size="small" @click="handleReject(item)">
              <el-icon><Close /></el-icon>
              拒绝
            </el-button>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 分页 -->
    <div class="pagination" v-if="!loading && approvalList.length > 0">
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

    <!-- 审批操作对话框（公共组件，携带 approverId 进行权限校验） -->
    <ApprovalProcessDialog
      v-model:visible="processDialogVisible"
      :loading="processLoading"
      :action="processForm.action"
      @confirm="(comment: string) => confirmProcess(comment, fetchApprovalList)"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { Search, Refresh, Calendar, Clock, User, Check, Close, Document } from '@element-plus/icons-vue'
import { unwrapResponseData } from '@/api'
import { unifiedApprovalApi } from '@/api/oa'
import type { ApprovalTask } from '@/api/oa'
import { ElMessage } from 'element-plus'
import { useApprovalProcess } from '@/composables/useApprovalProcess'
import ApprovalProcessDialog from '@/components/oa/ApprovalProcessDialog.vue'

/** 搜索参数（前端过滤，统一审批待办接口暂不支持后端筛选） */
const searchParams = reactive({
  processType: '',
  initiator: ''
})

/** 分页参数 */
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

/** 审批列表数据（来自统一审批待办接口） */
const approvalList = ref<ApprovalTask[]>([])
const loading = ref(false)

/** 接入公共审批操作 composable（通过 / 拒绝，自动携带 approverId/approverName） */
const {
  processDialogVisible, processLoading, processForm,
  openProcessDialog, confirmProcess, getCurrentUser
} = useApprovalProcess()

/**
 * 前端过滤后的列表（按流程类型 / 发起人筛选）
 * <p>统一审批待办接口 getTodoList 仅按审批人ID查询，不支持后端条件过滤，
 * 因此在前端基于 nodeName/name/assigneeName 等字段做客户端筛选。</p>
 */
const filteredList = computed(() => {
  return approvalList.value.filter((item) => {
    // 流程类型过滤：匹配 nodeId / nodeName
    if (searchParams.processType) {
      const keyword = searchParams.processType.toLowerCase()
      const nodeStr = `${item.nodeId || ''} ${item.nodeName || ''}`.toLowerCase()
      if (!nodeStr.includes(keyword)) return false
    }
    // 发起人过滤：由于待办任务字段中无直接发起人，使用 assigneeName 兜底匹配
    if (searchParams.initiator) {
      const initiator = String(item.assigneeName || '').toLowerCase()
      if (!initiator.includes(searchParams.initiator.toLowerCase())) return false
    }
    return true
  })
})

/**
 * 获取待审批列表数据（调用统一审批待办接口，按当前登录用户ID查询）
 */
const fetchApprovalList = async () => {
  loading.value = true
  try {
    const userId = getCurrentUser().id
    const response = await unifiedApprovalApi.getTodoList(userId, {
      page: pagination.currentPage,
      size: pagination.pageSize
    })
    const data = unwrapResponseData<any>(response, {}) || {}
    approvalList.value = data.records || data.list || []
    pagination.total = Number(data.total ?? approvalList.value.length)

    if (approvalList.value.length === 0 && pagination.currentPage === 1) {
      ElMessage.info('当前没有待审批任务')
    }
  } catch (error: any) {
    console.error('获取待审批列表失败:', error)
    approvalList.value = []
    pagination.total = 0

    const status = error?.response?.status ?? error?.data?.code
    if (status === 401) {
      ElMessage.error('登录状态已失效，请重新登录后查看待审批任务')
      return
    }
    if (status === 403) {
      ElMessage.error('当前账号无权查看待审批任务')
      return
    }
    ElMessage.error(error?.response?.data?.msg || error?.message || '获取待审批列表失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

/** 搜索 */
const search = () => {
  pagination.currentPage = 1
  fetchApprovalList()
}

/** 重置筛选 */
const reset = () => {
  Object.assign(searchParams, {
    processType: '',
    initiator: ''
  })
  pagination.currentPage = 1
  fetchApprovalList()
}

/** 分页大小变化 */
const handleSizeChange = (size: number) => {
  pagination.pageSize = size
  pagination.currentPage = 1
  fetchApprovalList()
}

/** 页码变化 */
const handleCurrentChange = (current: number) => {
  pagination.currentPage = current
  fetchApprovalList()
}

/**
 * 同意审批（打开公共审批对话框）
 */
const handleApprove = (item: ApprovalTask) => {
  if (!item?.id) {
    ElMessage.error('无效的审批任务ID')
    return
  }
  openProcessDialog(item, 'approve')
}

/**
 * 拒绝审批（打开公共审批对话框）
 */
const handleReject = (item: ApprovalTask) => {
  if (!item?.id) {
    ElMessage.error('无效的审批任务ID')
    return
  }
  openProcessDialog(item, 'reject')
}

/** 初始加载 */
onMounted(() => {
  fetchApprovalList()
})
</script>

<style scoped>
.pending-approval-view {
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

.loading-state {
  text-align: center;
}

.loading-text {
  margin-top: 16px;
  color: #909399;
  font-size: 14px;
}

.empty-image {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 16px;
}

@media (max-width: 768px) {
  .pending-approval-view {
    padding: 12px;
  }

  .filter-form {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }

  .approval-info {
    flex-direction: column;
    gap: 10px;
  }

  .approval-footer {
    flex-direction: column;
    gap: 8px;
  }

  .approval-footer .el-button {
    width: 100%;
  }
}
</style>
