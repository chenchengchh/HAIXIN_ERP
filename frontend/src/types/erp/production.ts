/**
 * ERP生产模块类型定义
 */

import type { PaginationParams, CommonStatus } from './basic-data'

// ==================== 生产订单 ====================

/**
 * 生产订单状态
 */
export type ProductionOrderStatus = 'draft' | 'approved' | 'in_production' | 'completed' | 'cancelled'

/**
 * 生产订单
 */
export interface ProductionOrder {
  id?: number
  source?: 'local' | 'aps' | string
  order_no: string
  product_code?: string
  product_name: string
  planned_qty: number
  actual_qty?: number
  start_date: string | Date
  end_date: string | Date
  status: ProductionOrderStatus | string
  workshop?: string
  production_line?: string
  remark?: string
  creator?: string
  create_time?: string
}

// ==================== 车间管理 ====================

/**
 * 工序工单状态
 */
export type WorkshopOrderStatus = 'pending' | 'in_progress' | 'completed' | 'cancelled'

/**
 * 工序工单
 */
export interface WorkshopOrder {
  id?: number
  order_no: string
  production_order_no: string
  product_name: string
  process_name: string
  planned_qty: number
  completed_qty: number
  status: WorkshopOrderStatus
  start_date: string | Date
  end_date: string | Date
  remark?: string
}

/**
 * 生产报工状态
 */
export type ProductionReportStatus = 'draft' | 'submitted' | 'approved' | 'rejected'

/**
 * 生产报工
 */
export interface ProductionReport {
  id?: number
  report_no: string
  workshop_order_id: string
  workshop_order_no: string
  production_order_no: string
  product_name: string
  process_name: string
  report_date: string | Date
  planned_qty: number
  actual_qty: number
  qualified_qty: number
  unqualified_qty: number
  reporter?: string
  status: ProductionReportStatus
  create_time?: string
  remark?: string
}

// ==================== 产能管理 ====================

/**
 * 产能数据
 */
export interface CapacityData {
  department_name: string
  work_center_name: string
  resource_name: string
  available_capacity: number
  used_capacity: number
  utilization_rate: number
  period: 'day' | 'week' | 'month'
  period_date: string
  remark?: string
}

/**
 * 生产负荷
 */
export interface ProductionLoad {
  department_name: string
  work_center_name: string
  product_name: string
  planned_load: number
  available_capacity: number
  load_rate: number
  load_status: 'low' | 'normal' | 'high'
  period: 'day' | 'week' | 'month'
  period_date: string
}

/**
 * 设备状态
 */
export type EquipmentStatus = 'online' | 'offline' | 'running' | 'stopped'

/**
 * 设备信息
 */
export interface Equipment {
  id?: number
  equipment_code: string
  equipment_name: string
  department_name: string
  work_center_name: string
  model: string
  capacity: number
  status: EquipmentStatus
  last_maintain_date?: string
  remark?: string
}

/**
 * 产能规划参数
 */
export interface CapacityPlanningParams {
  planning_period: 'week' | 'month' | 'quarter' | 'year'
  start_date: Date
  end_date: Date
  consider_overtime: boolean
  consider_outsource: boolean
  capacity_buffer_rate: number
}

/**
 * 产能规划建议类型
 */
export type CapacitySuggestionType = 'overtime' | 'outsource' | 'hire' | 'normal'

/**
 * 产能规划结果
 */
export interface CapacityPlanningResult {
  department_name: string
  work_center_name: string
  resource_name: string
  planned_load: number
  available_capacity: number
  capacity_gap: number
  suggestion_type: CapacitySuggestionType
  suggestion_content: string
  remark?: string
}
