<template>
  <div class="sales-forecast-view">
    <div class="page-header">
      <h3>销售预测</h3>
    </div>

    <!-- 筛选和控制区域 -->
    <el-card shadow="hover" class="filter-card">
      <el-form :inline="true" :model="filterForm" class="filter-form">
        <el-form-item label="统计周期">
          <el-input v-model="filterForm.period" placeholder="输入统计周期（如 2026-Q1 或 2026-07）" clearable style="width: 240px;" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleFilter">
            <el-icon><Search /></el-icon> 筛选
          </el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="primary" @click="handleCreate">
            <el-icon><Plus /></el-icon> 新建预测
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 销售预测数据卡片 -->
    <el-card shadow="hover" class="forecast-card">
      <template #header>
        <div class="card-header">
          <span>销售预测数据</span>
        </div>
      </template>
      <div class="forecast-content" v-loading="loading">
        <!-- 销售预测图表 -->
        <div ref="forecastChartRef" class="forecast-chart"></div>

        <!-- 销售预测数据表格 -->
        <el-table
          :data="forecastData"
          style="width: 100%; margin-top: 20px;"
          size="small"
        >
          <el-table-column prop="period" label="统计周期" width="110" />
          <el-table-column prop="ownerName" label="负责人" width="110" />
          <el-table-column prop="targetAmount" label="目标金额" align="right" min-width="120">
            <template #default="scope">
              {{ formatCurrency(scope.row.targetAmount) }}
            </template>
          </el-table-column>
          <el-table-column prop="forecastAmount" label="预测金额" align="right" min-width="120">
            <template #default="scope">
              {{ formatCurrency(scope.row.forecastAmount) }}
            </template>
          </el-table-column>
          <el-table-column prop="actualAmount" label="实际金额" align="right" min-width="120">
            <template #default="scope">
              {{ formatCurrency(scope.row.actualAmount) }}
            </template>
          </el-table-column>
          <el-table-column label="预测达成率" align="right" width="110">
            <template #default="scope">
              {{ calcForecastRate(scope.row) }}%
            </template>
          </el-table-column>
          <el-table-column label="实际偏差" align="right" min-width="120">
            <template #default="scope">
              <span :style="{ color: calcDeviation(scope.row) >= 0 ? '#67C23A' : '#F56C6C' }">
                {{ formatCurrency(calcDeviation(scope.row)) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="remark" label="备注" min-width="140" show-overflow-tooltip />
          <el-table-column prop="createTime" label="创建时间" width="160">
            <template #default="scope">
              {{ formatTime(scope.row.createTime) }}
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>

    <!-- 新建销售预测对话框 -->
    <el-dialog v-model="createDialogVisible" title="新建销售预测" width="560px">
      <el-form :model="forecastForm" :rules="forecastFormRules" ref="forecastFormRef" label-width="100px">
        <el-form-item label="统计周期" prop="period">
          <el-input v-model="forecastForm.period" placeholder="输入统计周期（如 2026-Q1 或 2026-07）" />
        </el-form-item>
        <el-form-item label="负责人" prop="ownerName">
          <el-input v-model="forecastForm.ownerName" placeholder="请输入负责人姓名" />
        </el-form-item>
        <el-form-item label="目标金额" prop="targetAmount">
          <el-input-number v-model="forecastForm.targetAmount" :min="0" :precision="2" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="预测金额" prop="forecastAmount">
          <el-input-number v-model="forecastForm.forecastAmount" :min="0" :precision="2" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="实际金额">
          <el-input-number v-model="forecastForm.actualAmount" :min="0" :precision="2" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="forecastForm.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSaveForecast">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { Search, Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { salesApi } from '../../../api/crm/sales'
import { unwrapListResponse } from '../../../api'

// 筛选表单（period 对应后端预测周期，如 2026-Q1 或 2026-07）
const filterForm = ref({
  period: getCurrentPeriod()
})

// 销售预测数据
const forecastData = ref<any[]>([])
const loading = ref(false)
const forecastChartRef = ref<HTMLElement | null>(null)
let forecastChart: echarts.ECharts | null = null

// 新建预测对话框状态
const createDialogVisible = ref(false)
const submitLoading = ref(false)
const forecastFormRef = ref()

// 新建预测表单（字段与后端 SalesForecast 实体对应）
const forecastForm = reactive({
  period: '',
  ownerName: '',
  targetAmount: 0,
  forecastAmount: 0,
  actualAmount: 0,
  remark: ''
})

// 新建预测表单校验规则
const forecastFormRules = reactive({
  period: [{ required: true, message: '请输入统计周期', trigger: 'blur' }],
  ownerName: [{ required: true, message: '请输入负责人姓名', trigger: 'blur' }],
  targetAmount: [{ required: true, message: '请输入目标金额', trigger: 'blur' }],
  forecastAmount: [{ required: true, message: '请输入预测金额', trigger: 'blur' }]
})

/**
 * 获取当前季度周期字符串（如 2026-Q3）
 */
function getCurrentPeriod(): string {
  const now = new Date()
  const quarter = Math.floor(now.getMonth() / 3) + 1
  return `${now.getFullYear()}-Q${quarter}`
}

// 初始化数据
onMounted(() => {
  fetchForecastData()
  initChart()
  window.addEventListener('resize', handleResize)
})

// 组件卸载时清理
onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  disposeChart()
})

// 监听数据变化，更新图表
watch(
  () => forecastData.value,
  () => {
    if (forecastChart) {
      updateChart()
    } else {
      initChart()
    }
  },
  { deep: true }
)

/**
 * 窗口大小变化时重绘图表
 */
const handleResize = () => {
  if (forecastChart) {
    forecastChart.resize()
  }
}

/**
 * 销毁图表实例
 */
const disposeChart = () => {
  if (forecastChart) {
    forecastChart.dispose()
    forecastChart = null
  }
}

/**
 * 获取销售预测数据（对接后端 GET /api/v1/crm/forecast/period/{period}，返回 Result<List>）
 */
const fetchForecastData = async () => {
  const period = filterForm.value.period?.trim()
  if (!period) {
    ElMessage.warning('请输入统计周期')
    return
  }
  loading.value = true
  try {
    const response = await salesApi.getSalesForecast(period)
    forecastData.value = unwrapListResponse<any>(response)
  } catch (error) {
    console.error('获取销售预测失败:', error)
    ElMessage.error('获取销售预测失败')
    forecastData.value = []
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
    if (forecastChartRef.value) {
      disposeChart()

      const container = forecastChartRef.value!
      const { clientWidth, clientHeight } = container

      // 容器尺寸为0时稍后重试
      if (clientWidth === 0 || clientHeight === 0) {
        setTimeout(() => initChart(), 100)
        return
      }

      forecastChart = echarts.init(container, undefined, {
        width: clientWidth,
        height: clientHeight
      })

      forecastChart.resize()
      updateChart()
    }
  })
}

/**
 * 更新图表：按负责人对比目标金额、预测金额、实际金额
 */
const updateChart = () => {
  if (!forecastChart) return

  forecastChart.resize()

  // 数据为空时显示空状态
  if (forecastData.value.length === 0) {
    forecastChart.setOption({
      title: {
        text: '暂无销售预测数据',
        left: 'center',
        top: 'center',
        textStyle: {
          color: '#909399',
          fontSize: 16
        }
      }
    })
    return
  }

  // X轴为负责人，三组柱状图分别展示目标/预测/实际金额
  const owners = forecastData.value.map(item => item.ownerName || '未指定')
  const targetAmounts = forecastData.value.map(item => Number(item.targetAmount || 0))
  const forecastAmounts = forecastData.value.map(item => Number(item.forecastAmount || 0))
  const actualAmounts = forecastData.value.map(item => Number(item.actualAmount || 0))

  const option = {
    tooltip: {
      trigger: 'axis',
      formatter: function (params: any) {
        let result = `${params[0].name}<br/>`
        params.forEach((param: any) => {
          result += `${param.marker}${param.seriesName}: ${formatCurrency(param.value)}<br/>`
        })
        return result
      }
    },
    legend: {
      data: ['目标金额', '预测金额', '实际金额'],
      top: 0
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: owners,
      axisLabel: {
        rotate: 30
      }
    },
    yAxis: {
      type: 'value',
      axisLabel: {
        formatter: (value: number) => {
          return (value / 10000).toFixed(0) + '万'
        }
      },
      splitLine: {
        lineStyle: {
          type: 'dashed'
        }
      }
    },
    series: [
      { name: '目标金额', type: 'bar', data: targetAmounts, itemStyle: { color: '#909399' } },
      { name: '预测金额', type: 'bar', data: forecastAmounts, itemStyle: { color: '#409EFF' } },
      { name: '实际金额', type: 'bar', data: actualAmounts, itemStyle: { color: '#67C23A' } }
    ]
  }

  forecastChart.setOption(option)
}

/**
 * 格式化货币显示
 * @param amount 金额
 */
const formatCurrency = (amount: number | undefined | null) => {
  return new Intl.NumberFormat('zh-CN', { style: 'currency', currency: 'CNY' }).format(Number(amount || 0))
}

/**
 * 格式化时间显示
 * @param time 后端返回的时间字符串
 */
const formatTime = (time: string | undefined | null) => {
  if (!time) return '-'
  return String(time).replace('T', ' ').substring(0, 19)
}

/**
 * 计算预测达成率（预测金额/目标金额*100，目标金额为0时返回0，保留1位小数）
 * @param row 预测行数据
 */
const calcForecastRate = (row: any): number => {
  const targetAmount = Number(row?.targetAmount || 0)
  const forecastAmount = Number(row?.forecastAmount || 0)
  if (!targetAmount || targetAmount <= 0) return 0
  return Math.round((forecastAmount / targetAmount) * 1000) / 10
}

/**
 * 计算实际偏差（实际金额-目标金额）
 * @param row 预测行数据
 */
const calcDeviation = (row: any): number => {
  return Number(row?.actualAmount || 0) - Number(row?.targetAmount || 0)
}

/**
 * 处理筛选：按周期重新查询预测数据
 */
const handleFilter = () => {
  fetchForecastData()
}

/**
 * 处理重置：恢复默认周期并重新查询
 */
const handleReset = () => {
  filterForm.value = {
    period: getCurrentPeriod()
  }
  fetchForecastData()
}

/**
 * 处理新建：打开新建预测对话框并重置表单
 */
const handleCreate = () => {
  Object.assign(forecastForm, {
    period: filterForm.value.period || getCurrentPeriod(),
    ownerName: '',
    targetAmount: 0,
    forecastAmount: 0,
    actualAmount: 0,
    remark: ''
  })
  createDialogVisible.value = true
}

/**
 * 保存销售预测：调用 POST /api/v1/crm/forecast
 */
const handleSaveForecast = () => {
  forecastFormRef.value.validate(async (valid: boolean) => {
    if (!valid) return
    submitLoading.value = true
    try {
      await salesApi.createSalesForecast({
        period: forecastForm.period,
        ownerName: forecastForm.ownerName,
        targetAmount: forecastForm.targetAmount,
        forecastAmount: forecastForm.forecastAmount,
        actualAmount: forecastForm.actualAmount,
        remark: forecastForm.remark
      })
      ElMessage.success('销售预测创建成功')
      createDialogVisible.value = false
      // 若新建预测周期与当前筛选周期一致，刷新列表
      if (forecastForm.period === filterForm.value.period?.trim()) {
        fetchForecastData()
      }
    } catch (error) {
      console.error('创建销售预测失败:', error)
      ElMessage.error('创建销售预测失败')
    } finally {
      submitLoading.value = false
    }
  })
}
</script>

<style scoped>
.sales-forecast-view {
  padding: 10px;
}

.filter-card {
  margin-bottom: 16px;
}

.filter-form {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.forecast-card {
  margin-bottom: 16px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
  font-size: 1rem;
}

.forecast-content {
  padding: 20px 0;
}

.forecast-chart {
  height: 400px;
  width: 100%;
}
</style>
