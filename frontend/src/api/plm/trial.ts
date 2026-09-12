/**
 * PLM试产管理API服务（试产计划/试产报告/质量问题）
 * - 统一网关前缀：/plm/**
 * 说明：后端接口若未全部补齐，调用方需做好空数据兜底。
 * @author author
 * @date 2026-02-01
 */

import api from '../index'

const API_PREFIX = '/api/v1/plm'

export const trialApi = {
  /**
   * 获取试产计划列表
   * @param params 查询参数
   */
  getTrialPlans: (params?: any) => {
    return api.get(`${API_PREFIX}/trial/plans`, { params })
  },

  /**
   * 创建试产计划
   * @param data 试产计划数据
   */
  createTrialPlan: (data: any) => {
    return api.post(`${API_PREFIX}/trial/plans`, data)
  },

  /**
   * 更新试产计划
   * @param id 计划ID
   * @param data 更新数据
   */
  updateTrialPlan: (id: string | number, data: any) => {
    return api.put(`${API_PREFIX}/trial/plans/${id}`, data)
  },

  /**
   * 删除试产计划
   * @param id 计划ID
   */
  deleteTrialPlan: (id: string | number) => {
    return api.delete(`${API_PREFIX}/trial/plans/${id}`)
  },

  /**
   * 获取试产报告列表
   * @param params 查询参数
   */
  getTrialReports: (params?: any) => {
    return api.get(`${API_PREFIX}/trial/reports`, { params })
  },

  /**
   * 创建试产报告
   * @param data 报告数据
   */
  createTrialReport: (data: any) => {
    return api.post(`${API_PREFIX}/trial/reports`, data)
  },

  /**
   * 更新试产报告
   * @param id 报告ID
   * @param data 更新数据
   */
  updateTrialReport: (id: string | number, data: any) => {
    return api.put(`${API_PREFIX}/trial/reports/${id}`, data)
  },

  /**
   * 删除试产报告
   * @param id 报告ID
   */
  deleteTrialReport: (id: string | number) => {
    return api.delete(`${API_PREFIX}/trial/reports/${id}`)
  },

  /**
   * 获取质量问题列表
   * @param params 查询参数
   */
  getQualityIssues: (params?: any) => {
    return api.get(`${API_PREFIX}/trial/quality-issues`, { params })
  },

  /**
   * 创建质量问题
   * @param data 质量问题数据
   */
  createQualityIssue: (data: any) => {
    return api.post(`${API_PREFIX}/trial/quality-issues`, data)
  },

  /**
   * 更新质量问题
   * @param id 问题ID
   * @param data 更新数据
   */
  updateQualityIssue: (id: string | number, data: any) => {
    return api.put(`${API_PREFIX}/trial/quality-issues/${id}`, data)
  },

  /**
   * 删除质量问题
   * @param id 问题ID
   */
  deleteQualityIssue: (id: string | number) => {
    return api.delete(`${API_PREFIX}/trial/quality-issues/${id}`)
  }
}
