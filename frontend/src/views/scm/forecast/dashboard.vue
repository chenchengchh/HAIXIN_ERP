<template>
  <div class="forecast-dashboard">
    <el-card shadow="hover" class="mb-4">
      <template #header>
        <div class="card-header">
          <h3>预测概览</h3>
        </div>
      </template>
      <div class="dashboard-content">
        <!-- 关键指标卡片 -->
        <div class="metric-cards">
          <el-card shadow="hover" class="metric-card">
            <div class="metric-content">
              <div class="metric-title">预测准确率</div>
              <div class="metric-value">{{ overview.accuracy }}%</div>
              <div class="metric-change">版本：{{ overview.versionNo ?? '-' }}</div>
            </div>
          </el-card>
          <el-card shadow="hover" class="metric-card">
            <div class="metric-content">
              <div class="metric-title">预测偏差</div>
              <div class="metric-value">{{ overview.bias }}%</div>
              <div class="metric-change">周期：{{ overview.period }}</div>
            </div>
          </el-card>
          <el-card shadow="hover" class="metric-card">
            <div class="metric-content">
              <div class="metric-title">预测周期</div>
              <div class="metric-value">{{ overview.horizon }}</div>
              <div class="metric-change">--</div>
            </div>
          </el-card>
          <el-card shadow="hover" class="metric-card">
            <div class="metric-content">
              <div class="metric-title">产品覆盖率</div>
              <div class="metric-value">{{ overview.productCount }}</div>
              <div class="metric-change">条</div>
            </div>
          </el-card>
        </div>

        <!-- 预测趋势图 -->
        <div class="chart-section">
          <el-card shadow="hover">
            <template #header>
              <div class="chart-header">
                <h4>预测趋势分析</h4>
                <el-select v-model="timePeriod" placeholder="选择时间周期" size="small">
                  <el-option label="月度" value="month" />
                  <el-option label="季度" value="quarter" />
                  <el-option label="年度" value="year" />
                </el-select>
              </div>
            </template>
            <div class="chart-container">
              <div id="forecast-trend-chart" style="width: 100%; height: 400px;"></div>
            </div>
          </el-card>
        </div>

        <!-- 预测状态统计 -->
        <div class="stats-section">
          <el-card shadow="hover" class="stats-card">
            <template #header>
              <h4>预测状态统计</h4>
            </template>
            <div class="stats-content">
              <div id="forecast-status-chart" style="width: 100%; height: 300px;"></div>
            </div>
          </el-card>
          <el-card shadow="hover" class="stats-card">
            <template #header>
              <h4>产品类别预测</h4>
            </template>
            <div class="stats-content">
              <div id="product-category-chart" style="width: 100%; height: 300px;"></div>
            </div>
          </el-card>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import { forecastApi } from '@/api/scm'

// 时间周期选择
const timePeriod = ref('month')

const overview = ref<any>({
  period: '',
  versionNo: null,
  accuracy: '0.0',
  bias: '0.0',
  horizon: '12个月',
  productCount: 0
})

const getCurrentPeriod = () => {
  const now = new Date()
  return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`
}

const loadOverview = async () => {
  const period = getCurrentPeriod()
  overview.value.period = period
  try {
    const versionsRes: any = await forecastApi.getForecastVersions(period)
    const versions = versionsRes?.data?.list || versionsRes?.data?.records || []
    const published = (Array.isArray(versions) ? versions : []).find((v: any) => v.status === 'PUBLISHED')
    const versionId = published ? published.id : versions?.[0]?.id
    overview.value.versionNo = published ? published.versionNo : versions?.[0]?.versionNo
    const accRes: any = await forecastApi.getForecastAccuracy(period, versionId)
    const acc = accRes?.data || {}
    if (acc?.mape !== null && acc?.mape !== undefined) {
      overview.value.accuracy = ((1 - Number(acc.mape)) * 100).toFixed(1)
    }
    if (acc?.avgBias !== null && acc?.avgBias !== undefined) {
      overview.value.bias = (Number(acc.avgBias) * 100).toFixed(1)
    }
    overview.value.productCount = (acc?.items || []).length
  } catch (e) {
  }
}

// 图表实例
let trendChart: echarts.ECharts | null = null
let statusChart: echarts.ECharts | null = null
let categoryChart: echarts.ECharts | null = null

const loadDashboardCharts = async (period: string) => {
  try {
    const [trend, status, category] = await Promise.all([
      forecastApi.getForecastDashboardTrend(period, 12),
      forecastApi.getForecastDashboardStatusSummary(period),
      forecastApi.getForecastDashboardCategorySummary(period, undefined, 5)
    ])

    const trendData = trend?.data || {}
    const x = trendData?.xAxis || []
    const actual = trendData?.actual || []
    const forecast = trendData?.forecast || []
    trendChart?.setOption({
      xAxis: { data: x },
      series: [{ data: actual }, { data: forecast }]
    })

    const statusDataObj = status?.data || {}
    const counts = statusDataObj?.counts || {}
    const statusData = [
      { value: counts.PUBLISHED || 0, name: '当前版本' },
      { value: counts.DRAFT || 0, name: '草稿' },
      { value: counts.ARCHIVED || 0, name: '已发布' }
    ]
    statusChart?.setOption({
      series: [{ data: statusData }]
    })

    const categoryDataObj = category?.data || {}
    const items = categoryDataObj?.items || []
    categoryChart?.setOption({
      yAxis: { data: items.map((i: any) => i.name) },
      series: [{ data: items.map((i: any) => i.value) }]
    })
  } catch (e) {
  }
}
// 初始化图表
const initCharts = () => {
  // 检查DOM尺寸并初始化图表的函数
  const checkAndInit = () => {
    // 检查所有图表容器是否都有有效的尺寸
    const trendChartDom = document.getElementById('forecast-trend-chart')
    const statusChartDom = document.getElementById('forecast-status-chart')
    const categoryChartDom = document.getElementById('product-category-chart')
    
    if (trendChartDom && trendChartDom.clientWidth > 0 && trendChartDom.clientHeight > 0 &&
        statusChartDom && statusChartDom.clientWidth > 0 && statusChartDom.clientHeight > 0 &&
        categoryChartDom && categoryChartDom.clientWidth > 0 && categoryChartDom.clientHeight > 0) {
      
      // 预测趋势图
      trendChart = echarts.init(trendChartDom)
      const trendOption = {
        title: {
          text: ''
        },
        tooltip: {
          trigger: 'axis'
        },
        legend: {
          data: ['实际销量', '预测销量']
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
        yAxis: {
          type: 'value'
        },
        series: [
          {
            name: '实际销量',
            type: 'line',
            data: []
          },
          {
            name: '预测销量',
            type: 'line',
            data: []
          }
        ]
      }
      trendChart.setOption(trendOption)

      // 预测状态统计
      statusChart = echarts.init(statusChartDom)
      const statusOption = {
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
            name: '预测状态',
            type: 'pie',
            radius: '50%',
            data: [],
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
      statusChart.setOption(statusOption)

      // 产品类别预测
      categoryChart = echarts.init(categoryChartDom)
      const categoryOption = {
        title: {
          text: ''
        },
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow'
          }
        },
        legend: {
          data: ['预测销量']
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'value'
        },
        yAxis: {
          type: 'category',
          data: []
        },
        series: [
          {
            name: '预测销量',
            type: 'bar',
            data: []
          }
        ]
      }
      categoryChart.setOption(categoryOption)
      loadDashboardCharts(getCurrentPeriod())
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
  statusChart?.resize()
  categoryChart?.resize()
}

// 组件挂载时初始化图表
onMounted(() => {
  loadOverview()
  initCharts()
  window.addEventListener('resize', handleResize, { passive: true })
})

// 组件卸载时清理资源
onUnmounted(() => {
  // 移除窗口大小变化监听器
  window.removeEventListener('resize', handleResize)
  
  // 销毁图表实例，自动移除所有事件监听器
  trendChart?.dispose()
  statusChart?.dispose()
  categoryChart?.dispose()
  
  // 重置图表实例引用
  trendChart = null
  statusChart = null
  categoryChart = null
})
</script>

<style scoped>
.forecast-dashboard {
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

.dashboard-content {
  padding: 10px 0;
}

/* 关键指标卡片 */
.metric-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.metric-card {
  cursor: pointer;
  transition: all 0.3s ease;
}

.metric-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
}

.metric-content {
  text-align: center;
  padding: 20px 0;
}

.metric-title {
  font-size: 14px;
  color: #606266;
  margin-bottom: 10px;
}

.metric-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 5px;
}

.metric-change {
  font-size: 12px;
}

.metric-change.positive {
  color: #67c23a;
}

.metric-change.negative {
  color: #f56c6c;
}

/* 图表区域 */
.chart-section {
  margin-bottom: 20px;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-container {
  margin-top: 20px;
}

/* 统计区域 */
.stats-section {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: 20px;
}

.stats-card {
  height: 100%;
}

.stats-content {
  margin-top: 20px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .forecast-dashboard {
    padding: 0 10px 10px;
  }

  .metric-cards {
    grid-template-columns: 1fr;
    gap: 10px;
  }

  .stats-section {
    grid-template-columns: 1fr;
    gap: 10px;
  }

  .stats-card {
    min-height: 300px;
  }
}
</style>
