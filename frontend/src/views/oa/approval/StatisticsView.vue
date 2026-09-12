<template>
  <div class="statistics-view" v-loading="loading">
    <!-- 页面操作栏 -->
    <div class="page-header">
      <div class="header-actions">
        <el-button :icon="Refresh" @click="fetchStatisticsData">刷新统计</el-button>
        <el-tag type="info" size="large">数据来源：统一审批服务实时统计</el-tag>
      </div>
    </div>

    <!-- 统计卡片（真实数据） -->
    <div class="stats-cards">
      <el-card class="stats-card" shadow="hover">
        <div class="card-content">
          <div class="stats-info">
            <div class="stats-value">{{ statsData.totalApprovals }}</div>
            <div class="stats-label">审批总数</div>
          </div>
          <div class="stats-icon total">
            <el-icon><Document /></el-icon>
          </div>
        </div>
      </el-card>

      <el-card class="stats-card" shadow="hover">
        <div class="card-content">
          <div class="stats-info">
            <div class="stats-value warning">{{ statsData.pendingApprovals }}</div>
            <div class="stats-label">待审批</div>
          </div>
          <div class="stats-icon warning">
            <el-icon><Clock /></el-icon>
          </div>
        </div>
      </el-card>

      <el-card class="stats-card" shadow="hover">
        <div class="card-content">
          <div class="stats-info">
            <div class="stats-value success">{{ statsData.approvedApprovals }}</div>
            <div class="stats-label">已通过</div>
          </div>
          <div class="stats-icon success">
            <el-icon><Check /></el-icon>
          </div>
        </div>
      </el-card>

      <el-card class="stats-card" shadow="hover">
        <div class="card-content">
          <div class="stats-info">
            <div class="stats-value danger">{{ statsData.rejectedApprovals }}</div>
            <div class="stats-label">已拒绝</div>
          </div>
          <div class="stats-icon danger">
            <el-icon><Close /></el-icon>
          </div>
        </div>
      </el-card>

      <el-card class="stats-card" shadow="hover">
        <div class="card-content">
          <div class="stats-info">
            <div class="stats-value">{{ approvalRate }}%</div>
            <div class="stats-label">审批通过率</div>
          </div>
          <div class="stats-icon info">
            <el-icon><CircleCheck /></el-icon>
          </div>
        </div>
      </el-card>

      <el-card class="stats-card" shadow="hover">
        <div class="card-content">
          <div class="stats-info">
            <div class="stats-value">{{ moduleCount }}</div>
            <div class="stats-label">已接入模块数</div>
          </div>
          <div class="stats-icon total">
            <el-icon><Grid /></el-icon>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 统计图表 -->
    <el-row :gutter="20" class="charts-container">
      <!-- 各模块审批分布 -->
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="chart-header">
              <span>各业务模块审批分布</span>
            </div>
          </template>
          <div class="chart-content">
            <el-empty v-if="moduleChartData.length === 0" description="暂无模块统计数据" :image-size="80" />
            <el-descriptions v-else :column="1" border>
              <el-descriptions-item
                :label="item.label"
                v-for="(item, index) in moduleChartData"
                :key="index"
              >
                <div class="progress-item">
                  <div class="progress-label">
                    <span class="progress-value">{{ item.value }} ({{ getPercentage(item.value) }}%)</span>
                  </div>
                  <el-progress :percentage="getPercentage(item.value)" :color="getProgressColor(index)" />
                </div>
              </el-descriptions-item>
            </el-descriptions>
          </div>
        </el-card>
      </el-col>

      <!-- 审批状态分布 -->
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="chart-header">
              <span>审批状态分布</span>
            </div>
          </template>
          <div class="chart-content">
            <el-row :gutter="20">
              <el-col :span="12" v-for="(item, index) in statusChartData" :key="index">
                <div class="status-item">
                  <el-statistic :value="item.value" :title="item.label" />
                  <el-tag :type="getStatusTagType(item.label)" style="margin-top: 8px;">{{ item.label }}</el-tag>
                </div>
              </el-col>
            </el-row>
          </div>
        </el-card>
      </el-col>

      <!-- 模块审批明细表 -->
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header>
            <div class="chart-header">
              <span>各业务模块审批明细</span>
            </div>
          </template>
          <div class="chart-content">
            <el-table :data="moduleDetailList" stripe style="width: 100%">
              <el-table-column prop="label" label="业务模块" min-width="120" />
              <el-table-column prop="total" label="审批总数" width="120" align="center" />
              <el-table-column prop="pending" label="待审批" width="120" align="center">
                <template #default="scope">
                  <el-tag type="warning">{{ scope.row.pending }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="approved" label="已通过" width="120" align="center">
                <template #default="scope">
                  <el-tag type="success">{{ scope.row.approved }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="rejected" label="已拒绝" width="120" align="center">
                <template #default="scope">
                  <el-tag type="danger">{{ scope.row.rejected }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="approvalRate" label="通过率" width="180" align="center">
                <template #default="scope">
                  <el-progress
                    :percentage="scope.row.approvalRate"
                    :color="scope.row.approvalRate >= 80 ? '#67c23a' : '#e6a23c'"
                    :stroke-width="10"
                  />
                </template>
              </el-table-column>
            </el-table>
            <el-empty v-if="moduleDetailList.length === 0" description="暂无模块明细数据" :image-size="80" />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 数据说明 -->
    <div class="data-note">
      <el-alert
        title="数据说明"
        type="info"
        :closable="false"
        show-icon
      >
        <template #default>
          本页数据由统一审批服务 <code>/api/v1/oa/unified-approval/statistics</code> 实时返回，
          统计范围覆盖 CRM / SCM / ERP / WMS / MES / LES / QMS / SRM / EAM / HR 全部已接入业务模块。
          部门/个人排名、平均审批时长等维度暂未在后端实现，后续版本将逐步完善。
        </template>
      </el-alert>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import {
  Document, Clock, Check, Close, CircleCheck, Grid, Refresh
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { unwrapResponseData } from '@/api'
import { unifiedApprovalApi } from '@/api/oa'

/** 业务模块定义（用于模块统计展示） */
const moduleOptions = [
  { value: 'crm', label: 'CRM 客户关系管理' },
  { value: 'scm', label: 'SCM 供应链管理' },
  { value: 'erp', label: 'ERP 企业资源计划' },
  { value: 'wms', label: 'WMS 仓储管理' },
  { value: 'mes', label: 'MES 制造执行' },
  { value: 'les', label: 'LES 物流执行' },
  { value: 'qms', label: 'QMS 质量管理' },
  { value: 'srm', label: 'SRM 供应商管理' },
  { value: 'eam', label: 'EAM 设备资产' },
  { value: 'hr', label: 'HR 人力资源' }
]

/** 加载状态 */
const loading = ref(false)

/** 总体统计数据（来自后端 getStatistics 接口） */
const statsData = reactive({
  totalApprovals: 0,
  pendingApprovals: 0,
  approvedApprovals: 0,
  rejectedApprovals: 0
})

/** 各模块统计原始数据（后端返回的 moduleStats 字段，键为模块编码） */
const moduleStatsRaw = ref<Record<string, any>>({})

/**
 * 审批通过率（已通过 / (已通过 + 已拒绝)，避免把待审批算入分母）
 */
const approvalRate = computed(() => {
  const decided = statsData.approvedApprovals + statsData.rejectedApprovals
  if (decided === 0) return 0
  return Math.round((statsData.approvedApprovals / decided) * 100)
})

/**
 * 已接入模块数（统计 total > 0 的模块数量）
 */
const moduleCount = computed(() => {
  return moduleOptions.filter((m) => {
    const stat = moduleStatsRaw.value[m.value]
    return stat && Number(stat.total || 0) > 0
  }).length
})

/**
 * 模块图表数据（用于"各业务模块审批分布"图表）
 */
const moduleChartData = computed(() => {
  return moduleOptions.map((m) => ({
    label: m.label.split(' ')[0],
    value: Number(moduleStatsRaw.value[m.value]?.total || 0)
  })).filter((item) => item.value > 0)
})

/**
 * 状态分布图表数据
 */
const statusChartData = computed(() => [
  { label: '已通过', value: statsData.approvedApprovals },
  { label: '已拒绝', value: statsData.rejectedApprovals },
  { label: '待审批', value: statsData.pendingApprovals },
  { label: '审批总数', value: statsData.totalApprovals }
])

/**
 * 模块明细列表（用于"各业务模块审批明细"表格）
 */
const moduleDetailList = computed(() => {
  return moduleOptions.map((m) => {
    const stat = moduleStatsRaw.value[m.value] || {}
    const total = Number(stat.total || 0)
    const pending = Number(stat.pending || 0)
    const approved = Number(stat.approved || 0)
    const rejected = Number(stat.rejected || 0)
    const decided = approved + rejected
    const rate = decided === 0 ? 0 : Math.round((approved / decided) * 100)
    return {
      label: m.label,
      total,
      pending,
      approved,
      rejected,
      approvalRate: rate
    }
  }).filter((item) => item.total > 0)
})

/**
 * 获取审批统计数据（调用统一审批统计接口）
 * <p>后端字段约定：totalInstances(避免与分页total冲突)、pending、approved、rejected、moduleStats</p>
 */
const fetchStatisticsData = async () => {
  loading.value = true
  try {
    const response = await unifiedApprovalApi.getStatistics()
    const data = unwrapResponseData<any>(response, {}) || {}

    // 总体统计
    statsData.totalApprovals = Number(data.totalInstances ?? 0)
    statsData.pendingApprovals = Number(data.pending ?? data.pendingTasks ?? 0)
    statsData.approvedApprovals = Number(data.approved ?? 0)
    statsData.rejectedApprovals = Number(data.rejected ?? 0)

    // 各模块统计
    moduleStatsRaw.value = data.moduleStats || data.modules || {}

    ElMessage.success('审批统计数据已刷新')
  } catch (error: any) {
    console.error('获取审批统计数据失败:', error)
    // 失败时重置数据为0
    statsData.totalApprovals = 0
    statsData.pendingApprovals = 0
    statsData.approvedApprovals = 0
    statsData.rejectedApprovals = 0
    moduleStatsRaw.value = {}
    const status = error?.response?.status ?? error?.data?.code
    if (status === 401) {
      ElMessage.error('登录状态已失效，请重新登录')
      return
    }
    ElMessage.error(error?.response?.data?.msg || error?.message || '获取审批统计数据失败')
  } finally {
    loading.value = false
  }
}

/**
 * 计算百分比（基于审批总数）
 */
const getPercentage = (value: number): number => {
  if (statsData.totalApprovals === 0) return 0
  return Math.round((value / statsData.totalApprovals) * 100)
}

/**
 * 获取进度条颜色
 */
const getProgressColor = (index: number) => {
  const colors = ['#409eff', '#67c23a', '#e6a23c', '#f56c6c', '#909399', '#722ed1', '#10b981', '#f97316', '#8b5cf6', '#6366f1']
  return colors[index % colors.length]
}

/**
 * 获取状态标签类型
 */
const getStatusTagType = (status: string) => {
  const statusMap: Record<string, string> = {
    '已通过': 'success',
    '已拒绝': 'danger',
    '待审批': 'warning',
    '审批总数': 'info'
  }
  return statusMap[status] || 'info'
}

/** 初始加载 */
onMounted(() => {
  fetchStatisticsData()
})
</script>

<style scoped>
.statistics-view {
  padding: 20px;
  box-sizing: border-box;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

/* 统计卡片 */
.stats-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
  width: 100%;
}

.stats-card {
  cursor: pointer;
  transition: all 0.3s ease;
  min-width: 0;
}

.stats-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
}

.stats-card .card-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px;
  min-height: 100px;
}

.stats-info {
  flex: 1;
  min-width: 0;
  overflow: hidden;
}

.stats-value {
  font-size: 32px;
  font-weight: bold;
  margin-bottom: 8px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.stats-value.warning { color: #e6a23c; }
.stats-value.success { color: #67c23a; }
.stats-value.danger { color: #f56c6c; }

.stats-label {
  font-size: 14px;
  color: #909399;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.stats-icon {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 28px;
  flex-shrink: 0;
  margin-left: 16px;
}

.stats-icon.success { background-color: #f0f9eb; color: #67c23a; }
.stats-icon.warning { background-color: #fdf6ec; color: #e6a23c; }
.stats-icon.danger { background-color: #fef0f0; color: #f56c6c; }
.stats-icon.info { background-color: #ecf5ff; color: #409eff; }
.stats-icon.total { background-color: #f4f4f5; color: #909399; }

/* 图表容器 */
.charts-container {
  margin-bottom: 20px;
  width: 100%;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}

.chart-content {
  padding: 12px 0;
}

.progress-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.progress-label {
  font-size: 13px;
  color: #606266;
}

.progress-value {
  font-weight: 600;
  color: #303133;
}

.status-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 16px;
  background: #fafafa;
  border-radius: 8px;
}

.data-note {
  margin-top: 20px;
}

.data-note code {
  background: #f5f7fa;
  padding: 2px 6px;
  border-radius: 3px;
  color: #409eff;
  font-family: 'Courier New', monospace;
  font-size: 13px;
}

@media (max-width: 768px) {
  .statistics-view {
    padding: 12px;
  }

  .stats-cards {
    grid-template-columns: 1fr 1fr;
  }

  .header-actions {
    flex-wrap: wrap;
  }
}
</style>
