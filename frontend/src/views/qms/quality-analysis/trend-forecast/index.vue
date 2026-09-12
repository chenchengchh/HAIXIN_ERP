<template>
  <div class="trend-forecast-view">
    <div class="page-header">
      <h3>质量趋势预测</h3>
      <el-button type="primary" @click="generateForecast">
        <el-icon><RefreshRight /></el-icon>生成预测
      </el-button>
    </div>

    <!-- 预测参数设置 -->
    <el-card class="mb-4" shadow="hover">
      <h4>预测参数设置</h4>
      <el-form :model="forecastParams" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="预测指标">
              <el-select v-model="forecastParams.indicator" placeholder="请选择预测指标" clearable>
                <el-option label="产品合格率" value="passRate" />
                <el-option label="不合格品数量" value="ncCount" />
                <el-option label="异常发生次数" value="anomalyCount" />
                <el-option label="客户投诉数量" value="complaintCount" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="预测周期">
              <el-select v-model="forecastParams.period" placeholder="请选择预测周期" clearable>
                <el-option label="日" value="daily" />
                <el-option label="周" value="weekly" />
                <el-option label="月" value="monthly" />
                <el-option label="季度" value="quarterly" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="历史数据跨度">
              <el-select v-model="forecastParams.historySpan" placeholder="请选择历史数据跨度" clearable>
                <el-option label="3个月" value="3" />
                <el-option label="6个月" value="6" />
                <el-option label="12个月" value="12" />
                <el-option label="24个月" value="24" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="预测时长">
              <el-select v-model="forecastParams.forecastLength" placeholder="请选择预测时长" clearable>
                <el-option label="1个月" value="1" />
                <el-option label="3个月" value="3" />
                <el-option label="6个月" value="6" />
                <el-option label="12个月" value="12" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="置信度">
              <el-slider
                v-model="forecastParams.confidence"
                :min="70"
                :max="99"
                :step="1"
                :marks="{ 70: '70%', 80: '80%', 90: '90%', 99: '99%' }"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="预测模型">
              <el-select v-model="forecastParams.model" placeholder="请选择预测模型" clearable>
                <el-option label="线性回归" value="linear" />
                <el-option label="ARIMA" value="arima" />
                <el-option label="指数平滑" value="exponential" />
                <el-option label="LSTM" value="lstm" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item>
              <el-button type="primary" @click="applyForecastParams">应用参数</el-button>
              <el-button @click="resetForecastParams">重置</el-button>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </el-card>

    <!-- 趋势预测图表 -->
    <el-card class="mb-4" shadow="hover">
      <h4>趋势预测图表</h4>
      <div class="chart-container">
        <div class="chart-wrapper">
          <el-empty v-if="!chartPoints.length" description="暂无预测数据" />
          <div v-else class="chart-points">
            <div class="chart-title">
              预测类型：{{ currentForecast.forecastType }}，周期：{{ currentForecast.period }}，置信区间：{{ Math.round(currentForecast.confidenceInterval * 100) }}%
            </div>
            <el-table :data="chartPoints" border size="small" style="width: 100%">
              <el-table-column prop="x" label="序号" min-width="80" />
              <el-table-column prop="y" label="数值" min-width="120" />
            </el-table>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 预测结果表格 -->
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>预测结果</span>
          <el-tag size="small" type="info">{{ forecastResults.length }} 条记录</el-tag>
        </div>
      </template>
      <el-table :data="forecastResults" border style="width: 100%">
        <el-table-column prop="period" label="预测周期" min-width="120" />
        <el-table-column prop="actualValue" label="实际值" min-width="100" />
        <el-table-column prop="forecastValue" label="预测值" min-width="100" />
        <el-table-column prop="lowerBound" label="下限" min-width="100" />
        <el-table-column prop="upperBound" label="上限" min-width="100" />
        <el-table-column prop="confidence" label="置信度" min-width="100" />
        <el-table-column prop="deviation" label="偏差" min-width="100" />
        <el-table-column prop="trend" label="趋势" min-width="100">
          <template #default="scope">
            <el-tag :type="getTrendTagType(scope.row.trend)">
              {{ getTrendLabel(scope.row.trend) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="200" show-overflow-tooltip />
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { RefreshRight } from '@element-plus/icons-vue'
import { unwrapPageResponse, unwrapResponseData } from '@/api'
import { TrendForecastAPI } from '@/api/qms'

// 预测结果类型
interface ForecastResult {
  id: string | number
  period: string
  actualValue: number | null
  forecastValue: number
  lowerBound: number
  upperBound: number
  confidence: number
  deviation: number
  trend: 'up' | 'down' | 'stable'
  remark: string
}

// 预测参数
const forecastParams = reactive({
  indicator: 'passRate',
  period: 'monthly',
  historySpan: '12',
  forecastLength: '6',
  confidence: 95,
  model: 'arima'
})

// 预测结果列表
const forecastResults = ref<ForecastResult[]>([])

const currentForecast = reactive({
  id: '' as string | number,
  forecastType: '',
  period: '',
  confidenceInterval: 0.95
})

const chartPoints = ref<{ x: number | string; y: number }[]>([])

const calcTrend = (prev: number | null, next: number): ForecastResult['trend'] => {
  if (prev == null) return 'stable'
  if (next > prev) return 'up'
  if (next < prev) return 'down'
  return 'stable'
}

const toForecastRows = (forecastData: any[], ci: number): ForecastResult[] => {
  const points = Array.isArray(forecastData) ? forecastData : []
  let prev: number | null = null
  return points.map((p: any, index: number) => {
    const y = Number(p?.y ?? 0)
    const delta = Math.abs(y) * (1 - ci) * 2
    const trend = calcTrend(prev, y)
    const row: ForecastResult = {
      id: `${index + 1}`,
      period: String(p?.x ?? index + 1),
      actualValue: null,
      forecastValue: y,
      lowerBound: y - delta,
      upperBound: y + delta,
      confidence: Math.round(ci * 100),
      deviation: prev == null ? 0 : Number((y - prev).toFixed(3)),
      trend,
      remark: '预测点'
    }
    prev = y
    return row
  })
}

const mapIndicatorToForecastType = (indicator: string) => {
  const map: Record<string, string> = {
    passRate: 'pass-rate',
    ncCount: 'nc-count',
    anomalyCount: 'anomaly-count',
    complaintCount: 'complaint-count'
  }
  return map[indicator] || indicator
}

// 获取趋势标签样式
const getTrendTagType = (trend: string) => {
  const trendMap: Record<string, string> = {
    up: 'success',
    down: 'danger',
    stable: 'info'
  }
  return trendMap[trend] || 'info'
}

// 获取趋势标签文本
const getTrendLabel = (trend: string) => {
  const trendMap: Record<string, string> = {
    up: '上升',
    down: '下降',
    stable: '稳定'
  }
  return trendMap[trend] || trend
}

// 应用预测参数
const applyForecastParams = () => {
  ElMessage.success('预测参数已应用')
}

// 重置预测参数
const resetForecastParams = () => {
  Object.assign(forecastParams, {
    indicator: 'passRate',
    period: 'monthly',
    historySpan: '12',
    forecastLength: '6',
    confidence: 95,
    model: 'arima'
  })
  ElMessage.success('预测参数已重置')
}

// 生成预测
const generateForecast = async () => {
  try {
    const forecastType = mapIndicatorToForecastType(forecastParams.indicator)
    const res = await TrendForecastAPI.generateForecast({
      forecastType,
      productIds: [],
      parameter: forecastParams.period,
      historicalPeriod: Number(forecastParams.historySpan),
      forecastPeriod: Number(forecastParams.forecastLength),
      confidenceInterval: forecastParams.confidence / 100
    } as any)
    const entity = unwrapResponseData<any>(res) || {}
    currentForecast.id = entity.id ?? ''
    currentForecast.forecastType = String(entity.forecastType ?? forecastType)
    currentForecast.period = String(entity.period ?? '')
    currentForecast.confidenceInterval = Number(entity.confidenceInterval ?? forecastParams.confidence / 100)

    const points = Array.isArray(entity.forecastData) ? entity.forecastData : []
    chartPoints.value = points.map((p: any) => ({ x: p?.x ?? '', y: Number(p?.y ?? 0) }))
    forecastResults.value = toForecastRows(points, currentForecast.confidenceInterval)
    ElMessage.success('预测生成成功')
  } catch (e: any) {
    ElMessage.error(e?.message || '预测生成失败')
  }
}

const loadLatestForecast = async () => {
  try {
    const forecastType = mapIndicatorToForecastType(forecastParams.indicator)
    const res = await TrendForecastAPI.getForecastResults({
      page: 1,
      size: 10,
      forecastType
    })
    const page = unwrapPageResponse<any>(res)
    const records = page.list as any[]
    const latest = records[0]
    if (!latest) {
      chartPoints.value = []
      forecastResults.value = []
      return
    }
    currentForecast.id = latest.id ?? ''
    currentForecast.forecastType = String(latest.forecastType ?? '')
    currentForecast.period = String(latest.period ?? '')
    currentForecast.confidenceInterval = Number(latest.confidenceInterval ?? 0.95)
    const points = Array.isArray(latest.forecastData) ? latest.forecastData : []
    chartPoints.value = points.map((p: any) => ({ x: p?.x ?? '', y: Number(p?.y ?? 0) }))
    forecastResults.value = toForecastRows(points, currentForecast.confidenceInterval)
  } catch (e: any) {
    ElMessage.error(e?.message || '加载预测结果失败')
  }
}

onMounted(loadLatestForecast)
</script>

<style scoped>
.trend-forecast-view {
  padding: 20px;
  height: 100%;
  overflow: auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.mb-4 {
  margin-bottom: 20px;
}

.chart-container {
  padding: 20px;
  background-color: #f5f7fa;
  border-radius: 8px;
}

.chart-wrapper {
  height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.simulated-chart {
  width: 100%;
  height: 100%;
  background-color: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.chart-title {
  text-align: center;
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 20px;
}

.chart-content {
  height: 300px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.chart-footer {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

/* Trend Chart */
.trend-chart .chart-content {
  position: relative;
}

.trend-chart .chart-axis {
  position: relative;
  width: 100%;
  height: 100%;
  display: flex;
}

.trend-chart .y-axis {
  width: 60px;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  align-items: flex-end;
}

.trend-chart .x-axis {
  flex: 1;
  height: 100%;
  position: relative;
  margin-left: 20px;
}

.trend-chart .axis-label {
  writing-mode: vertical-rl;
  text-orientation: mixed;
  font-size: 12px;
  margin: 5px 0;
}

.trend-chart .x-axis .axis-label {
  writing-mode: horizontal-tb;
  position: absolute;
  bottom: -20px;
  left: 50%;
  transform: translateX(-50%);
  margin: 0;
}

.trend-chart .y-axis .axis-ticks {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  align-items: flex-end;
  height: 260px;
  margin-top: 20px;
}

.trend-chart .x-axis .axis-ticks {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  position: absolute;
  bottom: 0;
  width: 100%;
  height: 20px;
  margin: 0;
}

.trend-chart .tick {
  font-size: 12px;
  margin-right: 5px;
  width: 40px;
  text-align: right;
}

.trend-chart .x-axis .tick {
  margin-right: 0;
  margin-top: 5px;
  text-align: center;
  width: auto;
  transform: translateX(-50%);
}

.trend-chart .chart-data {
  position: absolute;
  top: 20px;
  left: 80px;
  width: calc(100% - 80px);
  height: 260px;
  border-left: 1px solid #dcdfe6;
  border-bottom: 1px solid #dcdfe6;
}

.trend-chart .history-data, .trend-chart .forecast-data {
  position: relative;
  width: 100%;
  height: 100%;
}

.trend-chart .data-point {
  position: absolute;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  transform: translate(-50%, -50%);
}

.trend-chart .history-data .data-point {
  background-color: #409eff;
  box-shadow: 0 0 0 2px white, 0 0 4px rgba(64, 158, 255, 0.5);
}

.trend-chart .forecast-data .data-point {
  background-color: #67c23a;
  box-shadow: 0 0 0 2px white, 0 0 4px rgba(103, 194, 58, 0.5);
}

/* 置信区间 */
.trend-chart .confidence-interval {
  position: absolute;
  top: 0;
  left: 50%;
  width: 50%;
  height: 100%;
  background: linear-gradient(to bottom, rgba(103, 194, 58, 0.1) 0%, rgba(103, 194, 58, 0.1) 100%);
  border-left: 1px dashed #67c23a;
}

/* Legend */
.legend {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
  justify-content: center;
  align-items: center;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 14px;
}

.legend-line {
  width: 20px;
  height: 2px;
}

.legend-line.history-line {
  background-color: #409eff;
}

.legend-line.forecast-line {
  background-color: #67c23a;
}

.legend-area {
  width: 20px;
  height: 12px;
  background-color: rgba(103, 194, 58, 0.1);
  border: 1px dashed #67c23a;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .trend-chart .tick {
    font-size: 10px;
  }
}

@media (max-width: 768px) {
  .trend-forecast-view {
    padding: 12px;
  }
  
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .chart-container {
    padding: 12px;
  }
  
  .chart-wrapper {
    height: 300px;
  }
  
  .trend-chart .tick {
    font-size: 8px;
  }
  
  .legend {
    gap: 10px;
  }
  
  .legend-item {
    font-size: 12px;
  }
  
  .el-row {
    flex-direction: column;
  }
  
  .el-col {
    width: 100% !important;
  }
}
</style>
