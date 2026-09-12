<template>
  <div class="gantt-view">
    <h3>甘特图</h3>
    
    <!-- 项目选择 -->
    <div class="project-selector">
      <el-card shadow="hover">
        <div class="selector-content">
          <el-form :model="searchForm" inline>
            <el-form-item label="项目名称">
              <el-select v-model="searchForm.projectId" placeholder="请选择项目">
                <el-option
                  v-for="project in projects"
                  :key="project.id"
                  :label="project.name"
                  :value="project.id"
                ></el-option>
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadGanttData">加载甘特图</el-button>
              <el-button @click="resetFilters">重置</el-button>
            </el-form-item>
          </el-form>
          
          <!-- 甘特图操作按钮 -->
          <div class="gantt-actions">
            <el-button type="primary" size="small" @click="showAddTaskDialog">添加任务</el-button>
            <el-button size="small" @click="deleteTask">删除任务</el-button>
            <el-button size="small" @click="exportGantt">导出甘特图</el-button>
            <el-dropdown>
              <el-button size="small">
              视图 <el-icon class="el-icon--right"><ArrowDown /></el-icon>
            </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="zoomIn">放大</el-dropdown-item>
                  <el-dropdown-item @click="zoomOut">缩小</el-dropdown-item>
                  <el-dropdown-item @click="fitToScreen">适应屏幕</el-dropdown-item>
                  <el-dropdown-item @click="fullScreen">全屏</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </el-card>
    </div>
    
    <!-- 甘特图容器 -->
    <div class="gantt-container">
      <el-card shadow="hover" class="gantt-card">
        <div ref="ganttContainer" class="gantt-chart"></div>
      </el-card>
    </div>
    
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
        <el-form-item label="任务名称" prop="text">
          <el-input
            v-model="addTaskForm.text"
            placeholder="请输入任务名称"
            maxlength="100"
            show-word-limit
          ></el-input>
        </el-form-item>
        <el-form-item label="开始日期" prop="start_date">
          <el-date-picker
            v-model="addTaskForm.start_date"
            type="date"
            placeholder="请选择开始日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          ></el-date-picker>
        </el-form-item>
        <el-form-item label="持续时间(天)" prop="duration">
          <el-input-number
            v-model="addTaskForm.duration"
            :min="1"
            placeholder="请输入持续时间"
          ></el-input-number>
        </el-form-item>
        <el-form-item label="进度" prop="progress">
          <el-slider
            v-model="addTaskForm.progress"
            :min="0"
            :max="100"
            show-input
          ></el-slider>
        </el-form-item>
        <el-form-item label="父任务ID" prop="parent">
          <el-input-number
            v-model="addTaskForm.parent"
            :min="0"
            placeholder="请输入父任务ID，0表示根任务"
          ></el-input-number>
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
        <el-form-item label="任务名称" prop="text">
          <el-input
            v-model="editTaskForm.text"
            placeholder="请输入任务名称"
            maxlength="100"
            show-word-limit
          ></el-input>
        </el-form-item>
        <el-form-item label="开始日期" prop="start_date">
          <el-date-picker
            v-model="editTaskForm.start_date"
            type="date"
            placeholder="请选择开始日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          ></el-date-picker>
        </el-form-item>
        <el-form-item label="持续时间(天)" prop="duration">
          <el-input-number
            v-model="editTaskForm.duration"
            :min="1"
            placeholder="请输入持续时间"
          ></el-input-number>
        </el-form-item>
        <el-form-item label="进度" prop="progress">
          <el-slider
            v-model="editTaskForm.progress"
            :min="0"
            :max="100"
            show-input
          ></el-slider>
        </el-form-item>
        <el-form-item label="父任务ID" prop="parent">
          <el-input-number
            v-model="editTaskForm.parent"
            :min="0"
            placeholder="请输入父任务ID，0表示根任务"
          ></el-input-number>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="resetEditTaskForm">取消</el-button>
          <el-button type="primary" :loading="editTaskLoading" @click="handleEditTask">确定</el-button>
        </span>
      </template>
    </el-dialog>
    
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
          <el-input v-model="taskDetailForm.text" disabled></el-input>
        </el-form-item>
        <el-form-item label="开始日期">
          <el-input v-model="taskDetailForm.start_date" disabled></el-input>
        </el-form-item>
        <el-form-item label="结束日期">
          <el-input v-model="taskDetailForm.end_date" disabled></el-input>
        </el-form-item>
        <el-form-item label="持续时间(天)">
          <el-input v-model="taskDetailForm.duration" disabled></el-input>
        </el-form-item>
        <el-form-item label="进度">
          <el-progress :percentage="taskDetailForm.progress" :stroke-width="10"></el-progress>
        </el-form-item>
        <el-form-item label="父任务ID">
          <el-input v-model="taskDetailForm.parent" disabled></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="resetTaskDetailForm">关闭</el-button>
          <el-button type="primary" @click="showEditTaskDialog(taskDetailForm.id)">编辑任务</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { ArrowDown } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import 'dhtmlx-gantt/codebase/dhtmlxgantt.css'
// 移除不存在的皮肤文件导入
import gantt from 'dhtmlx-gantt'
// 将gantt断言为any类型，解决类型定义问题
const ganttInstance = gantt as any
import { useProjectManagementStore } from '@/stores/plm/projectManagement'

// 初始化项目管理状态
const projectStore = useProjectManagementStore()

// 从状态管理获取项目列表
const projects = computed(() => projectStore.projects)

// 搜索表单
const searchForm = ref({
  projectId: ''
})

/**
 * 将 dhtmlx-gantt 的日期值标准化为 YYYY-MM-DD 字符串
 * @param value gantt 返回的日期值（可能为 Date 或字符串）
 */
const formatGanttDate = (value: any): string => {
  if (!value) return ''
  if (typeof value === 'string') return value.slice(0, 10)
  try {
    const date = value instanceof Date ? value : new Date(value)
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    return `${year}-${month}-${day}`
  } catch (error) {
    return ''
  }
}

/**
 * 持久化当前甘特图任务与连线到后端，并重新渲染到图表
 */
const persistCurrentGanttData = async () => {
  if (!searchForm.value.projectId) return
  const serialized = ganttInstance.serialize?.() || { data: [], links: [] }
  const tasks = (serialized.data || []).map((t: any) => ({
    id: Number(t.id),
    text: String(t.text ?? ''),
    start_date: formatGanttDate(t.start_date),
    duration: Number(t.duration ?? 0),
    progress: Number(t.progress ?? 0),
    parent: Number(t.parent ?? 0),
    type: t.type ? String(t.type) : undefined
  }))
  const links = (serialized.links || []).map((l: any) => ({
    id: Number(l.id),
    source: Number(l.source),
    target: Number(l.target),
    type: String(l.type ?? '0')
  }))

  const ok = await projectStore.saveGanttData(searchForm.value.projectId, { data: tasks, links })
  if (!ok) {
    ElMessage.error('保存甘特图失败')
    return
  }

  await projectStore.fetchGanttData(searchForm.value.projectId)
  ganttInstance.clearAll()
  ganttInstance.parse(projectStore.ganttData)
  ElMessage.success('已保存')
}

// 甘特图容器
const ganttContainer = ref<HTMLElement | null>(null)

// 初始化甘特图
const initGantt = () => {
  if (!ganttContainer.value) return
  
  // 设置甘特图配置
  ganttInstance.config.xml_date = '%Y-%m-%d %H:%i:%s'
  ganttInstance.config.readonly = false
  ganttInstance.config.show_dependencies = true
  ganttInstance.config.show_links = true
  ganttInstance.config.order_branch = true
  ganttInstance.config.order_branch_free = true
  ganttInstance.config.scale_height = 50
  ganttInstance.config.autosize = true
  ganttInstance.config.fit_tasks = true
  ganttInstance.config.row_height = 40
  ganttInstance.config.scale_unit = 'week'
  ganttInstance.config.date_scale = '%m/%d'
  ganttInstance.config.subscales = [
    { unit: 'day', step: 1, date: '%j %D' }
  ]
  
  // 启用上下文菜单
  ganttInstance.plugins({
    marker: true,
    tooltip: true,
    drag_timeline: true,
    fullscreen: true,
    keyboard_nav: true,
    multiselect: true
  })
  
  // 添加任务点击事件
  ganttInstance.attachEvent('onTaskClick', (id: number) => {
    handleTaskClick(id)
  })
  
  // 添加任务双击事件
  ganttInstance.attachEvent('onTaskDblClick', (id: number) => {
    handleTaskDblClick(id)
  })
  
  // 设置甘特图容器
  ganttInstance.init(ganttContainer.value)
}

// 加载甘特图数据
const loadGanttData = async () => {
  if (!searchForm.value.projectId) {
    console.error('请选择项目')
    return
  }
  
  try {
    // 从状态管理获取甘特图数据
    await projectStore.fetchGanttData(searchForm.value.projectId)
    
    // 加载数据到甘特图
    ganttInstance.parse(projectStore.ganttData)
  } catch (error) {
    console.error('加载甘特图数据失败:', error)
  }
}

// 添加任务对话框
const addTaskDialogVisible = ref(false)
const addTaskFormRef = ref<any>(null)
const addTaskLoading = ref(false)

// 添加任务表单数据
const addTaskForm = ref({
  text: '',
  start_date: '',
  duration: 1,
  progress: 0,
  parent: 0
})

// 添加任务表单验证规则
const addTaskRules = ref({
  text: [
    { required: true, message: '请输入任务名称', trigger: 'blur' },
    { min: 2, max: 100, message: '任务名称长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  start_date: [
    { required: true, message: '请选择开始日期', trigger: 'change' }
  ],
  duration: [
    { required: true, message: '请输入持续时间', trigger: 'blur' }
  ],
  progress: [
    { required: true, message: '请输入进度', trigger: 'blur' }
  ]
})

// 显示添加任务对话框
const showAddTaskDialog = () => {
  addTaskDialogVisible.value = true
}

// 重置添加任务表单
const resetAddTaskForm = () => {
  if (addTaskFormRef.value) {
    addTaskFormRef.value.resetFields()
  }
  addTaskForm.value = {
    text: '',
    start_date: '',
    duration: 1,
    progress: 0,
    parent: 0
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
    
    const newTask = {
      id: Date.now(),
      ...addTaskForm.value
    }
    ganttInstance.addTask(newTask, null)
    await persistCurrentGanttData()
    
    // 关闭对话框
    addTaskDialogVisible.value = false
    resetAddTaskForm()
  } catch (error: any) {
    console.error('添加任务失败:', error)
  } finally {
    addTaskLoading.value = false
  }
}

// 删除任务
const deleteTask = () => {
  // 获取选中的任务
  const selectedTask = ganttInstance.getSelectedId()
  if (selectedTask) {
    ElMessageBox.confirm('确定要删除选中的任务吗？此操作不可撤销。', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
      .then(async () => {
        ganttInstance.deleteTask(selectedTask)
        await persistCurrentGanttData()
      })
      .catch(() => {})
  } else {
    ElMessage.warning('请先选择要删除的任务')
  }
}

// 编辑任务对话框
const editTaskDialogVisible = ref(false)
const editTaskFormRef = ref<any>(null)
const editTaskLoading = ref(false)
const editTaskForm = ref({
  id: 0,
  text: '',
  start_date: '',
  duration: 1,
  progress: 0,
  parent: 0
})

// 编辑任务表单验证规则
const editTaskRules = ref({
  text: [
    { required: true, message: '请输入任务名称', trigger: 'blur' },
    { min: 2, max: 100, message: '任务名称长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  start_date: [
    { required: true, message: '请选择开始日期', trigger: 'change' }
  ],
  duration: [
    { required: true, message: '请输入持续时间', trigger: 'blur' }
  ],
  progress: [
    { required: true, message: '请输入进度', trigger: 'blur' }
  ]
})

// 显示编辑任务对话框
const showEditTaskDialog = (taskId: number) => {
  // 获取任务数据
  const task = ganttInstance.getTask(taskId)
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
    text: '',
    start_date: '',
    duration: 1,
    progress: 0,
    parent: 0
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
    
    // 更新任务到甘特图
    ganttInstance.updateTask(editTaskForm.value.id, editTaskForm.value)
    await persistCurrentGanttData()
    
    // 关闭对话框
    editTaskDialogVisible.value = false
    resetEditTaskForm()
  } catch (error: any) {
    console.error('编辑任务失败:', error)
  } finally {
    editTaskLoading.value = false
  }
}

// 任务详情对话框
const taskDetailDialogVisible = ref(false)
const taskDetailForm = ref({
  id: 0,
  text: '',
  start_date: '',
  end_date: '',
  duration: 0,
  progress: 0,
  parent: 0
})

// 重置任务详情表单
const resetTaskDetailForm = () => {
  taskDetailForm.value = {
    id: 0,
    text: '',
    start_date: '',
    end_date: '',
    duration: 0,
    progress: 0,
    parent: 0
  }
  taskDetailDialogVisible.value = false
}

// 查看任务详情
const viewTaskDetail = (taskId: number) => {
  // 获取任务数据
  const task = ganttInstance.getTask(taskId)
  if (task) {
    taskDetailForm.value = { ...task }
    taskDetailDialogVisible.value = true
  }
}

// 甘特图任务点击事件
const handleTaskClick = (id: number) => {
  viewTaskDetail(id)
}

// 甘特图任务双击事件
const handleTaskDblClick = (id: number) => {
  showEditTaskDialog(id)
}

// 导出甘特图
const exportGantt = () => {
  const serialized = ganttInstance.serialize?.() || { data: [], links: [] }
  const content = JSON.stringify(serialized, null, 2)
  const blob = new Blob([content], { type: 'application/json;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `gantt_${searchForm.value.projectId || 'project'}.json`
  link.click()
  URL.revokeObjectURL(url)
}

// 放大
const zoomIn = () => {
  ganttInstance.ext.zoom.zoomIn()
}

// 缩小
const zoomOut = () => {
  ganttInstance.ext.zoom.zoomOut()
}

// 适应屏幕
const fitToScreen = () => {
  ganttInstance.ext.zoom.fit()
}

// 全屏
const fullScreen = () => {
  ganttInstance.fullscreen.toggle()
}

// 重置筛选条件
const resetFilters = () => {
  searchForm.value.projectId = ''
}

// 生命周期钩子
onMounted(async () => {
  // 加载项目列表
  await projectStore.fetchProjects()
  
  // 初始化甘特图
  initGantt()
  
  // 如果有项目，默认选择第一个
  if (projects.value.length > 0 && projects.value[0]) {
    searchForm.value.projectId = projects.value[0].id
    // 加载甘特图数据
    await loadGanttData()
  }
})

onUnmounted(() => {
  // 销毁甘特图实例
  if (ganttContainer.value) {
    ganttInstance.destructor()
  }
})
</script>

<style scoped lang="scss">
.gantt-view {
  padding: 20px;
  height: 100%;
  overflow: auto;

  h3 {
    margin-bottom: 20px;
    color: #333;
    font-size: 1.3rem;
  }

  /* 项目选择器 */
  .project-selector {
    margin-bottom: 20px;
  }

  .selector-content {
    display: flex;
    justify-content: space-between;
    align-items: center;
    flex-wrap: wrap;
    gap: 16px;

    @media (max-width: 768px) {
      flex-direction: column;
      align-items: stretch;
    }
  }

  /* 甘特图操作按钮 */
  .gantt-actions {
    display: flex;
    gap: 8px;
    flex-wrap: wrap;
  }

  /* 甘特图容器 */
  .gantt-container {
    height: calc(100% - 180px);
  }

  .gantt-card {
    height: 100%;
    display: flex;
    flex-direction: column;
  }

  .gantt-chart {
    height: 100%;
    min-height: 500px;
  }

  /* 响应式设计 */
  @media (max-width: 1024px) {
    padding: 16px;

    .gantt-container {
      height: calc(100% - 200px);
    }
  }

  @media (max-width: 768px) {
    padding: 12px;

    h3 {
      font-size: 1.1rem;
      margin-bottom: 16px;
    }

    .project-selector {
      margin-bottom: 16px;
    }

    .gantt-container {
      height: calc(100% - 250px);
    }

    .gantt-chart {
      min-height: 400px;
    }
  }
}
</style>
