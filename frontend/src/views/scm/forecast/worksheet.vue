<template>
  <div class="forecast-worksheet">
    <el-card shadow="hover" class="mb-4">
      <template #header>
        <div class="card-header">
          <h3>预测工作台</h3>
          <div class="header-actions">
            <el-button type="primary" size="small" @click="runForecast">
              <el-icon><RefreshRight /></el-icon> 运行预测
            </el-button>
            <el-button type="success" size="small" @click="saveForecast">
              <el-icon><Check /></el-icon> 保存
            </el-button>
            <el-button type="warning" size="small" @click="publishForecast">
              <el-icon><Upload /></el-icon> 发布
            </el-button>
            <el-button type="danger" size="small" @click="resetForecast">
              <el-icon><Refresh /></el-icon> 重置
            </el-button>
            <el-dropdown @command="handleExport">
              <el-button size="small">
                <el-icon><Download /></el-icon> 导出
                <el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="excel">导出Excel</el-dropdown-item>
                  <el-dropdown-item command="csv">导出CSV</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </template>
      
      <!-- 筛选条件 -->
      <div class="filter-section mb-4">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-input v-model="filter.productCategory" placeholder="产品类别" clearable />
          </el-col>
          <el-col :span="6">
            <el-input v-model="filter.product" placeholder="产品编码/名称" clearable />
          </el-col>
          <el-col :span="6">
            <el-input v-model="filter.region" placeholder="销售区域" clearable />
          </el-col>
          <el-col :span="6">
            <el-date-picker
              v-model="filter.period"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              format="YYYY-MM"
              value-format="YYYY-MM"
              picker-options="{ type: 'month' }"
            />
          </el-col>
        </el-row>
      </div>

      <!-- 预测数据表格 -->
      <div class="table-section">
        <el-table
          v-loading="loading"
          :data="forecastData"
          border
          style="width: 100%"
          :default-sort="{ prop: 'productName', order: 'asc' }"
        >
          <el-table-column prop="productName" label="产品名称" width="180" />
          <el-table-column prop="productCode" label="产品编码" width="150" />
          <el-table-column prop="region" label="销售区域" width="120" />
          
          <!-- 历史销量列 -->
          <el-table-column label="历史销量" width="80">
            <template #header>
              <div class="column-header">
                <span>历史销量</span>
                <el-popover
                  placement="top"
                  :width="200"
                  trigger="hover"
                  content="过去12个月的实际销量"
                >
                  <template #reference>
                    <el-icon class="info-icon"><InfoFilled /></el-icon>
                  </template>
                </el-popover>
              </div>
            </template>
            <template #default="scope">
              <div class="history-sales">
                <div v-for="(value, index) in scope.row.historySales" :key="index" class="sales-item">
                  {{ value }}
                </div>
              </div>
            </template>
          </el-table-column>
          
          <!-- 基准预测列 -->
          <el-table-column prop="baselineForecast" label="基准预测" width="120" align="right" />
          
          <!-- 促销调整列 -->
          <el-table-column prop="promotionAdjustment" label="促销调整" width="120" align="right">
            <template #default="scope">
              <el-input-number
                v-model="scope.row.promotionAdjustment"
                :min="-100"
                :max="100"
                :step="1"
                size="small"
                @change="onAdjustmentChange(scope.row)"
              />
            </template>
          </el-table-column>
          
          <!-- 季节调整列 -->
          <el-table-column prop="seasonalAdjustment" label="季节调整" width="120" align="right">
            <template #default="scope">
              <el-input-number
                v-model="scope.row.seasonalAdjustment"
                :min="-100"
                :max="100"
                :step="1"
                size="small"
                @change="onAdjustmentChange(scope.row)"
              />
            </template>
          </el-table-column>
          
          <!-- 人工调整列 -->
          <el-table-column prop="manualAdjustment" label="人工调整" width="120" align="right">
            <template #default="scope">
              <el-input-number
                v-model="scope.row.manualAdjustment"
                :min="-100"
                :max="100"
                :step="1"
                size="small"
                @change="onAdjustmentChange(scope.row)"
              />
            </template>
          </el-table-column>
          
          <!-- 最终预测列 -->
          <el-table-column prop="finalForecast" label="最终预测" width="120" align="right" />
          
          <!-- 操作列 -->
          <el-table-column label="操作" width="150" align="center">
            <template #default="scope">
              <el-button type="primary" size="small" link @click="editRow(scope.row)">
                <el-icon><Edit /></el-icon> 编辑
              </el-button>
              <el-button type="danger" size="small" link @click="deleteRow(scope.$index)">
                <el-icon><Delete /></el-icon> 删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      
      <!-- 预测结果统计 -->
      <div class="stats-section">
        <el-card shadow="hover" class="stats-card">
          <template #header>
            <h4>预测结果统计</h4>
          </template>
          <div class="stats-content">
            <div class="stat-item">
              <div class="stat-label">总产品数</div>
              <div class="stat-value">{{ forecastData.length }}</div>
            </div>
            <div class="stat-item">
              <div class="stat-label">总预测数量</div>
              <div class="stat-value">{{ totalForecast }}</div>
            </div>
            <div class="stat-item">
              <div class="stat-label">平均调整幅度</div>
              <div class="stat-value">{{ averageAdjustment }}%</div>
            </div>
            <div class="stat-item">
              <div class="stat-label">调整产品数</div>
              <div class="stat-value">{{ adjustedProductCount }}</div>
            </div>
          </div>
        </el-card>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { RefreshRight, Check, Upload, Refresh, Edit, Delete, InfoFilled, Download, ArrowDown } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { forecastApi } from '@/api/scm'

// 加载状态
const loading = ref(false)

// 筛选条件
const filter = ref({
  productCategory: '',
  product: '',
  region: '',
  period: [] as string[]
})

// 预测数据
const forecastData = ref<any[]>([])

// 页面加载时获取数据
onMounted(() => {
  fetchForecasts()
})

// 获取预测数据
const fetchForecasts = async () => {
  loading.value = true
  try {
    const params: any = {
      page: 1,
      size: 100 // 暂时获取所有，实际应分页
    }
    if (filter.value.product) {
      params.product = filter.value.product
    }
    if (filter.value.region) {
      params.region = filter.value.region
    }
    // 默认获取当前月，如果没有选择
    if (!filter.value.period || filter.value.period.length === 0) {
      const now = new Date()
      const currentMonth = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`
      params.period = currentMonth
    } else {
      params.period = filter.value.period[0] // 简化处理，取开始月份
    }

    const res = await forecastApi.getForecastList(params)
    // 解析历史销量JSON
    forecastData.value = res.data.list.map((item: any) => {
      let historySales = []
      try {
        historySales = JSON.parse(item.historySales || '[]')
      } catch (e) {
        historySales = []
      }
      return {
        ...item,
        historySales
      }
    })
  } catch (error) {
    console.error('获取预测数据失败:', error)
    forecastData.value = []
  } finally {
    loading.value = false
  }
}

// 运行预测
const runForecast = async () => {
  loading.value = true
  try {
    const now = new Date()
    const currentMonth = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`
    await forecastApi.generateForecast(currentMonth)
    ElMessage.success('预测运行完成')
    fetchForecasts()
  } catch (error) {
    console.error('运行预测失败:', error)
    ElMessage.error('运行预测失败')
  } finally {
    loading.value = false
  }
}

// 保存预测 (批量保存逻辑暂未实现，目前是单行实时保存)
const saveForecast = () => {
  ElMessage.success('更改已保存')
}

// 发布预测
const publishForecast = async () => {
  try {
    const period = filter.value.period?.[0] || `${new Date().getFullYear()}-${String(new Date().getMonth() + 1).padStart(2, '0')}`
    const versionsRes: any = await forecastApi.getForecastVersions(period)
    const versions = versionsRes?.data?.list || versionsRes?.data?.records || []
    const latest = (Array.isArray(versions) ? versions : []).sort((a: any, b: any) => (b.versionNo || 0) - (a.versionNo || 0))[0]
    if (!latest?.id) {
      ElMessage.warning('没有可发布的版本，请先运行预测')
      return
    }
    await forecastApi.publishForecastVersion(latest.id)
    ElMessage.success('预测已发布')
  } catch (e) {
    ElMessage.error('发布失败')
  }
}

// 重置预测
const resetForecast = () => {
  filter.value = { productCategory: '', product: '', region: '', period: [] as string[] }
  fetchForecasts()
}

// 调整值变化时重新计算最终预测并保存
const onAdjustmentChange = async (row: any) => {
  // 最终预测 = 基准预测 * (1 + 促销调整% + 季节调整% + 人工调整%)
  // 前端先计算展示
  const adjustmentRate = (row.promotionAdjustment + row.seasonalAdjustment + row.manualAdjustment) / 100
  row.finalForecast = Math.round(row.baselineForecast * (1 + adjustmentRate))
  
  // 调用后端更新
  try {
    await forecastApi.updateForecast(row.id, {
      promotionAdjustment: row.promotionAdjustment,
      seasonalAdjustment: row.seasonalAdjustment,
      manualAdjustment: row.manualAdjustment
    })
  } catch (error) {
    console.error('更新预测失败:', error)
    ElMessage.error('更新失败')
  }
}

// 计算属性：总预测数量
const totalForecast = computed(() => {
  return forecastData.value.reduce((sum, item) => sum + (item.finalForecast || 0), 0)
})

// 计算属性：平均调整幅度
const averageAdjustment = computed(() => {
  if (forecastData.value.length === 0) return 0
  const totalAdjustment = forecastData.value.reduce((sum, item) => {
    return sum + (item.promotionAdjustment || 0) + (item.seasonalAdjustment || 0) + (item.manualAdjustment || 0)
  }, 0)
  return (totalAdjustment / forecastData.value.length).toFixed(2)
})

// 计算属性：调整产品数
const adjustedProductCount = computed(() => {
  return forecastData.value.filter(item => {
    return (item.promotionAdjustment || 0) !== 0 || (item.seasonalAdjustment || 0) !== 0 || (item.manualAdjustment || 0) !== 0
  }).length
})

// 编辑行
const editRow = (row: any) => {
  ElMessageBox.alert(`产品：${row.productName || row.productCode}`, '行信息', { confirmButtonText: '确定' })
}

// 删除行
const deleteRow = (index: number) => {
  forecastData.value.splice(index, 1)
  ElMessage.success('行已删除')
}

// 处理导出
const handleExport = (command: string) => {
  switch (command) {
    case 'excel':
      exportToExcel()
      break
    case 'csv':
      exportToCsv()
      break
    default:
      break
  }
}

// 导出为Excel
const exportToExcel = () => {
  if (forecastData.value.length === 0) {
    ElMessage.warning('没有数据可以导出')
    return
  }
  const headers = ['产品名称', '产品编码', '销售区域', '基准预测', '促销调整', '季节调整', '人工调整', '最终预测']
  const rows = forecastData.value.map(item => [
    item.productName,
    item.productCode,
    item.region,
    item.baselineForecast,
    item.promotionAdjustment,
    item.seasonalAdjustment,
    item.manualAdjustment,
    item.finalForecast
  ])
  const table = `<table><thead><tr>${headers.map(h => `<th>${h}</th>`).join('')}</tr></thead><tbody>${rows
    .map(r => `<tr>${r.map(c => `<td>${c ?? ''}</td>`).join('')}</tr>`)
    .join('')}</tbody></table>`
  const blob = new Blob([table], { type: 'application/vnd.ms-excel;charset=utf-8' })
  const link = document.createElement('a')
  const url = URL.createObjectURL(blob)
  link.href = url
  link.download = `forecast_${new Date().toISOString().slice(0, 10)}.xls`
  link.click()
  URL.revokeObjectURL(url)
  ElMessage.success('Excel导出成功')
}

// 导出为CSV
const exportToCsv = () => {
  if (forecastData.value.length === 0) {
    ElMessage.warning('没有数据可以导出')
    return
  }
  
  // 构建CSV内容
  const headers = ['产品名称', '产品编码', '销售区域', '基准预测', '促销调整', '季节调整', '人工调整', '最终预测']
  const rows = forecastData.value.map(item => [
    item.productName,
    item.productCode,
    item.region,
    item.baselineForecast,
    item.promotionAdjustment,
    item.seasonalAdjustment,
    item.manualAdjustment,
    item.finalForecast
  ])
  
  // 转换为CSV格式
  const csvContent = [
    headers.join(','),
    ...rows.map(row => row.join(','))
  ].join('\n')
  
  // 创建下载链接
  const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
  const link = document.createElement('a')
  const url = URL.createObjectURL(blob)
  link.setAttribute('href', url)
  link.setAttribute('download', `forecast_${new Date().toISOString().slice(0, 10)}.csv`)
  link.style.visibility = 'hidden'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  
  ElMessage.success('CSV导出成功')
}
</script>

<style scoped>
.forecast-worksheet {
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
}

/* 筛选条件 */
.filter-section {
  padding: 10px 0;
}

/* 表格样式 */
.table-section {
  max-height: 600px;
  overflow-y: auto;
}

/* 列标题 */
.column-header {
  display: flex;
  align-items: center;
  gap: 5px;
}

.info-icon {
  color: #606266;
  font-size: 14px;
  cursor: help;
}

/* 历史销量 */
.history-sales {
  display: flex;
  flex-direction: column;
  gap: 2px;
  align-items: center;
}

.sales-item {
  font-size: 12px;
  color: #606266;
}

/* 统计信息 */
.stats-section {
  margin-top: 20px;
}

.stats-content {
  display: flex;
  gap: 40px;
  padding: 10px 0;
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
  .stats-content {
    gap: 20px;
  }
  
  .stat-value {
    font-size: 20px;
  }
}

@media (max-width: 768px) {
  .forecast-worksheet {
    padding: 0 10px 10px;
  }
  
  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .header-actions {
    flex-wrap: wrap;
  }
  
  .filter-section {
    padding: 10px 0 0;
  }
  
  .stats-content {
    flex-wrap: wrap;
    justify-content: center;
  }
  
  .stat-item {
    min-width: 120px;
  }
}
</style>
