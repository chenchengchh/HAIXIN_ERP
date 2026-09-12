// AGV系统API服务
import request from '../index'
import type { 
  AGV, AGVTask, PathPlan, TrafficNode, TrafficRule, 
  FaultAlert, CollaborationStrategy, CollisionAvoidanceConfig,
  BatteryStatus, OperationStats, CollaborationLog
} from '../../types/agv'

// AGV管理API
export const agvApi = {
  // 获取AGV列表
  getAGVs: () => request.get<AGV[]>('/agv/list'),
  
  // 获取AGV详情
  getAGVDetail: (agvCode: string) => {
    return request.get<AGV>(`/agv/${agvCode}`)
  },
  
  // 更新AGV状态
  updateAGVStatus: (agvCode: string, status: AGV['status']) => {
    return request.put(`/agv/${agvCode}/status`, { status })
  },
  
  // 获取AGV实时位置
  getAGVPosition: (agvCode: string) => {
    return request.get<{ position: string; speed: number; direction: string }>(`/agv/${agvCode}/position`)
  },
  
  // 任务管理API
  getTasks: () => {
    return request.get<AGVTask[]>('/agv/tasks')
  },
  
  // 获取任务详情
  getTaskDetail: (taskId: string) => {
    return request.get<AGVTask>(`/agv/tasks/${taskId}`)
  },
  
  // 创建任务
  createTask: (task: Omit<AGVTask, 'id' | 'taskNo' | 'createTime' | 'startTime' | 'endTime' | 'trackStatus' | 'progress' | 'estimatedCompletion'>) => {
    return request.post<AGVTask>('/agv/tasks', task)
  },
  
  // 更新任务状态
  updateTaskStatus: (taskId: string, status: AGVTask['status']) => {
    return request.put(`/agv/tasks/${taskId}/status`, { status })
  },

  updateTaskPriority: (taskId: string, priority: AGVTask['priority']) => {
    return request.put(`/agv/tasks/${taskId}/priority`, { priority })
  },
  
  // 分配任务给AGV
  assignTaskToAGV: (taskId: string, agvCode: string) => {
    return request.put(`/agv/tasks/${taskId}/assign`, { agvCode })
  },
  
  // 取消任务
  cancelTask: (taskId: string) => {
    return request.put(`/agv/tasks/${taskId}/cancel`)
  },
  
  // 路径规划API
  getPathPlans: () => request.get<PathPlan[]>('/agv/path-plans'),
  
  // 获取路径规划详情
  getPathPlanDetail: (planId: string) => request.get<PathPlan>(`/agv/path-plans/${planId}`),
  
  // 创建路径规划
  createPathPlan: (plan: Omit<PathPlan, 'id' | 'createTime'>) => request.post<PathPlan>('/agv/path-plans', plan),
  
  // 优化路径
  optimizePath: (planId: string) => {
    return request.post<PathPlan>(`/agv/path-plans/${planId}/optimize`)
  },
  
  // 交通管制API
  getTrafficNodes: () => {
    return request.get<TrafficNode[]>('/agv/traffic/nodes')
  },
  
  // 获取交通节点详情
  getTrafficNodeDetail: (nodeId: number) => {
    return request.get<TrafficNode>(`/agv/traffic/nodes/${nodeId}`)
  },
  
  // 释放交通节点
  releaseTrafficNode: (nodeId: number) => {
    return request.put(`/agv/traffic/nodes/${nodeId}/release`)
  },
  
  // 交通规则API
  getTrafficRules: () => {
    return request.get<TrafficRule[]>('/agv/traffic/rules')
  },
  
  // 创建交通规则
  createTrafficRule: (rule: Omit<TrafficRule, 'id' | 'createTime' | 'updateTime'>) => {
    return request.post<TrafficRule>('/agv/traffic/rules', rule)
  },
  
  // 更新交通规则
  updateTrafficRule: (ruleId: number, rule: Partial<TrafficRule>) => {
    return request.put<TrafficRule>(`/agv/traffic/rules/${ruleId}`, rule)
  },
  
  // 切换交通规则状态
  toggleTrafficRuleStatus: (ruleId: number) => {
    return request.put(`/agv/traffic/rules/${ruleId}/toggle`)
  },
  
  // 删除交通规则
  deleteTrafficRule: (ruleId: number) => {
    return request.delete(`/agv/traffic/rules/${ruleId}`)
  },
  
  // 故障告警API
  getFaultAlerts: () => {
    return request.get<FaultAlert[]>('/agv/alerts')
  },
  
  // 获取故障告警详情
  getFaultAlertDetail: (alertId: number) => {
    return request.get<FaultAlert>(`/agv/alerts/${alertId}`)
  },
  
  // 处理故障告警
  handleFaultAlert: (alertId: number, handleResult: string, operator: string = '系统') => {
    return request.put<FaultAlert>(`/agv/alerts/${alertId}/handle`, { handleResult, operator })
  },
  
  // 协同策略API
  getCollaborationStrategies: () => {
    return request.get<CollaborationStrategy[]>('/agv/collaboration/strategies')
  },
  
  // 创建协同策略
  createCollaborationStrategy: (strategy: Omit<CollaborationStrategy, 'id' | 'createTime' | 'updateTime'>) => {
    return request.post<CollaborationStrategy>('/agv/collaboration/strategies', strategy)
  },
  
  // 激活协同策略
  activateCollaborationStrategy: (strategyId: string) => {
    return request.put(`/agv/collaboration/strategies/${strategyId}/activate`)
  },
  
  // 更新协同策略
  updateCollaborationStrategy: (strategyId: string, strategy: Partial<CollaborationStrategy>) => {
    return request.put<CollaborationStrategy>(`/agv/collaboration/strategies/${strategyId}`, strategy)
  },
  
  // 冲突避免API
  getCollisionAvoidanceConfig: () => {
    return request.get<CollisionAvoidanceConfig>('/agv/collision/config')
  },
  
  // 更新冲突避免配置
  updateCollisionAvoidanceConfig: (config: Partial<CollisionAvoidanceConfig>) => {
    return request.put<CollisionAvoidanceConfig>('/agv/collision/config', config)
  },
  
  // 电池状态API
  getBatteryStatuses: () => {
    return request.get<BatteryStatus[]>('/agv/battery/status')
  },
  
  // 获取指定AGV的电池状态
  getAGVBatteryStatus: (agvCode: string) => {
    return request.get<BatteryStatus>(`/agv/battery/${agvCode}`)
  },
  
  // 运行统计API
  getOperationStats: () => {
    return request.get<OperationStats>('/agv/stats')
  },
  
  // 获取统计历史数据
  getStatsHistory: (params: { startDate: string; endDate: string }) => {
    return request.get<OperationStats[]>('/agv/stats/history', { params })
  },
  
  // 协同日志API
  getCollaborationLogs: () => {
    return request.get<CollaborationLog[]>('/agv/collaboration/logs')
  },
  
  // 获取协同日志详情
  getCollaborationLogDetail: (logId: number) => {
    return request.get<CollaborationLog>(`/agv/collaboration/logs/${logId}`)
  },
  
  // 控制指令API
  // 发送充电指令
  sendChargeCommand: (agvCode: string) => {
    return request.post(`/agv/commands/${agvCode}/charge`)
  },
  
  // 发送暂停指令
  sendPauseCommand: (agvCode: string) => request.post(`/agv/commands/${agvCode}/pause`),
  
  // 发送继续指令
  sendResumeCommand: (agvCode: string) => {
    return request.post(`/agv/commands/${agvCode}/resume`)
  },
  
  // 发送紧急停止指令
  sendEmergencyStopCommand: (agvCode: string) => {
    return request.post(`/agv/commands/${agvCode}/emergency-stop`)
  },
  
  // 紧急停止所有AGV
  emergencyStopAllAGVs: () => {
    return request.post('/agv/commands/emergency-stop-all')
  },
  
  pauseAllAGVs: () => {
    return request.post('/agv/commands/pause-all')
  },

  // 恢复所有AGV运行
  resumeAllAGVs: () => {
    return request.post('/agv/commands/resume-all')
  },
  
  // 路径模拟API
  simulatePath: (params: { startPoint: string; endPoint: string; algorithm: string }) => request.post('/agv/path/simulate', params),
  
  // 获取模拟结果
  getSimulationResult: (simulationId: string) => {
    return request.get(`/agv/path/simulate/${simulationId}`)
  },
  
  // 区域管理API
  getAreas: () => {
    return request.get<any[]>('/agv/areas')
  },
  
  // 创建区域
  createArea: (area: any) => {
    return request.post('/agv/areas', area)
  },
  
  // 更新区域
  updateArea: (areaId: string, area: Partial<any>) => {
    return request.put(`/agv/areas/${areaId}`, area)
  },
  
  // 删除区域
  deleteArea: (areaId: string) => {
    return request.delete(`/agv/areas/${areaId}`)
  }
}

export default agvApi
