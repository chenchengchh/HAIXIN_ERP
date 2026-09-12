<template>
  <div class="eam-submodule-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2>维护管理</h2>
      <el-breadcrumb>
        <el-breadcrumb-item>
          <router-link to="/home">首页</router-link>
        </el-breadcrumb-item>
        <el-breadcrumb-item>
          <router-link to="/home/eam">EAM系统</router-link>
        </el-breadcrumb-item>
        <el-breadcrumb-item>
          <router-link to="/home/eam/maintenance-management">维护管理</router-link>
        </el-breadcrumb-item>
        <el-breadcrumb-item :to="`/home/eam/maintenance-management#${activeTab}`">
          {{ tabLabelMap[activeTab] }}
        </el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <!-- 维护计划对话框 -->
    <el-dialog
      v-model="maintenancePlanDialogVisible"
      :title="isEditPlan ? '编辑维护计划' : '添加维护计划'"
      width="500px"
      destroy-on-close
    >
      <el-form
        :model="maintenancePlanForm"
        label-position="right"
        label-width="80px"
      >
        <el-form-item label="设备名称" required>
          <el-input v-model="maintenancePlanForm.equipmentName" placeholder="请输入设备名称" />
        </el-form-item>
        <el-form-item label="维护类型" required>
          <el-select v-model="maintenancePlanForm.maintenanceType" placeholder="请选择维护类型">
            <el-option label="定期保养" value="preventive" />
            <el-option label="润滑" value="lubrication" />
            <el-option label="检查" value="check" />
            <el-option label="更换部件" value="replace" />
          </el-select>
        </el-form-item>
        <el-form-item label="维护频率" required>
          <el-input v-model="maintenancePlanForm.frequency" placeholder="请输入维护频率，如：每月1次" />
        </el-form-item>
        <el-form-item label="下次执行时间" required>
          <el-date-picker
            v-model="maintenancePlanForm.nextExecution"
            type="date"
            placeholder="选择日期"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch
            v-model="maintenancePlanForm.status"
            active-value="active"
            inactive-value="inactive"
            active-text="执行中"
            inactive-text="已停用"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleCloseMaintenancePlanDialog">取消</el-button>
          <el-button type="primary" @click="handleSaveMaintenancePlan">保存</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 紧急报修对话框 -->
    <el-dialog
      v-model="emergencyRepairDialogVisible"
      title="紧急报修"
      width="500px"
      destroy-on-close
    >
      <el-form
        :model="emergencyRepairForm"
        label-position="right"
        label-width="80px"
      >
        <el-form-item label="设备名称" required>
          <el-input v-model="emergencyRepairForm.equipmentName" placeholder="请输入设备名称" />
        </el-form-item>
        <el-form-item label="故障类型" required>
          <el-input v-model="emergencyRepairForm.faultType" placeholder="请输入故障类型" />
        </el-form-item>
        <el-form-item label="故障描述" required>
          <el-input
            v-model="emergencyRepairForm.description"
            type="textarea"
            :rows="3"
            placeholder="请详细描述故障情况"
          />
        </el-form-item>
        <el-form-item label="报修人" required>
          <el-input v-model="emergencyRepairForm.reporter" placeholder="请输入报修人姓名" />
        </el-form-item>
        <el-form-item label="联系方式">
          <el-input v-model="emergencyRepairForm.contact" placeholder="请输入联系方式" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleCloseEmergencyRepairDialog">取消</el-button>
          <el-button type="primary" @click="handleSaveEmergencyRepair">提交报修</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 创建工单对话框 -->
    <el-dialog
      v-model="workorderDialogVisible"
      :title="isEditWorkorderDialog ? '编辑工单' : '创建工单'"
      width="500px"
      destroy-on-close
    >
      <el-form
        :model="workorderForm"
        label-position="right"
        label-width="80px"
      >
        <el-form-item label="工单类型" required>
          <el-select v-model="workorderForm.workorderType" placeholder="请选择工单类型">
            <el-option label="预防性维护" value="preventive" />
            <el-option label="故障维修" value="repair" />
            <el-option label="紧急维修" value="emergency" />
          </el-select>
        </el-form-item>
        <el-form-item label="设备名称" required>
          <el-input v-model="workorderForm.equipmentName" placeholder="请输入设备名称" />
        </el-form-item>
        <el-form-item label="优先级" required>
          <el-select v-model="workorderForm.priority" placeholder="请选择优先级">
            <el-option label="高" value="high" />
            <el-option label="中" value="medium" />
            <el-option label="低" value="low" />
          </el-select>
        </el-form-item>
        <el-form-item label="指派给">
          <el-input v-model="workorderForm.assignee" placeholder="请输入指派人员" />
        </el-form-item>
        <el-form-item label="工单描述">
          <el-input
            v-model="workorderForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入工单描述"
          />
        </el-form-item>
        <el-form-item label="状态" v-if="isEditWorkorderDialog">
          <el-select v-model="workorderForm.status" placeholder="请选择状态">
            <el-option label="待处理" value="pending" />
            <el-option label="处理中" value="processing" />
            <el-option label="已完成" value="completed" />
            <el-option label="已取消" value="cancelled" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleCloseWorkorderDialog">取消</el-button>
          <el-button type="primary" @click="handleSaveWorkorder">保存</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 批量指派对话框 -->
    <el-dialog
      v-model="batchAssignDialogVisible"
      title="批量指派工单"
      width="400px"
      destroy-on-close
    >
      <el-form
        :model="batchAssignForm"
        label-position="right"
        label-width="80px"
      >
        <el-form-item label="指派给" required>
          <el-input v-model="batchAssignForm.assignee" placeholder="请输入指派人员姓名" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="batchAssignForm.remark"
            type="textarea"
            :rows="2"
            placeholder="请输入备注信息"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleCloseBatchAssignDialog">取消</el-button>
          <el-button type="primary" @click="handleSaveBatchAssign">确认指派</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 工单详情对话框 -->
    <el-dialog
      v-model="workorderDetailDialogVisible"
      title="工单详情"
      width="500px"
      destroy-on-close
    >
      <el-descriptions :column="1" border>
        <el-descriptions-item label="工单ID">{{ selectedWorkorderDetail.id }}</el-descriptions-item>
        <el-descriptions-item label="工单类型">{{ workorderTypeText[selectedWorkorderDetail.workorderType] ?? selectedWorkorderDetail.workorderType }}</el-descriptions-item>
        <el-descriptions-item label="设备名称">{{ selectedWorkorderDetail.equipmentName }}</el-descriptions-item>
        <el-descriptions-item label="优先级">
          <el-tag :type="selectedWorkorderDetail.priority === 'high' ? 'danger' : selectedWorkorderDetail.priority === 'medium' ? 'warning' : 'info'">
            {{ selectedWorkorderDetail.priority === 'high' ? '高' : selectedWorkorderDetail.priority === 'medium' ? '中' : '低' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="指派给">{{ selectedWorkorderDetail.assignee || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="selectedWorkorderDetail.status === 'completed' ? 'success' : selectedWorkorderDetail.status === 'cancelled' ? 'info' : 'warning'">
            {{ selectedWorkorderDetail.status === 'pending' ? '待处理' : selectedWorkorderDetail.status === 'processing' ? '处理中' : selectedWorkorderDetail.status === 'completed' ? '已完成' : '已取消' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="工单描述">{{ selectedWorkorderDetail.description || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleCloseWorkorderDetailDialog">关闭</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 维修 Copilot 建议对话框 -->
    <el-dialog
      v-model="copilotDialogVisible"
      :title="`维修 Copilot 建议 - ${copilotTitle}`"
      width="680px"
      destroy-on-close
    >
      <div v-if="copilotAdvices.length > 0" class="copilot-advice-list">
        <el-card
          v-for="(advice, index) in copilotAdvices"
          :key="index"
          class="copilot-advice-card"
          shadow="hover"
        >
          <div class="copilot-advice-header">
            <el-tag type="success" effect="dark">相似度 {{ Math.round((advice.score ?? 0) * 100) }}%</el-tag>
            <el-tag type="primary">{{ advice.faultType || '未知类型' }}</el-tag>
          </div>
          <div class="copilot-advice-body">
            <p><span class="copilot-label">故障现象：</span>{{ advice.symptom || '-' }}</p>
            <p><span class="copilot-label">解决方案：</span><strong>{{ advice.solution || '-' }}</strong></p>
            <p v-if="advice.workOrderRef || advice.resolvedTime">
              <span class="copilot-label">关联工单：</span>{{ advice.workOrderRef || '-' }}
              <span v-if="advice.resolvedTime" class="copilot-resolved-time">（解决时间：{{ advice.resolvedTime }}）</span>
            </p>
            <div v-if="advice.documents && advice.documents.length > 0" class="copilot-documents">
              <span class="copilot-label">关联文档：</span>
              <el-tag
                v-for="doc in advice.documents"
                :key="doc.id"
                size="small"
                class="copilot-doc-tag"
              >
                {{ doc.name }}<template v-if="doc.category">（{{ doc.category }}）</template>
              </el-tag>
            </div>
          </div>
        </el-card>
      </div>
      <el-empty v-else description="暂无相似故障历史方案" />
      <div v-if="copilotSources.length > 0" class="copilot-evidence">
        证据来源：{{ copilotSources.join('、') }}
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="copilotDialogVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 标签页导航区域 -->
    <el-card class="submodule-tabs-card">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange" type="border-card">
        <el-tab-pane label="预防性维护" name="preventive-maintenance">
          <div class="tab-content">
            <div class="sub-card-header">
              <h3>预防性维护</h3>
              <el-button type="primary" size="small" @click="handleAddMaintenancePlan">添加维护计划</el-button>
            </div>
            <div class="preventive-maintenance-content">
              <el-table :data="pmPlans" style="width: 100%" height="400">
                <el-table-column prop="id" label="计划ID" width="100" />
                <el-table-column prop="equipmentName" label="设备名称" width="180" />
                <el-table-column prop="maintenanceType" label="维护类型" width="120">
                  <template #default="scope">
                    {{ planTypeText[scope.row.maintenanceType] ?? scope.row.maintenanceType }}
                  </template>
                </el-table-column>
                <el-table-column prop="frequency" label="维护频率" width="150" />
                <el-table-column prop="nextExecution" label="下次执行时间" width="180" />
                <el-table-column prop="status" label="状态" width="120">
                  <template #default="scope">
                    <el-tag :type="scope.row.status === 'active' ? 'success' : 'info'">
                      {{ scope.row.status === 'active' ? '执行中' : '已停用' }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="150">
                  <template #default="scope">
                    <el-button size="small" type="primary" @click="handleExecutePM(scope.row)">执行</el-button>
                    <el-button size="small" @click="handleEditPM(scope.row)">编辑</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="故障维修" name="fault-repair">
          <div class="tab-content">
            <div class="sub-card-header">
              <h3>故障维修</h3>
              <el-button type="danger" size="small" @click="handleEmergencyRepair">紧急报修</el-button>
              <el-button type="primary" size="small" @click="handleBatchProcess">批量处理</el-button>
            </div>
            <div class="fault-repair-content">
              <el-table :data="faults" style="width: 100%" height="400" @selection-change="handleFaultSelectionChange">
                <el-table-column type="selection" width="55" />
                <el-table-column prop="id" label="故障ID" width="100" />
                <el-table-column prop="equipmentName" label="设备名称" width="180" />
                <el-table-column prop="faultType" label="故障类型" width="120" />
                <el-table-column prop="reportTime" label="报修时间" width="180" />
                <el-table-column prop="status" label="状态" width="120">
                  <template #default="scope">
                    <el-tag :type="getStatusType(scope.row.status)">
                      {{ getStatusText(scope.row.status) }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="reporter" label="报修人" width="120" />
                <el-table-column label="操作" width="290">
                  <template #default="scope">
                    <el-button size="small" type="primary" @click="handleProcessFault(scope.row)">处理</el-button>
                    <el-button size="small" @click="handleViewFault(scope.row)">查看</el-button>
                    <el-button size="small" type="danger" @click="handleCloseFault(scope.row)">关闭</el-button>
                    <el-button size="small" type="success" :loading="copilotLoadingId === scope.row.id" @click="handleCopilotAdvice(scope.row)">Copilot</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="维护工单" name="maintenance-workorder">
          <div class="tab-content">
            <div class="sub-card-header">
              <h3>维护工单</h3>
              <el-button type="primary" size="small" @click="handleCreateWorkorder">创建工单</el-button>
              <el-button size="small" @click="handleBatchAssign">批量指派</el-button>
              <el-button size="small" @click="handleExportWorkorder">导出工单</el-button>
            </div>
            <div class="workorder-content">
              <el-table :data="workorders" style="width: 100%" height="400" @selection-change="handleWorkorderSelectionChange">
                <el-table-column type="selection" width="55" />
                <el-table-column prop="id" label="工单ID" width="100" />
                <el-table-column prop="workorderType" label="工单类型" width="120">
                  <template #default="scope">
                    {{ workorderTypeText[scope.row.workorderType] ?? scope.row.workorderType }}
                  </template>
                </el-table-column>
                <el-table-column prop="equipmentName" label="设备名称" width="180" />
                <el-table-column prop="priority" label="优先级" width="100">
                  <template #default="scope">
                    <el-tag :type="scope.row.priority === 'high' ? 'danger' : scope.row.priority === 'medium' ? 'warning' : 'info'">
                      {{ scope.row.priority === 'high' ? '高' : scope.row.priority === 'medium' ? '中' : '低' }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="status" label="状态" width="120">
                  <template #default="scope">
                    <el-tag :type="scope.row.status === 'completed' ? 'success' : scope.row.status === 'processing' ? 'warning' : scope.row.status === 'cancelled' ? 'info' : 'danger'">
                      {{ { pending: '待处理', processing: '处理中', completed: '已完成', cancelled: '已取消' }[scope.row.status as string] || scope.row.status }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="assignee" label="指派给" width="120" />
                <el-table-column label="操作" width="220">
                  <template #default="scope">
                    <el-button size="small" type="primary" @click="handleViewWorkorder(scope.row)">查看</el-button>
                    <el-button size="small" @click="handleEditWorkorder(scope.row)">编辑</el-button>
                    <el-button size="small" type="success" @click="handleCompleteWorkorder(scope.row)">完成</el-button>
                    <el-button size="small" type="danger" @click="handleCancelWorkorder(scope.row)">取消</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="维护记录" name="maintenance-record">
          <div class="tab-content">
            <div class="sub-card-header">
              <h3>维护记录</h3>
            </div>
            <div class="maintenance-record-content">
              <el-table :data="maintenanceRecords" style="width: 100%" height="400">
                <el-table-column prop="id" label="记录ID" width="100" />
                <el-table-column prop="equipmentName" label="设备名称" width="180" />
                <el-table-column prop="maintenanceType" label="维护类型" width="120" />
                <el-table-column prop="maintenanceContent" label="维护内容" width="200" />
                <el-table-column prop="maintenanceTime" label="维护时间" width="180" />
                <el-table-column prop="maintainer" label="维护人员" width="120" />
                <el-table-column prop="cost" label="维护成本" width="120" />
                <el-table-column label="操作" width="120">
                  <template #default="scope">
                    <el-button size="small" type="primary" @click="handleViewRecord(scope.row)">查看详情</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getMaintenancePlans, createMaintenancePlan, updateMaintenancePlan, getWorkOrders, createWorkOrder, updateWorkOrder, getFaults, createFault, updateFault, getMaintenanceRecords, executeMaintenancePlan, getFaultAdvice } from '@/api/eam'
import { unwrapListResponse, unwrapResponseData } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'

// 路由实例
const router = useRouter()
const route = useRoute()

// 激活的标签页
const activeTab = ref('preventive-maintenance')

// 标签页名称映射
const tabLabelMap: Record<string, string> = {
  'preventive-maintenance': '预防性维护',
  'fault-repair': '故障维修',
  'maintenance-workorder': '维护工单',
  'maintenance-record': '维护记录'
}

// 维护计划对话框相关
const maintenancePlanDialogVisible = ref(false)
const isEditPlan = ref(false)
const maintenancePlanForm = ref({
  id: null,
  equipmentName: '',
  maintenanceType: '',
  frequency: '',
  nextExecution: '',
  status: 'active'
})

// 紧急报修对话框相关
const emergencyRepairDialogVisible = ref(false)
const emergencyRepairForm = ref({
  equipmentName: '',
  faultType: '',
  description: '',
  reporter: '',
  contact: ''
})

// 故障选中列表
const selectedFaults = ref<any[]>([])

// 工单选中列表
const selectedWorkorders = ref<any[]>([])

// 工单对话框相关
const workorderDialogVisible = ref(false)
const isEditWorkorderDialog = ref(false)
const workorderForm = ref({
  id: null,
  workorderType: '',
  equipmentName: '',
  priority: 'medium',
  assignee: '',
  description: '',
  status: 'pending'
})

// 批量指派对话框相关
const batchAssignDialogVisible = ref(false)
const batchAssignForm = ref({
  assignee: '',
  remark: ''
})

// 工单详情对话框相关
const workorderDetailDialogVisible = ref(false)
const selectedWorkorderDetail = ref({
  id: '',
  workorderType: '',
  equipmentName: '',
  priority: '',
  status: '',
  assignee: '',
  description: ''
})

// 维修 Copilot 建议对话框相关
const copilotDialogVisible = ref(false)
const copilotLoadingId = ref<number | string | null>(null)
const copilotTitle = ref('')
const copilotAdvices = ref<any[]>([])
const copilotSources = ref<string[]>([])

// 维护计划
const pmPlans = ref<any[]>([])

// 维护类型中文映射（后端枚举→中文显示）
const planTypeText: Record<string, string> = {
  preventive: '定期保养',
  lubrication: '润滑',
  check: '检查',
  replace: '更换部件'
}

// 工单类型中文映射（后端枚举→中文显示）
const workorderTypeText: Record<string, string> = {
  preventive: '预防性维护',
  repair: '故障维修',
  emergency: '紧急维修'
}

// 故障维修
const faults = ref<any[]>([])

// 维护工单
const workorders = ref<any[]>([])

// 维护记录
const maintenanceRecords = ref<any[]>([])

// 获取维护计划（后端type字段映射为前端maintenanceType）
const fetchPlans = async () => {
  try {
    const res = await getMaintenancePlans()
    pmPlans.value = unwrapListResponse<any>(res).map((p: any) => ({ ...p, maintenanceType: p.type }))
  } catch (error) {
    console.error('获取维护计划失败:', error)
    ElMessage.error('获取维护计划失败')
  }
}

// 获取工单列表（后端type字段映射为前端workorderType）
const fetchWorkOrders = async () => {
  try {
    const res = await getWorkOrders()
    workorders.value = unwrapListResponse<any>(res).map((w: any) => ({ ...w, workorderType: w.type }))
  } catch (error) {
    console.error('获取工单列表失败:', error)
    ElMessage.error('获取工单列表失败')
  }
}

// 获取故障列表（后端type字段映射为前端faultType）
const fetchFaults = async () => {
  try {
    const res = await getFaults()
    faults.value = unwrapListResponse<any>(res).map((f: any) => ({ ...f, faultType: f.type }))
  } catch (error) {
    console.error('获取故障列表失败:', error)
    ElMessage.error('获取故障列表失败')
  }
}

// 获取维护记录（后端type/content字段映射为前端maintenanceType/maintenanceContent）
const fetchMaintenanceRecords = async () => {
  try {
    const res = await getMaintenanceRecords()
    maintenanceRecords.value = unwrapListResponse<any>(res).map((r: any) => ({
      ...r,
      maintenanceType: r.type,
      maintenanceContent: r.content
    }))
  } catch (error) {
    console.error('获取维护记录失败:', error)
    ElMessage.error('获取维护记录失败')
  }
}

// 从路由获取标签页状态
const getActiveTabFromRoute = () => {
  const tabMap: Record<string, string> = {
    'preventive-maintenance': 'preventive-maintenance',
    'fault-repair': 'fault-repair',
    'maintenance-workorder': 'maintenance-workorder',
    'maintenance-record': 'maintenance-record'
  }
  const tabName = route.params.tab || 'preventive-maintenance'
  return tabMap[tabName as string] || 'preventive-maintenance'
}

// 组件挂载时，从路由获取标签页状态
onMounted(() => {
  activeTab.value = getActiveTabFromRoute()
  fetchPlans()
  fetchWorkOrders()
  fetchFaults()
  fetchMaintenanceRecords()
})

// 标签页切换事件
const handleTabChange = (tabName: string) => {
  console.log('切换标签页:', tabName)
  // 更新路由，保持标签页状态
  router.push({
    path: `/home/eam/maintenance-management/${tabName}`
  })
}

// 方法
const handleExecutePM = async (plan: any) => {
  /**
   * 处理执行预防性维护
   * @param plan 维护计划信息
   */
  try {
    await executeMaintenancePlan(plan.id)
    ElMessage.success('执行成功，已生成工单')
    fetchWorkOrders()
  } catch (error) {
    console.error('执行失败:', error)
    ElMessage.error('执行失败')
  }
}

const handleEditPM = (plan: any) => {
  /**
   * 处理编辑维护计划
   * @param plan 要编辑的维护计划信息
   */
  console.log('编辑维护计划:', plan)
  isEditPlan.value = true
  maintenancePlanForm.value = { ...plan }
  maintenancePlanDialogVisible.value = true
}

const getStatusType = (status: string) => {
  /**
   * 获取故障状态对应的标签类型
   * @param status 故障状态
   * @returns 标签类型
   */
  switch (status) {
    case 'reported': return 'danger'
    case 'processing': return 'warning'
    case 'completed': return 'success'
    default: return 'info'
  }
}

const getStatusText = (status: string) => {
  /**
   * 获取故障状态对应的文本
   * @param status 故障状态
   * @returns 状态文本
   */
  switch (status) {
    case 'reported': return '已报修'
    case 'processing': return '处理中'
    case 'completed': return '已完成'
    default: return '未知'
  }
}

const handleProcessFault = async (fault: any) => {
  /**
   * 处理故障
   * @param fault 故障信息
   */
  try {
    fault.status = 'processing'
    await updateFault(fault.id, fault)
    ElMessage.success('状态更新成功')
    fetchFaults()
  } catch (error) {
    console.error('处理故障失败:', error)
    ElMessage.error('处理故障失败')
  }
}

const handleViewWorkorder = (workorder: any) => {
  /**
   * 查看工单
   * @param workorder 工单信息
   */
  console.log('查看工单:', workorder)
  selectedWorkorderDetail.value = { ...workorder }
  workorderDetailDialogVisible.value = true
}

const handleCloseWorkorderDetailDialog = () => {
  /**
   * 处理关闭工单详情对话框
   */
  workorderDetailDialogVisible.value = false
}

const handleViewRecord = (record: any) => {
  /**
   * 查看维护记录：弹出详情对话框
   * @param record 维护记录信息
   */
  ElMessageBox.alert(
    `<div style="line-height:1.8">
      <b>记录ID：</b>${record.id}<br/>
      <b>设备名称：</b>${record.equipmentName || '-'}<br/>
      <b>维护类型：</b>${record.maintenanceType || '-'}<br/>
      <b>维护内容：</b>${record.maintenanceContent || '-'}<br/>
      <b>维护时间：</b>${record.maintenanceTime || '-'}<br/>
      <b>维护人员：</b>${record.maintainer || '-'}<br/>
      <b>维护成本：</b>${record.cost != null ? `¥${record.cost}` : '-'}
    </div>`,
    '维护记录详情',
    { dangerouslyUseHTMLString: true, confirmButtonText: '关闭' }
  )
}

const handleEmergencyRepair = () => {
  /**
   * 处理紧急报修
   */
  console.log('紧急报修')
  emergencyRepairDialogVisible.value = true
}

const handleCloseEmergencyRepairDialog = () => {
  /**
   * 处理关闭紧急报修对话框
   */
  emergencyRepairDialogVisible.value = false
}

const handleSaveEmergencyRepair = async () => {
  /**
   * 处理保存紧急报修（前端faultType映射为后端type字段）
   */
  try {
    const fault = {
      ...emergencyRepairForm.value,
      type: emergencyRepairForm.value.faultType,
      status: 'reported',
      reportTime: new Date().toISOString()
    }
    await createFault(fault)
    ElMessage.success('报修成功')
    emergencyRepairDialogVisible.value = false
    fetchFaults()
  } catch (error) {
    console.error('报修失败:', error)
    ElMessage.error('报修失败')
  }
}

const handleBatchProcess = async () => {
  /**
   * 处理批量处理故障：循环调用更新接口将状态置为处理中
   */
  if (selectedFaults.value.length === 0) {
    ElMessage.warning('请选择要处理的故障')
    return
  }
  let successCount = 0
  for (const fault of selectedFaults.value) {
    try {
      await updateFault(fault.id, { ...fault, status: 'processing' })
      successCount++
    } catch (error) {
      console.error(`处理故障失败: id=${fault.id}`, error)
    }
  }
  ElMessage.success(`已处理 ${successCount} 条故障`)
  selectedFaults.value = [] // 清空选择
  fetchFaults()
}

const handleViewFault = (fault: any) => {
  /**
   * 查看故障：弹出详情对话框
   * @param fault 故障信息
   */
  ElMessageBox.alert(
    `<div style="line-height:1.8">
      <b>故障ID：</b>${fault.id}<br/>
      <b>设备名称：</b>${fault.equipmentName || '-'}<br/>
      <b>故障类型：</b>${fault.faultType || '-'}<br/>
      <b>故障描述：</b>${fault.description || '-'}<br/>
      <b>报修时间：</b>${fault.reportTime || '-'}<br/>
      <b>状态：</b>${getStatusText(fault.status)}<br/>
      <b>报修人：</b>${fault.reporter || '-'}<br/>
      <b>联系方式：</b>${fault.contact || '-'}
    </div>`,
    '故障详情',
    { dangerouslyUseHTMLString: true, confirmButtonText: '关闭' }
  )
}

const handleCloseFault = async (fault: any) => {
  /**
   * 关闭故障
   * @param fault 故障信息
   */
  try {
    fault.status = 'completed'
    await updateFault(fault.id, fault)
    ElMessage.success('故障已关闭')
    fetchFaults()
  } catch (error) {
    console.error('关闭故障失败:', error)
    ElMessage.error('关闭故障失败')
  }
}

const handleCopilotAdvice = async (fault: any) => {
  /**
   * 获取维修 Copilot 故障维修建议并弹出建议对话框
   * @param fault 故障信息（取 equipmentId 作为设备标识，faultType 或 description 前20字作为故障关键词）
   */
  const equipmentId = fault.equipmentId ?? fault.equipment_id ?? fault.assetId ?? ''
  const symptom = fault.faultType || String(fault.description || '').slice(0, 20)
  copilotLoadingId.value = fault.id
  try {
    const res = await getFaultAdvice({ equipmentId, symptom, limit: 5 })
    const data = unwrapResponseData<any>(res, {} as any) || {}
    const advices = Array.isArray(data.advices) ? data.advices : []
    // 按相似度得分降序排列
    copilotAdvices.value = [...advices].sort((a: any, b: any) => (b.score ?? 0) - (a.score ?? 0))
    copilotSources.value = Array.isArray(data.evidence?.sources) ? data.evidence.sources : []
    copilotTitle.value = fault.equipmentName || fault.faultType || '故障详情'
    copilotDialogVisible.value = true
  } catch (error) {
    console.error('获取维修 Copilot 建议失败:', error)
    ElMessage.error('获取维修 Copilot 建议失败')
  } finally {
    copilotLoadingId.value = null
  }
}

const handleFaultSelectionChange = (selection: any[]) => {
  /**
   * 处理故障选择变化
   * @param selection 选中的故障列表
   */
  selectedFaults.value = selection
  console.log('选中的故障:', selection)
}

const handleWorkorderSelectionChange = (selection: any[]) => {
  /**
   * 处理工单选择变化
   * @param selection 选中的工单列表
   */
  selectedWorkorders.value = selection
  console.log('选中的工单:', selection)
}

const handleCreateWorkorder = () => {
  /**
   * 处理创建工单
   */
  console.log('创建工单')
  isEditWorkorderDialog.value = false
  workorderForm.value = {
    id: null,
    workorderType: '',
    equipmentName: '',
    priority: 'medium',
    assignee: '',
    description: '',
    status: 'pending'
  }
  workorderDialogVisible.value = true
}

const handleCloseWorkorderDialog = () => {
  /**
   * 处理关闭工单对话框
   */
  workorderDialogVisible.value = false
}

const handleSaveWorkorder = async () => {
  /**
   * 处理保存工单（前端workorderType映射为后端type字段）
   */
  try {
    const payload = { ...workorderForm.value, type: workorderForm.value.workorderType }
    if (isEditWorkorderDialog.value) {
      await updateWorkOrder(workorderForm.value.id!, payload)
      ElMessage.success('更新成功')
    } else {
      await createWorkOrder(payload)
      ElMessage.success('创建成功')
    }
    workorderDialogVisible.value = false
    fetchWorkOrders()
  } catch (error) {
    console.error('保存工单失败:', error)
    ElMessage.error('保存工单失败')
  }
}

const handleBatchAssign = () => {
  /**
   * 处理批量指派工单
   */
  if (selectedWorkorders.value.length === 0) {
    ElMessage.warning('请选择要指派的工单')
    return
  }
  batchAssignDialogVisible.value = true
}

const handleCloseBatchAssignDialog = () => {
  /**
   * 处理关闭批量指派对话框
   */
  batchAssignDialogVisible.value = false
  batchAssignForm.value = {
    assignee: '',
    remark: ''
  }
}

const handleSaveBatchAssign = async () => {
  /**
   * 处理保存批量指派：循环调用更新接口写入指派人
   */
  if (!batchAssignForm.value.assignee) {
    ElMessage.warning('请输入指派人员')
    return
  }
  let successCount = 0
  for (const workorder of selectedWorkorders.value) {
    try {
      await updateWorkOrder(workorder.id, {
        ...workorder,
        type: workorder.workorderType,
        assignee: batchAssignForm.value.assignee
      })
      successCount++
    } catch (error) {
      console.error(`指派工单失败: id=${workorder.id}`, error)
    }
  }
  ElMessage.success(`已指派 ${successCount} 个工单给 ${batchAssignForm.value.assignee}`)
  // 清空选择和表单
  selectedWorkorders.value = []
  batchAssignForm.value = {
    assignee: '',
    remark: ''
  }
  batchAssignDialogVisible.value = false
  fetchWorkOrders()
}

const handleExportWorkorder = () => {
  /**
   * 处理导出工单：优先导出选中行，否则导出全部，生成CSV文件
   */
  const exportData = selectedWorkorders.value.length > 0 ? selectedWorkorders.value : workorders.value
  if (exportData.length === 0) {
    ElMessage.warning('暂无工单数据可导出')
    return
  }
  const headers = ['工单ID', '工单类型', '设备名称', '优先级', '指派给', '状态', '工单描述']
  const priorityText: Record<string, string> = { high: '高', medium: '中', low: '低' }
  const statusText: Record<string, string> = { pending: '待处理', processing: '处理中', completed: '已完成', cancelled: '已取消' }
  const rows = exportData.map((w: any) => [
    w.id, w.workorderType, w.equipmentName,
    priorityText[w.priority] ?? w.priority,
    w.assignee ?? '',
    statusText[w.status] ?? w.status,
    w.description ?? ''
  ])
  const csv = [headers, ...rows]
    .map(line => line.map(v => `"${String(v ?? '').replace(/"/g, '""')}"`).join(','))
    .join('\n')
  const blob = new Blob(['﻿' + csv], { type: 'text/csv;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `workorders-${Date.now()}.csv`
  a.click()
  URL.revokeObjectURL(url)
  ElMessage.success(`已导出 ${exportData.length} 个工单`)
}

const handleEditWorkorder = (workorder: any) => {
  /**
   * 处理编辑工单
   * @param workorder 要编辑的工单信息
   */
  console.log('编辑工单:', workorder)
  isEditWorkorderDialog.value = true
  workorderForm.value = { ...workorder }
  workorderDialogVisible.value = true
}

const handleCompleteWorkorder = async (workorder: any) => {
  /**
   * 处理完成工单
   * @param workorder 要完成的工单信息
   */
  try {
    workorder.status = 'completed'
    await updateWorkOrder(workorder.id, workorder)
    ElMessage.success('工单已完成')
    fetchWorkOrders()
  } catch (error) {
    console.error('完成工单失败:', error)
    ElMessage.error('完成工单失败')
  }
}

const handleCancelWorkorder = async (workorder: any) => {
  /**
   * 处理取消工单
   * @param workorder 要取消的工单信息
   */
  try {
    workorder.status = 'cancelled'
    await updateWorkOrder(workorder.id, workorder)
    ElMessage.success('工单已取消')
    fetchWorkOrders()
  } catch (error) {
    console.error('取消工单失败:', error)
    ElMessage.error('取消工单失败')
  }
}

const handleAddMaintenancePlan = () => {
  /**
   * 处理添加维护计划
   */
  isEditPlan.value = false
  maintenancePlanForm.value = {
    id: null,
    equipmentName: '',
    maintenanceType: '',
    frequency: '',
    nextExecution: '',
    status: 'active'
  }
  maintenancePlanDialogVisible.value = true
}

const handleCloseMaintenancePlanDialog = () => {
  /**
   * 处理关闭维护计划对话框
   */
  maintenancePlanDialogVisible.value = false
}

const handleSaveMaintenancePlan = async () => {
  /**
   * 处理保存维护计划（前端maintenanceType映射为后端type字段）
   */
  try {
    const payload = { ...maintenancePlanForm.value, type: maintenancePlanForm.value.maintenanceType }
    if (isEditPlan.value) {
      await updateMaintenancePlan(maintenancePlanForm.value.id!, payload)
      ElMessage.success('更新成功')
    } else {
      await createMaintenancePlan(payload)
      ElMessage.success('创建成功')
    }
    maintenancePlanDialogVisible.value = false
    fetchPlans()
  } catch (error) {
    console.error('保存维护计划失败:', error)
    ElMessage.error('保存维护计划失败')
  }
}
</script>

<style scoped>
.eam-submodule-container {
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 20px;
  min-height: 100vh;
  background-color: #f5f7fa;
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

.header-content {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.submodule-title {
  font-size: 24px;
  font-weight: bold;
  margin: 0;
  color: #303133;
}

.submodule-description {
  font-size: 14px;
  color: #606266;
  margin: 0;
}

.submodule-tabs-card {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.tab-content {
  padding: 20px;
}

.sub-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.sub-card-header h3 {
  font-size: 18px;
  font-weight: 600;
  margin: 0;
  color: #303133;
}

.preventive-maintenance-content {
  margin-top: 20px;
}

.fault-repair-content {
  margin-top: 20px;
}

.workorder-content {
  margin-top: 20px;
}

.maintenance-record-content {
  margin-top: 20px;
}

/* 维修 Copilot 建议对话框样式 */
.copilot-advice-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.copilot-advice-card :deep(.el-card__body) {
  padding: 12px 16px;
}

.copilot-advice-header {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
}

.copilot-advice-body p {
  margin: 4px 0;
  line-height: 1.6;
}

.copilot-label {
  color: #909399;
}

.copilot-resolved-time {
  color: #909399;
  font-size: 13px;
}

.copilot-documents {
  margin-top: 4px;
}

.copilot-doc-tag {
  margin-right: 6px;
}

.copilot-evidence {
  margin-top: 12px;
  font-size: 12px;
  color: #909399;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .eam-submodule-container {
    padding: 12px;
  }
  
  .submodule-title {
    font-size: 20px;
  }
  
  .tab-content {
    padding: 12px;
  }
  
  .sub-card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
}
</style>
