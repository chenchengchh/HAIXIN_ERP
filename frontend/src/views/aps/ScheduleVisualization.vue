<template>
  <div class="schedule-visualization"
       @touchstart="handleTouchStart"
       @touchmove="handleTouchMove"
       @touchend="handleTouchEnd"
       :style="{ transform: isPullToRefresh ? `translateY(${pullDistance}px)` : '' }">
    
    <!-- 下拉刷新指示器 -->
    <div v-if="isPullToRefresh && pullDistance > 20" class="pull-to-refresh-indicator" 
         :style="{ opacity: pullDistance / 100 }">
      <el-icon class="refresh-icon" :class="{ 'refresh-rotate': pullDistance > 50 }">
        <Refresh />
      </el-icon>
      <span class="refresh-text">{{ pullDistance > 50 ? '释放刷新' : '下拉刷新' }}</span>
    </div>
    
    <!-- 页面加载状态 -->
    <div v-if="isLoading" class="loading-overlay">
      <el-card class="loading-card">
        <div class="loading-content">
          <el-icon class="loading-icon"><Loading /></el-icon>
          <p class="loading-text">{{ loadingText }}</p>
          <el-progress :percentage="loadingPercentage" :stroke-width="4" />
        </div>
      </el-card>
    </div>
    
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>排程可视化</h2>
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/aps' }">APS系统</el-breadcrumb-item>
        <el-breadcrumb-item>排程可视化</el-breadcrumb-item>
      </el-breadcrumb>
    </div>
    
    <!-- 主要内容区域 -->
    <div class="main-content">
      <!-- 左侧工具栏 -->
      <div class="left-panel">
        <el-card shadow="hover">
          <template #header>
            <div class="panel-header">
              <span>排程控制</span>
            </div>
          </template>
          
          <!-- 计划选择 -->
          <el-form label-position="top" size="small" :model="scheduleParams">
            <el-form-item label="生产计划">
              <el-select v-model="scheduleParams.planId" placeholder="选择生产计划" @change="onPlanChange">
                <el-option
                  v-for="plan in planOptions"
                  :key="plan.id"
                  :label="`${plan.planNo} ${plan.planName}`"
                  :value="plan.id">
                </el-option>
              </el-select>
            </el-form-item>
            
            <!-- 排程算法选择 -->
            <el-form-item label="排程算法">
              <el-select v-model="scheduleParams.algorithm" placeholder="选择排程算法">
                <el-option label="优先级算法" value="PRIORITY"></el-option>
                <el-option label="最早完成时间" value="EDD"></el-option>
                <el-option label="关键路径法" value="CPM"></el-option>
              </el-select>
            </el-form-item>
            
            <!-- 优化目标 -->
            <el-form-item label="优化目标">
              <el-select v-model="scheduleParams.objectives" multiple placeholder="选择优化目标">
                <el-option label="最小化总工期" value="MIN_TOTAL_DURATION"></el-option>
                <el-option label="最小化延期订单" value="MIN_DELAYED_ORDERS"></el-option>
                <el-option label="最大化资源利用率" value="MAX_RESOURCE_UTILIZATION"></el-option>
                <el-option label="平衡资源负荷" value="BALANCE_RESOURCE_LOAD"></el-option>
              </el-select>
            </el-form-item>
            
            <!-- 执行排程按钮 -->
            <el-form-item>
              <el-button type="primary" @click="executeScheduling" :loading="isExecuting">
                <el-icon v-if="isExecuting"><Loading /></el-icon>
                执行排程
              </el-button>
            </el-form-item>
            
            <!-- 执行日志 -->
            <el-form-item label="执行日志">
              <div class="execution-log" v-if="executionLog.length > 0">
                <el-timeline>
                  <el-timeline-item v-for="(log, index) in executionLog" :key="index" :timestamp="log.time">
                    {{ log.message }}
                  </el-timeline-item>
                </el-timeline>
              </div>
              <el-empty description="暂无执行日志" v-else></el-empty>
            </el-form-item>
          </el-form>
        </el-card>
        
        <!-- 任务详情 -->
        <el-card shadow="hover" class="mt-20">
          <template #header>
            <div class="panel-header">
              <span>任务详情</span>
            </div>
          </template>
          <div v-if="selectedTask" class="task-details">
            <el-descriptions :column="1" size="small">
              <el-descriptions-item label="任务名称">{{ selectedTask.text }}</el-descriptions-item>
              <el-descriptions-item label="开始时间">{{ formatDate(selectedTask.start_date) }}</el-descriptions-item>
              <el-descriptions-item label="结束时间">{{ formatDate(selectedTask.end_date) }}</el-descriptions-item>
              <el-descriptions-item label="持续时间">{{ selectedTask.duration }} 小时</el-descriptions-item>
              <el-descriptions-item label="资源">{{ selectedTask.resource_name || '未分配' }}</el-descriptions-item>
              <el-descriptions-item label="状态">{{ getStatusText(selectedTask.status) }}</el-descriptions-item>
              <el-descriptions-item label="优先级">{{ selectedTask.priority }}</el-descriptions-item>
            </el-descriptions>
          </div>
          <el-empty description="请选择一个任务查看详情" v-else></el-empty>
        </el-card>
      </div>
      
      <!-- 右侧甘特图区域 -->
      <div class="right-panel">
        <!-- 甘特图组件 -->
        <el-card shadow="hover" class="gantt-card">
          <template #header>
            <div class="panel-header">
              <span>甘特图</span>
              <el-button type="info" size="small" @click="exportGanttChart">
                <el-icon><Download /></el-icon>
                导出甘特图
              </el-button>
            </div>
          </template>
          
          <div class="gantt-wrapper">
            <GanttChart
              :tasks="ganttTasks"
              :links="ganttLinks"
              :plan-id="scheduleParams.planId"
              :ws-url="websocketUrl"
              @task-selected="onTaskSelected"
              @task-updated="onTaskUpdated"
              @data-refreshed="fetchScheduleData"
              @realtime-event="handleRealtimeEvent"
            />
          </div>
        </el-card>
        
        <!-- 资源负荷图 -->
        <el-card shadow="hover" class="mt-20">
          <template #header>
            <div class="panel-header">
              <span>资源负荷分析</span>
            </div>
          </template>
          <div class="resource-load-chart-container">
            <ResourceLoadChart :plan-id="scheduleParams.planId" />
          </div>
        </el-card>
        
        <!-- What-If分析 -->
        <el-card shadow="hover" class="mt-20">
          <template #header>
            <div class="panel-header">
              <span>What-If 场景分析</span>
            </div>
          </template>
          <div class="what-if-analysis-container">
            <WhatIfAnalysis />
          </div>
        </el-card>
        
        <!-- 实时监控面板 -->
        <el-card shadow="hover" class="mt-20">
          <template #header>
            <div class="panel-header">
              <span>实时监控面板</span>
            </div>
          </template>
          <div class="realtime-monitoring-container">
            <RealtimeMonitoring :plan-id="scheduleParams.planId" />
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onBeforeUnmount, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Loading, Download, Refresh } from '@element-plus/icons-vue'
// 使用正确的相对路径导入
import GanttChart from '../../components/business/GanttChart.vue'
import ResourceLoadChart from '../../components/business/ResourceLoadChart.vue'
import WhatIfAnalysis from '../../components/business/WhatIfAnalysis.vue'
import RealtimeMonitoring from '../../components/business/RealtimeMonitoring.vue'
import { ProductionPlanAPI, ScheduleResultAPI, SchedulingEngineAPI } from '@/api/aps'
import { unwrapListResponse, unwrapResponseData } from '@/api'

// 定义本地类型，避免直接依赖dhtmlx-gantt的类型定义
type GanttTask = {
  id: number | string
  text: string
  start_date: string | Date
  end_date: string | Date
  duration: number
  resource_name?: string
  status: string
  priority: number
  [key: string]: any
}

type GanttLink = {
  id: number | string
  source: number | string
  target: number | string
  type: number
  [key: string]: any
}

// 路由对象
const router = useRouter()
const route = useRoute()

// WebSocket配置
const websocketUrl = computed(() => '')

// 生产计划下拉选项（真实API数据）
const planOptions = ref<Array<{ id: number; planNo: string; planName: string }>>([])

// 排程参数
const scheduleParams = reactive({
  planId: '' as string | number,
  algorithm: 'PRIORITY',
  objectives: ['MIN_TOTAL_DURATION', 'MAX_RESOURCE_UTILIZATION']
})

// 当前排程结果ID（用于获取甘特图数据）
const currentResultId = ref<number | null>(null)

// 甘特图数据
const ganttTasks = ref<GanttTask[]>([])
const ganttLinks = ref<GanttLink[]>([])

// 执行状态
const isExecuting = ref(false)
const executionLog = ref<Array<{ time: string; message: string }>>([])

// 选中的任务
const selectedTask = ref<GanttTask | null>(null)

// 页面加载状态
const isLoading = ref(false)
const loadingText = ref('正在加载排程数据...')
const loadingPercentage = ref(0)
const isAutoRefreshEnabled = ref(true)
const autoRefreshInterval = ref(30000) // 默认30秒自动刷新
const autoRefreshTimer = ref<number | null>(null)
const lastRefreshTime = ref<Date | null>(null)

// 数据缓存
const dataCache = ref<Map<string, any>>(new Map())
const cacheExpirationTime = ref<Map<string, Date>>(new Map())
const CACHE_TTL = 30000 // 缓存有效期30秒

// 下拉刷新状态
const isPullToRefresh = ref(false)
const pullStartY = ref(0)
const pullDistance = ref(0)
const isRefreshing = ref(false)

// 初始化数据：加载生产计划列表，选中第一个计划并加载其排程数据
onMounted(() => {
  loadPlanOptions()
  // 初始化自动刷新
  initAutoRefresh()
})

/**
 * 加载生产计划下拉选项
 */
const loadPlanOptions = async () => {
  try {
    const res = await ProductionPlanAPI.getProductionPlans()
    const plans = unwrapListResponse<any>(res)
    planOptions.value = plans.map((p: any) => ({
      id: p.id,
      planNo: p.planNo,
      planName: p.planName
    }))
    // 优先使用路由query中的planId（从计划生成页"查看完整甘特图"跳转），否则默认选中第一个计划
    const queryPlanId = Number(route.query.planId)
    const targetPlan = queryPlanId
      ? planOptions.value.find(p => p.id === queryPlanId)
      : undefined
    const firstPlan = planOptions.value[0]
    if (targetPlan) {
      scheduleParams.planId = targetPlan.id
      await fetchScheduleData()
    } else if (firstPlan && !scheduleParams.planId) {
      scheduleParams.planId = firstPlan.id
      await fetchScheduleData()
    }
  } catch (error) {
    console.error('加载生产计划列表失败:', error)
    ElMessage.error('加载生产计划列表失败')
  }
}

/**
 * 切换生产计划：重新加载该计划的排程数据
 */
const onPlanChange = () => {
  currentResultId.value = null
  ganttTasks.value = []
  ganttLinks.value = []
  fetchScheduleData()
}

// 组件卸载时清理
onBeforeUnmount(() => {
  // 停止自动刷新
  stopAutoRefresh()
})

// 获取排程数据：先查询该计划最新排程结果，再获取其甘特图数据
const fetchScheduleData = async (forceRefresh = false) => {
  if (!scheduleParams.planId) {
    return
  }

  isLoading.value = true
  isRefreshing.value = true
  loadingText.value = '正在加载排程数据...'
  loadingPercentage.value = 0

  try {
    // 1. 查询该计划的排程结果列表，取最新一条
    const resultRes = await ScheduleResultAPI.getScheduleResultsByPlanId(scheduleParams.planId)
    const resultList = unwrapListResponse<any>(resultRes)

    if (resultList.length === 0) {
      // 该计划尚未排程，清空甘特图
      ganttTasks.value = []
      ganttLinks.value = []
      currentResultId.value = null
      executionLog.value.unshift({
        time: new Date().toLocaleString(),
        message: '当前计划暂无排程结果，请先执行排程'
      })
      return
    }

    // 按创建时间倒序取最新
    const latest = resultList.sort((a: any, b: any) =>
      new Date(b.createdTime).getTime() - new Date(a.createdTime).getTime()
    )[0]
    currentResultId.value = latest.id
    loadingPercentage.value = 40

    // 2. 获取甘特图数据
    const ganttRes = await ScheduleResultAPI.getGanttData(latest.id)
    const ganttData = unwrapResponseData<any>(ganttRes) ?? {}
    ganttTasks.value = ganttData?.tasks ?? []
    ganttLinks.value = ganttData?.links ?? []
    loadingPercentage.value = 80

    // 更新最后刷新时间
    lastRefreshTime.value = new Date()

    // 添加日志
    executionLog.value.unshift({
      time: new Date().toLocaleString(),
      message: `排程数据刷新成功（${latest.scheduleNo}，共${ganttTasks.value.length}个任务）`
    })

    // 限制日志数量
    if (executionLog.value.length > 10) {
      executionLog.value = executionLog.value.slice(0, 10)
    }

    // 完成加载
    loadingPercentage.value = 100

  } catch (error) {
    console.error('获取排程数据失败:', error)
    // 添加错误日志
    executionLog.value.unshift({
      time: new Date().toLocaleString(),
      message: `数据加载失败: ${error instanceof Error ? error.message : String(error)}`
    })

    // 显示错误提示
    ElMessage.error('获取排程数据失败，请稍后重试')

    // 限制日志数量
    if (executionLog.value.length > 10) {
      executionLog.value = executionLog.value.slice(0, 10)
    }
  } finally {
    setTimeout(() => {
      isLoading.value = false
      isRefreshing.value = false
    }, 300)
  }
}

// 初始化自动刷新
const initAutoRefresh = () => {
  if (isAutoRefreshEnabled.value) {
    startAutoRefresh()
  }
}

// 开始自动刷新
const startAutoRefresh = () => {
  if (autoRefreshTimer.value) {
    clearInterval(autoRefreshTimer.value)
  }
  
  autoRefreshTimer.value = window.setInterval(() => {
    fetchScheduleData()
  }, autoRefreshInterval.value) as unknown as number
}

// 停止自动刷新
const stopAutoRefresh = () => {
  if (autoRefreshTimer.value) {
    clearInterval(autoRefreshTimer.value)
    autoRefreshTimer.value = null
  }
}

// 切换自动刷新状态
const toggleAutoRefresh = () => {
  isAutoRefreshEnabled.value = !isAutoRefreshEnabled.value
  if (isAutoRefreshEnabled.value) {
    startAutoRefresh()
  } else {
    stopAutoRefresh()
  }
}

// 优化的下拉刷新
const handlePullToRefresh = () => {
  if (isRefreshing.value) return
  
  isPullToRefresh.value = true
  fetchScheduleData(true) // 强制刷新
}

// 局部数据更新
const updatePartialData = (data: { tasks?: GanttTask[]; links?: GanttLink[] }) => {
  console.log('局部更新数据:', data)
  
  // 只更新变化的数据，避免全量刷新
  if (data.tasks) {
    // 更新任务数据
    data.tasks.forEach(updatedTask => {
      const index = ganttTasks.value.findIndex(task => task.id === updatedTask.id)
      if (index !== -1) {
        ganttTasks.value[index] = { ...ganttTasks.value[index], ...updatedTask }
      } else {
        ganttTasks.value.push(updatedTask)
      }
    })
  }
  
  if (data.links) {
    // 更新依赖关系数据
    data.links.forEach(updatedLink => {
      const index = ganttLinks.value.findIndex(link => link.id === updatedLink.id)
      if (index !== -1) {
        ganttLinks.value[index] = { ...ganttLinks.value[index], ...updatedLink }
      } else {
        ganttLinks.value.push(updatedLink)
      }
    })
  }
}

// 清除数据缓存
const clearCache = () => {
  dataCache.value.clear()
  cacheExpirationTime.value.clear()
  ElMessage.success('数据缓存已清除')
}

// 执行排程：调用后端排程引擎真实执行
const executeScheduling = async () => {
  if (!scheduleParams.planId) {
    ElMessage.warning('请先选择生产计划')
    return
  }

  isExecuting.value = true
  loadingText.value = '正在执行排程计算...'

  try {
    // 添加日志
    executionLog.value.unshift({
      time: new Date().toLocaleString(),
      message: `开始执行排程，算法：${scheduleParams.algorithm}`
    })

    // 真实API调用排程引擎
    const res = await SchedulingEngineAPI.executeAlgorithm({
      algorithmName: scheduleParams.algorithm,
      planId: scheduleParams.planId,
      params: {}
    })
    const execResult = unwrapResponseData<any>(res)

    // 添加日志
    executionLog.value.unshift({
      time: new Date().toLocaleString(),
      message: `排程计算完成（${execResult?.scheduleNo ?? ''}）`
    })

    // 限制日志数量
    if (executionLog.value.length > 10) {
      executionLog.value = executionLog.value.slice(0, 10)
    }

    // 刷新数据
    await fetchScheduleData(true)

    // 显示成功提示
    ElMessage.success('排程执行成功！')

  } catch (error) {
    console.error('执行排程失败:', error)
    // 添加错误日志
    executionLog.value.unshift({
      time: new Date().toLocaleString(),
      message: `排程执行失败: ${error instanceof Error ? error.message : String(error)}`
    })
    // 限制日志数量
    if (executionLog.value.length > 10) {
      executionLog.value = executionLog.value.slice(0, 10)
    }

    // 显示错误提示
    ElMessage.error('排程执行失败，请检查参数设置')
  } finally {
    isExecuting.value = false
  }
}

// 任务选择事件
const onTaskSelected = (taskId: string | number) => {
  selectedTask.value = ganttTasks.value.find(task => task.id === taskId) || null
  
  // 移动端震动反馈（如果支持）
  if ('vibrate' in navigator) {
    navigator.vibrate(50)
  }
}

// 任务更新事件
const onTaskUpdated = (task: GanttTask) => {
  console.log('任务更新:', task)
  // 这里应该调用API保存更新后的任务
}

// 处理实时事件
const handleRealtimeEvent = (event: any) => {
  console.log('收到实时事件:', event)
  // 根据事件类型执行相应的操作
  switch (event.type) {
    case 'taskUpdate':
      // 任务更新事件，已经在甘特图组件中处理
      break
    case 'scheduleUpdate':
      // 排程更新事件，已经在甘特图组件中处理
      break
    case 'resourceUpdate':
      // 资源更新事件，可以在这里更新资源相关数据
      break
    case 'statusUpdate':
      // 状态更新事件，可以在这里更新系统状态
      break
    default:
      console.log('未知实时事件类型:', event.type)
  }
}

// 导出甘特图
const exportGanttChart = () => {
  console.log('导出甘特图')
  // 这里应该实现甘特图导出功能
}

// 格式化日期
const formatDate = (date: string | Date | undefined | null) => {
  if (!date) return ''
  return new Date(date).toLocaleString('zh-CN')
}

// 获取状态文本
const getStatusText = (status: string) => {
  const statusMap = {
    planned: '计划中',
    in_progress: '进行中',
    done: '已完成',
    delayed: '延迟',
    canceled: '已取消'
  }
  return statusMap[status as keyof typeof statusMap] || status
}

// 触摸事件处理 - 下拉刷新
const handleTouchStart = (event: TouchEvent) => {
  // 只有在页面顶部才能触发下拉刷新
  const touches = (event as any).touches
  if (window.scrollY === 0 && touches && touches.length > 0) {
    pullStartY.value = touches[0].clientY
    isPullToRefresh.value = true
  }
}

const handleTouchMove = (event: TouchEvent) => {
  const touches = (event as any).touches
  if (!isPullToRefresh.value || !touches || touches.length === 0) return
  
  const currentY = touches[0].clientY
  const distance = currentY - pullStartY.value
  
  // 只处理向下的拉动
  if (distance > 0) {
    pullDistance.value = Math.min(distance, 100) // 最大拉动距离100px
    event.preventDefault() // 防止默认滚动行为
  }
}

const handleTouchEnd = () => {
  if (!isPullToRefresh.value) return
  
  // 如果拉动距离超过阈值，触发刷新
  if (pullDistance.value > 50) {
    fetchScheduleData()
  }
  
  // 重置状态
  isPullToRefresh.value = false
  pullDistance.value = 0
}
</script>

<style scoped>
.schedule-visualization {
  padding: 20px;
  height: 100%;
  background-color: #f5f7fa;
  box-sizing: border-box;
  transition: transform 0.2s ease-out;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #e4e7ed;
}

.page-header h2 {
  margin: 0;
  color: #333;
  font-size: 1.5rem;
}

.main-content {
  display: flex;
  gap: 20px;
  height: calc(100% - 80px);
  overflow: hidden;
}

.left-panel {
  width: 320px;
  display: flex;
  flex-direction: column;
  overflow-y: auto;
  flex-shrink: 0;
  box-sizing: border-box;
}

.right-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  min-width: 0; /* 防止flex子元素溢出 */
  box-sizing: border-box;
}

.gantt-card {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.gantt-wrapper {
  flex: 1;
  overflow: hidden;
  min-height: 500px;
  display: flex;
  flex-direction: column;
}

.resource-load-chart-container {
  height: 400px;
  flex-shrink: 0;
}

.what-if-analysis-container {
  height: 500px;
  flex-shrink: 0;
}

.realtime-monitoring-container {
  height: 600px;
  flex-shrink: 0;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.mt-20 {
  margin-top: 20px;
}

.gantt-card :deep(.el-card__body) {
  padding: 0;
  display: flex;
  flex-direction: column;
  height: 100%;
}

.gantt-card .panel-header {
  padding: 10px 20px;
  flex-shrink: 0;
}

.gantt-card .gantt-wrapper {
  padding: 0 20px 20px;
  flex: 1;
  overflow: hidden; /* 甘特图内部自管理滚动，外层hidden避免双重滚动条与高度累加 */
}

.execution-log {
  max-height: 200px;
  overflow-y: auto;
  font-size: 12px;
  box-sizing: border-box;
}

.task-details {
  padding: 10px 0;
}

/* 确保左侧面板内容不会溢出 */
.left-panel :deep(.el-card) {
  box-sizing: border-box;
}

.left-panel :deep(.el-card__body) {
  padding: 15px;
  box-sizing: border-box;
}

.left-panel :deep(.el-form-item) {
  margin-bottom: 15px;
}

/* 改进执行日志的滚动效果 */
.execution-log :deep(.el-timeline-item) {
  padding-bottom: 10px;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .left-panel {
    width: 280px;
  }
  
  .main-content {
    gap: 16px;
  }
}

@media (max-width: 992px) {
  .main-content {
    flex-direction: column;
    height: auto;
    overflow: visible;
  }
  
  .left-panel {
    width: 100%;
    max-height: 500px;
    margin-bottom: 20px;
    overflow-y: auto;
  }
  
  .right-panel {
    height: auto;
    min-height: 800px;
  }
  
  .gantt-card {
    height: 100%;
  }
  
  .gantt-wrapper {
    min-height: 400px;
  }
}

@media (max-width: 768px) {
  .schedule-visualization {
    padding: 12px;
    height: auto;
    min-height: 100vh;
  }
  
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
    margin-bottom: 15px;
  }
  
  .page-header h2 {
    font-size: 1.2rem;
    margin: 0;
  }
  
  .main-content {
    gap: 12px;
    height: auto;
    flex-direction: column;
  }
  
  .left-panel {
    width: 100%;
    max-height: none;
    margin-bottom: 15px;
    overflow-y: auto;
  }
  
  .right-panel {
    height: auto;
    min-height: 600px;
  }
  
  .gantt-wrapper {
    min-height: 300px;
  }
  
  .resource-load-chart {
    height: 250px;
  }
  
  .mt-20 {
    margin-top: 15px;
  }
  
  .gantt-card .panel-header {
    padding: 10px 15px;
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .gantt-card .gantt-wrapper {
    padding: 0 15px 15px;
  }
}

/* 加载状态样式 */
.loading-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(255, 255, 255, 0.9);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 9999;
}

.loading-card {
  width: 300px;
  text-align: center;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.loading-content {
  padding: 30px 20px;
}

.loading-icon {
  font-size: 48px;
  color: #409eff;
  animation: loading-rotate 1.5s linear infinite;
  margin-bottom: 20px;
}

.loading-text {
  color: #606266;
  font-size: 16px;
  margin-bottom: 20px;
}

@keyframes loading-rotate {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

/* 移动端适配 */
@media (max-width: 480px) {
  .schedule-visualization {
    padding: 8px;
    min-height: 100vh;
    overflow-x: hidden;
  }
  
  .page-header {
    margin-bottom: 10px;
    padding-bottom: 8px;
    position: sticky;
    top: 0;
    background: #f5f7fa;
    z-index: 100;
    padding-top: 8px;
  }
  
  .main-content {
    gap: 8px;
    overflow-x: hidden;
  }
  
  .left-panel :deep(.el-card__body) {
    padding: 10px;
  }
  
  .left-panel :deep(.el-form-item__label) {
    font-size: 14px;
    padding-bottom: 4px;
  }
  
  .left-panel :deep(.el-select) {
    width: 100%;
  }
  
  .left-panel :deep(.el-button) {
    width: 100%;
    margin-top: 8px;
  }
  
  .gantt-wrapper {
    min-height: 250px;
    overflow-x: auto;
  }
  
  .resource-load-chart {
    height: 200px;
  }
  
  .gantt-card .panel-header {
    padding: 8px 12px;
    flex-direction: column;
    align-items: stretch;
    gap: 8px;
  }
  
  .gantt-card .panel-header .el-button {
    width: 100%;
    margin-left: 0;
  }
  
  .gantt-card .gantt-wrapper {
    padding: 0 10px 10px;
  }
  
  .loading-card {
    width: 250px;
  }
  
  .loading-content {
    padding: 20px 15px;
  }
  
  .loading-icon {
    font-size: 36px;
  }
  
  /* 触摸优化 */
  .left-panel :deep(.el-select-dropdown__item) {
    padding: 12px 15px;
    font-size: 16px;
  }
  
  .left-panel :deep(.el-timeline-item__node) {
    width: 14px;
    height: 14px;
  }
  
  .left-panel :deep(.el-timeline-item__tail) {
    left: 6px;
  }
  
  .execution-log {
    max-height: 150px;
    font-size: 13px;
  }
  
  .task-details :deep(.el-descriptions__label) {
    font-size: 13px;
    width: 80px;
  }
  
  .task-details :deep(.el-descriptions__content) {
    font-size: 13px;
  }
  
  /* 横向滚动优化 */
  .right-panel {
    overflow-x: auto;
  }
  
  .el-card {
    min-width: 320px;
  }
  
  /* 按钮触摸反馈 */
  .left-panel :deep(.el-button:hover) {
    transform: scale(1.02);
    transition: transform 0.1s ease;
  }
  
  .left-panel :deep(.el-button:active) {
    transform: scale(0.98);
  }
  
  /* 卡片触摸反馈 */
  .el-card {
    transition: transform 0.2s ease, box-shadow 0.2s ease;
  }
  
  .el-card:active {
    transform: scale(0.995);
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  }
  
  /* 滚动条优化 */
  .left-panel::-webkit-scrollbar {
    width: 4px;
  }
  
  .left-panel::-webkit-scrollbar-track {
    background: #f1f1f1;
    border-radius: 2px;
  }
  
  .left-panel::-webkit-scrollbar-thumb {
    background: #c1c1c1;
    border-radius: 2px;
  }
  
  .left-panel::-webkit-scrollbar-thumb:hover {
    background: #a8a8a8;
  }
  
  /* 表单元素触摸优化 */
  .left-panel :deep(.el-input__inner) {
    font-size: 16px; /* 防止iOS缩放 */
    padding: 8px 12px;
  }
  
  .left-panel :deep(.el-select .el-input__inner) {
    font-size: 16px;
  }
  
  /* 时间轴触摸优化 */
  .left-panel :deep(.el-timeline-item) {
    padding: 8px 0;
  }
  
  .left-panel :deep(.el-timeline-item__timestamp) {
    font-size: 12px;
    margin-bottom: 4px;
  }
}

/* 下拉刷新指示器 */
.pull-to-refresh-indicator {
  position: fixed;
  top: 10px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  align-items: center;
  gap: 8px;
  background: rgba(255, 255, 255, 0.95);
  padding: 8px 16px;
  border-radius: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  z-index: 1000;
  transition: opacity 0.2s ease;
}

.refresh-icon {
  font-size: 18px;
  color: #409eff;
  transition: transform 0.3s ease;
}

.refresh-icon.refresh-rotate {
  transform: rotate(180deg);
}

.refresh-text {
  font-size: 14px;
  color: #606266;
}
</style>
