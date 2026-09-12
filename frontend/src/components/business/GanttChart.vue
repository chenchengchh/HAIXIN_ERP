<template>
  <div class="gantt-chart">
    <!-- 实时更新状态指示器（仅在配置了WebSocket地址时显示，避免无后端WS时误导性"已断开"） -->
    <div v-if="props.wsUrl" class="realtime-status" :class="{ 'active': isRealtimeEnabled && isConnected }">
      <el-tag size="small" :type="isConnected ? 'success' : 'danger'">
        {{ isConnected ? '实时连接已建立' : '实时连接已断开' }}
      </el-tag>
      <el-switch
        v-model="isRealtimeEnabled"
        size="small"
        active-text="开启"
        inactive-text="关闭"
        @change="toggleRealtime"
        style="margin-left: 10px;"
      />
    </div>
    
    <!-- 甘特图容器 -->
    <div ref="ganttContainer" class="gantt-container"></div>
    
    <!-- 工具栏 -->
    <div class="gantt-toolbar">
      <el-button type="primary" size="small" @click="refreshData" :loading="isRefreshing">刷新数据</el-button>
      <el-button type="success" size="small" @click="zoomIn">放大</el-button>
      <el-button type="info" size="small" @click="zoomOut">缩小</el-button>
      <el-button type="warning" size="small" @click="fitToScreen">适应屏幕</el-button>
      <el-select v-model="timeScale" size="small" @change="changeTimeScale" style="margin-left: 10px;">
        <el-option label="小时" value="hour"></el-option>
        <el-option label="日" value="day"></el-option>
        <el-option label="周" value="week"></el-option>
        <el-option label="月" value="month"></el-option>
      </el-select>
      
      <!-- 任务筛选 -->
      <el-select v-model="taskFilter" size="small" placeholder="筛选任务状态" style="margin-left: 10px;">
        <el-option label="全部" value=""></el-option>
        <el-option label="已计划" value="PLANNED"></el-option>
        <el-option label="执行中" value="EXECUTING"></el-option>
        <el-option label="已完成" value="COMPLETED"></el-option>
        <el-option label="已延迟" value="DELAYED"></el-option>
      </el-select>
      
      <!-- 快速定位 -->
      <el-button type="info" size="small" @click="locateToToday" style="margin-left: 10px;">定位到今天</el-button>
    </div>
    
    <!-- 实时更新日志 -->
    <div v-if="props.wsUrl && isRealtimeEnabled" class="realtime-log">
      <el-divider content-position="left">更新日志</el-divider>
      <div class="log-content">
        <el-scrollbar height="100px">
          <div v-for="(log, index) in realtimeLogs" :key="index" class="log-item">
            <span class="log-time">{{ log.time }}</span>
            <span class="log-message" :class="log.type">{{ log.message }}</span>
          </div>
        </el-scrollbar>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, watch, computed } from 'vue'
import 'dhtmlx-gantt/codebase/dhtmlxgantt.css'
import gantt from 'dhtmlx-gantt'
// 导入WebSocket工具
import { useWebSocket, WebSocketStatus } from '../../utils/websocket'

// 本地定义需要的类型，避免依赖dhtmlx-gantt的类型定义
type GanttTask = {
  id: number | string
  text: string
  start_date: string | Date
  end_date: string | Date
  duration: number
  resource_name?: string
  status: string
  priority: number
  [key: string]: any
}

type GanttLink = {
  id: number | string
  source: number | string
  target: number | string
  type: number
  [key: string]: any
}

type GanttConfig = {
  [key: string]: any
}

// 定义props
const props = defineProps<{
  tasks: GanttTask[]
  links: GanttLink[]
  planId: string | number
  wsUrl?: string
}>()

// 定义事件
const emit = defineEmits<{
  'taskSelected': [taskId: string | number]
  'taskUpdated': [task: GanttTask]
  'task-dblclicked': [taskId: string | number]
  'task-added': [task: any]
  'task-deleted': [taskId: string | number]
  'task-dragged': [taskId: string | number, mode: string]
  'linkUpdated': [link: GanttLink]
  'link-added': [link: any]
  'link-updated': [link: any]
  'link-deleted': [linkId: string | number]
  'dataRefreshed': []
  'realtimeEvent': [event: any]
  'task-progress-changed': [task: GanttTask]
  'task-priority-changed': [task: GanttTask]
  'task-resource-changed': [task: GanttTask]
}>()

// 甘特图容器
const ganttContainer = ref<HTMLElement | null>(null)

// 状态管理
const isRefreshing = ref(false)
const isConnected = ref(false)
const isRealtimeEnabled = ref(true)
const timeScale = ref<string>('day')
const taskFilter = ref<string>('')
const realtimeLogs = ref<Array<{ time: string; message: string; type: string }>>([])

// WebSocket实例
let wsInstance: ReturnType<typeof useWebSocket> | null = null

// 是否已完成首次"适应屏幕"（避免后续筛选/刷新重复缩放）
let hasFitOnce = false

// 初始化甘特图
const initGantt = () => {
  if (!ganttContainer.value) {
    handleError(new Error('甘特图容器不存在'), '初始化甘特图')
    return
  }
  
  try {
    // 确保容器有正确的高度
    if (ganttContainer.value && !ganttContainer.value.style.height) {
      ganttContainer.value.style.height = '600px'
    }

    // v9中扩展能力需通过plugins注册（自动排程/键盘导航/多选）
    (gantt as any).plugins({
      auto_scheduling: true,
      keyboard_navigation: true,
      multiselect: true
    })

    // 配置甘特图，使用any类型避免依赖dhtmlx-gantt的类型定义。
    // 注意：v9中事件回调不能放在config里（会被静默忽略），需在init前通过attachEvent注册
    const config: any = {
      // 列配置
      columns: [
        { name: 'text', label: '任务名称', width: 200, tree: true },
        { name: 'start_date', label: '开始时间', align: 'center' },
        { name: 'end_date', label: '结束时间', align: 'center' },
        { name: 'duration', label: '持续时间', width: 80, align: 'center' },
        { name: 'resource_name', label: '资源', width: 120 },
        { name: 'status', label: '状态', width: 100, align: 'center' },
        { name: 'priority', label: '优先级', width: 80, align: 'center' }
      ],

      // 时间刻度
      scales: [
        { unit: 'day', step: 1, format: '%Y-%m-%d' },
        { unit: 'hour', step: 1, format: '%H:00' }
      ],

      // 日期显示格式（任务已为Date对象，此配置控制列内文本格式化）
      date_format: '%Y-%m-%d %H:%i',

      // 持续时间按小时计算（与后端gantt-data返回的duration单位一致，避免按天取整显示0）
      duration_unit: 'hour',

      // 任务条配置
      task_height: 40,
      row_height: 40,
      bar_height: 30,

      // 网格宽度固定，超出部分网格内横向滚动，保证右侧时间轴有足够展示空间
      grid_width: 460,
      grid_elastic_columns: true,

      // 只读模式
      readonly: false,

      // 自动安排
      auto_scheduling: true,
      auto_scheduling_strict: true,

      // 启用任务依赖编辑
      drag_links: true,

      // 确保甘特图正确渲染
      container_width: '100%',
      container_height: '100%'
    }

    // 应用配置（末尾必须带分号：下一行以(gantt as any)开头，TS类型擦除后变成(gantt)，
    // 缺少分号会被合并解析为函数调用 Object.assign(...)(gantt).init(...) 导致运行时错误）
    Object.assign((gantt as any).config, config);

    // 注册事件回调（v9写法：attachEvent，init之前注册）
    (gantt as any).attachEvent('onTaskClick', (id: string | number) => {
      emit('taskSelected', id)
      return true
    });
    (gantt as any).attachEvent('onTaskDblClick', (id: string | number) => {
      emit('task-dblclicked', id)
      return true
    });
    (gantt as any).attachEvent('onAfterTaskUpdate', (id: string | number, task: any) => {
      emit('taskUpdated', task)
      return true
    });
    (gantt as any).attachEvent('onAfterTaskAdd', (id: string | number, task: any) => {
      emit('task-added', task)
      return true
    });
    (gantt as any).attachEvent('onAfterTaskDelete', (id: string | number) => {
      emit('task-deleted', id)
      return true
    });
    (gantt as any).attachEvent('onAfterLinkAdd', (id: string | number, link: any) => {
      emit('link-added', link)
      return true
    });
    (gantt as any).attachEvent('onAfterLinkUpdate', (id: string | number, link: any) => {
      emit('link-updated', link)
      return true
    });
    (gantt as any).attachEvent('onAfterLinkDelete', (id: string | number, link: any) => {
      emit('link-deleted', id)
      return true
    });
    (gantt as any).attachEvent('onAfterTaskDrag', (id: string | number, mode: string) => {
      emit('task-dragged', id, mode)
      return true
    });

    // 自定义任务状态样式（v9写法：直接设置templates，init之前生效）
    (gantt as any).templates.task_class = (start: Date, end: Date, task: any) => {
      let cssClass = ''
      if (task.status === 'in_progress') {
        cssClass += 'task-executing'
      } else if (task.status === 'done') {
        cssClass += 'task-completed'
      } else if (task.status === 'delayed') {
        cssClass += 'task-delayed'
      }
      return cssClass
    };

    // 初始化甘特图
    (gantt as any).init(ganttContainer.value);

    // 加载数据
    loadData()

    // 确保甘特图按容器大小渲染
    setTimeout(() => {
      (gantt as any).render()
    }, 100)
  } catch (e) {
    handleError(e as Error, '初始化甘特图')
  }
}

// 加载数据
const loadData = () => {
  if (!ganttContainer.value) return

  try {
    // 应用任务筛选
    let filteredTasks = [...props.tasks]
    if (taskFilter.value) {
      filteredTasks = filteredTasks.filter(task => task.status === taskFilter.value)
    }

    // 日期格式适配：后端返回ISO字符串(如2026-01-08T08:00:00)，
    // dhtmlx-gantt默认按"%d-%m-%Y %H:%i"解析会报"Invalid start_date argument"，
    // parse前统一转为Date对象
    const parsedTasks = filteredTasks.map(t => ({
      ...t,
      start_date: t.start_date ? new Date(t.start_date) : undefined,
      end_date: t.end_date ? new Date(t.end_date) : undefined
    }));

    // 使用类型断言处理Gantt对象
    // 启用静默渲染，提升大量数据加载时的性能
    (gantt as any).config.silent_rendering = true;
    (gantt as any).clearAll();
    (gantt as any).parse({
      data: parsedTasks,
      links: props.links
    });
    // 禁用静默渲染，恢复正常交互
    (gantt as any).config.silent_rendering = false;

    // 更新渲染
    (gantt as any).render()

    // 首次加载到任务后自动适应屏幕，让可视范围覆盖全部任务
    if (!hasFitOnce && parsedTasks.length > 0) {
      hasFitOnce = true
      fitToScreen()
    }
  } catch (e) {
    handleError(e as Error, '加载甘特图数据')
  }
}

// 更新甘特图数据（全量更新）
const updateData = () => {
  if (!ganttContainer.value) return
  loadData()
}

// 错误处理函数
const handleError = (error: any, context: string) => {
  console.error(`${context}出错:`, error)
  const errorMessage = error instanceof Error ? error.message : String(error)
  addRealtimeLog(`${context}出错: ${errorMessage}`, 'error')
}

// 部分数据更新（添加/更新/删除单个任务或链接）
const updatePartialData = (update: {
  type: 'add' | 'update' | 'delete'
  data: { tasks?: any[]; links?: any[] }
}) => {
  if (!ganttContainer.value) return
  
  const { type, data } = update
  
  if (data.tasks) {
    data.tasks.forEach(task => {
      switch (type) {
        case 'add':
          (gantt as any).addTask(task)
          addRealtimeLog(`任务 "${task.text}" 已添加`, 'info')
          break
        case 'update':
          (gantt as any).updateTask(task.id, task)
          addRealtimeLog(`任务 "${task.text}" 已更新`, 'success')
          break
        case 'delete':
          (gantt as any).deleteTask(task.id)
          addRealtimeLog(`任务 "${task.text}" 已删除`, 'warning')
          break
      }
    })
  }
  
  if (data.links) {
    data.links.forEach(link => {
      switch (type) {
        case 'add':
          (gantt as any).addLink(link)
          addRealtimeLog(`任务依赖关系已添加`, 'info')
          break
        case 'update':
          (gantt as any).updateLink(link.id, link)
          addRealtimeLog(`任务依赖关系已更新`, 'success')
          break
        case 'delete':
          (gantt as any).deleteLink(link.id)
          addRealtimeLog(`任务依赖关系已删除`, 'warning')
          break
      }
    })
  }
  
  // 触发实时事件
  emit('realtimeEvent', update)
}

// 刷新数据
const refreshData = async () => {
  try {
    isRefreshing.value = true
    emit('dataRefreshed')
    addRealtimeLog('手动刷新数据完成', 'success')
  } finally {
    isRefreshing.value = false
  }
}

// 时间刻度序列（粒度由细到粗），缩放按钮在序列上步进
const SCALE_LEVELS = ['hour', 'day', 'week', 'month'] as const

// 放大：时间刻度调细一档（v9中zoom能力在ext.zoom且需预配levels，这里复用时间刻度切换实现）
const zoomIn = () => {
  const idx = SCALE_LEVELS.indexOf(timeScale.value as (typeof SCALE_LEVELS)[number])
  const nextIdx = Math.max(0, (idx === -1 ? 1 : idx) - 1)
  timeScale.value = SCALE_LEVELS[nextIdx] ?? 'day'
  changeTimeScale()
}

// 缩小：时间刻度调粗一档
const zoomOut = () => {
  const idx = SCALE_LEVELS.indexOf(timeScale.value as (typeof SCALE_LEVELS)[number])
  const nextIdx = Math.min(SCALE_LEVELS.length - 1, (idx === -1 ? 1 : idx) + 1)
  timeScale.value = SCALE_LEVELS[nextIdx] ?? 'day'
  changeTimeScale()
}

// 适应屏幕：v9无fitToContent，手动将可视范围设置为全部任务的最早开始~最晚结束
const fitToScreen = () => {
  if (!props.tasks.length) return
  const starts = props.tasks
    .map(t => new Date(t.start_date).getTime())
    .filter(n => !Number.isNaN(n))
  const ends = props.tasks
    .map(t => new Date(t.end_date).getTime())
    .filter(n => !Number.isNaN(n))
  if (!starts.length || !ends.length) return
  (gantt as any).config.start_date = new Date(Math.min(...starts));
  (gantt as any).config.end_date = new Date(Math.max(...ends));
  (gantt as any).render()
}

// 定位到今天
const locateToToday = () => {
  const today = new Date();
  (gantt as any).showDate(today);
}

// 改变时间刻度
const changeTimeScale = () => {
  switch (timeScale.value) {
    case 'hour':
      // 使用类型断言来处理dhtmlx-gantt的类型问题
      (gantt as any).config.scales = [
        { unit: 'day', step: 1, format: '%Y-%m-%d' },
        { unit: 'hour', step: 1, format: '%H:00' }
      ]
      break
    case 'day':
      (gantt as any).config.scales = [
        { unit: 'week', step: 1, format: '%Y年第%W周' },
        { unit: 'day', step: 1, format: '%m-%d' }
      ]
      break
    case 'week':
      (gantt as any).config.scales = [
        { unit: 'month', step: 1, format: '%Y-%m' },
        { unit: 'week', step: 1, format: '第%W周' }
      ]
      break
    case 'month':
      (gantt as any).config.scales = [
        { unit: 'year', step: 1, format: '%Y年' },
        { unit: 'month', step: 1, format: '%m月' }
      ]
      break
  }
  // 使用类型断言来处理dhtmlx-gantt的类型问题
  (gantt as any).render()
}


// 初始化WebSocket连接
const initWebSocket = () => {
  if (!props.wsUrl) {
    console.warn('未提供WebSocket URL，无法建立连接')
    addRealtimeLog('未提供WebSocket URL，无法建立实时连接', 'warning')
    return
  }
  
  try {
    // 使用WebSocket工具创建连接
    wsInstance = useWebSocket(props.wsUrl, {
      onOpen: () => {
        isConnected.value = true
        addRealtimeLog('WebSocket连接已建立', 'success')
      },
      onMessage: (message) => {
        try {
          // 解析消息数据
          const data = JSON.parse(message.data)
          
          // 处理不同类型的消息
          switch (data.type) {
            case 'taskUpdate':
              handleTaskUpdate(data.payload)
              break
            case 'scheduleUpdate':
              handleScheduleUpdate(data.payload)
              break
            case 'resourceUpdate':
              handleResourceUpdate(data.payload)
              break
            case 'statusUpdate':
              handleStatusUpdate(data.payload)
              break
            default:
              console.log('收到未知类型的WebSocket消息:', data)
          }
          
          // 触发实时事件
          emit('realtimeEvent', data)
        } catch (error) {
          handleError(error as Error, '解析WebSocket消息')
        }
      },
      onClose: () => {
        isConnected.value = false
        addRealtimeLog('WebSocket连接已关闭', 'warning')
        // 尝试自动重连
        setTimeout(() => {
          if (isRealtimeEnabled.value && !isConnected.value) {
            addRealtimeLog('尝试重新连接WebSocket...', 'info')
            initWebSocket()
          }
        }, 5000)
      },
      onError: (error) => {
        isConnected.value = false
        handleError(error as unknown as Error, 'WebSocket连接')
      }
    })
  } catch (e) {
    handleError(e as Error, '初始化WebSocket')
    isConnected.value = false
  }
}

// 关闭WebSocket连接
const closeWebSocket = () => {
  if (wsInstance) {
    wsInstance.close()
    wsInstance = null
  }
  isConnected.value = false
  addRealtimeLog('WebSocket连接已关闭', 'info')
}

// 处理任务更新
const handleTaskUpdate = (taskData: any) => {
  // 更新任务数据
  const index = props.tasks.findIndex(task => task.id === taskData.id)
  if (index !== -1) {
    // 保存旧任务数据，用于比较变化
    const oldTask = props.tasks[index]
    const updatedTask = { ...oldTask, ...taskData }
    // 由于props是只读的，我们需要通过emit通知父组件更新数据
    emit('taskUpdated', updatedTask)
    addRealtimeLog(`任务 "${taskData.text}" 已更新`, 'success')
    
    // 检测任务属性变化，触发相应事件
    if (oldTask && updatedTask) {
      if (oldTask.priority !== updatedTask.priority) {
        emit('task-priority-changed', updatedTask)
      }
      if (oldTask.resource_name !== updatedTask.resource_name) {
        emit('task-resource-changed', updatedTask)
      }
    }
    // 可以在这里添加更多属性变化检测
  } else {
    // 添加新任务，通过emit通知父组件
    emit('task-added', taskData)
    addRealtimeLog(`任务 "${taskData.text}" 已添加`, 'info')
  }
  
  // 更新甘特图
  updateData()
}

// 任务进度调整
const updateTaskProgress = (taskId: string | number, progress: number) => {
  const taskIndex = props.tasks.findIndex(task => task.id === taskId)
  if (taskIndex !== -1) {
    const task = props.tasks[taskIndex]
    if (task && task.id) {
      const updatedTask: GanttTask = { ...task, progress, id: task.id }
      // 使用类型断言解决emit事件类型问题
      emit('task-progress-changed', updatedTask)
      emit('taskUpdated', updatedTask)
      updateData()
    }
  }
}

// 任务拆分功能
const splitTask = (taskId: string | number, splitTime: Date) => {
  console.log('拆分任务:', taskId, splitTime)
  // 这里应该实现任务拆分逻辑
  // 1. 找到要拆分的任务
  const task = props.tasks.find(task => task.id === taskId)
  if (!task) return
  
  // 2. 创建两个新任务
  // 3. 更新甘特图数据
  // 4. 通过emit通知父组件
  emit('task-added', { /* 新任务1数据 */ })
  emit('task-added', { /* 新任务2数据 */ })
  emit('task-deleted', taskId)
}

// 任务合并功能
const mergeTasks = (taskIds: Array<string | number>) => {
  console.log('合并任务:', taskIds)
  // 这里应该实现任务合并逻辑
  // 1. 找到要合并的任务
  const tasksToMerge = props.tasks.filter(task => taskIds.includes(task.id))
  if (tasksToMerge.length < 2) return
  
  // 2. 创建一个新任务
  // 3. 删除旧任务
  // 4. 通过emit通知父组件
  emit('task-added', { /* 合并后的新任务数据 */ })
  taskIds.forEach(id => emit('task-deleted', id))
}

// 资源分配调整
const updateResourceAssignment = (taskId: string | number, resourceId: string | number, resourceName: string) => {
  const taskIndex = props.tasks.findIndex(task => task.id === taskId)
  const task = props.tasks[taskIndex];
  if (task) {
    const updatedTask: GanttTask = { ...task, resource_id: resourceId, resource_name: resourceName }
    emit('task-resource-changed', updatedTask)
    emit('taskUpdated', updatedTask)
    updateData()
  }
}

// 处理排程更新
const handleScheduleUpdate = (scheduleData: any) => {
  // 更新整个排程数据
  addRealtimeLog('排程数据已更新', 'success')
  // 由于props是只读的，我们需要通过emit通知父组件更新数据
  // 在实际应用中，应该有一个专门的事件来处理整个排程数据的更新
  // 这里我们可以通过dataRefreshed事件通知父组件重新获取数据
  emit('dataRefreshed')
  updateData()
}

// 处理资源更新
const handleResourceUpdate = (resourceData: any) => {
  // 更新资源相关数据
  addRealtimeLog(`资源 "${resourceData.name}" 已更新`, 'info')
  // 可以添加资源数据更新逻辑
}

// 处理状态更新
const handleStatusUpdate = (statusData: any) => {
  // 更新系统状态
  addRealtimeLog(`系统状态更新: ${statusData.message}`, 'info')
  // 可以添加系统状态更新逻辑
}

// 切换实时更新
const toggleRealtime = () => {
  if (isRealtimeEnabled.value) {
    initWebSocket()
  } else {
    closeWebSocket()
  }
}

// 添加实时日志
const addRealtimeLog = (message: string, type: string = 'info') => {
  const now = new Date()
  const time = now.toLocaleTimeString()
  
  realtimeLogs.value.unshift({
    time,
    message,
    type
  })
  
  // 保留最近20条日志
  if (realtimeLogs.value.length > 20) {
    realtimeLogs.value = realtimeLogs.value.slice(0, 20)
  }
}

// 监听数据变化
watch(
  () => [props.tasks, props.links],
  () => {
    updateData()
  },
  { deep: true }
)

// 监听任务筛选变化
watch(
  () => taskFilter.value,
  () => {
    updateData()
  }
)

// 组件挂载时初始化
onMounted(() => {
  initGantt()
  // 仅在配置了WebSocket地址时建立实时连接，避免空地址告警
  if (isRealtimeEnabled.value && props.wsUrl) {
    initWebSocket()
  }
})

// 组件卸载前清理
onBeforeUnmount(() => {
  // 清理甘特图实例
  if (ganttContainer.value) {
    try {
      // 销毁甘特图实例，释放资源（v9方法名为destructor）
      (gantt as any).destructor()
    } catch (e) {
      handleError(e as Error, '销毁甘特图实例')
    }
  }
  
  // 关闭WebSocket连接
  closeWebSocket()
})
</script>

<style scoped>
/* 固定像素高度：打破 .gantt-chart(100%) ↔ 父级flex 的高度循环依赖，
   否则dhtmlx-gantt的resize watcher每轮渲染叠加补偿，容器持续增长导致页面"无限下拉" */
.gantt-chart {
  width: 100%;
  height: 720px;
  display: flex;
  flex-direction: column;
  box-sizing: border-box;
}

.gantt-container {
  width: 100%;
  flex: 1;
  min-height: 0; /* 允许在固定高度父容器内收缩，flex:1才不会被内容撑大 */
  overflow: hidden;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  margin: 10px 0;
  box-sizing: border-box;
}

.gantt-toolbar {
  margin-top: 10px;
  display: flex;
  align-items: center;
  justify-content: flex-start;
  flex-wrap: wrap;
  gap: 5px;
  box-sizing: border-box;
  padding: 5px 0;
}

.realtime-status {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  margin-bottom: 10px;
  box-sizing: border-box;
  padding: 0 5px;
}

.realtime-log {
  margin-top: 10px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  padding: 10px;
  box-sizing: border-box;
  background-color: #ffffff;
}

.log-content {
  max-height: 100px;
  overflow-y: auto;
  box-sizing: border-box;
}

.log-item {
  display: flex;
  margin-bottom: 5px;
  font-size: 12px;
  box-sizing: border-box;
}

.log-time {
  color: #909399;
  margin-right: 10px;
  min-width: 80px;
  box-sizing: border-box;
}

.log-message {
  flex: 1;
  box-sizing: border-box;
  word-break: break-word;
}

.log-message.info {
  color: #409eff;
}

.log-message.success {
  color: #67c23a;
}

.log-message.warning {
  color: #e6a23c;
}

.log-message.error {
  color: #f56c6c;
}

/* 确保甘特图容器正确渲染 */
.gantt-container :deep(.gantt_grid_head_cell) {
  box-sizing: border-box;
}

.gantt-container :deep(.gantt_task_cell) {
  box-sizing: border-box;
}

.gantt-container :deep(.gantt_task_row) {
  box-sizing: border-box;
}

/* 修复dhtmlx-gantt的样式冲突 */
.gantt-container :deep(.gantt_task_content) {
  box-sizing: border-box;
  font-size: 12px;
  padding: 2px 4px;
}

.gantt-container :deep(.gantt_task_line) {
  box-sizing: border-box;
  border-radius: 2px;
}

.gantt-container :deep(.gantt_link_line) {
  box-sizing: border-box;
}

/* 自定义任务样式 */
.gantt-container :deep(.task-executing) {
  background-color: #409eff !important;
  color: #ffffff;
}

.gantt-container :deep(.task-completed) {
  background-color: #67c23a !important;
  color: #ffffff;
}

.gantt-container :deep(.task-delayed) {
  background-color: #f56c6c !important;
  color: #ffffff;
}

/* 任务进度样式 */
.gantt-container :deep(.gantt_task_progress) {
  box-sizing: border-box;
  background-color: rgba(255, 255, 255, 0.3);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .gantt-toolbar {
    gap: 5px;
    padding: 5px 0;
    flex-wrap: wrap;
  }
  
  .gantt-toolbar .el-button {
    font-size: 12px;
    padding: 4px 8px;
  }
  
  .gantt-toolbar .el-select {
    font-size: 12px;
    width: auto;
    min-width: 120px;
  }
  
  .realtime-status {
    margin-bottom: 8px;
    padding: 0 5px;
    flex-direction: column;
    align-items: flex-end;
    gap: 5px;
  }
  
  .realtime-status .el-tag {
    font-size: 11px;
  }
  
  .realtime-status .el-switch {
    margin-left: 0;
  }
  
  .realtime-log {
    margin-top: 8px;
    padding: 8px;
  }
  
  .log-item {
    font-size: 11px;
    margin-bottom: 4px;
  }
  
  .log-time {
    margin-right: 8px;
    min-width: 70px;
  }
  
  .gantt-container {
    margin: 8px 0;
  }
}

@media (max-width: 480px) {
  .gantt-toolbar {
    justify-content: space-around;
    flex-wrap: wrap;
  }
  
  .gantt-toolbar .el-button {
    font-size: 11px;
    padding: 3px 6px;
    margin: 2px 0;
    flex: 1 1 auto;
    min-width: 80px;
  }
  
  .gantt-toolbar .el-select {
    font-size: 11px;
    width: 100%;
    margin: 2px 0;
  }
  
  .realtime-status {
    justify-content: center;
    flex-direction: row;
    flex-wrap: wrap;
    gap: 10px;
    margin-bottom: 5px;
  }
  
  .realtime-status .el-tag {
    font-size: 10px;
  }
  
  .realtime-status .el-switch {
    margin-left: 5px;
    transform: scale(0.8);
  }
  
  .gantt-container :deep(.gantt_grid_head_cell) {
    padding: 5px 2px;
    font-size: 10px;
  }
  
  .gantt-container :deep(.gantt_task_content) {
    font-size: 10px;
    padding: 1px 2px;
  }
  
  .gantt-container :deep(.gantt_task_line) {
    height: 20px !important;
  }
  
  .realtime-log {
    margin-top: 5px;
    padding: 5px;
  }
  
  .log-content {
    max-height: 80px;
  }
  
  .log-item {
    font-size: 10px;
    margin-bottom: 3px;
  }
  
  .log-time {
    margin-right: 5px;
    min-width: 60px;
  }
}
</style>
