import api from '../index'

/**
 * 客户服务模块API
 */
export const serviceApi = {
  /**
   * 创建服务工单
   * @param data 工单数据
   * @returns 工单创建结果
   */
  createTicket: (data: any) => {
    return api.post('/api/v1/crm/tickets', data)
  },

  /**
   * 查询工单详情
   * @param id 工单ID
   * @returns 工单详情
   */
  getTicketDetail: (id: string | number) => {
    return api.get(`/api/v1/crm/tickets/${id}`)
  },

  /**
   * 更新工单
   * @param id 工单ID
   * @param data 工单数据
   * @returns 更新结果
   */
  updateTicket: (id: string | number, data: any) => {
    return api.put(`/api/v1/crm/tickets/${id}`, data)
  },

  /**
   * 分配工单
   * @param id 工单ID
   * @param data 分配数据
   * @returns 分配结果
   */
  assignTicket: (id: string | number, data: any) => {
    return api.post(`/api/v1/crm/tickets/${id}/assign`, data)
  },

  /**
   * 回复工单
   * @param id 工单ID
   * @param data 回复数据
   * @returns 回复结果
   */
  replyTicket: (id: string | number, data: any) => {
    return api.post(`/api/v1/crm/tickets/${id}/reply`, data)
  },

  /**
   * 关闭工单
   * @param id 工单ID
   * @returns 关闭结果
   */
  closeTicket: (id: string | number) => {
    return api.post(`/api/v1/crm/tickets/${id}/close`)
  },

  /**
   * 获取我的工单列表
   * @param params 查询参数
   * @returns 工单列表
   */
  getMyTickets: (params: any) => {
    return api.get('/api/v1/crm/tickets/my', { params })
  },

  /**
   * 汇总查询所有工单的回复记录
   * @param params 查询参数（page 页码、size 每页大小、keyword 回复内容关键词、ticketId 工单ID过滤）
   * @returns 回复分页数据
   */
  getTicketReplies: (params: any) => {
    return api.get('/api/v1/crm/tickets/replies', { params })
  },

  /**
   * 创建知识库文章
   * @param data 文章数据
   * @returns 文章创建结果
   */
  createKnowledgeArticle: (data: any) => {
    return api.post('/api/v1/crm/knowledge', data)
  },

  /**
   * 查询知识库文章详情
   * @param id 文章ID
   * @returns 文章详情
   */
  getKnowledgeArticleDetail: (id: string | number) => {
    return api.get(`/api/v1/crm/knowledge/${id}`)
  },

  /**
   * 搜索知识库文章
   * @param params 搜索参数
   * @returns 搜索结果
   */
  searchKnowledgeArticles: (params: any) => {
    return api.get('/api/v1/crm/knowledge/search', { params })
  },

  /**
   * 创建满意度调查
   * @param data 调查数据
   * @returns 调查创建结果
   */
  createSatisfactionSurvey: (data: any) => {
    return api.post('/api/v1/crm/surveys', data)
  },

  /**
   * 获取客户调查记录
   * @param customerId 客户ID
   * @returns 调查记录列表
   */
  getCustomerSurveys: (customerId: string | number) => {
    return api.get(`/api/v1/crm/surveys/customer/${customerId}`)
  },

  /**
   * 获取满意度统计
   * @returns 满意度统计数据
   */
  getSatisfactionStatistics: () => {
    return api.get('/api/v1/crm/surveys/statistics')
  },

  /**
   * 提交NPS调查
   * @param data NPS数据
   * @returns 提交结果
   */
  submitNPSSurvey: (data: any) => {
    return api.post('/api/v1/crm/nps', data)
  },

  /**
   * 获取NPS得分
   * @returns NPS得分数据
   */
  getNPSScore: () => {
    return api.get('/api/v1/crm/nps/score')
  },

  /**
   * 管理端分页查询知识库文章列表
   * @param params 查询参数（page/size/keyword/category/status）
   * @returns 文章分页列表
   */
  getKnowledgeList: (params: any) => {
    return api.get('/api/v1/crm/knowledge/list', { params })
  },

  /**
   * 更新知识库文章
   * @param id 文章ID
   * @param data 文章数据
   * @returns 更新结果
   */
  updateKnowledgeArticle: (id: string | number, data: any) => {
    return api.put(`/api/v1/crm/knowledge/${id}`, data)
  },

  /**
   * 删除知识库文章
   * @param id 文章ID
   * @returns 删除结果
   */
  deleteKnowledgeArticle: (id: string | number) => {
    return api.delete(`/api/v1/crm/knowledge/${id}`)
  },

  /**
   * 发布知识库文章
   * @param id 文章ID
   * @returns 发布结果
   */
  publishKnowledgeArticle: (id: string | number) => {
    return api.post(`/api/v1/crm/knowledge/${id}/publish`)
  },

  /**
   * 下架知识库文章
   * @param id 文章ID
   * @returns 下架结果
   */
  offlineKnowledgeArticle: (id: string | number) => {
    return api.post(`/api/v1/crm/knowledge/${id}/offline`)
  },

  /**
   * 分页查询满意度调查列表
   * @param params 查询参数（page/size/keyword/surveyType）
   * @returns 调查分页列表
   */
  getSurveyList: (params: any) => {
    return api.get('/api/v1/crm/surveys/list', { params })
  },

  /**
   * 分页查询NPS调查列表
   * @param params 查询参数（page/size/keyword）
   * @returns NPS调查分页列表
   */
  getNpsSurveyList: (params: any) => {
    return api.get('/api/v1/crm/nps/list', { params })
  }
}