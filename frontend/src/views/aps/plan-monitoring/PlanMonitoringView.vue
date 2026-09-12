<template>
  <div class="plan-monitoring-view">
    <div class="page-header">
      <h2>计划执行监控</h2>
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/home/aps' }">APS系统</el-breadcrumb-item>
        <el-breadcrumb-item>计划执行监控</el-breadcrumb-item>
      </el-breadcrumb>
    </div>
    
    <!-- 统计卡片 -->
    <div class="statistics-cards" style="margin-bottom: 20px;">
      <el-row :gutter="20">
        <el-col :xs="24" :sm="12" :md="6">
          <el-card shadow="hover" class="stat-card">
            <el-statistic title="预警总数" :value="statistics.totalAlerts">
              <template #prefix>
                <el-icon style="vertical-align: -0.125em;"><Warning /></el-icon>
              </template>
            </el-statistic>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <el-card shadow="hover" class="stat-card">
            <el-statistic title="高优先级预警" :value="statistics.highPriorityAlerts">
              <template #prefix>
                <el-icon style="vertical-align: -0.125em; color: #f56c6c;"><WarningFilled /></el-icon>
              </template>
            </el-statistic>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <el-card shadow="hover" class="stat-card">
            <el-statistic title="进度跟踪项" :value="statistics.trackingItems">
              <template #prefix>
                <el-icon style="vertical-align: -0.125em;"><List /></el-icon>
              </template>
            </el-statistic>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <el-card shadow="hover" class="stat-card">
            <el-statistic title="对比数据项" :value="statistics.compareItems">
              <template #prefix>
                <el-icon style="vertical-align: -0.125em;"><DataAnalysis /></el-icon>
              </template>
            </el-statistic>
          </el-card>
        </el-col>
      </el-row>
    </div>
    
    <!-- 计划执行监控模块导航 -->
    <div class="module-nav">
      <el-tabs v-model="activeTab" type="card" @tab-change="handleTabChange">
        <el-tab-pane label="计划与实际对比" name="compare">
          <div class="tab-content">
            <el-card shadow="hover">
              <template #header>
                <div class="card-header">
                  <span>计划与实际对比</span>
                  <el-button type="primary" size="small">导出对比报告</el-button>
                </div>
              </template>
              <el-table :data="planCompareData" stripe style="width: 100%" v-loading="loading.compare" empty-text="暂无数据">
                <el-table-column prop="id" label="ID" width="80" fixed="left" />
                <el-table-column prop="planId" label="计划ID" width="100" />
                <el-table-column prop="planName" label="计划名称" min-width="150" show-overflow-tooltip />
                <el-table-column prop="itemName" label="项目名称" min-width="120" show-overflow-tooltip />
                <el-table-column prop="planValue" label="计划值" width="120" align="right">
                  <template #default="scope">
                    {{ formatNumber(scope.row.planValue) }}
                  </template>
                </el-table-column>
                <el-table-column prop="actualValue" label="实际值" width="120" align="right">
                  <template #default="scope">
                    {{ formatNumber(scope.row.actualValue) }}
                  </template>
                </el-table-column>
                <el-table-column prop="deviation" label="偏差" width="120" align="right">
                  <template #default="scope">
                    <span :class="{ 'positive': scope.row.deviation > 0, 'negative': scope.row.deviation < 0 }">
                      {{ formatNumber(scope.row.deviation) }}
                    </span>
                  </template>
                </el-table-column>
                <el-table-column prop="deviationRate" label="偏差率" width="100" align="right">
                  <template #default="scope">
                    <el-tag
                      :type="scope.row.deviationRate > 10 ? 'danger' : scope.row.deviationRate > 5 ? 'warning' : 'success'"
                      size="small"
                    >
                      {{ formatPercentage(scope.row.deviationRate) }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="100">
                  <template #default="scope">
                    <el-button type="primary" size="small" @click="handleViewDetails(scope.row)">详情</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-card>
          </div>
        </el-tab-pane>
        <el-tab-pane label="进度跟踪" name="tracking">
          <div class="tab-content">
            <el-card shadow="hover">
              <template #header>
                <div class="card-header">
                  <span>进度跟踪</span>
                  <el-button type="primary" size="small" @click="handleRefresh">刷新数据</el-button>
                </div>
              </template>
              <el-table :data="progressTrackingData" stripe style="width: 100%" v-loading="loading.tracking" empty-text="暂无数据">
                <el-table-column prop="id" label="ID" width="80" />
                <el-table-column prop="orderId" label="订单ID" />
                <el-table-column prop="orderNo" label="订单编号" />
                <el-table-column prop="productName" label="产品名称" />
                <el-table-column prop="totalQuantity" label="总数量" />
                <el-table-column prop="completedQuantity" label="已完成数量" />
                <el-table-column prop="progress" label="进度" />
                <el-table-column prop="status" label="状态" />
                <el-table-column label="操作" width="100">
                  <template #default="scope">
                    <el-button type="primary" size="small" @click="handleViewDetails(scope.row)">详情</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-card>
          </div>
        </el-tab-pane>
        <el-tab-pane label="偏差预警" name="alert">
          <div class="tab-content">
            <el-card shadow="hover">
              <template #header>
                <div class="card-header">
                  <span>偏差预警</span>
                  <el-button type="primary" size="small">处理预警</el-button>
                </div>
              </template>
              <el-table :data="deviationAlerts" stripe style="width: 100%" v-loading="loading.alert" empty-text="暂无数据">
                <el-table-column prop="id" label="ID" width="80" />
                <el-table-column prop="alertType" label="预警类型" />
                <el-table-column prop="alertContent" label="预警内容" />
                <el-table-column prop="severity" label="严重程度" />
                <el-table-column prop="occurTime" label="发生时间" />
                <el-table-column prop="status" label="状态" />
                <el-table-column label="操作" width="150">
                  <template #default="scope">
                    <el-button type="primary" size="small" @click="handleProcess(scope.row)">处理</el-button>
                    <el-button type="danger" size="small" @click="handleDismiss(scope.row)">忽略</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-card>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
    
    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="详情信息"
      width="700px"
    >
      <div v-if="detailData" class="detail-content">
        <el-descriptions :column="2" border>
          <el-descriptions-item
            v-for="(value, key) in detailData"
            :key="key"
            :label="getFieldLabel(String(key))"
            :span="String(key) === 'description' || String(key) === 'content' ? 2 : 1"
          >
            <template v-if="String(key) === 'status' || String(key) === 'severity'">
              <el-tag
                :type="String(value) === 'high' || String(value) === 'critical' ? 'danger' : String(value) === 'medium' || String(value) === 'warning' ? 'warning' : 'info'"
                size="small"
              >
                {{ value }}
              </el-tag>
            </template>
            <template v-else>
              {{ value || '-' }}
            </template>
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button type="primary" @click="detailDialogVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Warning, WarningFilled, List, DataAnalysis } from '@element-plus/icons-vue'
import { unwrapResponseData } from '../../../api'
import { DataTransformer } from '../../../utils/data-transformer'
// 导入API服务
import { PlanMonitoringAPI } from '../../../api/aps'

// 活跃标签
const activeTab = ref<string>('compare')

// 数据列表
const planCompareData = ref<any[]>([])
const progressTrackingData = ref<any[]>([])
const deviationAlerts = ref<any[]>([])

// 加载状态
const loading = ref<{ [key: string]: boolean }>({
  compare: false,
  tracking: false,
  alert: false
})

// 统计数据
const statistics = computed(() => {
  const totalAlerts = deviationAlerts.value.length
  const highPriorityAlerts = deviationAlerts.value.filter((alert: any) => 
    alert.severity === 'high' || alert.severity === 'critical'
  ).length
  const trackingItems = progressTrackingData.value.length
  const compareItems = planCompareData.value.length
  
  return {
    totalAlerts,
    highPriorityAlerts,
    trackingItems,
    compareItems
  }
})

const toArrayData = (response: any): any[] => {
  const data = unwrapResponseData<any>(response)
  if (Array.isArray(data)) return data
  if (Array.isArray(data?.list)) return data.list
  if (Array.isArray(data?.records)) return data.records
  return data ? [data] : []
}

const getResponseMessage = (response: any, fallback: string) => {
  const normalized = DataTransformer.normalizeResponse(response)
  return normalized?.msg || normalized?.message || fallback
}

// 使用真实API获取数据
const fetchData = async (tabName?: string) => {
  // 根据标签页获取对应的数据
  if (!tabName || tabName === 'compare') {
    loading.value.compare = true
    try {
      // 获取计划与实际对比数据
      const response = await PlanMonitoringAPI.getPlanCompareData({ planId: 1 })
      planCompareData.value = toArrayData(response)
    } catch (error: any) {
      console.error('获取计划对比数据失败:', error.message || error);
      ElMessage.error(error.message || '获取计划对比数据失败');
      planCompareData.value = []
    } finally {
      loading.value.compare = false
    }
  }
  
  if (!tabName || tabName === 'tracking') {
    loading.value.tracking = true
    try {
      // 获取进度跟踪数据
      const response = await PlanMonitoringAPI.getProgressTrackingData({ planId: 1 })
      progressTrackingData.value = toArrayData(response)
    } catch (error: any) {
      console.error('获取进度跟踪数据失败:', error.message || error);
      ElMessage.error(error.message || '获取进度跟踪数据失败');
      progressTrackingData.value = []
    } finally {
      loading.value.tracking = false
    }
  }
  
  if (!tabName || tabName === 'alert') {
    loading.value.alert = true
    try {
      // 获取偏差预警数据
      const response = await PlanMonitoringAPI.getDeviationAlerts({})
      deviationAlerts.value = toArrayData(response)
    } catch (error: any) {
      console.error('获取偏差预警数据失败:', error.message || error);
      ElMessage.error(error.message || '获取偏差预警数据失败');
      deviationAlerts.value = []
    } finally {
      loading.value.alert = false
    }
  }
}

// 根据标签切换获取数据
const handleTabChange = (tabName: string) => {
  fetchData(tabName)
}

// 详情对话框相关
const detailDialogVisible = ref(false)
const detailData = ref<any>(null)

// 处理查看详情
const handleViewDetails = (row: any) => {
  detailData.value = row
  detailDialogVisible.value = true
}

// 处理预警
const handleProcess = async (row: any) => {
  try {
    // 调用API处理偏差预警
    const response = await PlanMonitoringAPI.processAlert(row.id)
    const responseData = DataTransformer.normalizeResponse(response)
    
    if (DataTransformer.isSuccessCode(responseData?.code)) {
      // 更新本地数据状态
      const index = deviationAlerts.value.findIndex(item => item.id === row.id)
      if (index !== -1) {
        deviationAlerts.value[index].status = '已处理'
      }
      ElMessage.success('预警处理成功')
    } else {
      ElMessage.error(getResponseMessage(responseData, '预警处理失败'))
    }
  } catch (error: any) {
    console.error('处理预警失败:', error.message || error);
    ElMessage.error(error.message || '处理预警失败');
  }
}

// 处理忽略预警
const handleDismiss = async (row: any) => {
  try {
    // 调用API忽略偏差预警
    const response = await PlanMonitoringAPI.dismissAlert(row.id)
    const responseData = DataTransformer.normalizeResponse(response)
    
    if (DataTransformer.isSuccessCode(responseData?.code)) {
      // 更新本地数据状态
      const index = deviationAlerts.value.findIndex(item => item.id === row.id)
      if (index !== -1) {
        deviationAlerts.value[index].status = '已忽略'
      }
      ElMessage.success('预警已忽略')
    } else {
      ElMessage.error(getResponseMessage(responseData, '忽略预警失败'))
    }
  } catch (error: any) {
    console.error('忽略预警失败:', error.message || error);
    ElMessage.error(error.message || '忽略预警失败');
  }
}

// 刷新数据
const handleRefresh = () => {
  fetchData(activeTab.value)
}

// 获取字段标签
const getFieldLabel = (key: string): string => {
  const labelMap: { [key: string]: string } = {
    id: 'ID',
    planId: '计划ID',
    planName: '计划名称',
    itemName: '项目名称',
    orderId: '订单ID',
    orderNo: '订单编号',
    planValue: '计划值',
    actualValue: '实际值',
    deviation: '偏差',
    deviationRate: '偏差率',
    progress: '进度',
    status: '状态',
    severity: '严重程度',
    type: '类型',
    content: '内容',
    description: '描述',
    occurTime: '发生时间',
    processTime: '处理时间'
  }
  return labelMap[key] || key
}

// 格式化数字
const formatNumber = (value: any): string => {
  if (value === null || value === undefined) return '-'
  const num = Number(value)
  if (isNaN(num)) return String(value)
  return num.toLocaleString('zh-CN', { minimumFractionDigits: 0, maximumFractionDigits: 2 })
}

// 格式化百分比
const formatPercentage = (value: any): string => {
  if (value === null || value === undefined) return '-'
  const num = Number(value)
  if (isNaN(num)) return String(value)
  return `${num.toFixed(2)}%`
}

// 组件挂载时获取数据
onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.plan-monitoring-view {
  padding: 20px;
  height: 100%;
  box-sizing: border-box;
  background-color: #f5f7fa;
}

.page-header {
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #e4e7ed;
}

.page-header h2 {
  margin: 0 0 10px 0;
  color: #303133;
  font-size: 1.5rem;
}

.module-nav {
  background-color: #ffffff;
  border-radius: 4px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  min-height: calc(100% - 80px);
}

/* 标签内容样式 */
.tab-content {
  padding: 20px;
}

/* 卡片头部样式 */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
  font-size: 1.1rem;
}

/* 响应式设计 */
.statistics-cards {
  margin-bottom: 20px;
}

.stat-card {
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.stat-card :deep(.el-statistic__head) {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.stat-card :deep(.el-statistic__number) {
  font-size: 24px;
  font-weight: bold;
}

.positive {
  color: #67c23a;
  font-weight: bold;
}

.negative {
  color: #f56c6c;
  font-weight: bold;
}

@media (max-width: 768px) {
  .plan-monitoring-view {
    padding: 12px;
  }
  
  .page-header h2 {
    font-size: 1.2rem;
  }
  
  .module-nav {
    min-height: auto;
  }
  
  .tab-content {
    padding: 12px;
  }
}
</style>
