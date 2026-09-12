import api from '../index'

/**
 * 客户管理模块API
 */

// 客户数据类型定义
export interface Customer {
  id?: number
  customerNo: string
  customerName: string
  customerType: string // enterprise/individual
  industry?: string
  scale?: string // large/medium/small
  level?: string // A/B/C
  status: string // potential/active/inactive/lost
  source?: string
  tags?: string[]
  region?: string
  address?: string
  website?: string
  ownerId: number
  ownerName: string
  createTime?: string
  updateTime?: string
}

/**
 * 客户管理模块API
 */
export const customerApi = {
  /**
   * 创建客户
   * @param data 客户数据
   * @returns 客户创建结果
   */
  createCustomer: (data: Customer) => {
    // 创建客户时删除id字段，避免后端验证错误
    const { id, ...createData } = data
    return api.post('/api/v1/crm/customer', createData)
  },

  /**
   * 查询客户详情
   * @param id 客户ID
   * @returns 客户详情
   */
  getCustomerDetail: (id: string | number) => {
    return api.get(`/api/v1/crm/customer/${id}`)
  },

  /**
   * 更新客户
   * @param data 客户数据
   * @returns 更新结果
   */
  updateCustomer: (data: Customer) => {
    return api.put('/api/v1/crm/customer', data)
  },

  /**
   * 删除客户
   * @param id 客户ID
   * @returns 删除结果
   */
  deleteCustomer: (id: string | number) => {
    return api.delete(`/api/v1/crm/customer/${id}`)
  },

  /**
   * 分页查询客户列表
   * @param params 查询参数
   * @returns 客户列表
   */
  getCustomerList: (params: any) => {
    console.log('请求参数:', params)
    console.log('请求URL:', '/api/v1/crm/customer/list')
    return api.get('/api/v1/crm/customer/list', { params })
  },

  /**
   * 获取客户360°视图
   * @param id 客户ID
   * @returns 客户360°视图数据
   */
  getCustomer360View: (id: string | number) => {
    return api.get(`/api/v1/crm/customer/${id}/360-view`)
  },

  /**
   * 获取客户联系人列表
   * @param customerId 客户ID
   * @returns 联系人列表
   */
  getCustomerContacts: (customerId: string | number) => {
    return api.get(`/api/v1/crm/contacts/customer/${customerId}`)
  },

  /**
   * 获取客户跟进记录
   * @param customerId 客户ID
   * @returns 跟进记录列表
   */
  getCustomerFollowUps: (customerId: string | number) => {
    return api.get(`/api/v1/crm/follow-ups/customer/${customerId}`)
  },

  /**
   * 获取客户标签列表
   * @returns 标签列表
   */
  getCustomerTags: () => {
    return api.get('/api/v1/crm/tags')
  },

  /**
   * 新增标签
   * @param data 标签数据（tagName、tagType、tagCategory、color、sortOrder）
   * @returns 标签创建结果
   */
  createTag: (data: any) => {
    return api.post('/api/v1/crm/tags', data)
  },

  /**
   * 更新标签
   * @param data 标签数据（需包含 id）
   * @returns 标签更新结果
   */
  updateTag: (data: any) => {
    return api.put('/api/v1/crm/tags', data)
  },

  /**
   * 删除标签
   * @param id 标签ID
   * @returns 删除结果
   */
  deleteTag: (id: string | number) => {
    return api.delete(`/api/v1/crm/tags/${id}`)
  },

  /**
   * 查询某客户已关联的标签
   * @param customerId 客户ID
   * @returns 该客户已关联的标签列表
   */
  getCustomerTagRelations: (customerId: string | number) => {
    return api.get(`/api/v1/crm/customer/${customerId}/tags`)
  },

  /**
   * 为客户添加标签
   * @param customerId 客户ID
   * @param tagIds 标签ID列表
   * @returns 添加结果
   */
  addCustomerTags: (customerId: string | number, tagIds: number[]) => {
    return api.post(`/api/v1/crm/customer/${customerId}/tags`, tagIds)
  },

  /**
   * 移除客户标签
   * @param customerId 客户ID
   * @param tagId 标签ID
   * @returns 移除结果
   */
  removeCustomerTag: (customerId: string | number, tagId: number) => {
    return api.delete(`/api/v1/crm/customer/${customerId}/tags/${tagId}`)
  },

  /**
   * 设置客户标签
   * @param customerId 客户ID
   * @param tagIds 标签ID列表
   * @returns 设置结果
   */
  setCustomerTags: (customerId: string | number, tagIds: number[]) => {
    return api.put(`/api/v1/crm/customer/${customerId}/tags`, tagIds)
  },

  /**
   * 添加客户联系人
   * @param customerId 客户ID
   * @param data 联系人数据
   * @returns 添加结果
   */
  addCustomerContact: (customerId: string | number, data: any) => {
    return api.post(`/api/v1/crm/contacts`, { ...data, customerId })
  },

  /**
   * 导出客户交易记录
   * @param customerId 客户ID
   * @param params 导出参数
   * @returns 导出结果
   */
  exportCustomerTransactions: (customerId: string | number, params: any) => {
    return api.get(`/api/v1/crm/transactions/customer/${customerId}/export`, { params, responseType: 'blob' })
  },

  /**
   * 添加客户跟进记录
   * @param customerId 客户ID
   * @param data 跟进记录数据
   * @returns 添加结果
   */
  addCustomerFollowUp: (customerId: string | number, data: any) => {
    return api.post(`/api/v1/crm/follow-ups`, { ...data, customerId })
  },

  /**
   * 全量分页查询跟进记录列表
   * @param params 查询参数（page/size/customerName/followUpType/startTime/endTime）
   * @returns 跟进记录分页列表
   */
  getFollowUpList: (params: any) => {
    return api.get('/api/v1/crm/follow-ups', { params })
  },

  /**
   * 更新跟进记录
   * @param data 跟进记录数据（需包含 id）
   * @returns 更新结果
   */
  updateFollowUp: (data: any) => {
    return api.put('/api/v1/crm/follow-ups', data)
  },

  /**
   * 删除跟进记录
   * @param id 跟进记录ID
   * @returns 删除结果
   */
  deleteFollowUp: (id: string | number) => {
    return api.delete(`/api/v1/crm/follow-ups/${id}`)
  },

  /**
   * 全量分页查询联系人列表
   * @param params 查询参数（page/size/customerName/contactName/position）
   * @returns 联系人分页列表
   */
  getContactList: (params: any) => {
    return api.get('/api/v1/crm/contacts', { params })
  },

  /**
   * 更新联系人
   * @param data 联系人数据（需包含 id）
   * @returns 更新结果
   */
  updateContact: (data: any) => {
    return api.put('/api/v1/crm/contacts', data)
  },

  /**
   * 删除联系人
   * @param id 联系人ID
   * @returns 删除结果
   */
  deleteContact: (id: string | number) => {
    return api.delete(`/api/v1/crm/contacts/${id}`)
  },

  /**
   * 分页查询客户分类列表
   * @param params 查询参数（page 页码、size 每页大小、keyword 名称/编码关键词）
   * @returns 分类分页数据
   */
  getCategoryList: (params: any) => {
    return api.get('/api/v1/crm/categories/list', { params })
  },

  /**
   * 查询所有客户分类（用于树状结构展示）
   * @returns 分类列表
   */
  getAllCategories: () => {
    return api.get('/api/v1/crm/categories/all')
  },

  /**
   * 创建客户分类
   * @param data 分类数据（categoryName/categoryCode 必填）
   * @returns 创建结果
   */
  createCategory: (data: any) => {
    return api.post('/api/v1/crm/categories', data)
  },

  /**
   * 更新客户分类
   * @param id 分类ID
   * @param data 分类数据
   * @returns 更新结果
   */
  updateCategory: (id: string | number, data: any) => {
    return api.put(`/api/v1/crm/categories/${id}`, data)
  },

  /**
   * 删除客户分类（存在子分类时后端拒绝删除）
   * @param id 分类ID
   * @returns 删除结果
   */
  deleteCategory: (id: string | number) => {
    return api.delete(`/api/v1/crm/categories/${id}`)
  }
}