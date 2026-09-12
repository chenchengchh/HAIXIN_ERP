import api from '../index'
import type {
  PurchaseOrder,
  SalesOrder,
  Inventory,
  MRPParams,
  MRPResult,
} from '../../types/erp/supply-chain'
import type { PaginationResponse } from '../../types/erp/basic-data'

/**
 * 供应链模块API
 */
export const supplyChainApi = {
  // 采购管理
  /**
   * 获取采购订单列表
   */
  getPurchaseOrders: (params: any) => 
    api.get<PaginationResponse<PurchaseOrder>>('/api/v1/erp/supply-chain/purchase/orders', { params }),
  
  /**
   * 创建采购订单
   */
  createPurchaseOrder: (data: Omit<PurchaseOrder, 'id' | 'create_time'>) => 
    api.post<PurchaseOrder>('/api/v1/erp/supply-chain/purchase/orders', data),
  
  /**
   * 更新采购订单
   */
  updatePurchaseOrder: (id: string, data: Partial<PurchaseOrder>) => 
    api.put<PurchaseOrder>(`/api/v1/erp/supply-chain/purchase/orders/${id}`, data),

  getPurchaseOrderById: (id: string) =>
    api.get<PurchaseOrder>(`/api/v1/erp/supply-chain/purchase/orders/${id}`),
  
  /**
   * 审批采购订单
   */
  approvePurchaseOrder: (id: string | number) => 
    api.post<void>(`/api/v1/erp/supply-chain/purchase/orders/${id}/approve`),
  
  /**
   * 创建采购收货单
   */
  createPurchaseReceipt: (data: any) => 
    api.post('/api/v1/erp/supply-chain/purchase/receipt', data),

  /**
   * 创建采购退货单
   */
  createPurchaseReturn: (data: any) =>
    api.post('/api/v1/erp/supply-chain/purchase/return', data),
  
  // 销售管理
  /**
   * 获取销售订单列表
   */
  getSalesOrders: (params: any) => 
    api.get<PaginationResponse<SalesOrder>>('/api/v1/erp/supply-chain/sales/orders', { params }),

  getSalesOrderById: (id: string) =>
    api.get<SalesOrder>(`/api/v1/erp/supply-chain/sales/orders/${id}`),

  createSalesOrder: (data: any) =>
    api.post<SalesOrder>('/api/v1/erp/supply-chain/sales/orders', data),
  
  /**
   * 更新销售订单
   */
  updateSalesOrder: (id: string, data: Partial<SalesOrder>) => 
    api.put<SalesOrder>(`/api/v1/erp/supply-chain/sales/orders/${id}`, data),

  deleteSalesOrder: (id: string | number) =>
    api.delete(`/api/v1/erp/supply-chain/sales/orders/${id}`),

  submitSalesOrder: (id: string | number) =>
    api.post(`/api/v1/erp/supply-chain/sales/orders/${id}/submit`),

  approveSalesOrder: (id: string | number) =>
    api.post(`/api/v1/erp/supply-chain/sales/orders/${id}/approve`),
  
  /**
   * 创建销售发货单
   */
  createSalesDelivery: (data: any) => 
    api.post('/api/v1/erp/supply-chain/sales/delivery', data),
  
  // 库存管理
  /**
   * 获取库存列表
   */
  getInventory: (params: any) => 
    api.get<PaginationResponse<Inventory>>('/api/v1/erp/supply-chain/inventory', { params }),
  
  /**
   * 创建库存调拨单
   */
  createInventoryTransfer: (data: any) => 
    api.post('/api/v1/erp/supply-chain/inventory/transfer', data),
  
  /**
   * 创建库存盘点单
   */
  createInventoryCount: (data: any) => 
    api.post('/api/v1/erp/supply-chain/inventory/count', data),
  
  // MRP物料需求计划
  /**
   * 运行MRP计算
   */
  runMRP: (params: MRPParams) => 
    api.post<void>('/api/v1/erp/supply-chain/mrp/run', params),
  
  /**
   * 获取MRP运算结果
   */
  getMRPResults: (params: any) => 
    api.get<PaginationResponse<MRPResult>>('/api/v1/erp/supply-chain/mrp/results', { params })
}
