/**
 * ERP财务模块类型定义
 */

import type { PaginationParams, CommonStatus } from './basic-data'

// ==================== 总账 ====================

/**
 * 凭证状态
 */
export type VoucherStatus = 'draft' | 'posted' | 'reviewed' | 'cancelled'

/**
 * 总账记录
 */
export interface GeneralLedger {
  id?: number
  voucher_no: string
  voucher_date: string | Date
  account_code: string
  account_name: string
  debit_amount: number
  credit_amount: number
  balance: number
  summary: string
  status: VoucherStatus
  create_time?: string
}

export interface Voucher {
  id?: number
  voucher_no?: string
  voucher_date: string | Date
  voucher_type: string
  summary?: string
  status: string
  debit_total?: number
  credit_total?: number
  attachment_count?: number
  create_time?: string
}

// ==================== 应收管理 ====================

/**
 * 应收账款状态
 */
export type AccountsReceivableStatus = 'unpaid' | 'partial' | 'paid' | 'overdue'

/**
 * 应收账款
 */
export interface AccountsReceivable {
  id?: number
  bill_no: string
  customer_id: string
  customer_name: string
  amount: number
  paid_amount: number
  unpaid_amount: number
  bill_date: string | Date
  due_date: string | Date
  status: AccountsReceivableStatus
  aging_days?: number
  create_time?: string
  remark?: string
}

// ==================== 应付管理 ====================

/**
 * 应付账款状态
 */
export type AccountsPayableStatus = 'unpaid' | 'partial' | 'paid' | 'overdue'

/**
 * 应付账款
 */
export interface AccountsPayable {
  id?: number
  bill_no: string
  supplier_id: string
  supplier_name: string
  amount: number
  paid_amount: number
  unpaid_amount: number
  bill_date: string | Date
  due_date: string | Date
  status: AccountsPayableStatus
  aging_days?: number
  create_time?: string
  remark?: string
}

// ==================== 成本核算 ====================

/**
 * 成本核算类型
 */
export type CostType = '直接材料' | '直接人工' | '制造费用' | '其他'

/**
 * 成本核算
 */
export interface CostAccounting {
  id?: number
  cost_center: string
  period: string
  cost_type: CostType
  amount: number
  unit_cost?: number
  quantity?: number
  create_time?: string
  remark?: string
}

// ==================== 固定资产 ====================

/**
 * 资产状态
 */
export type AssetStatus = 'in_use' | 'idle' | 'maintenance' | 'scrapped'

/**
 * 折旧方法
 */
export type DepreciationMethod = '直线法' | '双倍余额递减法' | '年数总和法'

/**
 * 固定资产
 */
export interface FixedAsset {
  id?: number
  asset_code: string
  asset_name: string
  category: string
  original_value: number
  residual_value: number
  useful_life: number
  depreciation_method: DepreciationMethod
  accumulated_depreciation: number
  net_value: number
  purchase_date: string | Date
  start_depreciation_date?: string | Date
  status: AssetStatus
  department?: string
  location?: string
  create_time?: string
  remark?: string
}
