<template>
  <div class="task-management-view">
    <!-- 页面头部和面包屑导航 -->
    <div class="page-header">
      <h2>任务管理</h2>
      <el-breadcrumb>
        <el-breadcrumb-item>
          <router-link to="/home/agv">首页</router-link>
        </el-breadcrumb-item>
        <el-breadcrumb-item>
          <router-link to="/home/agv">AGV系统</router-link>
        </el-breadcrumb-item>
        <el-breadcrumb-item>
          <router-link to="/home/agv/task-management">任务管理</router-link>
        </el-breadcrumb-item>
        <el-breadcrumb-item :to="`/home/agv/task-management/${activeTab}`">
          {{ tabLabelMap[activeTab] }}
        </el-breadcrumb-item>
      </el-breadcrumb>
    </div>
    
    <!-- 功能标签页 -->
    <el-tabs v-model="activeTab" class="function-tabs">
      <!-- 任务创建 -->
      <el-tab-pane label="任务创建" name="creation">
        <div class="tab-content">
          <el-card shadow="hover">
            <template #header>
              <div class="card-header">
                <span>支持手动或自动（根据生产需求）生成搬运任务</span>
              </div>
            </template>
            <div class="task-creation-content">
              <!-- 任务创建表单 -->
              <div class="task-creation-form">
                <h3>创建新任务</h3>
                <el-form :model="newTask" label-width="120px" :rules="taskRules" ref="taskFormRef">
                  <el-row :gutter="20">
                    <el-col :span="12">
                      <el-form-item label="任务类型" prop="type">
                        <el-select v-model="newTask.type" placeholder="选择任务类型">
                          <el-option label="搬运任务" value="transport" />
                          <el-option label="充电任务" value="charge" />
                          <el-option label="巡检任务" value="inspection" />
                          <el-option label="紧急任务" value="emergency" />
                        </el-select>
                      </el-form-item>
                    </el-col>
                    <el-col :span="12">
                      <el-form-item label="优先级" prop="priority">
                        <el-select v-model="newTask.priority" placeholder="选择优先级">
                          <el-option label="低" value="low" />
                          <el-option label="中" value="medium" />
                          <el-option label="高" value="high" />
                          <el-option label="紧急" value="emergency" />
                        </el-select>
                      </el-form-item>
                    </el-col>
                    <el-col :span="12">
                      <el-form-item label="起点" prop="startPoint">
                        <el-input v-model="newTask.startPoint" placeholder="输入起点位置" />
                      </el-form-item>
                    </el-col>
                    <el-col :span="12">
                      <el-form-item label="终点" prop="endPoint">
                        <el-input v-model="newTask.endPoint" placeholder="输入终点位置" />
                      </el-form-item>
                    </el-col>
                    <el-col :span="12">
                      <el-form-item label="货物信息" prop="cargoInfo">
                        <el-input v-model="newTask.cargoInfo" placeholder="输入货物名称或ID" />
                      </el-form-item>
                    </el-col>
                    <el-col :span="12">
                      <el-form-item label="数量" prop="quantity">
                        <el-input-number v-model="newTask.quantity" :min="1" :max="100" />
                      </el-form-item>
                    </el-col>
                    <el-col :span="12">
                      <el-form-item label="要求完成日期" prop="requiredDate">
                        <el-date-picker v-model="newTask.requiredDate" type="date" placeholder="选择日期" style="width: 100%" />
                      </el-form-item>
                    </el-col>
                    <el-col :span="12">
                      <el-form-item label="负责人" prop="assignee">
                        <el-select v-model="newTask.assignee" placeholder="选择负责人">
                          <el-option label="系统自动分配" value="" />
                          <el-option label="AGV001" value="AGV001" />
                          <el-option label="AGV002" value="AGV002" />
                          <el-option label="AGV003" value="AGV003" />
                          <el-option label="AGV004" value="AGV004" />
                          <el-option label="AGV005" value="AGV005" />
                        </el-select>
                      </el-form-item>
                    </el-col>
                    <el-col :span="24">
                      <el-form-item label="任务描述">
                        <el-input v-model="newTask.description" type="textarea" :rows="2" placeholder="输入任务描述" />
                      </el-form-item>
                    </el-col>
                    <el-col :span="24">
                      <el-form-item label="备注">
                        <el-input v-model="newTask.notes" type="textarea" :rows="2" placeholder="输入备注信息" />
                      </el-form-item>
                    </el-col>
                  </el-row>
                  
                  <!-- 操作按钮 -->
                  <div class="action-buttons">
                    <el-button type="primary" @click="submitTask">创建任务</el-button>
                    <el-button @click="resetTaskForm">重置表单</el-button>
                  </div>
                </el-form>
              </div>
              
              <!-- 任务创建历史 -->
              <div class="task-creation-history">
                <h3>最近创建的任务</h3>
                <el-table :data="recentTasks" style="width: 100%" height="300">
                  <el-table-column prop="taskNo" label="任务编号" width="120" />
                  <el-table-column prop="type" label="类型" width="100">
                    <template #default="scope">
                      <el-tag :type="getTaskTypeTag(scope.row.type)">
                        {{ getTaskTypeLabel(scope.row.type) }}
                      </el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="priority" label="优先级" width="100">
                    <template #default="scope">
                      <el-tag :type="getPriorityTag(scope.row.priority)">
                        {{ getPriorityLabel(scope.row.priority) }}
                      </el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="createTime" label="创建时间" width="180" />
                  <el-table-column prop="status" label="状态" width="100">
                    <template #default="scope">
                      <el-tag :type="getStatusTag(scope.row.status)">
                        {{ getStatusLabel(scope.row.status) }}
                      </el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column label="操作" width="120">
                    <template #default="scope">
                      <el-button size="small" type="primary" @click="viewTaskDetail(scope.row)">
                        查看
                      </el-button>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
            </div>
          </el-card>
        </div>
      </el-tab-pane>
      
      <!-- 任务分配 -->
      <el-tab-pane label="任务分配" name="assignment">
        <div class="tab-content">
          <el-card shadow="hover">
            <template #header>
              <div class="card-header">
                <span>展示智能分配逻辑，将任务指派给最合适的空闲/高电量小车</span>
              </div>
            </template>
            <div class="task-assignment-content">
              <!-- 待分配任务列表 -->
              <div class="pending-tasks">
                <h3>待分配任务</h3>
                <el-table :data="pendingTasks" style="width: 100%" height="400" @selection-change="handlePendingSelectionChange">
                  <el-table-column type="selection" width="55" />
                  <el-table-column prop="taskNo" label="任务编号" width="120" />
                  <el-table-column prop="type" label="类型" width="100">
                    <template #default="scope">
                      <el-tag :type="getTaskTypeTag(scope.row.type)">
                        {{ getTaskTypeLabel(scope.row.type) }}
                      </el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="priority" label="优先级" width="100">
                    <template #default="scope">
                      <el-tag :type="getPriorityTag(scope.row.priority)">
                        {{ getPriorityLabel(scope.row.priority) }}
                      </el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="startPoint" label="起点" />
                  <el-table-column prop="endPoint" label="终点" />
                  <el-table-column label="操作" width="120">
                    <template #default="scope">
                      <el-button size="small" type="primary" @click="assignTaskManually(scope.row)">
                        手动分配
                      </el-button>
                    </template>
                  </el-table-column>
                </el-table>
                
                <!-- 批量操作按钮 -->
                <div class="batch-action-buttons">
                  <el-button type="primary" @click="assignTasksAutomatically">
                    <el-icon><MagicStick /></el-icon> 智能分配所选任务
                  </el-button>
                </div>
              </div>
              
              <!-- AGV可用列表 -->
              <div class="available-agvs">
                <h3>可用AGV列表</h3>
                <el-table :data="availableAgvs" style="width: 100%" height="400">
                  <el-table-column prop="code" label="AGV编号" width="100" />
                  <el-table-column prop="status" label="状态" width="100">
                    <template #default="scope">
                      <el-tag :type="getAgvStatusType(scope.row.status)">
                        {{ getAgvStatusLabel(scope.row.status) }}
                      </el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="batteryLevel" label="电量" width="100">
                    <template #default="scope">
                      <el-progress :percentage="scope.row.batteryLevel" :stroke-width="10" :show-text="false" />
                      <span class="battery-text">{{ scope.row.batteryLevel }}%</span>
                    </template>
                  </el-table-column>
                  <el-table-column prop="currentPosition" label="当前位置" />
                  <el-table-column prop="loadStatus" label="负载状态" width="100">
                    <template #default="scope">
                      <el-tag :type="scope.row.loadStatus === 'empty' ? 'success' : 'warning'">
                        {{ scope.row.loadStatus === 'empty' ? '空载' : '负载' }}
                      </el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="assignedTasks" label="已分配任务" width="80" />
                </el-table>
              </div>
            </div>
          </el-card>
        </div>
      </el-tab-pane>
      
      <!-- 任务调度 -->
      <el-tab-pane label="任务调度" name="scheduling">
        <div class="tab-content">
          <el-card shadow="hover">
            <template #header>
              <div class="card-header">
                <span>支持对运行中任务进行实时优先级调整</span>
              </div>
            </template>
            <div class="task-scheduling-content">
              <!-- 运行中任务列表 -->
              <div class="running-tasks">
                <h3>运行中任务</h3>
                <el-table :data="runningTasks" style="width: 100%" height="450">
                  <el-table-column prop="taskNo" label="任务编号" width="120" />
                  <el-table-column prop="agvCode" label="AGV编号" width="100" />
                  <el-table-column prop="type" label="类型" width="100">
                    <template #default="scope">
                      <el-tag :type="getTaskTypeTag(scope.row.type)">
                        {{ getTaskTypeLabel(scope.row.type) }}
                      </el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="priority" label="当前优先级" width="120">
                    <template #default="scope">
                      <el-select v-model="scope.row.priority" placeholder="选择优先级" @change="updateTaskPriority(scope.row)">
                        <el-option label="低" value="low" />
                        <el-option label="中" value="medium" />
                        <el-option label="高" value="high" />
                        <el-option label="紧急" value="emergency" />
                      </el-select>
                    </template>
                  </el-table-column>
                  <el-table-column prop="startTime" label="开始时间" width="180" />
                  <el-table-column prop="progress" label="进度" width="120">
                    <template #default="scope">
                      <el-progress :percentage="scope.row.progress" :stroke-width="10" />
                    </template>
                  </el-table-column>
                  <el-table-column prop="estimatedCompletion" label="预计完成" width="180" />
                  <el-table-column label="操作" width="160">
                    <template #default="scope">
                      <el-button size="small" type="warning" @click="pauseTask(scope.row)">
                        暂停
                      </el-button>
                      <el-button size="small" type="danger" @click="cancelTask(scope.row)">
                        取消
                      </el-button>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
            </div>
          </el-card>
        </div>
      </el-tab-pane>
      
      <!-- 任务跟踪 -->
      <el-tab-pane label="任务跟踪" name="tracking">
        <div class="tab-content">
          <el-card shadow="hover">
            <template #header>
              <div class="card-header">
                <span>甘特图或实时列表展示所有任务的执行状态</span>
              </div>
            </template>
            <div class="task-tracking-content">
              <!-- 任务跟踪视图切换 -->
              <div class="tracking-view-switch">
                <el-radio-group v-model="trackingView" size="large">
                  <el-radio-button value="列表视图" />
                  <el-radio-button value="甘特图视图" />
                  <el-radio-button value="地图视图" />
                </el-radio-group>
              </div>
              
              <!-- 列表视图 -->
              <div v-if="trackingView === '列表视图'" class="tracking-list-view">
                <el-table :data="allTasks" style="width: 100%" height="500">
                  <el-table-column prop="taskNo" label="任务编号" width="120" />
                  <el-table-column prop="agvCode" label="AGV编号" width="100" />
                  <el-table-column prop="type" label="类型" width="100">
                    <template #default="scope">
                      <el-tag :type="getTaskTypeTag(scope.row.type)">
                        {{ getTaskTypeLabel(scope.row.type) }}
                      </el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="priority" label="优先级" width="100">
                    <template #default="scope">
                      <el-tag :type="getPriorityTag(scope.row.priority)">
                        {{ getPriorityLabel(scope.row.priority) }}
                      </el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="startPoint" label="起点" width="120" />
                  <el-table-column prop="endPoint" label="终点" width="120" />
                  <el-table-column prop="status" label="状态" width="100">
                    <template #default="scope">
                      <el-tag :type="getStatusTag(scope.row.status)">
                        {{ getStatusLabel(scope.row.status) }}
                      </el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="createTime" label="创建时间" width="180" />
                  <el-table-column prop="startTime" label="开始时间" width="180" />
                  <el-table-column prop="endTime" label="结束时间" width="180" />
                  <el-table-column label="操作" width="120">
                    <template #default="scope">
                      <el-button size="small" type="primary" @click="viewTaskDetail(scope.row)">
                        详情
                      </el-button>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
              
              <!-- 甘特图视图 -->
              <div v-else-if="trackingView === '甘特图视图'" class="tracking-gantt-view">
                <div class="gantt-placeholder">
                  <el-icon class="gantt-icon"><DataAnalysis /></el-icon>
                  <p>任务甘特图</p>
                  <p class="text-secondary">可视化展示任务的时间线和进度</p>
                </div>
              </div>
              
              <!-- 地图视图 -->
              <div v-else-if="trackingView === '地图视图'" class="tracking-map-view">
                <div class="map-placeholder">
                  <el-icon class="map-icon"><MapLocation /></el-icon>
                  <p>任务地图视图</p>
                  <p class="text-secondary">在地图上展示任务的位置和AGV执行情况</p>
                </div>
              </div>
            </div>
          </el-card>
        </div>
      </el-tab-pane>
    </el-tabs>
    
    <!-- 任务详情对话框 -->
    <el-dialog v-model="taskDetailDialogVisible" :title="`任务详情 - ${selectedTask?.taskNo || ''}`" width="700px">
      <div v-if="selectedTask" class="task-detail-content">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="任务编号">{{ selectedTask.taskNo }}</el-descriptions-item>
          <el-descriptions-item label="任务类型">
            <el-tag :type="getTaskTypeTag(selectedTask.type)">{{ getTaskTypeLabel(selectedTask.type) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="优先级">
            <el-tag :type="getPriorityTag(selectedTask.priority)">{{ getPriorityLabel(selectedTask.priority) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusTag(selectedTask.status)">{{ getStatusLabel(selectedTask.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="AGV编号">{{ selectedTask.agvCode || '未分配' }}</el-descriptions-item>
          <el-descriptions-item label="进度">
            <el-progress :percentage="selectedTask.progress || 0" :stroke-width="10" />
          </el-descriptions-item>
          <el-descriptions-item label="起点">{{ selectedTask.startPoint || '-' }}</el-descriptions-item>
          <el-descriptions-item label="终点">{{ selectedTask.endPoint || '-' }}</el-descriptions-item>
          <el-descriptions-item label="货物信息">{{ selectedTask.cargoInfo || '-' }}</el-descriptions-item>
          <el-descriptions-item label="追踪状态">{{ selectedTask.trackStatus || '-' }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ selectedTask.createTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="开始时间">{{ selectedTask.startTime || '未开始' }}</el-descriptions-item>
          <el-descriptions-item label="结束时间">{{ selectedTask.endTime || '未结束' }}</el-descriptions-item>
          <el-descriptions-item label="预计完成">{{ selectedTask.estimatedCompletion || '-' }}</el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button @click="taskDetailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import type { FormInstance } from 'element-plus'
import { ElMessage } from 'element-plus'
import { MagicStick, DataAnalysis, MapLocation } from '@element-plus/icons-vue'
import { useAGVStore } from '@/stores/agv'
import agvApi from '@/api/agv'
import type { AGVTask, TaskPriority, TaskType } from '@/types/agv'

// 活动标签页
type TabName = 'creation' | 'assignment' | 'scheduling' | 'tracking'
const activeTab = ref<TabName>('creation')

// 标签页名称映射
const tabLabelMap: Record<TabName, string> = {
  creation: '任务创建',
  assignment: '任务分配',
  scheduling: '任务调度',
  tracking: '任务跟踪'
}

// 任务表单引用
const taskFormRef = ref<FormInstance>()

// 新任务表单
const newTask = ref({
  taskNo: '',
  type: 'transport' as TaskType,
  priority: 'medium' as TaskPriority,
  startPoint: '',
  endPoint: '',
  cargoInfo: '',
  description: '',
  quantity: 1,
  requiredDate: new Date().toISOString().split('T')[0],
  assignee: '',
  notes: ''
})

// 任务验证规则
const taskRules = reactive({
  type: [{ required: true, message: '请选择任务类型', trigger: 'change' }],
  priority: [{ required: true, message: '请选择优先级', trigger: 'change' }],
  startPoint: [{ required: true, message: '请输入起点', trigger: 'blur' }],
  endPoint: [{ required: true, message: '请输入终点', trigger: 'blur' }],
  quantity: [{ required: true, message: '请输入数量', trigger: 'blur' }, { type: 'number', min: 1, message: '数量必须大于0', trigger: 'blur' }],
  requiredDate: [{ required: true, message: '请选择要求完成日期', trigger: 'change' }]
})

const agvStore = useAGVStore()
const selectedPendingTasks = ref<AGVTask[]>([])

// 任务详情对话框
const taskDetailDialogVisible = ref(false)
const selectedTask = ref<AGVTask | null>(null)

onMounted(async () => {
  await Promise.all([agvStore.loadAGVs(), agvStore.loadTasks()])
})

const tasks = computed(() => agvStore.tasks)
const recentTasks = computed(() => [...tasks.value].sort((a, b) => new Date(b.createTime).getTime() - new Date(a.createTime).getTime()).slice(0, 5))
const pendingTasks = computed(() => tasks.value.filter(t => t.status === 'pending'))
const runningTasks = computed(() => tasks.value.filter(t => t.status === 'assigned' || t.status === 'running' || t.status === 'paused'))
const allTasks = computed(() => tasks.value)

const availableAgvs = computed(() => {
  return agvStore.agvs.map((a) => {
    const assignedTasks = tasks.value.filter(t => t.agvCode === a.code && (t.status === 'assigned' || t.status === 'running' || t.status === 'paused')).length
    return {
      code: a.code,
      status: a.status,
      batteryLevel: a.batteryLevel,
      currentPosition: a.position,
      loadStatus: a.loadStatus,
      assignedTasks
    }
  })
})

const handlePendingSelectionChange = (rows: AGVTask[]) => {
  selectedPendingTasks.value = rows || []
}

// 任务跟踪视图
const trackingView = ref('列表视图')

// 提交任务
const submitTask = async () => {
  taskFormRef.value?.validate(async (valid) => {
    if (valid) {
      try {
        ElMessage({ message: '正在创建任务...', type: 'info' })
        await agvStore.createTask({
          type: newTask.value.type,
          priority: newTask.value.priority,
          agvCode: newTask.value.assignee,
          startPoint: newTask.value.startPoint,
          endPoint: newTask.value.endPoint,
          cargoInfo: newTask.value.cargoInfo,
          status: 'pending'
        } as any)
        await agvStore.loadTasks()
        ElMessage.success('任务创建成功')
        resetTaskForm()
      } catch (error) {
        ElMessage.error('任务创建失败')
        console.error('Failed to submit task:', error)
      }
    }
  })
}

// 重置任务表单
const resetTaskForm = () => {
  taskFormRef.value?.resetFields()
  newTask.value = {
    taskNo: '',
    type: 'transport',
    priority: 'medium',
    startPoint: '',
    endPoint: '',
    cargoInfo: '',
    description: '',
    quantity: 1,
    requiredDate: new Date().toISOString().split('T')[0],
    assignee: '',
    notes: ''
  }
  ElMessage.info('表单已重置')
}

// 查看任务详情
const viewTaskDetail = async (task: any) => {
  try {
    const res = await agvApi.getTaskDetail(task.id)
    const detail = (res as any)?.data || task
    selectedTask.value = detail
    taskDetailDialogVisible.value = true
  } catch (error) {
    // 接口失败时直接使用列表数据
    selectedTask.value = task
    taskDetailDialogVisible.value = true
  }
}

// 手动分配任务
const assignTaskManually = async (task: any) => {
  try {
    ElMessage({ message: '正在手动分配任务...', type: 'info' })
    const candidate = [...availableAgvs.value].filter(a => a.status === 'idle').sort((a, b) => (b.batteryLevel || 0) - (a.batteryLevel || 0))[0]
    if (!candidate) {
      ElMessage.warning('暂无可用AGV')
      return
    }
    await agvStore.assignTaskToAGV(task.id, candidate.code)
    await agvStore.loadTasks()
    ElMessage.success(`任务 ${task.taskNo} 已手动分配给 ${candidate.code}`)
  } catch (error) {
    ElMessage.error('手动分配任务失败')
    console.error('Failed to assign task manually:', error)
  }
}

// 智能分配所选任务
const assignTasksAutomatically = async () => {
  try {
    ElMessage({ message: '正在智能分配所选任务...', type: 'info' })
    if (!selectedPendingTasks.value.length) {
      ElMessage.warning('请先选择待分配任务')
      return
    }
    const candidates = [...availableAgvs.value].filter(a => a.status === 'idle').sort((a, b) => (b.batteryLevel || 0) - (a.batteryLevel || 0))
    if (!candidates.length) {
      ElMessage.warning('暂无可用AGV')
      return
    }
    for (const [idx, task] of selectedPendingTasks.value.entries()) {
      const agv = candidates[idx % candidates.length]
      if (!agv) continue
      await agvStore.assignTaskToAGV(task.id, agv.code)
    }
    await agvStore.loadTasks()
    ElMessage.success('所选任务已智能分配完成')
  } catch (error) {
    ElMessage.error('智能分配任务失败')
    console.error('Failed to assign tasks automatically:', error)
  }
}

// 更新任务优先级
const updateTaskPriority = async (task: any) => {
  try {
    ElMessage({ message: '正在更新任务优先级...', type: 'info' })
    await agvStore.updateTaskPriority(task.id, task.priority)
    ElMessage.success(`任务 ${task.taskNo} 优先级已更新为 ${task.priority}`)
  } catch (error) {
    ElMessage.error('更新任务优先级失败')
    console.error('Failed to update task priority:', error)
  }
}

// 暂停任务
const pauseTask = async (task: any) => {
  try {
    ElMessage({ message: '正在暂停任务...', type: 'info' })
    await agvStore.updateTaskStatus(task.id, 'paused' as any)
    await agvStore.loadTasks()
    ElMessage.success(`任务 ${task.taskNo} 已暂停`)
  } catch (error) {
    ElMessage.error('暂停任务失败')
    console.error('Failed to pause task:', error)
  }
}

// 取消任务
const cancelTask = async (task: any) => {
  try {
    ElMessage({ message: '正在取消任务...', type: 'info' })
    await agvStore.cancelTask(task.id)
    await agvStore.loadTasks()
    ElMessage.success(`任务 ${task.taskNo} 已取消`)
  } catch (error) {
    ElMessage.error('取消任务失败')
    console.error('Failed to cancel task:', error)
  }
}

// 获取任务类型标签
const getTaskTypeTag = (type: string) => {
  switch (type) {
    case 'transport': return 'success'
    case 'charge': return 'warning'
    case 'inspection': return 'info'
    case 'emergency': return 'danger'
    default: return 'info'
  }
}

// 获取任务类型标签
const getTaskTypeLabel = (type: string) => {
  switch (type) {
    case 'transport': return '搬运任务'
    case 'charge': return '充电任务'
    case 'inspection': return '巡检任务'
    case 'emergency': return '紧急任务'
    default: return type
  }
}

// 获取优先级标签类型
const getPriorityTag = (priority: string) => {
  switch (priority) {
    case 'low': return 'info'
    case 'medium': return 'warning'
    case 'high': return 'warning'
    case 'emergency': return 'danger'
    default: return 'info'
  }
}

// 获取优先级标签文本
const getPriorityLabel = (priority: string) => {
  switch (priority) {
    case 'low': return '低'
    case 'medium': return '中'
    case 'high': return '高'
    case 'emergency': return '紧急'
    default: return priority
  }
}

// 获取状态标签类型
const getStatusTag = (status: string) => {
  switch (status) {
    case 'pending': return 'warning'
    case 'assigned': return 'primary'
    case 'running': return 'success'
    case 'paused': return 'warning'
    case 'completed': return 'success'
    case 'failed': return 'danger'
    case 'cancelled': return 'info'
    default: return 'info'
  }
}

// 获取状态标签文本
const getStatusLabel = (status: string) => {
  switch (status) {
    case 'pending': return '待处理'
    case 'assigned': return '已分配'
    case 'running': return '运行中'
    case 'paused': return '已暂停'
    case 'completed': return '已完成'
    case 'failed': return '失败'
    case 'cancelled': return '已取消'
    default: return status
  }
}

// 获取AGV状态类型
const getAgvStatusType = (status: string) => {
  switch (status) {
    case 'running': return 'success'
    case 'idle': return 'info'
    case 'charging': return 'warning'
    case 'fault': return 'danger'
    case 'error': return 'danger'
    default: return 'info'
  }
}

// 获取AGV状态标签文本
const getAgvStatusLabel = (status: string) => {
  switch (status) {
    case 'running': return '运行中'
    case 'idle': return '空闲'
    case 'charging': return '充电中'
    case 'fault': return '故障'
    case 'error': return '错误'
    default: return status
  }
}
</script>

<style scoped>
.task-management-view {
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

/* 任务创建内容布局 */
.task-creation-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

@media (max-width: 1200px) {
  .task-creation-content {
    grid-template-columns: 1fr;
  }
}

/* 任务创建表单 */
.task-creation-form h3,
.task-creation-history h3,
.pending-tasks h3,
.available-agvs h3,
.running-tasks h3 {
  margin-bottom: 16px;
  font-size: 1.2rem;
}

/* 操作按钮组 */
.action-buttons {
  margin-top: 20px;
  display: flex;
  gap: 10px;
}

/* 任务分配内容布局 */
.task-assignment-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

@media (max-width: 1200px) {
  .task-assignment-content {
    grid-template-columns: 1fr;
  }
}

/* 批量操作按钮 */
.batch-action-buttons {
  margin-top: 16px;
  display: flex;
  justify-content: flex-start;
}

/* 电池状态文本 */
.battery-text {
  margin-left: 10px;
  font-size: 14px;
}

/* 任务调度内容 */
.task-scheduling-content {
  margin-top: 20px;
}

/* 任务跟踪内容 */
.task-tracking-content {
  margin-top: 20px;
}

/* 跟踪视图切换 */
.tracking-view-switch {
  margin-bottom: 20px;
}

/* 甘特图占位符 */
.gantt-placeholder,
.map-placeholder {
  background-color: #f5f7fa;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  height: 500px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
}

.gantt-icon,
.map-icon {
  font-size: 48px;
  color: #409eff;
  margin-bottom: 16px;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .task-management-view {
    padding: 16px;
  }
  
  h2 {
    font-size: 1.5rem;
  }
}

@media (max-width: 768px) {
  .task-management-view {
    padding: 12px;
  }
  
  h2 {
    font-size: 1.3rem;
    margin-bottom: 16px;
  }
  
  .action-buttons {
    flex-direction: column;
  }
  
  .gantt-placeholder,
  .map-placeholder {
    height: 300px;
  }
}
</style>
