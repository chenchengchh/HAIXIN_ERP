/**
 * EMS能源优化模块API服务
 * @author author
 * @date 2025-12-31
 */

import api from '../index'

// 定义API路径前缀
const API_PREFIX = '/api/v1/ems'

// 节能潜力分析数据类型定义
export interface PotentialAnalysis {
  id: number
  area: string
  energyType: string
  potential: number
  estimatedSavings: string
  priority: '高' | '中' | '低'
}

// 优化建议数据类型定义
export interface OptimizationSuggestion {
  id: number
  title: string
  content: string
  targetArea: string
  estimatedEffect: string
  status: 'adopted' | 'pending'
  createdAt: string
}

// 优化方案执行数据类型定义
export interface OptimizationPlan {
  id: number
  planName: string
  targetArea: string
  predictedSaving: number
  actualSaving: number | null
  status: 1 | 2 | 3 | 4
  execContent: string
  reportUrl?: string
  startDate: string
  endDate: string
}

// 节能效果评估数据类型定义
export interface EffectEvaluation {
  id: number
  planName: string
  targetArea: string
  predictedSaving: number
  actualSaving: number
  evaluationDate: string
}

// 能源优化相关API
export const energyOptimizationApi = {
  /**
   * 获取节能潜力分析数据
   * @param params 查询参数
   * @returns 节能潜力分析数据
   */
  getPotentialAnalysis: (params?: {
    area?: string
    energyType?: string
    priority?: '高' | '中' | '低'
  }) => {
    return api.get(`${API_PREFIX}/optimization/potential`, { params })
  },
  
  /**
   * 获取优化建议列表
   * @param params 查询参数
   * @returns 优化建议列表
   */
  getOptimizationSuggestions: (params?: {
    status?: 'adopted' | 'pending'
    targetArea?: string
    page?: number
    size?: number
  }) => {
    return api.get(`${API_PREFIX}/optimization/suggestions`, { params })
  },
  
  /**
   * 采纳优化建议
   * @param id 建议ID
   * @returns 采纳结果
   */
  adoptSuggestion: (id: number) => {
    return api.put(`${API_PREFIX}/optimization/suggestions/${id}/adopt`)
  },
  
  /**
   * 获取优化方案执行列表
   * @param params 查询参数
   * @returns 优化方案执行列表
   */
  getOptimizationPlans: (params?: {
    status?: number
    targetArea?: string
    page?: number
    size?: number
  }) => {
    return api.get(`${API_PREFIX}/optimization/plans`, { params })
  },
  
  /**
   * 获取优化方案详情
   * @param id 方案ID
   * @returns 方案详情
   */
  getOptimizationPlanById: (id: number) => {
    return api.get(`${API_PREFIX}/optimization/plans/${id}`)
  },
  
  /**
   * 更新优化方案执行状态
   * @param id 方案ID
   * @param status 执行状态
   * @returns 更新结果
   */
  updatePlanStatus: (id: number, status: 1 | 2 | 3 | 4) => {
    return api.put(`${API_PREFIX}/optimization/plans/${id}/status`, { status })
  },
  
  /**
   * 获取节能效果评估列表
   * @param params 查询参数
   * @returns 节能效果评估列表
   */
  getEffectEvaluations: (params?: {
    planName?: string
    targetArea?: string
    dateRange?: string[]
    page?: number
    size?: number
  }) => {
    return api.get(`${API_PREFIX}/optimization/evaluations`, { params })
  },
  
  /**
   * 获取节能效果评估详情
   * @param id 评估ID
   * @returns 评估详情
   */
  getEvaluationDetail: (id: number) => {
    return api.get(`${API_PREFIX}/optimization/evaluations/${id}`)
  },

  /**
   * 下载节能效果评估报告
   * @param id 评估ID
   * @returns 报告文件流
   */
  downloadEvaluationReport: (id: number) => {
    return api.get(`${API_PREFIX}/optimization/evaluations/${id}/report`, { responseType: 'blob' })
  }
}

// 导入共享虚拟数据配置
import { energyTypes, areas, generateRandomTime, generateRandomValue } from './mockDataConfig'

// 模拟数据生成器
const generateMockPotentialAnalysis = (): PotentialAnalysis[] => {
  const analysis: PotentialAnalysis[] = []
  let id = 1
  
  // 为每个区域生成不同能源类型的节能潜力分析
  areas.forEach(area => {
    Object.values(energyTypes).forEach(energyType => {
      // 生成合理的节能潜力百分比
      const potential = generateRandomValue(5, 30, 1)
      
      // 根据潜力计算预估节能量
      let estimatedSavings: string
      if (energyType.code === 'electricity') {
        estimatedSavings = `${Math.round(potential * 5000)} kWh` // 电力：每1%潜力约5000 kWh
      } else if (energyType.code === 'water') {
        estimatedSavings = `${Math.round(potential * 800)} m³` // 水：每1%潜力约800 m³
      } else if (energyType.code === 'gas') {
        estimatedSavings = `${Math.round(potential * 300)} m³` // 燃气：每1%潜力约300 m³
      } else {
        estimatedSavings = `${Math.round(potential * 60)} GJ` // 热能：每1%潜力约60 GJ
      }
      
      // 根据潜力设置优先级
      let priority: '高' | '中' | '低' = '低'
      if (potential >= 20) priority = '高'
      else if (potential >= 10) priority = '中'
      
      analysis.push({
        id: id++,
        area: area.name,
        energyType: energyType.name,
        potential,
        estimatedSavings,
        priority
      })
    })
  })
  
  return analysis
}

const generateMockOptimizationSuggestions = (): OptimizationSuggestion[] => {
  const suggestions: OptimizationSuggestion[] = []
  const suggestionTitles = [
    '设备节能改造',
    '照明系统优化',
    '空调系统优化',
    '生产工艺调整',
    '能源管理系统升级',
    '余热回收利用',
    '设备运行调度优化',
    '节能意识培训'
  ]
  
  let id = 1
  
  // 为部分区域生成优化建议
  areas.slice(0, 3).forEach((area, areaIndex) => {
    // 每个区域生成2-3条优化建议
    const suggestionCount = Math.floor(Math.random() * 2) + 2
    
    for (let i = 0; i < suggestionCount; i++) {
      // 随机选择建议类型
      const title = suggestionTitles[Math.floor(Math.random() * suggestionTitles.length)] || '优化建议'
      
      suggestions.push({
        id: id++,
        title,
        content: `${title}：对${area.name}的相关设备和系统进行优化改造，预计可实现显著的节能效果。`,
        targetArea: area.name,
        estimatedEffect: `${generateRandomValue(10, 30, 0)}%节能率`,
        status: Math.random() > 0.5 ? 'adopted' : 'pending', // 50%已采纳，50%待处理
        createdAt: new Date(Date.now() - (areaIndex * 10 + i * 3) * 86400000).toISOString().split('T')[0] || '2025-12-31' // 不同的创建日期
      })
    }
  })
  
  return suggestions
}

const generateMockOptimizationPlans = (): OptimizationPlan[] => {
  const plans: OptimizationPlan[] = []
  const planNames = [
    '设备节能改造方案',
    '照明系统优化方案',
    '空调系统优化方案',
    '生产工艺调整方案',
    '能源管理系统升级方案',
    '余热回收利用方案'
  ]
  
  let id = 1
  
  // 为部分区域生成优化方案
  areas.forEach((area, areaIndex) => {
    // 每个区域生成1-2个优化方案
    const planCount = Math.floor(Math.random() * 2) + 1
    
    for (let i = 0; i < planCount; i++) {
      // 随机选择方案名称
      const planName = planNames[Math.floor(Math.random() * planNames.length)]
      
      // 生成方案数据
      const predictedSaving = generateRandomValue(10, 30, 0)
      
      plans.push({
        id: id++,
        planName: `${planName}（${area.name}）`,
        targetArea: area.name,
        predictedSaving,
        actualSaving: Math.random() > 0.5 ? generateRandomValue(predictedSaving * 0.8, predictedSaving * 1.2, 1) : null, // 50%已完成，50%未完成
        status: (Math.floor(Math.random() * 4) + 1) as 1 | 2 | 3 | 4, // 1-4随机状态
        execContent: `执行${planName}，对${area.name}的相关设备和系统进行优化改造。`,
        startDate: new Date(Date.now() - (areaIndex * 20 + i * 10) * 86400000).toISOString().split('T')[0] || '2025-12-31', // 开始日期
        endDate: new Date(Date.now() - (areaIndex * 20 + i * 10 - 30) * 86400000).toISOString().split('T')[0] || '2026-01-30', // 结束日期（30天后）
      })
    }
  })
  
  return plans
}

const generateMockEffectEvaluations = (): EffectEvaluation[] => {
  const evaluations: EffectEvaluation[] = []
  const planNames = [
    '2025年夏季节能方案',
    '空压机系统优化',
    '照明系统改造',
    '空调系统升级',
    '设备节能改造'
  ]
  
  let id = 1
  
  // 生成效果评估数据
  planNames.forEach((planName, index) => {
    // 为每个方案生成1-2条评估记录
    const evaluationCount = Math.floor(Math.random() * 2) + 1
    
    for (let i = 0; i < evaluationCount; i++) {
      const predictedSaving = generateRandomValue(10, 30, 0)
      const actualSaving = generateRandomValue(predictedSaving * 0.8, predictedSaving * 1.2, 1)
      
      // 随机选择区域，确保不为undefined
      const randomAreaIndex = Math.floor(Math.random() * areas.length)
      const randomArea = areas[randomAreaIndex] || { name: '车间1' }
      evaluations.push({
        id: id++,
        planName,
        targetArea: randomArea.name || '车间1',
        predictedSaving,
        actualSaving,
        evaluationDate: new Date(Date.now() - (index * 15 + i * 5) * 86400000).toISOString().split('T')[0] || '2025-12-31' // 评估日期
      })
    }
  })
  
  return evaluations
}

// 生成模拟数据
const mockPotentialAnalysis: PotentialAnalysis[] = generateMockPotentialAnalysis()
const mockOptimizationSuggestions: OptimizationSuggestion[] = generateMockOptimizationSuggestions()
const mockOptimizationPlans: OptimizationPlan[] = generateMockOptimizationPlans()
const mockEffectEvaluations: EffectEvaluation[] = generateMockEffectEvaluations()

// 导出模拟数据，用于前端模拟
export { mockPotentialAnalysis, mockOptimizationSuggestions, mockOptimizationPlans, mockEffectEvaluations }
