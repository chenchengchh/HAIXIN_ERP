/**
 * QMS（质量管理系统）API服务
 * @author author
 * @date 2025-12-22
 */

import api from '../index'
import type {
  // 质量检验计划模块
  InspectionStandard,
  InspectionPlan,
  InspectionTask,
  InspectionResult,
  // 不合格品管理模块
  NCRegistration,
  NCReview,
  NCDisposal,
  NCTracking,
  // 质量异常管理模块
  AnomalyReport,
  AnomalyAnalysis,
  AnomalyDisposal as QmsAnomalyDisposal,
  CAPA,
  // 质量数据分析模块
  DataCollection,
  StatisticalData,
  Report,
  ForecastResult
} from '../../types/qms'

// 定义API路径前缀
const API_PREFIX = '/api/v1/qms'

// 质量检验计划模块 - 检验标准管理
const InspectionStandardAPI = {
  /**
   * 获取检验标准列表
   * @param params 查询参数
   * @returns 检验标准列表
   */
  getInspectionStandards: (params?: any) => {
    return api.get(`${API_PREFIX}/inspection-standards`, { params })
  },
  
  /**
   * 获取检验标准详情
   * @param id 标准ID
   * @returns 检验标准详情
   */
  getInspectionStandardById: (id: string | number) => {
    return api.get(`${API_PREFIX}/inspection-standards/${id}`)
  },
  
  /**
   * 根据标准编号获取检验标准
   * @param standardNo 标准编号
   * @returns 检验标准详情
   */
  getInspectionStandardByNo: (standardNo: string) => {
    return api.get(`${API_PREFIX}/inspection-standards/no/${standardNo}`)
  },
  
  /**
   * 创建检验标准
   * @param data 检验标准数据
   * @returns 创建后的检验标准
   */
  createInspectionStandard: (data: Omit<InspectionStandard, 'id'>) => {
    return api.post(`${API_PREFIX}/inspection-standards`, data)
  },
  
  /**
   * 更新检验标准
   * @param id 标准ID
   * @param data 更新数据
   * @returns 更新后的检验标准
   */
  updateInspectionStandard: (id: string | number, data: Partial<InspectionStandard>) => {
    return api.put(`${API_PREFIX}/inspection-standards/${id}`, data)
  },
  
  /**
   * 删除检验标准
   * @param id 标准ID
   * @returns 删除结果
   */
  deleteInspectionStandard: (id: string | number) => {
    return api.delete(`${API_PREFIX}/inspection-standards/${id}`)
  },
  
  /**
   * 激活检验标准
   * @param id 标准ID
   * @returns 激活结果
   */
  activateInspectionStandard: (id: string | number) => {
    return api.put(`${API_PREFIX}/inspection-standards/${id}/activate`)
  },
  
  /**
   * 停用检验标准
   * @param id 标准ID
   * @returns 停用结果
   */
  deactivateInspectionStandard: (id: string | number) => {
    return api.put(`${API_PREFIX}/inspection-standards/${id}/deactivate`)
  }
}

// 质量检验计划模块 - 检验计划管理
const InspectionPlanAPI = {
  /**
   * 获取检验计划列表
   * @param params 查询参数
   * @returns 检验计划列表
   */
  getInspectionPlans: (params?: any) => {
    return api.get(`${API_PREFIX}/inspection-plans`, { params })
  },
  
  /**
   * 获取检验计划详情
   * @param id 计划ID
   * @returns 检验计划详情
   */
  getInspectionPlanById: (id: string | number) => {
    return api.get(`${API_PREFIX}/inspection-plans/${id}`)
  },
  
  /**
   * 创建检验计划
   * @param data 检验计划数据
   * @returns 创建后的检验计划
   */
  createInspectionPlan: (data: Omit<InspectionPlan, 'id'>) => {
    return api.post(`${API_PREFIX}/inspection-plans`, data)
  },
  
  /**
   * 更新检验计划
   * @param id 计划ID
   * @param data 更新数据
   * @returns 更新后的检验计划
   */
  updateInspectionPlan: (id: string | number, data: Partial<InspectionPlan>) => {
    return api.put(`${API_PREFIX}/inspection-plans/${id}`, data)
  },
  
  /**
   * 删除检验计划
   * @param id 计划ID
   * @returns 删除结果
   */
  deleteInspectionPlan: (id: string | number) => {
    return api.delete(`${API_PREFIX}/inspection-plans/${id}`)
  },
  
  /**
   * 激活检验计划
   * @param id 计划ID
   * @returns 激活结果
   */
  activateInspectionPlan: (id: string | number) => {
    return api.put(`${API_PREFIX}/inspection-plans/${id}/activate`)
  },
  
  /**
   * 失效检验计划
   * @param id 计划ID
   * @returns 失效结果
   */
  invalidateInspectionPlan: (id: string | number) => {
    return api.put(`${API_PREFIX}/inspection-plans/${id}/invalidate`)
  }
}

// 质量检验计划模块 - 检验任务管理
const InspectionTaskAPI = {
  /**
   * 获取检验任务列表
   * @param params 查询参数
   * @returns 检验任务列表
   */
  getInspectionTasks: (params?: any) => {
    return api.get(`${API_PREFIX}/inspection-tasks`, { params })
  },
  
  /**
   * 获取检验任务详情
   * @param id 任务ID
   * @returns 检验任务详情
   */
  getInspectionTaskById: (id: string | number) => {
    return api.get(`${API_PREFIX}/inspection-tasks/${id}`)
  },
  
  /**
   * 创建检验任务
   * @param data 检验任务数据
   * @returns 创建后的检验任务
   */
  createInspectionTask: (data: Omit<InspectionTask, 'id'>) => {
    return api.post(`${API_PREFIX}/inspection-tasks`, data)
  },
  
  /**
   * 更新检验任务
   * @param id 任务ID
   * @param data 更新数据
   * @returns 更新后的检验任务
   */
  updateInspectionTask: (id: string | number, data: Partial<InspectionTask>) => {
    return api.put(`${API_PREFIX}/inspection-tasks/${id}`, data)
  },
  
  /**
   * 删除检验任务
   * @param id 任务ID
   * @returns 删除结果
   */
  deleteInspectionTask: (id: string | number) => {
    return api.delete(`${API_PREFIX}/inspection-tasks/${id}`)
  },
  
  /**
   * 分配检验任务
   * @param id 任务ID
   * @param data 分配数据
   * @returns 分配结果
   */
  assignInspectionTask: (id: string | number, data: { assignee: string }) => {
    return api.put(`${API_PREFIX}/inspection-tasks/${id}/assign`, data)
  },
  
  /**
   * 取消检验任务
   * @param id 任务ID
   * @returns 取消结果
   */
  cancelInspectionTask: (id: string | number) => {
    return api.put(`${API_PREFIX}/inspection-tasks/${id}/cancel`)
  },
  
  /**
   * 自动分配检验任务
   * @param params 分配参数
   * @returns 分配结果
   */
  autoAssignInspectionTasks: (params?: { planId?: string | number; materialCode?: string }) => {
    return api.post(`${API_PREFIX}/inspection-tasks/auto-assign`, params)
  }
}

// 质量检验计划模块 - 检验结果管理
const InspectionResultAPI = {
  /**
   * 获取检验结果列表
   * @param params 查询参数
   * @returns 检验结果列表
   */
  getInspectionResults: (params?: any) => {
    return api.get(`${API_PREFIX}/inspection-results`, { params })
  },
  
  /**
   * 获取检验结果详情
   * @param id 结果ID
   * @returns 检验结果详情
   */
  getInspectionResultById: (id: string | number) => {
    return api.get(`${API_PREFIX}/inspection-results/${id}`)
  },
  
  /**
   * 创建检验结果
   * @param data 检验结果数据
   * @returns 创建后的检验结果
   */
  createInspectionResult: (data: Omit<InspectionResult, 'id'>) => {
    return api.post(`${API_PREFIX}/inspection-results`, data)
  },
  
  /**
   * 更新检验结果
   * @param id 结果ID
   * @param data 更新数据
   * @returns 更新后的检验结果
   */
  updateInspectionResult: (id: string | number, data: Partial<InspectionResult>) => {
    return api.put(`${API_PREFIX}/inspection-results/${id}`, data)
  },
  
  /**
   * 审核检验结果
   * @param id 结果ID
   * @param data 审核数据
   * @returns 审核结果
   */
  auditInspectionResult: (id: string | number, data: { auditStatus: 'approved' | 'rejected'; auditRemark?: string }) => {
    return api.put(`${API_PREFIX}/inspection-results/${id}/audit`, data)
  },
  
  /**
   * 批量审核检验结果
   * @param data 批量审核数据
   * @returns 批量审核结果
   */
  batchAuditInspectionResults: (data: { ids: (string | number)[]; auditStatus: 'approved' | 'rejected'; auditRemark?: string }) => {
    return api.put(`${API_PREFIX}/inspection-results/batch-audit`, data)
  }
}

// 不合格品管理模块 - 不合格品登记管理
const NCRegistrationAPI = {
  /**
   * 获取不合格品登记列表
   * @param params 查询参数
   * @returns 不合格品登记列表
   */
  getNcRegistrations: (params?: any) => {
    return api.get(`${API_PREFIX}/nc-registrations`, { params })
  },
  
  /**
   * 获取不合格品登记详情
   * @param id 登记ID
   * @returns 不合格品登记详情
   */
  getNcRegistrationById: (id: string | number) => {
    return api.get(`${API_PREFIX}/nc-registrations/${id}`)
  },
  
  /**
   * 创建不合格品登记
   * @param data 不合格品登记数据
   * @returns 创建后的不合格品登记
   */
  createNcRegistration: (data: Omit<NCRegistration, 'id'>) => {
    return api.post(`${API_PREFIX}/nc-registrations`, data)
  },
  
  /**
   * 更新不合格品登记
   * @param id 登记ID
   * @param data 更新数据
   * @returns 更新后的不合格品登记
   */
  updateNcRegistration: (id: string | number, data: Partial<NCRegistration>) => {
    return api.put(`${API_PREFIX}/nc-registrations/${id}`, data)
  },
  
  /**
   * 删除不合格品登记
   * @param id 登记ID
   * @returns 删除结果
   */
  deleteNcRegistration: (id: string | number) => {
    return api.delete(`${API_PREFIX}/nc-registrations/${id}`)
  },
  
  /**
   * 提交评审
   * @param id 登记ID
   * @returns 提交结果
   */
  submitForReview: (id: string | number) => {
    return api.put(`${API_PREFIX}/nc-registrations/${id}/submit-review`)
  }
}

// 不合格品管理模块 - 不合格品评审管理
const NCReviewAPI = {
  /**
   * 获取不合格品评审列表
   * @param params 查询参数
   * @returns 不合格品评审列表
   */
  getNcReviews: (params?: any) => {
    return api.get(`${API_PREFIX}/nc-reviews`, { params })
  },
  
  /**
   * 获取不合格品评审详情
   * @param id 评审ID
   * @returns 不合格品评审详情
   */
  getNcReviewById: (id: string | number) => {
    return api.get(`${API_PREFIX}/nc-reviews/${id}`)
  },
  
  /**
   * 创建不合格品评审
   * @param data 不合格品评审数据
   * @returns 创建后的不合格品评审
   */
  createNcReview: (data: Omit<NCReview, 'id'>) => {
    return api.post(`${API_PREFIX}/nc-reviews`, data)
  },
  
  /**
   * 更新不合格品评审
   * @param id 评审ID
   * @param data 更新数据
   * @returns 更新后的不合格品评审
   */
  updateNcReview: (id: string | number, data: Partial<NCReview>) => {
    return api.put(`${API_PREFIX}/nc-reviews/${id}`, data)
  },
  
  /**
   * 审批不合格品评审
   * @param id 评审ID
   * @param data 审批数据
   * @returns 审批结果
   */
  approveNcReview: (id: string | number, data: { reviewStatus: 'approved' | 'rejected'; reviewOpinion?: string }) => {
    return api.put(`${API_PREFIX}/nc-reviews/${id}/approve`, data)
  },
  
  /**
   * 批量审批不合格品评审
   * @param data 批量审批数据
   * @returns 批量审批结果
   */
  batchApproveNcReviews: (data: { ids: (string | number)[]; reviewStatus: 'approved' | 'rejected'; reviewOpinion?: string }) => {
    return api.put(`${API_PREFIX}/nc-reviews/batch-approve`, data)
  }
}

// 不合格品管理模块 - 不合格品处理管理
const NCDisposalAPI = {
  /**
   * 获取不合格品处理列表
   * @param params 查询参数
   * @returns 不合格品处理列表
   */
  getNcDisposals: (params?: any) => {
    return api.get(`${API_PREFIX}/nc-disposals`, { params })
  },
  
  /**
   * 获取不合格品处理详情
   * @param id 处理ID
   * @returns 不合格品处理详情
   */
  getNcDisposalById: (id: string | number) => {
    return api.get(`${API_PREFIX}/nc-disposals/${id}`)
  },
  
  /**
   * 创建不合格品处理
   * @param data 不合格品处理数据
   * @returns 创建后的不合格品处理
   */
  createNcDisposal: (data: Omit<NCDisposal, 'id'>) => {
    return api.post(`${API_PREFIX}/nc-disposals`, data)
  },
  
  /**
   * 更新不合格品处理
   * @param id 处理ID
   * @param data 更新数据
   * @returns 更新后的不合格品处理
   */
  updateNcDisposal: (id: string | number, data: Partial<NCDisposal>) => {
    return api.put(`${API_PREFIX}/nc-disposals/${id}`, data)
  },
  
  /**
   * 开始处理
   * @param id 处理ID
   * @returns 开始处理结果
   */
  startProcessing: (id: string | number) => {
    return api.put(`${API_PREFIX}/nc-disposals/${id}/start`)
  },
  
  /**
   * 完成处理
   * @param id 处理ID
   * @param data 处理结果数据
   * @returns 完成处理结果
   */
  completeProcessing: (id: string | number, data: { processResult: string }) => {
    return api.put(`${API_PREFIX}/nc-disposals/${id}/complete`, data)
  },
  
  /**
   * 批量开始处理
   * @param data 批量处理数据
   * @returns 批量开始处理结果
   */
  batchStartProcessing: (data: { ids: (string | number)[] }) => {
    return api.put(`${API_PREFIX}/nc-disposals/batch-start`, data)
  }
}

// 不合格品管理模块 - 不合格品追踪管理
const NCTrackingAPI = {
  /**
   * 获取不合格品追踪列表
   * @param params 查询参数
   * @returns 不合格品追踪列表
   */
  getNcTrackings: (params?: any) => {
    return api.get(`${API_PREFIX}/nc-trackings`, { params })
  },
  
  /**
   * 获取不合格品追踪详情
   * @param id 追踪ID
   * @returns 不合格品追踪详情
   */
  getNcTrackingById: (id: string | number) => {
    return api.get(`${API_PREFIX}/nc-trackings/${id}`)
  },
  
  /**
   * 创建不合格品追踪
   * @param data 不合格品追踪数据
   * @returns 创建后的不合格品追踪
   */
  createNcTracking: (data: Omit<NCTracking, 'id'>) => {
    return api.post(`${API_PREFIX}/nc-trackings`, data)
  },
  
  /**
   * 更新不合格品追踪
   * @param id 追踪ID
   * @param data 更新数据
   * @returns 更新后的不合格品追踪
   */
  updateNcTracking: (id: string | number, data: Partial<NCTracking>) => {
    return api.put(`${API_PREFIX}/nc-trackings/${id}`, data)
  },
  
  /**
   * 开始追踪
   * @param id 追踪ID
   * @returns 开始追踪结果
   */
  startTracking: (id: string | number) => {
    return api.put(`${API_PREFIX}/nc-trackings/${id}/start`)
  },
  
  /**
   * 完成追踪
   * @param id 追踪ID
   * @param data 追踪结果数据
   * @returns 完成追踪结果
   */
  completeTracking: (id: string | number, data: { effectiveness: 'effective' | 'ineffective'; improvementSuggestions?: string }) => {
    return api.put(`${API_PREFIX}/nc-trackings/${id}/complete`, data)
  }
}

// 质量异常管理模块 - 异常报告管理
const AnomalyReportAPI = {
  /**
   * 获取异常报告列表
   * @param params 查询参数
   * @returns 异常报告列表
   */
  getAnomalyReports: (params?: any) => {
    return api.get(`${API_PREFIX}/anomaly-reports`, { params })
  },
  
  /**
   * 获取异常报告详情
   * @param id 报告ID
   * @returns 异常报告详情
   */
  getAnomalyReportById: (id: string | number) => {
    return api.get(`${API_PREFIX}/anomaly-reports/${id}`)
  },
  
  /**
   * 创建异常报告
   * @param data 异常报告数据
   * @returns 创建后的异常报告
   */
  createAnomalyReport: (data: Omit<AnomalyReport, 'id'>) => {
    return api.post(`${API_PREFIX}/anomaly-reports`, data)
  },
  
  /**
   * 更新异常报告
   * @param id 报告ID
   * @param data 更新数据
   * @returns 更新后的异常报告
   */
  updateAnomalyReport: (id: string | number, data: Partial<AnomalyReport>) => {
    return api.put(`${API_PREFIX}/anomaly-reports/${id}`, data)
  },
  
  /**
   * 删除异常报告
   * @param id 报告ID
   * @returns 删除结果
   */
  deleteAnomalyReport: (id: string | number) => {
    return api.delete(`${API_PREFIX}/anomaly-reports/${id}`)
  },
  
  /**
   * 开始调查
   * @param id 报告ID
   * @returns 处理结果
   */
  startInvestigation: (id: string | number) => {
    return api.put(`${API_PREFIX}/anomaly-reports/${id}/start-investigation`)
  },

  /**
   * 解决异常
   * @param id 报告ID
   * @returns 处理结果
   */
  resolve: (id: string | number) => {
    return api.put(`${API_PREFIX}/anomaly-reports/${id}/resolve`)
  },

  /**
   * 关闭异常
   * @param id 报告ID
   * @returns 处理结果
   */
  close: (id: string | number) => {
    return api.put(`${API_PREFIX}/anomaly-reports/${id}/close`)
  }
}

// 质量异常管理模块 - 异常分析管理
const AnomalyAnalysisAPI = {
  /**
   * 获取异常分析列表
   * @param params 查询参数
   * @returns 异常分析列表
   */
  getAnomalyAnalyses: (params?: any) => {
    return api.get(`${API_PREFIX}/anomaly-analyses`, { params })
  },
  
  /**
   * 获取异常分析详情
   * @param id 分析ID
   * @returns 异常分析详情
   */
  getAnomalyAnalysisById: (id: string | number) => {
    return api.get(`${API_PREFIX}/anomaly-analyses/${id}`)
  },
  
  /**
   * 创建异常分析
   * @param data 异常分析数据
   * @returns 创建后的异常分析
   */
  createAnomalyAnalysis: (data: Omit<AnomalyAnalysis, 'id'>) => {
    return api.post(`${API_PREFIX}/anomaly-analyses`, data)
  },
  
  /**
   * 更新异常分析
   * @param id 分析ID
   * @param data 更新数据
   * @returns 更新后的异常分析
   */
  updateAnomalyAnalysis: (id: string | number, data: Partial<AnomalyAnalysis>) => {
    return api.put(`${API_PREFIX}/anomaly-analyses/${id}`, data)
  },
  
  /**
   * 完成异常分析
   * @param id 分析ID
   * @returns 完成结果
   */
  completeAnalysis: (id: string | number) => {
    return api.put(`${API_PREFIX}/anomaly-analyses/${id}/complete`)
  }
}

// 质量异常管理模块 - 异常处理管理
const QmsAnomalyDisposalAPI = {
  /**
   * 获取异常处理列表
   * @param params 查询参数
   * @returns 异常处理列表
   */
  getAnomalyDisposals: (params?: any) => {
    return api.get(`${API_PREFIX}/anomaly-disposals`, { params })
  },
  
  /**
   * 获取异常处理详情
   * @param id 处理ID
   * @returns 异常处理详情
   */
  getAnomalyDisposalById: (id: string | number) => {
    return api.get(`${API_PREFIX}/anomaly-disposals/${id}`)
  },
  
  /**
   * 创建异常处理
   * @param data 异常处理数据
   * @returns 创建后的异常处理
   */
  createAnomalyDisposal: (data: Omit<QmsAnomalyDisposal, 'id'>) => {
    return api.post(`${API_PREFIX}/anomaly-disposals`, data)
  },
  
  /**
   * 更新异常处理
   * @param id 处理ID
   * @param data 更新数据
   * @returns 更新后的异常处理
   */
  updateAnomalyDisposal: (id: string | number, data: Partial<QmsAnomalyDisposal>) => {
    return api.put(`${API_PREFIX}/anomaly-disposals/${id}`, data)
  },
  
  /**
   * 验证异常处理结果
   * @param id 处理ID
   * @param data 验证数据
   * @returns 验证结果
   */
  verifyDisposal: (id: string | number, data: { verificationResult: 'passed' | 'failed'; verifier: string }) => {
    return api.put(`${API_PREFIX}/anomaly-disposals/${id}/verify`, data)
  }
}

// 质量异常管理模块 - 预防措施管理
const CAPAAPI = {
  /**
   * 获取预防措施列表
   * @param params 查询参数
   * @returns 预防措施列表
   */
  getCAPAs: (params?: any) => {
    return api.get(`${API_PREFIX}/capas`, { params })
  },
  
  /**
   * 获取预防措施详情
   * @param id 措施ID
   * @returns 预防措施详情
   */
  getCAPAById: (id: string | number) => {
    return api.get(`${API_PREFIX}/capas/${id}`)
  },
  
  /**
   * 创建预防措施
   * @param data 预防措施数据
   * @returns 创建后的预防措施
   */
  createCAPA: (data: Omit<CAPA, 'id'>) => {
    return api.post(`${API_PREFIX}/capas`, data)
  },
  
  /**
   * 更新预防措施
   * @param id 措施ID
   * @param data 更新数据
   * @returns 更新后的预防措施
   */
  updateCAPA: (id: string | number, data: Partial<CAPA>) => {
    return api.put(`${API_PREFIX}/capas/${id}`, data)
  },
  
  /**
   * 审核预防措施
   * @param id 措施ID
   * @param data 审核数据
   * @returns 审核结果
   */
  reviewCAPA: (id: string | number, data: { reviewResult: 'approved' | 'rejected'; reviewer: string }) => {
    return api.put(`${API_PREFIX}/capas/${id}/review`, data)
  },
  
  /**
   * 验证预防措施
   * @param id 措施ID
   * @param data 验证数据
   * @returns 验证结果
   */
  verifyCAPA: (id: string | number, data: { verifyResult: string; verifyStatus: 'passed' | 'failed' }) => {
    return api.put(`${API_PREFIX}/capas/${id}/verify`, data)
  }
}

// 质量数据分析模块 - 质量数据采集管理
const DataCollectionAPI = {
  /**
   * 获取质量数据采集列表
   * @param params 查询参数
   * @returns 质量数据采集列表
   */
  getDataCollections: (params?: any) => {
    return api.get(`${API_PREFIX}/data-collections`, { params })
  },
  
  /**
   * 获取质量数据采集详情
   * @param id 采集ID
   * @returns 质量数据采集详情
   */
  getDataCollectionById: (id: string | number) => {
    return api.get(`${API_PREFIX}/data-collections/${id}`)
  },
  
  /**
   * 创建质量数据采集
   * @param data 质量数据采集数据
   * @returns 创建后的质量数据采集
   */
  createDataCollection: (data: Omit<DataCollection, 'id'>) => {
    return api.post(`${API_PREFIX}/data-collections`, data)
  },
  
  /**
   * 更新质量数据采集
   * @param id 采集ID
   * @param data 更新数据
   * @returns 更新后的质量数据采集
   */
  updateDataCollection: (id: string | number, data: Partial<DataCollection>) => {
    return api.put(`${API_PREFIX}/data-collections/${id}`, data)
  },
  
  /**
   * 提交质量数据采集
   * @param id 采集ID
   * @returns 提交结果
   */
  submitDataCollection: (id: string | number) => {
    return api.put(`${API_PREFIX}/data-collections/${id}/submit`)
  },

  /**
   * 删除质量数据采集
   * @param id 采集ID
   * @returns 删除结果
   */
  deleteDataCollection: (id: string | number) => {
    return api.delete(`${API_PREFIX}/data-collections/${id}`)
  },
  
  /**
   * 导入质量数据
   * @param file 文件对象
   * @returns 导入结果
   */
  importData: (file: FormData) => {
    return api.post(`${API_PREFIX}/data-collections/import`, file, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  }
}

// 质量数据分析模块 - 质量统计分析管理
const StatisticalAnalysisAPI = {
  /**
   * 获取质量统计数据
   * @param params 查询参数
   * @returns 质量统计数据
   */
  getStatisticalData: (params: { 
    dataType: string; 
    period: string; 
    startTime?: string; 
    endTime?: string;
    productIds?: (string | number)[]
  }) => {
    return api.get(`${API_PREFIX}/statistical-analysis`, { params })
  },
  
  /**
   * 获取帕累托图数据
   * @param params 查询参数
   * @returns 帕累托图数据
   */
  getParetoData: (params: { 
    category: string; 
    startTime: string; 
    endTime: string;
    productIds?: (string | number)[]
  }) => {
    return api.get(`${API_PREFIX}/statistical-analysis/pareto`, { params })
  },
  
  /**
   * 获取SPC控制图数据
   * @param params 查询参数
   * @returns SPC控制图数据
   */
  getSPCData: (params: { 
    chartType: 'xbar-r' | 'xbar-s' | 'p' | 'np' | 'c' | 'u'; 
    productId: string | number; 
    parameter: string; 
    startTime: string; 
    endTime: string;
    sampleSize?: number
  }) => {
    return api.get(`${API_PREFIX}/statistical-analysis/spc`, { params })
  },
  
  /**
   * 获取合格率数据
   * @param params 查询参数
   * @returns 合格率数据
   */
  getPassRateData: (params: { 
    productIds?: (string | number)[]; 
    startTime: string; 
    endTime: string;
    groupBy: 'day' | 'week' | 'month' | 'quarter' | 'year'
  }) => {
    return api.get(`${API_PREFIX}/statistical-analysis/pass-rate`, { params })
  }
}

// 质量数据分析模块 - 质量报告生成管理
const ReportGenerationAPI = {
  /**
   * 获取质量报告列表
   * @param params 查询参数
   * @returns 质量报告列表
   */
  getReports: (params?: any) => {
    return api.get(`${API_PREFIX}/reports`, { params })
  },
  
  /**
   * 获取质量报告详情
   * @param id 报告ID
   * @returns 质量报告详情
   */
  getReportById: (id: string | number) => {
    return api.get(`${API_PREFIX}/reports/${id}`)
  },
  
  /**
   * 创建质量报告
   * @param data 质量报告数据
   * @returns 创建后的质量报告
   */
  createReport: (data: Omit<Report, 'id'>) => {
    return api.post(`${API_PREFIX}/reports`, data)
  },
  
  /**
   * 更新质量报告
   * @param id 报告ID
   * @param data 更新数据
   * @returns 更新后的质量报告
   */
  updateReport: (id: string | number, data: Partial<Report>) => {
    return api.put(`${API_PREFIX}/reports/${id}`, data)
  },
  
  /**
   * 删除质量报告
   * @param id 报告ID
   * @returns 删除结果
   */
  deleteReport: (id: string | number) => {
    return api.delete(`${API_PREFIX}/reports/${id}`)
  },
  
  /**
   * 生成质量报告
   * @param data 报告生成参数
   * @returns 生成结果
   */
  generateReport: (data: {
    reportType: Report['reportType'];
    period: string;
    productIds?: (string | number)[];
    startTime?: string;
    endTime?: string;
    templateId?: string | number
  }) => {
    return api.post(`${API_PREFIX}/reports/generate`, data)
  },
  
  /**
   * 导出质量报告
   * @param id 报告ID
   * @param format 导出格式
   * @returns 导出文件
   */
  exportReport: (id: string | number, format: 'pdf' | 'excel' | 'word') => {
    return api.get(`${API_PREFIX}/reports/${id}/export`, {
      params: { format },
      responseType: 'blob'
    })
  }
}

// 质量数据分析模块 - 质量趋势预测管理
const TrendForecastAPI = {
  /**
   * 获取质量趋势预测结果
   * @param params 查询参数
   * @returns 质量趋势预测结果
   */
  getForecastResults: (params?: any) => {
    return api.get(`${API_PREFIX}/forecast-results`, { params })
  },
  
  /**
   * 获取质量趋势预测详情
   * @param id 预测ID
   * @returns 质量趋势预测详情
   */
  getForecastResultById: (id: string | number) => {
    return api.get(`${API_PREFIX}/forecast-results/${id}`)
  },
  
  /**
   * 生成质量趋势预测
   * @param data 预测参数
   * @returns 生成结果
   */
  generateForecast: (data: {
    forecastType: string;
    productIds: (string | number)[];
    parameter: string;
    historicalPeriod: number;
    forecastPeriod: number;
    confidenceInterval?: number
  }) => {
    return api.post(`${API_PREFIX}/forecast-results/generate`, data)
  },
  
  /**
   * 获取预测趋势图数据
   * @param id 预测ID
   * @returns 预测趋势图数据
   */
  getForecastChartData: (id: string | number) => {
    return api.get(`${API_PREFIX}/forecast-results/${id}/chart-data`)
  }
}

// 导出所有API
export default {
  // 质量检验计划模块
  InspectionStandardAPI,
  InspectionPlanAPI,
  InspectionTaskAPI,
  InspectionResultAPI,
  
  // 不合格品管理模块
  NCRegistrationAPI,
  NCReviewAPI,
  NCDisposalAPI,
  NCTrackingAPI,
  
  // 质量异常管理模块
  AnomalyReportAPI,
  AnomalyAnalysisAPI,
  QmsAnomalyDisposalAPI,
  CAPAAPI,
  
  // 质量数据分析模块
  DataCollectionAPI,
  StatisticalAnalysisAPI,
  ReportGenerationAPI,
  TrendForecastAPI
}

// 导出单个API模块
export {
  // 质量检验计划模块
  InspectionStandardAPI,
  InspectionPlanAPI,
  InspectionTaskAPI,
  InspectionResultAPI,
  
  // 不合格品管理模块
  NCRegistrationAPI,
  NCReviewAPI,
  NCDisposalAPI,
  NCTrackingAPI,
  
  // 质量异常管理模块
  AnomalyReportAPI,
  AnomalyAnalysisAPI,
  QmsAnomalyDisposalAPI,
  CAPAAPI,
  
  // 质量数据分析模块
  DataCollectionAPI,
  StatisticalAnalysisAPI,
  ReportGenerationAPI,
  TrendForecastAPI
}
