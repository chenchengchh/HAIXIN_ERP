<template>
  <div class="dashboard">
    <div class="dashboard-header">
      <h3 class="dashboard-title">运营概览</h3>
      <span class="dashboard-subtitle">海星数字化系统 · 数据驾驶舱</span>
    </div>
    
    <!-- 指标卡片 -->
    <div class="metrics-cards">
      <el-card class="metric-card" shadow="hover">
        <div class="metric-content">
          <div class="metric-icon income">
            <el-icon><Money /></el-icon>
          </div>
          <div class="metric-info">
            <div class="metric-value">{{ formatMoney(totalIncome) }}</div>
            <div class="metric-label">本月收入</div>
            <div class="metric-change">
              <el-icon :class="incomeChange > 0 ? 'positive' : 'negative'">
                <ArrowUp v-if="incomeChange > 0" />
                <ArrowDown v-else />
              </el-icon>
              <span :class="incomeChange > 0 ? 'positive' : 'negative'">
                {{ Math.abs(incomeChange) }}%
              </span>
              <span class="compared-to">较上月</span>
            </div>
          </div>
        </div>
      </el-card>
      
      <el-card class="metric-card" shadow="hover">
        <div class="metric-content">
          <div class="metric-icon expense">
            <el-icon><Wallet /></el-icon>
          </div>
          <div class="metric-info">
            <div class="metric-value">{{ formatMoney(totalExpense) }}</div>
            <div class="metric-label">本月支出</div>
            <div class="metric-change">
              <el-icon :class="expenseChange > 0 ? 'positive' : 'negative'">
                <ArrowUp v-if="expenseChange > 0" />
                <ArrowDown v-else />
              </el-icon>
              <span :class="expenseChange > 0 ? 'positive' : 'negative'">
                {{ Math.abs(expenseChange) }}%
              </span>
              <span class="compared-to">较上月</span>
            </div>
          </div>
        </div>
      </el-card>
      
      <el-card class="metric-card" shadow="hover">
        <div class="metric-content">
          <div class="metric-icon order">
            <el-icon><DocumentCopy /></el-icon>
          </div>
          <div class="metric-info">
            <div class="metric-value">{{ totalOrders }}</div>
            <div class="metric-label">本月订单</div>
            <div class="metric-change">
              <el-icon :class="orderChange > 0 ? 'positive' : 'negative'">
                <ArrowUp v-if="orderChange > 0" />
                <ArrowDown v-else />
              </el-icon>
              <span :class="orderChange > 0 ? 'positive' : 'negative'">
                {{ Math.abs(orderChange) }}%
              </span>
              <span class="compared-to">较上月</span>
            </div>
          </div>
        </div>
      </el-card>
      
      <el-card class="metric-card" shadow="hover">
        <div class="metric-content">
          <div class="metric-icon inventory">
            <el-icon><Box /></el-icon>
          </div>
          <div class="metric-info">
            <div class="metric-value">{{ totalInventory }}</div>
            <div class="metric-label">库存总量</div>
            <div class="metric-change">
              <el-icon :class="inventoryChange > 0 ? 'positive' : 'negative'">
                <ArrowUp v-if="inventoryChange > 0" />
                <ArrowDown v-else />
              </el-icon>
              <span :class="inventoryChange > 0 ? 'positive' : 'negative'">
                {{ Math.abs(inventoryChange) }}%
              </span>
              <span class="compared-to">较上月</span>
            </div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- AI 晨会简报 -->
    <el-card class="briefing-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span class="briefing-title">
            <el-icon class="briefing-title-icon"><Cpu /></el-icon>
            AI 晨会简报
          </span>
          <el-button type="primary" text @click="goToBriefing">查看详情</el-button>
        </div>
      </template>
      <div v-loading="briefingLoading" class="briefing-body">
        <template v-if="briefingList.length > 0">
          <div v-for="item in displayBriefingList" :key="item.id" class="briefing-item">
            <el-tag size="small" :type="briefingSeverityTag(item.severity)" effect="dark">
              {{ briefingSeverityLabel(item.severity) }}
            </el-tag>
            <span class="briefing-item-title" :title="item.title">{{ item.title }}</span>
          </div>
          <div v-if="briefingList.length > maxBriefingCount" class="briefing-more">
            等 {{ briefingList.length }} 条
          </div>
        </template>
        <div v-else-if="!briefingLoading" class="briefing-empty">
          <el-icon class="briefing-empty-icon"><CircleCheckFilled /></el-icon>
          <span>今日六域巡检无异常</span>
        </div>
      </div>
    </el-card>

    <!-- 图表区域 -->
    <div class="charts-area" v-loading="loading">
      <!-- 收入支出趋势图 -->
      <el-card class="chart-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <span>收入支出趋势</span>
            <el-select v-model="dateRange" size="small" style="width: 120px;">
              <el-option label="本月" value="month" />
              <el-option label="本季度" value="quarter" />
              <el-option label="本年" value="year" />
            </el-select>
          </div>
        </template>
        <div class="chart-container">
          <div ref="incomeExpenseChartRef" class="chart"></div>
        </div>
      </el-card>
      
      <!-- 订单状态分布 -->
      <el-card class="chart-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <span>订单状态分布</span>
          </div>
        </template>
        <div class="chart-container">
          <div ref="orderStatusChartRef" class="chart"></div>
        </div>
      </el-card>
      
      <!-- 销售趋势 -->
      <el-card class="chart-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <span>销售趋势</span>
          </div>
        </template>
        <div class="chart-container">
          <div ref="salesTrendChartRef" class="chart"></div>
        </div>
      </el-card>
      
      <!-- 库存周转率 -->
      <el-card class="chart-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <span>库存周转率</span>
          </div>
        </template>
        <div class="chart-container">
          <div ref="inventoryTurnoverChartRef" class="chart"></div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch, nextTick, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { Money, Wallet, DocumentCopy, Box, ArrowUp, ArrowDown, Cpu, CircleCheckFilled } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import { dashboardApi } from '../../api/erp/dashboard'
import { unwrapResponseData, unwrapListResponse } from '../../api/index'
import { getSuggestions, type AiSuggestion } from '@/api/ai'
import { DataTransformer } from '@/utils/data-transformer'

const router = useRouter()

// 响应式数据
const loading = ref(false)
const totalIncome = ref(0)
const totalExpense = ref(0)
const totalOrders = ref(0)
const totalInventory = ref(0)
const incomeChange = ref(0)
const expenseChange = ref(0)
const orderChange = ref(0)
const inventoryChange = ref(0)
const dateRange = ref('month')

// AI 晨会简报状态
const briefingLoading = ref(false)
const briefingList = ref<AiSuggestion[]>([])
const maxBriefingCount = 5
// 首页最多展示前5条简报，超出部分以"等 N 条"提示
const displayBriefingList = computed(() => briefingList.value.slice(0, maxBriefingCount))

/**
 * 获取 AI 晨会简报待处理列表
 * 调用 ai-brain 建议接口（状态 PENDING + 类型 MORNING_BRIEFING），
 * 采用与决策中心一致的 DataTransformer.unwrapList 解包模式；
 * 加载失败时静默降级为空状态，仅输出告警日志，不阻塞仪表盘
 */
const fetchMorningBriefing = async () => {
  briefingLoading.value = true
  try {
    const resp = await getSuggestions('PENDING', 'MORNING_BRIEFING')
    const list = DataTransformer.unwrapList<AiSuggestion>(resp)
    // 按类型过滤时前端再过滤状态（后端类型查询不带状态），与决策中心保持一致
    briefingList.value = list.filter(item => item.status === 'PENDING')
  } catch (error) {
    briefingList.value = []
    console.warn('AI 晨会简报加载失败，已降级为空状态', error)
  } finally {
    briefingLoading.value = false
  }
}

/**
 * 映射简报严重级别为 Element Plus 标签类型
 * @param severity 严重级别（CRITICAL/WARNING/INFO）
 * @returns 标签类型（danger/warning/info）
 */
const briefingSeverityTag = (severity: string): 'danger' | 'warning' | 'info' => {
  const tagMap: Record<string, 'danger' | 'warning' | 'info'> = {
    CRITICAL: 'danger',
    WARNING: 'warning',
    INFO: 'info'
  }
  return tagMap[severity] || 'info'
}

/**
 * 映射简报严重级别为中文标签
 * @param severity 严重级别（CRITICAL/WARNING/INFO）
 * @returns 中文标签（严重/警告/提示）
 */
const briefingSeverityLabel = (severity: string): string => {
  const labelMap: Record<string, string> = {
    CRITICAL: '严重',
    WARNING: '警告',
    INFO: '提示'
  }
  return labelMap[severity] || severity
}

/**
 * 跳转至 AI 晨会简报详情页
 */
const goToBriefing = () => {
  router.push('/home/ai/morning-briefing')
}

// 图表引用
const incomeExpenseChartRef = ref<HTMLElement | null>(null)
const orderStatusChartRef = ref<HTMLElement | null>(null)
const salesTrendChartRef = ref<HTMLElement | null>(null)
const inventoryTurnoverChartRef = ref<HTMLElement | null>(null)

// 图表实例
let incomeExpenseChart: echarts.ECharts | null = null
let orderStatusChart: echarts.ECharts | null = null
let salesTrendChart: echarts.ECharts | null = null
let inventoryTurnoverChart: echarts.ECharts | null = null

// 格式化金额
const formatMoney = (amount: number): string => {
  return new Intl.NumberFormat('zh-CN', {
    style: 'currency',
    currency: 'CNY',
    minimumFractionDigits: 2
  }).format(amount)
}

// 初始化收入支出趋势图
const initIncomeExpenseChart = () => {
  if (!incomeExpenseChartRef.value) return
  const container = incomeExpenseChartRef.value
  container.innerHTML = ''
  const fixedContainer = document.createElement('div')
  Object.assign(fixedContainer.style, { width: '100%', height: '320px', minHeight: '300px' })
  container.appendChild(fixedContainer)
  
  incomeExpenseChart = echarts.init(fixedContainer, null, { renderer: 'canvas' })
  incomeExpenseChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['收入', '支出'], top: 0 },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', boundaryGap: false, data: [] },
    yAxis: { type: 'value', name: '金额（元）' },
    series: [
      { name: '收入', type: 'line', smooth: true, areaStyle: { opacity: 0.3 }, data: [] },
      { name: '支出', type: 'line', smooth: true, areaStyle: { opacity: 0.3 }, data: [] }
    ]
  })
}

// 初始化订单状态分布图表
const initOrderStatusChart = () => {
  if (!orderStatusChartRef.value) return
  const container = orderStatusChartRef.value
  container.innerHTML = ''
  const fixedContainer = document.createElement('div')
  Object.assign(fixedContainer.style, { width: '100%', height: '320px', minHeight: '300px' })
  container.appendChild(fixedContainer)
  
  orderStatusChart = echarts.init(fixedContainer, null, { renderer: 'canvas' })
  orderStatusChart.setOption({
    tooltip: { trigger: 'item' },
    legend: { top: '5%', left: 'center' },
    series: [{
      name: '订单状态',
      type: 'pie',
      radius: ['40%', '70%'],
      avoidLabelOverlap: false,
      itemStyle: { borderRadius: 10, borderColor: '#fff', borderWidth: 2 },
      label: { show: false, position: 'center' },
      emphasis: { label: { show: true, fontSize: 20, fontWeight: 'bold' } },
      data: []
    }]
  })
}

// 初始化销售趋势图表
const initSalesTrendChart = () => {
  if (!salesTrendChartRef.value) return
  const container = salesTrendChartRef.value
  container.innerHTML = ''
  const fixedContainer = document.createElement('div')
  Object.assign(fixedContainer.style, { width: '100%', height: '320px', minHeight: '300px' })
  container.appendChild(fixedContainer)
  
  salesTrendChart = echarts.init(fixedContainer, null, { renderer: 'canvas' })
  salesTrendChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['销售额', '订单数量'], top: 0 },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', boundaryGap: false, data: [] },
    yAxis: [
      { type: 'value', name: '销售额（万元）', position: 'left' },
      { type: 'value', name: '订单数量', position: 'right' }
    ],
    series: [
      { name: '销售额', type: 'bar', itemStyle: { color: '#409eff' }, data: [] },
      { name: '订单数量', type: 'line', yAxisIndex: 1, itemStyle: { color: '#67c23a' }, data: [] }
    ]
  })
}

// 初始化库存周转率图表
const initInventoryTurnoverChart = () => {
  if (!inventoryTurnoverChartRef.value) return
  const container = inventoryTurnoverChartRef.value
  container.innerHTML = ''
  const fixedContainer = document.createElement('div')
  Object.assign(fixedContainer.style, { width: '100%', height: '320px', minHeight: '300px' })
  container.appendChild(fixedContainer)
  
  inventoryTurnoverChart = echarts.init(fixedContainer, null, { renderer: 'canvas' })
  inventoryTurnoverChart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    legend: { data: ['周转率'], top: 0 },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: ['1月', '2月', '3月', '4月', '5月', '6月'] },
    yAxis: { type: 'value', name: '周转率' },
    series: [{
      name: '周转率',
      type: 'bar',
      data: [],
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#67c23a' },
          { offset: 1, color: '#85ce61' }
        ])
      }
    }]
  })
}

// 初始化所有图表
const initCharts = () => {
  initIncomeExpenseChart()
  initOrderStatusChart()
  initSalesTrendChart()
  initInventoryTurnoverChart()
}

// 监听窗口大小变化
const handleResize = () => {
  incomeExpenseChart?.resize()
  orderStatusChart?.resize()
  salesTrendChart?.resize()
  inventoryTurnoverChart?.resize()
}

/**
 * 计算环比变化百分比
 * 取趋势数据最近两期计算变化率，数据不足时返回0
 * @param values 趋势数值列表
 * @returns 环比变化百分比（保留1位小数）
 */
const calculateChangeRate = (values: number[]): number => {
  if (values.length < 2) return 0
  const prev = values[values.length - 2] ?? 0
  const curr = values[values.length - 1] ?? 0
  if (!prev) return 0
  return Number((((curr - prev) / prev) * 100).toFixed(1))
}

/**
 * 映射生产订单状态编码为显示名称
 * @param status 后端订单状态编码
 * @returns 状态显示名称
 */
const mapOrderStatusName = (status: any): string => {
  switch (Number(status)) {
    case 0: return '待处理'
    case 1: return '处理中'
    case 2: return '已完成'
    case 3: return '已取消'
    default: return `状态${status ?? '未知'}`
  }
}

// 订单状态名称与颜色映射
const orderStatusColorMap: Record<string, string> = {
  '待处理': '#e6a23c',
  '处理中': '#67c23a',
  '已完成': '#409eff',
  '已取消': '#f56c6c'
}

/**
 * 获取仪表盘数据
 * 调用后端 ERP 仪表盘真实接口（/api/v1/erp/dashboard/*）获取统计与图表数据，
 * 接口异常或无数据时对应图表显示为空，不生成任何模拟数据
 * @param range 时间范围（month/quarter/year）
 */
const fetchDashboardData = async (range: string) => {
  loading.value = true
  try {
    const [statsResult, incomeExpenseResult, orderStatusResult, salesTrendResult, turnoverResult] = await Promise.allSettled([
      dashboardApi.getStats(),
      dashboardApi.getIncomeExpenseTrend(range),
      dashboardApi.getOrderStatusDistribution(),
      dashboardApi.getSalesTrend(),
      dashboardApi.getInventoryTurnover()
    ])

    // 统计数据（接口失败时按空对象处理）
    const stats = statsResult.status === 'fulfilled' ? (unwrapResponseData<any>(statsResult.value) ?? {}) : {}
    // 收支趋势列表
    const incomeExpenseList = incomeExpenseResult.status === 'fulfilled' ? unwrapListResponse<any>(incomeExpenseResult.value) : []
    // 订单状态分布列表
    const orderStatusList = orderStatusResult.status === 'fulfilled' ? unwrapListResponse<any>(orderStatusResult.value) : []
    // 销售趋势列表
    const salesTrendList = salesTrendResult.status === 'fulfilled' ? unwrapListResponse<any>(salesTrendResult.value) : []
    // 库存周转汇总
    const turnover = turnoverResult.status === 'fulfilled' ? (unwrapResponseData<any>(turnoverResult.value) ?? {}) : {}

    // 更新指标卡片：收入取凭证贷方合计，支出取借方合计，订单取生产单数，库存取物料数
    totalIncome.value = Number(stats?.totalCredit ?? 0)
    totalExpense.value = Number(stats?.totalDebit ?? 0)
    totalOrders.value = Number(stats?.productionCount ?? 0)
    totalInventory.value = Number(stats?.materialCount ?? 0)

    // 环比变化率基于真实收支趋势计算，订单与库存无历史序列时显示0
    incomeChange.value = calculateChangeRate(incomeExpenseList.map(item => Number(item?.income ?? 0)))
    expenseChange.value = calculateChangeRate(incomeExpenseList.map(item => Number(item?.expense ?? 0)))
    orderChange.value = 0
    inventoryChange.value = 0

    // 更新图表数据
    updateCharts(incomeExpenseList, orderStatusList, salesTrendList, turnover)
  } catch (error) {
    ElMessage.error('获取仪表盘数据失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

/**
 * 更新图表数据
 * 使用后端真实数据渲染各图表，无数据时图表显示为空
 * @param incomeExpenseList 收支趋势列表
 * @param orderStatusList 订单状态分布列表
 * @param salesTrendList 销售趋势列表
 * @param turnover 库存周转汇总数据
 */
const updateCharts = (incomeExpenseList: any[], orderStatusList: any[], salesTrendList: any[], turnover: any) => {
  // 1. 收入支出趋势（按后端返回的月份周期展示）
  if (incomeExpenseChart) {
    incomeExpenseChart.setOption({
      xAxis: { data: incomeExpenseList.map(item => String(item?.period ?? '')) },
      series: [
        { data: incomeExpenseList.map(item => Number(item?.income ?? 0)) },
        { data: incomeExpenseList.map(item => Number(item?.expense ?? 0)) }
      ]
    })
  }

  // 2. 订单状态分布（映射状态编码为名称）
  if (orderStatusChart) {
    orderStatusChart.setOption({
      series: [{
        data: orderStatusList.map(item => {
          const name = mapOrderStatusName(item?.status)
          return {
            value: Number(item?.count ?? 0),
            name,
            itemStyle: { color: orderStatusColorMap[name] || '#909399' }
          }
        })
      }]
    })
  }

  // 3. 销售趋势（销售额单位由元换算为万元）
  if (salesTrendChart) {
    salesTrendChart.setOption({
      xAxis: { data: salesTrendList.map(item => String(item?.period ?? '')) },
      series: [
        { data: salesTrendList.map(item => Number((Number(item?.amount ?? 0) / 10000).toFixed(2))) },
        { data: salesTrendList.map(item => Number(item?.orderCount ?? 0)) }
      ]
    })
  }

  // 4. 库存周转（后端提供汇总指标，以柱状图展示真实汇总数据）
  if (inventoryTurnoverChart) {
    inventoryTurnoverChart.setOption({
      legend: { data: ['数量'], top: 0 },
      xAxis: { data: ['物料总数', '低于安全库存', '年出入库业务量'] },
      yAxis: { name: '数量' },
      series: [{
        name: '数量',
        data: [
          Number(turnover?.materialCount ?? 0),
          Number(turnover?.belowSafetyStock ?? 0),
          Number(turnover?.yearInboundOutbound ?? 0)
        ]
      }]
    })
  }
}

// 监听日期范围变化
watch(dateRange, (newValue) => {
  fetchDashboardData(newValue)
})

onMounted(async () => {
  await nextTick()
  initCharts()
  // 晨会简报独立并行加载，失败静默降级不阻塞仪表盘主数据
  fetchMorningBriefing()
  await fetchDashboardData(dateRange.value)
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  incomeExpenseChart?.dispose()
  orderStatusChart?.dispose()
  salesTrendChart?.dispose()
  inventoryTurnoverChart?.dispose()
})
</script>

<style scoped lang="scss">
.dashboard {
  padding: 24px;
  min-height: 100%;
}

.dashboard-header {
  display: flex;
  align-items: baseline;
  gap: 14px;
  margin-bottom: 24px;
}

.dashboard-title {
  color: var(--text-primary);
  font-size: 1.5rem;
  font-weight: 700;
  letter-spacing: -0.5px;
  position: relative;
  padding-left: 14px;

  &::before {
    content: '';
    position: absolute;
    left: 0;
    top: 50%;
    transform: translateY(-50%);
    width: 5px;
    height: 70%;
    border-radius: 3px;
    background: var(--brand-gradient);
  }
}

.dashboard-subtitle {
  font-size: 13px;
  color: var(--text-secondary);
  letter-spacing: 0.5px;
}

/* 指标卡片 */
.metrics-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: 24px;
  margin-bottom: 32px;
}

.metric-card {
  cursor: pointer;
  background: #fff;
  border-radius: var(--border-radius-lg);
  overflow: hidden;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid rgba(255,255,255,0.6);
  
  &:hover {
    transform: translateY(-8px);
    box-shadow: var(--shadow-md) !important;
  }
}

.metric-content {
  display: flex;
  align-items: flex-start;
  padding: 8px 0;
}

.metric-icon {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  font-size: 28px;
  flex-shrink: 0;
  
  &.income {
    background: linear-gradient(135deg, rgba(16, 185, 129, 0.1) 0%, rgba(16, 185, 129, 0.2) 100%);
    color: #10b981;
  }
  
  &.expense {
    background: linear-gradient(135deg, rgba(245, 158, 11, 0.1) 0%, rgba(245, 158, 11, 0.2) 100%);
    color: #f59e0b;
  }
  
  &.order {
    background: linear-gradient(135deg, rgba(59, 130, 246, 0.1) 0%, rgba(59, 130, 246, 0.2) 100%);
    color: #3b82f6;
  }
  
  &.inventory {
    background: linear-gradient(135deg, rgba(99, 102, 241, 0.1) 0%, rgba(99, 102, 241, 0.2) 100%);
    color: #6366f1;
  }
}

.metric-info {
  flex: 1;
}

.metric-value {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 4px;
  line-height: 1.2;
}

.metric-label {
  font-size: 14px;
  color: var(--text-secondary);
  margin-bottom: 8px;
}

.metric-change {
  display: flex;
  align-items: center;
  font-size: 13px;
  font-weight: 500;
  
  .el-icon {
    margin-right: 2px;
  }
  
  .positive { color: #10b981; }
  .negative { color: #ef4444; }
  
  .compared-to {
    margin-left: 6px;
    color: var(--text-placeholder);
    font-weight: normal;
  }
}

/* AI 晨会简报 */
.briefing-card {
  margin-bottom: 32px;
  border-radius: var(--border-radius-lg);
  border: none;
  box-shadow: var(--shadow-sm);
  transition: all 0.3s ease;

  &:hover {
    box-shadow: var(--shadow-md);
  }
}

.briefing-title {
  display: flex;
  align-items: center;
  gap: 8px;

  .briefing-title-icon {
    font-size: 18px;
    color: var(--el-color-primary);
  }
}

.briefing-body {
  min-height: 72px;
}

.briefing-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 0;
  border-bottom: 1px solid var(--el-border-color-lighter);

  &:last-of-type {
    border-bottom: none;
  }

  .briefing-item-title {
    flex: 1;
    min-width: 0;
    font-size: 14px;
    color: var(--text-primary);
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.briefing-more {
  margin-top: 10px;
  text-align: right;
  font-size: 13px;
  color: var(--text-placeholder);
}

.briefing-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 24px 0;
  font-size: 14px;
  color: var(--text-secondary);

  .briefing-empty-icon {
    font-size: 20px;
    color: #10b981;
  }
}

/* 图表区域 */
.charts-area {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24px;
}

.chart-card {
  border-radius: var(--border-radius-lg);
  border: none;
  box-shadow: var(--shadow-sm);
  transition: all 0.3s ease;
  
  &:hover {
    box-shadow: var(--shadow-md);
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  font-size: 16px;
  color: var(--text-primary);
}

.chart-container {
  height: 320px;
  width: 100%;
  position: relative;
}

.chart {
  width: 100%;
  height: 100%;
}

@media (max-width: 1200px) {
  .charts-area {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .dashboard {
    padding: 16px;
  }
  
  .metrics-cards {
    grid-template-columns: 1fr;
    gap: 16px;
  }
  
  .chart-container {
    height: 280px;
  }
}
</style>
