// 供应商状态枚举
export enum SupplierStatus {
  PENDING = 'PENDING', // 待审核
  QUALIFIED = 'QUALIFIED', // 合格
  UNQUALIFIED = 'UNQUALIFIED', // 不合格
  BLACKLIST = 'BLACKLIST' // 黑名单
}

// 供应商等级枚举
export enum SupplierLevel {
  A = 'A',
  B = 'B',
  C = 'C'
}

// 供应商类型定义
export interface Supplier {
  id: number
  name: string
  contactPerson: string
  contactPhone: string
  email: string
  address: string
  status: SupplierStatus
  level: SupplierLevel
  createTime: string
  updateTime?: string
}

// 供应商资质文件类型定义
export interface SupplierQualification {
  id: number
  supplierId: number
  name: string
  type: string
  fileUrl: string
  expiryDate: string
  status: 'VALID' | 'INVALID'
  createTime: string
}

// 供应商审核记录类型定义
export interface SupplierAuditLog {
  id: number
  supplierId: number
  auditType: string
  content: string
  auditBy: string
  auditTime: string
}

// 供应商绩效评分类型定义
export interface SupplierPerformance {
  id: number
  supplierId: number
  period: string
  scoreQuality: number
  scoreDelivery: number
  scorePrice: number
  scoreService: number
  totalScore: number
  levelResult: SupplierLevel
  createTime: string
}

// 询价单类型枚举
export enum InquiryType {
  PUBLIC = 'PUBLIC', // 公开
  INVITE = 'INVITE' // 邀请
}

// 询价单状态枚举
export enum InquiryStatus {
  DRAFT = 'DRAFT', // 草稿
  PUBLISHED = 'PUBLISHED', // 已发布
  CLOSED = 'CLOSED', // 已关闭
  AWARDED = 'AWARDED' // 已定标
}

// 询价单商品类型定义
export interface InquiryItem {
  materialId: string
  materialName: string
  specification: string
  quantity: number
  unit: string
  requiredDate: string
}

// 询价单类型定义
export interface Inquiry {
  id: number
  inquiryNo: string
  title: string
  type: InquiryType
  status: InquiryStatus
  startTime: string
  endTime: string
  description: string
  items: InquiryItem[]
  invitedSuppliers: number[]
  quoteCount: number
  createBy: string
  createTime: string
  updateTime?: string
}

// 供应商报价单类型定义
export interface Quotation {
  id: number
  inquiryId: number
  supplierId: number
  supplierName: string
  quoteTotalAmount: number
  quoteTime: string
  status: number
  isRecommend: boolean
}

// 采购订单状态枚举
export enum PurchaseOrderStatus {
  DRAFT = 'DRAFT', // 草稿
  OPEN = 'OPEN', // 待确认
  CONFIRMED = 'CONFIRMED', // 已确认
  PARTIAL_DELIVERED = 'PARTIAL_DELIVERED', // 部分发货
  DELIVERED = 'DELIVERED', // 已发货
  RECEIVED = 'RECEIVED', // 已入库
  CLOSED = 'CLOSED' // 已关闭
}

// 采购订单商品类型定义
export interface PurchaseOrderItem {
  id?: number
  materialId: string
  materialName: string
  specification: string
  quantity: number
  unit: string
  price: number
  total: number
  requiredDate: string
}

// 采购订单类型定义
export interface PurchaseOrder {
  id?: number
  orderNo: string
  supplierId: number
  supplierName: string
  status: PurchaseOrderStatus
  purchaseDate: string
  expectedDeliveryDate: string
  totalAmount: number
  items: PurchaseOrderItem[]
  createTime?: string
  updateTime?: string
}

// 发货单(ASN)类型定义
export interface DeliveryNote {
  id?: number
  deliveryNo: string
  poId: number
  poNo: string
  supplierId: number
  supplierName: string
  deliveryDate: string
  logisticsInfo: string
  items: Array<{ lineId: number; qty: number }>
  status: string
  createTime?: string
}
