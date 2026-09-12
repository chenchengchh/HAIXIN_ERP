import { ref, reactive, computed, onMounted } from 'vue'
import type { DouyinTask, LogItem } from '../types'
import { ElMessage, ElMessageBox } from 'element-plus'
import { douyinApi } from '@/api/douyin'

export function useDouyinTask() {
  // --- State ---
  const tasks = ref<DouyinTask[]>([])
  const isLogged = ref(false)
  const qrCodeUrl = ref('')

  // --- Computed ---

  const totalStats = computed(() => {
    return tasks.value.reduce((acc, task) => {
      const taskStats = task.stats || { videosFound: 0, customersFound: 0, messagesSent: 0 }
      return {
        videosFound: acc.videosFound + taskStats.videosFound,
        customersFound: acc.customersFound + taskStats.customersFound,
        messagesSent: acc.messagesSent + taskStats.messagesSent
      }
    }, { videosFound: 0, customersFound: 0, messagesSent: 0 })
  })

  // --- Actions ---

  const fetchTasks = async () => {
    try {
      const data = await douyinApi.getTasks()
      tasks.value = Array.isArray(data) ? data.map(task => ({
        ...task,
        // 确保id字段为字符串类型，解决前后端类型不匹配问题
        id: String(task.id),
        stats: task.stats || { videosFound: 0, customersFound: 0, messagesSent: 0 }
      })) : []
    } catch (error) {
      console.error('Failed to fetch tasks:', error)
      tasks.value = []
    }
  }

  const checkLoginStatus = async () => {
    try {
      const status = await douyinApi.checkLoginStatus()
      if (status) {
        isLogged.value = !!status.isLogged
        qrCodeUrl.value = status.qrCodeUrl || ''
      }
    } catch (error) {
      console.error('Failed to check login status:', error)
    }
  }

  const addLog = (taskId: string, type: LogItem['type'], message: string) => {
    const task = tasks.value.find(t => t.id === taskId)
    if (task) {
      const time = new Date().toLocaleTimeString()
      task.logs.push({ time, type, message })
      if (task.logs.length > 100) task.logs.shift()
    }
  }

  const createTask = async (taskData: Partial<DouyinTask>) => {
    try {
      const newTask = await douyinApi.createTask(taskData)
      tasks.value.unshift({
        ...newTask,
        // 确保新创建任务的id为字符串类型
        id: String(newTask.id),
        stats: newTask.stats || { videosFound: 0, customersFound: 0, messagesSent: 0 }
      })
      ElMessage.success('任务创建成功')
    } catch (error) {
      ElMessage.error('创建任务失败')
    }
  }

  const updateTask = async (taskId: string, taskData: Partial<DouyinTask>) => {
    try {
      await douyinApi.updateTask(taskId, taskData)
      const index = tasks.value.findIndex(t => t.id === taskId)
      if (index !== -1 && tasks.value[index]) {
        // 获取当前任务数据，并确保TypeScript知道它不是undefined
        const currentTask = tasks.value[index] as DouyinTask
        // 更新任务数据，只更新实际提供的字段
        const updatedTask = {
          ...currentTask,
          ...taskData
        }
        // 确保id始终为字符串
        updatedTask.id = taskId
        // 将更新后的任务重新赋值，使用类型断言确保TypeScript接受
        tasks.value[index] = updatedTask as DouyinTask
      }
      ElMessage.success('任务更新成功')
    } catch (error) {
      ElMessage.error('更新任务失败')
    }
  }

  const deleteTask = (taskId: string) => {
    ElMessageBox.confirm('确定要删除该任务吗?', '提示', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    }).then(async () => {
      try {
        await douyinApi.deleteTask(taskId)
        const index = tasks.value.findIndex(t => t.id === taskId)
        if (index !== -1) {
          tasks.value.splice(index, 1)
        }
        ElMessage.success('任务已删除')
      } catch (error) {
        ElMessage.error('删除任务失败')
      }
    }).catch(() => {})
  }

  const startTask = async (taskId: string) => {
    const task = tasks.value.find(t => t.id === taskId)
    if (!task || task.status === 'running') return
    
    try {
      await douyinApi.startTask(taskId)
      task.status = 'running'
      task.lastRunTime = new Date().toLocaleString()
      addLog(taskId, 'info', '任务已下发至后端执行引擎...')
    } catch (error) {
      ElMessage.error('启动任务失败')
    }
  }

  const stopTask = async (taskId: string) => {
    const task = tasks.value.find(t => t.id === taskId)
    if (!task || task.status !== 'running') return
    
    try {
      await douyinApi.stopTask(taskId)
      task.status = 'stopped'
      addLog(taskId, 'warning', '任务已停止')
    } catch (error) {
      ElMessage.error('停止任务失败')
    }
  }

  onMounted(() => {
    fetchTasks()
    checkLoginStatus()
  })

  return {
    tasks,
    isLogged,
    qrCodeUrl,
    totalStats,
    fetchTasks,
    checkLoginStatus,
    createTask,
    updateTask,
    deleteTask,
    startTask,
    stopTask
  }
}
