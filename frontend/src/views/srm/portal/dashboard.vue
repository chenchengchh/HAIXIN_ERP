<template>
  <div class="supplier-dashboard">
    <h3>供应商仪表盘</h3>
    
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stat-cards">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="card-content">
            <div class="card-title">待处理询价单</div>
            <div class="card-value">{{ pendingInquiries }}</div>
            <div class="card-desc">需要您报价的询价单</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="card-content">待确认订单</div>
          <div class="card-value">{{ pendingOrders }}</div>
          <div class="card-desc">需要您确认的采购订单</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="card-title">待发货订单</div>
          <div class="card-value">{{ pendingDelivery }}</div>
          <div class="card-desc">已确认需要发货的订单</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="card-content">本月成交量</div>
          <div class="card-value">¥{{ monthlySales.toFixed(2) }}</div>
          <div class="card-desc">本月累计成交金额</div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 近期订单列表 -->
    <el-card class="recent-orders-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>近期订单</span>
          <el-button type="primary" size="small" @click="goToMyOrders">查看全部</el-button>
        </div>
      </template>
      <el-table :data="recentOrders" style="width: 100%">
        <el-table-column prop="orderNo" label="订单编号" min-width="180" />
        <el-table-column prop="status" label="状态" width="120">
          <template #default="scope">
            <el-tag :type="getStatusTagType(scope.row.status)">{{ scope.row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="totalAmount" label="订单金额" width="150" formatter="(row) => `¥${row.totalAmount.toFixed(2)}`" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column prop="expectedDeliveryDate" label="期望交货日期" width="180" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="viewOrderDetail(scope.row.id)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    
    <!-- 询价单列表 -->
    <el-card class="recent-inquiries-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>近期询价单</span>
          <el-button type="primary" size="small" @click="goToInquiries">查看全部</el-button>
        </div>
      </template>
      <el-table :data="recentInquiries" style="width: 100%">
        <el-table-column prop="inquiryNo" label="询价单号" min-width="180" />
        <el-table-column prop="title" label="询价标题" min-width="200" />
        <el-table-column prop="status" label="状态" width="120">
          <template #default="scope">
            <el-tag :type="getInquiryStatusTagType(scope.row.status)">{{ scope.row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="endTime" label="截止时间" width="180" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="viewInquiryDetail(scope.row.id)">
              {{ scope.row.status === 'PUBLISHED' ? '报价' : '查看' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
// 这里假设我们有供应商门户相关的API和类型定义

// 路由实例
const router = useRouter()

// 统计数据
const pendingInquiries = ref(5)
const pendingOrders = ref(3)
const pendingDelivery = ref(2)
const monthlySales = ref(150000.50)

// 近期订单
const recentOrders = ref([
  {
    id: 1,
    orderNo: 'PO20250101001',
    status: 'CONFIRMED',
    totalAmount: 50000,
    createTime: '2025-01-01 10:30:00',
    expectedDeliveryDate: '2025-01-15 18:00:00'
  },
  {
    id: 2,
    orderNo: 'PO20250102002',
    status: 'OPEN',
    totalAmount: 30000,
    createTime: '2025-01-02 14:20:00',
    expectedDeliveryDate: '2025-01-20 18:00:00'
  },
  {
    id: 3,
    orderNo: 'PO20250103003',
    status: 'DELIVERED',
    totalAmount: 20000,
    createTime: '2025-01-03 09:15:00',
    expectedDeliveryDate: '2025-01-18 18:00:00'
  }
])

// 近期询价单
const recentInquiries = ref([
  {
    id: 1,
    inquiryNo: 'RFQ20250101001',
    title: '钢材询价',
    status: 'PUBLISHED',
    endTime: '2025-01-10 18:00:00'
  },
  {
    id: 2,
    inquiryNo: 'RFQ20250102002',
    title: '塑料颗粒询价',
    status: 'PUBLISHED',
    endTime: '2025-01-12 18:00:00'
  },
  {
    id: 3,
    inquiryNo: 'RFQ20241228003',
    title: '电子元件询价',
    status: 'CLOSED',
    endTime: '2025-01-05 18:00:00'
  }
])

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
 * 根据询价单状态获取标签类型
 */
const getInquiryStatusTagType = (status: string) => {
  switch (status) {
    case 'DRAFT':
      return 'info'
    case 'PUBLISHED':
      return 'warning'
    case 'CLOSED':
      return 'warning'
    case 'AWARDED':
      return 'success'
    default:
      return ''
  }
}

/**
 * 查看订单详情
 */
const viewOrderDetail = (orderId: number) => {
  router.push(`/home/srm/portal/my-orders/${orderId}`)
}

/**
 * 查看询价单详情
 */
const viewInquiryDetail = (inquiryId: number) => {
  console.log('查看询价单详情:', inquiryId)
  ElMessage.info('询价单详情功能开发中')
}

/**
 * 跳转到我的订单
 */
const goToMyOrders = () => {
  router.push('/home/srm/portal/my-orders')
}

/**
 * 跳转到询价单列表
 */
const goToInquiries = () => {
  console.log('跳转到询价单列表')
  ElMessage.info('询价单列表功能开发中')
}

// 初始化数据
onMounted(() => {
  // 这里应该调用API获取真实数据
  console.log('初始化供应商仪表盘数据')
})
</script>

<style scoped>
.supplier-dashboard {
  padding: 20px;
}

.stat-cards {
  margin-bottom: 20px;
}

.stat-card {
  height: 100%;
}

.card-content {
  text-align: center;
}

.card-title {
  font-size: 16px;
  color: #666;
  margin-bottom: 10px;
}

.card-value {
  font-size: 28px;
  font-weight: bold;
  color: #333;
  margin-bottom: 5px;
}

.card-desc {
  font-size: 12px;
  color: #999;
}

.recent-orders-card,
.recent-inquiries-card {
  margin-bottom: 20px;
}

.card-header {
  font-weight: bold;
  font-size: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>