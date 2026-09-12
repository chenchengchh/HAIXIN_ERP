<template>
  <div class="sales-orders-view">
    <!-- 页面操作栏 -->
    <div class="page-actions">
      <el-input
        v-model="searchForm.keyword"
        placeholder="搜索订单编号、客户名称"
        style="width: 300px; margin-right: 10px;"
        clearable
      >
        <template #append>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-button type="primary" @click="handleSearch">
        <el-icon><Search /></el-icon> 搜索
      </el-button>
      <el-button type="primary" @click="handleCreate">
        <el-icon><Plus /></el-icon> 新建订单
      </el-button>
      <el-button @click="handleExport">
        <el-icon><Download /></el-icon> 导出
      </el-button>
    </div>

    <!-- 搜索条件展开面板 -->
    <el-collapse v-model="activeSearchPanel" style="margin-bottom: 20px;">
      <el-collapse-item title="高级搜索" name="1">
        <div class="advanced-search">
          <el-form :model="searchForm" inline>
            <el-form-item label="订单状态">
              <el-select v-model="searchForm.status" placeholder="选择状态" clearable>
                <el-option label="草稿" value="draft" />
                <el-option label="已提交" value="submitted" />
                <el-option label="已审批" value="approved" />
                <el-option label="已拒绝" value="rejected" />
                <el-option label="进行中" value="in_progress" />
                <el-option label="已完成" value="completed" />
                <el-option label="已取消" value="cancelled" />
              </el-select>
            </el-form-item>
            <el-form-item label="订单日期">
              <el-date-picker
                v-model="searchForm.orderDateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>
            <el-form-item label="客户">
              <el-input v-model="searchForm.customerName" placeholder="客户名称" clearable style="width: 200px;" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSearch">搜索</el-button>
              <el-button @click="handleReset">重置</el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-collapse-item>
    </el-collapse>

    <!-- 订单列表 -->
    <el-card shadow="never" class="orders-table-card">
      <el-table
        v-loading="loading"
        :data="ordersList"
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="orderNo" label="订单编号" min-width="150" sortable />
        <el-table-column prop="customerName" label="客户名称" min-width="180" />
        <el-table-column prop="totalAmount" label="订单总额" width="120" align="right" sortable>
          <template #default="scope">
            {{ formatCurrency(scope.row.totalAmount) }}
          </template>
        </el-table-column>
        <el-table-column prop="discountAmount" label="折扣金额" width="120" align="right">
          <template #default="scope">
            {{ formatCurrency(scope.row.discountAmount) }}
          </template>
        </el-table-column>
        <el-table-column prop="finalAmount" label="最终金额" width="120" align="right" sortable>
          <template #default="scope">
            {{ formatCurrency(scope.row.finalAmount) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120">
          <template #default="scope">
            <el-tag :type="getStatusTagType(scope.row.status)">
              {{ getStatusLabel(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="orderDate" label="订单日期" width="140" sortable />
        <el-table-column prop="deliveryDate" label="交付日期" width="140" sortable />
        <el-table-column prop="salesPersonId" label="销售人员" width="120" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="scope">
            <el-button 
              size="small" 
              type="success" 
              v-if="scope.row.status === 'draft'"
              @click="handleSubmit(scope.row)"
            >提交</el-button>
            <!-- 审批中订单由OA统一审批流驱动，审批人在OA待办中处理，此处不再提供本地审批按钮 -->
            <el-tag v-if="scope.row.status === 'submitted'" size="small" type="info">OA审批中</el-tag>
            <el-button size="small" @click="handleView(scope.row)">查看</el-button>
            <el-button 
              size="small" 
              type="primary" 
              v-if="scope.row.status === 'draft'"
              @click="handleEdit(scope.row)"
            >编辑</el-button>
            <el-button 
              size="small" 
              type="danger" 
              v-if="scope.row.status === 'draft' || scope.row.status === 'cancelled'"
              @click="handleDelete(scope.row)"
            >删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.currentPage"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 订单表单对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="70%"
      @close="handleDialogClose"
    >
      <OrderDialog
        v-model="orderForm"
        :dialog-type="dialogType"
      />
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="handleDialogCancel">取消</el-button>
          <el-button type="primary" @click="handleDialogConfirm">确认</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { Search, Plus, Download } from '@element-plus/icons-vue'
import type { FormInstance } from 'element-plus'
import { ElMessage, ElMessageBox } from 'element-plus'
import { orderApi } from '../../../api/crm/order'
import OrderDialog from './components/OrderDialog.vue'

// 订单列表数据
const ordersList = ref<any[]>([])

// 加载状态
const loading = ref(false)

// 分页数据
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 搜索表单
const searchForm = reactive({
  keyword: '',
  status: '',
  orderDateRange: [] as string[],
  customerName: ''
})

// 搜索面板展开状态
const activeSearchPanel = ref<string[]>([])

// 对话框状态
const dialogVisible = ref(false)
const dialogType = ref<'create' | 'edit'>('create')
const dialogTitle = ref('')

// 表单初始数据
const initialOrderForm = {
  customerId: null,
  customerName: '',
  opportunityId: null,
  totalAmount: 0,
  discountAmount: 0,
  finalAmount: 0,
  currency: 'CNY',
  orderDate: new Date().toISOString().split('T')[0],
  deliveryDate: '',
  paymentTerms: '',
  deliveryAddress: '',
  salesPersonId: 1,
  status: 'draft',
  approvalStatus: '',
  remark: '',
  items: []
}

// 表单数据
const orderForm = ref<any>({...initialOrderForm})

// 格式化货币
/**
 * 格式化货币显示（null/undefined/NaN兜底为0，避免显示¥NaN）
 * @param amount 金额
 * @returns 格式化后的货币字符串
 */
const formatCurrency = (amount: number | null | undefined) => {
  const safeAmount = Number(amount)
  return new Intl.NumberFormat('zh-CN', { style: 'currency', currency: 'CNY' })
    .format(Number.isFinite(safeAmount) ? safeAmount : 0)
}

// 获取状态标签类型
const getStatusTagType = (status: string) => {
  const typeMap: Record<string, string> = {
    draft: 'warning',
    submitted: 'info',
    approved: 'success',
    rejected: 'danger',
    in_progress: 'primary',
    completed: 'success',
    cancelled: 'danger'
  }
  return typeMap[status] || ''
}

// 获取状态标签
const getStatusLabel = (status: string) => {
  const labelMap: Record<string, string> = {
    draft: '草稿',
    submitted: '已提交',
    approved: '已审批',
    rejected: '已拒绝',
    in_progress: '进行中',
    completed: '已完成',
    cancelled: '已取消'
  }
  return labelMap[status] || status
}

// 获取订单列表
const fetchOrdersList = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.currentPage,
      size: pagination.pageSize,
      keyword: searchForm.keyword,
      status: searchForm.status,
      startDate: searchForm.orderDateRange[0] || '',
      endDate: searchForm.orderDateRange[1] || '',
      customerName: searchForm.customerName
    }
    const response = await orderApi.getMyOrders(params)
    ordersList.value = response.data?.list || []
    pagination.total = response.data?.total || 0
  } catch (error) {
    console.error('获取订单列表失败:', error)
    ElMessage.error('获取订单列表失败')
    // 使用模拟数据作为 fallback
    ordersList.value = [
      {
        id: 1,
        orderNo: 'ORD20240001',
        customerId: 1,
        customerName: 'ABC公司',
        totalAmount: 100000,
        discountAmount: 5000,
        finalAmount: 95000,
        currency: 'CNY',
        orderDate: '2024-01-15',
        deliveryDate: '2024-02-15',
        paymentTerms: '30天',
        deliveryAddress: '北京市朝阳区',
        salesPersonId: 1,
        status: 'approved',
        approvalStatus: 'approved',
        remark: '无'
      }
    ]
    pagination.total = 1
  } finally {
    loading.value = false
  }
}

// 处理搜索
const handleSearch = () => {
  // 重置页码
  pagination.currentPage = 1
  fetchOrdersList()
}

// 处理重置
const handleReset = () => {
  Object.assign(searchForm, {
    keyword: '',
    status: '',
    orderDateRange: [],
    customerName: ''
  })
  handleSearch()
}

// 处理创建
const handleCreate = () => {
  dialogType.value = 'create'
  dialogTitle.value = '新建销售订单'
  orderForm.value = {...initialOrderForm}
  dialogVisible.value = true
}

// 处理编辑
const handleEdit = (row: any) => {
  dialogType.value = 'edit'
  dialogTitle.value = '编辑销售订单'
  orderForm.value = {...row}
  dialogVisible.value = true
}

// 处理查看
const handleView = async (row: any) => {
  try {
    const response = await orderApi.getOrderDetail(row.id)
    const orderDetail = response.data
    console.log('订单详情:', orderDetail)
    // 这里可以跳转到订单详情页面或弹出详情对话框
    ElMessage.success('获取订单详情成功')
  } catch (error) {
    console.error('获取订单详情失败:', error)
    ElMessage.error('获取订单详情失败')
  }
}

// 处理删除
const handleDelete = async (row: any) => {
  ElMessageBox.confirm('确定要删除该订单吗？', '删除确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      // 这里需要调用实际的删除API，假设API路径是 /api/crm/orders/{id}
      await orderApi.deleteOrder(row.id)
      ElMessage.success('删除成功')
      fetchOrdersList()
    } catch (error) {
      console.error('删除订单失败:', error)
      ElMessage.error('删除订单失败')
    }
  }).catch(() => {
    // 取消删除
  })
}

// 处理提交审批
const handleSubmit = async (row: any) => {
  ElMessageBox.confirm('确定要提交该订单进行审批吗？提交后将进入OA统一审批流。', '提交确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'info'
  }).then(async () => {
    try {
      await orderApi.submitOrderApproval(row.id)
      ElMessage.success('提交成功，请在OA待办中跟进审批')
      fetchOrdersList()
    } catch (error) {
      console.error('提交审批失败:', error)
      ElMessage.error('提交审批失败')
    }
  }).catch(() => {})
}

// 处理导出
const handleExport = () => {
  // 这里可以实现导出功能
  ElMessage.info('导出功能开发中')
}

// 处理对话框确认
const handleDialogConfirm = async () => {
  try {
    if (dialogType.value === 'create') {
      await orderApi.createOrder(orderForm.value)
      ElMessage.success('创建成功')
    } else {
      await orderApi.updateOrder(orderForm.value.id, orderForm.value)
      ElMessage.success('编辑成功')
    }
    dialogVisible.value = false
    // 刷新列表
    fetchOrdersList()
  } catch (error) {
    console.error('保存订单失败:', error)
    ElMessage.error(dialogType.value === 'create' ? '创建失败' : '编辑失败')
  }
}

// 处理对话框取消
const handleDialogCancel = () => {
  dialogVisible.value = false
}

// 处理对话框关闭
const handleDialogClose = () => {
  // 重置表单
  orderForm.value = {...initialOrderForm}
}

// 处理分页大小变化
const handleSizeChange = (size: number) => {
  pagination.pageSize = size
  fetchOrdersList()
}

// 处理当前页码变化
const handleCurrentChange = (current: number) => {
  pagination.currentPage = current
  fetchOrdersList()
}

// 处理表格选择变化
const handleSelectionChange = (selection: any[]) => {
  console.log('选中的行:', selection)
}

// 组件挂载时初始化数据
onMounted(() => {
  fetchOrdersList()
})
</script>

<style scoped>
.sales-orders-view {
  padding: 20px;
  background-color: #ffffff;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.page-actions {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.advanced-search {
  background-color: #f5f7fa;
  padding: 20px;
  border-radius: 4px;
}

.orders-table-card {
  margin-bottom: 20px;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>