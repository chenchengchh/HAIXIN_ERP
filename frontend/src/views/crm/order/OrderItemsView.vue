<template>
  <div class="order-items-view">
    <!-- 页面操作栏 -->
    <div class="page-actions">
      <el-input
        v-model="searchForm.keyword"
        placeholder="搜索产品名称、编码"
        style="width: 240px; margin-right: 10px;"
        clearable
        @keyup.enter="handleSearch"
      >
        <template #append>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-input-number
        v-model="searchForm.orderId"
        placeholder="订单ID"
        :min="1"
        style="width: 140px; margin-right: 10px;"
      />
      <el-button type="primary" @click="handleSearch">
        <el-icon><Search /></el-icon> 搜索
      </el-button>
    </div>

    <!-- 订单明细列表 -->
    <el-card shadow="never" class="order-items-table-card">
      <el-table
        v-loading="loading"
        :data="orderItemsList"
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" sortable />
        <el-table-column prop="orderId" label="订单ID" width="90" sortable />
        <el-table-column prop="orderNo" label="订单编号" width="170" show-overflow-tooltip />
        <el-table-column prop="productCode" label="产品编码" width="120" sortable />
        <el-table-column prop="productName" label="产品名称" min-width="160" show-overflow-tooltip />
        <el-table-column prop="quantity" label="数量" width="100" align="right" sortable />
        <el-table-column prop="unit" label="单位" width="70" align="center" />
        <el-table-column prop="unitPrice" label="单价" width="120" align="right" sortable>
          <template #default="scope">
            {{ formatCurrency(scope.row.unitPrice) }}
          </template>
        </el-table-column>
        <el-table-column prop="discountRate" label="折扣率" width="100" align="right">
          <template #default="scope">
            {{ scope.row.discountRate != null ? scope.row.discountRate + '%' : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="subtotal" label="小计" width="130" align="right" sortable>
          <template #default="scope">
            {{ formatCurrency(scope.row.subtotal) }}
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="120" show-overflow-tooltip />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="handleView(scope.row)">查看</el-button>
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

    <!-- 明细详情对话框 -->
    <el-dialog v-model="viewDialogVisible" title="订单明细详情" width="520px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="明细ID">{{ currentItem.id }}</el-descriptions-item>
        <el-descriptions-item label="订单ID">{{ currentItem.orderId }}</el-descriptions-item>
        <el-descriptions-item label="订单编号" :span="2">{{ currentItem.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="产品编码">{{ currentItem.productCode }}</el-descriptions-item>
        <el-descriptions-item label="产品名称">{{ currentItem.productName }}</el-descriptions-item>
        <el-descriptions-item label="数量">{{ currentItem.quantity }} {{ currentItem.unit }}</el-descriptions-item>
        <el-descriptions-item label="单价">{{ formatCurrency(currentItem.unitPrice) }}</el-descriptions-item>
        <el-descriptions-item label="折扣率">{{ currentItem.discountRate != null ? currentItem.discountRate + '%' : '-' }}</el-descriptions-item>
        <el-descriptions-item label="小计">{{ formatCurrency(currentItem.subtotal) }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ currentItem.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="viewDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { orderApi } from '@/api/crm/order'
import { unwrapPageResponse } from '@/api'

// 订单明细列表数据
const orderItemsList = ref<any[]>([])

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
  orderId: undefined as number | undefined
})

// 详情对话框状态
const viewDialogVisible = ref(false)
const currentItem = ref<any>({})

/**
 * 格式化货币金额
 * @param amount 金额数值
 * @returns 格式化后的人民币金额字符串
 */
const formatCurrency = (amount: number) => {
  if (amount == null) return '-'
  return new Intl.NumberFormat('zh-CN', { style: 'currency', currency: 'CNY' }).format(amount)
}

/**
 * 分页查询订单明细列表
 */
const fetchOrderItemsList = async () => {
  loading.value = true
  try {
    const params: any = {
      page: pagination.currentPage,
      size: pagination.pageSize
    }
    if (searchForm.keyword) params.keyword = searchForm.keyword
    if (searchForm.orderId) params.orderId = searchForm.orderId
    const response = await orderApi.getOrderItems(params)
    const { list, total } = unwrapPageResponse<any>(response)
    orderItemsList.value = list
    pagination.total = total
  } catch (error) {
    console.error('获取订单明细失败:', error)
    ElMessage.error('获取订单明细失败')
  } finally {
    loading.value = false
  }
}

/**
 * 搜索：重置页码并重新查询
 */
const handleSearch = () => {
  pagination.currentPage = 1
  fetchOrderItemsList()
}

/**
 * 查看明细详情
 * @param row 明细行数据
 */
const handleView = (row: any) => {
  currentItem.value = row
  viewDialogVisible.value = true
}

// 处理分页大小变化
const handleSizeChange = (size: number) => {
  pagination.pageSize = size
  pagination.currentPage = 1
  fetchOrderItemsList()
}

// 处理当前页码变化
const handleCurrentChange = (current: number) => {
  pagination.currentPage = current
  fetchOrderItemsList()
}

// 处理表格选择变化
const handleSelectionChange = (selection: any[]) => {
  console.log('选中的行:', selection)
}

// 组件挂载时初始化数据
onMounted(() => {
  fetchOrderItemsList()
})
</script>

<style scoped>
.order-items-view {
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

.order-items-table-card {
  margin-bottom: 20px;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>
