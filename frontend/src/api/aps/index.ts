/**
 * APS系统API服务
 * @author author
 * @date 2025-12-16
 */

import api from '../index'
// 从本地类型定义或使用any
type GanttTask = any
type GanttLink = any

// 定义API路径前缀
const API_PREFIX = '/api/v1/aps'

// 生产计划相关接口
export const ProductionPlanAPI = {
  /**
   * 获取生产计划列表
   * @param params 查询参数（支持page, size分页参数）
   * @returns 生产计划列表
   */
  getProductionPlans: (params?: any) => {
    // 如果有分页参数，使用page端点
    if (params?.page && params?.size) {
      return api.get(`${API_PREFIX}/production-plans/page`, { params })
    }
    return api.get(`${API_PREFIX}/production-plans`, { params })
  },
  
  /**
   * 获取生产计划详情
   * @param id 计划ID
   * @returns 生产计划详情
   */
  getProductionPlanById: (id: string | number) => {
    return api.get(`${API_PREFIX}/production-plans/${id}`)
  },
  
  /**
   * 根据计划编号获取生产计划
   * @param planNo 计划编号
   * @returns 生产计划详情
   */
  getProductionPlanByNo: (planNo: string) => {
    return api.get(`${API_PREFIX}/production-plans/no/${planNo}`)
  },
  
  /**
   * 创建生产计划
   * @param data 生产计划数据
   * @returns 创建后的生产计划
   */
  createProductionPlan: (data: any) => {
    return api.post(`${API_PREFIX}/production-plans`, data)
  },
  
  /**
   * 更新生产计划
   * @param id 计划ID
   * @param data 更新数据
   * @returns 更新后的生产计划
   */
  updateProductionPlan: (id: string | number, data: any) => {
    return api.put(`${API_PREFIX}/production-plans/${id}`, data)
  },
  
  /**
   * 删除生产计划
   * @param id 计划ID
   * @returns 删除结果
   */
  deleteProductionPlan: (id: string | number) => {
    return api.delete(`${API_PREFIX}/production-plans/${id}`)
  },
  
  /**
   * 提交生产计划审批
   * @param id 计划ID
   * @param initiatorId 发起人ID
   * @param initiatorName 发起人名称
   * @returns 提交结果
   */
  submitPlanForApproval: (id: string | number, initiatorId?: number, initiatorName?: string) => {
    const params: any = {}
    if (initiatorId !== undefined) params.initiatorId = initiatorId
    if (initiatorName !== undefined) params.initiatorName = initiatorName
    return api.post(`${API_PREFIX}/production-plans/${id}/submit-approval`, null, { params })
  }
}

// 调度结果相关接口
export const ScheduleResultAPI = {
  /**
   * 获取调度结果列表
   * @param params 查询参数（支持page, size分页参数）
   * @returns 调度结果列表
   */
  getScheduleResults: (params?: any) => {
    // 如果有分页参数，使用page端点
    if (params?.page && params?.size) {
      return api.get(`${API_PREFIX}/schedule-results/page`, { params })
    }
    return api.get(`${API_PREFIX}/schedule-results`, { params })
  },
  
  /**
   * 获取调度结果详情
   * @param id 结果ID
   * @returns 调度结果详情
   */
  getScheduleResultById: (id: string | number) => {
    return api.get(`${API_PREFIX}/schedule-results/${id}`)
  },
  
  /**
   * 根据计划ID获取调度结果
   * @param planId 计划ID
   * @returns 调度结果列表
   */
  getScheduleResultsByPlanId: (planId: string | number) => {
    return api.get(`${API_PREFIX}/schedule-results/plan/${planId}`)
  },
  
  /**
   * 生成调度结果
   * @param data 调度参数
   * @returns 调度结果
   */
  generateSchedule: (data: {
    planId: string | number
    algorithm: string
    params: any
    objectives: string[]
  }) => {
    return api.post(`${API_PREFIX}/schedule-results/generate`, data)
  },
  
  /**
   * 删除调度结果
   * @param id 结果ID
   * @returns 删除结果
   */
  deleteScheduleResult: (id: string | number) => {
    return api.delete(`${API_PREFIX}/schedule-results/${id}`)
  },
  
  /**
   * 获取调度结果的甘特图数据
   * @param resultId 调度结果ID
   * @returns 甘特图数据（任务和链接）
   */
  getGanttData: (resultId: string | number) => {
    return api.get(`${API_PREFIX}/schedule-results/${resultId}/gantt-data`)
  }
}

// 资源约束相关接口
export const ResourceConstraintAPI = {
  /**
   * 获取资源约束列表
   * @param params 查询参数
   * @returns 资源约束列表
   */
  getResourceConstraints: (params?: any) => {
    return api.get(`${API_PREFIX}/resource-constraints`, { params })
  },
  
  /**
   * 获取资源约束详情
   * @param id 约束ID
   * @returns 资源约束详情
   */
  getResourceConstraintById: (id: string | number) => {
    return api.get(`${API_PREFIX}/resource-constraints/${id}`)
  },
  
  /**
   * 创建资源约束
   * @param data 资源约束数据
   * @returns 创建后的资源约束
   */
  createResourceConstraint: (data: any) => {
    return api.post(`${API_PREFIX}/resource-constraints`, data)
  },
  
  /**
   * 更新资源约束
   * @param id 约束ID
   * @param data 更新数据
   * @returns 更新后的资源约束
   */
  updateResourceConstraint: (id: string | number, data: any) => {
    return api.put(`${API_PREFIX}/resource-constraints/${id}`, data)
  },
  
  /**
   * 删除资源约束
   * @param id 约束ID
   * @returns 删除结果
   */
  deleteResourceConstraint: (id: string | number) => {
    return api.delete(`${API_PREFIX}/resource-constraints/${id}`)
  }
}

// 算法参数配置相关接口
export const AlgorithmParamAPI = {
  /**
   * 获取算法参数配置列表
   * @param params 查询参数
   * @returns 算法参数配置列表
   */
  getAlgorithmParams: (params?: any) => {
    return api.get(`${API_PREFIX}/algorithm-params`, { params })
  },
  
  /**
   * 获取算法参数配置详情
   * @param id 参数ID
   * @returns 算法参数配置详情
   */
  getAlgorithmParamById: (id: string | number) => {
    return api.get(`${API_PREFIX}/algorithm-params/${id}`)
  },
  
  /**
   * 根据算法名称获取参数配置
   * @param algorithmName 算法名称
   * @returns 参数配置列表
   */
  getParamsByAlgorithmName: (algorithmName: string) => {
    return api.get(`${API_PREFIX}/algorithm-params/algorithm/${algorithmName}`)
  },
  
  /**
   * 创建算法参数配置
   * @param data 参数配置数据
   * @returns 创建后的参数配置
   */
  createAlgorithmParam: (data: any) => {
    return api.post(`${API_PREFIX}/algorithm-params`, data)
  },
  
  /**
   * 更新算法参数配置
   * @param id 参数ID
   * @param data 更新数据
   * @returns 更新后的参数配置
   */
  updateAlgorithmParam: (id: string | number, data: any) => {
    return api.put(`${API_PREFIX}/algorithm-params/${id}`, data)
  },
  
  /**
   * 批量保存算法参数配置
   * @param data 参数配置列表
   * @returns 保存后的参数配置列表
   */
  batchSaveAlgorithmParams: (data: any[]) => {
    return api.post(`${API_PREFIX}/algorithm-params/batch`, data)
  },
  
  /**
   * 删除算法参数配置
   * @param id 参数ID
   * @returns 删除结果
   */
  deleteAlgorithmParam: (id: string | number) => {
    return api.delete(`${API_PREFIX}/algorithm-params/${id}`)
  },
  
  /**
   * 根据算法名称删除所有参数配置
   * @param algorithmName 算法名称
   * @returns 删除结果
   */
  deleteParamsByAlgorithmName: (algorithmName: string) => {
    return api.delete(`${API_PREFIX}/algorithm-params/algorithm/${algorithmName}`)
  },
  
  /**
   * 获取所有算法名称
   * @returns 算法名称列表
   */
  getAllAlgorithmNames: () => {
    return api.get(`${API_PREFIX}/algorithm-params/algorithm-names`)
  },
  
  /**
   * 获取算法默认参数
   * @param algorithmName 算法名称
   * @returns 默认参数映射
   */
  getDefaultParams: (algorithmName: string) => {
    return api.get(`${API_PREFIX}/algorithm-params/default-params/${algorithmName}`)
  }
}

// 产能数据相关接口
export const ResourceCapabilityAPI = {
  /**
   * 获取产能数据
   * @returns 产能数据列表
   */
  getResourceCapabilityData: () => {
    return api.get(`${API_PREFIX}/resource-capability`)
  },
  
  /**
   * 根据资源ID获取产能数据
   * @param resourceId 资源ID
   * @returns 产能数据列表
   */
  getResourceCapabilityByResourceId: (resourceId: string | number) => {
    return api.get(`${API_PREFIX}/resource-capability/resource/${resourceId}`)
  },
  
  /**
   * 根据产能类型获取产能数据
   * @param capabilityType 产能类型
   * @returns 产能数据列表
   */
  getResourceCapabilityByType: (capabilityType: string) => {
    return api.get(`${API_PREFIX}/resource-capability/type/${capabilityType}`)
  }
}

// 资源负载分析相关接口
export const ResourceLoadAPI = {
  /**
   * 获取资源负载数据
   * @param params 查询参数
   * @returns 资源负载数据
   */
  getResourceLoadData: (params: {
    planId: string | number
    resourceIds?: string[]
    startTime?: string | number
    endTime?: string | number
    timeScale?: 'hour' | 'day' | 'week' | 'month'
  }) => {
    return api.get(`${API_PREFIX}/resource-load`, { params })
  },
  
  /**
   * 获取资源列表
   * @param params 查询参数
   * @returns 资源列表
   */
  getResources: (params?: any) => {
    return api.get(`${API_PREFIX}/resources`, { params })
  },
  
  /**
   * 获取资源负载统计
   * @param params 查询参数
   * @returns 资源负载统计数据
   */
  getResourceLoadStats: (params: {
    planId: string | number
    resourceIds?: string[]
    startTime?: string | number
    endTime?: string | number
  }) => {
    return api.get(`${API_PREFIX}/resource-load/stats`, { params })
  }
}

// 工艺约束相关接口
export const SchedulingConstraintAPI = {
  /**
   * 获取工艺约束数据
   * @returns 工艺约束数据列表
   */
  getSchedulingConstraints: () => {
    return api.get(`${API_PREFIX}/scheduling-constraints`)
  },
  
  /**
   * 根据约束类型获取工艺约束
   * @param constraintType 约束类型
   * @returns 工艺约束数据列表
   */
  getSchedulingConstraintsByType: (constraintType: string) => {
    return api.get(`${API_PREFIX}/scheduling-constraints/type/${constraintType}`)
  }
}

// 调度优化建议相关接口
export const ScheduleOptimizationAPI = {
  /**
   * 获取调度优化建议
   * @param params 查询参数（scheduleResultId为空时返回全部建议）
   * @returns 调度优化建议列表
   */
  getOptimizationSuggestions: (params: {
    scheduleResultId?: string | number
    type?: string
    status?: string
  }) => {
    return api.get(`${API_PREFIX}/optimization-suggestions`, { params })
  },
  
  /**
   * 重新分析调度结果
   * @param scheduleResultId 调度结果ID
   * @returns 分析结果
   */
  reanalyzeSchedule: (scheduleResultId: string | number) => {
    return api.post(`${API_PREFIX}/optimization-suggestions/analyze/${scheduleResultId}`)
  },
  
  /**
   * 采纳优化建议
   * @param id 建议ID
   * @returns 采纳结果
   */
  acceptSuggestion: (id: string | number) => {
    return api.put(`${API_PREFIX}/optimization-suggestions/${id}/accept`)
  },
  
  /**
   * 忽略优化建议
   * @param id 建议ID
   * @returns 忽略结果
   */
  ignoreSuggestion: (id: string | number) => {
    return api.put(`${API_PREFIX}/optimization-suggestions/${id}/ignore`)
  },
  
  /**
   * 重置优化建议状态
   * @param id 建议ID
   * @returns 重置结果
   */
  resetSuggestionStatus: (id: string | number) => {
    return api.put(`${API_PREFIX}/optimization-suggestions/${id}/reset`)
  },
  
  /**
   * 获取优化建议报告
   * @param scheduleResultId 调度结果ID
   * @returns 优化建议报告
   */
  getSuggestionReport: (scheduleResultId: string | number) => {
    return api.get(`${API_PREFIX}/optimization-suggestions/report/${scheduleResultId}`)
  }
}

// 调度算法引擎相关接口
export const SchedulingEngineAPI = {
  /**
   * 获取所有可用算法
   * @returns 可用算法列表
   */
  getAllAlgorithms: () => {
    return api.get(`${API_PREFIX}/engine/algorithms`)
  },
  
  /**
   * 获取算法详情
   * @param algorithmName 算法名称
   * @returns 算法详情
   */
  getAlgorithmDetail: (algorithmName: string) => {
    return api.get(`${API_PREFIX}/engine/algorithms/${algorithmName}`)
  },
  
  /**
   * 执行调度算法
   * @param data 执行参数
   * @returns 调度结果
   */
  executeAlgorithm: (data: {
    algorithmName: string
    planId: string | number
    params: any
  }) => {
    return api.post(`${API_PREFIX}/engine/execute`, data)
  }
}

// 计划执行监控相关接口
export const PlanMonitoringAPI = {
  /**
   * 获取APS仪表盘全局统计数据
   * @returns 仪表盘统计（scheduleRate/resourceUtilization/pendingPlans/activeAlerts等）
   */
  getDashboardStats: () => {
    return api.get(`${API_PREFIX}/monitoring/dashboard/stats`)
  },

  /**
   * 获取计划与实际对比数据
   * @param params 查询参数
   * @returns 计划与实际对比数据
   */
  getPlanCompareData: (params: {
    planId: string | number
    startTime?: string | number
    endTime?: string | number
  }) => {
    return api.get(`${API_PREFIX}/monitoring/plan-compare`, { params })
  },
  
  /**
   * 获取进度跟踪数据
   * @param params 查询参数
   * @returns 进度跟踪数据
   */
  getProgressTrackingData: (params: {
    planId: string | number
    orderId?: string | number
    status?: string
  }) => {
    return api.get(`${API_PREFIX}/monitoring/progress-tracking`, { params })
  },
  
  /**
   * 获取偏差预警数据
   * @param params 查询参数
   * @returns 偏差预警数据
   */
  getDeviationAlerts: (params: {
    planId?: string | number
    severity?: string
    status?: string
  }) => {
    return api.get(`${API_PREFIX}/monitoring/deviation-alerts`, { params })
  },
  
  /**
   * 处理偏差预警
   * @param id 预警ID
   * @returns 处理结果
   */
  processAlert: (id: string | number) => {
    return api.put(`${API_PREFIX}/monitoring/alerts/${id}/process`)
  },
  
  /**
   * 忽略偏差预警
   * @param id 预警ID
   * @returns 忽略结果
   */
  dismissAlert: (id: string | number) => {
    return api.put(`${API_PREFIX}/monitoring/alerts/${id}/dismiss`)
  }
}

// 多周期计划相关接口
export const MultiPeriodPlanAPI = {
  /**
   * 获取多周期计划列表
   * @returns 多周期计划列表（按创建时间倒序）
   */
  getMultiPeriodPlans: () => {
    return api.get(`${API_PREFIX}/multi-period-plans`)
  },

  /**
   * 获取多周期计划详情（含周期片段明细）
   * @param id 计划ID
   * @returns {plan, items} 计划基本信息与周期片段明细
   */
  getMultiPeriodPlanDetail: (id: string | number) => {
    return api.get(`${API_PREFIX}/multi-period-plans/${id}`)
  },

  /**
   * 生成多周期计划
   * @param data 生成参数（planId/periodType/periodCount/startDate/algorithm/objectives）
   * @returns 生成的多周期计划与明细
   */
  generateMultiPeriodPlan: (data: {
    planId: string | number
    periodType: string
    periodCount: number
    startDate: string
    algorithm?: string
    objectives?: string[]
  }) => {
    return api.post(`${API_PREFIX}/multi-period-plans/generate`, data)
  },

  /**
   * 删除多周期计划
   * @param id 计划ID
   * @returns 删除结果
   */
  deleteMultiPeriodPlan: (id: string | number) => {
    return api.delete(`${API_PREFIX}/multi-period-plans/${id}`)
  }
}

// 导出所有API对象
export const apsApi = {
  ProductionPlanAPI,
  ScheduleResultAPI,
  ResourceConstraintAPI,
  AlgorithmParamAPI,
  ResourceLoadAPI,
  ScheduleOptimizationAPI,
  SchedulingEngineAPI,
  PlanMonitoringAPI,
  MultiPeriodPlanAPI
}

// 保持默认导出以兼容旧代码
export default apsApi
