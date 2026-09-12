<template>
  <div class="energy-analysis-view">
    <div class="page-header">
      <h2>能耗分析</h2>
      <el-breadcrumb>
        <el-breadcrumb-item>
          <router-link to="/home">首页</router-link>
        </el-breadcrumb-item>
        <el-breadcrumb-item>
          <router-link to="/home/ems">EMS系统</router-link>
        </el-breadcrumb-item>
        <el-breadcrumb-item>
          <router-link to="/home/ems/energy-analysis">能耗分析</router-link>
        </el-breadcrumb-item>
        <el-breadcrumb-item :to="`/home/ems/energy-analysis#${activeTab}`">
          {{ tabLabelMap[activeTab] }}
        </el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <!-- AI 能耗负荷预测（S04，能耗超限风险预警，空态自动隐藏） -->
    <ModuleAiSuggestionCard
      suggestion-type="ENERGY_LOAD"
      title="AI 能耗负荷预测"
      :max-display="5"
    />

    <el-tabs v-model="activeTab" class="function-tabs" @tab-change="handleTabChange">
      <el-tab-pane label="能耗统计" name="energy-statistics" />
      <el-tab-pane label="趋势分析" name="trend-analysis" />
      <el-tab-pane label="能耗对比" name="energy-comparison" />
      <el-tab-pane label="异常检测" name="anomaly-detection" />
    </el-tabs>
    
    <!-- 能耗统计 -->
    <div v-if="activeTab === 'energy-statistics'" class="tab-content">
      <el-card shadow="hover" class="sub-card">
        <template #header>
          <div class="sub-card-header">
            <span>能耗统计</span>
            <div class="filter-controls">
              <el-select v-model="statisticsParams.dimension" placeholder="统计维度" style="width: 150px; margin-right: 10px;">
                <el-option label="按车间" value="workshop" />
                <el-option label="按产线" value="production-line" />
                <el-option label="按设备" value="equipment" />
                <el-option label="按班组" value="team" />
              </el-select>
              <el-button type="primary" size="small" @click="updateEnergyStatistics">刷新数据</el-button>
            </div>
          </div>
        </template>
        <div class="statistics-content">
          <el-table v-loading="emsStore.loading.energyStatistics" :data="emsStore.energyStatistics" style="width: 100%" height="400">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="name" label="名称" width="150" />
            <el-table-column prop="electricity" label="电力(kWh)" width="120" />
            <el-table-column prop="water" label="水(m³)" width="100" />
            <el-table-column prop="gas" label="燃气(m³)" width="120" />
            <el-table-column prop="heat" label="热能(GJ)" width="120" />
            <el-table-column prop="totalCost" label="总成本(元)" width="120" />
            <el-table-column prop="statDate" label="统计日期" width="180" />
          </el-table>
        </div>
      </el-card>
    </div>
    
    <!-- 趋势分析 -->
    <div v-if="activeTab === 'trend-analysis'" class="tab-content">
      <el-card shadow="hover" class="sub-card">
        <template #header>
          <div class="sub-card-header">
            <span>趋势分析</span>
            <div class="filter-controls">
              <el-select v-model="trendParams.energyType" placeholder="能源类型" style="width: 120px; margin-right: 16px;">
                <el-option label="电力" value="electricity" />
                <el-option label="水" value="water" />
                <el-option label="燃气" value="gas" />
                <el-option label="热能" value="heat" />
              </el-select>
              <el-select v-model="trendParams.timeRange" placeholder="时间范围" style="width: 120px; margin-right: 16px;">
                <el-option label="日" value="day" />
                <el-option label="周" value="week" />
                <el-option label="月" value="month" />
                <el-option label="年" value="year" />
              </el-select>
              <el-button type="primary" size="small" @click="updateTrendAnalysis">刷新数据</el-button>
            </div>
          </div>
        </template>
        <div class="trend-content">
          <div class="trend-chart">
            <div id="trendChart" class="trend-echarts"></div>
          </div>
        </div>
      </el-card>
    </div>
    
    <!-- 能耗对比 -->
    <div v-if="activeTab === 'energy-comparison'" class="tab-content">
      <el-card shadow="hover" class="sub-card">
        <template #header>
          <div class="sub-card-header">
            <span>能耗对比</span>
            <div class="filter-controls">
              <el-select v-model="comparisonParams.type" placeholder="对比类型" style="width: 150px; margin-right: 10px;">
                <el-option label="同比" value="year-on-year" />
                <el-option label="环比" value="month-on-month" />
                <el-option label="多设备对比" value="multi-equipment" />
              </el-select>
              <el-button type="primary" size="small" @click="updateEnergyComparison">刷新数据</el-button>
            </div>
          </div>
        </template>
        <div class="comparison-content">
          <el-table :data="emsStore.energyComparison" style="width: 100%" height="400">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="name" label="名称" width="150" />
            <el-table-column prop="currentPeriod" label="本期值" width="120" />
            <el-table-column prop="previousPeriod" label="上期值" width="120" />
            <el-table-column prop="difference" label="差值" width="100">
              <template #default="scope">
                <span :class="scope.row.difference >= 0 ? 'text-danger' : 'text-success'">
                  {{ scope.row.difference >= 0 ? '+' : '' }}{{ scope.row.difference }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="growthRate" label="增长率(%)" width="120">
              <template #default="scope">
                <span :class="scope.row.growthRate >= 0 ? 'text-danger' : 'text-success'">
                  {{ scope.row.growthRate >= 0 ? '+' : '' }}{{ scope.row.growthRate }}%
                </span>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-card>
    </div>
    
    <!-- 异常检测 -->
    <div v-if="activeTab === 'anomaly-detection'" class="tab-content">
      <el-card shadow="hover" class="sub-card">
        <template #header>
          <div class="sub-card-header">
            <span>异常检测</span>
            <div class="filter-controls">
              <el-button type="success" size="small" @click="markAllAnomaliesAsProcessed">标记所有异常为已处理</el-button>
              <el-button type="primary" size="small" @click="refreshAnomalies" style="margin-left: 10px;">刷新数据</el-button>
            </div>
          </div>
        </template>
        <div class="anomaly-content">
          <el-table v-loading="emsStore.loading.energyAnomalies" :data="emsStore.energyAnomalies" style="width: 100%" height="400">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="energyType" label="能源类型" width="120" />
            <el-table-column prop="area" label="区域" width="120" />
            <el-table-column prop="anomalyType" label="异常类型" width="150" />
            <el-table-column prop="actualValue" label="异常值" width="100" />
            <el-table-column prop="expectedValue" label="预期值" width="100" />
            <el-table-column prop="detectionTime" label="检测时间" width="180" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="scope">
                <el-tag :type="scope.row.status === 'pending' ? 'warning' : 'success'">
                  {{ scope.row.status === 'pending' ? '待处理' : '已处理' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120">
              <template #default="scope">
                <el-button size="small" type="primary" @click="handleProcessAnomaly(scope.row)">
                  {{ scope.row.status === 'pending' ? '处理' : '查看' }}
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-card>
    </div>

    <!-- 异常详情对话框 -->
    <el-dialog v-model="anomalyDetailVisible" title="异常详情" width="500px">
      <el-descriptions v-if="currentAnomaly" :column="1" border>
        <el-descriptions-item label="ID">{{ currentAnomaly.id }}</el-descriptions-item>
        <el-descriptions-item label="能源类型">{{ currentAnomaly.energyType }}</el-descriptions-item>
        <el-descriptions-item label="区域">{{ currentAnomaly.area }}</el-descriptions-item>
        <el-descriptions-item label="异常类型">{{ currentAnomaly.anomalyType }}</el-descriptions-item>
        <el-descriptions-item label="异常值">{{ currentAnomaly.actualValue }}</el-descriptions-item>
        <el-descriptions-item label="预期值">{{ currentAnomaly.expectedValue }}</el-descriptions-item>
        <el-descriptions-item label="检测时间">{{ currentAnomaly.detectionTime }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="currentAnomaly.status === 'pending' ? 'warning' : 'success'">
            {{ currentAnomaly.status === 'pending' ? '待处理' : '已处理' }}
          </el-tag>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="anomalyDetailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useEmsStore } from '@/stores/ems'
import * as echarts from 'echarts'
import ModuleAiSuggestionCard from '@/components/ai/ModuleAiSuggestionCard.vue'

// 路由实例
const router = useRouter()
const route = useRoute()

// 激活的标签页
const activeTab = ref('energy-statistics')

// 标签页名称映射
const tabLabelMap: Record<string, string> = {
  'energy-statistics': '能耗统计',
  'trend-analysis': '趋势分析',
  'energy-comparison': '能耗对比',
  'anomaly-detection': '异常检测'
}

// 状态管理
const emsStore = useEmsStore()

// 能耗统计参数
const statisticsParams = ref({
  dimension: 'workshop' as 'workshop' | 'production-line' | 'equipment' | 'team'
})

// 趋势分析参数
const trendParams = ref({
  energyType: 'electricity' as 'electricity' | 'water' | 'gas' | 'heat',
  timeRange: 'month' as 'day' | 'week' | 'month' | 'year'
})

// 能耗对比参数
const comparisonParams = ref({
  type: 'year-on-year' as 'year-on-year' | 'month-on-month' | 'multi-equipment'
})

// 趋势图相关
const trendChart = ref<echarts.ECharts | null>(null)

// 初始化趋势图
const initTrendChart = () => {
  const chartDom = document.getElementById('trendChart')
  if (chartDom) {
    // 如果已存在图表实例，先销毁
    if (trendChart.value) {
      trendChart.value.dispose()
    }
    trendChart.value = echarts.init(chartDom)
    
    // 初始空数据配置
    const option = {
      title: {
        text: '能耗趋势',
        left: 'center'
      },
      tooltip: {
        trigger: 'axis'
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '15%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        data: []
      },
      yAxis: {
        type: 'value',
        name: '能耗值',
        nameLocation: 'middle',
        nameGap: 30
      },
      series: [
        {
          name: '能耗',
          type: 'line',
          data: [],
          smooth: true,
          itemStyle: {
            color: '#409EFF'
          },
          areaStyle: {
            color: {
              type: 'linear',
              x: 0,
              y: 0,
              x2: 0,
              y2: 1,
              colorStops: [
                { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
                { offset: 1, color: 'rgba(64, 158, 255, 0.05)' }
              ]
            }
          }
        }
      ]
    }
    
    trendChart.value.setOption(option)
    
    // 检查是否有数据，如果没有则调用API获取数据
    if (emsStore.energyTrend.length === 0) {
      updateTrendAnalysis()
    } else {
      // 初始化完成后立即更新图表数据
      updateTrendChart()
    }
  }
}

// 更新趋势图数据
const updateTrendChart = () => {
  if (trendChart.value) {
    const xData = emsStore.energyTrend.map((item: any) => item.time)
    const yData = emsStore.energyTrend.map((item: any) => item.value)
    
    // 获取当前能源类型的中文名称
    const energyTypeMap: Record<string, string> = {
      'electricity': '电力',
      'water': '水',
      'gas': '燃气',
      'heat': '热能'
    }
    const energyTypeName = energyTypeMap[trendParams.value.energyType] || '能耗'
    
    trendChart.value.setOption({
      xAxis: {
        data: xData
      },
      yAxis: {
        name: `${energyTypeName}值`
      },
      series: [
        {
          name: energyTypeName,
          data: yData
        }
      ]
    })
  }
}

// 从路由获取标签页状态
const getActiveTabFromRoute = () => {
  const tabMap: Record<string, string> = {
    'energy-statistics': 'energy-statistics',
    'trend-analysis': 'trend-analysis',
    'energy-comparison': 'energy-comparison',
    'anomaly-detection': 'anomaly-detection'
  }
  const tabName = route.params.tab || 'energy-statistics'
  return tabMap[tabName as string] || 'energy-statistics'
}

// 组件挂载时，从路由获取标签页状态并加载数据
onMounted(async () => {
  activeTab.value = getActiveTabFromRoute()
  
  // 加载能耗分析相关数据，包括趋势分析数据
  await Promise.all([
    emsStore.fetchEnergyStatistics({ dimension: 'workshop' }),
    emsStore.fetchEnergyComparison({ type: 'year-on-year' }),
    emsStore.fetchEnergyAnomalies(),
    emsStore.fetchEnergyTrend(trendParams.value) // 添加趋势分析数据加载
  ])
  
  // 如果当前激活的是趋势分析标签页，初始化图表
  if (activeTab.value === 'trend-analysis') {
    // 延迟初始化，确保DOM已经渲染
    setTimeout(() => {
      initTrendChart()
    }, 100)
  }
})

// 监听标签页切换
watch(activeTab, (newTab) => {
  if (newTab === 'trend-analysis') {
    // 延迟初始化，确保DOM已经渲染
    setTimeout(() => {
      initTrendChart()
    }, 100)
  }
})

// 监听趋势数据变化，更新图表
watch(() => emsStore.energyTrend, () => {
  updateTrendChart()
}, { deep: true })

// 标签页切换事件
const handleTabChange = (tabName: string) => {
  console.log('切换标签页:', tabName)
  // 更新路由，保持标签页状态
  router.push({
    path: `/home/ems/energy-analysis/${tabName}`
  })
}

// 方法
/**
 * 处理单个异常
 * @param anomaly 异常信息
 */
/**
 * 处理/查看异常：待处理状态执行处理操作，已处理状态弹出详情对话框
 */
const handleProcessAnomaly = async (anomaly: any) => {
  if (anomaly.status === 'pending') {
    try {
      await emsStore.processAnomaly(anomaly.id)
      ElMessage.success('异常处理成功')
    } catch (error) {
      console.error('异常处理失败:', error)
      ElMessage.error('异常处理失败')
    }
  } else {
    // 已处理状态：展示异常详情
    currentAnomaly.value = anomaly
    anomalyDetailVisible.value = true
  }
}

// 异常详情对话框状态
const anomalyDetailVisible = ref(false)
const currentAnomaly = ref<any>(null)

/**
 * 标记所有异常为已处理
 */
const markAllAnomaliesAsProcessed = async () => {
  try {
    await emsStore.markAllAnomaliesAsProcessed()
    ElMessage.success('所有异常已标记为已处理')
  } catch (error) {
    console.error('标记所有异常为已处理失败:', error)
    ElMessage.error('标记所有异常为已处理失败')
  }
}

/**
 * 刷新异常数据
 */
const refreshAnomalies = async () => {
  await emsStore.fetchEnergyAnomalies()
  ElMessage.success('异常数据刷新成功')
}

/**
 * 更新能耗统计数据
 */
const updateEnergyStatistics = async () => {
  await emsStore.fetchEnergyStatistics(statisticsParams.value)
  ElMessage.success('能耗统计数据更新成功')
}

/**
 * 更新能耗对比数据
 */
const updateEnergyComparison = async () => {
  await emsStore.fetchEnergyComparison(comparisonParams.value)
  ElMessage.success('能耗对比数据更新成功')
}

/**
 * 更新趋势分析数据
 */
const updateTrendAnalysis = async () => {
  try {
    await emsStore.fetchEnergyTrend(trendParams.value)
    ElMessage.success('趋势分析数据更新成功')
    // 数据更新后，确保图表也更新
    updateTrendChart()
  } catch (error) {
    console.error('更新趋势分析数据失败:', error)
    ElMessage.error('更新趋势分析数据失败')
  }
}
</script>

<style scoped>
.energy-analysis-view {
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

/* 趋势图样式 */
.trend-chart {
  height: 400px;
  width: 100%;
}

.trend-echarts {
  height: 100%;
  width: 100%;
}

.tab-content {
  padding: 10px 0;
}

.statistics-content {
  padding: 10px 0;
}

.trend-content {
  padding: 10px 0;
}

.trend-chart {
  height: 400px;
  background-color: #f5f7fa;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.comparison-content {
  padding: 10px 0;
}

.anomaly-content {
  padding: 10px 0;
}

.text-danger {
  color: #F56C6C;
}

.text-success {
  color: #67C23A;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .energy-analysis-view {
    padding: 12px;
  }
  
  .sub-card {
    margin-bottom: 12px;
  }
  
  .sub-card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
}
</style>
