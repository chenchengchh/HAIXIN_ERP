<template>
  <div class="realtime-monitoring">
    <!-- 控制面板 -->
    <div class="control-panel">
      <div class="control-group">
        <el-select v-model="selectedEquipmentIds" multiple placeholder="选择设备" size="small" style="width: 200px;">
          <el-option
            v-for="equipment in equipmentList"
            :key="equipment.id"
            :label="equipment.name"
            :value="equipment.id"
          />
        </el-select>
        
        <el-select v-model="refreshInterval" placeholder="刷新间隔" size="small" style="width: 120px; margin-left: 10px;" @change="handleRefreshIntervalChange">
          <el-option label="5秒" :value="5000" />
          <el-option label="10秒" :value="10000" />
          <el-option label="30秒" :value="30000" />
          <el-option label="1分钟" :value="60000" />
          <el-option label="5分钟" :value="300000" />
        </el-select>
        
        <el-button type="primary" size="small" @click="toggleAutoRefresh" style="margin-left: 10px;">
          <el-icon v-if="isAutoRefreshEnabled"><RefreshRight /></el-icon>
          <el-icon v-else><Refresh /></el-icon>
          {{ isAutoRefreshEnabled ? '停止刷新' : '自动刷新' }}
        </el-button>
        
        <el-button type="info" size="small" @click="manualRefresh" :loading="isLoading" style="margin-left: 10px;">
          <el-icon v-if="isLoading"><Loading /></el-icon>
          手动刷新
        </el-button>
      </div>
      
      <div class="control-group">
        <el-tag type="info" size="small">
          <el-icon><Clock /></el-icon>
          上次更新: {{ lastUpdateTime }}
        </el-tag>
      </div>
    </div>
    
    <!-- 关键指标卡片 -->
    <div class="metrics-cards">
      <!-- 生产进度 -->
      <el-card class="metric-card">
        <template #header>
          <div class="card-header">
            <span>生产进度</span>
            <el-icon><TrendCharts /></el-icon>
          </div>
        </template>
        <div class="card-content">
          <div class="metric-value">{{ productionProgress }}%</div>
          <el-progress :percentage="productionProgress" :stroke-width="10" :status="getProgressStatus(productionProgress)" />
          <div class="metric-desc">总生产数量: {{ totalProductionQty }} / {{ plannedProductionQty }}</div>
        </div>
      </el-card>
      
      <!-- 设备利用率 -->
      <el-card class="metric-card">
        <template #header>
          <div class="card-header">
            <span>设备利用率</span>
            <el-icon><Setting /></el-icon>
          </div>
        </template>
        <div class="card-content">
          <div class="metric-value">{{ equipmentUtilization }}%</div>
          <el-progress :percentage="equipmentUtilization" :stroke-width="10" :status="getUtilizationStatus(equipmentUtilization)" />
          <div class="metric-desc">运行设备: {{ runningEquipmentCount }} / {{ totalEquipmentCount }}</div>
        </div>
      </el-card>
      
      <!-- 准时交付率 -->
      <el-card class="metric-card">
        <template #header>
          <div class="card-header">
            <span>准时交付率</span>
            <el-icon><Timer /></el-icon>
          </div>
        </template>
        <div class="card-content">
          <div class="metric-value">{{ onTimeDeliveryRate }}%</div>
          <el-progress :percentage="onTimeDeliveryRate" :stroke-width="10" :status="getDeliveryRateStatus(onTimeDeliveryRate)" />
          <div class="metric-desc">准时订单: {{ onTimeOrders }} / {{ totalOrders }}</div>
        </div>
      </el-card>
      
      <!-- 质量合格率 -->
      <el-card class="metric-card">
        <template #header>
          <div class="card-header">
            <span>质量合格率</span>
            <el-icon><CircleCheck /></el-icon>
          </div>
        </template>
        <div class="card-content">
          <div class="metric-value">{{ qualityPassRate }}%</div>
          <el-progress :percentage="qualityPassRate" :stroke-width="10" :status="getQualityRateStatus(qualityPassRate)" />
          <div class="metric-desc">合格产品: {{ qualifiedProducts }} / {{ totalInspectedProducts }}</div>
        </div>
      </el-card>
    </div>
    
    <!-- 设备状态监控 -->
    <div class="equipment-monitoring">
      <el-card shadow="hover" class="section-card">
        <template #header>
          <div class="section-header">
            <span>设备状态监控</span>
            <el-dropdown trigger="click">
              <el-button type="primary" size="small">
                查看详情 <el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="viewAllEquipment">查看所有设备</el-dropdown-item>
                  <el-dropdown-item @click="viewFaultyEquipment">查看故障设备</el-dropdown-item>
                  <el-dropdown-item @click="viewIdleEquipment">查看闲置设备</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </template>
        
        <div class="equipment-list">
          <el-card
            v-for="equipment in equipmentStatusList"
            :key="equipment.id"
            class="equipment-card"
            :shadow="equipment.status === 'running' ? 'hover' : 'always'"
            :class="`status-${equipment.status}`"
          >
            <div class="equipment-header">
              <div class="equipment-info">
                <div class="equipment-name">{{ equipment.name }}</div>
                <div class="equipment-type">{{ equipment.type }}</div>
              </div>
              <el-tag
                :type="getStatusTagType(equipment.status)"
                size="small"
              >
                {{ getStatusText(equipment.status) }}
              </el-tag>
            </div>
            
            <div class="equipment-status">
              <div class="status-item">
                <span class="status-label">运行时长:</span>
                <span class="status-value">{{ equipment.runningTime }} 小时</span>
              </div>
              <div class="status-item">
                <span class="status-label">当前任务:</span>
                <span class="status-value">{{ equipment.currentTask || '无' }}</span>
              </div>
              <div class="status-item">
                <span class="status-label">生产数量:</span>
                <span class="status-value">{{ equipment.productionQty }} 件</span>
              </div>
              <div class="status-item">
                <span class="status-label">温度:</span>
                <span class="status-value">{{ equipment.temperature }}°C</span>
              </div>
            </div>
          </el-card>
        </div>
      </el-card>
    </div>
    
    <!-- 生产进度趋势图 -->
    <div class="trend-chart-section">
      <el-card shadow="hover" class="section-card">
        <template #header>
          <div class="section-header">
            <span>生产进度趋势</span>
            <el-select v-model="chartTimeRange" placeholder="时间范围" size="small" @change="handleChartTimeRangeChange">
              <el-option label="今天" value="today" />
              <el-option label="本周" value="week" />
              <el-option label="本月" value="month" />
            </el-select>
          </div>
        </template>
        
        <div class="chart-container">
          <div v-if="isLoading" class="loading-indicator">
            <el-icon class="loading-icon"><Loading /></el-icon>
            <span class="loading-text">加载中...</span>
          </div>
          <div v-else>
            <div ref="trendChartRef" class="trend-chart"></div>
          </div>
        </div>
      </el-card>
    </div>
    
    <!-- 告警信息 -->
    <div class="alerts-section">
      <el-card shadow="hover" class="section-card">
        <template #header>
          <div class="section-header">
            <span>告警信息</span>
            <el-badge :value="activeAlerts" type="danger" :max="99"></el-badge>
          </div>
        </template>
        
        <div class="alerts-container">
          <div v-if="alerts.length === 0" class="no-alerts">
            <el-empty description="暂无告警信息" />
          </div>
          <div v-else>
            <el-timeline>
              <el-timeline-item
                v-for="alert in alerts"
                :key="alert.id"
                :timestamp="alert.timestamp"
                :type="getAlertType(alert.severity)"
              >
                <el-card class="alert-card" :class="`severity-${alert.severity}`">
                  <div class="alert-content">
                    <div class="alert-title">{{ alert.title }}</div>
                    <div class="alert-message">{{ alert.message }}</div>
                    <div class="alert-meta">
                      <span class="alert-source">{{ alert.source }}</span>
                      <el-tag :type="getSeverityTagType(alert.severity)" size="small">
                        {{ getSeverityText(alert.severity) }}
                      </el-tag>
                    </div>
                  </div>
                </el-card>
              </el-timeline-item>
            </el-timeline>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, watch, computed } from 'vue'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import api, { unwrapListResponse } from '../../api/index'
import {
  RefreshRight,
  Refresh,
  Loading,
  Clock,
  TrendCharts,
  Setting,
  Timer,
  CircleCheck,
  ArrowDown,
  TrendCharts as TrendingUp,
  Warning,
  CircleCloseFilled as ErrorFilled
} from '@element-plus/icons-vue'

// 定义类型
interface Equipment {
  id: string | number
  name: string
  type: string
  status: 'running' | 'idle' | 'faulty' | 'maintenance'
  runningTime: number
  currentTask?: string
  productionQty: number
  temperature: number
}

interface Alert {
  id: string | number
  title: string
  message: string
  severity: 'info' | 'warning' | 'error'
  timestamp: string
  source: string
}

// 定义props
const props = defineProps<{
  planId: string | number
}>()

// 定义事件
const emit = defineEmits<{
  (e: 'equipment-selected', equipmentId: string | number): void
  (e: 'alert-clicked', alertId: string | number): void
  (e: 'data-refreshed'): void
}>()

// 状态管理
const isLoading = ref(false)
const isAutoRefreshEnabled = ref(true)
const refreshInterval = ref(10000) // 默认10秒刷新一次
const lastUpdateTime = ref('')
const selectedEquipmentIds = ref<(string | number)[]>([])
const chartTimeRange = ref('today')

// 设备列表（从后端设备状态接口加载，不再使用硬编码数据）
const equipmentList = ref<Equipment[]>([])

// 设备状态列表
const equipmentStatusList = ref<Equipment[]>([])

// 告警列表（从后端工艺参数监控数据生成，不再使用硬编码数据）
const alerts = ref<Alert[]>([])

// 生产报工记录（用于构建生产进度趋势图）
const reportList = ref<any[]>([])

// 关键指标（初始为0，由真实接口数据填充）
const productionProgress = ref(0) // 生产进度百分比
const totalProductionQty = ref(0)
const plannedProductionQty = ref(0)
const equipmentUtilization = ref(0) // 设备利用率百分比
const runningEquipmentCount = ref(0)
const totalEquipmentCount = ref(0)
const onTimeDeliveryRate = ref(0) // 准时交付率百分比
const onTimeOrders = ref(0)
const totalOrders = ref(0)
const qualityPassRate = ref(0) // 质量合格率百分比
const qualifiedProducts = ref(0)
const totalInspectedProducts = ref(0)

// 图表实例
let trendChartInstance: echarts.ECharts | null = null
const trendChartRef = ref<HTMLElement | null>(null)

// 自动刷新定时器
let autoRefreshTimer: number | null = null

// 计算属性
const activeAlerts = computed(() => {
  return alerts.value.filter(alert => alert.severity === 'warning' || alert.severity === 'error').length
})

// 初始化自动刷新
const initAutoRefresh = () => {
  if (isAutoRefreshEnabled.value) {
    startAutoRefresh()
  }
}

// 开始自动刷新
const startAutoRefresh = () => {
  if (autoRefreshTimer) {
    clearInterval(autoRefreshTimer)
  }
  
  autoRefreshTimer = window.setInterval(() => {
    fetchRealtimeData()
  }, refreshInterval.value)
}

// 停止自动刷新
const stopAutoRefresh = () => {
  if (autoRefreshTimer) {
    clearInterval(autoRefreshTimer)
    autoRefreshTimer = null
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

// 手动刷新数据
const manualRefresh = async () => {
  try {
    isLoading.value = true
    await fetchRealtimeData()
  } catch (error) {
    console.error('手动刷新数据失败:', error)
  } finally {
    isLoading.value = false
  }
}

/**
 * 获取实时数据
 * 并行调用后端 MES 真实接口（生产进度、设备状态、工艺参数、生产报工），
 * 所有指标均由真实数据聚合计算，接口异常时保持空数据状态，不生成任何模拟数据
 */
const fetchRealtimeData = async () => {
  try {
    const [progressResult, equipmentResult, paramsResult, reportResult] = await Promise.allSettled([
      api.get('/mes/production-progress'),
      api.get('/mes/equipment-status'),
      api.get('/mes/process-params'),
      api.get('/mes/reporting/list', { params: { page: 1, size: 100 } })
    ])

    // 生产进度列表（接口失败时按空数组处理）
    const progressList = progressResult.status === 'fulfilled' ? unwrapListResponse<any>(progressResult.value) : []
    // 设备状态列表
    const equipmentDataList = equipmentResult.status === 'fulfilled' ? unwrapListResponse<any>(equipmentResult.value) : []
    // 工艺参数列表
    const processParamList = paramsResult.status === 'fulfilled' ? unwrapListResponse<any>(paramsResult.value) : []
    // 生产报工列表
    reportList.value = reportResult.status === 'fulfilled' ? unwrapListResponse<any>(reportResult.value) : []

    // 基于真实数据更新各项指标
    updateMetrics(progressList, equipmentDataList)
    // 基于真实设备数据更新设备列表
    updateEquipmentStatus(equipmentDataList)
    // 基于真实工艺参数数据生成告警
    updateAlerts(processParamList)

    // 更新图表
    updateTrendChart()

    // 更新上次更新时间
    lastUpdateTime.value = new Date().toLocaleString()

    // 触发数据刷新事件
    emit('data-refreshed')
  } catch (error) {
    console.error('获取实时数据失败:', error)
    ElMessage.error('获取实时监控数据失败')
  }
}

/**
 * 更新指标数据
 * 由生产进度与设备状态真实数据聚合计算生产进度、设备利用率、准时交付率、质量合格率
 * @param progressList 生产进度列表
 * @param equipmentDataList 设备状态列表
 */
const updateMetrics = (progressList: any[], equipmentDataList: any[]) => {
  // 生产进度：完成数量合计 / 计划数量合计
  const totalPlan = progressList.reduce((sum, item) => sum + Number(item?.totalQty ?? 0), 0)
  const totalCompleted = progressList.reduce((sum, item) => sum + Number(item?.completedQty ?? 0), 0)
  plannedProductionQty.value = totalPlan
  totalProductionQty.value = totalCompleted
  productionProgress.value = totalPlan > 0 ? Math.round((totalCompleted / totalPlan) * 100) : 0

  // 设备利用率：运行中设备数 / 设备总数
  const runningCount = equipmentDataList.filter(item => mapBackendEquipmentStatus(item?.status) === 'running').length
  runningEquipmentCount.value = runningCount
  totalEquipmentCount.value = equipmentDataList.length
  equipmentUtilization.value = equipmentDataList.length > 0 ? Math.round((runningCount / equipmentDataList.length) * 100) : 0

  // 准时交付率：已完成工单数 / 工单总数
  const completedOrders = progressList.filter(item => item?.status === 'completed').length
  onTimeOrders.value = completedOrders
  totalOrders.value = progressList.length
  onTimeDeliveryRate.value = progressList.length > 0 ? Math.round((completedOrders / progressList.length) * 100) : 0

  // 质量合格率：报工合格数量 / 报工总投入数量
  const goodQty = reportList.value.reduce((sum, item) => sum + Number(item?.goodQty ?? 0), 0)
  const scrapQty = reportList.value.reduce((sum, item) => sum + Number(item?.scrapQty ?? 0), 0)
  const reworkQty = reportList.value.reduce((sum, item) => sum + Number(item?.reworkQty ?? 0), 0)
  const inspectedQty = goodQty + scrapQty + reworkQty
  qualifiedProducts.value = goodQty
  totalInspectedProducts.value = inspectedQty
  qualityPassRate.value = inspectedQty > 0 ? Math.round((goodQty / inspectedQty) * 100) : 0
}

/**
 * 映射后端设备状态到组件状态
 * 后端设备状态为 running/idle/down/maintenance，组件使用 faulty 表示故障
 * @param status 后端设备状态
 * @returns 组件设备状态
 */
const mapBackendEquipmentStatus = (status: any): Equipment['status'] => {
  const s = String(status ?? '').toLowerCase()
  if (s === 'running') return 'running'
  if (s === 'down' || s === 'faulty' || s === 'fault') return 'faulty'
  if (s === 'maintenance') return 'maintenance'
  return 'idle'
}

/**
 * 更新设备状态
 * 将后端设备状态数据映射为组件设备结构，并按选中设备过滤
 * @param equipmentDataList 后端设备状态列表
 */
const updateEquipmentStatus = (equipmentDataList: any[]) => {
  // 将后端设备数据映射为组件设备结构，后端未提供的字段（温度、产量等）以0展示
  equipmentList.value = equipmentDataList.map(item => ({
    id: item?.equipmentId ?? item?.id ?? '',
    name: String(item?.equipmentName ?? '未知设备'),
    type: String(item?.equipmentType ?? ''),
    status: mapBackendEquipmentStatus(item?.status),
    runningTime: Number(item?.uptime ?? 0),
    productionQty: Number(item?.productionQty ?? 0),
    temperature: Number(item?.temperature ?? 0)
  }))

  // 按选中设备过滤展示列表
  applyEquipmentFilter()
}

/**
 * 按选中设备ID过滤设备展示列表
 */
const applyEquipmentFilter = () => {
  equipmentStatusList.value = selectedEquipmentIds.value.length > 0
    ? equipmentList.value.filter(e => (selectedEquipmentIds.value as (string | number)[]).includes(e.id))
    : equipmentList.value
}

/**
 * 更新告警信息
 * 基于真实工艺参数监控数据生成告警：alarm映射为错误、warning映射为警告
 * @param processParamList 工艺参数列表
 */
const updateAlerts = (processParamList: any[]) => {
  alerts.value = processParamList
    .filter(item => item?.status === 'alarm' || item?.status === 'warning')
    .map((item, index) => ({
      id: item?.id ?? index,
      title: item?.status === 'alarm' ? '工艺参数超限告警' : '工艺参数接近边界',
      message: `${item?.workstationName ?? '未知工站'} ${item?.parameterName ?? '参数'} 当前值 ${item?.parameterValue ?? '-'}${item?.unit ?? ''}`,
      severity: (item?.status === 'alarm' ? 'error' : 'warning') as 'error' | 'warning',
      timestamp: item?.timestamp ? new Date(item.timestamp).toLocaleString() : '',
      source: '工艺参数监控'
    }))
    .slice(0, 20)
}

// 初始化趋势图
const initTrendChart = () => {
  if (!trendChartRef.value) return
  
  try {
    // 销毁现有实例
    if (trendChartInstance) {
      trendChartInstance.dispose()
    }
    
    // 创建新实例，使用延迟初始化提升性能
    setTimeout(() => {
      if (trendChartRef.value) {
        trendChartInstance = echarts.init(trendChartRef.value)
        
        // 监听窗口大小变化，使用防抖优化性能
        window.addEventListener('resize', debouncedResize)
        
        // 初始渲染
        renderTrendChart()
      }
    }, 100)
  } catch (error) {
    console.error('初始化趋势图失败:', error)
    ElMessage.error('初始化生产进度趋势图失败')
  }
}

// 渲染趋势图
const renderTrendChart = () => {
  if (!trendChartInstance) return
  
  try {
    // 基于真实报工数据构建趋势图数据
    const chartData = generateTrendChartData()
    
    // 图表配置
    const option: echarts.EChartsOption = {
      tooltip: {
        trigger: 'axis',
        formatter: '{b}: {c}%'
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        data: chartData.time
      },
      yAxis: {
        type: 'value',
        name: '进度(%)',
        min: 0,
        max: 100,
        axisLabel: {
          formatter: '{value}%'
        }
      },
      series: [
        {
          name: '生产进度',
          type: 'line',
          data: chartData.progress,
          smooth: true,
          symbol: 'circle',
          symbolSize: 6,
          emphasis: {
            focus: 'series'
          },
          lineStyle: {
            width: 3
          },
          areaStyle: {
            opacity: 0.3
          },
          // 性能优化配置
          animation: false,
          progressiveThreshold: 500,
          progressive: 200
        }
      ],
      // 全局性能优化
      animation: false,
      animationDuration: 0
    }
    
    // 设置图表配置
    trendChartInstance.setOption(option)
  } catch (error) {
    console.error('渲染趋势图失败:', error)
    ElMessage.error('渲染生产进度趋势图失败')
  }
}

// 防抖函数
const debounce = (fn: Function, delay: number) => {
  let timer: number | null = null
  return function(...args: any[]) {
    if (timer) {
      clearTimeout(timer)
    }
    timer = window.setTimeout(() => {
      fn(...args)
      timer = null
    }, delay)
  }
}

// 防抖处理窗口大小变化
const debouncedResize = debounce(() => {
  trendChartInstance?.resize()
}, 200)

// 更新趋势图
const updateTrendChart = () => {
  renderTrendChart()
}

/**
 * 构建趋势图数据
 * 基于真实生产报工记录，按报工结束时间排序后累计合格数量，
 * 计算占计划总量的百分比作为生产进度趋势，无报工数据时返回空
 * @returns 趋势图时间轴与进度数据
 */
const generateTrendChartData = () => {
  const time: string[] = []
  const progress: number[] = []

  // 根据时间范围过滤报工记录
  const now = Date.now()
  const rangeMs = chartTimeRange.value === 'week'
    ? 7 * 24 * 60 * 60 * 1000
    : chartTimeRange.value === 'month'
      ? 30 * 24 * 60 * 60 * 1000
      : 24 * 60 * 60 * 1000

  const filteredReports = reportList.value
    .map(item => ({ ...item, endTimeValue: item?.endTime ? new Date(item.endTime).getTime() : NaN }))
    .filter(item => !Number.isNaN(item.endTimeValue) && now - item.endTimeValue <= rangeMs)
    .sort((a, b) => a.endTimeValue - b.endTimeValue)

  if (filteredReports.length === 0 || plannedProductionQty.value <= 0) {
    return { time, progress }
  }

  // 按时间顺序累计合格数量并换算为进度百分比
  let cumulativeQty = 0
  filteredReports.forEach(item => {
    cumulativeQty += Number(item?.goodQty ?? 0)
    const date = new Date(item.endTimeValue)
    if (chartTimeRange.value === 'today') {
      time.push(`${date.getHours()}:00`)
    } else {
      time.push(`${date.getMonth() + 1}/${date.getDate()}`)
    }
    progress.push(Math.min(100, Number(((cumulativeQty / plannedProductionQty.value) * 100).toFixed(1))))
  })

  return { time, progress }
}

// 处理图表窗口大小变化
const handleTrendChartResize = () => {
  trendChartInstance?.resize()
}

// 处理刷新间隔变化
const handleRefreshIntervalChange = () => {
  if (isAutoRefreshEnabled.value) {
    stopAutoRefresh()
    startAutoRefresh()
  }
}

// 处理图表时间范围变化
const handleChartTimeRangeChange = () => {
  updateTrendChart()
}

// 查看所有设备
const viewAllEquipment = () => {
  selectedEquipmentIds.value = []
  applyEquipmentFilter()
}

// 查看故障设备
const viewFaultyEquipment = () => {
  selectedEquipmentIds.value = (equipmentList.value
    .filter(e => e.status === 'faulty')
    .map(e => e.id) as (string | number)[])
  applyEquipmentFilter()
}

// 查看闲置设备
const viewIdleEquipment = () => {
  selectedEquipmentIds.value = (equipmentList.value
    .filter(e => e.status === 'idle')
    .map(e => e.id) as any[])
  applyEquipmentFilter()
}

// 获取进度状态
const getProgressStatus = (progress: number): 'success' | 'warning' | 'exception' | '' => {
  if (progress >= 90) return 'success'
  if (progress >= 60) return 'warning'
  return 'exception'
}

// 获取利用率状态
const getUtilizationStatus = (utilization: number): 'success' | 'warning' | 'exception' | '' => {
  if (utilization >= 80) return 'success'
  if (utilization >= 50) return 'warning'
  return 'exception'
}

// 获取交付率状态
const getDeliveryRateStatus = (rate: number): 'success' | 'warning' | 'exception' | '' => {
  if (rate >= 95) return 'success'
  if (rate >= 85) return 'warning'
  return 'exception'
}

// 获取质量率状态
const getQualityRateStatus = (rate: number): 'success' | 'warning' | 'exception' | '' => {
  if (rate >= 98) return 'success'
  if (rate >= 95) return 'warning'
  return 'exception'
}

// 获取设备状态标签类型
const getStatusTagType = (status: string): 'success' | 'info' | 'warning' | 'danger' => {
  switch (status) {
    case 'running': return 'success'
    case 'idle': return 'info'
    case 'faulty': return 'danger'
    case 'maintenance': return 'warning'
    default: return 'info'
  }
}

// 获取设备状态文本
const getStatusText = (status: string): string => {
  switch (status) {
    case 'running': return '运行中'
    case 'idle': return '闲置'
    case 'faulty': return '故障'
    case 'maintenance': return '维护中'
    default: return status
  }
}

// 获取告警类型
const getAlertType = (severity: string): 'primary' | 'success' | 'warning' | 'danger' => {
  switch (severity) {
    case 'info': return 'primary'
    case 'warning': return 'warning'
    case 'error': return 'danger'
    default: return 'primary'
  }
}

// 获取告警严重程度标签类型
const getSeverityTagType = (severity: string): 'success' | 'info' | 'warning' | 'danger' => {
  switch (severity) {
    case 'info': return 'info'
    case 'warning': return 'warning'
    case 'error': return 'danger'
    default: return 'info'
  }
}

// 获取告警严重程度文本
const getSeverityText = (severity: string): string => {
  switch (severity) {
    case 'info': return '信息'
    case 'warning': return '警告'
    case 'error': return '错误'
    default: return severity
  }
}

// 组件挂载时初始化
onMounted(() => {
  initAutoRefresh()
  fetchRealtimeData()
  initTrendChart()
})

// 组件卸载前清理
onBeforeUnmount(() => {
  stopAutoRefresh()
  
  // 销毁图表实例
  if (trendChartInstance) {
    trendChartInstance.dispose()
    trendChartInstance = null
  }
  
  // 移除事件监听
  window.removeEventListener('resize', handleTrendChartResize)
})
</script>

<style scoped>
.realtime-monitoring {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  box-sizing: border-box;
}

/* 控制面板 */
.control-panel {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
  box-sizing: border-box;
  flex-wrap: wrap;
  gap: 10px;
}

.control-group {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

/* 指标卡片 */
.metrics-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 15px;
  margin-bottom: 20px;
  box-sizing: border-box;
}

.metric-card {
  box-sizing: border-box;
  transition: all 0.3s ease;
}

.metric-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px 0;
}

.metric-value {
  font-size: 36px;
  font-weight: bold;
  margin-bottom: 10px;
  color: #303133;
}

.metric-desc {
  font-size: 14px;
  color: #909399;
  margin-top: 10px;
}

/* 设备监控 */
.equipment-monitoring {
  margin-bottom: 20px;
  box-sizing: border-box;
}

.section-card {
  box-sizing: border-box;
  margin-bottom: 20px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.equipment-list {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 15px;
  box-sizing: border-box;
}

.equipment-card {
  box-sizing: border-box;
  transition: all 0.3s ease;
}

.equipment-card:hover {
  transform: translateY(-2px);
}

.equipment-card.status-running {
  border-left: 4px solid #67c23a;
}

.equipment-card.status-idle {
  border-left: 4px solid #909399;
}

.equipment-card.status-faulty {
  border-left: 4px solid #f56c6c;
}

.equipment-card.status-maintenance {
  border-left: 4px solid #e6a23c;
}

.equipment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.equipment-info {
  display: flex;
  flex-direction: column;
}

.equipment-name {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.equipment-type {
  font-size: 12px;
  color: #909399;
  margin-top: 3px;
}

.equipment-status {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}

.status-item {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
}

.status-label {
  color: #909399;
}

.status-value {
  font-weight: 500;
  color: #303133;
}

/* 趋势图 */
.trend-chart-section {
  margin-bottom: 20px;
  box-sizing: border-box;
}

.chart-container {
  height: 300px;
  position: relative;
}

.trend-chart {
  width: 100%;
  height: 100%;
  box-sizing: border-box;
}

.loading-indicator {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  color: #909399;
}

.loading-icon {
  margin-right: 8px;
  font-size: 20px;
  animation: rotate 1s linear infinite;
}

.loading-text {
  font-size: 14px;
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* 告警信息 */
.alerts-section {
  box-sizing: border-box;
}

.alerts-container {
  max-height: 300px;
  overflow-y: auto;
}

.no-alerts {
  padding: 20px;
  text-align: center;
}

.alert-card {
  margin-bottom: 10px;
  transition: all 0.3s ease;
}

.alert-card:hover {
  transform: translateY(-1px);
}

.alert-card.severity-warning {
  border-left: 4px solid #e6a23c;
}

.alert-card.severity-error {
  border-left: 4px solid #f56c6c;
}

.alert-content {
  padding: 10px;
}

.alert-title {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 5px;
  color: #303133;
}

.alert-message {
  font-size: 14px;
  color: #606266;
  margin-bottom: 10px;
}

.alert-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: #909399;
}

.alert-source {
  margin-right: 10px;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .metrics-cards {
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
    gap: 10px;
  }
  
  .equipment-list {
    grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
    gap: 10px;
  }
}

@media (max-width: 768px) {
  .control-panel {
    flex-direction: column;
    align-items: stretch;
  }
  
  .metrics-cards {
    grid-template-columns: 1fr;
    gap: 10px;
  }
  
  .equipment-list {
    grid-template-columns: 1fr;
    gap: 10px;
  }
  
  .metric-value {
    font-size: 28px;
  }
  
  .chart-container {
    height: 250px;
  }
  
  .alerts-container {
    max-height: 250px;
  }
}

@media (max-width: 480px) {
  .control-group {
    flex-direction: column;
    align-items: stretch;
  }
  
  .control-group .el-select,
  .control-group .el-button {
    width: 100% !important;
    margin-left: 0 !important;
  }
  
  .section-header {
    flex-direction: column;
    align-items: stretch;
    gap: 10px;
  }
  
  .equipment-status {
    grid-template-columns: 1fr;
  }
  
  .metric-value {
    font-size: 24px;
  }
  
  .chart-container {
    height: 200px;
  }
  
  .alerts-container {
    max-height: 200px;
  }
}
</style>