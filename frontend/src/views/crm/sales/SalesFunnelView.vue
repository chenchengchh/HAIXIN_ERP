<template>
  <div class="sales-funnel-view">
    <div class="page-header">
      <h3>销售漏斗分析</h3>
    </div>
    
    <!-- 筛选和控制区域 -->
    <el-card shadow="hover" class="filter-card">
      <div class="filter-bar">
        <span class="filter-tip">当前展示全部未删除商机的阶段分布统计</span>
        <el-button type="primary" @click="handleRefresh">
          <el-icon><Refresh /></el-icon> 刷新数据
        </el-button>
      </div>
    </el-card>
    
    <!-- 销售漏斗数据卡片 -->
    <el-card shadow="hover" class="funnel-card">
      <template #header>
        <div class="card-header">
          <span>销售漏斗数据</span>
        </div>
      </template>
      <div class="funnel-content">
        <!-- 销售漏斗图表 -->
        <div ref="funnelChartRef" class="funnel-chart"></div>
        
        <!-- 销售漏斗数据表格 -->
        <el-table
          v-loading="loading"
          :data="funnelData"
          style="width: 100%; margin-top: 20px;"
          size="small"
        >
          <el-table-column prop="stageName" label="阶段" />
          <el-table-column prop="count" label="数量" />
          <el-table-column prop="amount" label="金额" align="right">
            <template #default="scope">
              {{ formatCurrency(scope.row.amount) }}
            </template>
          </el-table-column>
          <el-table-column prop="conversionRate" label="转化率" align="right">
            <template #default="scope">
              {{ scope.row.conversionRate }}%
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { Refresh } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { salesApi } from '../../../api/crm/sales'
import { unwrapListResponse } from '../../../api'

// 销售漏斗数据
const funnelData = ref<any[]>([])
const loading = ref(false)
const funnelChartRef = ref<HTMLElement | null>(null)
let funnelChart: echarts.ECharts | null = null

// 初始化数据
onMounted(() => {
  // 对接后端真实接口获取销售漏斗数据
  fetchFunnelData()
  // 初始化图表
  initChart()
  // 添加窗口大小变化监听
  window.addEventListener('resize', handleResize)
})

// 组件卸载时清理
onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  disposeChart()
})

// 监听数据变化，更新图表
watch(
  () => funnelData.value,
  () => {
    if (funnelChart) {
      updateChart()
    } else {
      // 如果图表实例不存在，重新初始化
      initChart()
    }
  },
  { deep: true }
)

/**
 * 窗口大小变化时重绘图表
 */
const handleResize = () => {
  if (funnelChart) {
    funnelChart.resize()
  }
}

/**
 * 销毁图表实例
 */
const disposeChart = () => {
  if (funnelChart) {
    funnelChart.dispose()
    funnelChart = null
  }
}

/**
 * 获取销售漏斗数据（对接后端 GET /api/v1/crm/opportunities/funnel）
 * 注意：该接口 data 为数组，须使用 unwrapListResponse 解包
 */
const fetchFunnelData = async () => {
  loading.value = true
  try {
    const response = await salesApi.getSalesFunnel()
    funnelData.value = unwrapListResponse<any>(response)
  } catch (error) {
    console.error('获取销售漏斗数据失败:', error)
    ElMessage.error('获取销售漏斗数据失败')
    funnelData.value = []
  } finally {
    loading.value = false
  }
}

/**
 * 初始化图表
 */
const initChart = () => {
  // 确保DOM完全渲染后再初始化图表
  nextTick(() => {
    if (funnelChartRef.value) {
      // 先销毁已存在的图表实例
      disposeChart()

      // 获取容器的实际尺寸
      const container = funnelChartRef.value!
      const { clientWidth, clientHeight } = container

      // 确保容器有实际尺寸
      if (clientWidth === 0 || clientHeight === 0) {
        // 如果容器尺寸为0，可能是组件还未可见，稍后重试
        setTimeout(() => initChart(), 100)
        return
      }

      // 直接设置canvas元素的width和height属性，确保绘图区域尺寸正确
      funnelChart = echarts.init(container, undefined, {
        width: clientWidth,
        height: clientHeight
      })

      // 立即调整尺寸
      funnelChart.resize()
      updateChart()
    }
  })
}

/**
 * 更新图表
 */
const updateChart = () => {
  if (!funnelChart) return

  // 确保图表使用正确的尺寸
  funnelChart.resize()

  // 如果数据为空，显示空状态
  if (funnelData.value.length === 0) {
    const option = {
      title: {
        text: '暂无销售漏斗数据',
        left: 'center',
        top: 'center',
        textStyle: {
          color: '#909399',
          fontSize: 16
        }
      }
    }
    funnelChart.setOption(option)
    return
  }

  const chartData = funnelData.value.map(item => ({
    name: item.stageName,
    value: item.count
  }))

  const option = {
    tooltip: {
      trigger: 'item',
      formatter: function(params: any) {
        const item = funnelData.value.find(data => data.stageName === params.name)
        return `${params.name}<br/>数量: ${params.value}<br/>金额: ${item ? formatCurrency(item.amount) : '0'}<br/>转化率: ${item ? item.conversionRate + '%' : '0%'}`
      }
    },
    legend: {
      data: funnelData.value.map(item => item.stageName),
      orient: 'vertical',
      left: 10
    },
    series: [
      {
        name: '销售漏斗',
        type: 'funnel',
        left: '15%',
        width: '70%',
        min: 0,
        max: Math.max(...funnelData.value.map(item => item.count), 1),
        minSize: '0%',
        maxSize: '100%',
        sort: 'none',
        gap: 2,
        label: {
          show: true,
          position: 'inside',
          formatter: '{b}: {c}'
        },
        labelLine: {
          length: 10,
          lineStyle: {
            width: 1,
            type: 'solid'
          }
        },
        itemStyle: {
          borderColor: '#fff',
          borderWidth: 1
        },
        emphasis: {
          label: {
            fontSize: 16
          }
        },
        data: chartData
      }
    ]
  }

  funnelChart.setOption(option)
}

/**
 * 格式化货币
 * @param amount 金额
 * @returns 格式化后的货币字符串
 */
const formatCurrency = (amount: number) => {
  return new Intl.NumberFormat('zh-CN', { style: 'currency', currency: 'CNY' }).format(amount ?? 0)
}

/**
 * 处理刷新：重新拉取销售漏斗数据
 */
const handleRefresh = () => {
  fetchFunnelData()
}
</script>

<style scoped>
.sales-funnel-view {
  padding: 10px;
}

.filter-card {
  margin-bottom: 16px;
}

.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.filter-tip {
  color: #909399;
  font-size: 14px;
}

.filter-form {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.funnel-card {
  margin-bottom: 16px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
  font-size: 1rem;
}

.funnel-content {
  padding: 20px 0;
}

.funnel-chart {
  height: 400px;
  width: 100%;
}
</style>