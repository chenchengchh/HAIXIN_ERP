<template>
  <div class="supplies-management-view">
    <!-- 办公用品统计概览 -->
    <div class="stats-section">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-content">
              <div class="stat-label">总供应量</div>
              <div class="stat-value">{{ stats.totalSupplies }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-content">
              <div class="stat-label">低库存物品</div>
              <div class="stat-value">{{ stats.lowStockItems }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-content">
              <div class="stat-label">今日申领</div>
              <div class="stat-value">{{ stats.todayRequests }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-content">
              <div class="stat-label">待审批申领</div>
              <div class="stat-value">{{ stats.pendingRequests }}</div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 操作区 -->
    <div class="operation-section">
      <el-button type="primary" @click="showCreateRequestDialog = true">
        <el-icon><Plus /></el-icon> 申领办公用品
      </el-button>
      <el-input
        v-model="searchKeyword"
        placeholder="搜索办公用品名称"
        clearable
        style="width: 300px; margin-left: 10px;"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
    </div>

    <!-- 办公用品列表 -->
    <div class="supplies-list-section">
      <el-card shadow="hover">
        <template #header>
          <div class="card-header">
            <span>办公用品列表</span>
          </div>
        </template>
        <el-table :data="filteredSupplies" stripe style="width: 100%">
          <el-table-column prop="supplyCode" label="物品编码" width="120" />
          <el-table-column prop="supplyName" label="物品名称" min-width="200" />
          <el-table-column prop="category" label="分类" width="100" />
          <el-table-column prop="specification" label="规格" width="120" />
          <el-table-column prop="unit" label="单位" width="80" />
          <el-table-column prop="stock" label="库存" width="80">
            <template #default="scope">
              <span :class="{ 'low-stock': scope.row.stock <= scope.row.safetyStock }">
                {{ scope.row.stock }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="safetyStock" label="安全库存" width="100" />
          <el-table-column prop="storageLocation" label="存放位置" width="120" />
          <el-table-column label="操作" width="100" fixed="right">
            <template #default="scope">
              <el-button size="small" @click="requestSupply(scope.row)">申领</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>

    <!-- 我的申领记录 -->
    <div class="my-requests-section">
      <el-card shadow="hover" style="margin-top: 20px;">
        <template #header>
          <div class="card-header">
            <span>我的申领记录</span>
          </div>
        </template>
        <el-table :data="myRequests" stripe style="width: 100%">
          <el-table-column prop="requestNo" label="申领编号" width="120" />
          <el-table-column prop="requestDate" label="申领日期" width="150" />
          <el-table-column prop="totalItems" label="物品数量" width="100" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="scope">
              <el-tag :type="statusTypeMap[scope.row.status]">{{ scope.row.status }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="approverName" label="审批人" width="100" />
          <el-table-column label="操作" width="100" fixed="right">
            <template #default="scope">
              <el-button size="small" @click="showRequestDetail(scope.row)">详情</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>

    <!-- 申领表单对话框 -->
    <el-dialog
      v-model="showCreateRequestDialog"
      title="申领办公用品"
      width="600px"
    >
      <el-form :model="requestForm" label-width="100px">
        <el-form-item label="物品名称" required>
          <el-select v-model="requestForm.supplyId" placeholder="请选择办公用品">
            <el-option
              v-for="supply in supplies"
              :key="supply.id"
              :label="supply.supplyName"
              :value="supply.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="申领数量" required>
          <el-input v-model.number="requestForm.quantity" placeholder="请输入申领数量" />
        </el-form-item>
        <el-form-item label="用途" required>
          <el-input
            v-model="requestForm.purpose"
            type="textarea"
            placeholder="请输入申领用途"
            :rows="4"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showCreateRequestDialog = false">取消</el-button>
          <el-button type="primary" @click="submitRequest">提交</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 申领详情对话框 -->
    <el-dialog
      v-model="showRequestDetailDialog"
      title="申领详情"
      width="700px"
    >
      <div v-if="selectedRequest" class="request-detail">
        <div class="detail-info">
          <div class="info-item">
            <span class="info-label">申领编号：</span>
            <span class="info-value">{{ selectedRequest.requestNo }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">申领日期：</span>
            <span class="info-value">{{ selectedRequest.requestDate }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">状态：</span>
            <el-tag :type="statusTypeMap[selectedRequest.status]">{{ selectedRequest.status }}</el-tag>
          </div>
          <div class="info-item">
            <span class="info-label">审批人：</span>
            <span class="info-value">{{ selectedRequest.approverName }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">审批时间：</span>
            <span class="info-value">{{ selectedRequest.approveTime || '待审批' }}</span>
          </div>
        </div>
        <div class="detail-items">
          <h4>申领物品</h4>
          <el-table :data="selectedRequest.items" style="width: 100%">
            <el-table-column prop="supplyName" label="物品名称" width="200" />
            <el-table-column prop="quantity" label="数量" width="100" />
            <el-table-column prop="unit" label="单位" width="80" />
            <el-table-column prop="purpose" label="用途" min-width="200" />
          </el-table>
        </div>
        <div class="detail-purposes">
          <h4>申领用途</h4>
          <p>{{ selectedRequest.purpose }}</p>
        </div>
        <div class="detail-approval" v-if="selectedRequest.status !== 'pending'">
          <h4>审批意见</h4>
          <p>{{ selectedRequest.approvalComment || '无' }}</p>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { Plus, Search } from '@element-plus/icons-vue'

// 定义办公用品类型
interface OfficeSupply {
  id: number
  supplyCode: string
  supplyName: string
  category: string
  specification: string
  unit: string
  stock: number
  safetyStock: number
  storageLocation: string
}

// 定义申领记录类型
interface SupplyRequest {
  id: number
  requestNo: string
  requestDate: string
  requesterId: number
  requesterName: string
  totalItems: number
  purpose: string
  status: 'pending' | 'approved' | 'rejected' | 'completed'
  approverId: number | null
  approverName: string | null
  approveTime: string | null
  approvalComment: string | null
  items: Array<{
    id: number
    supplyId: number
    supplyName: string
    quantity: number
    unit: string
    purpose: string
  }>
}

// 模拟数据 - 办公用品列表
const supplies = ref<OfficeSupply[]>([
  {
    id: 1,
    supplyCode: 'SUPP001',
    supplyName: 'A4打印纸',
    category: '办公文具',
    specification: '70g',
    unit: '包',
    stock: 50,
    safetyStock: 20,
    storageLocation: '1号仓库'
  },
  {
    id: 2,
    supplyCode: 'SUPP002',
    supplyName: '黑色中性笔',
    category: '办公文具',
    specification: '0.5mm',
    unit: '支',
    stock: 120,
    safetyStock: 50,
    storageLocation: '1号仓库'
  },
  {
    id: 3,
    supplyCode: 'SUPP003',
    supplyName: '笔记本',
    category: '办公文具',
    specification: 'A5',
    unit: '本',
    stock: 30,
    safetyStock: 20,
    storageLocation: '1号仓库'
  },
  {
    id: 4,
    supplyCode: 'SUPP004',
    supplyName: '文件夹',
    category: '办公文具',
    specification: 'A4',
    unit: '个',
    stock: 15,
    safetyStock: 10,
    storageLocation: '2号仓库'
  },
  {
    id: 5,
    supplyCode: 'SUPP005',
    supplyName: '订书机',
    category: '办公设备',
    specification: '标准型',
    unit: '台',
    stock: 8,
    safetyStock: 5,
    storageLocation: '2号仓库'
  },
  {
    id: 6,
    supplyCode: 'SUPP006',
    supplyName: '回形针',
    category: '办公文具',
    specification: '标准型',
    unit: '盒',
    stock: 25,
    safetyStock: 15,
    storageLocation: '1号仓库'
  },
  {
    id: 7,
    supplyCode: 'SUPP007',
    supplyName: '透明胶带',
    category: '办公文具',
    specification: '18mm×30m',
    unit: '卷',
    stock: 10,
    safetyStock: 15,
    storageLocation: '1号仓库'
  },
  {
    id: 8,
    supplyCode: 'SUPP008',
    supplyName: '计算器',
    category: '办公设备',
    specification: '桌面型',
    unit: '台',
    stock: 6,
    safetyStock: 5,
    storageLocation: '2号仓库'
  }
])

// 模拟数据 - 我的申领记录
const myRequests = ref<SupplyRequest[]>([
  {
    id: 1,
    requestNo: 'REQ20251217001',
    requestDate: '2025-12-17',
    requesterId: 1,
    requesterName: '张三',
    totalItems: 2,
    purpose: '部门日常办公使用',
    status: 'approved',
    approverId: 2,
    approverName: '李四',
    approveTime: '2025-12-17 10:30',
    approvalComment: '同意',
    items: [
      {
        id: 1,
        supplyId: 1,
        supplyName: 'A4打印纸',
        quantity: 5,
        unit: '包',
        purpose: '部门日常办公使用'
      },
      {
        id: 2,
        supplyId: 2,
        supplyName: '黑色中性笔',
        quantity: 20,
        unit: '支',
        purpose: '部门日常办公使用'
      }
    ]
  },
  {
    id: 2,
    requestNo: 'REQ20251216001',
    requestDate: '2025-12-16',
    requesterId: 1,
    requesterName: '张三',
    totalItems: 1,
    purpose: '新项目启动需要',
    status: 'completed',
    approverId: 2,
    approverName: '李四',
    approveTime: '2025-12-16 14:20',
    approvalComment: '已发放',
    items: [
      {
        id: 3,
        supplyId: 3,
        supplyName: '笔记本',
        quantity: 10,
        unit: '本',
        purpose: '新项目启动需要'
      }
    ]
  },
  {
    id: 3,
    requestNo: 'REQ20251217002',
    requestDate: '2025-12-17',
    requesterId: 1,
    requesterName: '张三',
    totalItems: 1,
    purpose: '财务部门使用',
    status: 'pending',
    approverId: null,
    approverName: null,
    approveTime: null,
    approvalComment: null,
    items: [
      {
        id: 4,
        supplyId: 8,
        supplyName: '计算器',
        quantity: 2,
        unit: '台',
        purpose: '财务部门使用'
      }
    ]
  }
])

// 搜索关键词
const searchKeyword = ref('')

// 状态类型映射
const statusTypeMap: Record<string, string> = {
  pending: 'info',
  approved: 'success',
  rejected: 'danger',
  completed: 'success'
}

// 计算筛选后的办公用品列表
const filteredSupplies = computed(() => {
  if (!searchKeyword.value) {
    return supplies.value
  }
  const keyword = searchKeyword.value.toLowerCase()
  return supplies.value.filter(supply => 
    supply.supplyName.toLowerCase().includes(keyword)
  )
})

// 统计数据
const stats = ref({
  totalSupplies: supplies.value.length,
  lowStockItems: supplies.value.filter(s => s.stock <= s.safetyStock).length,
  todayRequests: myRequests.value.filter(r => r.requestDate === new Date().toISOString().split('T')[0]).length,
  pendingRequests: myRequests.value.filter(r => r.status === 'pending').length
})

// 申领表单
const showCreateRequestDialog = ref(false)
const requestForm = ref({
  supplyId: 0,
  quantity: 1,
  purpose: ''
})

// 申领详情
const showRequestDetailDialog = ref(false)
const selectedRequest = ref<SupplyRequest | null>(null)

// 发起申领
const requestSupply = (supply: OfficeSupply) => {
  requestForm.value.supplyId = supply.id
  requestForm.value.quantity = 1
  requestForm.value.purpose = ''
  showCreateRequestDialog.value = true
}

// 提交申领
const submitRequest = () => {
  // 这里可以添加表单验证和提交逻辑
  console.log('提交申领:', requestForm.value)
  showCreateRequestDialog.value = false
}

// 显示申领详情
const showRequestDetail = (request: SupplyRequest) => {
  selectedRequest.value = request
  showRequestDetailDialog.value = true
}
</script>

<style scoped>
.supplies-management-view {
  padding: 20px;
  height: 100%;
  box-sizing: border-box;
  background-color: #ffffff;
}

.stats-section {
  margin-bottom: 20px;
}

.stat-card {
  height: 100px;
}

.stat-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
}

.stat-label {
  font-size: 14px;
  color: #606266;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
}

.operation-section {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.low-stock {
  color: #f56c6c;
  font-weight: bold;
}

.my-requests-section {
  margin-top: 20px;
}

.detail-info {
  margin-bottom: 20px;
}

.info-item {
  margin-bottom: 10px;
  display: flex;
  align-items: center;
}

.info-label {
  width: 100px;
  font-weight: bold;
}

.detail-items {
  margin-bottom: 20px;
}

.detail-items h4,
.detail-purposes h4,
.detail-approval h4 {
  margin: 0 0 10px 0;
}

.detail-purposes {
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.detail-approval {
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
}
</style>
