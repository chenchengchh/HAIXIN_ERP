/**
 * PLM产品数据API服务（产品/工程物料/BOM）
 * - 统一网关前缀：/plm/**
 * @author author
 * @date 2026-02-01
 */

import api from '../index'

const API_PREFIX = '/api/v1/plm'

export const productDataApi = {
  /**
   * 分页获取产品列表（后端为1-based页码，直接透传）
   * @param params 分页参数（page从1开始）
   */
  getProductsByPage: (params?: { page?: number; size?: number; keyword?: string; type?: string }) => {
    return api.get(`${API_PREFIX}/products/page`, {
      params: {
        page: params?.page ?? 1,
        size: params?.size ?? 10,
        keyword: params?.keyword,
        type: params?.type
      }
    })
  },

  /**
   * 获取产品详情
   * @param id 产品ID
   */
  getProductById: (id: string | number) => {
    return api.get(`${API_PREFIX}/products/${id}`)
  },

  /**
   * 创建产品
   * @param data 产品数据
   */
  createProduct: (data: any) => {
    return api.post(`${API_PREFIX}/products`, data)
  },

  /**
   * 更新产品
   * @param id 产品ID
   * @param data 更新数据
   */
  updateProduct: (id: string | number, data: any) => {
    return api.put(`${API_PREFIX}/products/${id}`, data)
  },

  /**
   * 删除产品
   * @param id 产品ID
   */
  deleteProduct: (id: string | number) => {
    return api.delete(`${API_PREFIX}/products/${id}`)
  },

  /**
   * 发布产品设计（后端会同步到BOM服务）
   * @param id 产品ID
   */
  releaseProduct: (id: string | number) => {
    return api.post(`${API_PREFIX}/products/${id}/release`)
  },

  /**
   * 分页获取BOM列表（后端为1-based页码，直接透传）
   * @param params 分页参数（page从1开始）
   */
  getBomsByPage: (params?: { page?: number; size?: number }) => {
    return api.get(`${API_PREFIX}/boms/page`, {
      params: { page: params?.page ?? 1, size: params?.size ?? 10 }
    })
  },

  /**
   * 获取产品的BOM明细
   * @param productId 产品ID
   */
  getBomsByProductId: (productId: string | number, version?: string) => {
    return api.get(`${API_PREFIX}/boms/product/${productId}`, { params: { version } })
  },

  /**
   * 创建BOM行
   * @param data BOM数据
   */
  createBom: (data: any) => {
    return api.post(`${API_PREFIX}/boms`, data)
  },

  /**
   * 更新BOM行
   * @param id BOM ID
   * @param data 更新数据
   */
  updateBom: (id: string | number, data: any) => {
    return api.put(`${API_PREFIX}/boms/${id}`, data)
  },

  /**
   * 删除BOM行
   * @param id BOM ID
   */
  deleteBom: (id: string | number) => {
    return api.delete(`${API_PREFIX}/boms/${id}`)
  },

  checkoutBom: (productId: string | number) => {
    return api.post(`${API_PREFIX}/boms/product/${productId}/checkout`)
  },

  checkinBom: (productId: string | number) => {
    return api.post(`${API_PREFIX}/boms/product/${productId}/checkin`)
  },

  releaseBom: (productId: string | number) => {
    return api.post(`${API_PREFIX}/boms/product/${productId}/release`)
  },

  compareBomVersions: (productId: string | number, fromVersion: string, toVersion: string) => {
    return api.get(`${API_PREFIX}/boms/compare`, { params: { productId, fromVersion, toVersion } })
  },

  getDocumentsByPage: (params?: { page?: number; size?: number; keyword?: string; type?: string }) => {
    return api.get(`${API_PREFIX}/documents/page`, {
      params: {
        page: params?.page ?? 1,
        size: params?.size ?? 10,
        keyword: params?.keyword,
        type: params?.type
      }
    })
  },

  createDocument: (data: any) => {
    return api.post(`${API_PREFIX}/documents`, data)
  },

  updateDocument: (id: string | number, data: any) => {
    return api.put(`${API_PREFIX}/documents/${id}`, data)
  },

  deleteDocument: (id: string | number) => {
    return api.delete(`${API_PREFIX}/documents/${id}`)
  },

  uploadDocument: (formData: FormData) => {
    return api.post(`${API_PREFIX}/documents/upload`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  downloadDocument: (id: string | number) => {
    return api.get(`${API_PREFIX}/documents/${id}/download`, { responseType: 'blob' })
  }
}
