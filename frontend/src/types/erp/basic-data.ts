/**
 * ERP基础数据模块类型定义
 */

// ==================== 通用类型 ====================

/**
 * 通用状态
 */
export type CommonStatus = 'active' | 'inactive'

/**
 * 分页参数
 */
export interface PaginationParams {
  page?: number
  size?: number
}

/**
 * 分页响应
 */
export interface PaginationResponse<T> {
  list: T[]
  total: number
  page: number
  size: number
}

// ==================== 客户管理 ====================

/**
 * 客户等级
 */
export type CustomerLevel = 'vip' | 'normal' | 'potential'

/**
 * 客户接口
 */
export interface Customer {
  id?: number
  customer_code: string
  customer_name: string
  customer_level: CustomerLevel
  contact_person?: string
  contact_phone?: string
  email?: string
  fax?: string
  address?: string
  credit_limit?: number
  payment_terms?: string
  industry?: string
  company_size?: string
  status: CommonStatus
  remark?: string
  create_time?: string
  update_time?: string
}

/**
 * 客户查询参数
 */
export interface CustomerQueryParams extends PaginationParams {
  customer_code?: string
  customer_name?: string
  customer_level?: CustomerLevel
  status?: CommonStatus
  create_time?: [string, string]
}

// ==================== 供应商管理 ====================

/**
 * 供应商类型
 */
export type SupplierType = 'raw_material' | 'semi_finished' | 'finished' | 'service'

/**
 * 供应商评级
 */
export type SupplierRating = 'A' | 'B' | 'C' | 'D'

/**
 * 供应商接口
 */
export interface Supplier {
  id?: number
  supplier_code: string
  supplier_name: string
  supplier_type: SupplierType
  rating?: SupplierRating
  contact_person?: string
  contact_phone?: string
  email?: string
  fax?: string
  address?: string
  payment_method?: string
  bank_account?: string
  bank_name?: string
  status: CommonStatus
  remark?: string
  create_time?: string
  update_time?: string
}

/**
 * 供应商查询参数
 */
export interface SupplierQueryParams extends PaginationParams {
  supplier_code?: string
  supplier_name?: string
  supplier_type?: SupplierType
  rating?: SupplierRating
  status?: CommonStatus
  create_time?: [string, string]
}

// ==================== 物料管理 ====================

/**
 * 物料类型
 */
export type MaterialType = 'raw_material' | 'semi_finished' | 'finished' | 'auxiliary'

/**
 * 物料接口
 */
export interface Material {
  id?: number
  material_code: string
  material_name: string
  material_type: MaterialType
  specification?: string
  unit: string
  safety_stock?: number
  min_stock?: number
  max_stock?: number
  lead_time?: number
  default_supplier?: string
  unit_price?: number
  approval_status?: 'pending' | 'approved' | 'rejected'
  status: CommonStatus
  remark?: string
  create_time?: string
  update_time?: string
}

/**
 * 物料查询参数
 */
export interface MaterialQueryParams extends PaginationParams {
  material_code?: string
  material_name?: string
  material_type?: MaterialType
  status?: CommonStatus
  create_time?: [string, string]
}

// ==================== 会计科目 ====================

/**
 * 科目类型
 */
export type AccountType = '资产类' | '负债类' | '权益类' | '成本类' | '损益类'

/**
 * 余额方向
 */
export type BalanceDirection = '借方' | '贷方'

/**
 * 会计科目接口
 */
export interface Account {
  id?: number
  account_code: string
  account_name: string
  account_type: AccountType
  balance_direction: BalanceDirection
  parent_account?: string
  account_level?: number
  status: CommonStatus
  remark?: string
  create_time?: string
  update_time?: string
}

/**
 * 会计科目查询参数
 */
export interface AccountQueryParams extends PaginationParams {
  account_code?: string
  account_name?: string
  account_type?: AccountType
  status?: CommonStatus
  create_time?: [string, string]
}

// ==================== 组织架构 ====================

/**
 * 组织类型
 */
export type OrganizationType = 'group' | 'company' | 'department' | 'team'

/**
 * 组织架构接口
 */
export interface Organization {
  id?: number
  organizationCode: string
  organizationName: string
  organizationType: OrganizationType
  parentId?: number
  level?: number
  status: number
  remark?: string
  createdTime?: string
  updatedTime?: string
}

/**
 * 组织架构查询参数
 */
export interface OrganizationQueryParams extends PaginationParams {
  code?: string
  name?: string
}

// ==================== 仓库 ====================

export interface Warehouse {
  id?: number
  warehouseCode: string
  warehouseName: string
  manager?: string
  status: number
  remark?: string
  createdTime?: string
  updatedTime?: string
}

export interface WarehouseQueryParams extends PaginationParams {
  code?: string
  name?: string
}

// ==================== 表单数据类型 ====================

/**
 * 客户表单数据（用于新增/编辑）
 */
export type CustomerFormData = Omit<Customer, 'create_time' | 'update_time'>

/**
 * 供应商表单数据
 */
export type SupplierFormData = Omit<Supplier, 'create_time' | 'update_time'>

/**
 * 物料表单数据
 */
export type MaterialFormData = Omit<Material, 'create_time' | 'update_time'>

/**
 * 会计科目表单数据
 */
export type AccountFormData = Omit<Account, 'create_time' | 'update_time'>

/**
 * 组织架构表单数据
 */
export type OrganizationFormData = Omit<Organization, 'createdTime' | 'updatedTime'>
