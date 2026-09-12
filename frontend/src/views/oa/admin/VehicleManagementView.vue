<template>
  <div class="vehicle-management-view">
    <!-- 车辆调度统计概览 -->
    <div class="stats-section">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-content">
              <div class="stat-label">车辆总数</div>
              <div class="stat-value">{{ stats.totalVehicles }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-content">
              <div class="stat-label">可用车辆</div>
              <div class="stat-value">{{ stats.availableVehicles }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-content">
              <div class="stat-label">今日申请</div>
              <div class="stat-value">{{ stats.todayRequests }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-content">
              <div class="stat-label">待审批申请</div>
              <div class="stat-value">{{ stats.pendingRequests }}</div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 操作区 -->
    <div class="operation-section">
      <el-button type="primary" @click="showCreateRequestDialog = true">
        <el-icon><Plus /></el-icon> 申请用车
      </el-button>
      <el-select v-model="filterStatus" placeholder="状态" clearable style="width: 120px; margin-left: 10px;">
        <el-option label="全部" value="" />
        <el-option label="待审批" value="pending" />
        <el-option label="已批准" value="approved" />
        <el-option label="已拒绝" value="rejected" />
        <el-option label="进行中" value="in_progress" />
        <el-option label="已完成" value="completed" />
      </el-select>
      <el-date-picker
        v-model="filterDate"
        type="date"
        placeholder="选择日期"
        clearable
        style="width: 200px; margin-left: 10px;"
      />
      <el-input
        v-model="searchKeyword"
        placeholder="搜索车辆牌号或车型"
        clearable
        style="width: 300px; margin-left: 10px;"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
    </div>

    <!-- 车辆列表 -->
    <div class="vehicles-section">
      <el-card shadow="hover">
        <template #header>
          <div class="card-header">
            <span>车辆列表</span>
          </div>
        </template>
        <div class="vehicles-grid">
          <el-card
            v-for="vehicle in vehicles"
            :key="vehicle.id"
            shadow="hover"
            class="vehicle-card"
            :class="{ 'vehicle-available': vehicle.status === 'available', 'vehicle-in-use': vehicle.status === 'in_use', 'vehicle-maintenance': vehicle.status === 'maintenance' }"
          >
            <template #header>
              <div class="vehicle-header">
                <h4>{{ vehicle.vehicleNo }}</h4>
                <el-tag :type="vehicleStatusMap[vehicle.status]">{{ vehicle.status }}</el-tag>
              </div>
            </template>
            <div class="vehicle-info">
              <div class="info-item">
                <span class="info-label">车型：</span>
                <span class="info-value">{{ vehicle.vehicleModel }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">座位数：</span>
                <span class="info-value">{{ vehicle.seats }}座</span>
              </div>
              <div class="info-item">
                <span class="info-label">司机：</span>
                <span class="info-value">{{ vehicle.driverName || '无' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">燃料类型：</span>
                <span class="info-value">{{ vehicle.fuelType }}</span>
              </div>
            </div>
            <div class="vehicle-actions">
              <el-button size="small" type="primary" @click="requestVehicle(vehicle)" :disabled="vehicle.status !== 'available'">
                申请使用
              </el-button>
            </div>
          </el-card>
        </div>
      </el-card>
    </div>

    <!-- 用车申请记录 -->
    <div class="requests-section">
      <el-card shadow="hover" style="margin-top: 20px;">
        <template #header>
          <div class="card-header">
            <span>用车申请记录</span>
          </div>
        </template>
        <el-table :data="filteredRequests" stripe style="width: 100%">
          <el-table-column prop="reservationNo" label="申请编号" width="150" />
          <el-table-column prop="vehicleNo" label="车牌号" width="120" />
          <el-table-column prop="vehicleModel" label="车型" width="120" />
          <el-table-column prop="startTime" label="开始时间" width="180" />
          <el-table-column prop="endTime" label="结束时间" width="180" />
          <el-table-column prop="destination" label="目的地" min-width="150" />
          <el-table-column prop="passengerCount" label="乘车人数" width="100" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="scope">
              <el-tag :type="requestStatusMap[scope.row.status]">{{ scope.row.status }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="scope">
              <el-button size="small" @click="showRequestDetail(scope.row)">详情</el-button>
              <el-button size="small" type="danger" @click="cancelRequest(scope.row)" v-if="scope.row.status === 'pending' || scope.row.status === 'approved'">取消</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>

    <!-- 用车申请表单对话框 -->
    <el-dialog
      v-model="showCreateRequestDialog"
      title="申请用车"
      width="600px"
    >
      <el-form :model="requestForm" label-width="100px">
        <el-form-item label="车辆" required>
          <el-select v-model="requestForm.vehicleId" placeholder="请选择车辆">
            <el-option
              v-for="vehicle in vehicles"
              :key="vehicle.id"
              :label="`${vehicle.vehicleNo} (${vehicle.vehicleModel})`"
              :value="vehicle.id"
              :disabled="vehicle.status !== 'available'"
            />
          </el-select>
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="开始时间" required>
              <el-date-picker
                v-model="requestForm.startTime"
                type="datetime"
                placeholder="选择开始时间"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束时间" required>
              <el-date-picker
                v-model="requestForm.endTime"
                type="datetime"
                placeholder="选择结束时间"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="目的地" required>
          <el-input v-model="requestForm.destination" placeholder="请输入目的地" />
        </el-form-item>
        <el-form-item label="乘车人数" required>
          <el-input v-model.number="requestForm.passengerCount" placeholder="请输入乘车人数" />
        </el-form-item>
        <el-form-item label="用途" required>
          <el-input
            v-model="requestForm.purpose"
            type="textarea"
            placeholder="请输入用车用途"
            :rows="4"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showCreateRequestDialog = false">取消</el-button>
          <el-button type="primary" @click="submitRequest">提交申请</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 用车申请详情对话框 -->
    <el-dialog
      v-model="showRequestDetailDialog"
      title="用车申请详情"
      width="700px"
    >
      <div v-if="selectedRequest" class="request-detail">
        <div class="detail-info">
          <div class="info-item">
            <span class="info-label">申请编号：</span>
            <span class="info-value">{{ selectedRequest.reservationNo }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">车牌号：</span>
            <span class="info-value">{{ selectedRequest.vehicleNo }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">车型：</span>
            <span class="info-value">{{ selectedRequest.vehicleModel }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">申请人：</span>
            <span class="info-value">{{ selectedRequest.requesterName }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">开始时间：</span>
            <span class="info-value">{{ selectedRequest.startTime }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">结束时间：</span>
            <span class="info-value">{{ selectedRequest.endTime }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">目的地：</span>
            <span class="info-value">{{ selectedRequest.destination }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">乘车人数：</span>
            <span class="info-value">{{ selectedRequest.passengerCount }}人</span>
          </div>
          <div class="info-item">
            <span class="info-label">状态：</span>
            <el-tag :type="requestStatusMap[selectedRequest.status]">{{ selectedRequest.status }}</el-tag>
          </div>
        </div>
        <div class="detail-purpose">
          <h4>用车用途</h4>
          <p>{{ selectedRequest.purpose }}</p>
        </div>
        <div class="detail-approval" v-if="selectedRequest.status !== 'pending'">
          <h4>审批信息</h4>
          <div class="approval-item">
            <span class="approval-label">审批人：</span>
            <span class="approval-value">{{ selectedRequest.approverName || '无' }}</span>
          </div>
          <div class="approval-item">
            <span class="approval-label">审批时间：</span>
            <span class="approval-value">{{ selectedRequest.approveTime || '无' }}</span>
          </div>
          <div class="approval-item">
            <span class="approval-label">审批意见：</span>
            <span class="approval-value">{{ selectedRequest.approvalComment || '无' }}</span>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { Plus, Search } from '@element-plus/icons-vue'

// 定义车辆类型
interface Vehicle {
  id: number
  vehicleNo: string
  vehicleModel: string
  seats: number
  driverId: number | null
  driverName: string | null
  status: 'available' | 'in_use' | 'maintenance'
  fuelType: string
}

// 定义用车申请类型
interface VehicleReservation {
  id: number
  reservationNo: string
  vehicleId: number
  vehicleNo: string
  vehicleModel: string
  requesterId: number
  requesterName: string
  startTime: string
  endTime: string
  destination: string
  purpose: string
  passengerCount: number
  status: 'pending' | 'approved' | 'rejected' | 'in_progress' | 'completed'
  approverId: number | null
  approverName: string | null
  approveTime: string | null
  approvalComment: string | null
}

// 模拟数据 - 车辆列表
const vehicles = ref<Vehicle[]>([
  {
    id: 1,
    vehicleNo: '京A12345',
    vehicleModel: '奥迪A6L',
    seats: 5,
    driverId: 1,
    driverName: '张三',
    status: 'available',
    fuelType: '汽油'
  },
  {
    id: 2,
    vehicleNo: '京B67890',
    vehicleModel: '别克GL8',
    seats: 7,
    driverId: 2,
    driverName: '李四',
    status: 'in_use',
    fuelType: '汽油'
  },
  {
    id: 3,
    vehicleNo: '京C24680',
    vehicleModel: '丰田凯美瑞',
    seats: 5,
    driverId: 3,
    driverName: '王五',
    status: 'available',
    fuelType: '汽油'
  },
  {
    id: 4,
    vehicleNo: '京D13579',
    vehicleModel: '大众帕萨特',
    seats: 5,
    driverId: null,
    driverName: null,
    status: 'maintenance',
    fuelType: '柴油'
  },
  {
    id: 5,
    vehicleNo: '京E97531',
    vehicleModel: '金杯海狮',
    seats: 11,
    driverId: 4,
    driverName: '赵六',
    status: 'available',
    fuelType: '柴油'
  }
])

// 模拟数据 - 用车申请记录
const requests = ref<VehicleReservation[]>([
  {
    id: 1,
    reservationNo: 'CAR20251217001',
    vehicleId: 1,
    vehicleNo: '京A12345',
    vehicleModel: '奥迪A6L',
    requesterId: 1,
    requesterName: '张三',
    startTime: '2025-12-17 09:00:00',
    endTime: '2025-12-17 18:00:00',
    destination: '上海',
    purpose: '客户拜访',
    passengerCount: 3,
    status: 'in_progress',
    approverId: 2,
    approverName: '李四',
    approveTime: '2025-12-16 16:30:00',
    approvalComment: '同意'
  },
  {
    id: 2,
    reservationNo: 'CAR20251218001',
    vehicleId: 3,
    vehicleNo: '京C24680',
    vehicleModel: '丰田凯美瑞',
    requesterId: 1,
    requesterName: '张三',
    startTime: '2025-12-18 10:00:00',
    endTime: '2025-12-18 15:00:00',
    destination: '天津',
    purpose: '会议',
    passengerCount: 2,
    status: 'pending',
    approverId: null,
    approverName: null,
    approveTime: null,
    approvalComment: null
  },
  {
    id: 3,
    reservationNo: 'CAR20251219001',
    vehicleId: 5,
    vehicleNo: '京E97531',
    vehicleModel: '金杯海狮',
    requesterId: 1,
    requesterName: '张三',
    startTime: '2025-12-19 08:30:00',
    endTime: '2025-12-19 17:30:00',
    destination: '河北廊坊',
    purpose: '员工活动',
    passengerCount: 10,
    status: 'approved',
    approverId: 2,
    approverName: '李四',
    approveTime: '2025-12-17 10:15:00',
    approvalComment: '已批准'
  },
  {
    id: 4,
    reservationNo: 'CAR20251216001',
    vehicleId: 2,
    vehicleNo: '京B67890',
    vehicleModel: '别克GL8',
    requesterId: 1,
    requesterName: '张三',
    startTime: '2025-12-16 13:00:00',
    endTime: '2025-12-16 17:00:00',
    destination: '昌平',
    purpose: '培训',
    passengerCount: 6,
    status: 'completed',
    approverId: 2,
    approverName: '李四',
    approveTime: '2025-12-15 14:45:00',
    approvalComment: '已完成'
  }
])

// 筛选条件
const filterStatus = ref('')
const filterDate = ref('')
const searchKeyword = ref('')

// 状态映射
const vehicleStatusMap: Record<string, string> = {
  available: 'success',
  in_use: 'warning',
  maintenance: 'danger'
}

const requestStatusMap: Record<string, string> = {
  pending: 'info',
  approved: 'success',
  rejected: 'danger',
  in_progress: 'warning',
  completed: 'success'
}

// 计算筛选后的用车申请
const filteredRequests = computed(() => {
  return requests.value.filter(request => {
    // 状态筛选
    if (filterStatus.value && request.status !== filterStatus.value) {
      return false
    }
    // 日期筛选
    if (filterDate.value) {
      const requestDate = request.startTime.split(' ')[0]
      if (requestDate !== filterDate.value) {
        return false
      }
    }
    // 关键词搜索
    if (searchKeyword.value) {
      const keyword = searchKeyword.value.toLowerCase()
      return (
        request.vehicleNo.toLowerCase().includes(keyword) ||
        request.vehicleModel.toLowerCase().includes(keyword)
      )
    }
    return true
  })
})

// 统计数据
const stats = ref({
  totalVehicles: vehicles.value.length,
  availableVehicles: vehicles.value.filter(v => v.status === 'available').length,
  todayRequests: requests.value.filter(r => r.startTime.startsWith((new Date().toISOString().split('T')[0]) || '')).length,
  pendingRequests: requests.value.filter(r => r.status === 'pending').length
})

// 用车申请表单
const showCreateRequestDialog = ref(false)
const requestForm = ref({
  vehicleId: 0,
  startTime: '',
  endTime: '',
  destination: '',
  purpose: '',
  passengerCount: 1
})

// 用车申请详情
const showRequestDetailDialog = ref(false)
const selectedRequest = ref<VehicleReservation | null>(null)

// 申请用车
const requestVehicle = (vehicle: Vehicle) => {
  requestForm.value.vehicleId = vehicle.id
  requestForm.value.startTime = ''
  requestForm.value.endTime = ''
  requestForm.value.destination = ''
  requestForm.value.purpose = ''
  requestForm.value.passengerCount = 1
  showCreateRequestDialog.value = true
}

// 提交用车申请
const submitRequest = () => {
  // 这里可以添加表单验证和提交逻辑
  console.log('提交用车申请:', requestForm.value)
  showCreateRequestDialog.value = false
}

// 显示申请详情
const showRequestDetail = (request: VehicleReservation) => {
  selectedRequest.value = request
  showRequestDetailDialog.value = true
}

// 取消申请
const cancelRequest = (request: VehicleReservation) => {
  request.status = 'rejected'
  console.log('取消用车申请:', request)
}
</script>

<style scoped>
.vehicle-management-view {
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
  flex-wrap: wrap;
}

.vehicles-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.vehicle-card {
  transition: all 0.3s ease;
}

.vehicle-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
}

.vehicle-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.vehicle-header h4 {
  margin: 0;
}

.vehicle-info {
  margin: 10px 0;
}

.vehicle-info .info-item {
  margin-bottom: 5px;
  font-size: 14px;
}

.vehicle-actions {
  margin-top: 15px;
  text-align: right;
}

.vehicle-available {
  border-left: 4px solid #67c23a;
}

.vehicle-in-use {
  border-left: 4px solid #e6a23c;
}

.vehicle-maintenance {
  border-left: 4px solid #f56c6c;
}

.requests-section {
  margin-top: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
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

.detail-purpose {
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.detail-purpose h4 {
  margin: 0 0 10px 0;
}

.detail-approval {
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.detail-approval h4 {
  margin: 0 0 15px 0;
}

.approval-item {
  margin-bottom: 10px;
  display: flex;
  align-items: center;
}

.approval-label {
  width: 80px;
  font-weight: bold;
}
</style>
