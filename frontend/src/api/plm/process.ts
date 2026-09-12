/**
 * PLM工艺协同API服务（工艺路线/工艺文件/工艺变更）
 * - 统一网关前缀：/plm/**
 * 说明：后端接口若未全部补齐，调用方需做好空数据兜底。
 * @author author
 * @date 2026-02-01
 */

import api from '../index'

const API_PREFIX = '/api/v1/plm'

export const processApi = {
  /**
   * 获取工艺路线列表
   * @param params 查询参数
   */
  getProcessRoutes: (params?: any) => {
    return api.get(`${API_PREFIX}/process/routes`, { params })
  },

  /**
   * 创建工艺路线
   * @param data 工艺路线数据
   */
  createProcessRoute: (data: any) => {
    return api.post(`${API_PREFIX}/process/routes`, data)
  },

  /**
   * 更新工艺路线
   * @param id 工艺路线ID
   * @param data 更新数据
   */
  updateProcessRoute: (id: string | number, data: any) => {
    return api.put(`${API_PREFIX}/process/routes/${id}`, data)
  },

  /**
   * 删除工艺路线
   * @param id 工艺路线ID
   */
  deleteProcessRoute: (id: string | number) => {
    return api.delete(`${API_PREFIX}/process/routes/${id}`)
  },

  /**
   * 获取工艺文件列表
   * @param params 查询参数
   */
  getProcessFiles: (params?: any) => {
    return api.get(`${API_PREFIX}/process/files`, { params })
  },

  /**
   * 上传工艺文件（元数据）
   * @param data 文件元数据
   */
  createProcessFile: (data: any) => {
    return api.post(`${API_PREFIX}/process/files`, data)
  },

  /**
   * 更新工艺文件（元数据）
   * @param id 文件ID
   * @param data 更新数据
   */
  updateProcessFile: (id: string | number, data: any) => {
    return api.put(`${API_PREFIX}/process/files/${id}`, data)
  },

  uploadProcessFile: (formData: FormData) => {
    return api.post(`${API_PREFIX}/process/files/upload`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  downloadProcessFile: (id: string | number) => {
    return api.get(`${API_PREFIX}/process/files/${id}/download`, { responseType: 'blob' })
  },

  /**
   * 删除工艺文件
   * @param id 文件ID
   */
  deleteProcessFile: (id: string | number) => {
    return api.delete(`${API_PREFIX}/process/files/${id}`)
  },

  /**
   * 获取工艺变更列表
   * @param params 查询参数
   */
  getProcessChanges: (params?: any) => {
    return api.get(`${API_PREFIX}/process/changes`, { params })
  },

  /**
   * 创建工艺变更
   * @param data 工艺变更数据
   */
  createProcessChange: (data: any) => {
    return api.post(`${API_PREFIX}/process/changes`, data)
  },

  /**
   * 更新工艺变更
   * @param id 变更ID
   * @param data 更新数据
   */
  updateProcessChange: (id: string | number, data: any) => {
    return api.put(`${API_PREFIX}/process/changes/${id}`, data)
  },

  /**
   * 删除工艺变更
   * @param id 变更ID
   */
  deleteProcessChange: (id: string | number) => {
    return api.delete(`${API_PREFIX}/process/changes/${id}`)
  }
}
