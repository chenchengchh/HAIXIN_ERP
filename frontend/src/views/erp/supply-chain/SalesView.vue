<template>
  <div class="sales-view">
    <el-card class="module-card">
      <template #header>
        <div class="card-header">
          <el-icon><ShoppingBag /></el-icon>
          <span>销售管理</span>
        </div>
      </template>
      
      <!-- 销售订单列表 -->
      <TableComponent
        :data="salesOrders"
        :columns="salesOrderColumns"
        :total="salesOrderTotal"
        :loading="salesOrderLoading"
        :show-index="true"
        :show-selection="true"
        :show-action="true"
        :actions="salesOrderActions"
        :table-actions="tableActions"
        :filters="salesOrderFilters"
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
  </div>
  
  <!-- 销售发货对话框 -->
  <DialogComponent
    v-model="deliveryFormVisible"
    title="销售发货"
    width="800px"
    :show-footer="true"
    :show-cancel-button="true"
    :show-confirm-button="true"
    cancel-text="取消"
    confirm-text="保存"
    :show-close="true"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
    @confirm="handleDeliverySave"
  >
    <div class="delivery-form">
      <el-form
        ref="deliveryFormRef"
        :model="deliveryForm"
        label-width="120px"
      >
        <el-form-item label="发货单号" prop="delivery_no">
          <el-input v-model="deliveryForm.delivery_no" placeholder="自动生成" disabled />
        </el-form-item>
        <el-form-item label="销售订单" prop="sales_order_id" required>
          <el-select
            v-model="deliveryForm.sales_order_id"
            placeholder="选择销售订单"
            style="width: 100%"
          >
            <el-option
              v-for="order in salesOrderList"
              :key="order.id"
              :label="order.order_no"
              :value="order.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="发货日期" prop="delivery_date" required>
          <el-date-picker
            v-model="deliveryForm.delivery_date"
            type="date"
            placeholder="选择发货日期"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="仓库" prop="warehouse_id" required>
          <el-select
            v-model="deliveryForm.warehouse_id"
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
        <el-form-item label="物流单号" prop="logistics_no">
          <el-input v-model="deliveryForm.logistics_no" placeholder="输入物流单号" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="deliveryForm.remark"
            type="textarea"
            :rows="3"
            placeholder="输入备注信息"
          />
        </el-form-item>
      </el-form>
    </div>
  </DialogComponent>

  <DialogComponent
    v-model="orderFormVisible"
    :title="orderFormTitle"
    width="800px"
    :show-footer="true"
    :show-cancel-button="true"
    :show-confirm-button="true"
    cancel-text="取消"
    confirm-text="保存"
    :show-close="true"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
    @confirm="handleOrderSave"
  >
    <div class="order-form">
      <el-form ref="orderFormRef" :model="orderForm" label-width="120px">
        <el-form-item label="订单编号" prop="order_no">
          <el-input v-model="orderForm.order_no" placeholder="留空自动生成" />
        </el-form-item>
        <el-form-item label="客户名称" prop="customer_name" required>
          <el-input v-model="orderForm.customer_name" placeholder="请输入客户名称" />
        </el-form-item>
        <el-form-item label="订单金额" prop="total_amount" required>
          <el-input-number v-model="orderForm.total_amount" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="订单日期" prop="order_date" required>
          <el-date-picker v-model="orderForm.order_date" type="date" style="width: 100%" />
        </el-form-item>
        <el-form-item label="交货日期" prop="delivery_date">
          <el-date-picker v-model="orderForm.delivery_date" type="date" style="width: 100%" />
        </el-form-item>
        <el-form-item label="订单状态" prop="status">
          <el-select v-model="orderForm.status" style="width: 200px">
            <el-option label="草稿" value="draft" />
            <el-option label="已确认" value="confirmed" />
            <el-option label="处理中" value="processing" />
            <el-option label="已发货" value="shipped" />
            <el-option label="已完成" value="completed" />
            <el-option label="已取消" value="cancelled" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="orderForm.remark" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
    </div>
  </DialogComponent>
</template>

<script setup lang="ts">
import { computed, ref, onMounted } from 'vue'
import { ShoppingBag, Plus, Edit, Delete, RefreshLeft, View, Check, Close } from '@element-plus/icons-vue'
import { TableComponent, DialogComponent } from '../../../components/base'
import { unwrapResponseData } from '../../../api'
import { erpApi } from '../../../api/erp'
import { ErrorHandler } from '../../../utils/error-handler'
import { DataTransformer } from '../../../utils/data-transformer'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { SalesOrder, SalesOrderStatus } from '../../../types/erp/supply-chain'

// 响应式数据
const salesOrders = ref<SalesOrder[]>([])
const salesOrderTotal = ref(0)
const salesOrderLoading = ref(false)
const salesOrderPage = ref(1)
const salesOrderSize = ref(10)
const selectedRows = ref<SalesOrder[]>([])

const orderFormVisible = ref(false)
const isOrderEdit = ref(false)
const orderEditId = ref<number | null>(null)
const orderFormRef = ref<any>(null)
const orderFormTitle = computed(() => (isOrderEdit.value ? '编辑销售订单' : '新增销售订单'))
const orderForm = ref<any>({
  order_no: '',
  customer_name: '',
  total_amount: 0,
  order_date: new Date(),
  delivery_date: '',
  status: 'draft' as SalesOrderStatus,
  remark: ''
})

// 销售订单状态映射
const statusMap: Record<string, { label: string; type: string }> = {
  'draft': { label: '草稿', type: 'warning' },
  'confirmed': { label: '已确认', type: 'info' },
  'processing': { label: '处理中', type: 'primary' },
  'shipped': { label: '已发货', type: 'success' },
  'completed': { label: '已完成', type: 'success' },
  'cancelled': { label: '已取消', type: 'danger' }
}

// 销售订单筛选条件
const salesOrderFilters = [
  { prop: 'order_no', label: '订单编号', type: 'input' as 'input', placeholder: '请输入订单编号' },
  { prop: 'customer_name', label: '客户名称', type: 'input' as 'input', placeholder: '请输入客户名称' },
  { prop: 'status', label: '订单状态', type: 'select' as 'select', options: [
    { label: '草稿', value: 'draft' },
    { label: '已确认', value: 'confirmed' },
    { label: '处理中', value: 'processing' },
    { label: '已发货', value: 'shipped' },
    { label: '已完成', value: 'completed' },
    { label: '已取消', value: 'cancelled' }
  ]},
  { prop: 'order_date', label: '订单日期', type: 'daterange' as 'daterange' },
  { prop: 'delivery_date', label: '交货日期', type: 'daterange' as 'daterange' }
] as any

// 销售订单列配置
const salesOrderColumns = [
  { prop: 'order_no', label: '订单编号', width: 150 },
  { prop: 'customer_name', label: '客户名称', width: 200 },
  { prop: 'total_amount', label: '订单金额', width: 120, align: 'right' },
  { prop: 'order_date', label: '订单日期', width: 150 },
  { prop: 'delivery_date', label: '交货日期', width: 150 },
  { prop: 'status', label: '订单状态', width: 120, slotName: 'status' },
  { prop: 'salesperson', label: '业务员', width: 120 },
  { prop: 'create_time', label: '创建时间', width: 180 }
]

// 销售订单操作按钮
const salesOrderActions = [
  {
    text: '查看',
    type: 'primary',
    size: 'small',
    icon: View,
    handler: (row: any) => {
      handleViewSalesOrder(row)
    }
  },
  {
    text: '编辑',
    type: 'info',
    size: 'small',
    icon: Edit,
    handler: (row: any) => {
      handleEditSalesOrder(row)
    }
  },
  {
    text: '提交',
    type: 'warning',
    size: 'small',
    icon: Check,
    handler: (row: any) => {
      handleSubmitSalesOrder(row)
    }
  },
  {
    text: '审核',
    type: 'success',
    size: 'small',
    icon: Check,
    handler: (row: any) => {
      handleApproveSalesOrder(row)
    }
  },
  {
    text: '发货',
    type: 'success',
    size: 'small',
    icon: Check,
    handler: (row: any) => {
      handleOrderShipment(row)
    }
  },
  {
    text: '取消',
    type: 'danger',
    size: 'small',
    icon: Close,
    handler: (row: any) => {
      handleCancelSalesOrder(row)
    }
  },
  {
    text: '删除',
    type: 'danger',
    size: 'small',
    icon: Delete,
    handler: (row: any) => {
      handleDeleteSalesOrder(row)
    }
  }
]

// 表格操作按钮
const tableActions = [
  {
    key: 'add',
    text: '新增销售订单',
    type: 'primary',
    icon: Plus,
    handler: () => {
      handleCreateSalesOrder()
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

// 销售发货相关数据
const deliveryFormVisible = ref(false)
const deliveryFormRef = ref<any>(null)
const deliveryForm = ref({
  delivery_no: '',
  sales_order_id: '',
  delivery_date: new Date(),
  warehouse_id: '',
  logistics_no: '',
  remark: '',
  delivery_items: []
})
const salesOrderList = ref<SalesOrder[]>([])
const warehouseList = ref<any[]>([])

// 初始加载
onMounted(() => {
  handleSearch()
  getSalesOrdersForSelect()
  getWarehouseList()
})

// 获取销售订单列表
const getSalesOrderList = async (params: any) => {
  try {
    salesOrderLoading.value = true
    const result = await erpApi.supplyChain.getSalesOrders({
      page: params.page || salesOrderPage.value,
      size: params.size || salesOrderSize.value,
      ...params
    })
    const normalizedData = DataTransformer.normalizeResponse(result)
    salesOrders.value = normalizedData?.data?.list || normalizedData?.data || []
    salesOrderTotal.value = Number(normalizedData?.data?.total) || salesOrders.value.length || 0
    salesOrderPage.value = params.page || salesOrderPage.value
    salesOrderSize.value = params.size || salesOrderSize.value
  } catch (error) {
    ErrorHandler.handleApiError(error, false)
    salesOrders.value = []
    salesOrderTotal.value = 0
  } finally {
    salesOrderLoading.value = false
  }
}

// 搜索销售订单
const handleSearch = (params: any = {}) => {
  getSalesOrderList(params)
}

// 分页大小改变
const handleSizeChange = (size: number) => {
  salesOrderSize.value = size
  getSalesOrderList({ page: salesOrderPage.value, size })
}

// 页码改变
const handleCurrentChange = (page: number) => {
  salesOrderPage.value = page
  getSalesOrderList({ page, size: salesOrderSize.value })
}

// 选择行改变
const handleSelectionChange = (rows: SalesOrder[]) => {
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

// 获取销售订单下拉列表
const getSalesOrdersForSelect = async () => {
  try {
    const result = await erpApi.supplyChain.getSalesOrders({ page: 1, size: 100 })
    const normalizedData = DataTransformer.normalizeResponse(result)
    salesOrderList.value = normalizedData?.data?.list || []
  } catch (error) {
    ErrorHandler.handleApiError(error, false)
    salesOrderList.value = []
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

const formatDate = (v: any) => {
  if (!v) return ''
  const d = v instanceof Date ? v : new Date(v)
  if (Number.isNaN(d.getTime())) return ''
  return d.toISOString().slice(0, 10)
}

const handleCreateSalesOrder = () => {
  isOrderEdit.value = false
  orderEditId.value = null
  orderForm.value = { order_no: '', customer_name: '', total_amount: 0, order_date: new Date(), delivery_date: '', status: 'draft', remark: '' }
  orderFormVisible.value = true
}

const handleEditSalesOrder = (row: any) => {
  isOrderEdit.value = true
  orderEditId.value = Number(row.id)
  orderForm.value = {
    order_no: row.order_no || '',
    customer_name: row.customer_name || '',
    total_amount: Number(row.total_amount || 0),
    order_date: row.order_date ? new Date(row.order_date) : new Date(),
    delivery_date: row.delivery_date ? new Date(row.delivery_date) : '',
    status: (row.status || 'draft') as SalesOrderStatus,
    remark: row.remark || ''
  }
  orderFormVisible.value = true
}

const handleOrderSave = async () => {
  if (!orderFormRef.value) return
  try {
    await orderFormRef.value.validate()
    const payload: any = {
      order_no: orderForm.value.order_no,
      customer_name: orderForm.value.customer_name,
      total_amount: orderForm.value.total_amount,
      order_date: formatDate(orderForm.value.order_date),
      delivery_date: formatDate(orderForm.value.delivery_date),
      status: orderForm.value.status,
      remark: orderForm.value.remark
    }
    if (isOrderEdit.value && orderEditId.value) {
      await erpApi.supplyChain.updateSalesOrder(String(orderEditId.value), payload)
      ElMessage.success('销售订单更新成功')
    } else {
      await (erpApi.supplyChain as any).createSalesOrder(payload)
      ElMessage.success('销售订单创建成功')
    }
    orderFormVisible.value = false
    handleSearch()
    getSalesOrdersForSelect()
  } catch (error) {
    ErrorHandler.handleApiError(error)
  }
}

const handleViewSalesOrder = async (row: any) => {
  try {
    const detail = await (erpApi.supplyChain as any).getSalesOrderById(String(row.id))
    const data = unwrapResponseData<any>(detail) || row
    ElMessageBox.alert(`<pre>${JSON.stringify(data, null, 2)}</pre>`, '销售订单详情', { dangerouslyUseHTMLString: true })
  } catch (error) {
    ErrorHandler.handleApiError(error)
  }
}

const handleDeleteSalesOrder = (row: any) => {
  ElMessageBox.confirm(`确定要删除销售订单\"${row.order_no}\"吗？`, '删除确认',
    { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
  ).then(async () => {
    try {
      await (erpApi.supplyChain as any).deleteSalesOrder(row.id)
      ElMessage.success('删除成功')
      handleSearch()
      getSalesOrdersForSelect()
    } catch (error) {
      ErrorHandler.handleApiError(error)
    }
  }).catch(() => {})
}

const handleCancelSalesOrder = (row: any) => {
  ElMessageBox.confirm(`确定要取消销售订单\"${row.order_no}\"吗？`, '取消确认',
    { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
  ).then(async () => {
    try {
      await erpApi.supplyChain.updateSalesOrder(String(row.id), { status: 'cancelled' as any })
      ElMessage.success('取消成功')
      handleSearch()
    } catch (error) {
      ErrorHandler.handleApiError(error)
    }
  }).catch(() => {})
}

const handleSubmitSalesOrder = (row: any) => {
  ElMessageBox.confirm(`确认提交销售订单\"${row.order_no}\"吗？`, '提交确认',
    { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
  ).then(async () => {
    try {
      await (erpApi.supplyChain as any).submitSalesOrder(row.id)
      ElMessage.success('提交成功')
      handleSearch()
    } catch (error) {
      ErrorHandler.handleApiError(error)
    }
  }).catch(() => {})
}

const handleApproveSalesOrder = (row: any) => {
  ElMessageBox.confirm(`确认审核销售订单\"${row.order_no}\"吗？`, '审核确认',
    { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
  ).then(async () => {
    try {
      await (erpApi.supplyChain as any).approveSalesOrder(row.id)
      ElMessage.success('审核成功')
      handleSearch()
    } catch (error) {
      ErrorHandler.handleApiError(error)
    }
  }).catch(() => {})
}

// 打开销售发货对话框
const handleOpenDeliveryDialog = (row: any) => {
  // 初始化表单数据
  deliveryForm.value = {
    delivery_no: '',
    sales_order_id: row.id,
    delivery_date: new Date(),
    warehouse_id: '',
    logistics_no: '',
    remark: '',
    delivery_items: []
  }
  deliveryFormVisible.value = true
}

// 保存销售发货
const handleDeliverySave = async () => {
  if (!deliveryFormRef.value) return
  
  try {
    await deliveryFormRef.value.validate()
    const result = await erpApi.supplyChain.createSalesDelivery(deliveryForm.value)
    if (result) {
      deliveryFormVisible.value = false
      ElMessage.success('销售发货保存成功')
      handleSearch() // 刷新销售订单列表
    }
  } catch (error) {
    ErrorHandler.handleApiError(error)
  }
}

// 销售订单发货处理
const handleOrderShipment = (row: any) => {
  if (row.status !== 'confirmed' && row.status !== 'processing') {
    ElMessage.warning('只有已确认或处理中的订单才能发货')
    return
  }
  handleOpenDeliveryDialog(row)
}
</script>

<style scoped lang="scss">
.sales-view {
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
  .sales-view {
    padding: 16px;
  }
}

@media (max-width: 768px) {
  .sales-view {
    padding: 12px;
    
    .module-card {
      .card-header {
        font-size: 18px;
      }
    }
  }
}
</style>
