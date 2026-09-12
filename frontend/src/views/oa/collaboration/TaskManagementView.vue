<template>
  <div class="task-management-view">
    <!-- 任务统计概览 -->
    <div class="stats-section">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-content">
              <div class="stat-label">待办任务</div>
              <div class="stat-value">{{ stats.todo }}</div>
              <div class="stat-change">+5 个任务</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-content">
              <div class="stat-label">进行中</div>
              <div class="stat-value">{{ stats.doing }}</div>
              <div class="stat-change">+2 个任务</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-content">
              <div class="stat-label">已完成</div>
              <div class="stat-value">{{ stats.done }}</div>
              <div class="stat-change">+8 个任务</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-content">
              <div class="stat-label">延期任务</div>
              <div class="stat-value">{{ stats.overdue }}</div>
              <div class="stat-change">+1 个任务</div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 任务管理操作区 -->
    <div class="operation-section">
      <el-button type="primary" @click="showCreateTaskDialog = true">
        <el-icon><Plus /></el-icon> 新建任务
      </el-button>
      <el-select v-model="filterStatus" placeholder="状态" clearable style="width: 120px; margin-left: 10px;">
        <el-option label="全部" value="" />
        <el-option label="待办" value="todo" />
        <el-option label="进行中" value="doing" />
        <el-option label="已完成" value="done" />
        <el-option label="已取消" value="cancelled" />
      </el-select>
      <el-select v-model="filterPriority" placeholder="优先级" clearable style="width: 120px; margin-left: 10px;">
        <el-option label="全部" value="" />
        <el-option label="低" value="low" />
        <el-option label="中" value="medium" />
        <el-option label="高" value="high" />
        <el-option label="紧急" value="urgent" />
      </el-select>
      <el-input
        v-model="searchKeyword"
        placeholder="搜索任务名称或描述"
        clearable
        style="width: 300px; margin-left: 10px;"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
    </div>

    <!-- 任务列表 -->
    <div class="task-list-section">
      <el-card shadow="hover">
        <template #header>
          <div class="card-header">
            <span>任务列表</span>
          </div>
        </template>
        <el-table :data="filteredTasks" stripe style="width: 100%">
          <el-table-column prop="taskCode" label="任务编号" width="120" />
          <el-table-column prop="taskName" label="任务名称" min-width="200">
            <template #default="scope">
              <div class="task-name" @click="showTaskDetail(scope.row)">
                {{ scope.row.taskName }}
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="assigneeName" label="执行人" width="100" />
          <el-table-column prop="priority" label="优先级" width="80">
            <template #default="scope">
              <el-tag :type="priorityTypeMap[scope.row.priority]">{{ scope.row.priority }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="scope">
              <el-tag :type="statusTypeMap[scope.row.status]">{{ scope.row.status }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="dueDate" label="截止日期" width="150" />
          <el-table-column prop="progress" label="进度" width="120">
            <template #default="scope">
              <el-progress :percentage="scope.row.progress" size="small" />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="scope">
              <el-button size="small" @click="showTaskDetail(scope.row)">详情</el-button>
              <el-button size="small" type="primary" @click="editTask(scope.row)">编辑</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>

    <!-- 创建/编辑任务对话框 -->
    <el-dialog
      v-model="showCreateTaskDialog"
      title="{{ editingTask ? '编辑任务' : '新建任务' }}"
      width="600px"
    >
      <el-form :model="taskForm" label-width="100px">
        <el-form-item label="任务名称" required>
          <el-input v-model="taskForm.taskName" placeholder="请输入任务名称" />
        </el-form-item>
        <el-form-item label="任务描述">
          <el-input
            v-model="taskForm.description"
            type="textarea"
            placeholder="请输入任务描述"
            :rows="4"
          />
        </el-form-item>
        <el-form-item label="执行人" required>
          <el-select v-model="taskForm.assigneeId" placeholder="请选择执行人">
            <el-option
              v-for="user in users"
              :key="user.id"
              :label="user.name"
              :value="user.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级" required>
          <el-select v-model="taskForm.priority" placeholder="请选择优先级">
            <el-option label="低" value="low" />
            <el-option label="中" value="medium" />
            <el-option label="高" value="high" />
            <el-option label="紧急" value="urgent" />
          </el-select>
        </el-form-item>
        <el-form-item label="截止日期" required>
          <el-date-picker
            v-model="taskForm.dueDate"
            type="date"
            placeholder="选择截止日期"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showCreateTaskDialog = false">取消</el-button>
          <el-button type="primary" @click="saveTask">保存</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 任务详情对话框 -->
    <el-dialog
      v-model="showTaskDetailDialog"
      title="任务详情"
      width="800px"
    >
      <div v-if="selectedTask" class="task-detail">
        <div class="detail-header">
          <h3>{{ selectedTask.taskName }}</h3>
          <el-tag :type="priorityTypeMap[selectedTask.priority]">{{ selectedTask.priority }}</el-tag>
        </div>
        <div class="detail-info">
          <div class="info-item">
            <span class="info-label">任务编号：</span>
            <span class="info-value">{{ selectedTask.taskCode }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">执行人：</span>
            <span class="info-value">{{ selectedTask.assigneeName }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">截止日期：</span>
            <span class="info-value">{{ selectedTask.dueDate }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">状态：</span>
            <el-tag :type="statusTypeMap[selectedTask.status]">{{ selectedTask.status }}</el-tag>
          </div>
          <div class="info-item">
            <span class="info-label">进度：</span>
            <el-progress :percentage="selectedTask.progress" style="width: 200px; margin-left: 10px;" />
          </div>
        </div>
        <div class="detail-description">
          <h4>任务描述</h4>
          <p>{{ selectedTask.description }}</p>
        </div>
        <div class="detail-comments">
          <h4>评论</h4>
          <el-input
            v-model="newComment"
            type="textarea"
            placeholder="添加评论"
            :rows="3"
            style="margin-bottom: 10px;"
          />
          <el-button type="primary" @click="addComment">添加评论</el-button>
          <div class="comments-list" style="margin-top: 20px;">
            <div v-for="comment in selectedTask.comments" :key="comment.id" class="comment-item">
              <div class="comment-header">
                <span class="comment-user">{{ comment.userName }}</span>
                <span class="comment-time">{{ comment.createTime }}</span>
              </div>
              <div class="comment-content">{{ comment.content }}</div>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { Plus, Search } from '@element-plus/icons-vue'

// 定义任务类型
interface Task {
  id: number
  taskCode: string
  taskName: string
  description: string
  projectId: number
  assignerId: number
  assigneeId: number
  assigneeName: string
  priority: 'low' | 'medium' | 'high' | 'urgent'
  status: 'todo' | 'doing' | 'review' | 'done' | 'cancelled'
  dueDate: string
  progress: number
  tags: string
  parentTaskId: number | null
  comments: Array<{
    id: number
    userName: string
    content: string
    createTime: string
  }>
}

// 定义用户类型
interface User {
  id: number
  name: string
}

// 模拟数据 - 任务统计
const stats = ref({
  todo: 12,
  doing: 8,
  done: 25,
  overdue: 3
})

// 模拟数据 - 用户列表
const users = ref<User[]>([
  { id: 1, name: '张三' },
  { id: 2, name: '李四' },
  { id: 3, name: '王五' },
  { id: 4, name: '赵六' }
])

// 模拟数据 - 任务列表
const tasks = ref<Task[]>([
  {
    id: 1,
    taskCode: 'TASK001',
    taskName: '完成OA系统文档管理模块开发',
    description: '开发OA系统中的文档管理模块，包括上传、下载、版本控制等功能',
    projectId: 1,
    assignerId: 1,
    assigneeId: 2,
    assigneeName: '李四',
    priority: 'high',
    status: 'doing',
    dueDate: '2025-12-20',
    progress: 60,
    tags: 'OA,文档管理',
    parentTaskId: null,
    comments: [
      { id: 1, userName: '张三', content: '任务进度如何？', createTime: '2025-12-15 10:30' },
      { id: 2, userName: '李四', content: '正在进行中，预计后天完成', createTime: '2025-12-15 14:20' }
    ]
  },
  {
    id: 2,
    taskCode: 'TASK002',
    taskName: '实现流程审批的会签功能',
    description: '在OA系统流程审批模块中实现会签功能，支持多人同时审批',
    projectId: 1,
    assignerId: 1,
    assigneeId: 3,
    assigneeName: '王五',
    priority: 'urgent',
    status: 'todo',
    dueDate: '2025-12-18',
    progress: 0,
    tags: 'OA,流程审批',
    parentTaskId: null,
    comments: []
  },
  {
    id: 3,
    taskCode: 'TASK003',
    taskName: '设计协同办公模块UI',
    description: '设计OA系统协同办公模块的UI界面，包括即时通讯、日程管理等',
    projectId: 1,
    assignerId: 1,
    assigneeId: 4,
    assigneeName: '赵六',
    priority: 'medium',
    status: 'done',
    dueDate: '2025-12-15',
    progress: 100,
    tags: 'OA,UI设计',
    parentTaskId: null,
    comments: [
      { id: 1, userName: '张三', content: '设计稿已确认，可以开始开发', createTime: '2025-12-15 09:15' }
    ]
  },
  {
    id: 4,
    taskCode: 'TASK004',
    taskName: '集成文件存储服务',
    description: '将MinIO文件存储服务集成到OA系统中，用于文档存储',
    projectId: 1,
    assignerId: 1,
    assigneeId: 2,
    assigneeName: '李四',
    priority: 'high',
    status: 'doing',
    dueDate: '2025-12-22',
    progress: 45,
    tags: 'OA,文件存储',
    parentTaskId: null,
    comments: []
  },
  {
    id: 5,
    taskCode: 'TASK005',
    taskName: '测试OA系统移动端适配',
    description: '测试OA系统在移动端的适配情况，确保响应式设计正常工作',
    projectId: 1,
    assignerId: 1,
    assigneeId: 3,
    assigneeName: '王五',
    priority: 'medium',
    status: 'todo',
    dueDate: '2025-12-25',
    progress: 0,
    tags: 'OA,移动端',
    parentTaskId: null,
    comments: []
  }
])

// 筛选条件
const filterStatus = ref('')
const filterPriority = ref('')
const searchKeyword = ref('')

// 优先级类型映射
const priorityTypeMap: Record<string, string> = {
  low: 'default',
  medium: 'warning',
  high: 'danger',
  urgent: 'danger'
}

// 状态类型映射
const statusTypeMap: Record<string, string> = {
  todo: 'info',
  doing: 'warning',
  review: 'warning',
  done: 'success',
  cancelled: 'danger'
}

// 计算筛选后的任务列表
const filteredTasks = computed(() => {
  return tasks.value.filter(task => {
    // 状态筛选
    if (filterStatus.value && task.status !== filterStatus.value) {
      return false
    }
    // 优先级筛选
    if (filterPriority.value && task.priority !== filterPriority.value) {
      return false
    }
    // 关键词搜索
    if (searchKeyword.value) {
      const keyword = searchKeyword.value.toLowerCase()
      return (
        task.taskName.toLowerCase().includes(keyword) ||
        task.description.toLowerCase().includes(keyword)
      )
    }
    return true
  })
})

// 新建任务对话框
const showCreateTaskDialog = ref(false)

// 任务详情对话框
const showTaskDetailDialog = ref(false)
const selectedTask = ref<Task | null>(null)

// 编辑任务状态
const editingTask = ref(false)

// 任务表单
const taskForm = ref({
  taskName: '',
  description: '',
  assigneeId: 0,
  priority: 'medium' as 'low' | 'medium' | 'high' | 'urgent',
  dueDate: ''
})

// 新增评论
const newComment = ref('')

// 打开创建任务对话框
const openCreateTaskDialog = () => {
  editingTask.value = false
  taskForm.value = {
    taskName: '',
    description: '',
    assigneeId: 0,
    priority: 'medium',
    dueDate: ''
  }
  showCreateTaskDialog.value = true
}

// 编辑任务
const editTask = (task: Task) => {
  editingTask.value = true
  taskForm.value = {
    taskName: task.taskName,
    description: task.description,
    assigneeId: task.assigneeId,
    priority: task.priority,
    dueDate: task.dueDate
  }
  showCreateTaskDialog.value = true
}

// 保存任务
const saveTask = () => {
  // 这里可以添加表单验证和保存逻辑
  console.log('保存任务:', taskForm.value)
  showCreateTaskDialog.value = false
}

// 显示任务详情
const showTaskDetail = (task: Task) => {
  selectedTask.value = task
  showTaskDetailDialog.value = true
}

// 添加评论
const addComment = () => {
  if (newComment.value && selectedTask.value) {
    const newCommentItem = {
      id: Date.now(),
      userName: '当前用户',
      content: newComment.value,
      createTime: new Date().toLocaleString()
    }
    selectedTask.value.comments.push(newCommentItem)
    newComment.value = ''
  }
}
</script>

<style scoped>
.task-management-view {
  padding: 20px;
  height: 100%;
  box-sizing: border-box;
  background-color: #f5f7fa;
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
  margin-bottom: 4px;
}

.stat-change {
  font-size: 12px;
  color: #67c23a;
}

.operation-section {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
}

.task-list-section {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.task-name {
  color: #409eff;
  cursor: pointer;
}

.task-name:hover {
  text-decoration: underline;
}

.detail-header {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.detail-header h3 {
  margin: 0;
  margin-right: 20px;
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
  width: 80px;
  font-weight: bold;
}

.detail-description {
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.detail-description h4 {
  margin: 0 0 10px 0;
}

.detail-comments {
  margin-top: 20px;
}

.detail-comments h4 {
  margin: 0 0 15px 0;
}

.comment-item {
  margin-bottom: 15px;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 5px;
  font-size: 14px;
}

.comment-user {
  font-weight: bold;
  color: #409eff;
}

.comment-time {
  color: #909399;
  font-size: 12px;
}

.comment-content {
  font-size: 14px;
}
</style>
