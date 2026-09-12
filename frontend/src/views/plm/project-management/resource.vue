<template>
  <div class="resource-view">
    <h3>资源负载</h3>
    
    <!-- 筛选条件 -->
    <div class="filters">
      <el-card shadow="hover">
        <el-form :model="filterForm" inline>
          <el-form-item label="资源类型">
            <el-select v-model="filterForm.resourceType" placeholder="请选择资源类型">
              <el-option label="所有资源" value=""></el-option>
              <el-option label="人力资源" value="human"></el-option>
              <el-option label="设备资源" value="equipment"></el-option>
              <el-option label="材料资源" value="material"></el-option>
              <el-option label="资金资源" value="fund"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="时间范围">
            <el-date-picker
              v-model="filterForm.dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
            ></el-date-picker>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="applyFilters">查询</el-button>
            <el-button @click="resetFilters">重置</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
    
    <!-- 资源负载图表 -->
    <div class="resource-charts">
      <!-- 资源使用情况统计 -->
      <el-card shadow="hover" class="chart-card">
        <template #header>
          <div class="card-header">
            <span>资源使用情况统计</span>
          </div>
        </template>
        <div class="chart-content">
          <div ref="resourceStatsChart" class="chart"></div>
        </div>
      </el-card>
      
      <!-- 资源负载趋势 -->
      <el-card shadow="hover" class="chart-card">
        <template #header>
          <div class="card-header">
            <span>资源负载趋势</span>
          </div>
        </template>
        <div class="chart-content">
          <div ref="resourceTrendChart" class="chart"></div>
        </div>
      </el-card>
    </div>
    
    <!-- 资源列表 -->
    <el-card shadow="hover" class="resource-list-card">
      <template #header>
        <div class="card-header">
          <span>资源列表</span>
          <el-button type="primary" size="small">新增资源</el-button>
        </div>
      </template>
      <div class="resource-list">
        <el-table :data="pagedResources" style="width: 100%">
          <el-table-column prop="name" label="资源名称" min-width="150"></el-table-column>
          <el-table-column prop="type" label="资源类型" min-width="100">
            <template #default="scope">
              <el-tag :type="getResourceTypeTagType(scope.row.type)">{{ scope.row.typeLabel }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" min-width="100">
            <template #default="scope">
              <el-tag :type="getResourceStatusTagType(scope.row.status)">{{ scope.row.statusLabel }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="totalCapacity" label="总容量" min-width="100"></el-table-column>
          <el-table-column prop="usedCapacity" label="已使用" min-width="100"></el-table-column>
          <el-table-column prop="usageRate" label="使用率" min-width="120">
            <template #default="scope">
              <el-progress :percentage="scope.row.usageRate" :stroke-width="10"></el-progress>
            </template>
          </el-table-column>
          <el-table-column prop="availableCapacity" label="可用容量" min-width="120"></el-table-column>
          <el-table-column label="操作" min-width="150" fixed="right">
            <template #default="scope">
              <el-button type="primary" size="small" @click="viewResource(scope.row.id)">查看</el-button>
              <el-button size="small" @click="editResource(scope.row.id)">编辑</el-button>
              <el-dropdown>
                <el-button size="small">
                  更多 <el-icon class="el-icon--right"><ArrowDown /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item>分配资源</el-dropdown-item>
                    <el-dropdown-item>查看负载</el-dropdown-item>
                    <el-dropdown-item>导出数据</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </template>
          </el-table-column>
        </el-table>
        
        <!-- 分页 -->
        <div class="pagination">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="totalResources"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          ></el-pagination>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch, computed } from 'vue'
import { ArrowDown } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { useProjectManagementStore } from '@/stores/plm/projectManagement'
import { ElMessage, ElMessageBox } from 'element-plus'

// 初始化项目管理状态
const projectStore = useProjectManagementStore()

// 默认日期范围：当年全年
const defaultYear = new Date().getFullYear()
const defaultDateRange = [`${defaultYear}-01-01`, `${defaultYear}-12-31`]

// 筛选表单
const filterForm = ref({
  resourceType: '',
  dateRange: [...defaultDateRange]
})

// 从状态管理获取资源负载数据
const resourceLoadData = computed(() => projectStore.resourceLoad)

const resources = computed(() => {
  const rows = Array.isArray(resourceLoadData.value) ? resourceLoadData.value : []
  const start = filterForm.value.dateRange?.[0] || ''
  const end = filterForm.value.dateRange?.[1] || ''
  const type = filterForm.value.resourceType || ''
  const filtered = rows.filter((r: any) => {
    if (type && String(r.resourceId || '').startsWith(type) === false) return false
    if (start && String(r.date || '') < start) return false
    if (end && String(r.date || '') > end) return false
    return true
  })

  const map = new Map<string, any>()
  filtered.forEach((r: any) => {
    const key = String(r.resourceId)
    if (!map.has(key)) {
      map.set(key, { id: key, name: r.resourceName, loads: [] as number[] })
    }
    map.get(key).loads.push(Number(r.load || 0))
  })

  const result = Array.from(map.values()).map(v => {
    const avg = v.loads.length ? v.loads.reduce((a: number, b: number) => a + b, 0) / v.loads.length : 0
    const totalCapacity = 100
    const usedCapacity = Math.round(avg)
    const usageRate = usedCapacity
    const availableCapacity = Math.max(totalCapacity - usedCapacity, 0)
    const status = usageRate >= 90 ? 'busy' : 'active'
    return {
      id: v.id,
      name: v.name,
      type: 'human',
      typeLabel: '人力资源',
      status,
      statusLabel: status === 'busy' ? '繁忙' : '可用',
      totalCapacity,
      usedCapacity,
      usageRate,
      availableCapacity
    }
  })
  return result
})

const totalResources = computed(() => resources.value.length)

const pagedResources = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return resources.value.slice(start, end)
})

// 分页参数
const currentPage = ref(1)
const pageSize = ref(10)

// 图表引用
const resourceStatsChart = ref<HTMLElement | null>(null)
const resourceTrendChart = ref<HTMLElement | null>(null)

// 图表实例
let resourceStatsChartInstance: echarts.ECharts | null = null
let resourceTrendChartInstance: echarts.ECharts | null = null

// 资源类型标签类型映射
const getResourceTypeTagType = (type: string) => {
  const typeMap: Record<string, string> = {
    'human': 'info',
    'equipment': 'success',
    'material': 'info',
    'fund': 'warning'
  }
  return typeMap[type] || 'info'
}

// 资源状态标签类型映射
const getResourceStatusTagType = (status: string) => {
  const statusMap: Record<string, string> = {
    'active': 'success',
    'busy': 'danger',
    'maintenance': 'warning',
    'inactive': 'info'
  }
  return statusMap[status] || 'info'
}

// 应用筛选条件
const applyFilters = async () => {
  currentPage.value = 1
  await projectStore.fetchResourceLoad({
    startDate: filterForm.value.dateRange?.[0],
    endDate: filterForm.value.dateRange?.[1]
  })
}

// 重置筛选条件
const resetFilters = async () => {
  filterForm.value.resourceType = ''
  filterForm.value.dateRange = [...defaultDateRange]
  currentPage.value = 1
  await projectStore.fetchResourceLoad({
    startDate: filterForm.value.dateRange?.[0],
    endDate: filterForm.value.dateRange?.[1]
  })
}

// 查看资源详情
const viewResource = (id: number) => {
  const row = resources.value.find((r: any) => String(r.id) === String(id))
  if (!row) return
  ElMessageBox.alert(JSON.stringify(row, null, 2), '资源详情', { confirmButtonText: '确定' })
}

// 编辑资源
const editResource = (id: number) => {
  ElMessage.info('资源编辑功能需要与资源主数据模块联动')
}

// 分页大小变化
const handleSizeChange = (size: number) => {
  pageSize.value = size
}

// 当前页码变化
const handleCurrentChange = (page: number) => {
  currentPage.value = page
}

// 初始化资源使用情况统计图表
const initResourceStatsChart = () => {
  if (!resourceStatsChart.value) return
  
  resourceStatsChartInstance = echarts.init(resourceStatsChart.value)

  const rows = Array.isArray(resourceLoadData.value) ? resourceLoadData.value : []
  const avg = rows.length ? rows.reduce((a: number, b: any) => a + Number(b.load || 0), 0) / rows.length : 0
  const used = Math.max(Math.round(avg), 0)
  const available = Math.max(100 - used, 0)
  
  const option = {
    title: {
      text: '资源使用情况统计',
      left: 'center'
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
        name: '资源使用情况',
        type: 'pie',
        radius: '50%',
        data: [
          { value: used, name: '已使用' },
          { value: available, name: '可用' }
        ],
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
  
  resourceStatsChartInstance.setOption(option)
}

// 初始化资源负载趋势图表
const initResourceTrendChart = () => {
  if (!resourceTrendChart.value) return
  
  resourceTrendChartInstance = echarts.init(resourceTrendChart.value)

  const rows = Array.isArray(resourceLoadData.value) ? resourceLoadData.value : []
  const monthMap = new Map<string, { sum: number; count: number }>()
  rows.forEach((r: any) => {
    const date = String(r.date || '').slice(0, 7)
    if (!date) return
    if (!monthMap.has(date)) monthMap.set(date, { sum: 0, count: 0 })
    const v = monthMap.get(date)!
    v.sum += Number(r.load || 0)
    v.count += 1
  })
  const months = Array.from(monthMap.keys()).sort()
  const seriesData = months.map(m => {
    const v = monthMap.get(m)!
    return v.count ? Math.round(v.sum / v.count) : 0
  })
  
  const option = {
    title: {
      text: '资源负载趋势',
      left: 'center'
    },
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['负载'],
      bottom: 0
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
      data: months
    },
    yAxis: {
      type: 'value',
      name: '使用率 (%)'
    },
    series: [
      {
        name: '负载',
        type: 'line',
        stack: 'Total',
        data: seriesData
      }
    ]
  }
  
  resourceTrendChartInstance.setOption(option)
}

// 监听窗口大小变化，调整图表大小
const handleResize = () => {
  resourceStatsChartInstance?.resize()
  resourceTrendChartInstance?.resize()
}

// 生命周期钩子
onMounted(async () => {
  // 加载资源负载数据
  await projectStore.fetchResourceLoad()
  
  // 初始化图表
  initResourceStatsChart()
  initResourceTrendChart()
  
  // 监听窗口大小变化
  window.addEventListener('resize', handleResize)
  
  // 监听资源负载数据变化，更新图表
  watch(resourceLoadData, () => {
    updateCharts()
  })
})

// 更新图表数据
const updateCharts = () => {
  if (resourceStatsChartInstance && resourceTrendChartInstance) {
    const rows = Array.isArray(resourceLoadData.value) ? resourceLoadData.value : []
    const avg = rows.length ? rows.reduce((a: number, b: any) => a + Number(b.load || 0), 0) / rows.length : 0
    const used = Math.max(Math.round(avg), 0)
    const available = Math.max(100 - used, 0)

    const monthMap = new Map<string, { sum: number; count: number }>()
    rows.forEach((r: any) => {
      const date = String(r.date || '').slice(0, 7)
      if (!date) return
      if (!monthMap.has(date)) monthMap.set(date, { sum: 0, count: 0 })
      const v = monthMap.get(date)!
      v.sum += Number(r.load || 0)
      v.count += 1
    })
    const months = Array.from(monthMap.keys()).sort()
    const seriesData = months.map(m => {
      const v = monthMap.get(m)!
      return v.count ? Math.round(v.sum / v.count) : 0
    })

    // 更新资源使用情况统计图表
    resourceStatsChartInstance.setOption({
      series: [
        {
          data: [
            { value: used, name: '已使用' },
            { value: available, name: '可用' }
          ]
        }
      ]
    })
    
    resourceTrendChartInstance.setOption({
      xAxis: { data: months },
      series: [
        {
          data: seriesData
        }
      ]
    })
  }
}

// 清理资源
const cleanup = () => {
  // 移除事件监听
  window.removeEventListener('resize', handleResize)
  
  // 销毁图表实例
  resourceStatsChartInstance?.dispose()
  resourceTrendChartInstance?.dispose()
}

// 组件卸载时清理资源
onUnmounted(() => {
  cleanup()
})
</script>

<style scoped lang="scss">
.resource-view {
  padding: 20px;
  height: 100%;
  overflow: auto;

  h3 {
    margin-bottom: 20px;
    color: #333;
    font-size: 1.3rem;
  }

  /* 筛选条件 */
  .filters {
    margin-bottom: 20px;
  }

  /* 资源负载图表 */
  .resource-charts {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(500px, 1fr));
    gap: 20px;
    margin-bottom: 20px;
  }

  .chart-card {
    height: 400px;
    display: flex;
    flex-direction: column;
  }

  .chart-content {
    flex: 1;
    display: flex;
    flex-direction: column;
  }

  .chart {
    flex: 1;
    min-height: 300px;
  }

  /* 资源列表卡片 */
  .resource-list-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
  }

  .resource-list {
    padding: 16px 0;
  }

  /* 分页 */
  .pagination {
    display: flex;
    justify-content: flex-end;
    margin-top: 16px;
  }

  /* 响应式设计 */
  @media (max-width: 1024px) {
    padding: 16px;

    .resource-charts {
      grid-template-columns: 1fr;
      gap: 16px;
    }

    .chart-card {
      height: 350px;
    }
  }

  @media (max-width: 768px) {
    padding: 12px;

    h3 {
      font-size: 1.1rem;
      margin-bottom: 16px;
    }

    .filters {
      margin-bottom: 16px;
    }

    .resource-charts {
      gap: 12px;
    }

    .chart-card {
      height: 300px;
    }

    .chart {
      min-height: 200px;
    }
  }
}
</style>
