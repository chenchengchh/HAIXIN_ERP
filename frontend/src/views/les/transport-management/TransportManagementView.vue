<template>
  <div class="transport-management-view">
    <div class="page-header">
      <h2>运输管理</h2>
      <el-breadcrumb>
        <el-breadcrumb-item>
          <router-link to="/home/les">首页</router-link>
        </el-breadcrumb-item>
        <el-breadcrumb-item>
          <router-link to="/home/les">LES系统</router-link>
        </el-breadcrumb-item>
        <el-breadcrumb-item>
          <router-link to="/home/les/transport-management">运输管理</router-link>
        </el-breadcrumb-item>
        <el-breadcrumb-item :to="`/home/les/transport-management#${activeTab}`">
          {{ tabLabelMap[activeTab] }}
        </el-breadcrumb-item>
      </el-breadcrumb>
    </div>
    
    <!-- 功能标签页 -->
    <el-tabs v-model="activeTab" class="function-tabs">
      <!-- 运输计划 -->
      <el-tab-pane label="运输计划" name="plan">
        <div class="tab-content">
          <el-card shadow="hover">
            <template #header>
              <div class="card-header">
                <span>智能生成运输计划建议，预览目的地与货量</span>
                <el-button type="primary" @click="handleCreatePlan">创建计划</el-button>
              </div>
            </template>
            <div class="plan-content">
              <!-- 筛选条件 -->
              <div class="filter-bar">
                <el-input v-model="planFilter.keyword" placeholder="搜索计划编号" prefix-icon="Search" clearable style="width: 200px; margin-right: 16px;"></el-input>
                <el-select v-model="planFilter.status" placeholder="选择状态" style="width: 120px; margin-right: 16px;">
                  <el-option label="全部" value="" />
                  <el-option label="计划中" :value="types.TransportPlanStatus.PLANNING" />
                  <el-option label="运输中" :value="types.TransportPlanStatus.IN_TRANSIT" />
                  <el-option label="已签收" :value="types.TransportPlanStatus.SIGNED" />
                  <el-option label="异常中断" :value="types.TransportPlanStatus.ABORTED" />
                </el-select>
                <el-date-picker v-model="planFilter.dateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" style="width: 300px;"></el-date-picker>
              </div>
              
              <!-- 计划列表 -->
              <el-table :data="pagedPlans" style="width: 100%" height="500">
                <el-table-column prop="planNo" label="计划编号" width="150" />
                <el-table-column prop="salesOrderNo" label="销售订单" width="150" />
                <el-table-column prop="vehicleId" label="车辆ID" width="100" />
                <el-table-column prop="driverId" label="司机ID" width="100" />
                <el-table-column prop="routeId" label="路线ID" width="100" />
                <el-table-column prop="status" label="状态" width="100">
                  <template #default="scope">
                    <el-tag :type="getStatusTag(scope.row.status)">
                      {{ getStatusLabel(scope.row.status) }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="costEstimated" label="预估费用" width="100">
                  <template #default="scope">
                    {{ scope.row.costEstimated }} 元
                  </template>
                </el-table-column>
                <el-table-column prop="createTime" label="创建时间" width="180" />
                <el-table-column label="操作" width="150">
                  <template #default="scope">
                    <el-button size="small" type="primary" @click="handleViewDetail(scope.row)">详情</el-button>
                    <el-button size="small" @click="handleEdit(scope.row)">编辑</el-button>
                  </template>
                </el-table-column>
              </el-table>
              
              <!-- 分页 -->
              <div class="pagination-container">
                <el-pagination
                  layout="total, sizes, prev, pager, next, jumper"
                  :total="filteredPlans.length"
                  :page-size="planPageSize"
                  :current-page="planCurrentPage"
                  :page-sizes="[10, 20, 50, 100]"
                  @size-change="handleSizeChange"
                  @current-change="handleCurrentChange"
                />
              </div>
            </div>
          </el-card>
        </div>
      </el-tab-pane>
      
      <!-- 运输资源管理 -->
      <el-tab-pane label="资源管理" name="resource">
        <div class="tab-content">
          <el-card shadow="hover">
            <template #header>
              <div class="card-header">
                <span>车辆（车牌、载重）、司机（电话、驾照）及配送路线的综合档案管理</span>
                <el-button type="primary" @click="handleAddResource">添加资源</el-button>
              </div>
            </template>
            <div class="resource-content">
              <!-- 资源类型切换 -->
              <el-radio-group v-model="resourceType" size="large" class="resource-type-switch">
                <el-radio-button value="vehicle">车辆</el-radio-button>
                <el-radio-button value="driver">司机</el-radio-button>
                <el-radio-button value="route">路线</el-radio-button>
              </el-radio-group>
              
              <!-- 车辆列表 -->
              <div v-if="resourceType === 'vehicle'" class="resource-list">
                <el-table :data="store.vehicles" style="width: 100%" height="500">
                  <el-table-column prop="licensePlate" label="车牌" width="120" />
                  <el-table-column prop="vehicleType" label="车辆类型" width="100" />
                  <el-table-column prop="loadCapacity" label="载重(吨)" width="100" />
                  <el-table-column prop="status" label="状态" width="100">
                    <template #default="scope">
                      <el-tag :type="scope.row.status === 'active' ? 'success' : 'info'">
                        {{ scope.row.status === 'active' ? '活跃' : '可用' }}
                      </el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="createTime" label="创建时间" width="180" />
                  <el-table-column label="操作" width="120">
                    <template #default="scope">
                      <el-button size="small" type="primary" @click="handleResourceDetail(scope.row)">详情</el-button>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
              
              <!-- 司机列表 -->
              <div v-if="resourceType === 'driver'" class="resource-list">
                <el-table :data="store.drivers" style="width: 100%" height="500">
                  <el-table-column prop="name" label="姓名" width="100" />
                  <el-table-column prop="phone" label="电话" width="120" />
                  <el-table-column prop="licenseNo" label="驾照号" width="180" />
                  <el-table-column prop="status" label="状态" width="100">
                    <template #default="scope">
                      <el-tag :type="scope.row.status === 'available' ? 'success' : 'info'">
                        {{ scope.row.status === 'available' ? '可用' : '活跃' }}
                      </el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="createTime" label="创建时间" width="180" />
                  <el-table-column label="操作" width="120">
                    <template #default="scope">
                      <el-button size="small" type="primary" @click="handleResourceDetail(scope.row)">详情</el-button>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
              
              <!-- 路线列表 -->
              <div v-if="resourceType === 'route'" class="resource-list">
                <el-table :data="store.routes" style="width: 100%" height="500">
                  <el-table-column prop="routeName" label="路线名称" width="150" />
                  <el-table-column prop="startLocation" label="起点" width="100" />
                  <el-table-column prop="endLocation" label="终点" width="100" />
                  <el-table-column prop="distance" label="距离(km)" width="100" />
                  <el-table-column prop="estimatedTime" label="预计时间(min)" width="120" />
                  <el-table-column prop="createTime" label="创建时间" width="180" />
                  <el-table-column label="操作" width="120">
                    <template #default="scope">
                      <el-button size="small" type="primary" @click="handleResourceDetail(scope.row)">详情</el-button>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
            </div>
          </el-card>
        </div>
      </el-tab-pane>
      
      <!-- 运输调度 -->
      <el-tab-pane label="运输调度" name="dispatch">
        <div class="tab-content">
          <el-card shadow="hover">
            <template #header>
              <div class="card-header">
                <span>图形化展现任务池，实现运输任务的一键指派与司机抢单管理</span>
              </div>
            </template>
            <div class="dispatch-content">
              <div class="task-pool">
                <h3>待指派任务池</h3>
                <el-table :data="taskPool" style="width: 100%" height="400">
                  <el-table-column type="selection" width="55" />
                  <el-table-column prop="taskNo" label="任务编号" width="150" />
                  <el-table-column prop="planId" label="计划ID" width="100" />
                  <el-table-column prop="driverId" label="司机ID" width="100" />
                  <el-table-column prop="vehicleId" label="车辆ID" width="100" />
                  <el-table-column prop="status" label="状态" width="100">
                    <template #default="scope">
                      <el-tag type="warning">{{ scope.row.status }}</el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="createTime" label="创建时间" width="180" />
                  <el-table-column label="操作" width="120">
                    <template #default="scope">
                      <el-button size="small" type="primary" @click="handleAssignTask(scope.row)">一键指派</el-button>
                    </template>
                  </el-table-column>
                </el-table>
                <div class="task-action-buttons">
                  <el-button type="primary" @click="handleBatchAssign">批量指派</el-button>
                  <el-button type="info" @click="handleRefreshTaskPool">刷新任务池</el-button>
                </div>
              </div>
              
              <div class="dispatch-result">
                <h3>调度结果</h3>
                <el-table :data="dispatchResults" style="width: 100%" height="400">
                  <el-table-column prop="taskNo" label="任务编号" width="150" />
                  <el-table-column prop="driverName" label="司机" width="100" />
                  <el-table-column prop="vehiclePlate" label="车辆" width="120" />
                  <el-table-column prop="status" label="状态" width="100">
                    <template #default="scope">
                      <el-tag :type="scope.row.status === 'assigned' ? 'success' : 'warning'">
                        {{ scope.row.status === 'assigned' ? '已指派' : '待接收' }}
                      </el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="assignTime" label="指派时间" width="180" />
                  <el-table-column label="操作" width="120">
                    <template #default="scope">
                      <el-button size="small" type="primary" @click="handleViewDispatchDetail(scope.row)">详情</el-button>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
            </div>
          </el-card>
        </div>
      </el-tab-pane>
      
      <!-- 运输成本控制 -->
      <el-tab-pane label="成本控制" name="cost">
        <div class="tab-content">
          <el-card shadow="hover">
            <template #header>
              <div class="card-header">
                <span>自动核算每单的燃油、过路费及人员提成，生成费用对账单</span>
              </div>
            </template>
            <div class="cost-content">
              <!-- 成本统计 -->
              <div class="cost-stats">
                <el-row :gutter="20">
                  <el-col :span="6">
                    <el-card shadow="hover" class="stat-card">
                      <el-statistic :value="totalCost" title="总费用(元)">
                        <template #suffix>
                          <span style="font-size: 14px; color: #909399;">元</span>
                        </template>
                      </el-statistic>
                    </el-card>
                  </el-col>
                  <el-col :span="6">
                    <el-card shadow="hover" class="stat-card">
                      <el-statistic :value="fuelCost" title="燃油费用(元)">
                        <template #suffix>
                          <span style="font-size: 14px; color: #909399;">元</span>
                        </template>
                      </el-statistic>
                    </el-card>
                  </el-col>
                  <el-col :span="6">
                    <el-card shadow="hover" class="stat-card">
                      <el-statistic :value="tollCost" title="过路费(元)">
                        <template #suffix>
                          <span style="font-size: 14px; color: #909399;">元</span>
                        </template>
                      </el-statistic>
                    </el-card>
                  </el-col>
                  <el-col :span="6">
                    <el-card shadow="hover" class="stat-card">
                      <el-statistic :value="driverSalary" title="司机提成(元)">
                        <template #suffix>
                          <span style="font-size: 14px; color: #909399;">元</span>
                        </template>
                      </el-statistic>
                    </el-card>
                  </el-col>
                </el-row>
              </div>
              
              <!-- 费用对账单列表 -->
              <div class="cost-bill-list">
                <h3>费用对账单</h3>
                <el-table :data="costBills" style="width: 100%" height="400">
                  <el-table-column prop="billNo" label="账单编号" width="150" />
                  <el-table-column prop="planId" label="计划ID" width="100" />
                  <el-table-column prop="fuelCost" label="燃油费用" width="100">
                    <template #default="scope">
                      {{ scope.row.fuelCost }} 元
                    </template>
                  </el-table-column>
                  <el-table-column prop="tollCost" label="过路费" width="100">
                    <template #default="scope">
                      {{ scope.row.tollCost }} 元
                    </template>
                  </el-table-column>
                  <el-table-column prop="driverSalary" label="司机提成" width="120">
                    <template #default="scope">
                      {{ scope.row.driverSalary }} 元
                    </template>
                  </el-table-column>
                  <el-table-column prop="otherCost" label="其他费用" width="100">
                    <template #default="scope">
                      {{ scope.row.otherCost }} 元
                    </template>
                  </el-table-column>
                  <el-table-column prop="totalCost" label="总费用" width="100">
                    <template #default="scope">
                      <el-tag type="success">{{ scope.row.totalCost }} 元</el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="createTime" label="生成时间" width="180" />
                  <el-table-column label="操作" width="120">
                    <template #default="scope">
                      <el-button size="small" type="primary" @click="handleViewBill(scope.row)">查看账单</el-button>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
            </div>
          </el-card>
        </div>
      </el-tab-pane>
    </el-tabs>

    <el-drawer v-model="planDetailVisible" title="计划详情" size="520px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="计划编号">{{ planDetail?.planNo }}</el-descriptions-item>
        <el-descriptions-item label="销售订单">{{ planDetail?.salesOrderNo }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ getStatusLabel(planDetail?.status as any) }}</el-descriptions-item>
        <el-descriptions-item label="预估费用">{{ planDetail?.costEstimated }}</el-descriptions-item>
        <el-descriptions-item label="车辆">{{ planDetailVehicle?.licensePlate || planDetail?.vehicleId }}</el-descriptions-item>
        <el-descriptions-item label="司机">{{ planDetailDriver?.name || planDetail?.driverId }}</el-descriptions-item>
        <el-descriptions-item label="路线">{{ planDetailRoute?.routeName || planDetail?.routeId }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ planDetail?.createTime }}</el-descriptions-item>
      </el-descriptions>

      <div style="margin-top: 16px;">
        <el-table :data="planDetailTasks" style="width: 100%" max-height="260">
          <el-table-column prop="taskNo" label="任务编号" width="160" />
          <el-table-column prop="status" label="状态" width="120" />
          <el-table-column prop="createTime" label="创建时间" />
        </el-table>
      </div>
    </el-drawer>

    <el-dialog v-model="resourceDetailVisible" title="资源详情" width="520px">
      <el-form v-if="resourceEdit" label-width="100px">
        <template v-if="resourceType === 'vehicle'">
          <el-form-item label="车牌">
            <el-input v-model="resourceEdit.licensePlate" />
          </el-form-item>
          <el-form-item label="车辆类型">
            <el-input v-model="resourceEdit.vehicleType" />
          </el-form-item>
          <el-form-item label="载重(吨)">
            <el-input-number v-model="resourceEdit.loadCapacity" :min="0" :step="0.1" />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="resourceEdit.status" style="width: 100%;">
              <el-option label="可用" value="available" />
              <el-option label="活跃" value="active" />
            </el-select>
          </el-form-item>
        </template>

        <template v-else-if="resourceType === 'driver'">
          <el-form-item label="姓名">
            <el-input v-model="resourceEdit.name" />
          </el-form-item>
          <el-form-item label="电话">
            <el-input v-model="resourceEdit.phone" />
          </el-form-item>
          <el-form-item label="驾照号">
            <el-input v-model="resourceEdit.licenseNo" />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="resourceEdit.status" style="width: 100%;">
              <el-option label="可用" value="available" />
              <el-option label="活跃" value="active" />
            </el-select>
          </el-form-item>
        </template>

        <template v-else>
          <el-form-item label="路线名称">
            <el-input v-model="resourceEdit.routeName" />
          </el-form-item>
          <el-form-item label="起点">
            <el-input v-model="resourceEdit.startLocation" />
          </el-form-item>
          <el-form-item label="终点">
            <el-input v-model="resourceEdit.endLocation" />
          </el-form-item>
          <el-form-item label="距离(km)">
            <el-input-number v-model="resourceEdit.distance" :min="0" :step="0.1" />
          </el-form-item>
          <el-form-item label="预计时间(min)">
            <el-input-number v-model="resourceEdit.estimatedTime" :min="0" :step="1" />
          </el-form-item>
        </template>
      </el-form>
      <template #footer>
        <el-button @click="resourceDetailVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveResource">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="dispatchDetailVisible" title="调度详情" width="520px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="任务编号">{{ dispatchDetail?.taskNo }}</el-descriptions-item>
        <el-descriptions-item label="司机">{{ dispatchDetail?.driverName }}</el-descriptions-item>
        <el-descriptions-item label="车辆">{{ dispatchDetail?.vehiclePlate }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ dispatchDetail?.status }}</el-descriptions-item>
        <el-descriptions-item label="指派时间">{{ dispatchDetail?.assignTime }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button type="primary" @click="dispatchDetailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="billDetailVisible" title="账单详情" width="520px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="账单编号">{{ billDetail?.billNo }}</el-descriptions-item>
        <el-descriptions-item label="计划ID">{{ billDetail?.planId }}</el-descriptions-item>
        <el-descriptions-item label="燃油费用">{{ billDetail?.fuelCost }}</el-descriptions-item>
        <el-descriptions-item label="过路费">{{ billDetail?.tollCost }}</el-descriptions-item>
        <el-descriptions-item label="司机提成">{{ billDetail?.driverSalary }}</el-descriptions-item>
        <el-descriptions-item label="其他费用">{{ billDetail?.otherCost }}</el-descriptions-item>
        <el-descriptions-item label="总费用">{{ billDetail?.totalCost }}</el-descriptions-item>
        <el-descriptions-item label="生成时间">{{ billDetail?.createTime }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button type="primary" @click="billDetailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="planCreateVisible" title="创建运输计划" width="520px">
      <el-form :model="planCreateForm" label-width="90px">
        <el-form-item label="销售订单">
          <el-select v-model="planCreateForm.salesOrderNo" style="width: 100%;" filterable>
            <el-option v-for="o in store.salesOrders" :key="o.id" :label="o.orderNo" :value="o.orderNo" />
          </el-select>
        </el-form-item>
        <el-form-item label="车辆">
          <el-select v-model="planCreateForm.vehicleId" style="width: 100%;" filterable>
            <el-option v-for="v in store.vehicles" :key="v.id" :label="v.licensePlate" :value="v.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="司机">
          <el-select v-model="planCreateForm.driverId" style="width: 100%;" filterable>
            <el-option v-for="d in store.drivers" :key="d.id" :label="d.name" :value="d.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="路线">
          <el-select v-model="planCreateForm.routeId" style="width: 100%;" filterable>
            <el-option v-for="r in store.routes" :key="r.id" :label="r.routeName" :value="r.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="预估费用">
          <el-input-number v-model="planCreateForm.costEstimated" :min="0" :step="1" style="width: 100%;" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="planCreateVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitCreatePlan">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import useLesStore from '../../../stores/les'
import * as types from '../../../types/les'
import { ElMessage } from 'element-plus'

// 激活的标签页
const activeTab = ref('plan')

// 初始化store
const store = useLesStore()

// 标签页名称映射
const tabLabelMap: Record<string, string> = {
  plan: '运输计划',
  resource: '资源管理',
  dispatch: '运输调度',
  cost: '成本控制'
}

// 计划筛选条件
const planFilter = ref({
  keyword: '',
  status: '' as '' | types.TransportPlanStatus,
  dateRange: [] as Date[]
})

const planCurrentPage = ref(1)
const planPageSize = ref(10)

const filteredPlans = computed(() => {
  const keyword = (planFilter.value.keyword || '').trim().toLowerCase()
  const status = planFilter.value.status as any
  const [start, end] = (planFilter.value.dateRange || []) as any[]
  const startTime = start ? new Date(start).getTime() : undefined
  const endTime = end ? new Date(end).getTime() : undefined
  return store.transportPlans.filter(p => {
    if (status && p.status !== status) return false
    if (keyword && !(p.planNo || '').toLowerCase().includes(keyword)) return false
    if (startTime || endTime) {
      const t = p.createTime ? new Date(p.createTime).getTime() : 0
      if (startTime && t < startTime) return false
      if (endTime && t > endTime + 24 * 60 * 60 * 1000 - 1) return false
    }
    return true
  })
})

const pagedPlans = computed(() => {
  const start = (planCurrentPage.value - 1) * planPageSize.value
  return filteredPlans.value.slice(start, start + planPageSize.value)
})

// 资源类型
const resourceType = ref('vehicle')

// 任务池（从store计算）
const taskPool = computed(() => {
  const planIdWithTask = new Set(store.transportTasks.map(t => t.planId))
  return store.transportPlans
    .filter(p => p.status === types.TransportPlanStatus.PLANNING && !planIdWithTask.has(p.id))
    .map(p => ({
      id: p.id,
      taskNo: `AUTO-${p.planNo}`,
      planId: p.id,
      driverId: p.driverId || 0,
      vehicleId: p.vehicleId || 0,
      status: '待指派',
      createTime: p.createTime
    }))
})

const dispatchResults = computed(() => {
  return store.transportTasks.map(t => {
    const driver = store.drivers.find(d => d.id === t.driverId)
    const vehicle = store.vehicles.find(v => v.id === t.vehicleId)
    return {
      id: t.id,
      taskNo: t.taskNo,
      driverName: driver?.name || '未指派',
      vehiclePlate: vehicle?.licensePlate || '未指派',
      status: t.status,
      assignTime: t.createTime
    }
  })
})

// 费用对账单（从store计算）
const costBills = computed(() => {
  return store.transportCosts.map(cost => ({
    id: cost.id,
    billNo: `BILL-${new Date().getFullYear()}${String(new Date().getMonth() + 1).padStart(2, '0')}${String(new Date().getDate()).padStart(2, '0')}-${String(cost.id).padStart(3, '0')}`,
    planId: cost.planId,
    fuelCost: cost.fuelCost,
    tollCost: cost.tollCost,
    driverSalary: cost.driverSalary,
    otherCost: cost.otherCost,
    totalCost: cost.totalCost,
    createTime: cost.createTime
  }))
})

// 成本统计（从store获取）
const totalCost = computed(() => store.totalCost)
const fuelCost = computed(() => store.transportCosts.reduce((sum, cost) => sum + (cost.fuelCost || 0), 0))
const tollCost = computed(() => store.transportCosts.reduce((sum, cost) => sum + (cost.tollCost || 0), 0))
const driverSalary = computed(() => store.transportCosts.reduce((sum, cost) => sum + (cost.driverSalary || 0), 0))

// 获取状态标签类型
const getStatusTag = (status: types.TransportPlanStatus | string) => {
  switch (status) {
    case types.TransportPlanStatus.PLANNING: return 'warning'
    case types.TransportPlanStatus.IN_TRANSIT: return 'primary'
    case types.TransportPlanStatus.SIGNED: return 'success'
    case types.TransportPlanStatus.ABORTED: return 'danger'
    default: return 'info'
  }
}

// 获取状态标签
const getStatusLabel = (status: types.TransportPlanStatus | string) => {
  switch (status) {
    case types.TransportPlanStatus.PLANNING: return '计划中'
    case types.TransportPlanStatus.IN_TRANSIT: return '运输中'
    case types.TransportPlanStatus.SIGNED: return '已签收'
    case types.TransportPlanStatus.ABORTED: return '异常中断'
    default: return status
  }
}

// 组件挂载时加载数据
onMounted(async () => {
  await Promise.all([
    store.fetchTransportPlans(),
    store.fetchVehicles(),
    store.fetchDrivers(),
    store.fetchRoutes(),
    store.fetchSalesOrders(),
    store.fetchTransportTasks(),
    store.fetchTransportCosts(),
    store.fetchTransportStats()
  ])
})

// 创建计划
const handleCreatePlan = async () => {
  try {
    await Promise.all([store.fetchSalesOrders(), store.fetchVehicles(), store.fetchDrivers(), store.fetchRoutes()])
    planCreateForm.value = {
      salesOrderNo: store.salesOrders[0]?.orderNo || '',
      vehicleId: store.vehicles[0]?.id || 0,
      driverId: store.drivers[0]?.id || 0,
      routeId: store.routes[0]?.id || 0,
      costEstimated: 0
    }
    planCreateVisible.value = true
  } catch (error) {
    ElMessage.error('创建计划失败')
    console.error('Failed to create plan:', error)
  }
}

// 查看详情
const handleViewDetail = async (plan: types.TransportPlan) => {
  try {
    planDetailVisible.value = true
    planDetail.value = plan
    const detail = await store.getTransportPlanDetail(plan.id)
    planDetail.value = detail
  } catch (error) {
    ElMessage.error(`加载计划 ${plan.planNo} 详情失败`)
    console.error('Failed to view plan detail:', error)
  }
}

// 编辑计划
const handleEdit = async (plan: types.TransportPlan) => {
  try {
    const nextStatus =
      plan.status === types.TransportPlanStatus.PLANNING
        ? types.TransportPlanStatus.IN_TRANSIT
        : plan.status === types.TransportPlanStatus.IN_TRANSIT
          ? types.TransportPlanStatus.SIGNED
          : plan.status
    await store.updateTransportPlan(plan.id, { status: nextStatus } as any)
    ElMessage.success(`计划 ${plan.planNo} 状态已更新`)
    console.log('Edit plan:', plan)
  } catch (error) {
    ElMessage.error(`编辑计划 ${plan.planNo} 失败`)
    console.error('Failed to edit plan:', error)
  }
}

// 分页大小变化
const handleSizeChange = (size: number) => {
  try {
    planPageSize.value = size
    planCurrentPage.value = 1
  } catch (error) {
    ElMessage.error('切换分页大小失败')
    console.error('Failed to change page size:', error)
  }
}

// 当前页变化
const handleCurrentChange = (current: number) => {
  try {
    planCurrentPage.value = current
  } catch (error) {
    ElMessage.error('跳转页面失败')
    console.error('Failed to change current page:', error)
  }
}

// 添加资源
const handleAddResource = async () => {
  try {
    if (resourceType.value === 'vehicle') {
      resourceEdit.value = { licensePlate: '', vehicleType: '', loadCapacity: 0, status: 'available' } as any
    } else if (resourceType.value === 'driver') {
      resourceEdit.value = { name: '', phone: '', licenseNo: '', status: 'available' } as any
    } else {
      resourceEdit.value = { routeName: '', startLocation: '', endLocation: '', distance: 0, estimatedTime: 0 } as any
    }
    resourceDetailVisible.value = true
  } catch (error) {
    ElMessage.error('添加资源失败')
    console.error('Failed to add resource:', error)
  }
}

// 资源详情
const handleResourceDetail = async (resource: types.Vehicle | types.Driver | types.Route) => {
  try {
    resourceEdit.value = { ...resource } as any
    resourceDetailVisible.value = true
  } catch (error) {
    ElMessage.error('加载资源详情失败')
    console.error('Failed to view resource detail:', error)
  }
}

// 指派任务
const handleAssignTask = async (task: any) => {
  try {
    ElMessage({ message: `正在指派任务 ${task.taskNo}...`, type: 'info' })
    const plan = store.transportPlans.find(p => p.id === task.planId)
    const driverId = plan?.driverId || store.drivers[0]?.id || 0
    const vehicleId = plan?.vehicleId || store.vehicles[0]?.id || 0
    await store.createTransportTask({
      taskNo: '',
      planId: task.planId,
      driverId,
      vehicleId,
      status: 'pending'
    } as any)
    await store.fetchTransportTasks()
    ElMessage.success(`任务 ${task.taskNo} 已成功指派`)
    console.log('Assign task:', task)
  } catch (error) {
    ElMessage.error(`指派任务 ${task.taskNo} 失败`)
    console.error('Failed to assign task:', error)
  }
}

// 批量指派
const handleBatchAssign = async () => {
  try {
    ElMessage({ message: '正在批量指派任务...', type: 'info' })
    const assignCount = taskPool.value.length
    for (const t of taskPool.value as any[]) {
      const plan = store.transportPlans.find(p => p.id === t.planId)
      const driverId = plan?.driverId || store.drivers[0]?.id || 0
      const vehicleId = plan?.vehicleId || store.vehicles[0]?.id || 0
      await store.createTransportTask({
        taskNo: '',
        planId: t.planId,
        driverId,
        vehicleId,
        status: 'pending'
      } as any)
    }
    await store.fetchTransportTasks()
    ElMessage.success(`已成功批量指派 ${assignCount} 个任务`)
    console.log('Batch assign tasks')
  } catch (error) {
    ElMessage.error('批量指派任务失败')
    console.error('Failed to batch assign tasks:', error)
  }
}

// 刷新任务池
const handleRefreshTaskPool = async () => {
  try {
    ElMessage({ message: '正在刷新任务池...', type: 'info' })
    await Promise.all([store.fetchTransportPlans(), store.fetchTransportTasks()])
    ElMessage.success('任务池已刷新')
    console.log('Refresh task pool')
  } catch (error) {
    ElMessage.error('刷新任务池失败')
    console.error('Failed to refresh task pool:', error)
  }
}

// 查看调度详情
const handleViewDispatchDetail = async (result: any) => {
  try {
    dispatchDetail.value = result
    dispatchDetailVisible.value = true
  } catch (error) {
    ElMessage.error(`加载调度结果 ${result.taskNo} 详情失败`)
    console.error('Failed to view dispatch detail:', error)
  }
}

// 查看账单
const handleViewBill = async (bill: any) => {
  try {
    billDetail.value = bill
    billDetailVisible.value = true
  } catch (error) {
    ElMessage.error(`加载账单 ${bill.billNo} 失败`)
    console.error('Failed to view bill:', error)
  }
}

const planDetailVisible = ref(false)
const planDetail = ref<types.TransportPlan | null>(null)
const planDetailVehicle = computed(() => store.vehicles.find(v => v.id === (planDetail.value?.vehicleId || 0)))
const planDetailDriver = computed(() => store.drivers.find(d => d.id === (planDetail.value?.driverId || 0)))
const planDetailRoute = computed(() => store.routes.find(r => r.id === (planDetail.value?.routeId || 0)))
const planDetailTasks = computed(() => store.transportTasks.filter(t => t.planId === (planDetail.value?.id || 0)))

const resourceDetailVisible = ref(false)
const resourceEdit = ref<any | null>(null)

const handleSaveResource = async () => {
  if (!resourceEdit.value) return
  try {
    const id = resourceEdit.value.id ? Number(resourceEdit.value.id) : 0
    if (resourceType.value === 'vehicle') {
      if (id) {
        await store.updateVehicle(id, {
          licensePlate: resourceEdit.value.licensePlate,
          vehicleType: resourceEdit.value.vehicleType,
          loadCapacity: resourceEdit.value.loadCapacity,
          status: resourceEdit.value.status
        })
        ElMessage.success('车辆已保存')
      } else {
        await store.createVehicle({
          licensePlate: resourceEdit.value.licensePlate,
          vehicleType: resourceEdit.value.vehicleType,
          loadCapacity: resourceEdit.value.loadCapacity,
          status: resourceEdit.value.status
        } as any)
        ElMessage.success('车辆已创建')
      }
    } else if (resourceType.value === 'driver') {
      if (id) {
        await store.updateDriver(id, {
          name: resourceEdit.value.name,
          phone: resourceEdit.value.phone,
          licenseNo: resourceEdit.value.licenseNo,
          status: resourceEdit.value.status
        })
        ElMessage.success('司机已保存')
      } else {
        await store.createDriver({
          name: resourceEdit.value.name,
          phone: resourceEdit.value.phone,
          licenseNo: resourceEdit.value.licenseNo,
          status: resourceEdit.value.status
        } as any)
        ElMessage.success('司机已创建')
      }
    } else {
      if (id) {
        await store.updateRoute(id, {
          routeName: resourceEdit.value.routeName,
          startLocation: resourceEdit.value.startLocation,
          endLocation: resourceEdit.value.endLocation,
          distance: resourceEdit.value.distance,
          estimatedTime: resourceEdit.value.estimatedTime
        })
        ElMessage.success('路线已保存')
      } else {
        await store.createRoute({
          routeName: resourceEdit.value.routeName,
          startLocation: resourceEdit.value.startLocation,
          endLocation: resourceEdit.value.endLocation,
          distance: resourceEdit.value.distance,
          estimatedTime: resourceEdit.value.estimatedTime
        } as any)
        ElMessage.success('路线已创建')
      }
    }
    resourceDetailVisible.value = false
  } catch (error) {
    ElMessage.error('保存资源失败')
    console.error('Failed to save resource:', error)
  }
}

const planCreateVisible = ref(false)
const planCreateForm = ref<{ salesOrderNo: string; vehicleId: number; driverId: number; routeId: number; costEstimated: number }>({
  salesOrderNo: '',
  vehicleId: 0,
  driverId: 0,
  routeId: 0,
  costEstimated: 0
})

const handleSubmitCreatePlan = async () => {
  try {
    if (!planCreateForm.value.salesOrderNo) {
      ElMessage.warning('请选择销售订单')
      return
    }
    if (!planCreateForm.value.vehicleId) {
      ElMessage.warning('请选择车辆')
      return
    }
    if (!planCreateForm.value.driverId) {
      ElMessage.warning('请选择司机')
      return
    }
    if (!planCreateForm.value.routeId) {
      ElMessage.warning('请选择路线')
      return
    }
    const created = await store.createTransportPlan({
      planNo: '',
      salesOrderNo: planCreateForm.value.salesOrderNo,
      vehicleId: planCreateForm.value.vehicleId,
      driverId: planCreateForm.value.driverId,
      routeId: planCreateForm.value.routeId,
      status: types.TransportPlanStatus.PLANNING,
      costEstimated: planCreateForm.value.costEstimated
    } as any)
    planCreateVisible.value = false
    ElMessage.success(`运输计划已创建：${created.planNo}`)
  } catch (error) {
    ElMessage.error('创建计划失败')
    console.error('Failed to submit create plan:', error)
  }
}

const dispatchDetailVisible = ref(false)
const dispatchDetail = ref<any | null>(null)

const billDetailVisible = ref(false)
const billDetail = ref<any | null>(null)
</script>

<style scoped>
.transport-management-view {
  padding: 20px;
  height: 100%;
  overflow: auto;
}

/* 页面头部样式 */
.page-header {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 8px;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--border-color-light);
}

.page-header h2 {
  margin: 0;
  color: #333;
  font-size: 1.8rem;
}

/* 覆盖默认的h2样式，确保只影响页面标题 */
h2 {
  margin-bottom: 0;
}

/* 功能标签页 */
.function-tabs {
  margin-bottom: 20px;
}

.tab-content {
  padding: 10px 0;
}

/* 卡片样式 */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}

/* 计划内容 */
.plan-content {
  padding: 16px 0;
}

/* 筛选栏 */
.filter-bar {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
}

/* 分页容器 */
.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

/* 资源类型切换 */
.resource-type-switch {
  margin-bottom: 16px;
  border-bottom: 1px solid #e4e7ed;
}

/* 资源列表 */
.resource-list {
  margin-top: 16px;
}

/* 任务池 */
.task-pool {
  margin-bottom: 20px;
}

.task-pool h3,
.dispatch-result h3,
.cost-bill-list h3 {
  margin-bottom: 16px;
  font-size: 1.2rem;
}

/* 任务操作按钮 */
.task-action-buttons {
  margin-top: 16px;
  display: flex;
  gap: 10px;
}

/* 调度内容 */
.dispatch-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

@media (max-width: 1200px) {
  .dispatch-content {
    grid-template-columns: 1fr;
  }
}

/* 成本统计卡片 */
.cost-stats {
  margin-bottom: 20px;
}

.stat-card {
  text-align: center;
}

/* 成本内容 */
.cost-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .transport-management-view {
    padding: 16px;
  }
  
  h2 {
    font-size: 1.5rem;
  }
}

@media (max-width: 768px) {
  .transport-management-view {
    padding: 12px;
  }
  
  h2 {
    font-size: 1.3rem;
    margin-bottom: 16px;
  }
  
  .filter-bar {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .filter-bar .el-input,
  .filter-bar .el-select,
  .filter-bar .el-date-picker {
    width: 100% !important;
  }
}
</style>
