<template>
  <div class="project-detail-view">
    <h3>项目详情</h3>
    
    <!-- 返回按钮 -->
    <el-button
      type="default"
      size="small"
      @click="goBack"
      class="back-button"
    >
      返回项目列表
    </el-button>
    
    <!-- 项目基本信息 -->
    <el-card shadow="hover" class="info-card">
      <template #header>
        <div class="card-header">
          <span>基本信息</span>
          <el-button type="primary" size="small" @click="showEditDialog">编辑项目</el-button>
        </div>
      </template>
      <div class="project-info">
        <div class="info-row">
          <div class="info-label">项目名称</div>
          <div class="info-value">{{ project.name }}</div>
        </div>
        <div class="info-row">
          <div class="info-label">项目编码</div>
          <div class="info-value">{{ project.code }}</div>
        </div>
        <div class="info-row">
          <div class="info-label">项目类型</div>
          <div class="info-value">{{ project.type }}</div>
        </div>
        <div class="info-row">
          <div class="info-label">项目经理</div>
          <div class="info-value">{{ project.manager }}</div>
        </div>
        <div class="info-row">
          <div class="info-label">开始日期</div>
          <div class="info-value">{{ project.startDate }}</div>
        </div>
        <div class="info-row">
          <div class="info-label">结束日期</div>
          <div class="info-value">{{ project.endDate }}</div>
        </div>
        <div class="info-row">
          <div class="info-label">项目进度</div>
          <div class="info-value progress-container">
            <el-progress :percentage="project.progress" :stroke-width="15"></el-progress>
            <span class="progress-text">{{ project.progress }}%</span>
          </div>
        </div>
        <div class="info-row">
          <div class="info-label">项目状态</div>
          <div class="info-value">
            <el-tag :type="getStatusTagType(project.status)">{{ statusMap[project.status] || project.status }}</el-tag>
          </div>
        </div>
      </div>
    </el-card>
    
    <!-- 项目任务列表 -->
    <el-card shadow="hover" class="tasks-card">
      <template #header>
        <div class="card-header">
          <span>项目任务</span>
          <el-button type="primary" size="small" @click="showAddTaskDialog">添加任务</el-button>
        </div>
      </template>
      <el-table :data="projectTasks" style="width: 100%">
        <el-table-column prop="id" label="任务ID" min-width="80"></el-table-column>
        <el-table-column prop="name" label="任务名称" min-width="200"></el-table-column>
        <el-table-column prop="startDate" label="开始日期" min-width="120"></el-table-column>
        <el-table-column prop="endDate" label="结束日期" min-width="120"></el-table-column>
        <el-table-column prop="progress" label="进度" min-width="150">
          <template #default="scope">
            <el-progress :percentage="scope.row.progress" :stroke-width="10"></el-progress>
          </template>
        </el-table-column>
        <el-table-column prop="assignee" label="负责人" min-width="120"></el-table-column>
        <el-table-column prop="status" label="状态" min-width="100">
          <template #default="scope">
            <el-select
              v-model="scope.row.status"
              placeholder="请选择状态"
              @change="updateTaskStatus(scope.row.id, scope.row.status)"
              size="small"
            >
              <el-option label="规划中" value="planning"></el-option>
              <el-option label="执行中" value="in-progress"></el-option>
              <el-option label="已完成" value="completed"></el-option>
              <el-option label="延迟" value="delayed"></el-option>
              <el-option label="已暂停" value="suspended"></el-option>
              <el-option label="已取消" value="cancelled"></el-option>
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="180" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="viewTask(scope.row.id)">查看</el-button>
            <el-button type="primary" size="small" @click="showEditTaskDialog(scope.row.id)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDeleteTask(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    
    <!-- 任务详情对话框 -->
    <el-dialog
      v-model="taskDetailDialogVisible"
      title="任务详情"
      width="600px"
      @close="resetTaskDetailForm"
    >
      <el-form
        :model="taskDetailForm"
        label-width="120px"
      >
        <el-form-item label="任务ID">
          <el-input v-model="taskDetailForm.id" disabled></el-input>
        </el-form-item>
        <el-form-item label="任务名称">
          <el-input v-model="taskDetailForm.name" disabled></el-input>
        </el-form-item>
        <el-form-item label="开始日期">
          <el-date-picker
            v-model="taskDetailForm.startDate"
            type="date"
            disabled
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          ></el-date-picker>
        </el-form-item>
        <el-form-item label="结束日期">
          <el-date-picker
            v-model="taskDetailForm.endDate"
            type="date"
            disabled
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          ></el-date-picker>
        </el-form-item>
        <el-form-item label="进度">
          <el-progress :percentage="taskDetailForm.progress" :stroke-width="10"></el-progress>
        </el-form-item>
        <el-form-item label="负责人">
          <el-input v-model="taskDetailForm.assignee" disabled></el-input>
        </el-form-item>
        <el-form-item label="状态">
          <el-tag :type="getTaskStatusTagType(taskDetailForm.status)">{{ taskStatusMap[taskDetailForm.status] || taskDetailForm.status }}</el-tag>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="resetTaskDetailForm">关闭</el-button>
          <el-button type="primary" @click="showEditTaskDialog(taskDetailForm.id)">编辑任务</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 添加任务对话框 -->
    <el-dialog
      v-model="addTaskDialogVisible"
      title="添加任务"
      width="600px"
      @close="resetAddTaskForm"
    >
      <el-form
        ref="addTaskFormRef"
        :model="addTaskForm"
        :rules="addTaskRules"
        label-width="120px"
      >
        <el-form-item label="任务名称" prop="name">
          <el-input
            v-model="addTaskForm.name"
            placeholder="请输入任务名称"
            maxlength="100"
            show-word-limit
          ></el-input>
        </el-form-item>
        <el-form-item label="开始日期" prop="startDate">
          <el-date-picker
            v-model="addTaskForm.startDate"
            type="date"
            placeholder="请选择开始日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          ></el-date-picker>
        </el-form-item>
        <el-form-item label="结束日期" prop="endDate">
          <el-date-picker
            v-model="addTaskForm.endDate"
            type="date"
            placeholder="请选择结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          ></el-date-picker>
        </el-form-item>
        <el-form-item label="进度" prop="progress">
          <el-slider
            v-model="addTaskForm.progress"
            :min="0"
            :max="100"
            show-input
          ></el-slider>
        </el-form-item>
        <el-form-item label="负责人" prop="assignee">
          <el-input
            v-model="addTaskForm.assignee"
            placeholder="请输入负责人"
            maxlength="50"
            show-word-limit
          ></el-input>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select
            v-model="addTaskForm.status"
            placeholder="请选择状态"
          >
            <el-option label="规划中" value="planning"></el-option>
            <el-option label="执行中" value="in-progress"></el-option>
            <el-option label="已完成" value="completed"></el-option>
            <el-option label="延迟" value="delayed"></el-option>
            <el-option label="已暂停" value="suspended"></el-option>
            <el-option label="已取消" value="cancelled"></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="resetAddTaskForm">取消</el-button>
          <el-button type="primary" :loading="addTaskLoading" @click="handleAddTask">确定</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 编辑任务对话框 -->
    <el-dialog
      v-model="editTaskDialogVisible"
      title="编辑任务"
      width="600px"
      @close="resetEditTaskForm"
    >
      <el-form
        ref="editTaskFormRef"
        :model="editTaskForm"
        :rules="editTaskRules"
        label-width="120px"
      >
        <el-form-item label="任务名称" prop="name">
          <el-input
            v-model="editTaskForm.name"
            placeholder="请输入任务名称"
            maxlength="100"
            show-word-limit
          ></el-input>
        </el-form-item>
        <el-form-item label="开始日期" prop="startDate">
          <el-date-picker
            v-model="editTaskForm.startDate"
            type="date"
            placeholder="请选择开始日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          ></el-date-picker>
        </el-form-item>
        <el-form-item label="结束日期" prop="endDate">
          <el-date-picker
            v-model="editTaskForm.endDate"
            type="date"
            placeholder="请选择结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          ></el-date-picker>
        </el-form-item>
        <el-form-item label="进度" prop="progress">
          <el-slider
            v-model="editTaskForm.progress"
            :min="0"
            :max="100"
            show-input
          ></el-slider>
        </el-form-item>
        <el-form-item label="负责人" prop="assignee">
          <el-input
            v-model="editTaskForm.assignee"
            placeholder="请输入负责人"
            maxlength="50"
            show-word-limit
          ></el-input>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select
            v-model="editTaskForm.status"
            placeholder="请选择状态"
          >
            <el-option label="规划中" value="planning"></el-option>
            <el-option label="执行中" value="in-progress"></el-option>
            <el-option label="已完成" value="completed"></el-option>
            <el-option label="延迟" value="delayed"></el-option>
            <el-option label="已暂停" value="suspended"></el-option>
            <el-option label="已取消" value="cancelled"></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="resetEditTaskForm">取消</el-button>
          <el-button type="primary" :loading="editTaskLoading" @click="handleEditTask">确定</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 编辑项目对话框 -->
    <el-dialog
      v-model="editDialogVisible"
      title="编辑项目"
      width="600px"
      @close="resetEditForm"
    >
      <el-form
        ref="editProjectFormRef"
        :model="editProjectForm"
        :rules="editProjectRules"
        label-width="120px"
      >
        <el-form-item label="项目名称" prop="name">
          <el-input
            v-model="editProjectForm.name"
            placeholder="请输入项目名称"
            maxlength="100"
            show-word-limit
          ></el-input>
        </el-form-item>
        <el-form-item label="项目编码" prop="code">
          <el-input
            v-model="editProjectForm.code"
            placeholder="请输入项目编码"
            maxlength="50"
            show-word-limit
          ></el-input>
        </el-form-item>
        <el-form-item label="项目经理" prop="manager">
          <el-input
            v-model="editProjectForm.manager"
            placeholder="请输入项目经理"
            maxlength="50"
            show-word-limit
          ></el-input>
        </el-form-item>
        <el-form-item label="项目类型" prop="type">
          <el-select
            v-model="editProjectForm.type"
            placeholder="请选择项目类型"
          >
            <el-option label="新产品研发(NPI)" value="NPI"></el-option>
            <el-option label="产品改进" value="改进"></el-option>
            <el-option label="技术研发" value="研发"></el-option>
            <el-option label="其他" value="其他"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="开始日期" prop="startDate">
          <el-date-picker
            v-model="editProjectForm.startDate"
            type="date"
            placeholder="请选择开始日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          ></el-date-picker>
        </el-form-item>
        <el-form-item label="结束日期" prop="endDate">
          <el-date-picker
            v-model="editProjectForm.endDate"
            type="date"
            placeholder="请选择结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          ></el-date-picker>
        </el-form-item>
        <el-form-item label="项目进度" prop="progress">
          <el-slider
            v-model="editProjectForm.progress"
            :min="0"
            :max="100"
            show-input
          ></el-slider>
        </el-form-item>
        <el-form-item label="项目状态" prop="status">
          <el-select
            v-model="editProjectForm.status"
            placeholder="请选择项目状态"
          >
            <el-option label="规划中" value="planning"></el-option>
            <el-option label="执行中" value="in-progress"></el-option>
            <el-option label="已完成" value="completed"></el-option>
            <el-option label="延迟" value="delayed"></el-option>
            <el-option label="已暂停" value="suspended"></el-option>
            <el-option label="已取消" value="cancelled"></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="resetEditForm">取消</el-button>
          <el-button type="primary" :loading="editProjectLoading" @click="handleEditProject">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useProjectManagementStore } from '@/stores/plm/projectManagement'

// 初始化项目管理状态
const projectStore = useProjectManagementStore()
const router = useRouter()
const route = useRoute()

// 获取项目ID
const projectId = computed(() => route.params.id as string)

// 获取当前项目详情
const project = computed(() => projectStore.projects.find(p => p.id === projectId.value) || {
  id: '',
  name: '',
  code: '',
  type: '',
  manager: '',
  startDate: '',
  endDate: '',
  progress: 0,
  status: ''
})

// 从store获取项目任务列表
const projectTasks = computed(() => projectStore.tasks.filter(task => task.projectId === projectId.value))

// 英文状态到中文状态的映射
const statusMap: Record<string, string> = {
  'planning': '规划中',
  'in-progress': '执行中',
  'completed': '已完成',
  'delayed': '延迟',
  'suspended': '已暂停',
  'cancelled': '已取消'
}

// 英文任务状态到中文任务状态的映射
const taskStatusMap: Record<string, string> = {
  'planning': '规划中',
  'in-progress': '执行中',
  'completed': '已完成',
  'delayed': '延迟',
  'suspended': '已暂停',
  'cancelled': '已取消'
}

// 状态标签类型映射
const getStatusTagType = (status: string) => {
  const tagTypeMap: Record<string, string> = {
    'planning': 'info',
    'in-progress': 'info',
    'completed': 'success',
    'delayed': 'danger',
    'suspended': 'warning',
    'cancelled': 'danger'
  }
  return tagTypeMap[status] || 'info'
}

// 任务状态标签类型映射
const getTaskStatusTagType = (status: string) => {
  const tagTypeMap: Record<string, string> = {
    'planning': 'info',
    'in-progress': 'info',
    'completed': 'success',
    'delayed': 'danger',
    'suspended': 'warning',
    'cancelled': 'danger'
  }
  return tagTypeMap[status] || 'info'
}

// 返回项目列表
const goBack = () => {
  router.push('/home/plm/project-management/dashboard')
}

// 任务详情对话框
const taskDetailDialogVisible = ref(false)
const taskDetailForm = ref({
  id: 0,
  name: '',
  startDate: '',
  endDate: '',
  progress: 0,
  assignee: '',
  status: ''
})

// 查看任务详情
const viewTask = (taskId: number) => {
  const task = projectStore.tasks.find(task => task.id === taskId)
  if (task) {
    taskDetailForm.value = { ...task }
    taskDetailDialogVisible.value = true
  }
}

// 重置任务详情表单
const resetTaskDetailForm = () => {
  taskDetailForm.value = {
    id: 0,
    name: '',
    startDate: '',
    endDate: '',
    progress: 0,
    assignee: '',
    status: ''
  }
  taskDetailDialogVisible.value = false
}

// 添加任务对话框
const addTaskDialogVisible = ref(false)
const addTaskFormRef = ref<any>(null)
const addTaskLoading = ref(false)
const addTaskForm = ref({
  name: '',
  startDate: '',
  endDate: '',
  progress: 0,
  assignee: '',
  status: 'planning',
  projectId: projectId.value
})

// 添加任务表单验证规则
const addTaskRules = ref({
  name: [
    { required: true, message: '请输入任务名称', trigger: 'blur' },
    { min: 2, max: 100, message: '任务名称长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  startDate: [
    { required: true, message: '请选择开始日期', trigger: 'change' }
  ],
  endDate: [
    { required: true, message: '请选择结束日期', trigger: 'change' }
  ],
  assignee: [
    { required: true, message: '请输入负责人', trigger: 'blur' },
    { min: 2, max: 50, message: '负责人长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ]
})

// 显示添加任务对话框
const showAddTaskDialog = () => {
  addTaskForm.value.projectId = projectId.value
  addTaskDialogVisible.value = true
}

// 重置添加任务表单
const resetAddTaskForm = () => {
  if (addTaskFormRef.value) {
    addTaskFormRef.value.resetFields()
  }
  addTaskForm.value = {
    name: '',
    startDate: '',
    endDate: '',
    progress: 0,
    assignee: '',
    status: 'planning',
    projectId: projectId.value
  }
  addTaskDialogVisible.value = false
}

// 处理添加任务
const handleAddTask = async () => {
  if (!addTaskFormRef.value) return
  
  try {
    // 表单验证
    await addTaskFormRef.value.validate()
    
    addTaskLoading.value = true
    
    // 调用store添加任务
    const newTask = await projectStore.addTask(addTaskForm.value)
    
    if (newTask) {
      ElMessage.success('任务添加成功')
      addTaskDialogVisible.value = false
      resetAddTaskForm()
    } else {
      ElMessage.error('任务添加失败')
    }
  } catch (error: any) {
    if (error.message) {
      ElMessage.error(error.message)
    } else {
      ElMessage.error('表单验证失败，请检查输入')
    }
  } finally {
    addTaskLoading.value = false
  }
}

// 编辑任务对话框
const editTaskDialogVisible = ref(false)
const editTaskFormRef = ref<any>(null)
const editTaskLoading = ref(false)
const editTaskForm = ref({
  id: 0,
  name: '',
  startDate: '',
  endDate: '',
  progress: 0,
  assignee: '',
  status: '',
  projectId: projectId.value
})

// 编辑任务表单验证规则
const editTaskRules = ref({
  name: [
    { required: true, message: '请输入任务名称', trigger: 'blur' },
    { min: 2, max: 100, message: '任务名称长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  startDate: [
    { required: true, message: '请选择开始日期', trigger: 'change' }
  ],
  endDate: [
    { required: true, message: '请选择结束日期', trigger: 'change' }
  ],
  assignee: [
    { required: true, message: '请输入负责人', trigger: 'blur' },
    { min: 2, max: 50, message: '负责人长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ]
})

// 显示编辑任务对话框
const showEditTaskDialog = (taskId: number) => {
  const task = projectStore.tasks.find(task => task.id === taskId)
  if (task) {
    editTaskForm.value = { ...task }
    editTaskDialogVisible.value = true
  }
}

// 重置编辑任务表单
const resetEditTaskForm = () => {
  if (editTaskFormRef.value) {
    editTaskFormRef.value.resetFields()
  }
  editTaskForm.value = {
    id: 0,
    name: '',
    startDate: '',
    endDate: '',
    progress: 0,
    assignee: '',
    status: '',
    projectId: projectId.value
  }
  editTaskDialogVisible.value = false
}

// 处理编辑任务
const handleEditTask = async () => {
  if (!editTaskFormRef.value) return
  
  try {
    // 表单验证
    await editTaskFormRef.value.validate()
    
    editTaskLoading.value = true
    
    // 调用store更新任务
    const updatedTask = await projectStore.updateTask(editTaskForm.value.id, editTaskForm.value)
    
    if (updatedTask) {
      ElMessage.success('任务编辑成功')
      editTaskDialogVisible.value = false
      resetEditTaskForm()
    } else {
      ElMessage.error('任务编辑失败')
    }
  } catch (error: any) {
    if (error.message) {
      ElMessage.error(error.message)
    } else {
      ElMessage.error('表单验证失败，请检查输入')
    }
  } finally {
    editTaskLoading.value = false
  }
}

// 更新任务状态
const updateTaskStatus = async (taskId: number, status: string) => {
  try {
    // 调用store更新任务状态
    const updatedTask = await projectStore.updateTaskStatus(taskId, status)
    
    if (updatedTask) {
      ElMessage.success('任务状态更新成功')
    } else {
      ElMessage.error('任务状态更新失败')
    }
  } catch (error) {
    ElMessage.error('任务状态更新失败')
  }
}

// 处理删除任务
const handleDeleteTask = async (taskId: number) => {
  try {
    // 调用store删除任务
    const success = await projectStore.deleteTask(taskId)
    
    if (success) {
      ElMessage.success('任务删除成功')
    } else {
      ElMessage.error('任务删除失败')
    }
  } catch (error) {
    ElMessage.error('任务删除失败')
  }
}

// 编辑项目对话框
const editDialogVisible = ref(false)
const editProjectFormRef = ref<any>(null)
const editProjectLoading = ref(false)

// 编辑项目表单数据
const editProjectForm = ref({
  name: '',
  code: '',
  manager: '',
  type: '',
  startDate: '',
  endDate: '',
  progress: 0,
  status: 'planning'
})

// 编辑项目表单验证规则
const editProjectRules = ref({
  name: [
    { required: true, message: '请输入项目名称', trigger: 'blur' },
    { min: 2, max: 100, message: '项目名称长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入项目编码', trigger: 'blur' },
    { min: 2, max: 50, message: '项目编码长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  manager: [
    { required: true, message: '请输入项目经理', trigger: 'blur' },
    { min: 2, max: 50, message: '项目经理长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择项目类型', trigger: 'change' }
  ],
  startDate: [
    { required: true, message: '请选择开始日期', trigger: 'change' }
  ],
  endDate: [
    { required: true, message: '请选择结束日期', trigger: 'change' }
  ],
  status: [
    { required: true, message: '请选择项目状态', trigger: 'change' }
  ]
})

// 显示编辑项目对话框
const showEditDialog = () => {
  // 填充表单数据
  editProjectForm.value = {
    name: project.value.name,
    code: project.value.code,
    manager: project.value.manager,
    type: project.value.type,
    startDate: project.value.startDate,
    endDate: project.value.endDate,
    progress: project.value.progress,
    status: project.value.status
  }
  editDialogVisible.value = true
}

// 重置编辑项目表单
const resetEditForm = () => {
  if (editProjectFormRef.value) {
    editProjectFormRef.value.resetFields()
  }
  editProjectForm.value = {
    name: '',
    code: '',
    manager: '',
    type: '',
    startDate: '',
    endDate: '',
    progress: 0,
    status: 'planning'
  }
  editDialogVisible.value = false
}

// 处理编辑项目
const handleEditProject = async () => {
  if (!editProjectFormRef.value) return
  
  try {
    // 表单验证
    await editProjectFormRef.value.validate()
    
    editProjectLoading.value = true
    
    // 提交项目数据
    const updatedProject = await projectStore.updateProject(projectId.value, editProjectForm.value)
    
    if (updatedProject) {
      ElMessage.success('项目更新成功')
      editDialogVisible.value = false
      resetEditForm()
    } else {
      ElMessage.error('项目更新失败')
    }
  } catch (error: any) {
    if (error.message) {
      ElMessage.error(error.message)
    } else {
      ElMessage.error('表单验证失败，请检查输入')
    }
  } finally {
    editProjectLoading.value = false
  }
}

// 生命周期钩子
onMounted(async () => {
  // 如果项目列表为空，先加载项目列表
  if (projectStore.projects.length === 0) {
    await projectStore.fetchProjects()
  }
  
  // 加载项目任务列表
  await projectStore.fetchTasks(projectId.value)
})
</script>

<style scoped lang="scss">
.project-detail-view {
  padding: 20px;
  height: 100%;
  overflow: auto;

  h3 {
    margin-bottom: 20px;
    color: #333;
    font-size: 1.3rem;
  }
  
  .back-button {
    margin-bottom: 20px;
  }

  /* 信息卡片 */
  .info-card {
    margin-bottom: 20px;
    
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
  }
  
  .project-info {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
    gap: 20px;
    
    .info-row {
      display: flex;
      align-items: center;
      gap: 16px;
      
      .info-label {
        min-width: 100px;
        font-weight: 600;
        color: #606266;
      }
      
      .info-value {
        flex: 1;
        color: #303133;
        
        &.progress-container {
          display: flex;
          align-items: center;
          gap: 12px;
          
          .progress-text {
            min-width: 50px;
            text-align: right;
          }
        }
      }
    }
  }
  
  /* 任务卡片 */
  .tasks-card {
    margin-bottom: 20px;
    
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
  }

  /* 响应式设计 */
  @media (max-width: 1024px) {
    padding: 16px;
  }

  @media (max-width: 768px) {
    padding: 12px;

    h3 {
      font-size: 1.1rem;
      margin-bottom: 16px;
    }
    
    .project-info {
      grid-template-columns: 1fr;
      gap: 12px;
    }
  }
}
</style>