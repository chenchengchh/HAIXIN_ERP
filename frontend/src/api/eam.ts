import api from '@/api'

// 资产管理 API
export function getAssets() {
  return api.get('/api/v1/eam/assets')
}

export function createAsset(data: any) {
  return api.post('/api/v1/eam/assets', data)
}

export function updateAsset(id: number, data: any) {
  return api.put(`/api/v1/eam/assets/${id}`, data)
}

export function deleteAsset(id: number) {
  return api.delete(`/api/v1/eam/assets/${id}`)
}

export function getCategories() {
  return api.get('/api/v1/eam/assets/categories')
}

export function createCategory(data: any) {
  return api.post('/api/v1/eam/assets/categories', data)
}

export function updateCategory(id: number, data: any) {
  /**
   * 更新设备分类
   * @param id 分类ID
   * @param data 分类数据
   */
  return api.put(`/api/v1/eam/assets/categories/${id}`, data)
}

export function deleteCategory(id: number) {
  /**
   * 删除设备分类
   * @param id 分类ID
   */
  return api.delete(`/api/v1/eam/assets/categories/${id}`)
}

// 设备层次结构 API
export function getHierarchies() {
  return api.get('/api/v1/eam/assets/hierarchy')
}

export function createHierarchy(data: any) {
  /**
   * 新增设备层次结构节点
   * @param data 节点数据（parentId/childId/componentType/quantity/remark）
   */
  return api.post('/api/v1/eam/assets/hierarchy', data)
}

export function updateHierarchy(id: number, data: any) {
  /**
   * 更新设备层次结构节点
   * @param id 节点ID
   * @param data 节点数据
   */
  return api.put(`/api/v1/eam/assets/hierarchy/${id}`, data)
}

export function deleteHierarchy(id: number) {
  /**
   * 删除设备层次结构节点
   * @param id 节点ID
   */
  return api.delete(`/api/v1/eam/assets/hierarchy/${id}`)
}

// 设备文档管理 API
export function getDocuments() {
  return api.get('/api/v1/eam/assets/documents')
}

export function createDocument(data: any) {
  /**
   * 新增设备文档（content 为带 base64: 前缀的文件内容，可为空）
   * @param data 文档数据
   */
  return api.post('/api/v1/eam/assets/documents', data)
}

export function updateDocument(id: number, data: any) {
  /**
   * 更新设备文档元数据（不传 content 时后端保留原文件内容）
   * @param id 文档ID
   * @param data 文档数据
   */
  return api.put(`/api/v1/eam/assets/documents/${id}`, data)
}

export function deleteDocument(id: number) {
  /**
   * 删除设备文档
   * @param id 文档ID
   */
  return api.delete(`/api/v1/eam/assets/documents/${id}`)
}

export function downloadDocument(id: number) {
  /**
   * 下载设备文档（返回含 Base64 文件内容的数据）
   * @param id 文档ID
   */
  return api.get(`/api/v1/eam/assets/documents/${id}/download`)
}

// 维护管理 API
export function getMaintenancePlans() {
  return api.get('/api/v1/eam/maintenance/plans')
}

export function createMaintenancePlan(data: any) {
  return api.post('/api/v1/eam/maintenance/plans', data)
}

export function updateMaintenancePlan(id: number, data: any) {
  return api.put(`/api/v1/eam/maintenance/plans/${id}`, data)
}

export function deleteMaintenancePlan(id: number) {
  /**
   * 删除维护计划
   * @param id 计划ID
   */
  return api.delete(`/api/v1/eam/maintenance/plans/${id}`)
}

export function getWorkOrders() {
  return api.get('/api/v1/eam/maintenance/workorders')
}

export function createWorkOrder(data: any) {
  return api.post('/api/v1/eam/maintenance/workorders', data)
}

export function updateWorkOrder(id: number, data: any) {
  return api.put(`/api/v1/eam/maintenance/workorders/${id}`, data)
}

export function deleteWorkOrder(id: number) {
  /**
   * 删除维护工单
   * @param id 工单ID
   */
  return api.delete(`/api/v1/eam/maintenance/workorders/${id}`)
}

// 备件管理 API
export function getSpareParts() {
  return api.get('/api/v1/eam/spares')
}

export function createSparePart(data: any) {
  return api.post('/api/v1/eam/spares', data)
}

export function updateSparePart(id: number, data: any) {
  return api.put(`/api/v1/eam/spares/${id}`, data)
}

export function deleteSparePart(id: number) {
  return api.delete(`/api/v1/eam/spares/${id}`)
}

export function getInventory() {
  return api.get('/api/v1/eam/spares/inventory')
}

export function updateInventory(data: any) {
  return api.post('/api/v1/eam/spares/inventory', data)
}

export function getDemandPlans() {
  return api.get('/api/v1/eam/spares/demand-plans')
}

export function getSpareIssues() {
  return api.get('/api/v1/eam/spares/issues')
}

export function createSpareIssue(data: any) {
  return api.post('/api/v1/eam/spares/issues', data)
}

export function approveSpareIssue(id: number) {
  return api.put(`/api/v1/eam/spares/issues/${id}/approve`)
}

export function rejectSpareIssue(id: number) {
  /**
   * 拒绝备件领用申请
   * @param id 领用申请ID
   */
  return api.put(`/api/v1/eam/spares/issues/${id}/reject`)
}

export function returnSpareIssue(id: number) {
  /**
   * 归还备件（回补库存）
   * @param id 领用申请ID
   */
  return api.put(`/api/v1/eam/spares/issues/${id}/return`)
}

export function executeMaintenancePlan(id: number) {
  return api.post(`/api/v1/eam/maintenance/plans/${id}/execute`)
}

export function getFaults() {
  return api.get('/api/v1/eam/maintenance/faults')
}

export function createFault(data: any) {
  return api.post('/api/v1/eam/maintenance/faults', data)
}

export function updateFault(id: number, data: any) {
  return api.put(`/api/v1/eam/maintenance/faults/${id}`, data)
}

export function getMaintenanceRecords() {
  return api.get('/api/v1/eam/maintenance/records')
}

export function createMaintenanceRecord(data: any) {
  return api.post('/api/v1/eam/maintenance/records', data)
}

// 绩效分析 API
export function getAnalysisDashboard() {
  return api.get('/api/v1/eam/analysis/dashboard')
}

export function getOeeTrend(params?: { months?: number }) {
  /**
   * 获取OEE趋势分析数据
   * @param params 查询参数（months：最近N个月）
   */
  return api.get('/api/v1/eam/analysis/oee-trend', { params })
}

export function getFaultSummary(params?: { start?: string; end?: string }) {
  /**
   * 获取故障分析汇总数据
   * @param params 查询参数（start/end：日期范围，YYYY-MM-DD）
   */
  return api.get('/api/v1/eam/analysis/fault-summary', { params })
}

export function getCostSummary(params?: { start?: string; end?: string }) {
  /**
   * 获取维护成本分析汇总数据
   * @param params 查询参数（start/end：日期范围，YYYY-MM-DD）
   */
  return api.get('/api/v1/eam/analysis/cost-summary', { params })
}

// 维修 Copilot API
export function getFaultAdvice(params?: { equipmentId?: number | string; symptom?: string; limit?: number }) {
  /**
   * 获取维修 Copilot 故障维修建议（规则版）
   * @param params 查询参数（equipmentId：设备ID；symptom：故障现象关键词；limit：返回条数，默认5）
   */
  return api.get('/api/v1/eam/copilot/fault-advice', { params })
}
