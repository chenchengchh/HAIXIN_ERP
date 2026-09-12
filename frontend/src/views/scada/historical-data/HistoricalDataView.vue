<template>
  <div class="historical-data-view">
    <div class="page-header">
      <h2>历史数据</h2>
      <el-breadcrumb>
        <el-breadcrumb-item>
          <router-link to="/home">首页</router-link>
        </el-breadcrumb-item>
        <el-breadcrumb-item>
          <router-link to="/home/scada">SCADA系统</router-link>
        </el-breadcrumb-item>
        <el-breadcrumb-item>
          <router-link to="/home/scada/historical-data">历史数据</router-link>
        </el-breadcrumb-item>
        <el-breadcrumb-item :to="`/home/scada/historical-data#${activeTab}`">
          {{ tabLabelMap[activeTab] }}
        </el-breadcrumb-item>
      </el-breadcrumb>
    </div>
    <el-tabs v-model="activeTab" class="function-tabs" @tab-change="handleTabChange">
      <el-tab-pane label="历史数据检索" name="data-retrieval" />
      <el-tab-pane label="数据导出" name="data-export" />
      <el-tab-pane label="报表生成" name="report-generation" />
      <el-tab-pane label="数据分析" name="data-analysis" />
    </el-tabs>
    
    <!-- 历史数据检索 -->
    <div v-if="activeTab === 'data-retrieval'" class="tab-content">
      <el-card shadow="hover" class="sub-card">
        <template #header>
          <div class="sub-card-header">
            <span>历史数据检索</span>
          </div>
        </template>
        <div class="data-retrieval">
          <div class="filter-bar">
            <el-select v-model="retrievalParams.tagCode" placeholder="选择点位" style="width: 150px; margin-right: 16px;">
              <el-option label="温度" value="temperature" />
              <el-option label="压力" value="pressure" />
              <el-option label="流量" value="flow" />
            </el-select>
            <el-date-picker v-model="retrievalParams.dateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" style="width: 300px; margin-right: 16px;"></el-date-picker>
            <el-time-picker v-model="retrievalParams.timeRange" type="timerange" range-separator="至" start-placeholder="开始时间" end-placeholder="结束时间" style="width: 200px; margin-right: 16px;"></el-time-picker>
            <el-select v-model="retrievalParams.interval" placeholder="数据间隔" style="width: 120px; margin-right: 16px;">
              <el-option label="1分钟" value="1m" />
              <el-option label="5分钟" value="5m" />
              <el-option label="15分钟" value="15m" />
              <el-option label="1小时" value="1h" />
            </el-select>
            <el-button type="primary" @click="handleRetrieveData">检索</el-button>
          </div>
          <div class="retrieval-result">
            <el-table :data="historicalData" style="width: 100%" height="300">
              <el-table-column prop="timestamp" label="时间" width="180" />
              <el-table-column prop="value" label="数值" width="120" />
              <el-table-column prop="unit" label="单位" width="80" />
            </el-table>
          </div>
        </div>
      </el-card>
    </div>
    
    <!-- 数据导出 -->
    <div v-if="activeTab === 'data-export'" class="tab-content">
      <el-card shadow="hover" class="sub-card">
        <template #header>
          <div class="sub-card-header">
            <span>数据导出</span>
          </div>
        </template>
        <div class="data-export-content">
          <div class="export-options">
            <h4>导出选项</h4>
            <el-form :model="exportForm" label-width="120px">
              <el-form-item label="导出格式">
                <el-select v-model="exportForm.format" placeholder="选择格式" style="width: 150px;">
                  <el-option label="CSV" value="csv" />
                </el-select>
              </el-form-item>
              <el-form-item label="导出范围">
                <el-radio-group v-model="exportForm.range">
                  <el-radio value="当前检索结果">当前检索结果</el-radio>
                  <el-radio value="自定义范围">自定义范围</el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="handleExportData">导出数据</el-button>
                <el-button @click="handleClearResult">清空结果</el-button>
              </el-form-item>
            </el-form>
          </div>
        </div>
      </el-card>
    </div>
    
    <!-- 报表生成 -->
    <div v-if="activeTab === 'report-generation'" class="tab-content">
      <el-card shadow="hover" class="sub-card">
        <template #header>
          <div class="sub-card-header">
            <span>报表生成</span>
          </div>
        </template>
        <div class="report-generation">
          <el-form :model="reportForm" label-width="100px">
            <el-form-item label="报表类型">
              <el-radio-group v-model="reportForm.type">
                <el-radio value="班报">班报</el-radio>
                <el-radio value="日报">日报</el-radio>
                <el-radio value="月报">月报</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="生成日期">
              <el-date-picker v-model="reportForm.date" type="date" placeholder="选择日期" style="width: 100%;" />
            </el-form-item>
            <el-form-item label="报表格式">
              <el-select v-model="reportForm.format" placeholder="选择格式">
                <el-option label="Excel" value="excel" />
                <el-option label="PDF" value="pdf" />
                <el-option label="CSV" value="csv" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleGenerateReport">生成报表</el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-card>
    </div>
    
    <!-- 数据分析 -->
    <div v-if="activeTab === 'data-analysis'" class="tab-content">
      <el-card shadow="hover" class="sub-card">
        <template #header>
          <div class="sub-card-header">
            <span>历史数据分析</span>
          </div>
        </template>
        <div class="data-analysis">
          <div class="analysis-item">
            <h4>设备稼动率</h4>
            <el-progress :percentage="equipmentAvailability" :format="availabilityFormat" />
          </div>
          <div class="analysis-item">
            <h4>数据波动性分析</h4>
            <div class="volatility-chart">
              <div class="chart-title">温度波动趋势</div>
              <div class="chart-container">
                <svg width="100%" height="200" viewBox="0 0 800 200">
                  <!-- 网格线 -->
                  <line x1="50" y1="20" x2="750" y2="20" stroke="#e4e7ed" stroke-width="1" stroke-dasharray="2,2" />
                  <line x1="50" y1="60" x2="750" y2="60" stroke="#e4e7ed" stroke-width="1" stroke-dasharray="2,2" />
                  <line x1="50" y1="100" x2="750" y2="100" stroke="#e4e7ed" stroke-width="1" stroke-dasharray="2,2" />
                  <line x1="50" y1="140" x2="750" y2="140" stroke="#e4e7ed" stroke-width="1" stroke-dasharray="2,2" />
                  <line x1="50" y1="180" x2="750" y2="180" stroke="#e4e7ed" stroke-width="1" stroke-dasharray="2,2" />
                  <!-- 坐标轴 -->
                  <line x1="50" y1="20" x2="50" y2="180" stroke="#909399" stroke-width="2" />
                  <line x1="50" y1="180" x2="750" y2="180" stroke="#909399" stroke-width="2" />
                  <!-- Y轴标签（根据真实数据值域动态计算） -->
                  <text v-for="tick in volatilityYTicks" :key="tick.y" x="30" :y="tick.y + 5" font-size="12" fill="#606266" text-anchor="end">{{ tick.label }}</text>
                  <!-- 波动曲线（真实趋势数据） -->
                  <polyline v-if="volatilityPoints.length > 1"
                            :points="volatilityPoints.map(p => `${p.x},${p.y}`).join(' ')"
                            fill="none"
                            stroke="#409EFF"
                            stroke-width="3" />
                  <!-- 数据点 -->
                  <circle v-for="(p, index) in volatilityPoints" :key="index" :cx="p.x" :cy="p.y" r="4" fill="#409EFF" stroke="white" stroke-width="2" />
                  <text v-if="volatilityPoints.length === 0" x="400" y="100" font-size="14" fill="#909399" text-anchor="middle">暂无数据</text>
                </svg>
              </div>
            </div>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { unwrapListResponse, unwrapResponseData } from '@/api'
import { scadaHistoryApi, scadaReportApi, scadaOverviewApi, scadaRealtimeApi } from '@/api/scada'

// 路由实例
const router = useRouter()
const route = useRoute()

// 激活的标签页
const activeTab = ref('data-retrieval')

// 标签页名称映射
const tabLabelMap: Record<string, string> = {
  'data-retrieval': '历史数据检索',
  'data-export': '数据导出',
  'report-generation': '报表生成',
  'data-analysis': '数据分析'
}

// 历史数据查询模块
const retrievalParams = ref({
  tagCode: 'temperature',
  dateRange: [],
  timeRange: [],
  interval: '5m'
})

const historicalData = ref<any[]>([])

// 导出表单
const exportForm = ref({
  format: 'csv',
  range: '当前检索结果'
})

const reportForm = ref({
  type: '日报',
  date: new Date(),
  format: 'excel'
})

const equipmentAvailability = ref(0)

// 温度波动趋势原始数据（来自实时趋势接口）
const volatilityData = ref<number[]>([])

// 加载温度波动趋势数据（取温度点位最近12个值）
const loadVolatilityData = async () => {
  try {
    const response = await scadaRealtimeApi.getTrend({ point: 'temperature', size: 12 })
    const data = unwrapResponseData<any>(response)
    const values: number[] = Array.isArray(data)
      ? data.map((x: any) => (typeof x === 'number' ? x : Number(x?.value))).filter((x: any) => Number.isFinite(x))
      : (Array.isArray(data?.values) ? data.values.map(Number).filter((x: number) => Number.isFinite(x)) : [])
    volatilityData.value = values
  } catch (error) {
    console.error('加载温度波动趋势失败:', error)
  }
}

// 将趋势值映射为SVG坐标点（X: 50-750均分，Y: 20-180按值域反比映射）
const volatilityPoints = computed(() => {
  const values = volatilityData.value
  if (!values.length) return [] as Array<{ x: number; y: number }>
  const max = Math.max(...values)
  const min = Math.min(...values)
  const range = max - min || 1
  const stepX = values.length > 1 ? 700 / (values.length - 1) : 0
  return values.map((v, i) => ({
    x: Math.round((50 + i * stepX) * 10) / 10,
    y: Math.round((180 - ((v - min) / range) * 160) * 10) / 10
  }))
})

// Y轴刻度标签（max→min 五等分）
const volatilityYTicks = computed(() => {
  const values = volatilityData.value
  if (!values.length) return [] as Array<{ y: number; label: string }>
  const max = Math.max(...values)
  const min = Math.min(...values)
  const step = (max - min) / 4
  return [20, 60, 100, 140, 180].map((y, i) => ({
    y,
    label: `${(max - step * i).toFixed(1)}°C`
  }))
})

// 加载设备稼动率（基于在线设备占比计算）
const loadEquipmentAvailability = async () => {
  try {
    const response = await scadaOverviewApi.stats()
    const data = unwrapResponseData<any>(response) || {}
    const total = Number(data.totalDevices) || 0
    const online = Number(data.onlineDevices) || 0
    equipmentAvailability.value = total > 0 ? Math.round((online / total) * 1000) / 10 : 0
  } catch (error) {
    console.error('加载设备稼动率失败:', error)
  }
}

// 从路由获取标签页状态
const getActiveTabFromRoute = () => {
  const tabMap: Record<string, string> = {
    'data-retrieval': 'data-retrieval',
    'data-export': 'data-export',
    'report-generation': 'report-generation',
    'data-analysis': 'data-analysis'
  }
  const tabName = route.params.tab || 'data-retrieval'
  return tabMap[tabName as string] || 'data-retrieval'
}

// 组件挂载时，从路由获取标签页状态
onMounted(() => {
  activeTab.value = getActiveTabFromRoute()
  loadEquipmentAvailability()
  loadVolatilityData()
})

// 标签页切换事件
const handleTabChange = (tabName: string) => {
  console.log('切换标签页:', tabName)
  // 更新路由，保持标签页状态
  router.push({
    path: `/home/scada/historical-data/${tabName}`
  })
}

// 计算属性
const availabilityFormat = (percentage: number) => {
  return `${percentage}%`
}

// 方法
const handleRetrieveData = async () => {
  try {
    const [startDate, endDate] = (retrievalParams.value.dateRange as any) || []
    const response = await scadaHistoryApi.retrieve({
      tagCode: retrievalParams.value.tagCode,
      startTime: startDate ? new Date(startDate).toISOString() : undefined,
      endTime: endDate ? new Date(endDate).toISOString() : undefined,
      interval: retrievalParams.value.interval
    })

    const list = unwrapListResponse<any>(response)
    historicalData.value = list.map((item: any) => {
      return {
        timestamp: item.timestamp || item.ts || '',
        value: item.value,
        unit: item.unit || '°C'
      }
    })

    ElMessage({ message: `历史数据检索成功，共找到${historicalData.value.length}条记录`, type: 'success' })
  } catch (error) {
    console.error('检索历史数据失败:', error)
    ElMessage({ message: '历史数据检索失败', type: 'error' })
  }
}

const handleExportData = async () => {
  try {
    const [startDate, endDate] = (retrievalParams.value.dateRange as any) || []
    const response = await scadaHistoryApi.export({
      tagCode: retrievalParams.value.tagCode,
      startTime: startDate ? new Date(startDate).toISOString() : undefined,
      endTime: endDate ? new Date(endDate).toISOString() : undefined,
      interval: retrievalParams.value.interval,
      format: exportForm.value.format
    })

    const mime = exportForm.value.format === 'csv'
      ? 'text/csv;charset=utf-8'
      : 'application/vnd.ms-excel'
    const blob = new Blob([response.data], { type: mime })
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `SCADA_历史数据_${retrievalParams.value.tagCode}_${new Date().toISOString().split('T')[0]}.${exportForm.value.format === 'csv' ? 'csv' : 'xlsx'}`
    link.click()
    URL.revokeObjectURL(url)

    ElMessage({ message: `数据已成功导出为${exportForm.value.format.toUpperCase()}格式`, type: 'success' })
  } catch (error) {
    console.error('导出数据失败:', error)
    ElMessage({ message: '数据导出失败', type: 'error' })
  }
}

const handleClearResult = () => {
  try {
    console.log('清空结果')
    historicalData.value = []
    // 显示成功消息
    ElMessage({ message: '结果已清空', type: 'success' })
  } catch (error) {
    console.error('清空结果失败:', error)
    ElMessage({ message: '清空结果失败', type: 'error' })
  }
}

const handleGenerateReport = async () => {
  try {
    const response = await scadaReportApi.generate(reportForm.value)

    // 按报表格式确定MIME类型与文件扩展名，触发浏览器下载
    const format = reportForm.value.format
    const mimeMap: Record<string, string> = {
      excel: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
      csv: 'text/csv;charset=utf-8',
      pdf: 'application/pdf'
    }
    const extMap: Record<string, string> = { excel: 'xlsx', csv: 'csv', pdf: 'pdf' }
    const blob = new Blob([response.data], { type: mimeMap[format] || mimeMap.excel })
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    const dateText = reportForm.value.date ? new Date(reportForm.value.date).toISOString().split('T')[0] : new Date().toISOString().split('T')[0]
    link.download = `SCADA_${reportForm.value.type}_${dateText}.${extMap[format] || 'xlsx'}`
    link.click()
    URL.revokeObjectURL(url)

    ElMessage({ message: `${reportForm.value.type}报表生成成功，格式为${format.toUpperCase()}`, type: 'success' })
  } catch (error) {
    console.error('生成报表失败:', error)
    ElMessage({ message: '报表生成失败', type: 'error' })
  }
}
</script>

<style scoped>
.historical-data-view {
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

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
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

.sub-card-content {
  padding: 10px 0;
}

.filter-bar {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
}

.retrieval-result {
  padding: 10px 0;
}

.result-actions {
  display: flex;
  justify-content: flex-start;
  gap: 10px;
}

.report-generation {
  padding: 10px 0;
}

/* 数据分析 */
.data-analysis {
  padding: 10px 0;
}

.analysis-item {
  margin-bottom: 20px;
}

.analysis-item h4 {
  margin: 0 0 10px 0;
  font-size: 14px;
  color: #606266;
}

/* 波动性分析图表 */
.volatility-chart {
  background-color: #fafafa;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 16px;
}

.volatility-chart .chart-title {
  font-size: 14px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 12px;
  text-align: center;
}

.volatility-chart .chart-container {
  width: 100%;
  height: 200px;
  position: relative;
  overflow: hidden;
}

.volatility-chart svg {
  width: 100%;
  height: 100%;
}

.data-export-content {
  padding: 10px 0;
}

.export-options h4 {
  margin: 0 0 15px 0;
  font-size: 14px;
  color: #606266;
}

.tab-content {
  padding: 10px 0;
}
</style>
