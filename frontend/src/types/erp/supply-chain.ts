/**
 * ERP供应链模块类型定义
 */

import type { PaginationParams, CommonStatus } from './basic-data'

// ==================== 采购管理 ====================

/**
 * 采购订单状态
 */
export type PurchaseOrderStatus = 'draft' | 'submitted' | 'approved' | 'processed' | 'closed' | 'rejected'

/**
 * 采购订单明细
 */
export interface PurchaseOrderItem {
  id?: string
  material_id: string
  material_code: string
  material_name: string
  quantity: number
  unit_price: number
  amount?: number
  unit: string
  remark?: string
}

/**
 * 采购订单
 */
export interface PurchaseOrder {
  id?: number
  order_no: string
  supplier_id: string
  supplier_name: string
  purchase_date: string | Date
  expected_delivery_date: string | Date
  actual_delivery_date?: string | Date
  buyer?: string
  total_amount: number
  payment_status: string
  delivery_status: string
  status: PurchaseOrderStatus
  creator?: string
  create_time?: string
  approval_time?: string
  remark?: string
  items?: PurchaseOrderItem[]
}

// ==================== 销售管理 ====================

/**
 * 销售订单状态
 */
export type SalesOrderStatus = 'draft' | 'confirmed' | 'processing' | 'shipped' | 'completed' | 'cancelled'

/**
 * 销售订单
 */
export interface SalesOrder {
  id?: number
  order_no: string
  customer_name: string
  total_amount: number
  order_date: string | Date
  delivery_date: string | Date
  status: SalesOrderStatus
  salesperson?: string
  create_time?: string
}

// ==================== 库存管理 ====================

/**
 * 库存状态
 */
export type InventoryStatus = 'normal' | 'low' | 'out' | 'high'

/**
 * 库存记录
 */
export interface Inventory {
  id?: number
  material_id: string
  material_code: string
  material_name: string
  specification?: string
  unit: string
  warehouse_id: string
  warehouse_name: string
  current_qty: number
  safety_stock: number
  max_stock?: number
  inventory_status: InventoryStatus
  update_time?: string
  remark?: string
}

// ==================== MRP ====================

/**
 * MRP运算参数
 */
export interface MRPParams {
  run_date: Date
  plan_period: number
  consider_safety_stock: boolean
  lot_rule: 'LOT_FOR_LOT' | 'FIXED_LOT' | 'ECONOMIC_LOT'
  fixed_lot_size?: number
  run_mode: 'FULL_REGENERATION' | 'NET_CHANGE'
}

/**
 * MRP建议类型
 */
export type MRPSuggestionType = 'PURCHASE' | 'PRODUCTION' | 'INVENTORY' | 'DELAY'

/**
 * MRP运算结果
 */
export interface MRPResult {
  material_code: string
  material_name: string
  specification?: string
  material_type: string
  current_qty: number
  safety_stock: number
  demand_qty: number
  supply_qty: number
  suggestion_type: MRPSuggestionType
  suggestion_qty: number
  suggestion_date: string
  remark?: string
}
