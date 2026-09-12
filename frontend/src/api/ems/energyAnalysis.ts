/**
 * EMS能耗分析模块API服务
 * @author author
 * @date 2025-12-31
 */

import api from '../index'

// 定义API路径前缀
const API_PREFIX = '/api/v1/ems'

// 能耗统计数据类型定义
export interface EnergyStatistics {
  id: number
  name: string
  electricity: number
  water: number
  gas: number
  heat: number
  totalCost: number
  statDate: string
}

// 能耗对比数据类型定义
export interface EnergyComparison {
  id: number
  name: string
  currentPeriod: number
  previousPeriod: number
  difference: number
  growthRate: number
}

// 能耗异常数据类型定义
export interface EnergyAnomaly {
  id: number
  energyType: string
  area: string
  anomalyType: string
  actualValue: number
  expectedValue: number
  detectionTime: string
  status: 'pending' | 'processed'
}

// 能耗分析相关API
export const energyAnalysisApi = {
  /**
   * 获取能耗统计数据
   * @param params 查询参数
   * @returns 能耗统计数据
   */
  getEnergyStatistics: (params?: {
    dimension: 'workshop' | 'production-line' | 'equipment' | 'team'
    dateRange?: string[]
    energyType?: string
  }) => {
    return api.get(`${API_PREFIX}/analysis/statistics`, { params })
  },
  
  /**
   * 获取能耗趋势分析数据
   * @param params 查询参数
   * @returns 能耗趋势数据
   */
  getTrendAnalysis: (params?: {
    energyType: 'electricity' | 'water' | 'gas' | 'heat'
    timeRange: 'day' | 'week' | 'month' | 'year'
    area?: string
    startDate?: string
    endDate?: string
  }) => {
    return api.get(`${API_PREFIX}/analysis/trend`, { params })
  },
  
  /**
   * 获取能耗对比数据
   * @param params 查询参数
   * @returns 能耗对比数据
   */
  getEnergyComparison: (params?: {
    type: 'year-on-year' | 'month-on-month' | 'multi-equipment'
    energyType?: string
    dimension?: string
    dateRange?: string[]
  }) => {
    return api.get(`${API_PREFIX}/analysis/comparison`, { params })
  },
  
  /**
   * 获取能耗异常检测数据
   * @param params 查询参数
   * @returns 能耗异常数据
   */
  getAnomalyDetection: (params?: {
    energyType?: string
    area?: string
    status?: 'pending' | 'processed'
    page?: number
    size?: number
  }) => {
    return api.get(`${API_PREFIX}/analysis/anomalies`, { params })
  },
  
  /**
   * 处理能耗异常
   * @param id 异常ID
   * @returns 处理结果
   */
  processAnomaly: (id: number) => {
    return api.put(`${API_PREFIX}/analysis/anomalies/${id}/process`)
  },
  
  /**
   * 获取能耗异常详情
   * @param id 异常ID
   * @returns 异常详情
   */
  getAnomalyDetail: (id: number) => {
    return api.get(`${API_PREFIX}/analysis/anomalies/${id}`)
  },
  
  /**
   * 标记所有异常为已处理
   * @returns 处理结果
   */
  markAllAnomaliesAsProcessed: () => {
    return api.put(`${API_PREFIX}/analysis/anomalies/mark-all-processed`)
  }
}

// 导入共享虚拟数据配置
import { energyTypes, areas, generateRandomTime, generateRandomValue } from './mockDataConfig'

// 模拟数据生成器
const generateMockEnergyStatistics = (): EnergyStatistics[] => {
  const statistics: EnergyStatistics[] = []
  let id = 1
  
  // 为每个区域生成能耗统计数据
  areas.forEach(area => {
    // 生成基于区域的合理能耗数据
    const electricity = generateRandomValue(3000, 18000, 0)
    const water = generateRandomValue(100, 1000, 0)
    const gas = generateRandomValue(800, 4000, 0)
    const heat = generateRandomValue(300, 1500, 0)
    
    // 计算总成本（模拟价格：电力0.8元/kWh，水5元/m³，燃气3元/m³，热能20元/GJ）
    const totalCost = Math.round(electricity * 0.8 + water * 5 + gas * 3 + heat * 20)
    
    statistics.push({
      id: id++,
      name: area.name,
      electricity,
      water,
      gas,
      heat,
      totalCost,
      statDate: new Date(Date.now() - 86400000).toISOString().split('T')[0] || '2025-12-30' // 昨天的日期
    })
  })
  
  return statistics
}

const generateMockEnergyComparison = (): EnergyComparison[] => {
  const comparison: EnergyComparison[] = []
  let id = 1
  
  // 为每个区域生成能耗对比数据
  areas.forEach(area => {
    // 生成当前时期数据
    const currentPeriod = generateRandomValue(3000, 18000, 0)
    // 生成上一时期数据（与当前时期有合理波动）
    const fluctuation = generateRandomValue(-2000, 2000, 0)
    const previousPeriod = Math.max(1000, currentPeriod + fluctuation)
    // 计算差异和增长率
    const difference = currentPeriod - previousPeriod
    const growthRate = parseFloat(((difference / previousPeriod) * 100).toFixed(1))
    
    comparison.push({
      id: id++,
      name: area.name,
      currentPeriod,
      previousPeriod,
      difference,
      growthRate
    })
  })
  
  return comparison
}

const generateMockEnergyAnomaly = (): EnergyAnomaly[] => {
  const anomalies: EnergyAnomaly[] = []
  let id = 1
  
  // 为部分区域生成能耗异常数据
  areas.forEach(area => {
    // 每个区域生成1-2个异常
    const anomalyCount = Math.floor(Math.random() * 2) + 1
    
    for (let i = 0; i < anomalyCount; i++) {
      // 随机选择能源类型
      const energyTypeKey = Object.keys(energyTypes)[Math.floor(Math.random() * Object.keys(energyTypes).length)]
      const energyType = energyTypes[energyTypeKey as keyof typeof energyTypes]
      
      // 异常类型列表
      const anomalyTypes = ['非工作时间超标', '异常波动', '漏水检测', '设备故障', '管道泄漏', '计量异常']
      const anomalyType = anomalyTypes[Math.floor(Math.random() * anomalyTypes.length)] || '异常波动'
      
      // 生成异常值和预期值
      const expectedValue = generateRandomValue(50, 500)
      const value = expectedValue * generateRandomValue(1.5, 5) // 异常值是预期值的1.5-5倍
      
      anomalies.push({
        id: id++,
        energyType: energyType.name,
        area: area.name,
        anomalyType,
        actualValue: value,
        expectedValue,
        detectionTime: generateRandomTime(new Date(Date.now() - i * 86400000).toISOString().split('T')[0]),
        status: Math.random() > 0.5 ? 'pending' : 'processed' // 50%待处理，50%已处理
      })
    }
  })
  
  return anomalies
}

// 生成模拟数据
const mockEnergyStatistics: EnergyStatistics[] = generateMockEnergyStatistics()
const mockEnergyComparison: EnergyComparison[] = generateMockEnergyComparison()
const mockEnergyAnomaly: EnergyAnomaly[] = generateMockEnergyAnomaly()

// 导出模拟数据，用于前端模拟
export { mockEnergyStatistics, mockEnergyComparison, mockEnergyAnomaly }
