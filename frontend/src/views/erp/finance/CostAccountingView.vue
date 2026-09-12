<template>
  <div class="cost-accounting-view">
    <el-card class="module-card">
      <template #header>
        <div class="card-header">
          <el-icon><Document /></el-icon>
          <span>成本核算</span>
        </div>
      </template>
      
      <!-- 成本列表 -->
      <TableComponent
        :data="costAccountingList"
        :columns="costAccountingColumns"
        :total="costAccountingTotal"
        :loading="costAccountingLoading"
        :show-index="true"
        :show-selection="true"
        :show-action="true"
        :actions="costAccountingActions"
        :table-actions="tableActions"
        :filters="costAccountingFilters"
        :show-filter="true"
        @search="handleSearch"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        @selection-change="handleSelectionChange"
      >
        <!-- 成本类型列自定义 -->
        <template #costType="{ row }">
          <el-tag
            :type="row.cost_type === 'product' ? 'success' : 'info'"
            size="small"
          >
            {{ row.cost_type === 'product' ? '产品成本' : '销售成本' }}
          </el-tag>
        </template>
      </TableComponent>
    </el-card>
    
    <!-- 成本核算详情对话框 -->
    <DialogComponent
      v-model="detailVisible"
      title="成本核算详情"
      width="800px"
      :show-footer="true"
      :show-cancel-button="true"
      :show-confirm-button="false"
      cancel-text="关闭"
      :show-close="true"
      :close-on-click-modal="true"
      :close-on-press-escape="true"
    >
      <div class="cost-detail">
        <el-row :gutter="20">
          <el-col :span="12">
            <div class="detail-item">
              <span class="label">产品编码：</span>
              <span class="value">{{ selectedCost?.product_code }}</span>
            </div>
            <div class="detail-item">
              <span class="label">产品名称：</span>
              <span class="value">{{ selectedCost?.product_name }}</span>
            </div>
            <div class="detail-item">
              <span class="label">成本类型：</span>
              <span class="value">
                <el-tag :type="selectedCost?.cost_type === 'product' ? 'success' : 'info'" size="small">
                  {{ selectedCost?.cost_type === 'product' ? '产品成本' : '销售成本' }}
                </el-tag>
              </span>
            </div>
            <div class="detail-item">
              <span class="label">材料成本：</span>
              <span class="value">{{ selectedCost?.material_cost }}</span>
            </div>
            <div class="detail-item">
              <span class="label">人工成本：</span>
              <span class="value">{{ selectedCost?.labor_cost }}</span>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="detail-item">
              <span class="label">制造费用：</span>
              <span class="value">{{ selectedCost?.overhead_cost }}</span>
            </div>
            <div class="detail-item">
              <span class="label">总成本：</span>
              <span class="value">{{ selectedCost?.total_cost }}</span>
            </div>
            <div class="detail-item">
              <span class="label">单位成本：</span>
              <span class="value">{{ selectedCost?.unit_cost }}</span>
            </div>
            <div class="detail-item">
              <span class="label">成本日期：</span>
              <span class="value">{{ selectedCost?.cost_date }}</span>
            </div>
            <div class="detail-item">
              <span class="label">创建时间：</span>
              <span class="value">{{ selectedCost?.create_time }}</span>
            </div>
          </el-col>
        </el-row>
      </div>
    </DialogComponent>
    
    <!-- 成本核算编辑对话框 -->
    <DialogComponent
      v-model="editFormVisible"
      title="编辑成本核算"
      width="800px"
      :show-footer="true"
      :show-cancel-button="true"
      :show-confirm-button="true"
      cancel-text="取消"
      confirm-text="保存"
      :show-close="true"
      :close-on-click-modal="true"
      :close-on-press-escape="true"
      @confirm="handleEditSave"
    >
      <div class="edit-form">
        <el-form
          ref="editFormRef"
          :model="editForm"
          :rules="editFormRules"
          label-width="120px"
        >
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="产品编码" prop="product_code">
                <el-input v-model="editForm.product_code" placeholder="请输入产品编码" />
              </el-form-item>
              <el-form-item label="产品名称" prop="product_name">
                <el-input v-model="editForm.product_name" placeholder="请输入产品名称" />
              </el-form-item>
              <el-form-item label="成本类型" prop="cost_type">
                <el-select v-model="editForm.cost_type" placeholder="请选择成本类型">
                  <el-option label="产品成本" value="product" />
                  <el-option label="销售成本" value="sales" />
                </el-select>
              </el-form-item>
              <el-form-item label="材料成本" prop="material_cost">
                <el-input-number
                  v-model="editForm.material_cost"
                  :min="0"
                  :step="0.01"
                  placeholder="输入材料成本"
                  style="width: 100%"
                  @change="calculateTotalCost"
                />
              </el-form-item>
              <el-form-item label="人工成本" prop="labor_cost">
                <el-input-number
                  v-model="editForm.labor_cost"
                  :min="0"
                  :step="0.01"
                  placeholder="输入人工成本"
                  style="width: 100%"
                  @change="calculateTotalCost"
                />
              </el-form-item>
              <el-form-item label="制造费用" prop="overhead_cost">
                <el-input-number
                  v-model="editForm.overhead_cost"
                  :min="0"
                  :step="0.01"
                  placeholder="输入制造费用"
                  style="width: 100%"
                  @change="calculateTotalCost"
                />
              </el-form-item>
              <el-form-item label="总成本" prop="total_cost">
                <el-input-number
                  v-model="editForm.total_cost"
                  :min="0"
                  :step="0.01"
                  placeholder="输入总成本"
                  style="width: 100%"
                  disabled
                />
              </el-form-item>
              <el-form-item label="单位成本" prop="unit_cost">
                <el-input-number
                  v-model="editForm.unit_cost"
                  :min="0"
                  :step="0.01"
                  placeholder="输入单位成本"
                  style="width: 100%"
                />
              </el-form-item>
              <el-form-item label="成本日期" prop="cost_date">
                <el-date-picker
                  v-model="editForm.cost_date"
                  type="date"
                  placeholder="选择成本日期"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item label="备注" prop="remark">
            <el-input
              v-model="editForm.remark"
              type="textarea"
              rows="3"
              placeholder="输入备注信息"
            />
          </el-form-item>
        </el-form>
      </div>
    </DialogComponent>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Document, Plus, Edit, Delete, RefreshLeft, View } from '@element-plus/icons-vue'
import { TableComponent, DialogComponent } from '../../../components/base'
import { erpApi } from '../../../api/erp'
import { ErrorHandler } from '../../../utils/error-handler'
import { DataTransformer } from '../../../utils/data-transformer'

// 响应式数据
const costAccountingList = ref<any[]>([])
const costAccountingTotal = ref(0)
const costAccountingLoading = ref(false)
const costAccountingPage = ref(1)
const costAccountingSize = ref(10)
const selectedRows = ref<any[]>([])
const selectedCost = ref<any>(null)

// 对话框状态
const detailVisible = ref(false)
const editFormVisible = ref(false)

// 表单数据
const editFormRef = ref<any>(null)
const editFormRules = ref<any>({
  product_code: [{ required: true, message: '请输入产品编码', trigger: 'blur' }],
  product_name: [{ required: true, message: '请输入产品名称', trigger: 'blur' }],
  cost_type: [{ required: true, message: '请选择成本类型', trigger: 'change' }],
  material_cost: [{ required: true, message: '请输入材料成本', trigger: 'blur' }, { type: 'number', min: 0, message: '材料成本必须大于等于0', trigger: 'blur' }],
  labor_cost: [{ required: true, message: '请输入人工成本', trigger: 'blur' }, { type: 'number', min: 0, message: '人工成本必须大于等于0', trigger: 'blur' }],
  overhead_cost: [{ required: true, message: '请输入制造费用', trigger: 'blur' }, { type: 'number', min: 0, message: '制造费用必须大于等于0', trigger: 'blur' }],
  unit_cost: [{ required: true, message: '请输入单位成本', trigger: 'blur' }, { type: 'number', min: 0, message: '单位成本必须大于等于0', trigger: 'blur' }],
  cost_date: [{ required: true, message: '请选择成本日期', trigger: 'change' }]
})
const editForm = ref({
  id: 0,
  product_code: '',
  product_name: '',
  cost_type: 'product',
  material_cost: 0,
  labor_cost: 0,
  overhead_cost: 0,
  total_cost: 0,
  unit_cost: 0,
  cost_date: '',
  remark: ''
})

// 计算总成本
const calculateTotalCost = () => {
  editForm.value.total_cost = editForm.value.material_cost + editForm.value.labor_cost + editForm.value.overhead_cost
}

// 成本核算筛选条件
const costAccountingFilters = [
  { prop: 'product_code', label: '产品编码', type: 'input' as 'input', placeholder: '请输入产品编码' },
  { prop: 'product_name', label: '产品名称', type: 'input' as 'input', placeholder: '请输入产品名称' },
  { prop: 'cost_type', label: '成本类型', type: 'select' as 'select', options: [
    { label: '产品成本', value: 'product' },
    { label: '销售成本', value: 'sales' }
  ]},
  { prop: 'cost_date', label: '成本日期', type: 'daterange' as 'daterange' }
] as any

// 成本核算列配置
const costAccountingColumns = [
  { prop: 'product_code', label: '产品编码', width: 150 },
  { prop: 'product_name', label: '产品名称', width: 200 },
  { prop: 'cost_type', label: '成本类型', width: 120, slotName: 'costType' },
  { prop: 'material_cost', label: '材料成本', width: 120, align: 'right' },
  { prop: 'labor_cost', label: '人工成本', width: 120, align: 'right' },
  { prop: 'overhead_cost', label: '制造费用', width: 120, align: 'right' },
  { prop: 'total_cost', label: '总成本', width: 120, align: 'right' },
  { prop: 'unit_cost', label: '单位成本', width: 120, align: 'right' },
  { prop: 'cost_date', label: '成本日期', width: 150 },
  { prop: 'create_time', label: '创建时间', width: 180 }
]

// 成本核算操作按钮
const costAccountingActions = [
  {
    text: '查看',
    type: 'primary',
    size: 'small',
    icon: View,
    handler: (row: any) => {
      selectedCost.value = row
      detailVisible.value = true
    }
  },
  {
    text: '编辑',
    type: 'info',
    size: 'small',
    icon: Edit,
    handler: (row: any) => {
      selectedCost.value = row
      // 确保数值类型的属性转换为Number类型
      const rowData = { ...row }
      // 转换所有数值类型的属性
      rowData.material_cost = Number(rowData.material_cost) || 0
      rowData.labor_cost = Number(rowData.labor_cost) || 0
      rowData.overhead_cost = Number(rowData.overhead_cost) || 0
      rowData.total_cost = Number(rowData.total_cost) || 0
      rowData.unit_cost = Number(rowData.unit_cost) || 0
      Object.assign(editForm.value, rowData)
      editFormVisible.value = true
    }
  },
  {
    text: '删除',
    type: 'danger',
    size: 'small',
    icon: Delete,
    handler: (row: any) => {
      ElMessageBox.confirm(`确定要删除成本记录"${row.product_code}"吗？`, '删除确认',
        { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
      ).then(async () => {
        try {
          await erpApi.finance.deleteCostAccounting(Number(row.id))
          ElMessage.success('删除成功')
          handleSearch()
        } catch (e) {
          ErrorHandler.handleApiError(e)
        }
      }).catch(() => {})
    }
  }
]

// 表格操作按钮
const tableActions = [
  {
    key: 'calculate',
    text: '计算成本',
    type: 'primary',
    icon: Document,
    handler: async () => {
      if (!selectedRows.value.length) {
        ElMessage.warning('请先勾选一条成本记录')
        return
      }
      const row: any = selectedRows.value[0]
      try {
        const payload: any = {
          calculation_no: `CC-${Date.now()}`,
          calculation_type: 'product',
          object_id: Number(row.id) || 0,
          object_name: row.product_name || row.product_code || '',
          calculation_amount: Number(row.total_cost) || 0,
          calculation_date: new Date().toISOString().replace('T', ' ').slice(0, 19),
          status: 'completed',
          remark: `由成本记录触发：${row.product_code || ''}`
        }
        await erpApi.finance.calculateCost(payload)
        ElMessage.success('成本核算已提交')
      } catch (e) {
        ErrorHandler.handleApiError(e)
      }
    }
  },
  {
    key: 'add',
    text: '新增成本记录',
    type: 'success',
    icon: Plus,
    handler: () => {
      // 重置表单
      Object.assign(editForm.value, {
        id: 0,
        product_code: '',
        product_name: '',
        cost_type: 'product',
        material_cost: 0,
        labor_cost: 0,
        overhead_cost: 0,
        total_cost: 0,
        unit_cost: 0,
        cost_date: '',
        remark: ''
      })
      selectedCost.value = null
      editFormVisible.value = true
    }
  },
  {
    key: 'refresh',
    text: '刷新',
    icon: RefreshLeft,
    handler: () => {
      handleSearch()
    }
  }
]

// 初始加载
onMounted(() => {
  handleSearch()
})

// 转换成本核算数据中的数值类型
const transformCostData = (data: any[]) => {
  return data.map(item => {
    return {
      ...item,
      material_cost: Number(item.material_cost) || 0,
      labor_cost: Number(item.labor_cost) || 0,
      overhead_cost: Number(item.overhead_cost) || 0,
      total_cost: Number(item.total_cost) || 0,
      unit_cost: Number(item.unit_cost) || 0,
      id: Number(item.id) || 0
    }
  })
}

// 获取成本核算列表
const getCostAccountingList = async (params: any) => {
  try {
    costAccountingLoading.value = true
    const result = await erpApi.finance.getCostAccountingList({
      page: params.page || costAccountingPage.value,
      size: params.size || costAccountingSize.value,
      ...params
    })
    
    // 使用DataTransformer标准化响应数据
    const normalizedData = DataTransformer.normalizeResponse(result)
    
    // 确保数据格式正确
    let list = normalizedData?.data?.list || normalizedData?.data || []
    // 确保list是数组类型，避免TableComponent出现TypeError: data2 is not iterable错误
    if (!Array.isArray(list)) {
      list = []
    }
    // 确保total是数字类型
    const total = normalizedData?.data?.total !== undefined ? Number(normalizedData?.data?.total) : list.length
    
    // 转换数值类型
    costAccountingList.value = transformCostData(list)
    costAccountingTotal.value = total
    costAccountingPage.value = params.page || costAccountingPage.value
    costAccountingSize.value = params.size || costAccountingSize.value
  } catch (error) {
    ErrorHandler.handleApiError(error)
    costAccountingList.value = []
    costAccountingTotal.value = 0
  } finally {
    costAccountingLoading.value = false
  }
}

// 搜索成本核算
const handleSearch = (params: any = {}) => {
  getCostAccountingList(params)
}

// 分页大小改变
const handleSizeChange = (size: number) => {
  costAccountingSize.value = size
  getCostAccountingList({ page: costAccountingPage.value, size })
}

// 页码改变
const handleCurrentChange = (page: number) => {
  costAccountingPage.value = page
  getCostAccountingList({ page, size: costAccountingSize.value })
}

// 选择行改变
const handleSelectionChange = (rows: any[]) => {
  selectedRows.value = rows
}

// 保存编辑
const handleEditSave = async () => {
  if (!editFormRef.value) return
  
  try {
    await editFormRef.value.validate()
    
    // 计算总成本
    editForm.value.total_cost = editForm.value.material_cost + editForm.value.labor_cost + editForm.value.overhead_cost
    
    if (selectedCost.value && selectedCost.value.id) {
      // 更新成本记录
      await erpApi.finance.updateCostAccounting(selectedCost.value.id, editForm.value as any)
      ElMessage.success('成本记录更新成功')
    } else {
      // 创建成本记录
      await erpApi.finance.createCostAccounting(editForm.value as any)
      ElMessage.success('成本记录创建成功')
    }
    
    // 关闭对话框
    editFormVisible.value = false
    // 刷新列表
    handleSearch()
  } catch (error) {
    ErrorHandler.handleApiError(error)
  }
}
</script>

<style scoped lang="scss">
.cost-accounting-view {
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
}

// 响应式设计
@media (max-width: 1024px) {
  .cost-accounting-view {
    padding: 16px;
  }
}

@media (max-width: 768px) {
  .cost-accounting-view {
    padding: 12px;
    
    .module-card {
      .card-header {
        font-size: 18px;
      }
    }
  }
}

// 详情对话框样式
.cost-detail {
  .detail-item {
    margin-bottom: 16px;
    display: flex;
    align-items: center;
    
    .label {
      font-weight: bold;
      width: 120px;
      color: #606266;
    }
    
    .value {
      color: #303133;
    }
  }
}
</style>
