import api from '../index'

/**
 * 订单管理模块API
 */
export const orderApi = {
  /**
   * 创建销售订单
   * @param data 订单数据
   * @returns 订单创建结果
   */
  createOrder: (data: any) => {
    return api.post('/api/v1/crm/orders', data)
  },

  /**
   * 查询订单详情
   * @param id 订单ID
   * @returns 订单详情
   */
  getOrderDetail: (id: string | number) => {
    return api.get(`/api/v1/crm/orders/${id}`)
  },

  /**
   * 更新订单
   * @param id 订单ID
   * @param data 订单数据
   * @returns 更新结果
   */
  updateOrder: (id: string | number, data: any) => {
    return api.put(`/api/v1/crm/orders/${id}`, data)
  },

  /**
   * 提交订单审批
   * @param id 订单ID
   * @returns 提交结果
   */
  submitOrderApproval: (id: string | number) => {
    return api.post(`/api/v1/crm/orders/${id}/submit`)
  },

  /**
   * 获取我的订单列表
   * @param params 查询参数
   * @returns 订单列表
   */
  getMyOrders: (params: any) => {
    return api.get('/api/v1/crm/orders/my', { params })
  },

  /**
   * 删除销售订单
   * @param id 订单ID
   * @returns 删除结果
   */
  deleteOrder: (id: string | number) => {
    return api.delete(`/api/v1/crm/orders/${id}`)
  },

  /**
   * 获取待审批订单列表
   * @param params 查询参数
   * @returns 待审批订单列表
   */
  getPendingApprovalOrders: (params: any) => {
    return api.get('/api/v1/crm/orders/pending-approval', { params })
  },

  /**
   * 分页查询合同列表
   * @param params 查询参数（page 页码、size 每页大小、status 状态筛选、customerId 客户ID筛选）
   * @returns 合同分页数据
   */
  getContractsList: (params: any) => {
    return api.get('/api/v1/crm/contracts/list', { params })
  },

  /**
   * 创建合同
   * @param data 合同数据
   * @returns 合同创建结果
   */
  createContract: (data: any) => {
    return api.post('/api/v1/crm/contracts', data)
  },

  /**
   * 查询合同详情
   * @param id 合同ID
   * @returns 合同详情
   */
  getContractDetail: (id: string | number) => {
    return api.get(`/api/v1/crm/contracts/${id}`)
  },

  /**
   * 更新合同
   * @param id 合同ID
   * @param data 合同数据
   * @returns 更新结果
   */
  updateContract: (id: string | number, data: any) => {
    return api.put(`/api/v1/crm/contracts/${id}`, data)
  },

  /**
   * 签署合同
   * @param id 合同ID
   * @returns 签署结果
   */
  signContract: (id: string | number) => {
    return api.post(`/api/v1/crm/contracts/${id}/sign`)
  },

  /**
   * 获取即将到期合同
   * @param params 查询参数
   * @returns 即将到期合同列表
   */
  getExpiringContracts: (params: any) => {
    return api.get('/api/v1/crm/contracts/expiring', { params })
  },

  /**
   * 分页查询订单明细列表
   * @param params 查询参数（page 页码、size 每页大小、keyword 产品名称/编码关键词、orderId 订单ID过滤）
   * @returns 明细分页数据
   */
  getOrderItems: (params: any) => {
    return api.get('/api/v1/crm/orders/items', { params })
  },

  /**
   * 分页查询合同附件列表
   * @param params 查询参数（page 页码、size 每页大小、keyword 文件名关键词、contractId 合同ID过滤）
   * @returns 附件分页数据
   */
  getContractAttachmentsList: (params: any) => {
    return api.get('/api/v1/crm/contracts/attachments/list', { params })
  },

  /**
   * 登记合同附件元数据
   * @param data 附件数据（contractId/fileName/fileUrl 必填）
   * @returns 创建结果
   */
  createContractAttachment: (data: any) => {
    return api.post('/api/v1/crm/contracts/attachments', data)
  },

  /**
   * 删除合同附件
   * @param id 附件ID
   * @returns 删除结果
   */
  deleteContractAttachment: (id: string | number) => {
    return api.delete(`/api/v1/crm/contracts/attachments/${id}`)
  }
}