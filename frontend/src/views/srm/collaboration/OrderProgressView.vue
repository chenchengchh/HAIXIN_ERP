<template>
  <div class="order-progress-view">
    <div class="content-header">
      <h3>订单进度共享</h3>
      <div class="action-buttons">
        <el-button type="primary" @click="handleRefresh">刷新数据</el-button>
      </div>
    </div>
    
    <!-- 搜索区域 -->
    <div class="search-area">
      <el-card shadow="never">
        <el-form :model="searchForm" layout="inline" size="small">
          <el-form-item label="订单编号">
            <el-input v-model="searchForm.orderNo" placeholder="请输入订单编号" />
          </el-form-item>
          <el-form-item label="供应商名称">
            <el-input v-model="searchForm.supplierName" placeholder="请输入供应商名称" />
          </el-form-item>
          <el-form-item label="订单状态">
            <el-select v-model="searchForm.status" placeholder="请选择订单状态">
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
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
    
    <!-- 订单进度列表 -->
    <div class="table-area">
      <el-card shadow="never">
        <el-table v-loading="loading" :data="orderProgressList" style="width: 100%">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="orderNo" label="订单编号" width="180" />
          <el-table-column prop="supplierName" label="供应商名称" width="220" />
          <el-table-column prop="status" label="订单状态" width="120">
            <template #default="scope">
              <el-tag :type="getStatusType(scope.row.status)">
                {{ scope.row.status }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="totalAmount" label="订单金额" width="120">
            <template #default="scope">
              ¥{{ formatMoney(scope.row.totalAmount) }}
            </template>
          </el-table-column>
          <el-table-column prop="purchaseDate" label="下单日期" width="180" />
          <el-table-column prop="expectedDeliveryDate" label="期望交货日期" width="180" />
          <el-table-column prop="actualDeliveryDate" label="实际交货日期" width="180" />
          <el-table-column label="操作" width="240" fixed="right">
            <template #default="scope">
              <el-button size="small" @click="handleView(scope.row)">查看详情</el-button>
              <el-button size="small" type="primary" @click="handleTrack(scope.row)">跟踪进度</el-button>
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
    
    <!-- 订单进度详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      :title="`订单进度详情 - ${currentOrder?.orderNo}`"
      width="80%"
      :close-on-click-modal="false"
    >
      <div class="order-detail">
        <h4>基本信息</h4>
        <el-descriptions :column="3" border>
          <el-descriptions-item label="订单编号">{{ currentOrder?.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="供应商名称">{{ currentOrder?.supplierName }}</el-descriptions-item>
          <el-descriptions-item label="订单状态">{{ currentOrder?.status }}</el-descriptions-item>
          <el-descriptions-item label="订单金额">¥{{ formatMoney(currentOrder?.totalAmount) }}</el-descriptions-item>
          <el-descriptions-item label="下单日期">{{ currentOrder?.purchaseDate }}</el-descriptions-item>
          <el-descriptions-item label="期望交货日期">{{ currentOrder?.expectedDeliveryDate }}</el-descriptions-item>
          <el-descriptions-item label="实际交货日期">{{ currentOrder?.actualDeliveryDate || '未交货' }}</el-descriptions-item>
          <el-descriptions-item label="供应商联系人">{{ currentOrder?.contactPerson }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ currentOrder?.contactPhone }}</el-descriptions-item>
        </el-descriptions>
        
        <h4 style="margin-top: 20px;">订单进度跟踪</h4>
        <el-timeline>
          <el-timeline-item
            v-for="(item, index) in currentOrder?.progress" 
            :key="index"
            :timestamp="item.time"
            :placement="'top'"
          >
            <el-card shadow="hover">
              <h5>{{ item.title }}</h5>
              <p>{{ item.description }}</p>
              <div class="progress-info">
                <span class="operator">操作人：{{ item.operator }}</span>
                <span class="time">{{ item.time }}</span>
              </div>
            </el-card>
          </el-timeline-item>
        </el-timeline>
        
        <h4 style="margin-top: 20px;">订单商品明细</h4>
        <el-table :data="currentOrder?.items" style="width: 100%">
          <el-table-column prop="materialName" label="商品名称" width="200" />
          <el-table-column prop="specification" label="规格型号" width="150" />
          <el-table-column prop="quantity" label="数量" width="100" />
          <el-table-column prop="unit" label="单位" width="80" />
          <el-table-column prop="price" label="单价" width="100" />
          <el-table-column prop="total" label="金额" width="120" />
        </el-table>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="detailDialogVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { procurementApi } from '../../../api/srm'
import { DataTransformer } from '../../../utils/data-transformer'

// 搜索表单
const searchForm = reactive({
  orderNo: '',
  supplierName: '',
  status: ''
})

// 分页
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 加载状态
const loading = ref(false)

const formatMoney = (value: unknown) => DataTransformer.formatMoney(value)

// 订单进度列表
const orderProgressList = ref<any[]>([])

// 详情对话框
const detailDialogVisible = ref(false)
const currentOrder = ref<any>(null)

// 页面加载时获取数据
onMounted(() => {
  fetchOrderProgress()
})

// 获取订单进度列表
const fetchOrderProgress = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.currentPage - 1,
      size: pagination.pageSize,
      orderNo: searchForm.orderNo,
      supplierName: searchForm.supplierName,
      status: searchForm.status
    }
    const res = await procurementApi.getPurchaseOrderList(params)
    orderProgressList.value = res.data.list
    pagination.total = res.data.total
  } catch (error) {
    console.error(error)
    ElMessage.error('获取订单进度列表失败')
  } finally {
    loading.value = false
  }
}

// 根据状态获取标签类型
const getStatusType = (status: string) => {
  switch (status) {
    case 'DRAFT':
      return 'info'
    case 'OPEN':
      return 'warning'
    case 'CONFIRMED':
      return 'primary'
    case 'PARTIAL_DELIVERED':
      return 'success'
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

// 搜索
const handleSearch = () => {
  pagination.currentPage = 1
  fetchOrderProgress()
}

// 重置
const handleReset = () => {
  Object.assign(searchForm, {
    orderNo: '',
    supplierName: '',
    status: ''
  })
  pagination.currentPage = 1
  fetchOrderProgress()
}

// 刷新数据
const handleRefresh = () => {
  fetchOrderProgress()
  ElMessage.success('数据已刷新')
}

// 查看详情
const handleView = async (row: any) => {
  try {
    const res = await procurementApi.getPurchaseOrderDetail(row.id)
    currentOrder.value = res.data
    // 模拟进度跟踪数据，实际应有独立API
    if (!currentOrder.value.progress) {
      currentOrder.value.progress = [
        { time: currentOrder.value.createTime, title: '订单创建', description: '采购订单已创建', operator: '系统' },
        { time: currentOrder.value.updateTime, title: '最近更新', description: '订单状态更新', operator: '系统' }
      ]
    }
    detailDialogVisible.value = true
  } catch (error) {
    ElMessage.error('获取详情失败')
  }
}

// 跟踪进度
const handleTrack = (row: any) => {
  handleView(row)
}

// 分页大小变化
const handleSizeChange = (size: number) => {
  pagination.pageSize = size
  fetchOrderProgress()
}

// 当前页码变化
const handleCurrentChange = (current: number) => {
  pagination.currentPage = current
  fetchOrderProgress()
}
</script>

<style scoped>
.order-progress-view {
  padding: 16px;
}

.content-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.content-header h3 {
  margin: 0;
  color: #303133;
  font-size: 18px;
}

.action-buttons {
  display: flex;
  gap: 8px;
}

.search-area {
  margin-bottom: 16px;
}

.table-area {
  margin-bottom: 16px;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.dialog-footer {
  text-align: right;
}

.order-detail h4 {
  margin: 0 0 16px 0;
  color: #303133;
  font-size: 16px;
}

.progress-info {
  display: flex;
  justify-content: space-between;
  margin-top: 10px;
  font-size: 12px;
  color: #909399;
}
</style>
