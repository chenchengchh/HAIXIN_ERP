<template>
  <div class="energy-optimization-view">
    <div class="page-header">
      <h2>能源优化</h2>
      <el-breadcrumb>
        <el-breadcrumb-item>
          <router-link to="/home">首页</router-link>
        </el-breadcrumb-item>
        <el-breadcrumb-item>
          <router-link to="/home/ems">EMS系统</router-link>
        </el-breadcrumb-item>
        <el-breadcrumb-item>
          <router-link to="/home/ems/energy-optimization">能源优化</router-link>
        </el-breadcrumb-item>
        <el-breadcrumb-item :to="`/home/ems/energy-optimization#${activeTab}`">
          {{ tabLabelMap[activeTab] }}
        </el-breadcrumb-item>
      </el-breadcrumb>
    </div>
    <el-tabs v-model="activeTab" class="function-tabs" @tab-change="handleTabChange">
      <el-tab-pane label="节能潜力分析" name="potential-analysis" />
      <el-tab-pane label="优化建议" name="optimization-suggestions" />
      <el-tab-pane label="优化方案执行" name="plan-execution" />
      <el-tab-pane label="节能效果评估" name="effect-evaluation" />
    </el-tabs>
    
    <!-- 建议详情对话框 -->
    <el-dialog
      v-model="suggestionDetailVisible"
      title="建议详情"
      width="50%"
      :before-close="handleCloseSuggestionDetail"
    >
      <div v-if="selectedSuggestion" class="suggestion-detail">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="建议ID">{{ selectedSuggestion.id }}</el-descriptions-item>
          <el-descriptions-item label="建议标题">{{ selectedSuggestion.title }}</el-descriptions-item>
          <el-descriptions-item label="建议内容">{{ selectedSuggestion.content }}</el-descriptions-item>
          <el-descriptions-item label="目标区域">{{ selectedSuggestion.targetArea }}</el-descriptions-item>
          <el-descriptions-item label="预计效果">{{ selectedSuggestion.estimatedEffect }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ selectedSuggestion.status === 'adopted' ? '已采纳' : '待审批' }}</el-descriptions-item>
          <el-descriptions-item label="创建日期">{{ selectedSuggestion.createdAt }}</el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleCloseSuggestionDetail">关闭</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 方案详情对话框 -->
    <el-dialog
      v-model="planDetailVisible"
      title="方案详情"
      width="50%"
      :before-close="handleClosePlanDetail"
    >
      <div v-if="selectedPlan" class="plan-detail">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="方案ID">{{ selectedPlan.id }}</el-descriptions-item>
          <el-descriptions-item label="方案名称">{{ selectedPlan.planName }}</el-descriptions-item>
          <el-descriptions-item label="目标区域/设备">{{ selectedPlan.targetArea }}</el-descriptions-item>
          <el-descriptions-item label="预期节能量(%)">{{ selectedPlan.predictedSaving }}</el-descriptions-item>
          <el-descriptions-item label="实际节能量(%)">{{ selectedPlan.actualSaving || '未完成' }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ getStatusText(selectedPlan.status) }}</el-descriptions-item>
          <el-descriptions-item label="执行内容">{{ selectedPlan.execContent }}</el-descriptions-item>
          <el-descriptions-item label="开始日期">{{ formatDate(selectedPlan.startDate) }}</el-descriptions-item>
          <el-descriptions-item label="结束日期">{{ formatDate(selectedPlan.endDate) }}</el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleClosePlanDetail">关闭</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 方案跟踪对话框 -->
    <el-dialog
      v-model="planTrackingVisible"
      title="方案跟踪"
      width="60%"
      :before-close="handleClosePlanTracking"
    >
      <div v-if="selectedPlan" class="plan-tracking">
        <el-timeline>
          <el-timeline-item
            v-for="(item, index) in trackingItems"
            :key="index"
            :timestamp="item.time"
            :type="item.type"
          >
            {{ item.content }}
          </el-timeline-item>
        </el-timeline>
        
        <div class="tracking-stats" style="margin-top: 20px;">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-card shadow="hover">
                <template #header>
                  <div class="card-header">
                    <span>执行进度</span>
                  </div>
                </template>
                <div class="progress-content">
                  <el-progress :percentage="85" status="success"></el-progress>
                  <div class="progress-text">已完成85%</div>
                </div>
              </el-card>
            </el-col>
            <el-col :span="12">
              <el-card shadow="hover">
                <template #header>
                  <div class="card-header">
                    <span>节能效果</span>
                  </div>
                </template>
                <div class="energy-savings">
                  <el-statistic :value="selectedPlan.actualSaving || 0" title="实际节能量">
                    <template #suffix>
                      <span style="color: #67C23A; font-size: 18px;">%</span>
                    </template>
                  </el-statistic>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleClosePlanTracking">关闭</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 评估报告详情对话框 -->
    <el-dialog
      v-model="reportDetailVisible"
      title="评估报告详情"
      width="60%"
      :before-close="handleCloseReportDetail"
    >
      <div v-if="selectedReport" class="report-detail">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="报告ID">{{ selectedReport.id }}</el-descriptions-item>
          <el-descriptions-item label="方案名称">{{ selectedReport.planName }}</el-descriptions-item>
          <el-descriptions-item label="目标区域">{{ selectedReport.targetArea }}</el-descriptions-item>
          <el-descriptions-item label="预期节能量(%)">{{ selectedReport.predictedSaving }}</el-descriptions-item>
          <el-descriptions-item label="实际节能量(%)">
            <span :class="selectedReport.actualSaving >= selectedReport.predictedSaving ? 'text-success' : 'text-danger'">
              {{ selectedReport.actualSaving }}%
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="评估日期">{{ formatDate(selectedReport.evaluationDate) }}</el-descriptions-item>
          <el-descriptions-item label="评估结果">
            <el-tag :type="selectedReport.actualSaving >= selectedReport.predictedSaving ? 'success' : 'warning'">
              {{ selectedReport.actualSaving >= selectedReport.predictedSaving ? '达到预期' : '未达到预期' }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>
        
        <!-- 节能量对比图表 -->
        <div class="report-chart" style="margin-top: 20px;">
          <el-card shadow="hover">
            <template #header>
              <div class="card-header">
                <span>节能量对比</span>
              </div>
            </template>
            <div class="chart-container">
              <el-progress 
                :percentage="selectedReport.actualSaving" 
                :color="selectedReport.actualSaving >= selectedReport.predictedSaving ? '#67C23A' : '#F56C6C'"
              >
                <template #default="{ percentage }">
                  <span style="font-size: 18px;">
                    实际节能量: {{ percentage }}%
                  </span>
                </template>
              </el-progress>
              <div class="predicted-line" :style="{ left: `${selectedReport.predictedSaving}%` }">
                预期: {{ selectedReport.predictedSaving }}%
              </div>
            </div>
          </el-card>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleCloseReportDetail">关闭</el-button>
          <el-button type="warning" @click="handleExportReport(selectedReport)">导出报告</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 节能潜力分析 -->
    <div v-if="activeTab === 'potential-analysis'" class="tab-content">
      <el-card shadow="hover" class="sub-card">
        <template #header>
          <div class="sub-card-header">
            <span>节能潜力分析</span>
            <div class="filter-controls">
              <el-button type="primary" size="small" @click="refreshData">刷新数据</el-button>
            </div>
          </div>
        </template>
        <div class="potential-content">
          <el-row :gutter="20">
            <el-col :span="12">
              <div class="potential-item">
                <h4>总体节能潜力</h4>
                <div class="potential-value">
                  <el-statistic :value="potentialSummary.avgPotential" title="节能潜力">
                    <template #suffix>
                      <span style="color: #67C23A; font-size: 18px;">%</span>
                    </template>
                  </el-statistic>
                </div>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="potential-item">
                <h4>预计年节能量</h4>
                <div class="potential-value">
                  <el-statistic :value="potentialSummary.totalSavings" title="年节能量">
                    <template #suffix>
                      <span style="color: #67C23A; font-size: 18px;">kWh</span>
                    </template>
                  </el-statistic>
                </div>
              </div>
            </el-col>
            <el-col :span="24">
              <div class="potential-list">
                <h4>节能潜力分布</h4>
                <el-table :data="emsStore.potentialAnalysis" style="width: 100%" height="300">
                  <el-table-column prop="id" label="ID" width="80" />
                  <el-table-column prop="area" label="区域" width="120" />
                  <el-table-column prop="energyType" label="能源类型" width="120" />
                  <el-table-column prop="potential" label="节能潜力(%)" width="120" />
                  <el-table-column prop="estimatedSavings" label="预计节能量" width="150" />
                  <el-table-column prop="priority" label="优先级" width="100">
                    <template #default="scope">
                      <el-tag :type="getPriorityColor(scope.row.priority)">
                        {{ scope.row.priority }}
                      </el-tag>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
            </el-col>
          </el-row>
        </div>
      </el-card>
    </div>
    
    <!-- 优化建议 -->
    <div v-if="activeTab === 'optimization-suggestions'" class="tab-content">
      <el-card shadow="hover" class="sub-card">
        <template #header>
          <div class="sub-card-header">
            <span>优化建议</span>
            <div class="filter-controls">
              <el-button type="primary" size="small" @click="refreshData">刷新数据</el-button>
            </div>
          </div>
        </template>
        <div class="suggestions-content">
          <el-table :data="emsStore.optimizationSuggestions" style="width: 100%" height="400">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="title" label="建议标题" width="200" />
            <el-table-column prop="content" label="建议内容" width="300" />
            <el-table-column prop="targetArea" label="目标区域" width="120" />
            <el-table-column prop="estimatedEffect" label="预计效果" width="150" />
            <el-table-column prop="status" label="状态" width="120">
              <template #default="scope">
                <el-tag :type="scope.row.status === 'adopted' ? 'success' : 'info'">
                  {{ scope.row.status === 'adopted' ? '已采纳' : '待审批' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150">
              <template #default="scope">
                <el-button size="small" type="primary" @click="handleAdoptSuggestion(scope.row)">
                  {{ scope.row.status === 'adopted' ? '查看' : '采纳' }}
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-card>
    </div>
    
    <!-- 优化方案执行 -->
    <div v-if="activeTab === 'plan-execution'" class="tab-content">
      <el-card shadow="hover" class="sub-card">
        <template #header>
          <div class="sub-card-header">
            <span>优化方案执行</span>
            <div class="filter-controls">
              <el-button type="primary" size="small" @click="refreshData">刷新数据</el-button>
            </div>
          </div>
        </template>
        <div class="execution-content">
          <el-table v-loading="emsStore.loading.optimizationPlans" :data="emsStore.optimizationPlans" style="width: 100%" height="400">
            <el-table-column prop="id" label="方案ID" width="100" />
            <el-table-column prop="planName" label="方案名称" width="180" />
            <el-table-column prop="targetArea" label="目标区域/设备" width="150" />
            <el-table-column prop="predictedSaving" label="预期节能量(%)" width="150" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="scope">
                <el-tag :type="getStatusColor(scope.row.status)">
                  {{ getStatusText(scope.row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="startDate" label="开始日期" width="120" />
            <el-table-column prop="endDate" label="结束日期" width="120" />
            <el-table-column label="操作" width="150">
              <template #default="scope">
                <el-button size="small" type="primary" @click="handleTrackPlan(scope.row)">跟踪</el-button>
                <el-button size="small" type="success" @click="handleViewPlanDetail(scope.row)">详情</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-card>
    </div>
    
    <!-- 节能效果评估 -->
    <div v-if="activeTab === 'effect-evaluation'" class="tab-content">
      <el-card shadow="hover" class="sub-card">
        <template #header>
          <div class="sub-card-header">
            <span>节能效果评估</span>
            <div class="filter-controls">
              <el-button type="primary" size="small" @click="refreshData">刷新数据</el-button>
            </div>
          </div>
        </template>
        <div class="evaluation-content">
          <el-table :data="emsStore.effectEvaluations" style="width: 100%" height="400">
            <el-table-column prop="id" label="报告ID" width="100" />
            <el-table-column prop="planName" label="方案名称" width="180" />
            <el-table-column prop="targetArea" label="目标区域" width="120" />
            <el-table-column prop="predictedSaving" label="预期节能量(%)" width="150" />
            <el-table-column prop="actualSaving" label="实际节能量(%)" width="150">
              <template #default="scope">
                <span :class="scope.row.actualSaving >= scope.row.predictedSaving ? 'text-success' : 'text-danger'">
                  {{ scope.row.actualSaving }}%
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="evaluationDate" label="评估日期" width="150" />
            <el-table-column label="操作" width="150">
              <template #default="scope">
                <el-button size="small" type="primary" @click="handleViewReport(scope.row)">查看报告</el-button>
                <el-button size="small" type="warning" @click="handleExportReport(scope.row)">导出</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useEmsStore } from '@/stores/ems'

// 路由实例
const router = useRouter()
const route = useRoute()

// 激活的标签页
const activeTab = ref('potential-analysis')

// 标签页名称映射
const tabLabelMap: Record<string, string> = {
  'potential-analysis': '节能潜力分析',
  'optimization-suggestions': '优化建议',
  'plan-execution': '优化方案执行',
  'effect-evaluation': '节能效果评估'
}

// 状态管理
const emsStore = useEmsStore()

/**
 * 节能潜力汇总：平均节能潜力（%）与预计年节能量合计（kWh）
 * estimatedSavings为字符串（如"12000 kWh"），需解析其中的数值
 */
const potentialSummary = computed(() => {
  const list = emsStore.potentialAnalysis || []
  if (list.length === 0) {
    return { avgPotential: 0, totalSavings: 0 }
  }
  let potentialSum = 0
  let savingsSum = 0
  for (const item of list) {
    potentialSum += Number(item.potential) || 0
    // 从字符串中提取数值部分（兼容"12000 kWh"、"12000"等格式）
    const match = String(item.estimatedSavings ?? '').replace(/,/g, '').match(/[\d.]+/)
    savingsSum += match ? parseFloat(match[0]) : 0
  }
  return {
    avgPotential: parseFloat((potentialSum / list.length).toFixed(1)),
    totalSavings: Math.round(savingsSum)
  }
})

// 对话框状态
const suggestionDetailVisible = ref(false)
const selectedSuggestion = ref<any>(null)

// 方案详情对话框状态
const planDetailVisible = ref(false)
const selectedPlan = ref<any>(null)

// 方案跟踪对话框状态
const planTrackingVisible = ref(false)
const trackingItems = ref([
  { time: '2025-11-25', type: 'success', content: '方案已创建' },
  { time: '2025-11-26', type: 'info', content: '方案已审批通过' },
  { time: '2025-11-27', type: 'info', content: '开始执行方案' },
  { time: '2025-12-01', type: 'info', content: '设备改造完成' },
  { time: '2025-12-03', type: 'info', content: '开始试运行' },
  { time: '2025-12-05', type: 'warning', content: '开始回访验证' }
])

// 评估报告对话框状态
const reportDetailVisible = ref(false)
const selectedReport = ref<any>(null)

// 从路由获取标签页状态
const getActiveTabFromRoute = () => {
  const tabMap: Record<string, string> = {
    'potential-analysis': 'potential-analysis',
    'optimization-suggestions': 'optimization-suggestions',
    'plan-execution': 'plan-execution',
    'effect-evaluation': 'effect-evaluation'
  }
  const tabName = route.params.tab || 'potential-analysis'
  return tabMap[tabName as string] || 'potential-analysis'
}

// 格式化日期
const formatDate = (dateString: string) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleDateString()
}

// 组件挂载时，从路由获取标签页状态并加载数据
onMounted(async () => {
  activeTab.value = getActiveTabFromRoute()
  
  // 加载能源优化相关数据
  await Promise.all([
    emsStore.fetchPotentialAnalysis(),
    emsStore.fetchOptimizationSuggestions(),
    emsStore.fetchOptimizationPlans(),
    emsStore.fetchEffectEvaluations()
  ])
})

// 标签页切换事件
const handleTabChange = (tabName: string) => {
  console.log('切换标签页:', tabName)
  // 更新路由，保持标签页状态
  router.push({
    path: `/home/ems/energy-optimization/${tabName}`
  })
}

// 方法
const getPriorityColor = (priority: string) => {
  switch (priority) {
    case '高': return 'danger'
    case '中': return 'warning'
    case '低': return 'info'
    default: return 'info'
  }
}

const getStatusColor = (status: number) => {
  switch (status) {
    case 1: return 'info'
    case 2: return 'success'
    case 3: return 'warning'
    case 4: return 'success'
    default: return 'info'
  }
}

const getStatusText = (status: number) => {
  switch (status) {
    case 1: return '建议中'
    case 2: return '执行中'
    case 3: return '回访验证'
    case 4: return '已结案'
    default: return '未知'
  }
}

/**
 * 采纳优化建议或查看建议详情
 * @param suggestion 优化建议
 */
const handleAdoptSuggestion = async (suggestion: any) => {
  console.log('处理建议:', suggestion)
  if (suggestion.status === 'pending') {
    // 采纳建议
    try {
      await emsStore.adoptSuggestion(suggestion.id)
      ElMessage.success('建议采纳成功')
    } catch (error) {
      console.error('采纳建议失败:', error)
      ElMessage.error('采纳建议失败')
    }
  } else if (suggestion.status === 'adopted') {
    // 查看建议详情
    selectedSuggestion.value = suggestion
    suggestionDetailVisible.value = true
  }
}

/**
 * 关闭建议详情对话框
 */
const handleCloseSuggestionDetail = () => {
  suggestionDetailVisible.value = false
  selectedSuggestion.value = null
}

/**
 * 跟踪方案执行情况
 * @param plan 优化方案
 */
const handleTrackPlan = async (plan: any) => {
  console.log('跟踪方案:', plan)
  try {
    // 调用后端API获取方案执行详情
    const planDetail = await emsStore.fetchOptimizationPlanById(plan.id)
    if (planDetail) {
      selectedPlan.value = planDetail
      planTrackingVisible.value = true
    } else {
      // 如果获取详情失败，使用当前行数据
      selectedPlan.value = plan
      planTrackingVisible.value = true
    }
  } catch (error) {
    console.error('跟踪方案失败:', error)
    // 错误情况下，使用当前行数据
    selectedPlan.value = plan
    planTrackingVisible.value = true
    ElMessage.error('获取方案详情失败，使用当前数据显示')
  }
}

/**
 * 查看方案详情
 * @param plan 优化方案
 */
const handleViewPlanDetail = async (plan: any) => {
  console.log('查看方案详情:', plan)
  try {
    // 调用后端API获取方案详情
    const planDetail = await emsStore.fetchOptimizationPlanById(plan.id)
    if (planDetail) {
      selectedPlan.value = planDetail
      planDetailVisible.value = true
    } else {
      // 如果获取详情失败，使用当前行数据
      selectedPlan.value = plan
      planDetailVisible.value = true
    }
  } catch (error) {
    console.error('查看方案详情失败:', error)
    // 错误情况下，使用当前行数据
    selectedPlan.value = plan
    planDetailVisible.value = true
    ElMessage.error('获取方案详情失败，使用当前数据显示')
  }
}

/**
 * 关闭方案详情对话框
 */
const handleClosePlanDetail = () => {
  planDetailVisible.value = false
  selectedPlan.value = null
}

/**
 * 关闭方案跟踪对话框
 */
const handleClosePlanTracking = () => {
  planTrackingVisible.value = false
  selectedPlan.value = null
}

/**
 * 查看评估报告
 * @param report 效果评估报告
 */
const handleViewReport = async (report: any) => {
  console.log('查看报告:', report)
  try {
    const evaluationDetail = await emsStore.getEvaluationReport(report.id)
    selectedReport.value = evaluationDetail || report
    reportDetailVisible.value = true
  } catch (error) {
    console.error('查看报告失败:', error)
    // 错误情况下，使用当前行数据
    selectedReport.value = report
    reportDetailVisible.value = true
    ElMessage.error('获取报告详情失败，使用当前数据显示')
  }
}

/**
 * 导出评估报告
 * @param report 效果评估报告
 */
const handleExportReport = async (report: any) => {
  console.log('导出报告:', report)
  try {
    const response = await emsStore.downloadEvaluationReport(report.id)
    if (!response) {
      ElMessage.error('报告导出失败')
      return
    }
    const blob = new Blob([response.data], { type: response.headers?.['content-type'] || 'application/octet-stream' })
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `${report.planName || '评估'}_评估报告_${new Date().toISOString().split('T')[0]}.csv`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    URL.revokeObjectURL(url)
    ElMessage.success(`报告导出成功: ${report.planName}`)
  } catch (error) {
    console.error('导出报告失败:', error)
    ElMessage.error('导出报告失败')
  }
}

/**
 * 关闭评估报告详情对话框
 */
const handleCloseReportDetail = () => {
  reportDetailVisible.value = false
  selectedReport.value = null
}

/**
 * 刷新数据
 */
const refreshData = async () => {
  await Promise.all([
    emsStore.fetchPotentialAnalysis(),
    emsStore.fetchOptimizationSuggestions(),
    emsStore.fetchOptimizationPlans(),
    emsStore.fetchEffectEvaluations()
  ])
  ElMessage.success('数据刷新成功')
}
</script>

<style scoped>
.energy-optimization-view {
  padding: 20px;
  height: 100%;
  overflow: auto;
}

/* 页面头部样式 */
.page-header {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 8px;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--border-color-light);
}

.page-header h2 {
  margin: 0;
  color: #333;
  font-size: 1.8rem;
}

/* 覆盖默认的h2样式，确保只影响页面标题 */
h2 {
  margin-bottom: 0;
}

.sub-card {
  margin-bottom: 20px;
}

.sub-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
  font-size: 1rem;
}

.tab-content {
  padding: 10px 0;
}

.potential-content {
  padding: 10px 0;
}

.potential-item {
  background-color: #f5f7fa;
  padding: 20px;
  border-radius: 8px;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.potential-item h4 {
  margin: 0 0 15px 0;
  font-size: 16px;
  color: #303133;
}

.potential-value {
  width: 100%;
  text-align: center;
}

.potential-list {
  margin-top: 20px;
}

.potential-list h4 {
  margin: 0 0 10px 0;
  font-size: 14px;
  color: #606266;
}

.suggestions-content {
  padding: 10px 0;
}

.execution-content {
  padding: 10px 0;
}

.evaluation-content {
  padding: 10px 0;
}

.text-success {
  color: #67C23A;
  font-weight: bold;
}

.text-danger {
  color: #F56C6C;
  font-weight: bold;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .energy-optimization-view {
    padding: 12px;
  }
  
  .sub-card {
    margin-bottom: 12px;
  }
  
  .potential-item {
    padding: 15px;
  }
}

/* 建议详情样式 */
.suggestion-detail {
  padding: 10px 0;
}

.suggestion-detail .el-descriptions {
  margin: 10px 0;
}

.suggestion-detail .el-descriptions__label {
  font-weight: bold;
  background-color: #f5f7fa;
}

.suggestion-detail .el-descriptions__content {
  word-break: break-all;
}

/* 方案详情样式 */
.plan-detail {
  padding: 10px 0;
}

.plan-detail .el-descriptions {
  margin: 10px 0;
}

.plan-detail .el-descriptions__label {
  font-weight: bold;
  background-color: #f5f7fa;
}

.plan-detail .el-descriptions__content {
  word-break: break-all;
}

/* 方案跟踪样式 */
.plan-tracking {
  padding: 10px 0;
}

.plan-tracking .el-timeline {
  margin: 10px 0;
}

.plan-tracking .el-timeline-item {
  margin-bottom: 20px;
}

.plan-tracking .el-timeline-item__timestamp {
  font-weight: bold;
  color: #606266;
}

.tracking-stats {
  margin-top: 20px;
}

.card-header {
  font-weight: bold;
  font-size: 16px;
}

.progress-content {
  padding: 10px 0;
}

.progress-text {
  text-align: center;
  margin-top: 10px;
  font-weight: bold;
  color: #67C23A;
}

.energy-savings {
  text-align: center;
  padding: 20px 0;
}

/* 报告详情样式 */
.report-detail {
  padding: 10px 0;
}

.report-detail .el-descriptions {
  margin: 10px 0;
}

.report-detail .el-descriptions__label {
  font-weight: bold;
  background-color: #f5f7fa;
}

.report-detail .el-descriptions__content {
  word-break: break-all;
}

/* 报告图表样式 */
.report-chart {
  margin-top: 20px;
}

.chart-container {
  position: relative;
  padding: 20px 0;
}

.chart-container .el-progress {
  margin-bottom: 20px;
}

.predicted-line {
  position: absolute;
  top: 30px;
  height: 2px;
  background-color: #E6A23C;
  width: 2px;
  display: flex;
  align-items: center;
  justify-content: center;
  transform: translateX(-50%);
}

.predicted-line::before {
  content: '';
  position: absolute;
  top: -6px;
  left: 50%;
  transform: translateX(-50%);
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background-color: #E6A23C;
}

.predicted-line::after {
  content: attr(data-text);
  position: absolute;
  top: 20px;
  left: 50%;
  transform: translateX(-50%);
  background-color: #E6A23C;
  color: white;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  white-space: nowrap;
}

.dialog-footer {
  text-align: center;
}
</style>
