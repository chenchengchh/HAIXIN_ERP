import request, { unwrapResponseData } from '../index'

const unwrap = async <T = any>(p: Promise<any>): Promise<{ data: T }> => {
  const res = await p
  return { data: unwrapResponseData<T>(res) as T }
}

const SCM_V1 = '/api/v1/scm'

export const forecastApi = {
  generateForecast: (period: string) => unwrap(request.post(`${SCM_V1}/forecast/generate`, null, { params: { period } })),
  getForecastList: (params?: any) => unwrap(request.get(`${SCM_V1}/forecast`, { params })),
  updateForecast: (id: number, data: any) => unwrap(request.put(`${SCM_V1}/forecast/${id}`, data)),

  getForecastVersions: (period: string) => unwrap(request.get(`${SCM_V1}/forecast/versions`, { params: { period } })),
  createForecastVersion: (period: string, fromVersionId?: number) =>
    unwrap(request.post(`${SCM_V1}/forecast/versions`, null, { params: { period, fromVersionId } })),
  publishForecastVersion: (id: number) => unwrap(request.post(`${SCM_V1}/forecast/versions/${id}/publish`, {})),
  rollbackForecastVersion: (id: number) => unwrap(request.post(`${SCM_V1}/forecast/versions/${id}/rollback`, {})),
  updateForecastsByVersion: (id: number, list: any[]) => unwrap(request.put(`${SCM_V1}/forecast/versions/${id}/forecasts`, list)),

  getForecastAccuracy: (period: string, versionId?: number) =>
    unwrap(request.get(`${SCM_V1}/forecast/analysis/accuracy`, { params: { period, versionId } })),
  getForecastAccuracyTrend: (start: string, end: string, versionId?: number) =>
    unwrap(request.get(`${SCM_V1}/forecast/analysis/accuracy-trend`, { params: { start, end, versionId } })),

  getForecastDashboardTrend: (period: string, months: number = 12) =>
    unwrap(request.get(`${SCM_V1}/forecast/dashboard/trend`, { params: { period, months } })),
  getForecastDashboardStatusSummary: (period: string) =>
    unwrap(request.get(`${SCM_V1}/forecast/dashboard/status-summary`, { params: { period } })),
  getForecastDashboardCategorySummary: (period: string, versionId?: number, top: number = 5) =>
    unwrap(request.get(`${SCM_V1}/forecast/dashboard/category-summary`, { params: { period, versionId, top } })),

  getForecastConfig: () => unwrap(request.get(`${SCM_V1}/forecast/config`)),
  saveForecastConfig: (data: any) => unwrap(request.put(`${SCM_V1}/forecast/config`, data)),
  getForecastDataSources: () => unwrap(request.get(`${SCM_V1}/forecast/data-sources`)),
  createForecastDataSource: (data: any) => unwrap(request.post(`${SCM_V1}/forecast/data-sources`, data)),
  updateForecastDataSource: (id: number, data: any) => unwrap(request.put(`${SCM_V1}/forecast/data-sources/${id}`, data)),
  deleteForecastDataSource: (id: number) => unwrap(request.delete(`${SCM_V1}/forecast/data-sources/${id}`)),
  testForecastDataSource: (id: number) => unwrap(request.post(`${SCM_V1}/forecast/data-sources/${id}/test`, {})),
  testForecastModel: (period?: string) => unwrap(request.post(`${SCM_V1}/forecast/model/test`, null, { params: { period } })),

  getMrpHistory: (params?: any) => unwrap(request.get(`${SCM_V1}/mrp/history`, { params })),
  runMrp: (data: any) => unwrap(request.post(`${SCM_V1}/mrp/run`, data)),
  getMrpResults: (planId: number) => unwrap(request.get(`${SCM_V1}/mrp/results/${planId}`)),
  confirmMrpResult: (id: number, operator?: string) =>
    unwrap(request.put(`${SCM_V1}/mrp/results/${id}/confirm`, null, { params: { operator } })),
  rejectMrpResult: (id: number, reason: string, operator?: string) =>
    unwrap(request.put(`${SCM_V1}/mrp/results/${id}/reject`, null, { params: { reason, operator } })),
  batchConfirmMrpResults: (ids: number[], operator?: string) =>
    unwrap(request.post(`${SCM_V1}/mrp/results/batch/confirm`, { ids }, { params: { operator } })),
  batchRejectMrpResults: (ids: number[], reason: string, operator?: string) =>
    unwrap(request.post(`${SCM_V1}/mrp/results/batch/reject`, { ids, reason }, { params: { operator } })),
  releasePlan: (planId: number) => unwrap(request.post(`${SCM_V1}/mrp/release/${planId}`, {})),
  deleteMrpPlan: (planId: number) => unwrap(request.delete(`${SCM_V1}/mrp/history/${planId}`)),

  getControlTowerKpis: () => unwrap(request.get(`${SCM_V1}/control-tower/kpis`)),
  getControlTowerKpiTrend: (metric: string, from: string, to: string) =>
    unwrap(request.get(`${SCM_V1}/control-tower/kpis/trend`, { params: { metric, from, to } })),
  getControlTowerAlerts: () => unwrap(request.get(`${SCM_V1}/control-tower/alerts`)),
  getNetworkData: () => unwrap(request.get(`${SCM_V1}/control-tower/network`)),

  getInventoryHealthAlerts: (limit: number = 50) => unwrap(request.get(`${SCM_V1}/inventory-health/alerts`, { params: { limit } })),
  getInventoryHealthSummary: (days: number = 30) => unwrap(request.get(`${SCM_V1}/inventory-health/summary`, { params: { days } })),
  getInventoryHealthDistribution: (dimension: string = 'abc') =>
    unwrap(request.get(`${SCM_V1}/inventory-health/distribution`, { params: { dimension } })),
  getInventoryHealthTurnoverTrend: (days: number = 30) =>
    unwrap(request.get(`${SCM_V1}/inventory-health/turnover/trend`, { params: { days } })),

  calculateStrategies: () => unwrap(request.post(`${SCM_V1}/inventory-optimization/calculate`, {})),
  getStrategies: (params?: any) => unwrap(request.get(`${SCM_V1}/inventory-optimization/strategies`, { params })),
  createStrategy: (data: any) => unwrap(request.post(`${SCM_V1}/inventory-optimization/strategies`, data)),
  updateStrategy: (id: number, data: any) => unwrap(request.put(`${SCM_V1}/inventory-optimization/strategies/${id}`, data)),
  deleteStrategy: (id: number) => unwrap(request.delete(`${SCM_V1}/inventory-optimization/strategies/${id}`)),

  getShipments: (params?: any) => unwrap(request.get(`${SCM_V1}/logistics/shipments`, { params })),
  optimizeRoute: (data: any) => unwrap(request.post(`${SCM_V1}/route-optimization/optimize`, data)),
  getSupplierCollaborationPurchaseOrders: (params?: any) =>
    unwrap(request.get(`${SCM_V1}/supplier-collaboration/purchase-orders`, { params })),
  getLogisticsProviders: () => unwrap(request.get(`${SCM_V1}/logistics/providers`))
}

/**
 * 生产完工事实查询参数（B6 闭环前端 F2 页面）
 */
export interface CompletionFactQueryParams {
  /** ERP 生产单号（模糊匹配） */
  erpProductionNo?: string
  /** MES 工单号（模糊匹配） */
  workOrderNo?: string
  /** SCM 侧订单状态（精确匹配，如 COMPLETED） */
  scmOrderStatus?: string
  /** 完工时间下限（含，ISO-8601） */
  completedTimeFrom?: string
  /** 完工时间上限（含，ISO-8601） */
  completedTimeTo?: string
  /** 页码（从 0 开始） */
  page?: number
  /** 每页条数 */
  size?: number
}

/**
 * 生产完工事实 API（B6 闭环：MES 完工回流 SCM 查询）
 *
 * 对应后端 GET /api/v1/scm/completion-facts，供前端 F2（SCM 完工事实页）调用。
 */
export const completionFactApi = {
  /**
   * 分页查询生产完工事实
   * @param params 查询参数（所有字段可选）
   * @returns 完工事实分页结果
   */
  fetchCompletionFacts: (params?: CompletionFactQueryParams) =>
    unwrap(request.get(`${SCM_V1}/completion-facts`, { params }))
}

export const reportApi = {
  getPurchaseReconciliationPage: (params?: any) => unwrap(request.get(`${SCM_V1}/reports/purchase-reconciliation/page`, { params })),
  getPurchaseReconciliationSummary: (params?: any) =>
    unwrap(request.get(`${SCM_V1}/reports/purchase-reconciliation/summary`, { params })),
  getPurchaseReconciliationReasonSummary: (params?: any) =>
    unwrap(request.get(`${SCM_V1}/reports/purchase-reconciliation/reason-summary`, { params })),
  getPurchaseReconciliationSupplierAggPage: (params?: any) =>
    unwrap(request.get(`${SCM_V1}/reports/purchase-reconciliation/aggregate/suppliers/page`, { params })),
  getPurchaseReconciliationMaterialAggPage: (params?: any) =>
    unwrap(request.get(`${SCM_V1}/reports/purchase-reconciliation/aggregate/materials/page`, { params })),
  getQcHoldOrdersPage: (params?: any) => unwrap(request.get(`${SCM_V1}/reports/purchase-orders/qc-hold/page`, { params })),
  exportPurchaseReconciliation: (params?: any) =>
    request.get(`${SCM_V1}/reports/purchase-reconciliation/export`, { params, responseType: 'blob' as any })
}
