<template>
  <div class="douyin-active-dashboard">
    <!-- 顶部统计卡片 -->
    <div class="stats-row">
      <el-alert v-if="!isLogged" title="检测到未登录，请点击右侧按钮进行扫码登录" type="warning" show-icon style="margin-bottom: 15px">
         <template #default>
            <el-button type="primary" link @click="checkLoginStatus">刷新状态</el-button>
            <el-button type="primary" link @click="showLoginModal = true">扫码登录</el-button>
         </template>
      </el-alert>
      <el-row :gutter="20">
        <el-col :span="6">
          <StatCard label="当前任务数" :value="tasks.length" icon="List" type="primary" />
        </el-col>
        <el-col :span="6">
          <StatCard label="运行中任务" :value="runningTasksCount" icon="VideoPlay" type="success" />
        </el-col>
        <el-col :span="6">
          <StatCard label="今日采集视频" :value="totalStats.videosFound" icon="VideoCamera" type="warning" />
        </el-col>
        <el-col :span="6">
          <StatCard label="今日触达人数" :value="totalStats.messagesSent" icon="Message" type="danger" />
        </el-col>
      </el-row>
    </div>

    <!-- 任务列表区域 -->
    <el-card class="task-table-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="title">采集任务列表</span>
          <el-button type="primary" icon="Plus" @click="openCreateDrawer">新建任务</el-button>
        </div>
      </template>

      <div class="table-header">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索任务名称或关键词"
          clearable
          style="width: 300px; margin-bottom: 15px"
          @keyup.enter="handleSearch"
        >
          <template #append>
            <el-button @click="handleSearch"><el-icon><Search /></el-icon></el-button>
          </template>
        </el-input>
      </div>

      <el-table 
        :data="filteredTasks" 
        style="width: 100%" 
        v-loading="isLoading"
        @sort-change="handleSortChange"
      >
        <el-table-column prop="name" label="任务名称" min-width="180" show-overflow-tooltip sortable="custom" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" effect="light">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="关键词" min-width="150" show-overflow-tooltip>
          <template #default="{ row }">
            <el-tag size="small" type="info">{{ row.searchKeyword }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="视频数" width="100" align="center">
          <template #default="{ row }">
            {{ row.stats.videosFound }}
          </template>
        </el-table-column>
        <el-table-column label="客户数" width="100" align="center">
          <template #default="{ row }">
            {{ row.stats.customersFound }}
          </template>
        </el-table-column>
        <el-table-column label="触达数" width="100" align="center">
          <template #default="{ row }">
            {{ row.stats.messagesSent }}
          </template>
        </el-table-column>
        <el-table-column prop="lastRunTime" label="上次运行" width="180" sortable="custom" />
        <el-table-column label="创建时间" width="180" prop="createTime" sortable="custom" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button 
              link 
              :type="row.status === 'running' ? 'danger' : 'success'" 
              @click="toggleTaskStatus(row)"
            >
              {{ row.status === 'running' ? '停止' : '启动' }}
            </el-button>
            <el-button link type="primary" @click="openDetail(row)">详情</el-button>
            <el-button link type="primary" @click="openEditDrawer(row)">编辑</el-button>
            <el-button link type="warning" @click="openLogPanel(row)">日志</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="table-footer" v-if="tasks.length > 0">
        <el-pagination
          v-model:current-page="pagination.currentPage"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="tasks.length"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 弹窗组件 -->
    <!-- 详情弹窗 -->
    <el-drawer v-model="detailVisible" title="采集详情" size="60%">
      <el-tabs v-model="activeDetailTab">
        <el-tab-pane label="采集视频" name="videos">
          <el-table :data="currentVideos" style="width: 100%" height="500">
             <el-table-column prop="title" label="标题" show-overflow-tooltip />
             <el-table-column prop="author" label="作者" width="120" />
             <el-table-column prop="crawledTime" label="采集时间" width="160">
               <template #default="{row}">{{ new Date(row.crawledTime).toLocaleString() }}</template>
             </el-table-column>
             <el-table-column label="操作" width="80">
               <template #default="{row}">
                 <el-link type="primary" :href="row.videoUrl" target="_blank">查看</el-link>
               </template>
             </el-table-column>
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="意向客户" name="customers">
           <el-table :data="currentCustomers" style="width: 100%" height="500">
             <el-table-column prop="nickname" label="昵称" width="120" />
             <el-table-column prop="commentContent" label="评论内容" show-overflow-tooltip />
             <el-table-column prop="matchKeyword" label="命中词" width="100">
               <template #default="{row}">
                 <el-tag size="small">{{ row.matchKeyword }}</el-tag>
               </template>
             </el-table-column>
             <el-table-column prop="status" label="状态" width="100">
               <template #default="{row}">
                 <el-tag size="small" :type="row.status === 'sent' ? 'success' : (row.status === 'converted' ? 'warning' : 'info')">{{ row.status }}</el-tag>
               </template>
             </el-table-column>
             <el-table-column label="操作" width="220" fixed="right">
               <template #default="{row}">
                 <el-button 
                   v-if="row.status === 'pending'"
                   link type="primary" size="small"
                   @click="handleUpdateStatus(row, 'sent')"
                 >标记已发</el-button>
                 <el-button 
                   v-if="row.status !== 'converted'"
                   link type="success" size="small"
                   @click="handleConvert(row)"
                 >转为客户</el-button>
                 <el-button 
                   v-if="row.status !== 'failed' && row.status !== 'converted'"
                   link type="danger" size="small"
                   @click="handleUpdateStatus(row, 'failed')"
                 >标记无效</el-button>
               </template>
             </el-table-column>
           </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-drawer>

    <TaskDrawer 
      v-model="drawerVisible" 
      :task-data="currentTask"
      @save="handleSaveTask" 
    />

    <LogPanel 
      v-model="logVisible" 
      :logs="currentLogs"
      :task-name="currentLogTaskName"
    />

    <!-- 登录二维码弹窗 -->
    <el-dialog v-model="showLoginModal" title="抖音扫码登录" width="300px" center>
      <div style="text-align: center;">
        <img :src="qrCodeUrl || 'https://via.placeholder.com/200'" alt="QR Code" style="width: 200px; height: 200px; margin-bottom: 10px;" />
        <p style="color: #666; font-size: 13px;">请使用抖音APP扫码登录</p>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { Plus, List, VideoPlay, VideoCamera, Message, Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useDouyinTask } from './hooks/useDouyinTask'
import { douyinApi } from '@/api/douyin'
import StatCard from './components/StatCard.vue'
import TaskDrawer from './components/TaskDrawer.vue'
import LogPanel from './components/LogPanel.vue'
import type { DouyinTask, Customer, VideoItem } from './types'

const { 
  tasks, 
  isLogged,
  qrCodeUrl,
  totalStats,
  fetchTasks,
  createTask, 
  updateTask, 
  deleteTask, 
  startTask, 
  stopTask,
  checkLoginStatus
} = useDouyinTask()

// UI State
const showLoginModal = ref(false)
const drawerVisible = ref(false)
const logVisible = ref(false)
const detailVisible = ref(false)
const activeDetailTab = ref('videos')
const currentVideos = ref<VideoItem[]>([])
const currentCustomers = ref<Customer[]>([])
const currentTask = ref<DouyinTask | null>(null)
const currentLogTaskName = ref('')
const currentLogs = ref<any[]>([])
const isLoading = ref(false)

// 搜索和排序状态
const searchKeyword = ref('')
const sortField = ref<string>('createTime')
const sortOrder = ref<'ascending' | 'descending'>('descending')

// 分页状态
const pagination = ref({
  currentPage: 1,
  pageSize: 10
})

// Computed
const runningTasksCount = computed(() => tasks.value.filter(t => t.status === 'running').length)

// 过滤和排序后的任务列表
const filteredTasks = computed(() => {
  let result = [...tasks.value]
  
  // 搜索过滤
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(task => 
      task.name.toLowerCase().includes(keyword) || 
      task.searchKeyword.toLowerCase().includes(keyword)
    )
  }
  
  // 排序
  if (sortField.value) {
    result.sort((a, b) => {
      const aVal = a[sortField.value as keyof DouyinTask]
      const bVal = b[sortField.value as keyof DouyinTask]
      
      if (aVal === null || aVal === undefined) return sortOrder.value === 'ascending' ? -1 : 1
      if (bVal === null || bVal === undefined) return sortOrder.value === 'ascending' ? 1 : -1
      
      let comparison = 0
      if (typeof aVal === 'string' && typeof bVal === 'string') {
        comparison = aVal.localeCompare(bVal)
      } else if (typeof aVal === 'number' && typeof bVal === 'number') {
        comparison = aVal - bVal
      } else if (aVal instanceof Date && bVal instanceof Date) {
        comparison = aVal.getTime() - bVal.getTime()
      }
      
      return sortOrder.value === 'ascending' ? comparison : -comparison
    })
  }
  
  // 分页
  const startIndex = (pagination.value.currentPage - 1) * pagination.value.pageSize
  const endIndex = startIndex + pagination.value.pageSize
  result = result.slice(startIndex, endIndex)
  
  return result
})

// Helpers
const getStatusType = (status: string) => {
  const map: Record<string, string> = {
    running: 'success',
    stopped: 'info',
    completed: 'success',
    error: 'danger'
  }
  return map[status] || 'info'
}

const getStatusText = (status: string) => {
  const map: Record<string, string> = {
    running: '运行中',
    stopped: '已停止',
    completed: '已完成',
    error: '异常'
  }
  return map[status] || status
}

// Handlers
const openCreateDrawer = () => {
  currentTask.value = null
  drawerVisible.value = true
}

const openEditDrawer = (task: DouyinTask) => {
  currentTask.value = task
  drawerVisible.value = true
}

const openDetail = async (task: DouyinTask) => {
  detailVisible.value = true
  activeDetailTab.value = 'videos'
  try {
    isLoading.value = true
    const [videos, customers] = await Promise.all([
      douyinApi.getTaskVideos(task.id),
      douyinApi.getTaskCustomers(task.id)
    ])
    currentVideos.value = videos
    currentCustomers.value = customers
  } catch (e) {
    console.error(e)
    ElMessage.error('获取详情失败')
  } finally {
    isLoading.value = false
  }
}

const handleSaveTask = (taskData: Partial<DouyinTask>) => {
  if (currentTask.value) {
    updateTask(currentTask.value.id, taskData)
  } else {
    createTask(taskData)
  }
}

const toggleTaskStatus = (task: DouyinTask) => {
  if (task.status === 'running') {
    stopTask(task.id)
  } else {
    startTask(task.id)
  }
}

const handleUpdateStatus = async (row: Customer, status: string) => {
  try {
    await douyinApi.updateCustomerStatus(row.id, status)
    row.status = status as any
    ElMessage.success('状态更新成功')
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

const handleConvert = async (row: Customer) => {
  try {
    await douyinApi.convertCustomer(row.id)
    row.status = 'converted'
    ElMessage.success('转化成功')
  } catch (e) {
    ElMessage.error('转化失败，可能已转化')
  }
}

const openLogPanel = (task: DouyinTask) => {
  currentLogTaskName.value = task.name
  // Reactive binding to logs
  currentLogs.value = task.logs
  logVisible.value = true
}

const handleDelete = (row: DouyinTask) => {
  ElMessageBox.confirm('确定要删除该任务吗？删除后将无法恢复。', '删除确认', {
    confirmButtonText: '确定删除',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    deleteTask(row.id)
  }).catch(() => {
    // 取消删除
  })
}

// 搜索和排序处理
const handleSearch = () => {
  pagination.value.currentPage = 1
}

const handleSortChange = ({ prop, order }: { prop: string, order: 'ascending' | 'descending' | null }) => {
  if (prop && order) {
    sortField.value = prop
    sortOrder.value = order
  }
}

// 分页处理
const handleSizeChange = (size: number) => {
  pagination.value.pageSize = size
  pagination.value.currentPage = 1
}

const handleCurrentChange = (current: number) => {
  pagination.value.currentPage = current
}

// 定时刷新任务列表（每30秒）
const startAutoRefresh = () => {
  const interval = setInterval(() => {
    fetchTasks()
  }, 30000)
  
  return () => clearInterval(interval)
}

// 页面挂载时启动自动刷新
const stopAutoRefresh = startAutoRefresh()

// 组件卸载时停止自动刷新
watch(
  () => detailVisible.value,
  (newVal) => {
    if (newVal) {
      // 打开详情页时停止自动刷新
      stopAutoRefresh()
    } else {
      // 关闭详情页时重新启动自动刷新
      startAutoRefresh()
    }
  }
)
</script>

<style scoped lang="scss">
.douyin-active-dashboard {
  height: 100%;
  display: flex;
  flex-direction: column;
  gap: 20px;
  padding: 0 20px 20px 20px;
  background-color: #f5f7fa;
}

.stats-row {
  margin-bottom: 0;
}

.task-table-card {
  flex: 1;
  display: flex;
  flex-direction: column;
  
  :deep(.el-card__body) {
    flex: 1;
    padding: 20px;
  }
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .title {
      font-size: 16px;
      font-weight: 600;
      color: #303133;
    }
  }
  
  .table-header {
    margin-bottom: 15px;
    display: flex;
    justify-content: flex-start;
    align-items: center;
  }
  
  .table-footer {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
    align-items: center;
  }
}

.stats-mini {
  font-size: 12px;
  color: #303133;
  font-weight: 500;
  display: flex;
  align-items: center;
}

/* 优化卡片样式 */
:deep(.el-card) {
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

/* 优化按钮样式 */
:deep(.el-button) {
  border-radius: 4px;
  font-size: 14px;
}

/* 优化表格样式 */
:deep(.el-table) {
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

:deep(.el-table__header-wrapper) {
  background-color: #fafafa;
  border-bottom: 1px solid #ebeef5;
}

:deep(.el-table__header tr th) {
  background-color: #fafafa;
  font-weight: 600;
  color: #303133;
  border-bottom: 1px solid #ebeef5;
}

:deep(.el-table__body tr) {
  cursor: pointer;
  transition: all 0.3s;
}

:deep(.el-table__body tr:hover) {
  background-color: #f5f7fa;
}
</style>
