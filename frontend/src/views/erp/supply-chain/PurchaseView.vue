<template>
  <div class="purchase-view">
    <el-card class="module-card">
      <template #header>
        <div class="card-header">
          <el-icon><ShoppingCart /></el-icon>
          <span>采购管理</span>
        </div>
      </template>
      
      <!-- 采购订单列表 -->
      <TableComponent
        :data="purchaseOrders"
        :columns="purchaseOrderColumns"
        :total="purchaseOrderTotal"
        :loading="purchaseOrderLoading"
        :show-index="true"
        :show-selection="true"
        :show-action="true"
        :actions="purchaseOrderActions"
        :table-actions="tableActions"
        :filters="purchaseOrderFilters"
        :show-filter="true"
        @search="handleSearch"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        @selection-change="handleSelectionChange"
      >
        <!-- 状态列自定义 -->
        <template #status="{ row }">
          <el-tag
            :type="getStatusType(row.status)"
            size="small"
          >
            {{ getStatusLabel(row.status) }}
          </el-tag>
        </template>
      </TableComponent>
    </el-card>

    <!-- 采购订单创建/编辑对话框 -->
    <DialogComponent
      v-model="purchaseOrderFormVisible"
      :title="isEditMode ? '编辑采购订单' : '创建采购订单'"
      width="900px"
      :show-footer="true"
      :show-cancel-button="true"
      :show-confirm-button="true"
      cancel-text="取消"
      confirm-text="保存"
      :show-close="true"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      @confirm="handlePurchaseOrderSave"
    >
      <div class="purchase-order-form">
        <el-form
          ref="purchaseOrderFormRef"
          :model="purchaseOrderForm"
          label-width="120px"
        >
          <el-form-item label="订单编号" prop="order_no">
            <el-input v-model="purchaseOrderForm.order_no" placeholder="自动生成" disabled />
          </el-form-item>
          <el-form-item label="供应商" prop="supplier_id" required>
            <el-select
              v-model="purchaseOrderForm.supplier_id"
              placeholder="选择供应商"
              style="width: 100%"
            >
              <el-option
                v-for="supplier in supplierList"
                :key="supplier.id"
                :label="supplier.supplier_name"
                :value="supplier.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="采购日期" prop="purchase_date" required>
            <el-date-picker
              v-model="purchaseOrderForm.purchase_date"
              type="date"
              placeholder="选择采购日期"
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item label="期望交货日期" prop="expected_delivery_date" required>
            <el-date-picker
              v-model="purchaseOrderForm.expected_delivery_date"
              type="date"
              placeholder="选择期望交货日期"
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item label="采购员" prop="buyer">
            <el-input v-model="purchaseOrderForm.buyer" placeholder="输入采购员名称" />
          </el-form-item>
          <el-form-item label="备注" prop="remark">
          <el-input
            v-model="purchaseOrderForm.remark"
            type="textarea"
            :rows="3"
            placeholder="输入备注信息"
          />
        </el-form-item>
        </el-form>
        
        <!-- 采购订单明细 -->
        <div class="purchase-order-detail">
          <h4>采购订单明细</h4>
          <el-table
            :data="purchaseOrderForm.items"
            border
            style="width: 100%"
            max-height="400px"
          >
            <el-table-column prop="material_id" label="物料" width="200">
              <template #default="scope">
                <el-select
                  v-model="scope.row.material_id"
                  placeholder="选择物料"
                  style="width: 100%"
                >
                  <el-option
                    v-for="material in materialList"
                    :key="material.id"
                    :label="material.material_name"
                    :value="material.id"
                  />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column prop="material_code" label="物料编码" width="150" />
            <el-table-column prop="quantity" label="数量" width="100">
              <template #default="scope">
                <el-input-number
                  v-model="scope.row.quantity"
                  :min="0"
                  :precision="2"
                  style="width: 100%"
                />
              </template>
            </el-table-column>
            <el-table-column prop="unit_price" label="单价" width="120">
              <template #default="scope">
                <el-input-number
                  v-model="scope.row.unit_price"
                  :min="0"
                  :precision="2"
                  style="width: 100%"
                />
              </template>
            </el-table-column>
            <el-table-column prop="amount" label="金额" width="120" align="right">
              <template #default="scope">
                {{ (scope.row.quantity * scope.row.unit_price).toFixed(2) }}
              </template>
            </el-table-column>
            <el-table-column prop="unit" label="单位" width="80" />
            <el-table-column prop="remark" label="备注" min-width="150">
              <template #default="scope">
                <el-input v-model="scope.row.remark" placeholder="输入备注" />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="80">
              <template #default="scope">
                <el-button
                  type="danger"
                  size="small"
                  @click="handleDeleteOrderItem(scope.$index)"
                >
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="table-actions">
            <el-button type="primary" @click="handleAddOrderItem">
              <el-icon><Plus /></el-icon> 新增明细
            </el-button>
          </div>
        </div>
      </div>
    </DialogComponent>

    <!-- 采购收货对话框 -->
    <DialogComponent
      v-model="receiptFormVisible"
      title="采购收货"
      width="900px"
      :show-footer="true"
      :show-cancel-button="true"
      :show-confirm-button="true"
      cancel-text="取消"
      confirm-text="保存"
      :show-close="true"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      @confirm="handleReceiptSave"
    >
      <div class="receipt-form">
        <el-form
          ref="receiptFormRef"
          :model="receiptForm"
          label-width="120px"
        >
          <el-form-item label="收货单编号" prop="receipt_no">
            <el-input v-model="receiptForm.receipt_no" placeholder="自动生成" disabled />
          </el-form-item>
          <el-form-item label="采购订单" prop="purchase_order_id" required>
            <el-select
              v-model="receiptForm.purchase_order_id"
              placeholder="选择采购订单"
              style="width: 100%"
              @change="handlePurchaseOrderChange"
            >
              <el-option
                v-for="order in purchaseOrderList"
                :key="order.id"
                :label="order.order_no"
                :value="order.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="收货日期" prop="receipt_date" required>
            <el-date-picker
              v-model="receiptForm.receipt_date"
              type="date"
              placeholder="选择收货日期"
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item label="仓库" prop="warehouse_id" required>
            <el-select
              v-model="receiptForm.warehouse_id"
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
          <el-form-item label="备注" prop="remark">
          <el-input
            v-model="receiptForm.remark"
            type="textarea"
            :rows="3"
            placeholder="输入备注信息"
          />
        </el-form-item>
        </el-form>
        
        <!-- 采购订单明细和收货数量录入 -->
        <div class="receipt-items">
          <h4>采购订单明细</h4>
          <el-table
            :data="receiptItems"
            border
            style="width: 100%"
            max-height="400px"
          >
            <el-table-column prop="material_name" label="物料名称" min-width="150" />
            <el-table-column prop="material_code" label="物料编码" width="120" />
            <el-table-column prop="unit" label="单位" width="80" />
            <el-table-column prop="order_quantity" label="订单数量" width="100" align="right" />
            <el-table-column prop="received_quantity" label="已收数量" width="100" align="right" />
            <el-table-column prop="receipt_quantity" label="本次收货数量" width="150">
              <template #default="scope">
                <el-input-number
                  v-model="scope.row.receipt_quantity"
                  :min="0"
                  :max="scope.row.order_quantity - scope.row.received_quantity"
                  :precision="2"
                  style="width: 100%"
                />
              </template>
            </el-table-column>
            <el-table-column prop="unit_price" label="单价" width="100" align="right" />
            <el-table-column prop="remark" label="备注" min-width="150">
              <template #default="scope">
                <el-input v-model="scope.row.remark" placeholder="输入备注" />
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </DialogComponent>

    <!-- 采购退货对话框 -->
    <DialogComponent
      v-model="returnFormVisible"
      title="采购退货"
      width="900px"
      :show-footer="true"
      :show-cancel-button="true"
      :show-confirm-button="true"
      cancel-text="取消"
      confirm-text="保存"
      :show-close="true"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      @confirm="handleReturnSave"
    >
      <div class="return-form">
        <el-form
          ref="returnFormRef"
          :model="returnForm"
          label-width="120px"
        >
          <el-form-item label="退货单编号" prop="return_no">
            <el-input v-model="returnForm.return_no" placeholder="自动生成" disabled />
          </el-form-item>
          <el-form-item label="采购订单" prop="purchase_order_id" required>
            <el-select
              v-model="returnForm.purchase_order_id"
              placeholder="选择采购订单"
              style="width: 100%"
              @change="handleReturnPurchaseOrderChange"
            >
              <el-option
                v-for="order in purchaseOrderList"
                :key="order.id"
                :label="order.order_no"
                :value="order.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="退货日期" prop="return_date" required>
            <el-date-picker
              v-model="returnForm.return_date"
              type="date"
              placeholder="选择退货日期"
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item label="仓库" prop="warehouse_id" required>
            <el-select
              v-model="returnForm.warehouse_id"
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
          <el-form-item label="退货原因" prop="return_reason" required>
          <el-input
            v-model="returnForm.return_reason"
            type="textarea"
            :rows="3"
            placeholder="输入退货原因"
          />
        </el-form-item>
          <el-form-item label="备注" prop="remark">
          <el-input
            v-model="returnForm.remark"
            type="textarea"
            :rows="3"
            placeholder="输入备注信息"
          />
        </el-form-item>
        </el-form>
        
        <!-- 采购退货明细 -->
        <div class="return-items">
          <h4>退货明细</h4>
          <el-table
            :data="returnItems"
            border
            style="width: 100%"
            max-height="400px"
          >
            <el-table-column prop="material_name" label="物料名称" min-width="150" />
            <el-table-column prop="material_code" label="物料编码" width="120" />
            <el-table-column prop="unit" label="单位" width="80" />
            <el-table-column prop="received_quantity" label="已收数量" width="100" align="right" />
            <el-table-column prop="return_quantity" label="退货数量" width="150">
              <template #default="scope">
                <el-input-number
                  v-model="scope.row.return_quantity"
                  :min="0"
                  :max="scope.row.received_quantity"
                  :precision="2"
                  style="width: 100%"
                />
              </template>
            </el-table-column>
            <el-table-column prop="unit_price" label="单价" width="100" align="right" />
            <el-table-column prop="remark" label="备注" min-width="150">
              <template #default="scope">
                <el-input v-model="scope.row.remark" placeholder="输入备注" />
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </DialogComponent>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ShoppingCart, Plus, Edit, Delete, RefreshLeft, View } from '@element-plus/icons-vue'
import { TableComponent, DialogComponent } from '../../../components/base'
import { unwrapResponseData } from '../../../api'
import { erpApi } from '../../../api/erp'
import { ErrorHandler } from '../../../utils/error-handler'
import { DataTransformer } from '../../../utils/data-transformer'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as XLSX from 'xlsx'
import type { PurchaseOrder, PurchaseOrderItem, PurchaseOrderStatus } from '../../../types/erp/supply-chain'

import type { Supplier } from '../../../types/erp/basic-data'
import { useVoiceContext } from '../../../composables/useVoiceContext'

// 注册语音上下文
useVoiceContext({
  id: 'purchase-view',
  description: '采购订单管理',
  actions: {
    'search': () => handleSearch(),
    'create': () => handleCreatePurchaseOrder(),
    'refresh': () => handleSearch(),
    'export': () => handleExportPurchaseOrders()
  },
  setters: {
    'supplier_name': (val: string) => { 
       (purchaseOrderForm.value as any).supplier_name = val 
       ElMessage.success(`已自动填写供应商: ${val}`)
    },
    'remark': (val: string) => { 
       purchaseOrderForm.value.remark = val
       ElMessage.success(`已自动填写备注: ${val}`)
    },
    'quantity': (val: number | string) => {
      // 假设填充第一项的数量作为演示
      const items = purchaseOrderForm.value.items
      if (items && items.length > 0) {
         (items[0] as any).quantity = Number(val)
         ElMessage.success(`已自动填写第一项数量: ${val}`)
      } else {
         ElMessage.warning('请先添加物料行再填写数量')
      }
    }
  }
})

// 响应式数据
const purchaseOrders = ref<PurchaseOrder[]>([])
const purchaseOrderTotal = ref(0)
const purchaseOrderLoading = ref(false)
const purchaseOrderPage = ref(1)
const purchaseOrderSize = ref(10)
const selectedRows = ref<PurchaseOrder[]>([])

// 采购订单状态映射
const statusMap: Record<string, { label: string; type: string }> = {
  'draft': { label: '草稿', type: 'warning' },
  'submitted': { label: '已提交', type: 'info' },
  'approved': { label: '已审核', type: 'success' },
  'processed': { label: '已处理', type: 'info' },
  'closed': { label: '已关闭', type: 'info' },
  'rejected': { label: '已拒绝', type: 'danger' }
}

// 采购订单筛选条件
const purchaseOrderFilters = [
  { prop: 'order_no', label: '订单编号', type: 'input' as 'input', placeholder: '请输入订单编号' },
  { prop: 'supplier_name', label: '供应商名称', type: 'input' as 'input', placeholder: '请输入供应商名称' },
  { prop: 'status', label: '状态', type: 'select' as 'select', options: [
    { label: '草稿', value: 'draft' },
    { label: '已提交', value: 'submitted' },
    { label: '已审核', value: 'approved' },
    { label: '已处理', value: 'processed' },
    { label: '已关闭', value: 'closed' },
    { label: '已拒绝', value: 'rejected' }
  ]},
  { prop: 'create_time', label: '创建时间', type: 'daterange' as 'daterange' }
] as any

// 采购订单列配置
const purchaseOrderColumns = [
  { prop: 'order_no', label: '订单编号', width: 150 },
  { prop: 'supplier_name', label: '供应商名称', width: 200 },
  { prop: 'purchase_date', label: '采购日期', width: 120 },
  { prop: 'expected_delivery_date', label: '期望交货日期', width: 150 },
  { prop: 'actual_delivery_date', label: '实际交货日期', width: 150 },
  { prop: 'buyer', label: '采购员', width: 100 },
  { prop: 'total_amount', label: '订单金额', width: 120, align: 'right' },
  { prop: 'payment_status', label: '付款状态', width: 100 },
  { prop: 'delivery_status', label: '交货状态', width: 100 },
  { prop: 'status', label: '订单状态', width: 100, slotName: 'status' },
  { prop: 'creator', label: '创建人', width: 100 },
  { prop: 'create_time', label: '创建时间', width: 180 },
  { prop: 'approval_time', label: '审核时间', width: 180 }
]

// 采购订单操作按钮
const purchaseOrderActions = [
  {
    text: '查看',
    type: 'primary',
    size: 'small',
    icon: View,
    handler: (row: any) => {
      handleViewPurchaseOrder(row)
    }
  },
  {
    text: '编辑',
    type: 'info',
    size: 'small',
    icon: Edit,
    handler: (row: any) => {
      handleEditPurchaseOrder(row)
    }
  },
  {
    text: '审核',
    type: 'success',
    size: 'small',
    icon: Edit,
    handler: (row: any) => {
      handleApprovePurchaseOrder(row.id)
    }
  },
  {
    text: '收货',
    type: 'success',
    size: 'small',
    icon: ShoppingCart,
    handler: (row: any) => {
      handleOpenReceiptDialog(row)
    }
  },
  {
    text: '退货',
    type: 'warning',
    size: 'small',
    icon: Delete,
    handler: (row: any) => {
      handleOpenReturnDialog(row)
    }
  },
  {
    text: '关闭',
    type: 'warning',
    size: 'small',
    icon: Delete,
    handler: (row: any) => {
      handleClosePurchaseOrder(row.id)
    }
  },
  {
    text: '删除',
    type: 'danger',
    size: 'small',
    icon: Delete,
    handler: (row: any) => {
      handleDeletePurchaseOrder(row.id)
    }
  }
]

// 表格操作按钮
const tableActions = [
  {
    key: 'add',
    text: '新增采购订单',
    type: 'primary',
    icon: Plus,
    handler: () => {
      handleCreatePurchaseOrder()
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

// 定义类型 - 使用导入的PurchaseOrderItem
// interface OrderItem 已由 PurchaseOrderItem 替代

interface ReceiptItem {
  material_id: string
  material_name: string
  material_code: string
  unit: string
  order_quantity: number
  received_quantity: number
  receipt_quantity: number
  unit_price: number
  remark: string
}

interface ReturnItem {
  material_id: string
  material_name: string
  material_code: string
  unit: string
  received_quantity: number
  return_quantity: number
  unit_price: number
  remark: string
}

// 采购订单创建/编辑相关数据
const purchaseOrderFormVisible = ref(false)
const purchaseOrderFormRef = ref<any>(null)
const isEditMode = ref(false)
const purchaseOrderForm = ref<Partial<PurchaseOrder> & { items: PurchaseOrderItem[] }>({
  id: undefined,
  order_no: '',
  supplier_id: '',
  supplier_name: '',
  purchase_date: new Date(),
  expected_delivery_date: new Date(),
  actual_delivery_date: undefined,
  buyer: '',
  total_amount: 0,
  payment_status: '未付款',
  delivery_status: '未交货',
  status: 'draft' as PurchaseOrderStatus,
  creator: '',
  create_time: '',
  approval_time: '',
  remark: '',
  items: [] as PurchaseOrderItem[]
})

// 采购收货相关数据
const receiptFormVisible = ref(false)
const receiptFormRef = ref<any>(null)
const receiptForm = ref({
  receipt_no: '',
  purchase_order_id: '',
  receipt_date: new Date(),
  warehouse_id: '',
  remark: '',
  receipt_items: [] as any[]
})

// 采购退货相关数据
const returnFormVisible = ref(false)
const returnFormRef = ref<any>(null)
const returnForm = ref({
  return_no: '',
  purchase_order_id: '',
  return_date: new Date(),
  warehouse_id: '',
  supplier_id: '',
  supplier_name: '',
  return_reason: '',
  remark: '',
  return_items: [] as any[]
})

// 基础数据列表
const purchaseOrderList = ref<PurchaseOrder[]>([])
const warehouseList = ref<any[]>([])
const supplierList = ref<Supplier[]>([])
const materialList = ref<any[]>([])

// 收货和退货明细数据
const receiptItems = ref<ReceiptItem[]>([])
const returnItems = ref<ReturnItem[]>([])

// 初始加载
onMounted(() => {
  handleSearch()
  getPurchaseOrdersForSelect()
  getWarehouseList()
  getSupplierList()
  getMaterialList()
})

// 获取采购订单列表
const getPurchaseOrderList = async (params: any) => {
  try {
    purchaseOrderLoading.value = true
    const result = await erpApi.supplyChain.getPurchaseOrders({
      page: params.page || purchaseOrderPage.value,
      size: params.size || purchaseOrderSize.value,
      ...params
    })
    const normalizedData = DataTransformer.normalizeResponse(result)
    // 确保数据是数组类型，避免将Object类型的数据传递给TableComponent
    let data = normalizedData?.data?.list || normalizedData?.data || []
    if (!Array.isArray(data)) {
      data = []
    }
    purchaseOrders.value = data
    // 确保total是数字类型
    purchaseOrderTotal.value = Number(normalizedData?.data?.total) || data.length || 0
    purchaseOrderPage.value = params.page || purchaseOrderPage.value
    purchaseOrderSize.value = params.size || purchaseOrderSize.value
  } catch (error) {
    ErrorHandler.handleApiError(error, false)
    purchaseOrders.value = []
    purchaseOrderTotal.value = 0
  } finally {
    purchaseOrderLoading.value = false
  }
}

// 搜索采购订单
const handleSearch = (params: any = {}) => {
  getPurchaseOrderList(params)
}

// 分页大小改变
const handleSizeChange = (size: number) => {
  purchaseOrderSize.value = size
  getPurchaseOrderList({ page: purchaseOrderPage.value, size })
}

// 页码改变
const handleCurrentChange = (page: number) => {
  purchaseOrderPage.value = page
  getPurchaseOrderList({ page, size: purchaseOrderSize.value })
}

// 选择行改变
const handleSelectionChange = (rows: PurchaseOrder[]) => {
  selectedRows.value = rows
}

// 状态标签类型
const getStatusType = (status: string) => {
  return statusMap[status]?.type || 'info'
}

// 状态标签文本
const getStatusLabel = (status: string) => {
  return statusMap[status]?.label || status
}

// 获取采购订单下拉列表
const getPurchaseOrdersForSelect = async () => {
  try {
    const result = await erpApi.supplyChain.getPurchaseOrders({ page: 1, size: 100 })
    const normalizedData = DataTransformer.normalizeResponse(result)
    purchaseOrderList.value = normalizedData?.data?.list || []
  } catch (error) {
    ErrorHandler.handleApiError(error, false)
    purchaseOrderList.value = []
  }
}

// 获取仓库列表
const getWarehouseList = async () => {
  try {
    const result = await erpApi.basicData.getWarehouses()
    const normalizedData = DataTransformer.normalizeResponse(result)
    warehouseList.value = normalizedData?.data?.list || []
  } catch (error) {
    ErrorHandler.handleApiError(error, false)
    warehouseList.value = []
  }
}

const handleViewPurchaseOrder = async (row: any) => {
  try {
    const detail = await (erpApi.supplyChain as any).getPurchaseOrderById(String(row.id))
    const data = unwrapResponseData<any>(detail) || row
    ElMessageBox.alert(`<pre>${JSON.stringify(data, null, 2)}</pre>`, '采购订单详情', { dangerouslyUseHTMLString: true })
  } catch (error) {
    ErrorHandler.handleApiError(error)
  }
}

const handleExportPurchaseOrders = async () => {
  try {
    const result = await erpApi.supplyChain.getPurchaseOrders({ page: 1, size: 1000 })
    const normalizedData = DataTransformer.normalizeResponse(result)
    const list = Array.isArray(normalizedData?.data?.list) ? normalizedData.data.list : []

    const headers = ['订单编号', '供应商名称', '采购日期', '期望交货日期', '采购员', '订单金额', '订单状态', '创建人', '创建时间']
    const worksheetData = [
      headers,
      ...list.map((row: any) => [
        row.order_no,
        row.supplier_name,
        row.purchase_date,
        row.expected_delivery_date,
        row.buyer,
        row.total_amount,
        row.status,
        row.creator,
        row.create_time
      ])
    ]
    const worksheet = XLSX.utils.aoa_to_sheet(worksheetData)
    const workbook = XLSX.utils.book_new()
    XLSX.utils.book_append_sheet(workbook, worksheet, '采购订单')
    XLSX.writeFile(workbook, `采购订单_${new Date().toISOString().slice(0, 10)}.xlsx`)
    ElMessage.success('导出成功')
  } catch (error) {
    ErrorHandler.handleApiError(error)
  }
}



// 审核采购订单
const handleApprovePurchaseOrder = async (id: string) => {
  try {
    const result = await erpApi.supplyChain.approvePurchaseOrder(id)
    if (result) {
      ElMessage.success('采购订单审核成功')
      handleSearch() // 刷新采购订单列表
    }
  } catch (error) {
    ErrorHandler.handleApiError(error)
  } finally {
    purchaseOrderLoading.value = false
  }
}

// 关闭采购订单
const handleClosePurchaseOrder = async (id: string) => {
  try {
    purchaseOrderLoading.value = true
    const result = await erpApi.supplyChain.updatePurchaseOrder(id, { status: 'closed' })
    if (result) {
      ElMessage.success('采购订单关闭成功')
      handleSearch() // 刷新采购订单列表
    }
  } catch (error) {
    ErrorHandler.handleApiError(error)
  } finally {
    purchaseOrderLoading.value = false
  }
}

// 删除采购订单
const handleDeletePurchaseOrder = async (id: string) => {
  try {
    purchaseOrderLoading.value = true
    // 注意：'deleted' 不在 PurchaseOrderStatus 枚举中，使用 'closed' 代替
    const result = await erpApi.supplyChain.updatePurchaseOrder(id, { status: 'closed' as PurchaseOrderStatus })
    if (result) {
      ElMessage.success('采购订单删除成功')
      handleSearch() // 刷新采购订单列表
    }
  } catch (error) {
    ErrorHandler.handleApiError(error)
  } finally {
    purchaseOrderLoading.value = false
  }
}

// ====================== 采购订单创建/编辑功能 ======================

// 打开创建采购订单对话框
const handleCreatePurchaseOrder = () => {
  isEditMode.value = false
  purchaseOrderForm.value = {
    id: undefined,
    order_no: '',
    supplier_id: '',
    supplier_name: '',
    purchase_date: new Date(),
    expected_delivery_date: new Date(),
    actual_delivery_date: '',
    buyer: '',
    total_amount: 0,
    payment_status: '未付款',
    delivery_status: '未交货',
    status: 'draft',
    creator: '',
    create_time: '',
    approval_time: '',
    remark: '',
    items: []
  }
  handleAddOrderItem() // 添加一个默认的订单明细
  purchaseOrderFormVisible.value = true
}

// 打开编辑采购订单对话框
const handleEditPurchaseOrder = async (row: any) => {
  try {
    isEditMode.value = true
    const detail = await (erpApi.supplyChain as any).getPurchaseOrderById(String(row.id))
    purchaseOrderForm.value = unwrapResponseData<any>(detail) || { ...row }
    if (!purchaseOrderForm.value.items) purchaseOrderForm.value.items = []
    purchaseOrderFormVisible.value = true
  } catch (error) {
    ErrorHandler.handleApiError(error)
    purchaseOrderForm.value = { ...row }
    if (!purchaseOrderForm.value.items) purchaseOrderForm.value.items = []
    purchaseOrderFormVisible.value = true
  }
}

// 添加采购订单明细
const handleAddOrderItem = () => {
  purchaseOrderForm.value.items.push({
    id: '',
    material_id: '',
    material_code: '',
    material_name: '',
    quantity: 0,
    unit_price: 0,
    amount: 0,
    unit: '',
    remark: ''
  })
}

// 删除采购订单明细
const handleDeleteOrderItem = (index: number) => {
  purchaseOrderForm.value.items.splice(index, 1)
}

// 保存采购订单
const handlePurchaseOrderSave = async () => {
  if (!purchaseOrderFormRef.value) return
  
  try {
    await purchaseOrderFormRef.value.validate()
    
    // 计算总金额
    purchaseOrderForm.value.total_amount = purchaseOrderForm.value.items.reduce((total: number, item: any) => {
      return total + (item.quantity * item.unit_price)
    }, 0)
    
    let result
    if (isEditMode.value && purchaseOrderForm.value.id) {
      result = await erpApi.supplyChain.updatePurchaseOrder(String(purchaseOrderForm.value.id), purchaseOrderForm.value as any)
    } else {
      result = await erpApi.supplyChain.createPurchaseOrder(purchaseOrderForm.value as any)
    }
    
    if (result) {
      purchaseOrderFormVisible.value = false
      ElMessage.success(isEditMode.value ? '采购订单编辑成功' : '采购订单创建成功')
      handleSearch() // 刷新采购订单列表
    }
  } catch (error) {
    ErrorHandler.handleApiError(error)
  }
}

// ====================== 采购收货功能 ======================

// 打开采购收货对话框
const handleOpenReceiptDialog = (row: any) => {
  // 初始化表单数据
  receiptForm.value = {
    receipt_no: '',
    purchase_order_id: row.id,
    receipt_date: new Date(),
    warehouse_id: '',
    remark: '',
    receipt_items: []
  }
  receiptItems.value = []
  receiptFormVisible.value = true
  
  // 加载采购订单明细
  loadPurchaseOrderDetails(row.id)
}

// 加载采购订单明细
const loadPurchaseOrderDetails = async (orderId: string) => {
  try {
    const detail = await (erpApi.supplyChain as any).getPurchaseOrderById(String(orderId))
    const order = unwrapResponseData<any>(detail) || {}
    const items = Array.isArray(order.items) ? order.items : []
    receiptItems.value = items.map((item: any) => ({
      material_id: item.material_id || item.materialId || '',
      material_name: item.material_name || item.materialName || '',
      material_code: item.material_code || item.materialCode || '',
      unit: item.unit || '',
      order_quantity: Number(item.quantity || item.expectedQty || 0),
      received_quantity: Number(item.received_quantity || item.receivedQty || 0),
      receipt_quantity: 0,
      unit_price: Number(item.unit_price || item.unitPrice || 0),
      remark: item.remark || ''
    }))
  } catch (error) {
    ErrorHandler.handleApiError(error)
    receiptItems.value = []
  }
}

// 采购订单选择变化时加载明细
const handlePurchaseOrderChange = (orderId: string) => {
  loadPurchaseOrderDetails(orderId)
}

// 保存采购收货
const handleReceiptSave = async () => {
  if (!receiptFormRef.value) return
  
  try {
    await receiptFormRef.value.validate()
    
    // 准备收货明细数据
    receiptForm.value.receipt_items = receiptItems.value
      .filter((item: any) => item.receipt_quantity > 0)
      .map((item: any) => ({
        material_id: item.material_id,
        quantity: item.receipt_quantity,
        unit_price: item.unit_price,
        remark: item.remark
      }))
    
    if (receiptForm.value.receipt_items.length === 0) {
      ElMessage.warning('请至少输入一条收货明细')
      return
    }
    
    const result = await erpApi.supplyChain.createPurchaseReceipt(receiptForm.value)
    if (result) {
      receiptFormVisible.value = false
      ElMessage.success('采购收货保存成功')
      handleSearch() // 刷新采购订单列表
    }
  } catch (error) {
    ErrorHandler.handleApiError(error)
  }
}

// ====================== 采购退货功能 ======================

// 打开采购退货对话框
const handleOpenReturnDialog = (row: any) => {
  // 初始化表单数据
  returnForm.value = {
    return_no: '',
    purchase_order_id: row.id,
    return_date: new Date(),
    warehouse_id: '',
    supplier_id: row.supplier_id || '',
    supplier_name: row.supplier_name || '',
    return_reason: '',
    remark: '',
    return_items: []
  }
  returnItems.value = []
  returnFormVisible.value = true
  
  // 加载采购订单明细
  loadPurchaseOrderDetailsForReturn(row.id)
}

// 加载采购订单明细用于退货
const loadPurchaseOrderDetailsForReturn = async (orderId: string) => {
  try {
    const detail = await (erpApi.supplyChain as any).getPurchaseOrderById(String(orderId))
    const order = unwrapResponseData<any>(detail) || {}
    const items = Array.isArray(order.items) ? order.items : []
    returnItems.value = items.map((item: any) => ({
      material_id: item.material_id || item.materialId || '',
      material_name: item.material_name || item.materialName || '',
      material_code: item.material_code || item.materialCode || '',
      unit: item.unit || '',
      received_quantity: Number(item.received_quantity || item.receivedQuantity || item.quantity || 0),
      return_quantity: 0,
      unit_price: Number(item.unit_price || item.unitPrice || 0),
      remark: ''
    }))
  } catch (error) {
    ErrorHandler.handleApiError(error)
    returnItems.value = []
  }
}

// 采购订单选择变化时加载退货明细
const handleReturnPurchaseOrderChange = (orderId: string) => {
  loadPurchaseOrderDetailsForReturn(orderId)
}

// 保存采购退货
const handleReturnSave = async () => {
  if (!returnFormRef.value) return
  
  try {
    await returnFormRef.value.validate()
    
    // 准备退货明细数据
    returnForm.value.return_items = returnItems.value
      .filter((item: any) => item.return_quantity > 0)
      .map((item: any) => ({
        material_id: item.material_id,
        quantity: item.return_quantity,
        unit_price: item.unit_price,
        remark: item.remark
      }))
    
    if (returnForm.value.return_items.length === 0) {
      ElMessage.warning('请至少输入一条退货明细')
      return
    }

    const payload: any = {
      warehouse_code: returnForm.value.warehouse_id,
      supplier_code: returnForm.value.supplier_id || '',
      supplier_name: returnForm.value.supplier_name || '',
      remark: returnForm.value.remark,
      return_items: returnForm.value.return_items
    }
    const result = await (erpApi.supplyChain as any).createPurchaseReturn(payload)
    if (result) {
      ElMessage.success('采购退货保存成功')
      returnFormVisible.value = false
      handleSearch()
    }
  } catch (error) {
    ErrorHandler.handleApiError(error)
  }
}

// ====================== 基础数据获取功能 ======================

// 获取供应商列表
const getSupplierList = async () => {
  try {
    const result = await erpApi.basicData.getSuppliers({ page: 1, size: 200 })
    const normalizedData = DataTransformer.normalizeResponse(result)
    supplierList.value = normalizedData?.data?.list || normalizedData?.data || []
  } catch (error) {
    ErrorHandler.handleApiError(error, false)
    supplierList.value = []
  }
}

// 获取物料列表
const getMaterialList = async () => {
  try {
    const result = await erpApi.basicData.getMaterials({ page: 1, size: 200 })
    const normalizedData = DataTransformer.normalizeResponse(result)
    materialList.value = normalizedData?.data?.list || normalizedData?.data || []
  } catch (error) {
    ErrorHandler.handleApiError(error, false)
    materialList.value = []
  }
}
</script>

<style scoped lang="scss">
.purchase-view {
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

// 采购订单表单样式
.purchase-order-form {
  .purchase-order-detail {
    margin-top: 20px;
    
    h4 {
      margin-bottom: 16px;
      font-size: 16px;
      font-weight: bold;
      color: #333;
    }
    
    .table-actions {
      margin-top: 16px;
      text-align: right;
    }
  }
}

// 采购收货表单样式
.receipt-form {
  .receipt-items {
    margin-top: 20px;
    
    h4 {
      margin-bottom: 16px;
      font-size: 16px;
      font-weight: bold;
      color: #333;
    }
  }
}

// 采购退货表单样式
.return-form {
  .return-items {
    margin-top: 20px;
    
    h4 {
      margin-bottom: 16px;
      font-size: 16px;
      font-weight: bold;
      color: #333;
    }
  }
}

// 响应式设计
@media (max-width: 1024px) {
  .purchase-view {
    padding: 16px;
  }
  
  .purchase-order-form,
  .receipt-form,
  .return-form {
    padding: 0 8px;
  }
}

@media (max-width: 768px) {
  .purchase-view {
    padding: 12px;
    
    .module-card {
      .card-header {
        font-size: 18px;
      }
    }
  }
  
  .purchase-order-form,
  .receipt-form,
  .return-form {
    padding: 0;
    
    .purchase-order-detail,
    .receipt-items,
    .return-items {
      h4 {
        font-size: 14px;
      }
      
      :deep(.el-table) {
        font-size: 12px;
      }
    }
  }
}
</style>
