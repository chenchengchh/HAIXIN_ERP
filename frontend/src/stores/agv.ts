import { defineStore } from 'pinia'
import { unwrapListResponse, unwrapResponseData } from '@/api'
import agvApi from '../api/agv'
import type { 
  AGV, AGVTask, PathPlan, TrafficNode, TrafficRule, 
  FaultAlert, CollaborationStrategy, CollisionAvoidanceConfig,
  BatteryStatus, OperationStats, CollaborationLog, TaskStatus
} from '../types/agv'

const unwrapApiData = <T = any>(response: any): T => {
  return (unwrapResponseData<T>(response) ?? null) as T
}

const unwrapApiList = <T = any>(response: any): T[] => {
  return unwrapListResponse<T>(response)
}

// 定义AGV状态管理
export const useAGVStore = defineStore('agv', {
  state: () => ({
    // AGV列表
    agvs: [] as AGV[],
    
    // 任务列表
    tasks: [] as AGVTask[],
    
    // 路径规划列表
    pathPlans: [] as PathPlan[],
    
    // 交通节点列表
    trafficNodes: [] as TrafficNode[],
    
    // 交通规则列表
    trafficRules: [] as TrafficRule[],
    
    // 故障告警列表
    faultAlerts: [] as FaultAlert[],
    
    // 协同策略列表
    collaborationStrategies: [] as CollaborationStrategy[],
    
    // 冲突避免配置
    collisionAvoidanceConfig: {} as CollisionAvoidanceConfig,
    
    // 电池状态列表
    batteryStatuses: [] as BatteryStatus[],
    
    // 运行统计
    operationStats: {} as OperationStats,
    
    // 协同日志
    collaborationLogs: [] as CollaborationLog[],
    
    // 当前选中的AGV
    selectedAGV: null as string | null,
    
    // 当前选中的任务
    selectedTask: null as string | null,
    
    // 系统状态
    systemStatus: 'normal' as 'normal' | 'warning' | 'error' | 'maintenance'
  }),

  getters: {
    // 获取所有AGV数量
    totalAGVCount: (state) => state.agvs.length,
    
    // 获取活跃AGV数量
    activeAGVCount: (state) => state.agvs.filter(agv => agv.status === 'running' || agv.status === 'idle').length,
    
    // 获取运行中AGV数量
    runningAGVCount: (state) => state.agvs.filter(agv => agv.status === 'running').length,
    
    // 获取充电中AGV数量
    chargingAGVCount: (state) => state.agvs.filter(agv => agv.status === 'charging').length,
    
    // 获取故障AGV数量
    faultAGVCount: (state) => state.agvs.filter(agv => agv.status === 'fault').length,
    
    // 获取运行中任务数量
    runningTaskCount: (state) => state.tasks.filter(task => task.status === 'running' || task.status === 'assigned').length,
    
    // 获取待处理任务数量
    pendingTaskCount: (state) => state.tasks.filter(task => task.status === 'pending').length,
    
    // 获取已完成任务数量
    completedTaskCount: (state) => state.tasks.filter(task => task.status === 'completed').length,
    
    // 获取待处理告警数量
    pendingAlertCount: (state) => state.faultAlerts.filter(alert => alert.status === 'pending').length,
    
    // 获取指定AGV的任务历史
    getAGVTaskHistory: (state) => (agvCode: string) => {
      return state.tasks.filter(task => task.agvCode === agvCode).sort((a, b) => {
        return new Date(b.createTime).getTime() - new Date(a.createTime).getTime()
      })
    },
    
    // 获取指定AGV的电池状态
    getAGVBatteryStatus: (state) => (agvCode: string) => {
      return state.batteryStatuses.find(status => status.agvCode === agvCode)
    },
    
    // 获取当前活跃的协同策略
    activeCollaborationStrategy: (state) => {
      return state.collaborationStrategies.find(strategy => strategy.status === true)
    }
  },

  actions: {
    /**
     * 加载AGV列表（真实后端接口）
     */
    async loadAGVs() {
      try {
        const response = await agvApi.getAGVs()
        this.agvs = unwrapApiList<AGV>(response)
      } catch (error) {
        console.error('加载AGV列表失败:', error)
        this.agvs = []
      }
    },
    
    /**
     * 加载任务列表（真实后端接口）
     */
    async loadTasks() {
      try {
        const response = await agvApi.getTasks()
        this.tasks = unwrapApiList<AGVTask>(response)
      } catch (error) {
        console.error('加载任务列表失败:', error)
        this.tasks = []
      }
    },
    
    /**
     * 创建新任务（真实后端接口）
     */
    async createTask(task: Omit<AGVTask, 'id' | 'taskNo' | 'createTime' | 'startTime' | 'endTime' | 'trackStatus' | 'progress' | 'estimatedCompletion'>) {
      try {
        const response = await agvApi.createTask(task as any)
        const created = unwrapApiData<AGVTask>(response)
        this.tasks.push(created)
        return created
      } catch (error) {
        console.error('创建任务失败:', error)
        throw error
      }
    },
    
    /**
     * 更新任务状态（真实后端接口）
     */
    async updateTaskStatus(taskId: string, status: TaskStatus) {
      try {
        const response = await agvApi.updateTaskStatus(taskId, status)
        const updated = unwrapApiData<AGVTask>(response)
        const idx = this.tasks.findIndex(t => t.id === taskId || t.taskNo === taskId)
        if (idx >= 0) this.tasks[idx] = updated
      } catch (error) {
        console.error('更新任务状态失败:', error)
      }
    },

    async updateTaskPriority(taskId: string, priority: AGVTask['priority']) {
      try {
        const response = await agvApi.updateTaskPriority(taskId, priority)
        const updated = unwrapApiData<AGVTask>(response)
        const idx = this.tasks.findIndex(t => t.id === taskId || t.taskNo === taskId)
        if (idx >= 0) this.tasks[idx] = updated
      } catch (error) {
        console.error('更新任务优先级失败:', error)
      }
    },

    async cancelTask(taskId: string) {
      try {
        const response = await agvApi.cancelTask(taskId)
        const updated = unwrapApiData<AGVTask>(response)
        const idx = this.tasks.findIndex(t => t.id === taskId || t.taskNo === taskId)
        if (idx >= 0) this.tasks[idx] = updated
      } catch (error) {
        console.error('取消任务失败:', error)
      }
    },
    
    /**
     * 分配任务给AGV（真实后端接口）
     */
    async assignTaskToAGV(taskId: string, agvCode: string) {
      try {
        const response = await agvApi.assignTaskToAGV(taskId, agvCode)
        const updated = unwrapApiData<AGVTask>(response)
        const idx = this.tasks.findIndex(t => t.id === taskId || t.taskNo === taskId)
        if (idx >= 0) this.tasks[idx] = updated
      } catch (error) {
        console.error('分配任务失败:', error)
      }
    },
    
    /**
     * 加载路径规划列表（真实后端接口）
     */
    async loadPathPlans() {
      try {
        const response = await agvApi.getPathPlans()
        this.pathPlans = unwrapApiList<PathPlan>(response)
      } catch (error) {
        console.error('加载路径规划失败:', error)
        this.pathPlans = []
      }
    },
    
    /**
     * 创建新路径规划（真实后端接口）
     */
    async createPathPlan(pathPlan: Omit<PathPlan, 'id' | 'createTime'>) {
      try {
        const response = await agvApi.createPathPlan(pathPlan as any)
        const created = unwrapApiData<PathPlan>(response)
        this.pathPlans.push(created)
        return created
      } catch (error) {
        console.error('创建路径规划失败:', error)
        throw error
      }
    },
    
    /**
     * 加载交通节点（真实后端接口）
     */
    async loadTrafficNodes() {
      try {
        const response = await agvApi.getTrafficNodes()
        this.trafficNodes = unwrapApiList<TrafficNode>(response)
      } catch (error) {
        console.error('加载交通节点失败:', error)
        this.trafficNodes = []
      }
    },
    
    /**
     * 释放交通节点（真实后端接口）
     */
    async releaseTrafficNode(nodeId: number) {
      try {
        await agvApi.releaseTrafficNode(nodeId)
        await this.loadTrafficNodes()
      } catch (error) {
        console.error('释放交通节点失败:', error)
      }
    },
    
    /**
     * 加载交通规则（真实后端接口）
     */
    async loadTrafficRules() {
      try {
        const response = await agvApi.getTrafficRules()
        this.trafficRules = unwrapApiList<TrafficRule>(response)
      } catch (error) {
        console.error('加载交通规则失败:', error)
        this.trafficRules = []
      }
    },
    
    /**
     * 切换交通规则状态（真实后端接口）
     */
    async toggleTrafficRuleStatus(ruleId: number) {
      try {
        await agvApi.toggleTrafficRuleStatus(ruleId)
        await this.loadTrafficRules()
      } catch (error) {
        console.error('切换交通规则状态失败:', error)
      }
    },
    
    /**
     * 加载故障告警（真实后端接口）
     */
    async loadFaultAlerts() {
      try {
        const response = await agvApi.getFaultAlerts()
        this.faultAlerts = unwrapApiList<FaultAlert>(response)
      } catch (error) {
        console.error('加载故障告警失败:', error)
        this.faultAlerts = []
      }
    },
    
    /**
     * 处理故障告警（真实后端接口）
     */
    async handleFaultAlert(alertId: number, handleResult: string, operator: string = '系统') {
      try {
        await agvApi.handleFaultAlert(alertId, handleResult, operator)
        await this.loadFaultAlerts()
      } catch (error) {
        console.error('处理故障告警失败:', error)
      }
    },
    
    /**
     * 加载协同策略（真实后端接口）
     */
    async loadCollaborationStrategies() {
      try {
        const response = await agvApi.getCollaborationStrategies()
        this.collaborationStrategies = unwrapApiList<CollaborationStrategy>(response)
      } catch (error) {
        console.error('加载协同策略失败:', error)
        this.collaborationStrategies = []
      }
    },
    
    /**
     * 激活协同策略（真实后端接口）
     */
    async activateCollaborationStrategy(strategyId: string) {
      try {
        await agvApi.activateCollaborationStrategy(strategyId)
        await this.loadCollaborationStrategies()
      } catch (error) {
        console.error('激活协同策略失败:', error)
      }
    },
    
    /**
     * 加载冲突避免配置（真实后端接口）
     */
    async loadCollisionAvoidanceConfig() {
      try {
        const response = await agvApi.getCollisionAvoidanceConfig()
        this.collisionAvoidanceConfig = unwrapApiData<CollisionAvoidanceConfig>(response)
      } catch (error) {
        console.error('加载冲突避免配置失败:', error)
        this.collisionAvoidanceConfig = {} as CollisionAvoidanceConfig
      }
    },
    
    /**
     * 更新冲突避免配置（真实后端接口）
     */
    async updateCollisionAvoidanceConfig(config: Partial<CollisionAvoidanceConfig>) {
      try {
        const response = await agvApi.updateCollisionAvoidanceConfig(config)
        this.collisionAvoidanceConfig = unwrapApiData<CollisionAvoidanceConfig>(response)
      } catch (error) {
        console.error('更新冲突避免配置失败:', error)
      }
    },
    
    /**
     * 加载电池状态（真实后端接口）
     */
    async loadBatteryStatuses() {
      try {
        const response = await agvApi.getBatteryStatuses()
        this.batteryStatuses = unwrapApiList<BatteryStatus>(response)
      } catch (error) {
        console.error('加载电池状态失败:', error)
        this.batteryStatuses = []
      }
    },
    
    /**
     * 加载运行统计（真实后端接口）
     */
    async loadOperationStats() {
      try {
        const response = await agvApi.getOperationStats()
        this.operationStats = unwrapApiData<OperationStats>(response)
      } catch (error) {
        console.error('加载运行统计失败:', error)
        this.operationStats = {} as OperationStats
      }
    },
    
    /**
     * 加载协同日志（真实后端接口）
     */
    async loadCollaborationLogs() {
      try {
        const response = await agvApi.getCollaborationLogs()
        this.collaborationLogs = unwrapApiList<CollaborationLog>(response)
      } catch (error) {
        console.error('加载协同日志失败:', error)
        this.collaborationLogs = []
      }
    },
    
    /**
     * 发送充电指令（真实后端接口）
     */
    async sendChargeCommand(agvCode: string) {
      try {
        await agvApi.sendChargeCommand(agvCode)
        await this.loadAGVs()
      } catch (error) {
        console.error('发送充电指令失败:', error)
      }
    },
    
    /**
     * 紧急停止所有AGV（真实后端接口）
     */
    async emergencyStopAllAGVs() {
      try {
        await agvApi.emergencyStopAllAGVs()
        this.systemStatus = 'error'
        await this.loadAGVs()
      } catch (error) {
        console.error('紧急停止失败:', error)
      }
    },
    
    async pauseAllAGVs() {
      try {
        await agvApi.pauseAllAGVs()
        await this.loadAGVs()
      } catch (error) {
        console.error('暂停所有AGV失败:', error)
      }
    },

    /**
     * 恢复所有AGV运行（真实后端接口）
     */
    async resumeAllAGVs() {
      try {
        await agvApi.resumeAllAGVs()
        this.systemStatus = 'normal'
        await this.loadAGVs()
      } catch (error) {
        console.error('恢复运行失败:', error)
      }
    },
    
    // 设置选中的AGV
    setSelectedAGV(agvCode: string | null) {
      this.selectedAGV = agvCode
    },
    
    // 设置选中的任务
    setSelectedTask(taskId: string | null) {
      this.selectedTask = taskId
    }
  }
})
