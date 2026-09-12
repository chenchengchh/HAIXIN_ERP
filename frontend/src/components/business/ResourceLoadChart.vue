<template>
  <div class="resource-load-chart">
    <!-- 资源选择和时间范围过滤 -->
    <div class="chart-header">
      <div class="filter-group">
        <el-select v-model="selectedResourceIds" multiple placeholder="选择资源" size="small" style="width: 200px;">
          <el-option
            v-for="resource in resources"
            :key="resource.id"
            :label="resource.name"
            :value="resource.id"
          />
        </el-select>
        
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          size="small"
          style="width: 280px; margin-left: 10px;"
        />
        
        <el-select v-model="timeScale" placeholder="时间刻度" size="small" style="width: 120px; margin-left: 10px;" @change="handleTimeScaleChange">
          <el-option label="小时" value="hour" />
          <el-option label="日" value="day" />
          <el-option label="周" value="week" />
          <el-option label="月" value="month" />
        </el-select>
        
        <el-button type="primary" size="small" @click="refreshData" :loading="isLoading" style="margin-left: 10px;">
          <el-icon v-if="isLoading"><Loading /></el-icon>
          刷新数据
        </el-button>
      </div>
    </div>
    
    <!-- 资源负载图表 -->
    <div class="chart-container">
      <div v-if="isLoading" class="loading-indicator">
        <el-icon class="loading-icon"><Loading /></el-icon>
        <span class="loading-text">加载中...</span>
      </div>
      <div v-else-if="chartData.length === 0" class="empty-data">
        <el-empty description="暂无资源负载数据" />
      </div>
      <div v-else>
        <div ref="chartRef" class="chart"></div>
      </div>
    </div>
    
    <!-- 资源负载统计 -->
    <div class="statistics" v-if="statisticsData.length > 0">
      <el-card shadow="hover" v-for="stat in statisticsData" :key="stat.resourceId" class="stat-card">
        <template #header>
          <div class="stat-header">
            <span>{{ stat.resourceName }}</span>
            <el-tag :type="getUtilizationTagType(stat.utilizationRate)">
              {{ stat.utilizationRate.toFixed(1) }}%
            </el-tag>
          </div>
        </template>
        <div class="stat-content">
          <el-descriptions :column="2" size="small" border>
            <el-descriptions-item label="总工作量">{{ stat.totalWorkload }} 小时</el-descriptions-item>
            <el-descriptions-item label="可用时间">{{ stat.availableTime }} 小时</el-descriptions-item>
            <el-descriptions-item label="工作时长">{{ stat.workTime }} 小时</el-descriptions-item>
            <el-descriptions-item label="空闲时长">{{ stat.idleTime }} 小时</el-descriptions-item>
            <el-descriptions-item label="加班时长" :span="2">
              <span v-if="stat.overtime > 0" style="color: #f56c6c;">{{ stat.overtime }} 小时</span>
              <span v-else>{{ stat.overtime }} 小时</span>
            </el-descriptions-item>
          </el-descriptions>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, watch, reactive } from 'vue'
import * as echarts from 'echarts'
import { ElMessage, ElEmpty, ElTag, ElCard, ElDescriptions, ElDatePicker, ElSelect, ElOption, ElButton } from 'element-plus'
import { ResourceLoadAPI } from '../../api/aps'
import { unwrapListResponse } from '../../api/index'

// 定义类型
interface Resource {
  id: string | number
  name: string
  type: string
}

interface ResourceLoadData {
  time: string
  resourceId: string | number
  resourceName: string
  load: number // 负载百分比
  workload: number // 工作量（小时）
  availableCapacity: number // 可用容量（小时）
}

interface StatisticsData {
  resourceId: string | number
  resourceName: string
  totalWorkload: number
  availableTime: number
  workTime: number
  idleTime: number
  overtime: number
  utilizationRate: number // 利用率百分比
}

// 定义props
const props = defineProps<{
  planId: string | number
  resourceIds?: string[] | number[]
}>()

// 定义事件
const emit = defineEmits<{
  (e: 'data-refreshed'): void
  (e: 'resource-selected', resourceId: string | number): void
}>()

// 状态管理
const chartRef = ref<HTMLElement | null>(null)
let chartInstance: echarts.ECharts | null = null
const isLoading = ref(false)

// 资源列表（从后端资源接口加载，不再使用硬编码数据）
const resources = ref<Resource[]>([])

// 过滤条件
const selectedResourceIds = ref<(string | number)[]>(props.resourceIds || [])
const dateRange = ref<[Date, Date]>([new Date(Date.now() - 7 * 24 * 60 * 60 * 1000), new Date()])
const timeScale = ref('day')

// 图表数据
const chartData = ref<ResourceLoadData[]>([])
const statisticsData = ref<StatisticsData[]>([])

// 初始化图表
const initChart = () => {
  if (!chartRef.value) return
  
  try {
    // 销毁现有实例
    if (chartInstance) {
      chartInstance.dispose()
    }
    
    // 创建新实例
    chartInstance = echarts.init(chartRef.value)
    
    // 监听窗口大小变化，使用防抖优化性能
    window.addEventListener('resize', debouncedResize)
    
    // 初始渲染
    renderChart()
  } catch (error) {
    console.error('初始化图表失败:', error)
    ElMessage.error('初始化资源负载图表失败')
  }
}

// 渲染图表
const renderChart = () => {
  if (!chartInstance || chartData.value.length === 0) return
  
  try {
    // 按资源分组数据
    const resourceDataMap = new Map<string | number, ResourceLoadData[]>()
    chartData.value.forEach(data => {
      if (!resourceDataMap.has(data.resourceId)) {
        resourceDataMap.set(data.resourceId, [])
      }
      resourceDataMap.get(data.resourceId)!.push(data)
    })
    
    // 准备图表数据
    const legendData = Array.from(resourceDataMap.keys()).map(key => {
      const resource = resources.value.find(r => r.id === key)
      return resource ? resource.name : `资源${key}`
    })
    
    const series = Array.from(resourceDataMap.entries()).map(([resourceId, data]) => {
      const resource = resources.value.find(r => r.id === resourceId)
      return {
        name: resource ? resource.name : `资源${resourceId}`,
        type: 'line' as const,
        data: data.map(item => [item.time, item.load]),
        smooth: true,
        symbol: 'circle',
        symbolSize: 4,
        emphasis: {
          focus: 'series',
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        },
        areaStyle: {
          opacity: 0.1
        },
        // 优化大数据量下的渲染性能
        sampling: 'lttb',
        progressive: 400
      } as any
    })
    
    // 图表配置
    const option: echarts.EChartsOption = {
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'cross',
          label: {
            backgroundColor: '#6a7985'
          }
        },
        formatter: (params: any) => {
          let result = `${params[0].axisValue}<br/>`
          params.forEach((param: any) => {
            result += `${param.marker} ${param.seriesName}: ${param.value[1]}%<br/>`
          })
          return result
        }
      },
      legend: {
        data: legendData,
        top: 0,
        right: 0,
        type: 'scroll'
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true
      },
      xAxis: {
        type: 'time',
        boundaryGap: false as any,
        axisLabel: {
          formatter: (value: any) => {
            // 根据时间刻度格式化x轴标签
            const date = new Date(value)
            switch (timeScale.value) {
              case 'hour':
                return `${date.getHours().toString().padStart(2, '0')}:00`
              case 'day':
                return `${date.getMonth() + 1}/${date.getDate()}`
              case 'week':
                return `第${Math.ceil((date.getDate() + 6) / 7)}周`
              case 'month':
                return `${date.getFullYear()}/${date.getMonth() + 1}`
              default:
                return `${date.getMonth() + 1}/${date.getDate()}`
            }
          }
        }
      },
      yAxis: {
        type: 'value',
        name: '负载率(%)',
        min: 0,
        max: 100,
        axisLabel: {
          formatter: '{value}%'
        },
        splitLine: {
          lineStyle: {
            type: 'dashed'
          }
        },
        axisLine: {
          show: true
        },
        axisTick: {
          show: true
        }
      },
      series: series,
      // 性能优化配置
      animation: false, // 禁用动画，提升性能
      animationDuration: 0,
      textStyle: {
        fontFamily: 'Arial, sans-serif',
        fontSize: 12
      },
      // 渐进式渲染配置
      progressiveThreshold: 500, // 数据量超过500时启用渐进式渲染
      progressive: 200 // 每次渐进渲染200个数据点
    }
    
    // 设置图表配置
    chartInstance.setOption(option)
  } catch (error) {
    console.error('渲染图表失败:', error)
    ElMessage.error('渲染资源负载图表失败')
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
  chartInstance?.resize()
}, 200)

// 窗口大小变化处理
const handleResize = () => {
  chartInstance?.resize()
}

// 刷新数据
const refreshData = async () => {
  try {
    isLoading.value = true
    await fetchResourceLoadData()
    renderChart()
    calculateStatistics()
    emit('data-refreshed')
  } catch (error) {
    console.error('获取资源负载数据失败:', error)
    // 显示错误提示
    ElMessage.error('获取资源负载数据失败，请稍后重试')
  } finally {
    isLoading.value = false
  }
}

/**
 * 加载资源列表
 * 调用后端 /api/v1/aps/resource-load/resources 接口获取真实资源数据
 */
const loadResources = async () => {
  try {
    const response = await ResourceLoadAPI.getResources()
    const list = unwrapListResponse<any>(response)
    resources.value = list.map((item: any) => ({
      id: item?.id ?? '',
      name: String(item?.name ?? `资源${item?.id ?? ''}`),
      type: String(item?.type ?? 'equipment')
    }))
  } catch (error) {
    // 接口异常时资源列表置空，不生成任何模拟数据
    console.error('获取资源列表失败:', error)
    resources.value = []
  }
}

/**
 * 获取资源负载数据
 * 调用后端 /api/v1/aps/resource-load 接口获取真实负载数据并映射为图表数据结构
 */
const fetchResourceLoadData = async () => {
  try {
    const params = {
      planId: props.planId,
      resourceIds: selectedResourceIds.value.map(String),
      startTime: dateRange.value[0]?.getTime(),
      endTime: dateRange.value[1]?.getTime(),
      timeScale: timeScale.value as 'hour' | 'day' | 'week' | 'month'
    }

    const response = await ResourceLoadAPI.getResourceLoadData(params)
    const list = unwrapListResponse<any>(response)

    // 将后端实体字段映射为图表所需结构
    chartData.value = list.map((item: any) => {
      const totalCapacity = Number(item?.totalCapacity ?? 0)
      const usedCapacity = Number(item?.usedCapacity ?? 0)
      return {
        time: String(item?.startTime ?? item?.timePeriod ?? ''),
        resourceId: item?.resourceId ?? '',
        resourceName: String(item?.resourceName ?? `资源${item?.resourceId ?? ''}`),
        load: Number(item?.loadRate ?? 0),
        workload: usedCapacity,
        availableCapacity: totalCapacity
      }
    })
  } catch (error) {
    // 接口异常时图表数据置空，展示空状态，不生成任何模拟数据
    console.error('获取资源负载数据失败:', error)
    chartData.value = []
  }
}

// 计算统计数据
const calculateStatistics = () => {
  if (chartData.value.length === 0) {
    statisticsData.value = []
    return
  }
  
  // 按资源分组计算统计数据
  const resourceStatsMap = new Map<string | number, StatisticsData>()
  
  chartData.value.forEach(data => {
    const resourceId = data.resourceId
    if (!resourceStatsMap.has(resourceId)) {
      resourceStatsMap.set(resourceId, {
        resourceId: resourceId,
        resourceName: data.resourceName,
        totalWorkload: 0,
        availableTime: 0,
        workTime: 0,
        idleTime: 0,
        overtime: 0,
        utilizationRate: 0
      })
    }
    
    const stats = resourceStatsMap.get(resourceId)!
    stats.totalWorkload += data.workload
    stats.availableTime += data.availableCapacity
    stats.workTime += Math.min(data.workload, data.availableCapacity)
    stats.idleTime += Math.max(0, data.availableCapacity - data.workload)
    stats.overtime += Math.max(0, data.workload - data.availableCapacity)
  })
  
  // 计算利用率
  statisticsData.value = Array.from(resourceStatsMap.values()).map(stats => {
    stats.utilizationRate = stats.availableTime > 0 
      ? (stats.workTime / stats.availableTime) * 100 
      : 0
    return stats
  })
}

// 处理时间刻度变化
const handleTimeScaleChange = () => {
  refreshData()
}

// 获取利用率标签类型
const getUtilizationTagType = (rate: number): 'success' | 'warning' | 'danger' => {
  if (rate < 70) return 'success'
  if (rate < 90) return 'warning'
  return 'danger'
}

// 监听props变化
watch(
  () => props.planId,
  () => {
    refreshData()
  }
)

watch(
  () => props.resourceIds,
  (newResourceIds) => {
    if (newResourceIds && newResourceIds.length > 0) {
      selectedResourceIds.value = newResourceIds
    }
  }
)

// 组件挂载时初始化
onMounted(async () => {
  initChart()
  // 先加载真实资源列表，再刷新负载数据
  await loadResources()
  await refreshData()
})

// 组件卸载前清理
onBeforeUnmount(() => {
  // 销毁图表实例
  if (chartInstance) {
    chartInstance.dispose()
    chartInstance = null
  }
  
  // 移除事件监听
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.resource-load-chart {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  box-sizing: border-box;
}

.chart-header {
  padding: 10px 0;
  margin-bottom: 10px;
  box-sizing: border-box;
}

.filter-group {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 5px;
  box-sizing: border-box;
}

.chart-container {
  flex: 1;
  min-height: 300px;
  position: relative;
  box-sizing: border-box;
  background-color: #ffffff;
  border-radius: 4px;
  border: 1px solid #e4e7ed;
  padding: 10px;
}

.chart {
  width: 100%;
  height: 100%;
  min-height: 300px;
  box-sizing: border-box;
}

.loading-indicator {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 300px;
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

.empty-data {
  height: 300px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.statistics {
  margin-top: 20px;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 15px;
  box-sizing: border-box;
}

.stat-card {
  box-sizing: border-box;
}

.stat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stat-content {
  margin-top: 10px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .filter-group {
    flex-direction: column;
    align-items: stretch;
  }
  
  .filter-group .el-select,
  .filter-group .el-date-picker,
  .filter-group .el-button {
    width: 100% !important;
    margin-left: 0 !important;
    margin-top: 10px;
  }
  
  .statistics {
    grid-template-columns: 1fr;
    gap: 10px;
  }
  
  .chart-container {
    min-height: 250px;
  }
  
  .chart {
    min-height: 250px;
  }
}

@media (max-width: 480px) {
  .resource-load-chart {
    padding: 5px;
  }
  
  .chart-header {
    padding: 5px 0;
    margin-bottom: 5px;
  }
  
  .chart-container {
    padding: 5px;
    min-height: 200px;
  }
  
  .chart {
    min-height: 200px;
  }
}
</style>