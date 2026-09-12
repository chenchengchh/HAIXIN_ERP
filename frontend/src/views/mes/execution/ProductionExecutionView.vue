<template>
  <div class="mes-execution-container">
    <!-- 页面头部和面包屑导航 -->
    <div class="page-header">
      <h2>生产执行管理</h2>
      <el-breadcrumb>
        <el-breadcrumb-item>
          <router-link to="/home/mes">首页</router-link>
        </el-breadcrumb-item>
        <el-breadcrumb-item>
          <router-link to="/home/mes">MES系统</router-link>
        </el-breadcrumb-item>
        <el-breadcrumb-item>
          <router-link to="/home/mes/execution">生产执行</router-link>
        </el-breadcrumb-item>
        <el-breadcrumb-item :to="`/home/mes/execution/${activeTab}`">
          {{ tabLabelMap[activeTab] }}
        </el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <!-- AI 故障预测建议（S01，设备时序漂移预警，空态自动隐藏） -->
    <ModuleAiSuggestionCard
      suggestion-type="FAULT_PREDICTION"
      title="AI 设备故障预测"
      :max-display="5"
    />

    <!-- 模块导航 -->
    <div class="module-nav">
      <el-tabs v-model="activeTab" type="card">
        <el-tab-pane label="生产执行概览" name="execution-overview">
          <el-card shadow="hover" class="content-card">
            <div class="card-content">
              <div class="stats-grid">
                <div class="stat-item">
                  <div class="stat-value">{{ pendingOrders.length }}</div>
                  <div class="stat-label">待处理订单</div>
                </div>
                <div class="stat-item">
                  <div class="stat-value">{{ inProcessWorkOrders.length }}</div>
                  <div class="stat-label">处理中工单</div>
                </div>
                <div class="stat-item">
                  <div class="stat-value">{{ assignedProcesses.length }}</div>
                  <div class="stat-label">已分配工序</div>
                </div>
              </div>
            </div>
          </el-card>
          
          <!-- 订单完成情况图表 -->
          <el-card shadow="hover" class="content-card">
            <div class="card-content">
              <div class="chart-container">
                <div id="orderChart" class="echarts-chart"></div>
              </div>
            </div>
          </el-card>
          
          <!-- 工单执行状态图表 -->
          <el-card shadow="hover" class="content-card">
            <div class="card-content">
              <div class="chart-container">
                <div id="workOrderChart" class="echarts-chart"></div>
              </div>
            </div>
          </el-card>
        </el-tab-pane>
        
        <el-tab-pane label="生产订单管理" name="order-management">
          <el-card shadow="hover" class="content-card">
            <div class="card-header">
              <h3>生产订单管理</h3>
              <el-button type="primary" @click="showOrderDialog = true">
                <el-icon><Plus /></el-icon> 接收ERP订单
              </el-button>
            </div>
            <div class="card-content">
              <el-table v-loading="loading.orders" :data="orders" style="width: 100%">
                <el-table-column prop="orderNo" label="订单号" min-width="120" />
                <el-table-column prop="erpOrderNo" label="ERP订单号" min-width="120" />
                <el-table-column prop="materialName" label="产品名称" min-width="150" />
                <el-table-column prop="qty" label="数量" width="80" />
                <el-table-column prop="priority" label="优先级" width="80" />
                <el-table-column prop="planStartTime" label="计划开始时间" width="150" />
                <el-table-column prop="planEndTime" label="计划结束时间" width="150" />
                <el-table-column prop="status" label="状态" width="100">
                  <template #default="scope">
                    <el-tag
                      :type="scope.row.status === 'pending' ? 'info' : 
                             scope.row.status === 'processing' ? 'warning' : 
                             scope.row.status === 'completed' ? 'success' : 'danger'">
                      {{ statusMap[scope.row.status] }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="200" fixed="right">
                  <template #default="scope">
                    <el-button type="primary" size="small" @click="createWorkOrder(scope.row)">
                      创建工单
                    </el-button>
                    <el-button type="info" size="small" @click="viewOrderDetail(scope.row)">
                      详情
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </el-card>
        </el-tab-pane>
        
        <el-tab-pane label="工单管理" name="work-order-management">
          <el-card shadow="hover" class="content-card">
            <div class="card-header">
              <h3>工单管理</h3>
            </div>
            <div class="card-content">
              <el-table v-loading="loading.workOrders" :data="workOrders" style="width: 100%">
                <el-table-column prop="workOrderNo" label="工单号" min-width="120" />
                <el-table-column prop="erpProductionNo" label="ERP生产单号" min-width="140">
                  <template #default="scope">
                    <span v-if="scope.row.erpProductionNo" class="erp-prod-no">
                      {{ scope.row.erpProductionNo }}
                    </span>
                    <span v-else class="text-muted">-</span>
                  </template>
                </el-table-column>
                <el-table-column prop="orderNo" label="关联订单号" min-width="120" />
                <el-table-column prop="materialName" label="产品名称" min-width="150" />
                <el-table-column prop="qty" label="数量" width="80" />
                <el-table-column prop="status" label="状态" width="100">
                  <template #default="scope">
                    <el-tag
                      :type="scope.row.status === 'created' ? 'info' : 
                             scope.row.status === 'released' ? 'warning' : 
                             scope.row.status === 'in_process' ? 'success' : 
                             scope.row.status === 'finished' ? 'success' : 'danger'">
                      {{ workOrderStatusMap[scope.row.status] }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="createTime" label="创建时间" width="150" />
                <el-table-column prop="updateTime" label="更新时间" width="150" />
                <el-table-column label="操作" width="200" fixed="right">
                  <template #default="scope">
                    <el-button 
                      v-if="scope.row.status === 'created'" 
                      type="primary" 
                      size="small" 
                      @click="releaseWorkOrder(scope.row.id)">
                      释放工单
                    </el-button>
                    <el-button 
                      v-if="scope.row.status === 'released'" 
                      type="success" 
                      size="small" 
                      @click="createProcessAssignmentDialog(scope.row)">
                      工序派工
                    </el-button>
                    <el-button type="info" size="small" @click="viewWorkOrderDetail(scope.row)">
                      详情
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </el-card>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- 订单接收对话框 -->
    <el-dialog
      v-model="showOrderDialog"
      title="接收ERP订单"
      width="600px"
      destroy-on-close>
      <el-form :model="newOrder" label-position="top" :rules="orderRules" ref="orderFormRef">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="ERP订单号" prop="erpOrderNo">
              <el-input v-model="newOrder.erpOrderNo" placeholder="请输入ERP订单号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="产品名称" prop="materialName">
              <el-input v-model="newOrder.materialName" placeholder="请输入产品名称" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="数量" prop="qty">
              <el-input-number v-model="newOrder.qty" :min="1" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="优先级" prop="priority">
              <el-select v-model="newOrder.priority" style="width: 100%">
                <el-option label="1 (最高)" :value="1" />
                <el-option label="2" :value="2" />
                <el-option label="3" :value="3" />
                <el-option label="4" :value="4" />
                <el-option label="5 (最低)" :value="5" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="计划开始时间" prop="planStartTime">
              <el-date-picker
                v-model="newOrder.planStartTime"
                type="datetime"
                value-format="yyyy-MM-dd HH:mm:ss"
                style="width: 100%"
                placeholder="选择计划开始时间"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="计划结束时间" prop="planEndTime">
              <el-date-picker
                v-model="newOrder.planEndTime"
                type="datetime"
                value-format="yyyy-MM-dd HH:mm:ss"
                style="width: 100%"
                placeholder="选择计划结束时间"
              />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showOrderDialog = false">取消</el-button>
          <el-button type="primary" @click="submitOrder">确认</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 工单详情对话框 -->
    <el-dialog
      v-model="showWorkOrderDetailDialog"
      title="工单详情"
      width="800px"
      destroy-on-close>
      <div v-if="selectedWorkOrder" class="work-order-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="工单号" :span="1">{{ selectedWorkOrder.workOrderNo }}</el-descriptions-item>
          <el-descriptions-item label="ERP生产单号" :span="1">
            <span v-if="selectedWorkOrder.erpProductionNo" class="erp-prod-no">
              {{ selectedWorkOrder.erpProductionNo }}
            </span>
            <span v-else class="text-muted">-</span>
          </el-descriptions-item>
          <el-descriptions-item label="关联订单号" :span="1">{{ selectedWorkOrder.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="产品名称" :span="1">{{ selectedWorkOrder.materialName }}</el-descriptions-item>
          <el-descriptions-item label="数量" :span="1">{{ selectedWorkOrder.qty }}</el-descriptions-item>
          <el-descriptions-item label="状态" :span="1">
            <el-tag
              :type="selectedWorkOrder.status === 'created' ? 'info' : 
                     selectedWorkOrder.status === 'released' ? 'warning' : 
                     selectedWorkOrder.status === 'in_process' ? 'success' : 
                     selectedWorkOrder.status === 'finished' ? 'success' : 'danger'">
              {{ workOrderStatusMap[selectedWorkOrder.status] }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间" :span="1">{{ selectedWorkOrder.createTime }}</el-descriptions-item>
          <el-descriptions-item label="更新时间" :span="1">{{ selectedWorkOrder.updateTime }}</el-descriptions-item>
          <el-descriptions-item label="计划工时" :span="1">8小时</el-descriptions-item>
          <el-descriptions-item label="实际工时" :span="1">4.5小时</el-descriptions-item>
          <el-descriptions-item label="完成率" :span="1">56.25%</el-descriptions-item>
        </el-descriptions>
        
        <!-- 工序列表 -->
        <div class="process-list">
          <h3 style="margin-top: 20px; margin-bottom: 10px;">工序列表</h3>
          <el-table :data="selectedWorkOrder.processes || []" border style="width: 100%">
            <el-table-column prop="stepName" label="工序名称" width="150" />
            <el-table-column prop="workstationName" label="工站名称" width="150" />
            <el-table-column prop="operatorName" label="操作员" width="120" />
            <el-table-column prop="startTime" label="开始时间" width="180" />
            <el-table-column prop="endTime" label="结束时间" width="180" />
            <el-table-column prop="workingHours" label="工时" width="80" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="scope">
                <el-tag
                  :type="scope.row.status === 'assigned' ? 'info' : 
                         scope.row.status === 'in_progress' ? 'warning' : 'success'">
                  {{ scope.row.status === 'assigned' ? '已分配' : 
                     scope.row.status === 'in_progress' ? '处理中' : '已完成' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120" fixed="right">
              <template #default="scope">
                <el-button
                  v-if="scope.row.status === 'assigned'"
                  type="warning"
                  size="small"
                  @click="startProcess(scope.row)">
                  开始
                </el-button>
                <el-button
                  v-if="scope.row.status === 'in_progress'"
                  type="success"
                  size="small"
                  @click="completeProcess(scope.row)">
                  完成
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showWorkOrderDetailDialog = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 订单详情对话框 -->
    <el-dialog
      v-model="showOrderDetailDialog"
      title="订单详情"
      width="800px"
      destroy-on-close>
      <div v-if="selectedOrder" class="order-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单号" :span="1">{{ selectedOrder.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="ERP订单号" :span="1">{{ selectedOrder.erpOrderNo }}</el-descriptions-item>
          <el-descriptions-item label="产品名称" :span="1">{{ selectedOrder.materialName }}</el-descriptions-item>
          <el-descriptions-item label="数量" :span="1">{{ selectedOrder.qty }}</el-descriptions-item>
          <el-descriptions-item label="优先级" :span="1">{{ selectedOrder.priority }}</el-descriptions-item>
          <el-descriptions-item label="状态" :span="1">
            <el-tag
              :type="selectedOrder.status === 'pending' ? 'info' : 
                     selectedOrder.status === 'processing' ? 'warning' : 
                     selectedOrder.status === 'completed' ? 'success' : 'danger'">
              {{ statusMap[selectedOrder.status] }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="计划开始时间" :span="1">{{ selectedOrder.planStartTime }}</el-descriptions-item>
          <el-descriptions-item label="计划结束时间" :span="1">{{ selectedOrder.planEndTime }}</el-descriptions-item>
          <el-descriptions-item label="创建时间" :span="1">{{ selectedOrder.createTime }}</el-descriptions-item>
          <el-descriptions-item label="更新时间" :span="1">{{ selectedOrder.updateTime }}</el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showOrderDetailDialog = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 工序派工对话框 -->
    <el-dialog
      v-model="showProcessAssignmentDialog"
      title="工序派工"
      width="600px"
      destroy-on-close>
      <el-form :model="processAssignmentForm" label-position="top" :rules="processAssignmentRules" ref="processAssignmentFormRef">
        <el-form-item label="工单编号" disabled>
          <el-input v-model="processAssignmentForm.workOrderNo" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="工序" prop="stepId">
              <el-select v-model="processAssignmentForm.stepId" placeholder="选择工序" style="width: 100%">
                <el-option v-for="step in processSteps" :key="step.id" :label="step.name" :value="step.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="工站" prop="workstationId">
              <el-select v-model="processAssignmentForm.workstationId" placeholder="选择工站" style="width: 100%">
                <el-option v-for="workstation in workstations" :key="workstation.id" :label="workstation.name" :value="workstation.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="操作员" prop="operatorId">
              <el-select v-model="processAssignmentForm.operatorId" placeholder="选择操作员" style="width: 100%">
                <el-option v-for="operator in operators" :key="operator.id" :label="operator.name" :value="operator.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="开始时间" prop="startTime">
              <el-date-picker
                v-model="processAssignmentForm.startTime"
                type="datetime"
                value-format="yyyy-MM-dd HH:mm:ss"
                style="width: 100%"
                placeholder="选择开始时间"
              />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showProcessAssignmentDialog = false">取消</el-button>
          <el-button type="primary" @click="submitProcessAssignment">确认</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { useMesExecutionStore } from '../../../stores/mes/execution'
import { storeToRefs } from 'pinia'
import type { Order, WorkOrder } from '../../../api/mes/execution'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import ModuleAiSuggestionCard from '../../../components/ai/ModuleAiSuggestionCard.vue'

// 状态管理
// 使用storeToRefs保持state/getters响应性，actions直接解构
const store = useMesExecutionStore()
const { 
  orders, 
  workOrders, 
  processAssignments,
  pendingOrders, 
  inProcessWorkOrders, 
  assignedProcesses,
  loading
} = storeToRefs(store)
const {
  fetchOrders,
  fetchWorkOrders,
  fetchProcessAssignments,
  receiveErpOrder,
  createWorkOrder: storeCreateWorkOrder,
  releaseWorkOrder: storeReleaseWorkOrder,
  createProcessAssignment,
  updateProcessAssignmentStatus
} = store

// 状态映射
const statusMap: Record<string, string> = {
  pending: '待处理',
  processing: '处理中',
  completed: '已完成',
  closed: '已关闭'
}

const workOrderStatusMap: Record<string, string> = {
  created: '已创建',
  released: '已释放',
  in_process: '处理中',
  finished: '已完成',
  closed: '已关闭'
}

// 订单对话框
const showOrderDialog = ref(false)
const orderFormRef = ref()
const newOrder = reactive<Partial<Order>>({
  erpOrderNo: '',
  materialName: '',
  qty: 1,
  priority: 5,
  planStartTime: new Date().toISOString(),
  planEndTime: new Date(Date.now() + 7 * 24 * 60 * 60 * 1000).toISOString()
})

const orderRules = {
  erpOrderNo: [{ required: true, message: '请输入ERP订单号', trigger: 'blur' }],
  materialName: [{ required: true, message: '请输入产品名称', trigger: 'blur' }],
  qty: [{ required: true, type: 'number', min: 1, message: '数量必须大于0', trigger: 'blur' }],
  planStartTime: [{ required: true, message: '请选择计划开始时间', trigger: 'change' }],
  planEndTime: [{ required: true, message: '请选择计划结束时间', trigger: 'change' }]
}

// 工单详情对话框
const showWorkOrderDetailDialog = ref(false)
const selectedWorkOrder = ref<WorkOrder & { processes?: any[] } | null>(null)

// 订单详情对话框
const showOrderDetailDialog = ref(false)
const selectedOrder = ref<Order | null>(null)

// 工序派工对话框
const showProcessAssignmentDialog = ref(false)
const processAssignmentFormRef = ref()
const processAssignmentForm = reactive({
  workOrderId: '',
  workOrderNo: '',
  stepId: '',
  workstationId: '',
  operatorId: '',
  startTime: new Date().toISOString()
})

const processAssignmentRules = {
  stepId: [{ required: true, message: '请选择工序', trigger: 'blur' }],
  workstationId: [{ required: true, message: '请选择工站', trigger: 'blur' }],
  operatorId: [{ required: true, message: '请选择操作员', trigger: 'blur' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }]
}

// 模拟数据：工序列表
const processSteps = ref([
  { id: '1', name: '工序1' },
  { id: '2', name: '工序2' },
  { id: '3', name: '工序3' },
  { id: '4', name: '工序4' },
  { id: '5', name: '工序5' }
])

// 模拟数据：工站列表
const workstations = ref([
  { id: '1', name: '工站1' },
  { id: '2', name: '工站2' },
  { id: '3', name: '工站3' }
])

// 模拟数据：操作员列表
const operators = ref([
  { id: '1', name: '操作员1' },
  { id: '2', name: '操作员2' },
  { id: '3', name: '操作员3' }
])

const buildProcessesForWorkOrder = (workOrderNo: string) => {
  // processAssignments 为 storeToRefs 返回的 Ref，需通过 .value 取数组
  return (processAssignments.value || [])
    .filter(p => p.workOrderNo === workOrderNo)
    .map(p => {
      let workingHours = 0
      if (p.startTime && p.endTime) {
        const start = new Date(p.startTime).getTime()
        const end = new Date(p.endTime).getTime()
        if (!Number.isNaN(start) && !Number.isNaN(end) && end >= start) {
          workingHours = Math.round(((end - start) / (1000 * 60 * 60)) * 100) / 100
        }
      }
      return {
        id: p.id,
        stepName: p.stepName,
        workstationName: p.workstationName,
        operatorName: p.operatorName,
        startTime: p.startTime || null,
        endTime: p.endTime || null,
        status: p.status,
        workingHours
      }
    })
}

// 工序开始
const startProcess = async (process: any) => {
  try {
    await updateProcessAssignmentStatus(process.id, 'in_progress')
    if (selectedWorkOrder.value) {
      await fetchProcessAssignments(selectedWorkOrder.value.workOrderNo)
      selectedWorkOrder.value = { ...selectedWorkOrder.value, processes: buildProcessesForWorkOrder(selectedWorkOrder.value.workOrderNo) }
    }
    ElMessage.success('工序开始成功')
  } catch (err) {
    console.error('开始工序失败:', err)
    ElMessage.error('工序开始失败')
  }
}

// 工序完成
const completeProcess = async (process: any) => {
  try {
    await updateProcessAssignmentStatus(process.id, 'completed')
    if (selectedWorkOrder.value) {
      await fetchProcessAssignments(selectedWorkOrder.value.workOrderNo)
      selectedWorkOrder.value = { ...selectedWorkOrder.value, processes: buildProcessesForWorkOrder(selectedWorkOrder.value.workOrderNo) }
    }
    ElMessage.success('工序完成成功')
  } catch (err) {
    console.error('完成工序失败:', err)
    ElMessage.error('工序完成失败')
  }
}

// 活跃标签页
const activeTab = ref('execution-overview')

// 标签页名称映射，用于面包屑导航
const tabLabelMap: Record<string, string> = {
  'execution-overview': '生产执行概览',
  'order-management': '生产订单管理',
  'work-order-management': '工单管理'
}

// 处理中订单数量
const inProcessOrders = computed(() => {
  return orders.value.filter(order => order.status === 'processing')
})

// 已完成工单数量
const completedWorkOrders = computed(() => {
  return workOrders.value.filter(workOrder => workOrder.status === 'finished')
})

// 图表引用
let orderChart: echarts.ECharts | null = null
let workOrderChart: echarts.ECharts | null = null

// 导入ECharts
import * as echarts from 'echarts'

// 图表初始化函数
const initCharts = () => {
  // 订单完成情况图表
  const orderChartDom = document.getElementById('orderChart')
  if (orderChartDom) {
    // 如果已存在图表实例，先销毁
    if (orderChart) {
      orderChart.dispose()
    }
    orderChart = echarts.init(orderChartDom)
    orderChart.setOption({
      title: {
        text: '订单完成情况',
        left: 'center'
      },
      tooltip: {
        trigger: 'item'
      },
      legend: {
        bottom: 10,
        data: ['待处理', '处理中', '已完成', '已关闭']
      },
      series: [
        {
          name: '订单状态',
          type: 'pie',
          radius: ['40%', '70%'],
          avoidLabelOverlap: false,
          itemStyle: {
            borderRadius: 10,
            borderColor: '#fff',
            borderWidth: 2
          },
          label: {
            show: false
          },
          emphasis: {
            label: {
              show: true,
              fontSize: '20',
              fontWeight: 'bold'
            }
          },
          labelLine: {
            show: false
          },
          data: [
            { value: orders.value.filter(o => o.status === 'pending').length, name: '待处理' },
            { value: orders.value.filter(o => o.status === 'processing').length, name: '处理中' },
            { value: orders.value.filter(o => o.status === 'completed').length, name: '已完成' },
            { value: orders.value.filter(o => o.status === 'closed').length, name: '已关闭' }
          ]
        }
      ]
    })
  }

  // 工单执行状态图表
    const workOrderChartDom = document.getElementById('workOrderChart')
    if (workOrderChartDom) {
      // 如果已存在图表实例，先销毁
      if (workOrderChart) {
        workOrderChart.dispose()
      }
      workOrderChart = echarts.init(workOrderChartDom)
      
      // 基于实际工单数据统计各状态数量
      const createdCount = workOrders.value.filter(wo => wo.status === 'created').length
      const releasedCount = workOrders.value.filter(wo => wo.status === 'released').length
      const inProcessCount = workOrders.value.filter(wo => wo.status === 'in_process').length
      const finishedCount = workOrders.value.filter(wo => wo.status === 'finished').length
      const closedCount = workOrders.value.filter(wo => wo.status === 'closed').length
      
      workOrderChart.setOption({
        title: {
          text: '工单执行状态',
          left: 'center'
        },
        tooltip: {
          trigger: 'axis'
        },
        legend: {
          bottom: 10,
          data: ['已创建', '已释放', '处理中', '已完成', '已关闭']
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '15%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          boundaryGap: false,
          data: ['当前']
        },
        yAxis: {
          type: 'value'
        },
        series: [
          { name: '已创建', type: 'bar', data: [createdCount] },
          { name: '已释放', type: 'bar', data: [releasedCount] },
          { name: '处理中', type: 'bar', data: [inProcessCount] },
          { name: '已完成', type: 'bar', data: [finishedCount] },
          { name: '已关闭', type: 'bar', data: [closedCount] }
        ]
      })
    }
}

// 更新图表数据
const updateCharts = () => {
  // 更新订单完成情况图表
  if (orderChart) {
    orderChart.setOption({
      series: [
        {
          data: [
            { value: orders.value.filter(o => o.status === 'pending').length, name: '待处理' },
            { value: orders.value.filter(o => o.status === 'processing').length, name: '处理中' },
            { value: orders.value.filter(o => o.status === 'completed').length, name: '已完成' },
            { value: orders.value.filter(o => o.status === 'closed').length, name: '已关闭' }
          ]
        }
      ]
    })
  }
  
  // 更新工单执行状态图表
  if (workOrderChart) {
    // 基于实际工单数据统计各状态数量
    const createdCount = workOrders.value.filter(wo => wo.status === 'created').length
    const releasedCount = workOrders.value.filter(wo => wo.status === 'released').length
    const inProcessCount = workOrders.value.filter(wo => wo.status === 'in_process').length
    const finishedCount = workOrders.value.filter(wo => wo.status === 'finished').length
    const closedCount = workOrders.value.filter(wo => wo.status === 'closed').length
    
    workOrderChart.setOption({
      series: [
        { name: '已创建', type: 'bar', data: [createdCount] },
        { name: '已释放', type: 'bar', data: [releasedCount] },
        { name: '处理中', type: 'bar', data: [inProcessCount] },
        { name: '已完成', type: 'bar', data: [finishedCount] },
        { name: '已关闭', type: 'bar', data: [closedCount] }
      ]
    })
  }
}

// 页面加载时获取数据
onMounted(() => {
  fetchOrders()
  fetchWorkOrders()
  fetchProcessAssignments()
  
  // 延迟初始化图表，确保DOM已经渲染
  setTimeout(() => {
    initCharts()
  }, 100)
})

// 监听数据变化，更新图表
watch([orders, workOrders], () => {
  updateCharts()
}, { deep: true })

// 监听标签页切换，初始化图表
watch(activeTab, (newTab) => {
  if (newTab === 'execution-overview') {
    // 延迟初始化，确保DOM已经渲染
    setTimeout(() => {
      initCharts()
    }, 100)
  }
})

// 提交订单
const submitOrder = async () => {
  if (orderFormRef.value) {
    await orderFormRef.value.validate(async (valid: boolean) => {
      if (valid) {
        try {
          await receiveErpOrder(newOrder)
          showOrderDialog.value = false
          // 重置表单
          Object.assign(newOrder, {
            erpOrderNo: '',
            materialName: '',
            qty: 1,
            priority: 5,
            planStartTime: new Date().toISOString(),
            planEndTime: new Date(Date.now() + 7 * 24 * 60 * 60 * 1000).toISOString()
          })
          if (orderFormRef.value) {
            orderFormRef.value.resetFields()
          }
          // 刷新数据
          fetchOrders()
          // 显示成功消息
          ElMessage.success('ERP订单接收成功')
        } catch (err) {
          console.error('接收ERP订单失败:', err)
          ElMessage.error('接收ERP订单失败，请重试')
        }
      }
    })
  }
}

// 创建工单
const createWorkOrder = async (order: Order) => {
  try {
    await storeCreateWorkOrder({ orderId: order.id, orderNo: order.orderNo, materialId: order.materialId, materialName: order.materialName, qty: order.qty })
    // 刷新数据
    fetchWorkOrders()
    // 显示成功消息
    ElMessage.success('工单创建成功')
  } catch (err) {
    console.error('创建工单失败:', err)
    ElMessage.error('工单创建失败，请重试')
  }
}

// 释放工单
const releaseWorkOrder = async (id: string) => {
  try {
    await storeReleaseWorkOrder(id)
    // 刷新数据
    fetchWorkOrders()
    // 显示成功消息
    ElMessage.success('工单释放成功')
  } catch (err) {
    console.error('释放工单失败:', err)
    ElMessage.error('工单释放失败，请重试')
  }
}

// 查看订单详情
const viewOrderDetail = async (order: Order) => {
  try {
    // 调用API获取订单详情
    const response = await store.fetchOrderDetail(order.id)
    selectedOrder.value = order
    showOrderDetailDialog.value = true
  } catch (err) {
    console.error('获取订单详情失败:', err)
    ElMessage.error('获取订单详情失败')
  }
}

// 查看工单详情
const viewWorkOrderDetail = async (workOrder: WorkOrder) => {
  try {
    // 调用API获取工单详情
    await store.fetchWorkOrderDetail(workOrder.id)
    await fetchProcessAssignments(workOrder.workOrderNo)
    const processes = buildProcessesForWorkOrder(workOrder.workOrderNo)
    selectedWorkOrder.value = { ...workOrder, processes }
    showWorkOrderDetailDialog.value = true
  } catch (err) {
    console.error('获取工单详情失败:', err)
    ElMessage.error('获取工单详情失败')
  }
}

// 打开工序派工对话框
const createProcessAssignmentDialog = (workOrder: WorkOrder) => {
  // 设置工单信息
  processAssignmentForm.workOrderId = workOrder.id
  processAssignmentForm.workOrderNo = workOrder.workOrderNo
  // 打开对话框
  showProcessAssignmentDialog.value = true
}

// 提交工序派工
const submitProcessAssignment = async () => {
  if (processAssignmentFormRef.value) {
    await processAssignmentFormRef.value.validate(async (valid: boolean) => {
      if (valid) {
        try {
          const step = processSteps.value.find(p => p.id === processAssignmentForm.stepId)
          const workstation = workstations.value.find(w => w.id === processAssignmentForm.workstationId)
          const operator = operators.value.find(o => o.id === processAssignmentForm.operatorId)
          const startTime = typeof processAssignmentForm.startTime === 'string'
            ? processAssignmentForm.startTime
            : new Date(processAssignmentForm.startTime).toISOString()

          await createProcessAssignment({
            workOrderId: processAssignmentForm.workOrderId,
            workOrderNo: processAssignmentForm.workOrderNo,
            stepId: processAssignmentForm.stepId,
            stepName: step?.name || processAssignmentForm.stepId,
            workstationId: processAssignmentForm.workstationId,
            workstationName: workstation?.name || processAssignmentForm.workstationId,
            operatorId: processAssignmentForm.operatorId,
            operatorName: operator?.name || processAssignmentForm.operatorId,
            startTime
          })

          await fetchProcessAssignments(processAssignmentForm.workOrderNo)
          // 关闭对话框
          showProcessAssignmentDialog.value = false
          // 显示成功消息
          ElMessage.success('工序派工成功')
        } catch (err) {
          console.error('工序派工失败:', err)
          ElMessage.error('工序派工失败，请重试')
        }
      }
    })
  }
}
</script>

<style scoped>
.mes-execution-container {
  padding: var(--page-padding);
  height: 100%;
  box-sizing: border-box;
  background-color: var(--bg-color-primary);
}

/* ERP 生产单号高亮（B4 闭环可视化） */
.erp-prod-no {
  color: #409eff;
  font-weight: 600;
  font-family: 'Courier New', Courier, monospace;
}
.text-muted {
  color: var(--text-color-placeholder, #c0c4cc);
}

/* 页面头部样式 */
.page-header {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  margin-bottom: var(--spacing-large);
  gap: var(--spacing-small);
}

.page-header h2 {
  font-size: var(--font-size-h2);
  font-weight: 600;
  color: var(--text-color-primary);
  margin: 0;
}

/* 模块导航样式 */
.module-nav {
  background-color: var(--bg-color-secondary);
  border-radius: var(--border-radius);
  box-shadow: var(--box-shadow);
  min-height: calc(100% - 120px);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-medium);
}

.card-content {
  padding: 0;
}

/* 统计数据样式 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: var(--spacing-large);
  margin-bottom: var(--spacing-large);
  padding: var(--spacing-large);
}

.stat-item {
  text-align: center;
  padding: var(--spacing-large);
  background-color: var(--bg-color-primary);
  border-radius: var(--border-radius);
  box-shadow: var(--box-shadow);
}

.stat-value {
  font-size: var(--font-size-h1);
  font-weight: bold;
  color: var(--primary-color);
  margin-bottom: var(--spacing-small);
}

.stat-label {
  font-size: var(--font-size-body);
  color: var(--text-color-secondary);
}

/* 卡片样式 */
.content-card {
  margin: var(--spacing-large);
}

/* 图表容器样式 */
.chart-container {
  height: 300px;
  width: 100%;
  padding: var(--spacing-large);
  box-sizing: border-box;
}

.echarts-chart {
  width: 100%;
  height: 100%;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .mes-execution-container {
    padding: var(--page-padding-mobile);
  }
  
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--spacing-small);
  }
  
  .module-nav {
    min-height: auto;
  }
  
  .stats-grid {
    grid-template-columns: 1fr;
    padding: var(--spacing-medium);
  }
  
  .content-card {
    margin: var(--spacing-medium);
  }
}
</style>
