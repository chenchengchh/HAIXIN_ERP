<script setup lang="ts">
import { ref, computed, onBeforeUnmount, onMounted, nextTick, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import * as echarts from 'echarts'
import { getAnalysisDashboard, getCostSummary, getFaultSummary, getOeeTrend } from '@/api/eam'
import { unwrapListResponse, unwrapResponseData } from '@/api'
import { ElMessage } from 'element-plus'

// 路由相关
const route = useRoute()
const router = useRouter()

// 活跃标签页
const activeTab = ref<string>('dashboard')

// 标签页名称映射
const tabLabelMap: Record<string, string> = {
  dashboard: 'KPI看板',
  oee: 'OEE分析',
  fault: '故障分析',
  cost: '维护成本分析'
}

// KPI数据
const kpiData = ref({
  oee: 0,
  mtbf: 0,
  mttr: 0,
  maintenanceCost: 0
})

// KPI数据实际统计月份（后端回退月份时用于页面提示）
const kpiDataMonth = ref<string>('')

// OEE分析数据
const oeeTrendData = ref<any[]>([])

/**
 * 有KPI数据的月份平均值（排除无数据月份，避免拉低平均值）
 */
const oeeAvgSummary = computed(() => {
  // 仅统计任一指标非零的月份，视为有效数据月
  const valid = oeeTrendData.value.filter(
    (i: any) => toNumber(i.avgOee) > 0 || toNumber(i.avgMtbf) > 0 || toNumber(i.avgMttr) > 0
  )
  if (valid.length === 0) {
    return { avgOee: 0, avgMtbf: 0, avgMttr: 0 }
  }
  const sum = (key: string) => valid.reduce((acc: number, i: any) => acc + toNumber(i[key]), 0)
  return {
    avgOee: sum('avgOee') / valid.length,
    avgMtbf: sum('avgMtbf') / valid.length,
    avgMttr: sum('avgMttr') / valid.length
  }
})

// 故障分析数据
const faultSummaryData = ref<any | null>(null)

// 维护成本分析数据
const costSummaryData = ref<any | null>(null)

const tabLoaded = ref({
  dashboard: false,
  oee: false,
  fault: false,
  cost: false
})

const tabLoading = ref({
  dashboard: false,
  oee: false,
  fault: false,
  cost: false
})

// 图表DOM引用
const dashboardOeeRef = ref<HTMLDivElement | null>(null)
const dashboardFailureRef = ref<HTMLDivElement | null>(null)
const dashboardCostRef = ref<HTMLDivElement | null>(null)

const oeeTrendRef = ref<HTMLDivElement | null>(null)
const oeeMtbfMttrRef = ref<HTMLDivElement | null>(null)

const faultTypeRef = ref<HTMLDivElement | null>(null)
const faultStatusRef = ref<HTMLDivElement | null>(null)
const faultTopEquipmentRef = ref<HTMLDivElement | null>(null)

const costMonthTrendRef = ref<HTMLDivElement | null>(null)
const costTypeRef = ref<HTMLDivElement | null>(null)
const costTopEquipmentRef = ref<HTMLDivElement | null>(null)

// 图表实例（仅KPI看板页签使用）
let oeeChart: echarts.ECharts | null = null
let failureChart: echarts.ECharts | null = null
let costChart: echarts.ECharts | null = null

let oeeTrendChart: echarts.ECharts | null = null
let oeeMtbfMttrChart: echarts.ECharts | null = null

let faultTypeChart: echarts.ECharts | null = null
let faultStatusChart: echarts.ECharts | null = null
let faultTopEquipmentChart: echarts.ECharts | null = null

let costMonthTrendChart: echarts.ECharts | null = null
let costTypeChart: echarts.ECharts | null = null
let costTopEquipmentChart: echarts.ECharts | null = null

const toNumber = (value: unknown, fallback = 0) => {
  /**
   * 将后端返回的数值（number/string/BigDecimal序列化结果）安全转为number
   * @param value 原始值
   * @param fallback 默认值
   */
  if (value === null || value === undefined) return fallback
  if (typeof value === 'number') return Number.isFinite(value) ? value : fallback
  const parsed = Number(value)
  return Number.isFinite(parsed) ? parsed : fallback
}

const formatDate = (date: Date) => {
  /**
   * 格式化日期为YYYY-MM-DD
   * @param date 日期对象
   */
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

const getDefaultRange = () => {
  /**
   * 获取默认统计范围：最近30天
   */
  const end = new Date()
  const start = new Date()
  start.setDate(end.getDate() - 29)
  return { start: formatDate(start), end: formatDate(end) }
}

const disposeDashboardCharts = () => {
  /**
   * 释放KPI看板页签图表实例，避免重复初始化导致内存泄漏
   */
  oeeChart?.dispose()
  failureChart?.dispose()
  costChart?.dispose()
  oeeChart = null
  failureChart = null
  costChart = null
}

// 初始化 OEE 仪表盘（KPI看板）
const initOeeChart = () => {
  /**
   * 初始化KPI看板的OEE仪表盘
   */
  const chartDom = dashboardOeeRef.value
  if (!chartDom) return
  oeeChart = echarts.getInstanceByDom(chartDom) || echarts.init(chartDom)
  const option = {
    series: [
      {
        type: 'gauge',
        startAngle: 180,
        endAngle: 0,
        min: 0,
        max: 100,
        splitNumber: 10,
        itemStyle: {
          color: '#58D9F9',
          shadowColor: 'rgba(0,138,255,0.45)',
          shadowBlur: 10,
          shadowOffsetX: 2,
          shadowOffsetY: 2
        },
        progress: {
          show: true,
          roundCap: true,
          width: 18
        },
        pointer: {
          icon: 'path://M2090.36389,615.30999 L2090.36389,615.30999 C2091.48372,615.30999 2092.40383,616.194028 2092.44859,617.312956 L2096.90698,728.755929 C2100.13176,809.343585 2048.10088,875.185884 1981.23753,875.626359 C1977.95825,875.648125 1974.67976,875.648125 1971.4,875.626359 C1904.53665,875.185884 1852.50577,809.343585 1855.73055,728.755929 L1860.18893,617.312956 C1860.23369,616.194028 1861.1538,615.30999 1862.27363,615.30999 L2090.36389,615.30999 Z',
          length: '75%',
          width: 16,
          offsetCenter: [0, '5%']
        },
        axisLine: {
          roundCap: true,
          lineStyle: {
            width: 18
          }
        },
        axisTick: {
          splitNumber: 2,
          lineStyle: {
            width: 2,
            color: '#999'
          }
        },
        splitLine: {
          length: 12,
          lineStyle: {
            width: 3,
            color: '#999'
          }
        },
        axisLabel: {
          distance: 30,
          color: '#999',
          fontSize: 14
        },
        title: {
          show: false
        },
        detail: {
          backgroundColor: '#fff',
          borderColor: '#999',
          borderWidth: 2,
          width: '60%',
          lineHeight: 40,
          height: 40,
          borderRadius: 8,
          offsetCenter: [0, '35%'],
          valueAnimation: true,
          formatter: function (value: number) {
            return '{value|' + value.toFixed(1) + '}{unit|%}'
          },
          rich: {
            value: {
              fontSize: 30,
              fontWeight: 'bolder',
              color: '#777'
            },
            unit: {
              fontSize: 16,
              color: '#999',
              padding: [0, 0, -10, 10]
            }
          }
        },
        data: [
          {
            value: kpiData.value.oee
          }
        ]
      }
    ]
  }
  oeeChart.setOption(option)
}

// 初始化故障分析图（KPI看板）
const initFailureChart = () => {
  /**
   * 初始化KPI看板的故障类型分布示例图（后续由“故障分析”页签接管真实分析）
   */
  const chartDom = dashboardFailureRef.value
  if (!chartDom) return
  failureChart = echarts.getInstanceByDom(chartDom) || echarts.init(chartDom)
  const option = {
    title: {
      text: '故障类型分布',
      left: 'center'
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: [
      {
        type: 'category',
        data: ['机械故障', '电气故障', '液压故障', '软件故障', '其他'],
        axisTick: {
          alignWithLabel: true
        }
      }
    ],
    yAxis: [
      {
        type: 'value'
      }
    ],
    series: [
      {
        name: '故障次数',
        type: 'bar',
        barWidth: '60%',
        data: [10, 52, 200, 334, 390] // 示例数据，后续可对接后端
      }
    ]
  }
  failureChart.setOption(option)
}

// 初始化成本分析图（KPI看板）
const initCostChart = () => {
  /**
   * 初始化KPI看板的维护成本构成示例图（后续由“维护成本分析”页签接管真实分析）
   */
  const chartDom = dashboardCostRef.value
  if (!chartDom) return
  costChart = echarts.getInstanceByDom(chartDom) || echarts.init(chartDom)
  const option = {
    title: {
      text: '维护成本构成',
      left: 'center'
    },
    tooltip: {
      trigger: 'item'
    },
    legend: {
      orient: 'vertical',
      left: 'left'
    },
    series: [
      {
        name: '成本',
        type: 'pie',
        radius: '50%',
        data: [
          { value: 1048, name: '备件成本' },
          { value: 735, name: '人工成本' },
          { value: 580, name: '外协成本' },
          { value: 484, name: '能源消耗' },
          { value: 300, name: '其他' }
        ],
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }
    ]
  }
  costChart.setOption(option)
}

const initDashboardCharts = () => {
  /**
   * 初始化KPI看板页签的全部图表
   */
  initOeeChart()
  initFailureChart()
  initCostChart()
}

// 获取看板数据
const fetchDashboard = async () => {
  /**
   * 获取KPI看板数据并更新卡片与图表
   */
  tabLoading.value.dashboard = true
  try {
    const res = await getAnalysisDashboard()
    const data = unwrapResponseData<any>(res) || {}
    kpiData.value = {
      oee: data.avgOee || 85.5,
      mtbf: data.avgMtbf || 120,
      mttr: data.avgMttr || 2.5,
      maintenanceCost: data.totalCost || 50000
    }
    // 展示后端实际统计月份（当月无数据回退时给出提示）
    kpiDataMonth.value = data.dataMonth || ''
    
    // 更新图表
    if (oeeChart) {
      oeeChart.setOption({
        series: [{
          data: [{ value: kpiData.value.oee }]
        }]
      })
    }
    tabLoaded.value.dashboard = true
  } catch (error) {
    console.error('获取看板数据失败:', error)
    ElMessage.error('获取看板数据失败')
  } finally {
    tabLoading.value.dashboard = false
  }
}

const disposeAnalysisCharts = () => {
  /**
   * 释放OEE/故障/成本分析页签的图表实例
   */
  oeeTrendChart?.dispose()
  oeeMtbfMttrChart?.dispose()
  faultTypeChart?.dispose()
  faultStatusChart?.dispose()
  faultTopEquipmentChart?.dispose()
  costMonthTrendChart?.dispose()
  costTypeChart?.dispose()
  costTopEquipmentChart?.dispose()

  oeeTrendChart = null
  oeeMtbfMttrChart = null
  faultTypeChart = null
  faultStatusChart = null
  faultTopEquipmentChart = null
  costMonthTrendChart = null
  costTypeChart = null
  costTopEquipmentChart = null
}

const initOeeCharts = () => {
  /**
   * 初始化OEE分析页签图表
   */
  const trendDom = oeeTrendRef.value
  const mtbfMttrDom = oeeMtbfMttrRef.value
  if (trendDom) oeeTrendChart = echarts.getInstanceByDom(trendDom) || echarts.init(trendDom)
  if (mtbfMttrDom) oeeMtbfMttrChart = echarts.getInstanceByDom(mtbfMttrDom) || echarts.init(mtbfMttrDom)
}

const renderOeeCharts = () => {
  /**
   * 渲染OEE分析页签图表
   */
  if (!oeeTrendChart || !oeeMtbfMttrChart) return
  const months = oeeTrendData.value.map((p: any) => p.month)
  const oee = oeeTrendData.value.map((p: any) => toNumber(p.avgOee))
  const mtbf = oeeTrendData.value.map((p: any) => toNumber(p.avgMtbf))
  const mttr = oeeTrendData.value.map((p: any) => toNumber(p.avgMttr))

  oeeTrendChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: months },
    yAxis: { type: 'value', max: 100, min: 0 },
    series: [{ name: '平均OEE(%)', type: 'line', smooth: true, data: oee }]
  })

  oeeMtbfMttrChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['MTBF(小时)', 'MTTR(小时)'] },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: months },
    yAxis: { type: 'value' },
    series: [
      { name: 'MTBF(小时)', type: 'line', smooth: true, data: mtbf },
      { name: 'MTTR(小时)', type: 'line', smooth: true, data: mttr }
    ]
  })
}

const fetchOeeTrendData = async () => {
  /**
   * 获取OEE趋势数据（默认最近12个月）
   */
  tabLoading.value.oee = true
  try {
    const res = await getOeeTrend({ months: 12 })
    oeeTrendData.value = unwrapListResponse<any>(res)
    tabLoaded.value.oee = true
  } catch (error) {
    console.error('获取OEE趋势失败:', error)
    ElMessage.error('获取OEE趋势失败')
  } finally {
    tabLoading.value.oee = false
  }
}

const initFaultCharts = () => {
  /**
   * 初始化故障分析页签图表
   */
  const typeDom = faultTypeRef.value
  const statusDom = faultStatusRef.value
  const topDom = faultTopEquipmentRef.value
  if (typeDom) faultTypeChart = echarts.getInstanceByDom(typeDom) || echarts.init(typeDom)
  if (statusDom) faultStatusChart = echarts.getInstanceByDom(statusDom) || echarts.init(statusDom)
  if (topDom) faultTopEquipmentChart = echarts.getInstanceByDom(topDom) || echarts.init(topDom)
}

const renderFaultCharts = () => {
  /**
   * 渲染故障分析页签图表
   */
  if (!faultSummaryData.value) return
  const byType = Array.isArray(faultSummaryData.value.byType) ? faultSummaryData.value.byType : []
  const byStatus = Array.isArray(faultSummaryData.value.byStatus) ? faultSummaryData.value.byStatus : []
  const topEquipment = Array.isArray(faultSummaryData.value.topEquipment) ? faultSummaryData.value.topEquipment : []

  faultTypeChart?.setOption({
    tooltip: { trigger: 'item' },
    legend: { top: 'bottom' },
    series: [{ name: '故障类型', type: 'pie', radius: '55%', data: byType }]
  })

  faultStatusChart?.setOption({
    tooltip: { trigger: 'item' },
    legend: { top: 'bottom' },
    series: [{ name: '故障状态', type: 'pie', radius: '55%', data: byStatus }]
  })

  faultTopEquipmentChart?.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'value' },
    yAxis: { type: 'category', data: topEquipment.map((i: any) => i.name) },
    series: [{ name: '故障次数', type: 'bar', data: topEquipment.map((i: any) => i.value) }]
  })
}

const fetchFaultSummaryData = async () => {
  /**
   * 获取故障分析汇总数据（默认最近30天）
   */
  const range = getDefaultRange()
  tabLoading.value.fault = true
  try {
    const res = await getFaultSummary(range)
    faultSummaryData.value = unwrapResponseData<any>(res) || null
    tabLoaded.value.fault = true
  } catch (error) {
    console.error('获取故障分析失败:', error)
    ElMessage.error('获取故障分析失败')
  } finally {
    tabLoading.value.fault = false
  }
}

const initCostCharts = () => {
  /**
   * 初始化维护成本分析页签图表
   */
  const monthDom = costMonthTrendRef.value
  const typeDom = costTypeRef.value
  const topDom = costTopEquipmentRef.value
  if (monthDom) costMonthTrendChart = echarts.getInstanceByDom(monthDom) || echarts.init(monthDom)
  if (typeDom) costTypeChart = echarts.getInstanceByDom(typeDom) || echarts.init(typeDom)
  if (topDom) costTopEquipmentChart = echarts.getInstanceByDom(topDom) || echarts.init(topDom)
}

const renderCostCharts = () => {
  /**
   * 渲染维护成本分析页签图表
   */
  if (!costSummaryData.value) return
  const byMonth = Array.isArray(costSummaryData.value.byMonth) ? costSummaryData.value.byMonth : []
  const byType = Array.isArray(costSummaryData.value.byType) ? costSummaryData.value.byType : []
  const topEquipment = Array.isArray(costSummaryData.value.topEquipment) ? costSummaryData.value.topEquipment : []

  costMonthTrendChart?.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: byMonth.map((i: any) => i.month) },
    yAxis: { type: 'value' },
    series: [{ name: '维护成本', type: 'bar', data: byMonth.map((i: any) => toNumber(i.value)) }]
  })

  costTypeChart?.setOption({
    tooltip: { trigger: 'item' },
    legend: { top: 'bottom' },
    series: [{ name: '成本类型', type: 'pie', radius: '55%', data: byType }]
  })

  costTopEquipmentChart?.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'value' },
    yAxis: { type: 'category', data: topEquipment.map((i: any) => i.name) },
    series: [{ name: '成本', type: 'bar', data: topEquipment.map((i: any) => toNumber(i.value)) }]
  })
}

const fetchCostSummaryData = async () => {
  /**
   * 获取维护成本分析汇总数据（默认最近30天）
   */
  const range = getDefaultRange()
  tabLoading.value.cost = true
  try {
    const res = await getCostSummary(range)
    costSummaryData.value = unwrapResponseData<any>(res) || null
    tabLoaded.value.cost = true
  } catch (error) {
    console.error('获取维护成本分析失败:', error)
    ElMessage.error('获取维护成本分析失败')
  } finally {
    tabLoading.value.cost = false
  }
}

const ensureTabDataAndCharts = async (tab: string) => {
  /**
   * 按需加载页签数据并初始化/渲染对应图表
   * @param tab 当前页签
   */
  if (tab === 'dashboard') {
    if (!tabLoaded.value.dashboard) await fetchDashboard()
    await nextTick()
    initDashboardCharts()
    handleResize()
    return
  }
  if (tab === 'oee') {
    if (!tabLoaded.value.oee) await fetchOeeTrendData()
    await nextTick()
    initOeeCharts()
    renderOeeCharts()
    handleResize()
    return
  }
  if (tab === 'fault') {
    if (!tabLoaded.value.fault) await fetchFaultSummaryData()
    await nextTick()
    initFaultCharts()
    renderFaultCharts()
    handleResize()
    return
  }
  if (tab === 'cost') {
    if (!tabLoaded.value.cost) await fetchCostSummaryData()
    await nextTick()
    initCostCharts()
    renderCostCharts()
    handleResize()
  }
}

const handleTabChange = (tabName: string) => {
  /**
   * 处理页签切换：同步更新路由参数
   * @param tabName 页签名称
   */
  // 更新路由参数
  router.push({
    path: `/home/eam/asset-performance/${tabName}`
  })
}

const getActiveTabFromRoute = () => {
  /**
   * 从路由参数解析当前页签
   */
  const tab = route.params.tab as string | undefined
  if (tab && tabLabelMap[tab]) return tab
  return 'dashboard'
}

const handleResize = () => {
  /**
   * 处理窗口尺寸变化，触发图表自适应
   */
  oeeChart?.resize()
  failureChart?.resize()
  costChart?.resize()
  oeeTrendChart?.resize()
  oeeMtbfMttrChart?.resize()
  faultTypeChart?.resize()
  faultStatusChart?.resize()
  faultTopEquipmentChart?.resize()
  costMonthTrendChart?.resize()
  costTypeChart?.resize()
  costTopEquipmentChart?.resize()
}

onMounted(() => {
  activeTab.value = getActiveTabFromRoute()
  nextTick(() => {
    ensureTabDataAndCharts(activeTab.value)
  })
  
  window.addEventListener('resize', handleResize)
})

watch(
  () => route.params.tab,
  () => {
    const tabFromRoute = getActiveTabFromRoute()
    if (activeTab.value !== tabFromRoute) {
      activeTab.value = tabFromRoute
    }
  }
)

watch(
  () => activeTab.value,
  (tab) => {
    ensureTabDataAndCharts(tab)
  }
)

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  disposeDashboardCharts()
  disposeAnalysisCharts()
})
</script>

<template>
  <div class="eam-submodule-container">
    <div class="page-header">
      <h2>绩效分析</h2>
      <el-breadcrumb>
        <el-breadcrumb-item>
          <router-link to="/home">首页</router-link>
        </el-breadcrumb-item>
        <el-breadcrumb-item>
          <router-link to="/home/eam">EAM系统</router-link>
        </el-breadcrumb-item>
        <el-breadcrumb-item>
          <router-link to="/home/eam/asset-performance">绩效分析</router-link>
        </el-breadcrumb-item>
        <el-breadcrumb-item :to="`/home/eam/asset-performance/${activeTab}`">
          {{ tabLabelMap[activeTab] }}
        </el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <el-card class="submodule-tabs-card">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange" type="border-card">
        <el-tab-pane label="KPI看板" name="dashboard">
          <div class="tab-content dashboard-container">
            <!-- 数据月份提示：当月无KPI数据时后端回退到最近有数据的月份 -->
            <div v-if="kpiDataMonth" class="data-month-tip">数据统计月份：{{ kpiDataMonth }}</div>
            <div class="kpi-cards">
              <div class="kpi-card">
                <div class="kpi-title">设备综合效率 (OEE)</div>
                <div class="kpi-value highlight">{{ kpiData.oee }}%</div>
              </div>
              <div class="kpi-card">
                <div class="kpi-title">平均故障间隔 (MTBF)</div>
                <div class="kpi-value">{{ kpiData.mtbf }} 小时</div>
              </div>
              <div class="kpi-card">
                <div class="kpi-title">平均修复时间 (MTTR)</div>
                <div class="kpi-value">{{ kpiData.mttr }} 小时</div>
              </div>
              <div class="kpi-card">
                <div class="kpi-title">本月维护成本</div>
                <div class="kpi-value">¥{{ kpiData.maintenanceCost.toLocaleString() }}</div>
              </div>
            </div>

            <div class="charts-row">
              <div class="chart-container">
                <div class="chart-title">OEE</div>
                <div ref="dashboardOeeRef" class="chart-body"></div>
              </div>
              <div class="chart-container">
                <div class="chart-title">故障类型分布</div>
                <div ref="dashboardFailureRef" class="chart-body"></div>
              </div>
              <div class="chart-container">
                <div class="chart-title">维护成本构成</div>
                <div ref="dashboardCostRef" class="chart-body"></div>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="OEE分析" name="oee">
          <div class="tab-content">
            <div v-if="tabLoading.oee" class="loading-block">
              <el-skeleton :rows="6" animated />
            </div>
            <el-empty v-else-if="oeeTrendData.length === 0" description="暂无OEE趋势数据" :image-size="140" />
            <div v-else>
              <div class="analysis-summary-cards">
                <div class="summary-card">
                  <div class="summary-title">最近12个月平均OEE</div>
                  <div class="summary-value highlight">
                    {{ oeeAvgSummary.avgOee.toFixed(2) }}%
                  </div>
                </div>
                <div class="summary-card">
                  <div class="summary-title">最近12个月平均MTBF</div>
                  <div class="summary-value">
                    {{ oeeAvgSummary.avgMtbf.toFixed(2) }} 小时
                  </div>
                </div>
                <div class="summary-card">
                  <div class="summary-title">最近12个月平均MTTR</div>
                  <div class="summary-value">
                    {{ oeeAvgSummary.avgMttr.toFixed(2) }} 小时
                  </div>
                </div>
              </div>

              <div class="charts-row-2">
                <div class="chart-container">
                  <div class="chart-title">OEE 月度趋势</div>
                  <div ref="oeeTrendRef" class="chart-body"></div>
                </div>
                <div class="chart-container">
                  <div class="chart-title">MTBF / MTTR 月度趋势</div>
                  <div ref="oeeMtbfMttrRef" class="chart-body"></div>
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="故障分析" name="fault">
          <div class="tab-content">
            <div v-if="tabLoading.fault" class="loading-block">
              <el-skeleton :rows="6" animated />
            </div>
            <el-empty
              v-else-if="!faultSummaryData || (faultSummaryData.totalCount || 0) === 0"
              description="暂无故障数据"
              :image-size="140"
            />
            <div v-else>
              <div class="analysis-summary-cards">
                <div class="summary-card">
                  <div class="summary-title">统计范围</div>
                  <div class="summary-value">
                    {{ faultSummaryData.start }} ~ {{ faultSummaryData.end }}
                  </div>
                </div>
                <div class="summary-card">
                  <div class="summary-title">故障总数</div>
                  <div class="summary-value highlight">
                    {{ faultSummaryData.totalCount }}
                  </div>
                </div>
                <div class="summary-card">
                  <div class="summary-title">Top设备数</div>
                  <div class="summary-value">
                    {{ (faultSummaryData.topEquipment || []).length }}
                  </div>
                </div>
              </div>

              <div class="charts-row">
                <div class="chart-container">
                  <div class="chart-title">故障类型分布</div>
                  <div ref="faultTypeRef" class="chart-body"></div>
                </div>
                <div class="chart-container">
                  <div class="chart-title">故障状态分布</div>
                  <div ref="faultStatusRef" class="chart-body"></div>
                </div>
                <div class="chart-container">
                  <div class="chart-title">设备故障次数排行（Top10）</div>
                  <div ref="faultTopEquipmentRef" class="chart-body"></div>
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="维护成本分析" name="cost">
          <div class="tab-content">
            <div v-if="tabLoading.cost" class="loading-block">
              <el-skeleton :rows="6" animated />
            </div>
            <el-empty
              v-else-if="!costSummaryData || toNumber(costSummaryData.totalCost) === 0"
              description="暂无维护成本数据"
              :image-size="140"
            />
            <div v-else>
              <div class="analysis-summary-cards">
                <div class="summary-card">
                  <div class="summary-title">统计范围</div>
                  <div class="summary-value">
                    {{ costSummaryData.start }} ~ {{ costSummaryData.end }}
                  </div>
                </div>
                <div class="summary-card">
                  <div class="summary-title">维护总成本</div>
                  <div class="summary-value highlight">
                    ¥{{ toNumber(costSummaryData.totalCost).toLocaleString() }}
                  </div>
                </div>
                <div class="summary-card">
                  <div class="summary-title">Top设备数</div>
                  <div class="summary-value">
                    {{ (costSummaryData.topEquipment || []).length }}
                  </div>
                </div>
              </div>

              <div class="charts-row">
                <div class="chart-container">
                  <div class="chart-title">维护成本趋势</div>
                  <div ref="costMonthTrendRef" class="chart-body"></div>
                </div>
                <div class="chart-container">
                  <div class="chart-title">成本类型分布</div>
                  <div ref="costTypeRef" class="chart-body"></div>
                </div>
                <div class="chart-container">
                  <div class="chart-title">设备成本排行（Top10）</div>
                  <div ref="costTopEquipmentRef" class="chart-body"></div>
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<style scoped>
.eam-submodule-container {
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 20px;
  min-height: 100vh;
  background-color: #f5f7fa;
}

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

h2 {
  margin-bottom: 0;
}

/* KPI看板数据月份提示样式 */
.data-month-tip {
  margin-bottom: 12px;
  font-size: 13px;
  color: #909399;
}

.kpi-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 30px;
}

.kpi-card {
  background-color: #f9fafc;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 20px;
  text-align: center;
}

.kpi-title {
  color: #909399;
  font-size: 14px;
  margin-bottom: 10px;
}

.kpi-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 10px;
}

.kpi-value.highlight {
  color: #409EFF;
}

.kpi-trend {
  font-size: 12px;
}

.kpi-trend.up {
  color: #67C23A;
}

.kpi-trend.down {
  color: #F56C6C;
}

.submodule-tabs-card {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.tab-content {
  padding: 20px;
}

.loading-block {
  padding: 12px;
}

.analysis-summary-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-bottom: 20px;
}

.summary-card {
  background-color: #f9fafc;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 16px 20px;
}

.summary-title {
  color: #909399;
  font-size: 13px;
  margin-bottom: 8px;
}

.summary-value {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.summary-value.highlight {
  color: #409EFF;
}

.charts-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.charts-row-2 {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.chart-container {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 20px;
  height: 360px;
  display: flex;
  flex-direction: column;
}

.chart-title {
  font-weight: 500;
  margin-bottom: 15px;
  color: #303133;
}

.chart-body {
  flex: 1;
  width: 100%;
}

@media (max-width: 1200px) {
  .charts-row {
    grid-template-columns: repeat(2, 1fr);
  }

  .charts-row-2 {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .eam-submodule-container {
    padding: 12px;
  }

  .kpi-cards {
    grid-template-columns: repeat(2, 1fr);
  }

  .analysis-summary-cards {
    grid-template-columns: 1fr;
  }

  .charts-row {
    grid-template-columns: 1fr;
  }

  .charts-row-2 {
    grid-template-columns: 1fr;
  }

  .tab-content {
    padding: 12px;
  }
}
</style>
