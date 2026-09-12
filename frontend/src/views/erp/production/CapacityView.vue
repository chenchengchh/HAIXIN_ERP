<template>
  <div class="capacity-view">
    <el-card class="module-card">
      <template #header>
        <div class="card-header">
          <el-icon><Cpu /></el-icon>
          <span>产能管理</span>
        </div>
      </template>
      
      <el-tabs v-model="activeTab" type="card" class="capacity-tabs">
        <!-- 产能数据标签页 -->
        <el-tab-pane label="产能数据" name="capacity-data">
          <div class="tab-content">
            <!-- 产能数据图表 -->
            <el-card shadow="hover" class="chart-card">
              <template #header>
                <div class="card-header">
                  <span>产能利用率趋势</span>
                </div>
              </template>
              <div class="chart-container">
                <!-- 这里可以集成ECharts或其他图表库 -->
                <div class="chart-placeholder">
                  <el-empty description="产能利用率趋势图表将在这里显示" />
                </div>
              </div>
            </el-card>
            
            <!-- 产能数据列表 -->
            <TableComponent
              :data="capacityData"
              :columns="capacityDataColumns"
              :total="capacityDataTotal"
              :loading="capacityDataLoading"
              :show-index="true"
              :show-action="true"
              :actions="capacityDataActions"
              :table-actions="capacityDataTableActions"
              :filters="capacityDataFilters"
              :show-filter="true"
              @search="handleCapacityDataSearch"
              @size-change="handleCapacityDataSizeChange"
              @current-change="handleCapacityDataCurrentChange"
            >
              <!-- 产能利用率列自定义 -->
              <template #utilizationRate="{ row }">
                <el-progress
                  :percentage="row.utilization_rate"
                  :color="getUtilizationRateColor(row.utilization_rate)"
                  :stroke-width="8"
                  text-inside
                />
              </template>
            </TableComponent>
          </div>
        </el-tab-pane>
        
        <!-- 生产负荷标签页 -->
        <el-tab-pane label="生产负荷" name="production-load">
          <div class="tab-content">
            <!-- 生产负荷图表 -->
            <el-card shadow="hover" class="chart-card">
              <template #header>
                <div class="card-header">
                  <span>部门生产负荷分布</span>
                </div>
              </template>
              <div class="chart-container">
                <!-- 这里可以集成ECharts或其他图表库 -->
                <div class="chart-placeholder">
                  <el-empty description="部门生产负荷分布图表将在这里显示" />
                </div>
              </div>
            </el-card>
            
            <!-- 生产负荷列表 -->
            <TableComponent
              :data="productionLoadData"
              :columns="productionLoadColumns"
              :total="productionLoadTotal"
              :loading="productionLoadLoading"
              :show-index="true"
              :show-action="true"
              :actions="productionLoadActions"
              :table-actions="productionLoadTableActions"
              :filters="productionLoadFilters"
              :show-filter="true"
              @search="handleProductionLoadSearch"
              @size-change="handleProductionLoadSizeChange"
              @current-change="handleProductionLoadCurrentChange"
            >
              <!-- 负荷率列自定义 -->
              <template #loadRate="{ row }">
                <el-progress
                  :percentage="row.load_rate"
                  :color="getLoadRateColor(row.load_rate)"
                  :stroke-width="8"
                  text-inside
                />
              </template>
            </TableComponent>
          </div>
        </el-tab-pane>
        
        <!-- 设备管理标签页 -->
        <el-tab-pane label="设备管理" name="equipment">
          <div class="tab-content">
            <!-- 设备状态概览 -->
            <el-card shadow="hover" class="status-card">
              <template #header>
                <div class="card-header">
                  <span>设备状态概览</span>
                </div>
              </template>
              <div class="equipment-status-overview">
                <div class="status-item">
                  <el-statistic title="在线设备" :value="equipmentStatusOverview.online" />
                </div>
                <div class="status-item">
                  <el-statistic title="离线设备" :value="equipmentStatusOverview.offline" />
                </div>
                <div class="status-item">
                  <el-statistic title="运行中" :value="equipmentStatusOverview.running" />
                </div>
                <div class="status-item">
                  <el-statistic title="停机中" :value="equipmentStatusOverview.stopped" />
                </div>
              </div>
            </el-card>
            
            <!-- 设备列表 -->
            <TableComponent
              :data="equipmentList"
              :columns="equipmentColumns"
              :total="equipmentTotal"
              :loading="equipmentLoading"
              :show-index="true"
              :show-action="true"
              :actions="equipmentActions"
              :table-actions="equipmentTableActions"
              :filters="equipmentFilters"
              :show-filter="true"
              @search="handleEquipmentSearch"
              @size-change="handleEquipmentSizeChange"
              @current-change="handleEquipmentCurrentChange"
            >
              <!-- 设备状态列自定义 -->
              <template #equipmentStatus="{ row }">
                <el-tag
                  :type="getEquipmentStatusType(row.status)"
                  size="small"
                >
                  {{ getEquipmentStatusLabel(row.status) }}
                </el-tag>
              </template>
            </TableComponent>
          </div>
        </el-tab-pane>
        
        <!-- 产能规划标签页 -->
        <el-tab-pane label="产能规划" name="capacity-planning">
          <div class="tab-content">
            <!-- 产能规划表单 -->
            <el-form
              ref="planningFormRef"
              :model="planningForm"
              label-width="120px"
              class="planning-form"
            >
              <el-row :gutter="20">
                <el-col :span="8">
                  <el-form-item label="规划周期" prop="planning_period" required>
                    <el-select
                      v-model="planningForm.planning_period"
                      placeholder="选择规划周期"
                      style="width: 100%"
                    >
                      <el-option label="周" value="week" />
                      <el-option label="月" value="month" />
                      <el-option label="季度" value="quarter" />
                      <el-option label="年度" value="year" />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item label="开始日期" prop="start_date" required>
                    <el-date-picker
                      v-model="planningForm.start_date"
                      type="date"
                      placeholder="选择开始日期"
                      style="width: 100%"
                    />
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item label="结束日期" prop="end_date" required>
                    <el-date-picker
                      v-model="planningForm.end_date"
                      type="date"
                      placeholder="选择结束日期"
                      style="width: 100%"
                    />
                  </el-form-item>
                </el-col>
              </el-row>
              
              <el-row :gutter="20">
                <el-col :span="8">
                  <el-form-item label="考虑加班" prop="consider_overtime">
                    <el-switch
                      v-model="planningForm.consider_overtime"
                      active-text="考虑"
                      inactive-text="不考虑"
                    />
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item label="考虑外协" prop="consider_outsource">
                    <el-switch
                      v-model="planningForm.consider_outsource"
                      active-text="考虑"
                      inactive-text="不考虑"
                    />
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item label="产能缓冲率" prop="capacity_buffer_rate">
                    <el-slider
                      v-model="planningForm.capacity_buffer_rate"
                      :min="0"
                      :max="50"
                      :step="1"
                      show-input
                    />
                  </el-form-item>
                </el-col>
              </el-row>
              
              <div class="form-actions">
                <el-button type="primary" @click="handleRunCapacityPlanning" :loading="planningLoading">
                  <el-icon><VideoPlay /></el-icon>
                  运行产能规划
                </el-button>
                <el-button @click="handleResetPlanningForm">重置</el-button>
              </div>
            </el-form>
            
            <!-- 规划结果 -->
            <div v-if="planningResults.length > 0" class="planning-results">
              <el-card shadow="hover" class="results-card">
                <template #header>
                  <div class="card-header">
                    <span>产能规划结果</span>
                  </div>
                </template>
                <TableComponent
                  :data="planningResults"
                  :columns="planningResultColumns"
                  :total="planningResultsTotal"
                  :loading="planningResultsLoading"
                  :show-index="true"
                >
                  <!-- 建议类型列自定义 -->
                  <template #suggestionType="{ row }">
                    <el-tag
                      :type="getSuggestionTypeType(row.suggestion_type)"
                      size="small"
                    >
                      {{ getSuggestionTypeLabel(row.suggestion_type) }}
                    </el-tag>
                  </template>
                </TableComponent>
              </el-card>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Cpu, RefreshLeft, View, Download, VideoPlay } from '@element-plus/icons-vue'
import { TableComponent } from '../../../components/base'
import { unwrapPageResponse, unwrapResponseData } from '../../../api'
import { erpApi } from '../../../api/erp'
import { ErrorHandler } from '../../../utils/error-handler'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as XLSX from 'xlsx'
import type { CapacityData, ProductionLoad, Equipment, CapacityPlanningParams, CapacityPlanningResult } from '../../../types/erp/production'

// 响应式数据
const activeTab = ref('capacity-data')

// 产能数据相关
const capacityData = ref<CapacityData[]>([])
const capacityDataTotal = ref(0)
const capacityDataLoading = ref(false)
const capacityDataPage = ref(1)
const capacityDataSize = ref(10)

// 生产负荷相关
const productionLoadData = ref<ProductionLoad[]>([])
const productionLoadTotal = ref(0)
const productionLoadLoading = ref(false)
const productionLoadPage = ref(1)
const productionLoadSize = ref(10)

// 设备相关
const equipmentList = ref<Equipment[]>([])
const equipmentTotal = ref(0)
const equipmentLoading = ref(false)
const equipmentPage = ref(1)
const equipmentSize = ref(10)
const equipmentStatusOverview = ref({
  online: 0,
  offline: 0,
  running: 0,
  stopped: 0
})

// 产能规划相关
const planningFormRef = ref<any>(null)
const planningForm = ref<CapacityPlanningParams>({
  planning_period: 'month',
  start_date: new Date(),
  end_date: new Date(new Date().setMonth(new Date().getMonth() + 1)),
  consider_overtime: false,
  consider_outsource: false,
  capacity_buffer_rate: 10
})
const planningLoading = ref(false)
const planningResults = ref<CapacityPlanningResult[]>([])
const planningResultsTotal = ref(0)
const planningResultsLoading = ref(false)

// 产能数据筛选条件
const capacityDataFilters = [
  { prop: 'department_name', label: '部门名称', type: 'input' as 'input', placeholder: '请输入部门名称' },
  { prop: 'work_center_name', label: '工作中心', type: 'input' as 'input', placeholder: '请输入工作中心' },
  { prop: 'period', label: '统计周期', type: 'select' as 'select', options: [
    { label: '日', value: 'day' },
    { label: '周', value: 'week' },
    { label: '月', value: 'month' }
  ]},
  { prop: 'start_date', label: '统计日期', type: 'daterange' as 'daterange' }
] as any

// 产能数据列配置
const capacityDataColumns = [
  { prop: 'department_name', label: '部门名称', width: 150 },
  { prop: 'work_center_name', label: '工作中心', width: 150 },
  { prop: 'resource_name', label: '资源名称', width: 150 },
  { prop: 'available_capacity', label: '可用产能', width: 120, align: 'right' },
  { prop: 'used_capacity', label: '已用产能', width: 120, align: 'right' },
  { prop: 'utilization_rate', label: '利用率', width: 180, slotName: 'utilizationRate' },
  { prop: 'period', label: '统计周期', width: 120 },
  { prop: 'period_date', label: '统计日期', width: 150 },
  { prop: 'remark', label: '备注', minWidth: 200 }
]

// 产能数据操作按钮
const capacityDataActions = [
  {
    text: '查看详情',
    type: 'primary',
    size: 'small',
    icon: View,
    handler: (row: any) => {
      ElMessageBox.alert(`<pre>${JSON.stringify(row, null, 2)}</pre>`, '产能详情', { dangerouslyUseHTMLString: true })
    }
  }
]

// 产能数据表格操作按钮
const capacityDataTableActions = [
  {
    key: 'export',
    text: '导出数据',
    type: 'primary',
    icon: Download,
    handler: () => {
      handleExportCapacityData()
    }
  },
  {
    key: 'refresh',
    text: '刷新',
    icon: RefreshLeft,
    handler: () => {
      handleCapacityDataSearch()
    }
  }
]

// 生产负荷筛选条件
const productionLoadFilters = [
  { prop: 'department_name', label: '部门名称', type: 'input' as 'input', placeholder: '请输入部门名称' },
  { prop: 'work_center_name', label: '工作中心', type: 'input' as 'input', placeholder: '请输入工作中心' },
  { prop: 'product_name', label: '产品名称', type: 'input' as 'input', placeholder: '请输入产品名称' },
  { prop: 'load_status', label: '负荷状态', type: 'select' as 'select', options: [
    { label: '低负荷', value: 'low' },
    { label: '正常', value: 'normal' },
    { label: '高负荷', value: 'high' }
  ]},
  { prop: 'period', label: '统计周期', type: 'select' as 'select', options: [
    { label: '日', value: 'day' },
    { label: '周', value: 'week' },
    { label: '月', value: 'month' }
  ]}
] as any

// 生产负荷列配置
const productionLoadColumns = [
  { prop: 'department_name', label: '部门名称', width: 150 },
  { prop: 'work_center_name', label: '工作中心', width: 150 },
  { prop: 'product_name', label: '产品名称', width: 200 },
  { prop: 'planned_load', label: '计划负荷', width: 120, align: 'right' },
  { prop: 'available_capacity', label: '可用产能', width: 120, align: 'right' },
  { prop: 'load_rate', label: '负荷率', width: 180, slotName: 'loadRate' },
  { prop: 'load_status', label: '负荷状态', width: 120 },
  { prop: 'period', label: '统计周期', width: 120 },
  { prop: 'period_date', label: '统计日期', width: 150 }
]

// 生产负荷操作按钮
const productionLoadActions = [
  {
    text: '查看详情',
    type: 'primary',
    size: 'small',
    icon: View,
    handler: (row: any) => {
      ElMessageBox.alert(`<pre>${JSON.stringify(row, null, 2)}</pre>`, '生产负荷详情', { dangerouslyUseHTMLString: true })
    }
  }
]

// 生产负荷表格操作按钮
const productionLoadTableActions = [
  {
    key: 'export',
    text: '导出数据',
    type: 'primary',
    icon: Download,
    handler: () => {
      handleExportProductionLoad()
    }
  },
  {
    key: 'refresh',
    text: '刷新',
    icon: RefreshLeft,
    handler: () => {
      handleProductionLoadSearch()
    }
  }
]

// 设备筛选条件
const equipmentFilters = [
  { prop: 'equipment_name', label: '设备名称', type: 'input' as 'input', placeholder: '请输入设备名称' },
  { prop: 'equipment_code', label: '设备编码', type: 'input' as 'input', placeholder: '请输入设备编码' },
  { prop: 'status', label: '设备状态', type: 'select' as 'select', options: [
    { label: '在线', value: 'online' },
    { label: '离线', value: 'offline' },
    { label: '运行中', value: 'running' },
    { label: '停机中', value: 'stopped' }
  ]},
  { prop: 'department_name', label: '所属部门', type: 'input' as 'input', placeholder: '请输入所属部门' }
] as any

// 设备列配置
const equipmentColumns = [
  { prop: 'equipment_code', label: '设备编码', width: 150 },
  { prop: 'equipment_name', label: '设备名称', width: 200 },
  { prop: 'department_name', label: '所属部门', width: 150 },
  { prop: 'work_center_name', label: '工作中心', width: 150 },
  { prop: 'model', label: '设备型号', width: 150 },
  { prop: 'capacity', label: '额定产能', width: 120, align: 'right' },
  { prop: 'status', label: '设备状态', width: 120, slotName: 'equipmentStatus' },
  { prop: 'last_maintain_date', label: '上次维护日期', width: 150 },
  { prop: 'remark', label: '备注', minWidth: 200 }
]

// 设备操作按钮
const equipmentActions = [
  {
    text: '查看详情',
    type: 'primary',
    size: 'small',
    icon: View,
    handler: (row: any) => {
      ElMessageBox.alert(`<pre>${JSON.stringify(row, null, 2)}</pre>`, '设备详情', { dangerouslyUseHTMLString: true })
    }
  }
]

// 设备表格操作按钮
const equipmentTableActions = [
  {
    key: 'refresh',
    text: '刷新状态',
    type: 'primary',
    icon: RefreshLeft,
    handler: () => {
      handleEquipmentSearch()
      handleUpdateEquipmentStatus()
    }
  }
]

// 产能规划结果列配置
const planningResultColumns = [
  { prop: 'department_name', label: '部门名称', width: 150 },
  { prop: 'work_center_name', label: '工作中心', width: 150 },
  { prop: 'resource_name', label: '资源名称', width: 150 },
  { prop: 'planned_load', label: '计划负荷', width: 120, align: 'right' },
  { prop: 'available_capacity', label: '可用产能', width: 120, align: 'right' },
  { prop: 'capacity_gap', label: '产能缺口', width: 120, align: 'right' },
  { prop: 'suggestion_type', label: '建议类型', width: 120, slotName: 'suggestionType' },
  { prop: 'suggestion_content', label: '建议内容', minWidth: 200 },
  { prop: 'remark', label: '备注', minWidth: 200 }
]

// 初始加载
onMounted(() => {
  handleCapacityDataSearch()
  handleProductionLoadSearch()
  handleEquipmentSearch()
  handleUpdateEquipmentStatus()
})

// 获取产能数据
const getCapacityData = async (params: any) => {
  try {
    capacityDataLoading.value = true
    const requestParams = normalizeDateRangeParams({
      page: params.page || capacityDataPage.value,
      size: params.size || capacityDataSize.value,
      ...params
    })
    const result = await erpApi.production.getCapacity(requestParams)
    const page = unwrapPageResponse<CapacityData>(result)
    capacityData.value = page.list
    capacityDataTotal.value = page.total || page.list.length || 0
    capacityDataPage.value = params.page || capacityDataPage.value
    capacityDataSize.value = params.size || capacityDataSize.value
  } catch (error) {
    ErrorHandler.handleApiError(error)
    // 显示虚拟数据或空数据
    capacityData.value = []
    capacityDataTotal.value = 0
  } finally {
    capacityDataLoading.value = false
  }
}

// 搜索产能数据
const handleCapacityDataSearch = (params: any = {}) => {
  getCapacityData(params)
}

// 产能数据分页大小改变
const handleCapacityDataSizeChange = (size: number) => {
  capacityDataSize.value = size
  getCapacityData({ page: capacityDataPage.value, size })
}

// 产能数据页码改变
const handleCapacityDataCurrentChange = (page: number) => {
  capacityDataPage.value = page
  getCapacityData({ page, size: capacityDataSize.value })
}

// 导出产能数据
const handleExportCapacityData = () => {
  try {
    const headers = ['部门名称', '工作中心', '资源名称', '可用产能', '已用产能', '利用率(%)', '统计周期', '统计日期', '备注']
    const worksheetData = [
      headers,
      ...capacityData.value.map((row: any) => [
        row.department_name,
        row.work_center_name,
        row.resource_name,
        row.available_capacity,
        row.used_capacity,
        row.utilization_rate,
        row.period,
        row.period_date,
        row.remark || ''
      ])
    ]
    const worksheet = XLSX.utils.aoa_to_sheet(worksheetData)
    const workbook = XLSX.utils.book_new()
    XLSX.utils.book_append_sheet(workbook, worksheet, '产能数据')
    XLSX.writeFile(workbook, `产能数据_${new Date().toISOString().slice(0, 10)}.xlsx`)
    ElMessage.success('导出成功')
  } catch (error) {
    ErrorHandler.handleApiError(error)
  }
}

// 获取生产负荷数据
const getProductionLoadData = async (params: any) => {
  try {
    productionLoadLoading.value = true
    const result = await erpApi.production.getProductionLoad({
      page: params.page || productionLoadPage.value,
      size: params.size || productionLoadSize.value,
      ...params
    })
    const page = unwrapPageResponse<ProductionLoad>(result)
    productionLoadData.value = page.list
    productionLoadTotal.value = page.total || page.list.length || 0
    productionLoadPage.value = params.page || productionLoadPage.value
    productionLoadSize.value = params.size || productionLoadSize.value
  } catch (error) {
    ErrorHandler.handleApiError(error)
    // 显示虚拟数据或空数据
    productionLoadData.value = []
    productionLoadTotal.value = 0
  } finally {
    productionLoadLoading.value = false
  }
}

// 搜索生产负荷数据
const handleProductionLoadSearch = (params: any = {}) => {
  getProductionLoadData(params)
}

// 生产负荷数据分页大小改变
const handleProductionLoadSizeChange = (size: number) => {
  productionLoadSize.value = size
  getProductionLoadData({ page: productionLoadPage.value, size })
}

// 生产负荷数据页码改变
const handleProductionLoadCurrentChange = (page: number) => {
  productionLoadPage.value = page
  getProductionLoadData({ page, size: productionLoadSize.value })
}

// 导出生产负荷数据
const handleExportProductionLoad = () => {
  try {
    const headers = ['部门名称', '工作中心', '产品名称', '计划负荷', '可用产能', '负荷率(%)', '负荷状态', '统计周期', '统计日期']
    const worksheetData = [
      headers,
      ...productionLoadData.value.map((row: any) => [
        row.department_name,
        row.work_center_name,
        row.product_name,
        row.planned_load,
        row.available_capacity,
        row.load_rate,
        row.load_status,
        row.period,
        row.period_date
      ])
    ]
    const worksheet = XLSX.utils.aoa_to_sheet(worksheetData)
    const workbook = XLSX.utils.book_new()
    XLSX.utils.book_append_sheet(workbook, worksheet, '生产负荷')
    XLSX.writeFile(workbook, `生产负荷_${new Date().toISOString().slice(0, 10)}.xlsx`)
    ElMessage.success('导出成功')
  } catch (error) {
    ErrorHandler.handleApiError(error)
  }
}

// 获取设备列表
const getEquipmentList = async (params: any) => {
  try {
    equipmentLoading.value = true
    const result = await erpApi.production.getEquipmentList({
      page: params.page || equipmentPage.value,
      size: params.size || equipmentSize.value,
      ...params
    })
    const page = unwrapPageResponse<Equipment>(result)
    equipmentList.value = page.list
    equipmentTotal.value = page.total || page.list.length || 0
    equipmentPage.value = params.page || equipmentPage.value
    equipmentSize.value = params.size || equipmentSize.value
  } catch (error) {
    ErrorHandler.handleApiError(error)
    // 显示虚拟数据或空数据
    equipmentList.value = []
    equipmentTotal.value = 0
  } finally {
    equipmentLoading.value = false
  }
}

// 搜索设备
const handleEquipmentSearch = (params: any = {}) => {
  getEquipmentList(params)
}

// 设备分页大小改变
const handleEquipmentSizeChange = (size: number) => {
  equipmentSize.value = size
  getEquipmentList({ page: equipmentPage.value, size })
}

// 设备页码改变
const handleEquipmentCurrentChange = (page: number) => {
  equipmentPage.value = page
  getEquipmentList({ page, size: equipmentSize.value })
}

// 更新设备状态概览
const handleUpdateEquipmentStatus = async () => {
  try {
    const result = await erpApi.production.getEquipmentStatus({})
    const data = unwrapResponseData<any>(result) || {}
    if (data?.status_overview) {
      equipmentStatusOverview.value = data.status_overview
    } else if (data?.online !== undefined) {
      equipmentStatusOverview.value = data
    }
  } catch (error) {
    ErrorHandler.handleApiError(error)
    // 保留现有状态或重置为默认值
  }
}

// 运行产能规划
const handleRunCapacityPlanning = async () => {
  if (!planningFormRef.value) return
  
  try {
    await planningFormRef.value.validate()
    planningLoading.value = true
    
    const payload = {
      ...planningForm.value,
      start_date: formatDateParam(planningForm.value.start_date),
      end_date: formatDateParam(planningForm.value.end_date)
    } as any
    const result = await erpApi.production.runCapacityPlanning(payload)
    if (result) {
      ElMessage.success('产能规划运行成功')
      // 加载规划结果
      await getPlanningResults()
    }
  } catch (error) {
    ErrorHandler.handleApiError(error)
  } finally {
    planningLoading.value = false
  }
}

// 获取产能规划结果
const getPlanningResults = async () => {
  try {
    planningResultsLoading.value = true
    const result = await erpApi.production.getCapacityPlanningResults({ page: 1, size: 1000 })
    const page = unwrapPageResponse<CapacityPlanningResult>(result)
    planningResults.value = page.list
    planningResultsTotal.value = page.total || page.list.length || 0
  } catch (error) {
    ErrorHandler.handleApiError(error)
  } finally {
    planningResultsLoading.value = false
  }
}

const formatDateParam = (value: string | Date) => {
  if (!value) return ''
  if (typeof value === 'string') return value
  return value.toISOString().slice(0, 10)
}

const normalizeDateRangeParams = (params: any) => {
  if (!params) return params
  const out = { ...params }
  const range = out.start_date
  if (Array.isArray(range) && range.length === 2) {
    out.start_date = formatDateParam(range[0])
    out.end_date = formatDateParam(range[1])
  }
  return out
}

// 重置产能规划表单
const handleResetPlanningForm = () => {
  if (!planningFormRef.value) return
  planningFormRef.value.resetFields()
  planningForm.value = {
    planning_period: 'month',
    start_date: new Date(),
    end_date: new Date(new Date().setMonth(new Date().getMonth() + 1)),
    consider_overtime: false,
    consider_outsource: false,
    capacity_buffer_rate: 10
  }
}

// 获取利用率颜色
const getUtilizationRateColor = (rate: number) => {
  if (rate < 60) return '#67c23a' // 绿色
  if (rate < 85) return '#e6a23c' // 黄色
  return '#f56c6c' // 红色
}

// 获取负荷率颜色
const getLoadRateColor = (rate: number) => {
  if (rate < 60) return '#67c23a' // 绿色
  if (rate < 85) return '#e6a23c' // 黄色
  return '#f56c6c' // 红色
}

// 设备状态标签类型
const getEquipmentStatusType = (status: string) => {
  const statusMap: Record<string, string> = {
    'online': 'success',
    'offline': 'danger',
    'running': 'success',
    'stopped': 'warning'
  }
  return statusMap[status] || 'info'
}

// 设备状态标签文本
const getEquipmentStatusLabel = (status: string) => {
  const statusMap: Record<string, string> = {
    'online': '在线',
    'offline': '离线',
    'running': '运行中',
    'stopped': '停机中'
  }
  return statusMap[status] || status
}

// 建议类型标签类型
const getSuggestionTypeType = (type: string) => {
  const typeMap: Record<string, string> = {
    'overtime': 'warning',
    'outsource': 'info',
    'hire': 'primary',
    'normal': 'success'
  }
  return typeMap[type] || 'info'
}

// 建议类型标签文本
const getSuggestionTypeLabel = (type: string) => {
  const typeMap: Record<string, string> = {
    'overtime': '加班建议',
    'outsource': '外协建议',
    'hire': '招聘建议',
    'normal': '正常'
  }
  return typeMap[type] || type
}
</script>

<style scoped lang="scss">
.capacity-view {
  padding: 20px;
  
  .module-card {
    border-radius: 8px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
    
    .card-header {
      display: flex;
      align-items: center;
      gap: 10px;
      font-size: 20px;
      font-weight: bold;
      color: #1890ff;
    }
  }
  
  .capacity-tabs {
    margin-top: 20px;
  }
  
  .tab-content {
    padding: 20px 0;
  }
  
  .chart-card {
    margin-bottom: 20px;
    
    .chart-container {
      height: 300px;
      display: flex;
      align-items: center;
      justify-content: center;
      
      .chart-placeholder {
        width: 100%;
        height: 100%;
        display: flex;
        align-items: center;
        justify-content: center;
      }
    }
  }
  
  .status-card {
    margin-bottom: 20px;
    
    .equipment-status-overview {
      display: flex;
      gap: 30px;
      justify-content: center;
      flex-wrap: wrap;
      
      .status-item {
        text-align: center;
      }
    }
  }
  
  .planning-form {
    margin-bottom: 20px;
    padding: 20px;
    background-color: #fafafa;
    border-radius: 8px;
    
    .form-actions {
      margin-top: 20px;
      display: flex;
      justify-content: center;
      gap: 10px;
    }
  }
  
  .planning-results {
    margin-top: 30px;
  }
}

// 响应式设计
@media (max-width: 1024px) {
  .capacity-view {
    padding: 16px;
    
    .chart-container {
      height: 250px;
    }
    
    .planning-form {
      padding: 16px;
    }
  }
}

@media (max-width: 768px) {
  .capacity-view {
    padding: 12px;
    
    .module-card {
      .card-header {
        font-size: 18px;
      }
    }
    
    .chart-container {
      height: 200px;
    }
    
    .equipment-status-overview {
      gap: 20px;
    }
    
    .planning-form {
      padding: 12px;
      
      .form-actions {
        flex-direction: column;
        align-items: center;
      }
    }
  }
}
</style>
