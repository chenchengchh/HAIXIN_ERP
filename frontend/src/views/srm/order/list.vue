<template>
  <div class="order-list">
    <h3>采购订单列表</h3>
    
    <!-- 查询条件 -->
    <el-card class="search-card" shadow="hover">
      <el-form :model="searchForm" inline>
        <el-form-item label="订单编号">
          <el-input v-model="searchForm.orderNo" placeholder="请输入订单编号" />
        </el-form-item>
        <el-form-item label="供应商名称">
          <el-input v-model="searchForm.supplierName" placeholder="请输入供应商名称" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态">
            <el-option label="全部" value="" />
            <el-option label="草稿" value="DRAFT" />
            <el-option label="待确认" value="OPEN" />
            <el-option label="已确认" value="CONFIRMED" />
            <el-option label="部分发货" value="PARTIAL_DELIVERED" />
            <el-option label="已发货" value="DELIVERED" />
            <el-option label="已入库" value="RECEIVED" />
            <el-option label="已关闭" value="CLOSED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 操作按钮 -->
    <div class="action-buttons">
      <el-button type="primary" @click="handleAdd">新增订单</el-button>
      <el-button @click="handleImport">导入</el-button>
      <el-button @click="handleExport">导出</el-button>
    </div>
    
    <!-- 订单列表 -->
    <el-card class="list-card" shadow="hover">
      <el-table :data="orderList" stripe style="width: 100%" v-loading="loading">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="orderNo" label="订单编号" min-width="150" />
        <el-table-column prop="supplierName" label="供应商名称" min-width="150" />
        <el-table-column prop="status" label="状态" width="120">
          <template #default="scope">
            <el-tag :type="getStatusTagType(scope.row.status)">{{ scope.row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="totalAmount" label="订单金额" width="120">
          <template #default="scope">
            {{ formatCurrency(scope.row.totalAmount) }}
          </template>
        </el-table-column>
        <el-table-column prop="purchaseDate" label="下单日期" width="150" />
        <el-table-column prop="expectedDeliveryDate" label="期望交货日期" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="handleDetail(scope.row)">查看详情</el-button>
            <el-button type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="success" size="small" @click="handleConfirm(scope.row)" :disabled="scope.row.status !== 'OPEN'">
              确认
            </el-button>
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useOrderStore } from '@/stores/srm/order'
import { PurchaseOrderStatus } from '@/types/srm'
import type { PurchaseOrder } from '@/types/srm'

// 路由实例
const router = useRouter()

// 订单管理状态存储
const orderStore = useOrderStore()

// 搜索表单
const searchForm = reactive({
  orderNo: '',
  supplierName: '',
  status: ''
})

// 订单列表数据
const orderList = ref<PurchaseOrder[]>([])

// 分页数据
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 加载状态
const loading = ref(false)

const formatCurrency = (value: unknown) => {
  const n = Number(value ?? 0)
  const safe = Number.isFinite(n) ? n : 0
  return `¥${safe.toFixed(2)}`
}

/**
 * 根据状态获取标签类型
 * @param status 订单状态
 * @returns 标签类型
 */
const getStatusTagType = (status: PurchaseOrderStatus) => {
  switch (status) {
    case PurchaseOrderStatus.DRAFT:
      return 'info'
    case PurchaseOrderStatus.OPEN:
      return 'warning'
    case PurchaseOrderStatus.CONFIRMED:
      return 'success'
    case PurchaseOrderStatus.PARTIAL_DELIVERED:
      return 'primary'
    case PurchaseOrderStatus.DELIVERED:
      return 'success'
    case PurchaseOrderStatus.RECEIVED:
      return 'success'
    case PurchaseOrderStatus.CLOSED:
      return 'warning'
    default:
      return ''
  }
}

/**
 * 查询订单列表
 */
const handleSearch = async () => {
  loading.value = true
  try {
    await orderStore.fetchPurchaseOrderList(searchForm)
    pagination.currentPage = orderStore.pagination.currentPage
    pagination.pageSize = orderStore.pagination.pageSize
    pagination.total = orderStore.pagination.total
    orderList.value = orderStore.purchaseOrderList
    ElMessage.success('查询成功')
  } catch (error) {
    console.error('获取订单列表失败:', error)
    ElMessage.error('获取订单列表失败')
  } finally {
    loading.value = false
  }
}

/**
 * 重置搜索条件
 */
const handleReset = () => {
  Object.assign(searchForm, {
    orderNo: '',
    supplierName: '',
    status: ''
  })
  handleSearch()
}

/**
 * 分页大小变化处理
 * @param size 每页条数
 */
const handleSizeChange = (size: number) => {
  orderStore.setPagination(1, size)
  handleSearch()
}

/**
 * 当前页码变化处理
 * @param current 当前页码
 */
const handleCurrentChange = (current: number) => {
  orderStore.setPagination(current, orderStore.pagination.pageSize)
  handleSearch()
}

/**
 * 新增订单
 */
const handleAdd = () => {
  router.push('/home/srm/order/detail/new')
}

/**
 * 编辑订单
 * @param row 订单数据
 */
const handleEdit = (row: PurchaseOrder) => {
  router.push(`/home/srm/order/detail/${row.id}`)
}

/**
 * 查看订单详情
 * @param row 订单数据
 */
const handleDetail = (row: PurchaseOrder) => {
  router.push(`/home/srm/order/detail/${row.id}`)
}

/**
 * 确认订单
 * @param row 订单数据
 */
const handleConfirm = async (row: PurchaseOrder) => {
  try {
    await ElMessageBox.confirm('确定要确认该订单吗？', '确认订单', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    if (row.id) {
      await orderStore.confirmPurchaseOrder(row.id)
    }
    ElMessage.success('订单确认成功')
    handleSearch()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('确认订单失败:', error)
      ElMessage.error('确认订单失败')
    }
  }
}

/**
 * 导入订单
 */
const handleImport = () => {
  console.log('导入订单')
  ElMessage.info('导入功能开发中')
}

/**
 * 导出订单
 */
const handleExport = () => {
  console.log('导出订单')
  ElMessage.info('导出功能开发中')
}

// 组件挂载时获取数据
onMounted(() => {
  handleSearch()
})
</script>

<style scoped>
.order-list {
  padding: 20px;
}

.search-card {
  margin-bottom: 20px;
}

.action-buttons {
  margin-bottom: 20px;
  display: flex;
  gap: 10px;
}

.list-card {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
