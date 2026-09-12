/**
 * 订单管理模块类型定义
 */

/**
 * 销售订单
 */
export interface SalesOrderEntity {
  id: number;
  orderNo: string;           // 订单编号
  customerId: number;        // 客户ID
  customerName: string;      // 客户名称
  opportunityId?: number;    // 关联商机ID
  totalAmount: number;       // 订单总额
  discountAmount: number;    // 折扣金额
  finalAmount: number;       // 最终金额
  currency: string;          // 币种
  orderDate: string;         // 订单日期
  deliveryDate: string;      // 交付日期
  paymentTerms: string;      // 付款条款
  deliveryAddress: string;   // 交付地址
  salesPersonId: number;     // 销售人员ID
  status: 'draft' | 'submitted' | 'approved' | 'in_progress' | 'completed' | 'cancelled';  // 状态
  approvalStatus: string;    // 审批状态
  remark: string;            // 备注
}

/**
 * 订单明细
 */
export interface SalesOrderItemEntity {
  id: number;
  orderId: number;           // 订单ID
  productCode: string;       // 产品编码
  productName: string;       // 产品名称
  specification: string;     // 规格
  quantity: number;          // 数量
  unit: string;              // 单位
  unitPrice: number;         // 单价
  amount: number;            // 金额
  discount: number;          // 折扣率
  deliveryDate: string;      // 交付日期
}

/**
 * 合同
 */
export interface ContractEntity {
  id: number;
  contractNo: string;        // 合同编号
  contractName: string;      // 合同名称
  customerId: number;        // 客户ID
  orderId?: number;          // 关联订单ID
  contractType: string;      // 合同类型
  contractAmount: number;    // 合同金额
  signDate: string;          // 签订日期
  startDate: string;         // 开始日期
  endDate: string;           // 结束日期
  paymentTerms: string;      // 付款条款
  status: 'drafting' | 'reviewing' | 'signed' | 'executing' | 'completed' | 'terminated';  // 状态
  fileUrl: string;           // 合同文件URL
  signerUserId: number;      // 签署人ID
}

/**
 * 合同附件
 */
export interface ContractAttachmentEntity {
  id: number;
  contractId: number;        // 合同ID
  fileName: string;          // 文件名
  fileUrl: string;           // 文件URL
  fileSize: number;          // 文件大小
  uploadTime: string;        // 上传时间
  uploadUserId: number;      // 上传人ID
}

/**
 * 订单查询参数
 */
export interface OrderQueryParams {
  page?: number;
  size?: number;
  keyword?: string;
  orderNo?: string;
  customerId?: number;
  status?: string;
  orderDateStart?: string;
  orderDateEnd?: string;
  salesPersonId?: number;
}

/**
 * 合同查询参数
 */
export interface ContractQueryParams {
  page?: number;
  size?: number;
  keyword?: string;
  contractNo?: string;
  customerId?: number;
  status?: string;
  signDateStart?: string;
  signDateEnd?: string;
  endDateStart?: string;
  endDateEnd?: string;
}
