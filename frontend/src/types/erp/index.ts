/**
 * ERP系统通用类型定义
 */

// 分页参数类型
export interface PageParams {
  page: number;
  size: number;
  [key: string]: any;
}

// 分页结果类型
export interface PageResult<T> {
  list: T[];
  total: number;
  page: number;
  size: number;
}

// 响应结果类型
export interface ResponseResult<T = any> {
  success: boolean;
  message: string;
  data: T;
  code: number;
}

// 基础数据类型
export interface BaseEntity {
  id: number;
  created_by: string;
  created_time: string;
  updated_by?: string;
  updated_time?: string;
  is_deleted: boolean;
}

// 用户信息类型
export interface UserInfo {
  id: number;
  username: string;
  real_name: string;
  role: string;
  department: string;
}

// 部门类型
export interface Department {
  id: number;
  department_name: string;
  parent_id?: number;
  level: number;
  description?: string;
}

// 岗位类型
export interface Position {
  id: number;
  position_name: string;
  department_id: number;
  description?: string;
}

// 状态枚举
export const StatusEnum = {
  ACTIVE: 'active',
  INACTIVE: 'inactive'
} as const

// 状态类型
export type Status = typeof StatusEnum[keyof typeof StatusEnum]

// 审核状态枚举
export const ApprovalStatusEnum = {
  PENDING: 'pending',
  APPROVED: 'approved',
  REJECTED: 'rejected'
} as const

// 审核状态类型
export type ApprovalStatus = typeof ApprovalStatusEnum[keyof typeof ApprovalStatusEnum]

// 物料类型枚举
export const MaterialTypeEnum = {
  RAW_MATERIAL: 'raw_material',
  SEMI_FINISHED: 'semi_finished',
  FINISHED: 'finished',
  AUXILIARY: 'auxiliary'
} as const

// 物料类型
export type MaterialType = typeof MaterialTypeEnum[keyof typeof MaterialTypeEnum]

// 计量单位枚举
export const UnitEnum = {
  PIECE: 'piece',
  KG: 'kg',
  M: 'm',
  M2: 'm2',
  M3: 'm3'
} as const

// 计量单位类型
export type Unit = typeof UnitEnum[keyof typeof UnitEnum]

// 操作类型枚举
export const OperationTypeEnum = {
  CREATE: 'create',
  UPDATE: 'update',
  DELETE: 'delete',
  APPROVE: 'approve',
  REJECT: 'reject',
  POST: 'post'
} as const

// 操作类型
export type OperationType = typeof OperationTypeEnum[keyof typeof OperationTypeEnum]

// 日志级别枚举
export const LogLevelEnum = {
  DEBUG: 'debug',
  INFO: 'info',
  WARN: 'warn',
  ERROR: 'error',
  FATAL: 'fatal'
} as const

// 日志级别类型
export type LogLevel = typeof LogLevelEnum[keyof typeof LogLevelEnum]