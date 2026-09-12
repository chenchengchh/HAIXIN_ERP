/**
 * EMS能源管理系统API入口
 * @author author
 * @date 2025-12-31
 */

// 导入各模块API
import * as energyCollectionApi from './energyCollection'
import * as energyAnalysisApi from './energyAnalysis'
import * as energyOptimizationApi from './energyOptimization'
import * as reportManagementApi from './reportManagement'

// 导出所有API
export const emsApi = {
  collection: energyCollectionApi,
  analysis: energyAnalysisApi,
  optimization: energyOptimizationApi,
  reports: reportManagementApi
}

// 导出类型定义
export type {
  // 能源采集相关类型
  RealTimeData,
  MeterDevice,
  CalibrationRecord
} from './energyCollection'

export type {
  // 能耗分析相关类型
  EnergyStatistics,
  EnergyComparison,
  EnergyAnomaly
} from './energyAnalysis'

export type {
  // 能源优化相关类型
  PotentialAnalysis,
  OptimizationSuggestion,
  OptimizationPlan,
  EffectEvaluation
} from './energyOptimization'

export type {
  // 报表管理相关类型
  StandardReport,
  CustomReport,
  AutoGenerationTask,
  ExportHistory
} from './reportManagement'
