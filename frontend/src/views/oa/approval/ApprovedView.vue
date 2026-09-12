<template>
  <div class="approved-view">
    <!-- 搜索和筛选区域 -->
    <div class="search-filter">
      <el-form :inline="true" :model="searchParams" class="filter-form">
        <el-form-item label="审批结果">
          <el-select v-model="searchParams.result" placeholder="选择审批结果" size="small" clearable>
            <el-option label="全部" value="" />
            <el-option label="已同意" value="approved" />
            <el-option label="已拒绝" value="rejected" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="searchParams.keyword" placeholder="任务名称 / 节点" size="small" clearable />
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
          <el-button @click="fetchApprovedList" size="small">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 已审批列表 -->
    <div class="approval-list">
      <!-- 加载状态 -->
      <el-skeleton :rows="5" animated v-if="loading" />

      <!-- 空状态 -->
      <el-empty v-else-if="filteredList.length === 0" description="暂无已审批任务">
        <el-button type="primary" @click="fetchApprovedList">刷新列表</el-button>
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
              <span class="title-text">{{ item.name || '未命名任务' }}</span>
              <el-tag :type="getStatusTagType(item.result || item.status)">{{ getStatusText(item.result || item.status) }}</el-tag>
            </div>
            <div class="approval-meta">
              <span class="initiator">审批人：{{ item.assigneeName || '-' }}</span>
              <span class="approve-time">审批时间：{{ item.approveTime || item.updateTime || '-' }}</span>
            </div>
          </div>
          <div class="approval-body">
            <div class="approval-info">
              <div class="info-item">
                <el-icon><User /></el-icon>
                <span>{{ item.assigneeName || '-' }}</span>
              </div>
              <div class="info-item">
                <el-icon><Clock /></el-icon>
                <span>创建时间：{{ item.createTime || '-' }}</span>
              </div>
              <div class="info-item" v-if="item.assigneeRole">
                <el-icon><Stamp /></el-icon>
                <el-tag size="small" type="info">{{ item.assigneeRole }}</el-tag>
              </div>
            </div>
            <div class="approval-preview">
              <el-descriptions :column="2" size="small">
                <el-descriptions-item label="任务名称">{{ item.name || '-' }}</el-descriptions-item>
                <el-descriptions-item label="节点名称">{{ item.nodeName || '-' }}</el-descriptions-item>
                <el-descriptions-item label="审批结果">{{ getStatusText(item.result || item.status) }}</el-descriptions-item>
                <el-descriptions-item label="审批意见">{{ item.comment || '-' }}</el-descriptions-item>
              </el-descriptions>
            </div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 分页 -->
    <div class="pagination" v-if="!loading && approvedList.length > 0">
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { Search, Refresh, User, Clock, Stamp } from '@element-plus/icons-vue'
import { unwrapResponseData } from '@/api'
import { unifiedApprovalApi } from '@/api/oa'
import type { ApprovalTask } from '@/api/oa'
import { ElMessage } from 'element-plus'
import { useApprovalProcess } from '@/composables/useApprovalProcess'

/** 搜索参数（前端过滤，统一审批已办接口暂不支持后端筛选） */
const searchParams = reactive({
  result: '',
  keyword: ''
})

/** 分页参数 */
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

/** 已审批列表数据（来自统一审批已办接口） */
const approvedList = ref<ApprovalTask[]>([])
const loading = ref(false)

/** 接入公共审批操作 composable（复用 getCurrentUser 获取当前用户ID） */
const { getCurrentUser } = useApprovalProcess()

/**
 * 前端过滤后的列表（按审批结果 / 关键词筛选）
 */
const filteredList = computed(() => {
  return approvedList.value.filter((item) => {
    // 审批结果过滤
    if (searchParams.result) {
      const itemResult = String(item.result || item.status || '').toLowerCase()
      if (!itemResult.includes(searchParams.result.toLowerCase())) return false
    }
    // 关键词过滤：匹配任务名称 / 节点名称
    if (searchParams.keyword) {
      const keyword = searchParams.keyword.toLowerCase()
      const matchStr = `${item.name || ''} ${item.nodeName || ''} ${item.assigneeName || ''}`.toLowerCase()
      if (!matchStr.includes(keyword)) return false
    }
    return true
  })
})

/**
 * 获取已审批列表数据（调用统一审批已办接口，按当前登录用户ID查询）
 */
const fetchApprovedList = async () => {
  loading.value = true
  try {
    const userId = getCurrentUser().id
    const response = await unifiedApprovalApi.getDoneList(userId, {
      page: pagination.currentPage,
      size: pagination.pageSize
    })
    const data = unwrapResponseData<any>(response, {}) || {}
    approvedList.value = data.records || data.list || []
    pagination.total = Number(data.total ?? approvedList.value.length)
  } catch (error: any) {
    console.error('获取已审批列表失败:', error)
    approvedList.value = []
    pagination.total = 0
    const status = error?.response?.status ?? error?.data?.code
    if (status === 401) {
      ElMessage.error('登录状态已失效，请重新登录')
      return
    }
    ElMessage.error(error?.response?.data?.msg || error?.message || '获取已审批列表失败')
  } finally {
    loading.value = false
  }
}

/** 搜索 */
const search = () => {
  pagination.currentPage = 1
  fetchApprovedList()
}

/** 重置筛选 */
const reset = () => {
  Object.assign(searchParams, {
    result: '',
    keyword: ''
  })
  pagination.currentPage = 1
  fetchApprovedList()
}

/** 分页大小变化 */
const handleSizeChange = (size: number) => {
  pagination.pageSize = size
  pagination.currentPage = 1
  fetchApprovedList()
}

/** 页码变化 */
const handleCurrentChange = (current: number) => {
  pagination.currentPage = current
  fetchApprovedList()
}

/**
 * 获取状态标签类型
 */
const getStatusTagType = (status: string) => {
  const typeMap: Record<string, string> = {
    approved: 'success',
    rejected: 'danger',
    cancelled: 'info',
    completed: 'success',
    pending: 'warning'
  }
  return typeMap[status] || 'info'
}

/**
 * 获取状态文本
 */
const getStatusText = (status: string) => {
  const statusMap: Record<string, string> = {
    approved: '已同意',
    rejected: '已拒绝',
    cancelled: '已取消',
    running: '运行中',
    completed: '已完成',
    pending: '待审批'
  }
  return statusMap[status] || status || '未知'
}

/** 初始加载 */
onMounted(() => {
  fetchApprovedList()
})
</script>

<style scoped>
.approved-view {
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
  cursor: default;
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

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

@media (max-width: 768px) {
  .approved-view {
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
}
</style>
