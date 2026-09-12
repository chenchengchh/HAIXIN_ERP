<template>
  <div class="forecast-accuracy">
    <el-card shadow="hover" class="mb-4">
      <template #header>
        <div class="card-header">
          <h3>预测准确率分析</h3>
          <div class="header-actions">
            <el-select v-model="timeRange" placeholder="时间范围" size="small">
              <el-option label="近3个月" value="3month" />
              <el-option label="近6个月" value="6month" />
              <el-option label="近12个月" value="12month" />
              <el-option label="自定义" value="custom" />
            </el-select>
            <el-button type="primary" size="small" @click="exportReport">
              <el-icon><Download /></el-icon> 导出报告
            </el-button>
          </div>
        </div>
      </template>
      
      <!-- 准确率趋势图 -->
      <div class="chart-section mb-4">
        <el-card shadow="hover">
          <template #header>
            <h4>准确率趋势</h4>
          </template>
          <div class="chart-container">
            <div id="accuracy-trend-chart" style="width: 100%; height: 400px;"></div>
          </div>
        </el-card>
      </div>

      <!-- 多维度分析 -->
      <div class="analysis-section">
        <el-row :gutter="20">
          <!-- 产品类别分析 -->
          <el-col :span="12">
            <el-card shadow="hover" class="mb-4">
              <template #header>
                <h4>产品类别准确率</h4>
              </template>
              <div class="chart-container">
                <div id="category-accuracy-chart" style="width: 100%; height: 300px;"></div>
              </div>
            </el-card>
          </el-col>
          
          <!-- 区域分析 -->
          <el-col :span="12">
            <el-card shadow="hover" class="mb-4">
              <template #header>
                <h4>区域准确率</h4>
              </template>
              <div class="chart-container">
                <div id="region-accuracy-chart" style="width: 100%; height: 300px;"></div>
              </div>
            </el-card>
          </el-col>
          
          <!-- 产品准确率排名 -->
          <el-col :span="12">
            <el-card shadow="hover" class="mb-4">
              <template #header>
                <h4>产品准确率排名</h4>
              </template>
              <div class="table-container">
                <el-table
                  :data="productAccuracyData"
                  border
                  style="width: 100%"
                  :default-sort="{ prop: 'accuracy', order: 'descending' }"
                >
                  <el-table-column type="index" label="排名" width="60" />
                  <el-table-column prop="productName" label="产品名称" width="150" />
                  <el-table-column prop="productCode" label="产品编码" width="120" />
                  <el-table-column prop="accuracy" label="准确率" width="100" align="right">
                    <template #default="scope">
                      <div class="accuracy-value">
                        <span :class="scope.row.accuracy >= 80 ? 'high-accuracy' : scope.row.accuracy >= 60 ? 'medium-accuracy' : 'low-accuracy'">
                          {{ scope.row.accuracy }}%
                        </span>
                      </div>
                    </template>
                  </el-table-column>
                  <el-table-column prop="bias" label="偏差" width="100" align="right">
                    <template #default="scope">
                      <span :class="scope.row.bias >= 0 ? 'positive-bias' : 'negative-bias'">
                        {{ scope.row.bias >= 0 ? '+' : '' }}{{ scope.row.bias }}%
                      </span>
                    </template>
                  </el-table-column>
                  <el-table-column prop="sampleSize" label="样本量" width="100" align="right" />
                </el-table>
              </div>
            </el-card>
          </el-col>
          
          <!-- 准确率统计 -->
          <el-col :span="12">
            <el-card shadow="hover" class="mb-4">
              <template #header>
                <h4>准确率统计</h4>
              </template>
              <div class="stats-container">
                <div class="stat-item">
                  <div class="stat-label">平均准确率</div>
                  <div class="stat-value">{{ averageAccuracy }}%</div>
                </div>
                <div class="stat-item">
                  <div class="stat-label">高准确率产品数</div>
                  <div class="stat-value">{{ highAccuracyCount }} (≥80%)</div>
                </div>
                <div class="stat-item">
                  <div class="stat-label">中等准确率产品数</div>
                  <div class="stat-value">{{ mediumAccuracyCount }} (60%-80%)</div>
                </div>
                <div class="stat-item">
                  <div class="stat-label">低准确率产品数</div>
                  <div class="stat-value">{{ lowAccuracyCount }} (<60%)</div>
                </div>
                <div class="stat-item">
                  <div class="stat-label">总产品数</div>
                  <div class="stat-value">{{ totalProductCount }}</div>
                </div>
                <div class="stat-item">
                  <div class="stat-label">平均偏差</div>
                  <div class="stat-value">{{ averageBias }}%</div>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { Download } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { forecastApi } from '@/api/scm'

// 时间范围
const timeRange = ref('6month')

const getCurrentPeriod = () => {
  const now = new Date()
  return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`
}

const currentPeriod = ref(getCurrentPeriod())
const currentVersionId = ref<number | undefined>(undefined)

// 图表实例
let trendChart: echarts.ECharts | null = null
let categoryChart: echarts.ECharts | null = null
let regionChart: echarts.ECharts | null = null

const monthAdd = (period: string, delta: number) => {
  const parts = period.split('-')
  const y = Number(parts[0]) || new Date().getFullYear()
  const m = Number(parts[1]) || 1
  const d = new Date(y, m - 1, 1)
  d.setMonth(d.getMonth() + delta)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`
}

const loadChartSeries = async () => {
  const period = currentPeriod.value
  try {
    const start = monthAdd(period, -11)
    const end = period
    const trendRes: any = await forecastApi.getForecastAccuracyTrend(start, end, currentVersionId.value)
    const trend = trendRes?.data?.list || trendRes?.data?.records || []
    const x = (Array.isArray(trend) ? trend : []).map((r: any) => r.period)
    const acc = (Array.isArray(trend) ? trend : []).map((r: any) => {
      const mape = r.mape === null || r.mape === undefined ? null : Number(r.mape)
      return mape === null ? null : Number(((1 - mape) * 100).toFixed(1))
    })
    const bias = (Array.isArray(trend) ? trend : []).map((r: any) => {
      const b = r.avgBias === null || r.avgBias === undefined ? null : Number(r.avgBias)
      return b === null ? null : Number((b * 100).toFixed(1))
    })
    trendChart?.setOption({
      xAxis: { data: x },
      series: [{ data: acc }, { data: bias }]
    })
  } catch (e) {
  }

  try {
    const items = productAccuracyData.value.slice().sort((a, b) => b.accuracy - a.accuracy).slice(0, 5)
    categoryChart?.setOption({
      series: [{
        data: items.map((i: any) => ({ value: i.accuracy, name: i.productName }))
      }]
    })

    const byRegion: Record<string, { sum: number, count: number }> = {}
    for (const i of productAccuracyData.value) {
      const key = i.region || 'ALL'
      byRegion[key] = byRegion[key] || { sum: 0, count: 0 }
      byRegion[key].sum += Number(i.accuracy || 0)
      byRegion[key].count += 1
    }
    const regions = Object.keys(byRegion)
    const vals = regions.map((k) => {
      const v = byRegion[k] || { sum: 0, count: 0 }
      return Number((v.sum / Math.max(1, v.count)).toFixed(1))
    })
    regionChart?.setOption({
      yAxis: { data: regions },
      series: [{ data: vals }]
    })
  } catch (e) {
  }
}

// 产品准确率数据
const productAccuracyData = ref<any[]>([])

const loadAccuracy = async () => {
  try {
    const versionsRes: any = await forecastApi.getForecastVersions(currentPeriod.value)
    const versions = versionsRes?.data?.list || versionsRes?.data?.records || []
    const published = (Array.isArray(versions) ? versions : []).find((v: any) => v.status === 'PUBLISHED')
    currentVersionId.value = published ? published.id : versions?.[0]?.id
    const res: any = await forecastApi.getForecastAccuracy(currentPeriod.value, currentVersionId.value)
    const items = res?.data?.items || []
    productAccuracyData.value = items.map((it: any) => {
      const ape = it.ape === null || it.ape === undefined ? null : Number(it.ape)
      const bias = it.bias === null || it.bias === undefined ? null : Number(it.bias)
      return {
        productName: it.productName,
        productCode: it.productCode,
        region: it.region,
        accuracy: ape === null ? 0 : Math.max(0, Math.min(100, (1 - ape) * 100)),
        bias: bias === null ? 0 : bias * 100,
        sampleSize: it.actual && Number(it.actual) > 0 ? 1 : 0
      }
    })
  } catch (e) {
    productAccuracyData.value = []
  }
}

// 计算属性：平均准确率
const averageAccuracy = computed(() => {
  if (productAccuracyData.value.length === 0) return 0
  const total = productAccuracyData.value.reduce((sum, item) => sum + item.accuracy, 0)
  return (total / productAccuracyData.value.length).toFixed(1)
})

// 计算属性：平均偏差
const averageBias = computed(() => {
  if (productAccuracyData.value.length === 0) return 0
  const total = productAccuracyData.value.reduce((sum, item) => sum + item.bias, 0)
  return (total / productAccuracyData.value.length).toFixed(1)
})

// 计算属性：高准确率产品数
const highAccuracyCount = computed(() => {
  return productAccuracyData.value.filter(item => item.accuracy >= 80).length
})

// 计算属性：中等准确率产品数
const mediumAccuracyCount = computed(() => {
  return productAccuracyData.value.filter(item => item.accuracy >= 60 && item.accuracy < 80).length
})

// 计算属性：低准确率产品数
const lowAccuracyCount = computed(() => {
  return productAccuracyData.value.filter(item => item.accuracy < 60).length
})

// 计算属性：总产品数
const totalProductCount = computed(() => {
  return productAccuracyData.value.length
})

// 初始化图表
const initCharts = () => {
  // 检查DOM尺寸并初始化图表的函数
  const checkAndInit = () => {
    // 检查所有图表容器是否都有有效的尺寸
    const trendChartDom = document.getElementById('accuracy-trend-chart')
    const categoryChartDom = document.getElementById('category-accuracy-chart')
    const regionChartDom = document.getElementById('region-accuracy-chart')
    
    if (trendChartDom && trendChartDom.clientWidth > 0 && trendChartDom.clientHeight > 0 &&
        categoryChartDom && categoryChartDom.clientWidth > 0 && categoryChartDom.clientHeight > 0 &&
        regionChartDom && regionChartDom.clientWidth > 0 && regionChartDom.clientHeight > 0) {
      
      // 准确率趋势图
      if (trendChart) {
        trendChart.dispose()
      }
      trendChart = echarts.init(trendChartDom)
      const trendOption = {
        title: {
          text: ''
        },
        tooltip: {
          trigger: 'axis'
        },
        legend: {
          data: ['准确率', '偏差']
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
          data: []
        },
        yAxis: [
          {
            type: 'value',
            name: '准确率',
            min: 0,
            max: 100,
            axisLabel: {
              formatter: '{value}%'
            }
          },
          {
            type: 'value',
            name: '偏差',
            min: -10,
            max: 10,
            axisLabel: {
              formatter: '{value}%'
            }
          }
        ],
        series: [
          {
            name: '准确率',
            type: 'line',
            data: []
          },
          {
            name: '偏差',
            type: 'line',
            yAxisIndex: 1,
            data: []
          }
        ]
      }
      trendChart.setOption(trendOption)

      // 产品类别准确率
      if (categoryChart) {
        categoryChart.dispose()
      }
      categoryChart = echarts.init(categoryChartDom)
      const categoryOption = {
        title: {
          text: ''
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
            name: '产品类别准确率',
            type: 'pie',
            radius: '50%',
            data: [],
            emphasis: {
              itemStyle: {
                shadowBlur: 10,
                shadowOffsetX: 0,
                shadowColor: 'rgba(0, 0, 0, 0.5)'
              }
            },
            label: {
              formatter: '{b}: {c}%'
            }
          }
        ]
      }
      categoryChart.setOption(categoryOption)

      // 区域准确率
      if (regionChart) {
        regionChart.dispose()
      }
      regionChart = echarts.init(regionChartDom)
      const regionOption = {
        title: {
          text: ''
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
        xAxis: {
          type: 'value',
          name: '准确率',
          min: 0,
          max: 100,
          axisLabel: {
            formatter: '{value}%'
          }
        },
        yAxis: {
          type: 'category',
          data: []
        },
        series: [
          {
            name: '区域准确率',
            type: 'bar',
            data: [],
            itemStyle: {
              color: function(params: any) {
                const colorList = ['#5470c6', '#91cc75', '#fac858', '#ee6666', '#73c0de', '#3ba272', '#fc8452']
                return colorList[params.dataIndex]
              }
            }
          }
        ]
      }
      regionChart.setOption(regionOption)
      loadChartSeries()
    } else {
      // DOM尺寸为0，延迟100ms后重试
      setTimeout(checkAndInit, 100)
    }
  }
  
  // 开始检查和初始化
  checkAndInit()
}

// 监听窗口大小变化，调整图表大小
const handleResize = () => {
  trendChart?.resize()
  categoryChart?.resize()
  regionChart?.resize()
}

// 组件挂载时初始化图表
onMounted(() => {
  loadAccuracy().finally(() => {
    initCharts()
  })
  window.addEventListener('resize', handleResize, { passive: true })
})

// 组件卸载时清理资源
onUnmounted(() => {
  // 移除窗口大小变化监听器
  window.removeEventListener('resize', handleResize)
  
  // 销毁图表实例
  trendChart?.dispose()
  categoryChart?.dispose()
  regionChart?.dispose()
  
  // 重置图表实例引用
  trendChart = null
  categoryChart = null
  regionChart = null
})

// 导出报告
const exportReport = () => {
  const rows = productAccuracyData.value || []
  const header = ['productCode', 'productName', 'region', 'accuracy', 'bias']
  const lines = [header.join(',')]
  for (const r of rows) {
    lines.push([
      r.productCode ?? '',
      `"${String(r.productName ?? '').replace(/\"/g, '\"\"')}"`,
      r.region ?? '',
      Number(r.accuracy ?? 0).toFixed(2),
      Number(r.bias ?? 0).toFixed(2)
    ].join(','))
  }
  const blob = new Blob([lines.join('\n')], { type: 'text/csv;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `forecast-accuracy-${currentPeriod.value}.csv`
  a.click()
  URL.revokeObjectURL(url)
  ElMessage.success('报告已导出')
}
</script>

<style scoped>
.forecast-accuracy {
  padding: 0 20px 20px;
}

.mb-4 {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  gap: 10px;
  align-items: center;
}

/* 图表区域 */
.chart-section {
  margin-bottom: 20px;
}

.chart-container {
  margin-top: 20px;
}

/* 分析区域 */
.analysis-section {
  margin-top: 20px;
}

/* 表格容器 */
.table-container {
  max-height: 400px;
  overflow-y: auto;
}

/* 准确率值样式 */
.accuracy-value {
  display: flex;
  justify-content: flex-end;
}

.high-accuracy {
  color: #67c23a;
  font-weight: bold;
}

.medium-accuracy {
  color: #e6a23c;
  font-weight: bold;
}

.low-accuracy {
  color: #f56c6c;
  font-weight: bold;
}

/* 偏差样式 */
.positive-bias {
  color: #f56c6c;
}

.negative-bias {
  color: #67c23a;
}

/* 统计容器 */
.stats-container {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 20px;
  padding: 20px 0;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stat-label {
  font-size: 14px;
  color: #606266;
  margin-bottom: 5px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .stats-container {
    grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
    gap: 15px;
  }
  
  .stat-value {
    font-size: 20px;
  }
}

@media (max-width: 768px) {
  .forecast-accuracy {
    padding: 0 10px 10px;
  }
  
  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .header-actions {
    flex-wrap: wrap;
    width: 100%;
  }
  
  .stats-container {
    grid-template-columns: repeat(2, 1fr);
    gap: 10px;
  }
  
  .stat-item {
    min-width: 100px;
  }
  
  .stat-value {
    font-size: 18px;
  }
}
</style>
