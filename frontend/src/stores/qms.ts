import { defineStore } from 'pinia'
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
  AnomalyDisposal,
  CAPA,
  // 质量数据分析模块
  DataCollection,
  StatisticalData,
  Report,
  ForecastResult
} from '../types/qms'

// 定义QMS模块的状态类型
interface QMSState {
  // 质量检验计划模块
  inspectionStandards: InspectionStandard[]
  inspectionPlans: InspectionPlan[]
  inspectionTasks: InspectionTask[]
  inspectionResults: InspectionResult[]
  
  // 不合格品管理模块
  ncRegistrations: NCRegistration[]
  ncReviews: NCReview[]
  ncDisposals: NCDisposal[]
  ncTrackings: NCTracking[]
  
  // 异常报告
  anomalyReports: AnomalyReport[]
  // 异常分析
  anomalyAnalyses: AnomalyAnalysis[]
  // 异常处理
  anomalyDisposals: AnomalyDisposal[]
  // 预防措施
  capas: CAPA[]
  
  // 质量数据分析模块
  dataCollections: DataCollection[]
  statisticalData: StatisticalData[]
  reports: Report[]
  forecastResults: ForecastResult[]
  
  // 当前选中项
  currentReport: Report | null
  currentAnomaly: AnomalyReport | null
  currentInspectionStandard: InspectionStandard | null
  currentInspectionPlan: InspectionPlan | null
  currentInspectionTask: InspectionTask | null
  currentNcRegistration: NCRegistration | null
}

// 定义QMS模块的状态管理
export const useQMSStore = defineStore('qms', {
  state: (): QMSState => ({
    // 质量检验计划模块
    inspectionStandards: [],
    inspectionPlans: [],
    inspectionTasks: [],
    inspectionResults: [],
    
    // 不合格品管理模块
    ncRegistrations: [],
    ncReviews: [],
    ncDisposals: [],
    ncTrackings: [],
    
    // 异常报告
    anomalyReports: [],
    // 异常分析
    anomalyAnalyses: [],
    // 异常处理
    anomalyDisposals: [],
    // 预防措施
    capas: [],
    
    // 质量数据分析模块
    dataCollections: [],
    statisticalData: [],
    reports: [],
    forecastResults: [],
    
    // 当前选中项
    currentReport: null,
    currentAnomaly: null,
    currentInspectionStandard: null,
    currentInspectionPlan: null,
    currentInspectionTask: null,
    currentNcRegistration: null
  }),
  
  getters: {
    // 质量检验计划模块
    getInspectionStandardCount: (state) => state.inspectionStandards.length,
    getActiveInspectionStandardCount: (state) => {
      return state.inspectionStandards.filter(standard => standard.status === 'active').length
    },
    getInspectionPlanCount: (state) => state.inspectionPlans.length,
    getPendingInspectionTaskCount: (state) => {
      return state.inspectionTasks.filter(task => task.status === 'pending').length
    },
    getCompletedInspectionResultCount: (state) => {
      return state.inspectionResults.filter(result => result.auditStatus === 'approved').length
    },
    
    // 不合格品管理模块
    getNcRegistrationCount: (state) => state.ncRegistrations.length,
    getPendingNcReviewCount: (state) => {
      return state.ncReviews.filter(review => review.reviewStatus === 'pending').length
    },
    getCompletedNcDisposalCount: (state) => {
      return state.ncDisposals.filter(disposal => disposal.disposalStatus === 'completed').length
    },
    getEffectiveNcTrackingCount: (state) => {
      return state.ncTrackings.filter(tracking => tracking.effectiveness === 'effective').length
    },
    
    // 质量异常管理模块
    getAnomalyReportCount: (state) => state.anomalyReports.length,
    getCompletedAnomalyAnalysisCount: (state) => {
      return state.anomalyAnalyses.filter(analysis => analysis.analysisStatus === 'completed').length
    },
    getCompletedAnomalyDisposalCount: (state) => {
      return state.anomalyDisposals.filter(disposal => disposal.disposalStatus === 'completed').length
    },
    getVerifiedCAPACount: (state) => {
      return state.capas.filter(capa => capa.verifyStatus === 'passed').length
    },
    
    // 质量数据分析模块
    getReportList: (state) => state.reports,
    getCompletedForecastCount: (state) => {
      return state.forecastResults.filter(result => result.status === 'completed').length
    }
  },
  
  actions: {
    // 质量检验计划模块
    
    // 检验标准管理
    setInspectionStandards(standards: InspectionStandard[]) {
      this.inspectionStandards = standards
    },
    
    addInspectionStandard(standard: InspectionStandard) {
      this.inspectionStandards.unshift(standard)
    },
    
    updateInspectionStandard(standard: InspectionStandard) {
      const index = this.inspectionStandards.findIndex(item => item.id === standard.id)
      if (index !== -1) {
        this.inspectionStandards[index] = standard
      }
    },
    
    deleteInspectionStandard(id: string) {
      const index = this.inspectionStandards.findIndex(item => item.id === id)
      if (index !== -1) {
        this.inspectionStandards.splice(index, 1)
      }
    },
    
    setCurrentInspectionStandard(standard: InspectionStandard | null) {
      this.currentInspectionStandard = standard
    },
    
    // 检验计划管理
    setInspectionPlans(plans: InspectionPlan[]) {
      this.inspectionPlans = plans
    },
    
    addInspectionPlan(plan: InspectionPlan) {
      this.inspectionPlans.unshift(plan)
    },
    
    updateInspectionPlan(plan: InspectionPlan) {
      const index = this.inspectionPlans.findIndex(item => item.id === plan.id)
      if (index !== -1) {
        this.inspectionPlans[index] = plan
      }
    },
    
    deleteInspectionPlan(id: string) {
      const index = this.inspectionPlans.findIndex(item => item.id === id)
      if (index !== -1) {
        this.inspectionPlans.splice(index, 1)
      }
    },
    
    setCurrentInspectionPlan(plan: InspectionPlan | null) {
      this.currentInspectionPlan = plan
    },
    
    // 检验任务管理
    setInspectionTasks(tasks: InspectionTask[]) {
      this.inspectionTasks = tasks
    },
    
    addInspectionTask(task: InspectionTask) {
      this.inspectionTasks.unshift(task)
    },
    
    updateInspectionTask(task: InspectionTask) {
      const index = this.inspectionTasks.findIndex(item => item.id === task.id)
      if (index !== -1) {
        this.inspectionTasks[index] = task
      }
    },
    
    deleteInspectionTask(id: string) {
      const index = this.inspectionTasks.findIndex(item => item.id === id)
      if (index !== -1) {
        this.inspectionTasks.splice(index, 1)
      }
    },
    
    setCurrentInspectionTask(task: InspectionTask | null) {
      this.currentInspectionTask = task
    },
    
    // 检验结果管理
    setInspectionResults(results: InspectionResult[]) {
      this.inspectionResults = results
    },
    
    addInspectionResult(result: InspectionResult) {
      this.inspectionResults.unshift(result)
    },
    
    updateInspectionResult(result: InspectionResult) {
      const index = this.inspectionResults.findIndex(item => item.id === result.id)
      if (index !== -1) {
        this.inspectionResults[index] = result
      }
    },
    
    // 不合格品管理模块
    
    // 不合格品登记管理
    setNcRegistrations(registrations: NCRegistration[]) {
      this.ncRegistrations = registrations
    },
    
    addNcRegistration(registration: NCRegistration) {
      this.ncRegistrations.unshift(registration)
    },
    
    updateNcRegistration(registration: NCRegistration) {
      const index = this.ncRegistrations.findIndex(item => item.id === registration.id)
      if (index !== -1) {
        this.ncRegistrations[index] = registration
      }
    },
    
    deleteNcRegistration(id: string) {
      const index = this.ncRegistrations.findIndex(item => item.id === id)
      if (index !== -1) {
        this.ncRegistrations.splice(index, 1)
      }
    },
    
    setCurrentNcRegistration(registration: NCRegistration | null) {
      this.currentNcRegistration = registration
    },
    
    // 不合格品评审管理
    setNcReviews(reviews: NCReview[]) {
      this.ncReviews = reviews
    },
    
    addNcReview(review: NCReview) {
      this.ncReviews.unshift(review)
    },
    
    updateNcReview(review: NCReview) {
      const index = this.ncReviews.findIndex(item => item.id === review.id)
      if (index !== -1) {
        this.ncReviews[index] = review
      }
    },
    
    // 不合格品处理管理
    setNcDisposals(disposals: NCDisposal[]) {
      this.ncDisposals = disposals
    },
    
    addNcDisposal(disposal: NCDisposal) {
      this.ncDisposals.unshift(disposal)
    },
    
    updateNcDisposal(disposal: NCDisposal) {
      const index = this.ncDisposals.findIndex(item => item.id === disposal.id)
      if (index !== -1) {
        this.ncDisposals[index] = disposal
      }
    },
    
    // 不合格品追踪管理
    setNcTrackings(trackings: NCTracking[]) {
      this.ncTrackings = trackings
    },
    
    addNcTracking(tracking: NCTracking) {
      this.ncTrackings.unshift(tracking)
    },
    
    updateNcTracking(tracking: NCTracking) {
      const index = this.ncTrackings.findIndex(item => item.id === tracking.id)
      if (index !== -1) {
        this.ncTrackings[index] = tracking
      }
    },
    
    // 质量异常管理模块
    
    // 设置异常报告列表
    setAnomalyReports(reports: AnomalyReport[]) {
      this.anomalyReports = reports
    },
    
    // 添加异常报告
    addAnomalyReport(report: AnomalyReport) {
      this.anomalyReports.unshift(report)
    },
    
    // 更新异常报告
    updateAnomalyReport(report: AnomalyReport) {
      const index = this.anomalyReports.findIndex(item => item.id === report.id)
      if (index !== -1) {
        this.anomalyReports[index] = report
      }
    },
    
    // 删除异常报告
    deleteAnomalyReport(id: string) {
      const index = this.anomalyReports.findIndex(item => item.id === id)
      if (index !== -1) {
        this.anomalyReports.splice(index, 1)
      }
    },
    
    // 设置当前选中的异常
    setCurrentAnomaly(anomaly: AnomalyReport | null) {
      this.currentAnomaly = anomaly
    },
    
    // 设置异常分析列表
    setAnomalyAnalyses(analyses: AnomalyAnalysis[]) {
      this.anomalyAnalyses = analyses
    },
    
    // 添加异常分析
    addAnomalyAnalysis(analysis: AnomalyAnalysis) {
      this.anomalyAnalyses.unshift(analysis)
    },
    
    // 更新异常分析
    updateAnomalyAnalysis(analysis: AnomalyAnalysis) {
      const index = this.anomalyAnalyses.findIndex(item => item.id === analysis.id)
      if (index !== -1) {
        this.anomalyAnalyses[index] = analysis
      }
    },
    
    // 设置异常处理列表
    setAnomalyDisposals(disposals: AnomalyDisposal[]) {
      this.anomalyDisposals = disposals
    },
    
    // 添加异常处理
    addAnomalyDisposal(disposal: AnomalyDisposal) {
      this.anomalyDisposals.unshift(disposal)
    },
    
    // 更新异常处理
    updateAnomalyDisposal(disposal: AnomalyDisposal) {
      const index = this.anomalyDisposals.findIndex(item => item.id === disposal.id)
      if (index !== -1) {
        this.anomalyDisposals[index] = disposal
      }
    },
    
    // 设置预防措施列表
    setCAPAs(capas: CAPA[]) {
      this.capas = capas
    },
    
    // 添加预防措施
    addCAPA(capa: CAPA) {
      this.capas.unshift(capa)
    },
    
    // 更新预防措施
    updateCAPA(capa: CAPA) {
      const index = this.capas.findIndex(item => item.id === capa.id)
      if (index !== -1) {
        this.capas[index] = capa
      }
    },
    
    // 质量数据分析模块
    
    // 设置数据采集列表
    setDataCollections(collections: DataCollection[]) {
      this.dataCollections = collections
    },
    
    // 添加数据采集
    addDataCollection(collection: DataCollection) {
      this.dataCollections.unshift(collection)
    },
    
    // 更新数据采集
    updateDataCollection(collection: DataCollection) {
      const index = this.dataCollections.findIndex(item => item.id === collection.id)
      if (index !== -1) {
        this.dataCollections[index] = collection
      }
    },
    
    // 设置统计数据列表
    setStatisticalData(data: StatisticalData[]) {
      this.statisticalData = data
    },
    
    // 设置报告列表
    setReports(reports: Report[]) {
      this.reports = reports
    },
    
    // 添加报告
    addReport(report: Report) {
      this.reports.unshift(report)
    },
    
    // 更新报告
    updateReport(report: Report) {
      const index = this.reports.findIndex(item => item.id === report.id)
      if (index !== -1) {
        this.reports[index] = report
      }
    },
    
    // 删除报告
    deleteReport(id: string) {
      const index = this.reports.findIndex(item => item.id === id)
      if (index !== -1) {
        this.reports.splice(index, 1)
      }
    },
    
    // 设置当前选中的报告
    setCurrentReport(report: Report | null) {
      this.currentReport = report
    },
    
    // 设置预测结果列表
    setForecastResults(results: ForecastResult[]) {
      this.forecastResults = results
    }
  }
})
