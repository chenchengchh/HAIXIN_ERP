<template>
  <div class="inventory-view">
    <el-card class="module-card">
      <template #header>
        <div class="card-header">
          <el-icon><Box /></el-icon>
          <span>库存管理</span>
        </div>
      </template>
      
      <!-- 库存列表 -->
      <TableComponent
        :data="inventoryList"
        :columns="inventoryColumns"
        :total="inventoryTotal"
        :loading="inventoryLoading"
        :show-index="true"
        :show-selection="true"
        :show-action="true"
        :actions="inventoryActions"
        :table-actions="tableActions"
        :filters="inventoryFilters"
        :show-filter="true"
        @search="handleSearch"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        @selection-change="handleSelectionChange"
      >
        <!-- 库存状态列自定义 -->
        <template #inventoryStatus="{ row }">
          <el-tag
            :type="getInventoryStatusType(row.inventory_status)"
            size="small"
          >
            {{ getInventoryStatusLabel(row.inventory_status) }}
          </el-tag>
        </template>
      </TableComponent>
    </el-card>
  </div>
  
  <!-- 库存调拨对话框 -->
  <DialogComponent
    v-model="transferFormVisible"
    title="库存调拨"
    width="800px"
    :show-footer="true"
    :show-cancel-button="true"
    :show-confirm-button="true"
    cancel-text="取消"
    confirm-text="保存"
    :show-close="true"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
    @confirm="handleTransferSave"
  >
    <div class="transfer-form">
      <el-form
        ref="transferFormRef"
        :model="transferForm"
        label-width="120px"
      >
        <el-form-item label="调拨单号" prop="transfer_no">
          <el-input v-model="transferForm.transfer_no" placeholder="自动生成" disabled />
        </el-form-item>
        <el-form-item label="调出仓库" prop="from_warehouse_id" required>
          <el-select
            v-model="transferForm.from_warehouse_id"
            placeholder="选择调出仓库"
            style="width: 100%"
          >
            <el-option
              v-for="warehouse in warehouseList"
              :key="warehouse.id"
              :label="warehouse.warehouse_name"
              :value="warehouse.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="调入仓库" prop="to_warehouse_id" required>
          <el-select
            v-model="transferForm.to_warehouse_id"
            placeholder="选择调入仓库"
            style="width: 100%"
          >
            <el-option
              v-for="warehouse in warehouseList"
              :key="warehouse.id"
              :label="warehouse.warehouse_name"
              :value="warehouse.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="调拨日期" prop="transfer_date" required>
          <el-date-picker
            v-model="transferForm.transfer_date"
            type="date"
            placeholder="选择调拨日期"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="transferForm.remark"
            type="textarea"
            :rows="3"
            placeholder="输入备注信息"
          />
        </el-form-item>
      </el-form>
    </div>
  </DialogComponent>
  
  <!-- 库存盘点对话框 -->
  <DialogComponent
    v-model="countFormVisible"
    title="库存盘点"
    width="800px"
    :show-footer="true"
    :show-cancel-button="true"
    :show-confirm-button="true"
    cancel-text="取消"
    confirm-text="保存"
    :show-close="true"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
    @confirm="handleCountSave"
  >
    <div class="count-form">
      <el-form
        ref="countFormRef"
        :model="countForm"
        label-width="120px"
      >
        <el-form-item label="盘点单号" prop="count_no">
          <el-input v-model="countForm.count_no" placeholder="自动生成" disabled />
        </el-form-item>
        <el-form-item label="仓库" prop="warehouse_id" required>
          <el-select
            v-model="countForm.warehouse_id"
            placeholder="选择仓库"
            style="width: 100%"
          >
            <el-option
              v-for="warehouse in warehouseList"
              :key="warehouse.id"
              :label="warehouse.warehouse_name"
              :value="warehouse.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="盘点日期" prop="count_date" required>
          <el-date-picker
            v-model="countForm.count_date"
            type="date"
            placeholder="选择盘点日期"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="盘点人员" prop="count_person">
          <el-input v-model="countForm.count_person" placeholder="输入盘点人员" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="countForm.remark"
            type="textarea"
            :rows="3"
            placeholder="输入备注信息"
          />
        </el-form-item>
      </el-form>
    </div>
  </DialogComponent>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Box, Plus, Edit, RefreshLeft, View, Check } from '@element-plus/icons-vue'
import { TableComponent, DialogComponent } from '../../../components/base'
import { erpApi } from '../../../api/erp'
import { ErrorHandler } from '../../../utils/error-handler'
import { DataTransformer } from '../../../utils/data-transformer'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { Inventory, InventoryStatus } from '../../../types/erp/supply-chain'

// 响应式数据
const inventoryList = ref<Inventory[]>([])
const inventoryTotal = ref(0)
const inventoryLoading = ref(false)
const inventoryPage = ref(1)
const inventorySize = ref(10)
const selectedRows = ref<Inventory[]>([])

// 库存状态映射
const inventoryStatusMap: Record<string, { label: string; type: string }> = {
  'normal': { label: '正常', type: 'success' },
  'low': { label: '低于安全库存', type: 'warning' },
  'out': { label: '缺货', type: 'danger' },
  'high': { label: '高于最高库存', type: 'info' }
}

// 库存筛选条件
const inventoryFilters = [
  { prop: 'material_code', label: '物料编码', type: 'input' as 'input', placeholder: '请输入物料编码' },
  { prop: 'material_name', label: '物料名称', type: 'input' as 'input', placeholder: '请输入物料名称' },
  { prop: 'warehouse_name', label: '仓库名称', type: 'input' as 'input', placeholder: '请输入仓库名称' },
  { prop: 'inventory_status', label: '库存状态', type: 'select' as 'select', options: [
    { label: '正常', value: 'normal' },
    { label: '低于安全库存', value: 'low' },
    { label: '缺货', value: 'out' },
    { label: '高于最高库存', value: 'high' }
  ]},
  { prop: 'update_time', label: '更新时间', type: 'daterange' as 'daterange' }
] as any

// 库存列配置
const inventoryColumns = [
  { prop: 'material_code', label: '物料编码', width: 150 },
  { prop: 'material_name', label: '物料名称', width: 200 },
  { prop: 'specification', label: '规格型号', width: 150 },
  { prop: 'unit', label: '计量单位', width: 100 },
  { prop: 'warehouse_name', label: '仓库名称', width: 150 },
  { prop: 'current_qty', label: '当前库存', width: 120, align: 'right' },
  { prop: 'safety_stock', label: '安全库存', width: 120, align: 'right' },
  { prop: 'max_stock', label: '最高库存', width: 120, align: 'right' },
  { prop: 'inventory_status', label: '库存状态', width: 120, slotName: 'inventoryStatus' },
  { prop: 'update_time', label: '更新时间', width: 180 },
  { prop: 'remark', label: '备注', minWidth: 200 }
]

// 库存操作按钮
const inventoryActions = [
  {
    text: '查看',
    type: 'primary',
    size: 'small',
    icon: View,
    handler: (row: any) => {
      ElMessageBox.alert(`<pre>${JSON.stringify(row, null, 2)}</pre>`, '库存详情', { dangerouslyUseHTMLString: true })
    }
  },
  {
    text: '盘点',
    type: 'info',
    size: 'small',
    icon: Check,
    handler: (row: any) => {
      handleMaterialCount(row)
    }
  },
  {
    text: '调整',
    type: 'warning',
    size: 'small',
    icon: Edit,
    handler: (row: any) => {
      handleMaterialCount(row)
    }
  }
]

// 表格操作按钮
const tableActions = [
  {
    key: 'add',
    text: '库存调拨',
    type: 'primary',
    icon: Plus,
    handler: () => {
      handleOpenTransferDialog()
    }
  },
  {
    key: 'inventory-check',
    text: '库存盘点',
    type: 'success',
    icon: Check,
    handler: () => {
      handleOpenCountDialog()
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

// 库存调拨和盘点相关数据
const transferFormVisible = ref(false)
const transferFormRef = ref<any>(null)
const transferForm = ref({
  transfer_no: '',
  from_warehouse_id: '',
  to_warehouse_id: '',
  transfer_date: new Date(),
  remark: '',
  transfer_items: []
})

const countFormVisible = ref(false)
const countFormRef = ref<any>(null)
const countForm = ref({
  count_no: '',
  warehouse_id: '',
  count_date: new Date(),
  count_person: '',
  remark: '',
  count_items: [] as Array<{
    material_id: any;
    material_code: any;
    material_name: any;
    specification: any;
    unit: any;
    current_qty: any;
    count_qty: any;
    difference_qty: number;
  }>
})

const warehouseList = ref<any[]>([])

// 初始加载
onMounted(() => {
  handleSearch()
  getWarehouseList()
})

// 获取库存列表
const getInventoryList = async (params: any) => {
  try {
    inventoryLoading.value = true
    const result = await erpApi.supplyChain.getInventory({
      page: params.page || inventoryPage.value,
      size: params.size || inventorySize.value,
      ...params
    })
    const normalizedData = DataTransformer.normalizeResponse(result)
    // 确保data是数组类型，避免将Object类型的数据传递给TableComponent
    let data = normalizedData?.data?.list || normalizedData?.data || []
    if (!Array.isArray(data)) {
      data = []
    }
    inventoryList.value = data
    // 确保total是数字类型
    const total = normalizedData?.data?.total !== undefined ? Number(normalizedData?.data?.total) : data.length
    inventoryTotal.value = total
    inventoryPage.value = params.page || inventoryPage.value
    inventorySize.value = params.size || inventorySize.value
  } catch (error) {
    ErrorHandler.handleApiError(error)
  } finally {
    inventoryLoading.value = false
  }
}

// 搜索库存
const handleSearch = (params: any = {}) => {
  getInventoryList(params)
}

// 分页大小改变
const handleSizeChange = (size: number) => {
  inventorySize.value = size
  getInventoryList({ page: inventoryPage.value, size })
}

// 页码改变
const handleCurrentChange = (page: number) => {
  inventoryPage.value = page
  getInventoryList({ page, size: inventorySize.value })
}

// 选择行改变
const handleSelectionChange = (rows: Inventory[]) => {
  selectedRows.value = rows
}

// 库存状态标签类型
const getInventoryStatusType = (status: string) => {
  // 确保返回有效的type值，避免ElTag组件type属性验证失败
  return inventoryStatusMap[status]?.type || 'info'
}

// 库存状态标签文本
const getInventoryStatusLabel = (status: string) => {
  return inventoryStatusMap[status]?.label || status
}

// 获取仓库列表
const getWarehouseList = async () => {
  try {
    const result = await erpApi.basicData.getWarehouses()
    const normalizedData = DataTransformer.normalizeResponse(result)
    // 确保data是数组类型，避免将Object类型的数据传递给组件
    let data = normalizedData?.data?.list || normalizedData?.data || []
    if (!Array.isArray(data)) {
      data = []
    }
    warehouseList.value = data
  } catch (error) {
    ErrorHandler.handleApiError(error)
  }
}

// 打开库存调拨对话框
const handleOpenTransferDialog = () => {
  // 初始化表单数据
  transferForm.value = {
    transfer_no: '',
    from_warehouse_id: '',
    to_warehouse_id: '',
    transfer_date: new Date(),
    remark: '',
    transfer_items: []
  }
  transferFormVisible.value = true
}

// 保存库存调拨
const handleTransferSave = async () => {
  if (!transferFormRef.value) return
  
  try {
    await transferFormRef.value.validate()
    const result = await erpApi.supplyChain.createInventoryTransfer(transferForm.value)
    if (result) {
      transferFormVisible.value = false
      ElMessage.success('库存调拨保存成功')
      handleSearch() // 刷新库存列表
    }
  } catch (error) {
    ErrorHandler.handleApiError(error)
  }
}

// 打开库存盘点对话框
const handleOpenCountDialog = () => {
  // 初始化表单数据
  countForm.value = {
    count_no: '',
    warehouse_id: '',
    count_date: new Date(),
    count_person: '',
    remark: '',
    count_items: []
  }
  countFormVisible.value = true
}

// 保存库存盘点
const handleCountSave = async () => {
  if (!countFormRef.value) return
  
  try {
    await countFormRef.value.validate()
    const result = await erpApi.supplyChain.createInventoryCount(countForm.value)
    if (result) {
      countFormVisible.value = false
      ElMessage.success('库存盘点保存成功')
      handleSearch() // 刷新库存列表
    }
  } catch (error) {
    ErrorHandler.handleApiError(error)
  }
}

// 物料盘点处理
const handleMaterialCount = (row: any) => {
  // 初始化表单数据，默认选择当前物料的仓库
  countForm.value = {
    count_no: '',
    warehouse_id: row.warehouse_id,
    count_date: new Date(),
    count_person: '',
    remark: '',
    count_items: [
      {
        material_id: row.material_id,
        material_code: row.material_code,
        material_name: row.material_name,
        specification: row.specification,
        unit: row.unit,
        current_qty: row.current_qty,
        count_qty: row.current_qty,
        difference_qty: 0
      }
    ]
  }
  countFormVisible.value = true
}
</script>

<style scoped lang="scss">
.inventory-view {
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
  .inventory-view {
    padding: 16px;
  }
}

@media (max-width: 768px) {
  .inventory-view {
    padding: 12px;
    
    .module-card {
      .card-header {
        font-size: 18px;
      }
    }
  }
}
</style>
