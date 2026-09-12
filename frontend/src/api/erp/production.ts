import api from '../index'
import type {
  ProductionOrder,
  WorkshopOrder,
  ProductionReport,
  CapacityData,
  ProductionLoad,
  Equipment,
  CapacityPlanningParams,
  CapacityPlanningResult,
} from '../../types/erp/production'
import type { PaginationResponse } from '../../types/erp/basic-data'

/**
 * 生产模块API
 */
export const productionApi = {
  // 生产订单管理
  /**
   * 获取生产订单列表
   */
  getProductionOrders: (params: any) => 
    api.get<PaginationResponse<ProductionOrder>>('/api/v1/erp/production/orders', { params }),
  
  /**
   * 创建生产订单
   */
  createProductionOrder: (data: Omit<ProductionOrder, 'id' | 'create_time'>) => 
    api.post<ProductionOrder>('/api/v1/erp/production/orders', data),
  
  /**
   * 更新生产订单
   */
  updateProductionOrder: (id: number, data: Partial<ProductionOrder>) => 
    api.put<ProductionOrder>(`/api/v1/erp/production/orders/${id}`, data),
  
  /**
   * 删除生产订单
   */
  deleteProductionOrder: (id: number) => 
    api.delete<void>(`/api/v1/erp/production/orders/${id}`),

  releaseProductionOrderToMes: (id: number) =>
    api.post(`/api/v1/erp/production/orders/${id}/release`),

  startProductionOrder: (id: number, source?: string) =>
    api.post(`/api/v1/erp/production/orders/${id}/start`, null, { params: source ? { source } : {} }),

  pauseProductionOrder: (id: number, source?: string) =>
    api.post(`/api/v1/erp/production/orders/${id}/pause`, null, { params: source ? { source } : {} }),

  resumeProductionOrder: (id: number, source?: string) =>
    api.post(`/api/v1/erp/production/orders/${id}/resume`, null, { params: source ? { source } : {} }),

  completeProductionOrder: (id: number, source?: string) =>
    api.post(`/api/v1/erp/production/orders/${id}/complete`, null, { params: source ? { source } : {} }),

  cancelProductionOrder: (id: number, source?: string) =>
    api.post(`/api/v1/erp/production/orders/${id}/cancel`, null, { params: source ? { source } : {} }),

  /**
   * 提交生产订单审批（走OA统一审批流）。
   * <p>仅草稿(draft/待生产)状态允许提交，审批完成后OA回调更新订单状态：
   * 通过→已审核(approved)，拒绝→回退草稿(draft)。</p>
   * @param id            生产订单ID
   * @param initiatorId   发起人ID（当前登录用户）
   * @param initiatorName 发起人名称
   */
  submitOrderApproval: (id: number, initiatorId?: number, initiatorName?: string) =>
    api.post(`/api/v1/erp/production/orders/${id}/submit-approval`, null, {
      params: {
        ...(initiatorId != null ? { initiatorId } : {}),
        ...(initiatorName ? { initiatorName } : {}),
      },
    }),
  
  // 工序工单管理
  /**
   * 获取工序工单列表
   */
  getWorkshopOrders: (params: any) => 
    api.get<PaginationResponse<WorkshopOrder>>('/api/v1/erp/production/workshop-orders', { params }),
  
  /**
   * 创建工序工单
   */
  createWorkshopOrder: (data: Omit<WorkshopOrder, 'id'>) => 
    api.post<WorkshopOrder>('/api/v1/erp/production/workshop-orders', data),
  
  // 生产报工管理
  /**
   * 获取生产报工列表
   */
  getProductionReports: (params: any) => 
    api.get<PaginationResponse<ProductionReport>>('/api/v1/erp/production/reports', { params }),
  
  /**
   * 创建生产报工
   */
  createProductionReport: (data: Omit<ProductionReport, 'id' | 'create_time'>) => 
    api.post<ProductionReport>('/api/v1/erp/production/reports', data),
  
  /**
   * 更新生产报工
   */
  updateProductionReport: (id: number, data: Partial<ProductionReport>) => 
    api.put<ProductionReport>(`/api/v1/erp/production/reports/${id}`, data),
  
  // 产能管理
  /**
   * 获取产能数据
   */
  getCapacity: (params: any) => 
    api.get<PaginationResponse<CapacityData>>('/api/v1/erp/production/capacity', { params }),
  
  /**
   * 获取生产负荷
   */
  getProductionLoad: (params: any) => 
    api.get<PaginationResponse<ProductionLoad>>('/api/v1/erp/production/load', { params }),
  
  /**
   * 获取设备列表
   */
  getEquipmentList: (params: any) => 
    api.get<PaginationResponse<Equipment>>('/api/v1/erp/production/equipment', { params }),
  
  /**
   * 获取设备状态
   */
  getEquipmentStatus: (params: any) => 
    api.get('/api/v1/erp/production/equipment/status', { params }),
  
  /**
   * 运行产能规划
   */
  runCapacityPlanning: (params: CapacityPlanningParams) => 
    api.post<PaginationResponse<CapacityPlanningResult>>('/api/v1/erp/production/capacity-planning/run', params),
  
  /**
   * 获取产能规划结果
   */
  getCapacityPlanningResults: (params: any) => 
    api.get<PaginationResponse<CapacityPlanningResult>>('/api/v1/erp/production/capacity-planning/results', { params })
}
