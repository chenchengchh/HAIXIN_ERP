import api from '../index'

/**
 * 销售管理模块API
 */
export const salesApi = {
  /**
   * 获取线索列表
   * @param params 查询参数
   * @returns 线索列表
   */
  getLeadsList: (params: any) => {
    return api.get('/api/v1/crm/leads', { params })
  },

  /**
   * 创建销售线索
   * @param data 线索数据
   * @returns 线索创建结果
   */
  createLead: (data: any) => {
    return api.post('/api/v1/crm/leads', data)
  },

  /**
   * 查询线索详情
   * @param id 线索ID
   * @returns 线索详情
   */
  getLeadDetail: (id: string | number) => {
    return api.get(`/api/v1/crm/leads/${id}`)
  },

  /**
   * 更新线索
   * @param id 线索ID
   * @param data 线索数据
   * @returns 更新结果
   */
  updateLead: (id: string | number, data: any) => {
    return api.put(`/api/v1/crm/leads/${id}`, data)
  },

  /**
   * 将线索转化为客户
   * @param id 线索ID
   * @param data 转化数据
   * @returns 转化结果
   */
  convertLead: (id: string | number, data: any) => {
    return api.post(`/api/v1/crm/leads/${id}/convert`, data)
  },

  /**
   * 获取商机列表
   * @param params 查询参数
   * @returns 商机列表
   */
  getOpportunitiesList: (params: any) => {
    return api.get('/api/v1/crm/opportunities', { params })
  },

  /**
   * 创建商机
   * @param data 商机数据
   * @returns 商机创建结果
   */
  createOpportunity: (data: any) => {
    return api.post('/api/v1/crm/opportunities', data)
  },

  /**
   * 查询商机详情
   * @param id 商机ID
   * @returns 商机详情
   */
  getOpportunityDetail: (id: string | number) => {
    return api.get(`/api/v1/crm/opportunities/${id}`)
  },

  /**
   * 更新商机
   * @param id 商机ID
   * @param data 商机数据
   * @returns 更新结果
   */
  updateOpportunity: (id: string | number, data: any) => {
    return api.put(`/api/v1/crm/opportunities/${id}`, data)
  },

  /**
   * 推进商机阶段
   * @param id 商机ID
   * @param data 阶段数据
   * @returns 推进结果
   */
  updateOpportunityStage: (id: string | number, data: any) => {
    return api.post(`/api/v1/crm/opportunities/${id}/stage`, data)
  },

  /**
   * 获取我的商机列表
   * @param params 查询参数
   * @returns 商机列表
   */
  getMyOpportunities: (params: any) => {
    return api.get('/api/v1/crm/opportunities/my', { params })
  },

  /**
   * 获取销售漏斗分析
   * @returns 销售漏斗数据
   */
  getSalesFunnel: () => {
    return api.get('/api/v1/crm/opportunities/funnel')
  },

  /**
   * 获取销售预测
   * @param period 预测周期
   * @returns 销售预测数据
   */
  getSalesForecast: (period: string) => {
    return api.get(`/api/v1/crm/forecast/period/${period}`)
  },

  /**
   * 创建销售预测
   * @param data 预测数据
   * @returns 预测创建结果
   */
  createSalesForecast: (data: any) => {
    return api.post('/api/v1/crm/forecast', data)
  },

  /**
   * 获取销售目标
   * @param params 查询参数
   * @returns 销售目标数据
   */
  getSalesTargets: (params: any) => {
    return api.get('/api/v1/crm/targets/my', { params })
  }
}