<template>
  <div class="statistical-analysis-view">
    <div class="page-header">
      <h3>质量统计分析</h3>
      <el-button type="primary" @click="generateReport">
        <el-icon><Document /></el-icon>生成报告
      </el-button>
    </div>

    <!-- 统计分析类型选择 -->
    <el-card class="mb-4" shadow="hover">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="Pareto 柏拉图" name="pareto">
          <div class="chart-container">
            <el-empty v-if="loading" description="加载中..." />
            <div v-else>
              <el-alert type="info" :closable="false" show-icon>
                <template #title>柏拉图数据（来自后端统计接口）</template>
              </el-alert>
              <pre class="raw-json">{{ rawJson }}</pre>
            </div>
          </div>
        </el-tab-pane>
        <el-tab-pane label="SPC 控制图" name="spc">
          <div class="chart-container">
            <el-empty v-if="loading" description="加载中..." />
            <div v-else>
              <el-alert type="info" :closable="false" show-icon>
                <template #title>SPC 数据（来自后端统计接口）</template>
              </el-alert>
              <pre class="raw-json">{{ rawJson }}</pre>
            </div>
          </div>
        </el-tab-pane>
        <el-tab-pane label="合格率统计" name="pass-rate">
          <div class="chart-container">
            <el-empty v-if="loading" description="加载中..." />
            <div v-else>
              <el-alert type="info" :closable="false" show-icon>
                <template #title>合格率数据（来自后端统计接口）</template>
              </el-alert>
              <pre class="raw-json">{{ rawJson }}</pre>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 统计数据表格 -->
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>统计数据详情</span>
          <el-tag size="small" type="info">{{ total }} 条记录</el-tag>
        </div>
      </template>
      <el-table :data="statisticalData" border style="width: 100%">
        <el-table-column prop="category" label="类别" min-width="120" />
        <el-table-column prop="subCategory" label="子类别" min-width="120" />
        <el-table-column prop="count" label="数量" min-width="100" />
        <el-table-column prop="percentage" label="百分比" min-width="100">
          <template #default="scope">
            <el-progress :percentage="scope.row.percentage" :format="percentageFormat" :show-text="true" />
          </template>
        </el-table-column>
        <el-table-column prop="unit" label="单位" min-width="80" />
        <el-table-column prop="period" label="统计周期" min-width="120" />
        <el-table-column prop="remark" label="备注" min-width="200" show-overflow-tooltip />
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Document } from '@element-plus/icons-vue'
import { unwrapResponseData } from '@/api'
import { StatisticalAnalysisAPI } from '@/api/qms'

// 统计数据类型
interface StatisticalData {
  id: string | number
  category: string
  subCategory: string
  count: number
  percentage: number
  unit: string
  period: string
  remark: string
}

// 当前激活的标签页
const activeTab = ref('pareto')

// 统计数据列表
const statisticalData = ref<StatisticalData[]>([])
const total = ref(0)
const loading = ref(false)
const rawResult = ref<any>(null)
const rawJson = ref('')

const formatDateTime = (date: Date) => {
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}

const getTimeRange = () => {
  const end = new Date()
  const start = new Date(end.getTime() - 30 * 24 * 60 * 60 * 1000)
  return { startTime: formatDateTime(start), endTime: formatDateTime(end) }
}

const loadTabData = async () => {
  loading.value = true
  try {
    const { startTime, endTime } = getTimeRange()
    if (activeTab.value === 'pareto') {
      const res = await StatisticalAnalysisAPI.getParetoData({
        category: 'defectType',
        startTime,
        endTime
      })
      rawResult.value = unwrapResponseData<any>(res) || {}
      rawJson.value = JSON.stringify(rawResult.value, null, 2)
      const items = Array.isArray(rawResult.value?.items) ? rawResult.value.items : []
      statisticalData.value = items.map((it: any, index: number) => ({
        id: index + 1,
        category: '质量问题',
        subCategory: String(it?.name ?? ''),
        count: Number(it?.count ?? 0),
        percentage: Number(it?.percent ?? 0),
        unit: '个',
        period: `${startTime.slice(0, 10)}~${endTime.slice(0, 10)}`,
        remark: `累计 ${Number(it?.cumulativePercent ?? 0)}%`
      }))
      total.value = statisticalData.value.length
      return
    }

    if (activeTab.value === 'spc') {
      const res = await StatisticalAnalysisAPI.getSPCData({
        chartType: 'xbar-r',
        productId: '0',
        parameter: 'value',
        startTime,
        endTime,
        sampleSize: 10
      })
      rawResult.value = unwrapResponseData<any>(res) || {}
      rawJson.value = JSON.stringify(rawResult.value, null, 2)
      const points = Array.isArray(rawResult.value?.points) ? rawResult.value.points : []
      statisticalData.value = points.map((v: any, index: number) => ({
        id: index + 1,
        category: 'SPC',
        subCategory: `样本${index + 1}`,
        count: Number(v ?? 0),
        percentage: 0,
        unit: '',
        period: `${startTime.slice(0, 10)}~${endTime.slice(0, 10)}`,
        remark: ''
      }))
      total.value = statisticalData.value.length
      return
    }

    const res = await StatisticalAnalysisAPI.getPassRateData({
      startTime,
      endTime,
      groupBy: 'month'
    })
    rawResult.value = unwrapResponseData<any>(res) || {}
    rawJson.value = JSON.stringify(rawResult.value, null, 2)
    const series = Array.isArray(rawResult.value?.series) ? rawResult.value.series : []
    statisticalData.value = series.map((it: any, index: number) => ({
      id: index + 1,
      category: '合格率',
      subCategory: String(it?.label ?? ''),
      count: Number(it?.value ?? 0),
      percentage: Number(it?.value ?? 0),
      unit: '%',
      period: `${startTime.slice(0, 10)}~${endTime.slice(0, 10)}`,
      remark: `合格 ${rawResult.value?.qualified ?? 0} / 总数 ${rawResult.value?.totalCount ?? 0}`
    }))
    total.value = statisticalData.value.length
  } catch (e: any) {
    statisticalData.value = []
    total.value = 0
    rawResult.value = null
    rawJson.value = ''
    ElMessage.error(e?.message || '加载统计数据失败')
  } finally {
    loading.value = false
  }
}

// 百分比格式化函数
const percentageFormat = (percentage: number) => {
  return `${percentage}%`
}

// 标签页切换处理
const handleTabChange = (tab: string) => {
  activeTab.value = tab
  loadTabData()
}

// 生成报告
const generateReport = () => {
  ElMessage.success('报告生成成功')
}

onMounted(loadTabData)
</script>

<style scoped>
.statistical-analysis-view {
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

.chart-title {
  text-align: center;
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 20px;
}

.raw-json {
  margin-top: 12px;
  padding: 12px;
  background: #111827;
  color: #e5e7eb;
  border-radius: 8px;
  overflow: auto;
  max-height: 320px;
  font-size: 12px;
}

.chart-content {
  height: 300px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}

.chart-footer {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

/* Pareto Chart */
.pareto-chart .chart-content {
  position: relative;
  flex-direction: row;
  justify-content: space-between;
}

.pareto-chart .bar-chart {
  width: 70%;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: space-around;
}

.pareto-chart .bar-item {
  display: flex;
  align-items: center;
  margin: 10px 0;
}

.pareto-chart .bar-label {
  width: 100px;
  text-align: right;
  margin-right: 10px;
  font-size: 14px;
}

.pareto-chart .bar {
  height: 30px;
  border-radius: 4px;
  transition: width 0.3s ease;
}

.pareto-chart .bar-value {
  margin-left: 10px;
  font-weight: bold;
  width: 40px;
  text-align: left;
}

.pareto-chart .line-chart {
  width: 30%;
  height: 100%;
  position: relative;
}

.pareto-chart .line-title {
  text-align: center;
  margin-bottom: 10px;
  font-size: 14px;
  font-weight: bold;
}

.pareto-chart .line-content {
  position: relative;
  height: 280px;
  width: 100%;
}

.pareto-chart .line-point {
  position: absolute;
  width: 10px;
  height: 10px;
  background-color: #409eff;
  border-radius: 50%;
  border: 2px solid white;
  box-shadow: 0 0 0 2px #409eff;
}

/* SPC Chart */
.spc-chart .chart-content {
  display: flex;
  justify-content: center;
  align-items: center;
}

.spc-chart .spc-axis {
  position: relative;
  width: 100%;
  height: 300px;
  display: flex;
}

.spc-chart .spc-y-axis {
  width: 60px;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.spc-chart .spc-x-axis {
  flex: 1;
  height: 100%;
  position: relative;
  margin-left: 20px;
}

.spc-chart .axis-label {
  writing-mode: vertical-rl;
  text-orientation: mixed;
  font-size: 12px;
  margin: 5px 0;
}

.spc-chart .axis-ticks {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  align-items: flex-end;
  height: 260px;
  margin-top: 20px;
}

.spc-chart .spc-x-axis .axis-ticks {
  flex-direction: row;
  align-items: flex-start;
  height: auto;
  margin-top: 280px;
  justify-content: space-between;
}

.spc-chart .tick {
  font-size: 12px;
  margin-right: 5px;
  width: 20px;
  text-align: right;
}

.spc-chart .spc-x-axis .tick {
  margin-right: 0;
  margin-top: 5px;
  text-align: center;
  width: auto;
}

.spc-chart .spc-data {
  position: absolute;
  top: 20px;
  left: 80px;
  width: calc(100% - 80px);
  height: 260px;
  border-left: 1px solid #dcdfe6;
  border-bottom: 1px solid #dcdfe6;
}

.spc-chart .control-line {
  position: absolute;
  width: 100%;
  height: 1px;
  background-color: #909399;
  opacity: 0.5;
}

.spc-chart .center-line {
  top: 50%;
  background-color: #67c23a;
  opacity: 0.8;
}

.spc-chart .upper-line {
  top: 25%;
  background-color: #f56c6c;
  opacity: 0.8;
}

.spc-chart .lower-line {
  top: 75%;
  background-color: #f56c6c;
  opacity: 0.8;
}

.spc-chart .data-point {
  position: absolute;
  width: 8px;
  height: 8px;
  background-color: #409eff;
  border-radius: 50%;
  transform: translate(-50%, -50%);
  box-shadow: 0 0 0 2px white, 0 0 4px rgba(64, 158, 255, 0.5);
}

/* Pass Rate Chart */
.pass-rate-chart .chart-content {
  display: flex;
  justify-content: center;
  align-items: center;
}

.pass-rate-chart .bar-chart {
  display: flex;
  align-items: flex-end;
  justify-content: space-around;
  width: 100%;
  height: 100%;
  max-width: 600px;
}

.pass-rate-chart .bar-item.horizontal {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin: 0 15px;
}

.pass-rate-chart .bar-item.horizontal .bar-value {
  margin-bottom: 5px;
  font-weight: bold;
  font-size: 14px;
}

.pass-rate-chart .bar-item.horizontal .bar {
  width: 60px;
  border-radius: 4px 4px 0 0;
  transition: height 0.3s ease;
}

.pass-rate-chart .bar-item.horizontal .bar-label {
  margin-top: 5px;
  font-size: 14px;
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

.legend-color {
  width: 16px;
  height: 16px;
  border-radius: 4px;
}

.legend-line {
  width: 20px;
  height: 2px;
}

.legend-line.center-line {
  background-color: #67c23a;
}

.legend-line.upper-line, .legend-line.lower-line {
  background-color: #f56c6c;
}

/* Chart Summary */
.chart-summary {
  text-align: center;
  font-size: 14px;
}

.chart-summary p {
  margin: 5px 0;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .pareto-chart .chart-content {
    flex-direction: column;
    height: auto;
  }
  
  .pareto-chart .bar-chart {
    width: 100%;
    height: 300px;
  }
  
  .pareto-chart .line-chart {
    width: 100%;
    height: 200px;
    margin-top: 20px;
  }
  
  .pareto-chart .line-content {
    height: 180px;
  }
}

@media (max-width: 768px) {
  .statistical-analysis-view {
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
  
  .pareto-chart .bar-label {
    width: 80px;
    font-size: 12px;
  }
  
  .pareto-chart .bar {
    height: 25px;
  }
  
  .spc-chart .axis-label {
    font-size: 10px;
  }
  
  .spc-chart .tick {
    font-size: 10px;
  }
  
  .pass-rate-chart .bar-item.horizontal .bar {
    width: 40px;
  }
  
  .pass-rate-chart .bar-item.horizontal {
    margin: 0 8px;
  }
  
  .pass-rate-chart .bar-item.horizontal .bar-label {
    font-size: 12px;
  }
  
  .legend {
    gap: 10px;
  }
  
  .legend-item {
    font-size: 12px;
  }
}
</style>
