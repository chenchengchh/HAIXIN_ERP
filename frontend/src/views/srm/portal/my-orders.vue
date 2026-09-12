<template>
  <div class="my-orders">
    <h3>我的订单</h3>
    
    <!-- 查询条件 -->
    <el-card class="search-card" shadow="hover">
      <el-form :model="searchForm" inline>
        <el-form-item label="订单编号">
          <el-input v-model="searchForm.orderNo" placeholder="请输入订单编号" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
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
    
    <!-- 订单列表 -->
    <el-card class="orders-card" shadow="hover">
      <el-table
        :data="orderList"
        stripe
        style="width: 100%"
        v-loading="loading"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="orderNo" label="订单编号" min-width="180" />
        <el-table-column prop="status" label="状态" width="120">
          <template #default="scope">
            <el-tag :type="getStatusTagType(scope.row.status)">{{ scope.row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="totalAmount" label="订单金额" width="150">
          <template #default="scope">
            ¥{{ formatMoney(scope.row.totalAmount) }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column prop="expectedDeliveryDate" label="期望交货日期" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="viewOrderDetail(scope.row.id)">查看详情</el-button>
            <el-button 
              type="primary" 
              size="small" 
              @click="handleConfirmOrder(scope.row)"
              :disabled="scope.row.status !== 'OPEN'"
            >
              确认订单
            </el-button>
            <el-button 
              type="success" 
              size="small" 
              @click="handleDeliverOrder(scope.row)"
              :disabled="scope.row.status !== 'CONFIRMED' && scope.row.status !== 'PARTIAL_DELIVERED'"
            >
              发货
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
import { procurementApi } from '../../../api/srm'
import { DataTransformer } from '../../../utils/data-transformer'

// 路由实例
const router = useRouter()

// 搜索表单
const searchForm = reactive({
  orderNo: '',
  status: ''
})

// 订单列表
const orderList = ref<any[]>([])

// 分页数据
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 加载状态
const loading = ref(false)

const formatMoney = (value: unknown) => DataTransformer.formatMoney(value)

/**
 * 根据订单状态获取标签类型
 */
const getStatusTagType = (status: string) => {
  switch (status) {
    case 'DRAFT':
      return 'info'
    case 'OPEN':
      return 'warning'
    case 'CONFIRMED':
      return 'success'
    case 'PARTIAL_DELIVERED':
      return 'primary'
    case 'DELIVERED':
      return 'success'
    case 'RECEIVED':
      return 'success'
    case 'CLOSED':
      return 'warning'
    default:
      return ''
  }
}

/**
 * 查看订单详情
 */
const viewOrderDetail = (orderId: number) => {
  console.log('查看订单详情:', orderId)
  ElMessage.info('订单详情功能开发中')
}

/**
 * 确认订单
 */
const handleConfirmOrder = async (order: any) => {
  try {
    await ElMessageBox.confirm('确定要确认该订单吗？', '确认订单', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await procurementApi.confirmPurchaseOrder(order.id)
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
 * 发货
 */
const handleDeliverOrder = (order: any) => {
  console.log('发货:', order.id)
  ElMessage.info('发货功能开发中')
}

/**
 * 查询订单
 */
const handleSearch = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.currentPage - 1,
      size: pagination.pageSize
    }
    const res = await procurementApi.getPurchaseOrderList(params)
    orderList.value = res.data.list || []
    pagination.total = res.data.total || 0
  } catch (error) {
    console.error('查询订单失败:', error)
    ElMessage.error('查询订单失败')
    orderList.value = []
    pagination.total = 0
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
    status: ''
  })
  handleSearch()
}

/**
 * 分页大小变化
 */
const handleSizeChange = (size: number) => {
  pagination.pageSize = size
  pagination.currentPage = 1
  handleSearch()
}

/**
 * 当前页码变化
 */
const handleCurrentChange = (current: number) => {
  pagination.currentPage = current
  handleSearch()
}

// 初始化数据
onMounted(() => {
  handleSearch()
})
</script>

<style scoped>
.my-orders {
  padding: 20px;
}

.search-card {
  margin-bottom: 20px;
}

.orders-card {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
