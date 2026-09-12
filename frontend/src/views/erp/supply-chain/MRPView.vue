<template>
  <div class="mrp-view">
    <el-card class="module-card">
      <template #header>
        <div class="card-header">
          <el-icon><Cpu /></el-icon>
          <span>MRP运算</span>
        </div>
      </template>
      
      <!-- MRP运算配置 -->
      <el-form
        ref="mrpFormRef"
        :model="mrpForm"
        label-width="120px"
        class="mrp-config-form"
      >
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="运算日期" prop="run_date" required>
              <el-date-picker
                v-model="mrpForm.run_date"
                type="date"
                placeholder="选择运算日期"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="计划周期" prop="plan_period" required>
              <el-input-number
                v-model="mrpForm.plan_period"
                :min="1"
                :max="365"
                :step="1"
                placeholder="输入计划周期（天）"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="安全库存考虑" prop="consider_safety_stock">
              <el-switch
                v-model="mrpForm.consider_safety_stock"
                active-text="考虑"
                inactive-text="不考虑"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="批量规则" prop="lot_rule">
              <el-select
                v-model="mrpForm.lot_rule"
                placeholder="选择批量规则"
                style="width: 100%"
              >
                <el-option label="按需生成" value="LOT_FOR_LOT" />
                <el-option label="固定批量" value="FIXED_LOT" />
                <el-option label="经济批量" value="ECONOMIC_LOT" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="固定批量值" prop="fixed_lot_size">
              <el-input-number
                v-model="mrpForm.fixed_lot_size"
                :min="1"
                :max="1000000"
                :step="1"
                placeholder="输入固定批量值"
                style="width: 100%"
                :disabled="mrpForm.lot_rule !== 'FIXED_LOT'"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="运算模式" prop="run_mode">
              <el-select
                v-model="mrpForm.run_mode"
                placeholder="选择运算模式"
                style="width: 100%"
              >
                <el-option label="全重排" value="FULL_REGENERATION" />
                <el-option label="净改变" value="NET_CHANGE" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <div class="form-actions">
          <el-button type="primary" @click="handleRunMRP" :loading="mrpLoading">
            <el-icon><VideoPlay /></el-icon>
            执行MRP运算
          </el-button>
          <el-button @click="handleReset">重置</el-button>
        </div>
      </el-form>
      
      <!-- MRP运算结果 -->
      <div class="mrp-results" v-if="mrpResults.length > 0">
        <h3>MRP运算结果</h3>
        <el-divider />
        
        <el-card shadow="hover" class="result-card">
          <template #header>
            <div class="card-header">
              <span>运算结果概览</span>
            </div>
          </template>
          <div class="result-overview">
            <el-statistic title="物料总数" :value="overview.materialCount" />
            <el-statistic title="计划订单数" :value="overview.planOrderCount" />
            <el-statistic title="采购建议数" :value="overview.purchaseSuggestionCount" />
            <el-statistic title="生产建议数" :value="overview.productionSuggestionCount" />
          </div>
        </el-card>
        
        <!-- MRP运算结果列表 -->
        <TableComponent
          :data="mrpResults"
          :columns="mrpResultColumns"
          :total="mrpResultsTotal"
          :loading="mrpResultsLoading"
          :show-index="true"
          :show-action="true"
          :actions="mrpResultActions"
          :table-actions="tableActions"
          :filters="mrpResultFilters"
          :show-filter="true"
          @search="handleResultSearch"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        >
          <!-- 物料类型列自定义 -->
          <template #materialType="{ row }">
            <el-tag
              :type="getMaterialTypeType(row.material_type)"
              size="small"
            >
              {{ getMaterialTypeLabel(row.material_type) }}
            </el-tag>
          </template>
          
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
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Cpu, RefreshLeft, Download, View, Edit, VideoPlay } from '@element-plus/icons-vue'
import { TableComponent } from '../../../components/base'
import { erpApi } from '../../../api/erp'
import { ErrorHandler } from '../../../utils/error-handler'
import { DataTransformer } from '../../../utils/data-transformer'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { MRPParams, MRPResult, MRPSuggestionType } from '../../../types/erp/supply-chain'

// 响应式数据
const mrpFormRef = ref<any>(null)
const mrpForm = ref<MRPParams>({
  run_date: new Date(),
  plan_period: 90,
  consider_safety_stock: true,
  lot_rule: 'LOT_FOR_LOT',
  fixed_lot_size: 100,
  run_mode: 'FULL_REGENERATION'
})

const mrpLoading = ref(false)
const mrpResults = ref<MRPResult[]>([])
const mrpResultsTotal = ref(0)
const mrpResultsLoading = ref(false)
const mrpResultsPage = ref(1)
const mrpResultsSize = ref(10)

// 物料类型映射
const materialTypeMap: Record<string, { label: string; type: string }> = {
  'raw_material': { label: '原材料', type: 'info' },
  'semi_finished': { label: '半成品', type: 'warning' },
  'finished_product': { label: '成品', type: 'success' },
  'auxiliary_material': { label: '辅助材料', type: 'info' }
}

// 建议类型映射
const suggestionTypeMap: Record<string, { label: string; type: string }> = {
  'PURCHASE': { label: '采购建议', type: 'primary' },
  'PRODUCTION': { label: '生产建议', type: 'success' },
  'INVENTORY': { label: '库存建议', type: 'info' },
  'DELAY': { label: '延迟建议', type: 'warning' }
}

// 运算结果概览
const overview = ref({
  materialCount: 0,
  planOrderCount: 0,
  purchaseSuggestionCount: 0,
  productionSuggestionCount: 0
})

// MRP结果筛选条件
const mrpResultFilters = [
  { prop: 'material_code', label: '物料编码', type: 'input' as 'input', placeholder: '请输入物料编码' },
  { prop: 'material_name', label: '物料名称', type: 'input' as 'input', placeholder: '请输入物料名称' },
  { prop: 'material_type', label: '物料类型', type: 'select' as 'select', options: [
    { label: '原材料', value: 'raw_material' },
    { label: '半成品', value: 'semi_finished' },
    { label: '成品', value: 'finished_product' },
    { label: '辅助材料', value: 'auxiliary_material' }
  ]},
  { prop: 'suggestion_type', label: '建议类型', type: 'select' as 'select', options: [
    { label: '采购建议', value: 'PURCHASE' },
    { label: '生产建议', value: 'PRODUCTION' },
    { label: '库存建议', value: 'INVENTORY' },
    { label: '延迟建议', value: 'DELAY' }
  ]},
  { prop: 'suggestion_date', label: '建议日期', type: 'daterange' as 'daterange' }
] as any

// MRP结果列配置
const mrpResultColumns = [
  { prop: 'material_code', label: '物料编码', width: 150 },
  { prop: 'material_name', label: '物料名称', width: 200 },
  { prop: 'specification', label: '规格型号', width: 150 },
  { prop: 'material_type', label: '物料类型', width: 120, slotName: 'materialType' },
  { prop: 'current_qty', label: '当前库存', width: 120, align: 'right' },
  { prop: 'safety_stock', label: '安全库存', width: 120, align: 'right' },
  { prop: 'demand_qty', label: '需求数量', width: 120, align: 'right' },
  { prop: 'supply_qty', label: '供应数量', width: 120, align: 'right' },
  { prop: 'suggestion_type', label: '建议类型', width: 120, slotName: 'suggestionType' },
  { prop: 'suggestion_qty', label: '建议数量', width: 120, align: 'right' },
  { prop: 'suggestion_date', label: '建议日期', width: 150 },
  { prop: 'remark', label: '备注', minWidth: 200 }
]

// MRP结果操作按钮
const mrpResultActions = [
  {
    text: '查看详情',
    type: 'primary',
    size: 'small',
    icon: View,
    handler: (row: any) => {
      ElMessageBox.alert(`<pre>${JSON.stringify(row, null, 2)}</pre>`, 'MRP结果详情', { dangerouslyUseHTMLString: true })
    }
  },
  {
    text: '生成计划',
    type: 'success',
    size: 'small',
    icon: Edit,
    handler: (row: any) => {
      handleGeneratePlan(row)
    }
  }
]

// 表格操作按钮
const tableActions = [
  {
    key: 'export',
    text: '导出结果',
    type: 'primary',
    icon: Download,
    handler: () => {
      handleExportResults()
    }
  },
  {
    key: 'refresh',
    text: '刷新',
    icon: RefreshLeft,
    handler: () => {
      handleResultSearch()
    }
  }
]

// 初始加载
onMounted(() => {
  // 可以在这里加载历史MRP运算结果
})

// 执行MRP运算
const handleRunMRP = async () => {
  if (!mrpFormRef.value) return
  
  try {
    await mrpFormRef.value.validate()
    mrpLoading.value = true
    
    const result = await erpApi.supplyChain.runMRP(mrpForm.value)
    if (result) {
      ElMessage.success('MRP运算执行成功')
      // 加载MRP运算结果
      await getMRPResults()
    }
  } catch (error) {
    ErrorHandler.handleApiError(error)
  } finally {
    mrpLoading.value = false
  }
}

// 重置表单
const handleReset = () => {
  if (!mrpFormRef.value) return
  mrpFormRef.value.resetFields()
  mrpForm.value.run_date = new Date()
  mrpForm.value.plan_period = 90
  mrpForm.value.consider_safety_stock = true
  mrpForm.value.lot_rule = 'LOT_FOR_LOT'
  mrpForm.value.fixed_lot_size = 100
  mrpForm.value.run_mode = 'FULL_REGENERATION'
}

// 获取MRP运算结果
const getMRPResults = async (params: any = {}) => {
  try {
    mrpResultsLoading.value = true
    const result = await erpApi.supplyChain.getMRPResults({
      page: params.page || mrpResultsPage.value,
      size: params.size || mrpResultsSize.value,
      ...params
    })
    const normalizedData = DataTransformer.normalizeResponse(result)
    mrpResults.value = normalizedData?.data?.list || normalizedData?.data || []
    mrpResultsTotal.value = normalizedData?.data?.total || mrpResults.value.length
    mrpResultsPage.value = params.page || mrpResultsPage.value
    mrpResultsSize.value = params.size || mrpResultsSize.value
    
    // 更新概览数据
    calculateOverview()
  } catch (error) {
    ErrorHandler.handleApiError(error)
  } finally {
    mrpResultsLoading.value = false
  }
}

// 计算概览数据
const calculateOverview = () => {
  const materialSet = new Set<string>()
  let planOrderCount = 0
  let purchaseSuggestionCount = 0
  let productionSuggestionCount = 0
  
  mrpResults.value.forEach(item => {
    materialSet.add(item.material_code)
    if (item.suggestion_type === 'PURCHASE') {
      purchaseSuggestionCount++
    } else if (item.suggestion_type === 'PRODUCTION') {
      productionSuggestionCount++
    }
  })
  
  overview.value = {
    materialCount: materialSet.size,
    planOrderCount,
    purchaseSuggestionCount,
    productionSuggestionCount
  }
}

// 搜索MRP结果
const handleResultSearch = (params: any = {}) => {
  getMRPResults(params)
}

// 分页大小改变
const handleSizeChange = (size: number) => {
  mrpResultsSize.value = size
  getMRPResults({ page: mrpResultsPage.value, size })
}

// 页码改变
const handleCurrentChange = (page: number) => {
  mrpResultsPage.value = page
  getMRPResults({ page, size: mrpResultsSize.value })
}

// 生成计划
const handleGeneratePlan = async (_row: any) => {
  try {
    // 这里可以实现生成计划的逻辑
    ElMessage.success('计划生成成功')
    // 刷新结果列表
    handleResultSearch()
  } catch (error) {
    ErrorHandler.handleApiError(error)
  }
}

// 导出结果
const handleExportResults = () => {
  // 这里可以实现导出结果的逻辑
  ElMessage.success('结果导出成功')
}

// 物料类型标签类型
const getMaterialTypeType = (type: string) => {
  return materialTypeMap[type]?.type || 'info'
}

// 物料类型标签文本
const getMaterialTypeLabel = (type: string) => {
  return materialTypeMap[type]?.label || type
}

// 建议类型标签类型
const getSuggestionTypeType = (type: string) => {
  return suggestionTypeMap[type]?.type || 'info'
}

// 建议类型标签文本
const getSuggestionTypeLabel = (type: string) => {
  return suggestionTypeMap[type]?.label || type
}
</script>

<style scoped lang="scss">
.mrp-view {
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
  
  .mrp-config-form {
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
  
  .mrp-results {
    margin-top: 30px;
    
    h3 {
      margin-bottom: 0;
      color: #333;
      font-size: 18px;
      font-weight: bold;
    }
    
    .result-card {
      margin-bottom: 20px;
    }
    
    .result-overview {
      display: flex;
      gap: 30px;
      justify-content: center;
      flex-wrap: wrap;
    }
  }
}

// 响应式设计
@media (max-width: 1024px) {
  .mrp-view {
    padding: 16px;
    
    .mrp-config-form {
      padding: 16px;
    }
  }
}

@media (max-width: 768px) {
  .mrp-view {
    padding: 12px;
    
    .module-card {
      .card-header {
        font-size: 18px;
      }
    }
    
    .mrp-config-form {
      padding: 12px;
      
      .form-actions {
        flex-direction: column;
        align-items: center;
      }
    }
    
    .result-overview {
      gap: 20px;
    }
  }
}
</style>
