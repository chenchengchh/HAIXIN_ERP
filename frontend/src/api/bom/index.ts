import api from '../index'

/**
 * BOM模块 API
 */

// 物料接口定义
export interface Material {
  id?: number
  materialCode: string
  materialName: string
  materialType?: string
  unit: string
  materialSpec?: string
  status?: string
  remark?: string
  createdBy?: string
  createdTime?: string
  updatedBy?: string
  updatedTime?: string
  specification?: string
}

export interface BomHeader {
  id?: number
  materialId: number
  materialCode?: string
  materialName?: string
  bomCode: string
  version: string
  type?: number
  status?: number
  isDefault?: boolean
  effectiveDate?: string
  expireDate?: string
  remark?: string
}

export interface BomLine {
  id?: number
  headerId?: number
  parentMaterialId?: number
  childMaterialId: number
  childMaterialCode?: string
  childMaterialName?: string
  quantity: number
  unit: string
  scrapRate?: number
  level?: number
  sequence?: number
  usageType?: string
  effectiveDate?: string
  expireDate?: string
  remark?: string
}

// BOM树节点接口定义
export interface BomNode {
  id: number | string
  materialId: number
  materialCode: string
  materialName: string
  specification: string
  unit: string
  quantity: number
  level: number
  type: 'root' | 'sub' | 'virtual'
  scrapRate?: number
  remark?: string
  unitCost?: number
  totalCost?: number
  children?: BomNode[]
}

// 替代料接口定义
export interface Substitute {
  id?: number
  mainMaterialId: number
  subMaterialId: number
  bomLineId?: number
  ratio: number
  priority?: number
  status: number
  remark?: string
  mainMaterialCode?: string
  mainMaterialName?: string
  mainMaterialSpec?: string
  subMaterialCode?: string
  subMaterialName?: string
  subMaterialSpec?: string
  substituteType?: number
  createdTime?: string
  updatedTime?: string
}

export interface MaterialCategory {
  id?: number
  name: string
  code: string
  parentId?: number
  description?: string
  status?: number
  remark?: string
  children?: MaterialCategory[]
}

/**
 * BOM模块 API 封装
 */
export const bomApi = {
  // 物料管理
  getMaterialList: (params: any) => api.get('/api/v1/bom/material', { params }),
  getMaterialDetail: (id: number | string) => api.get(`/api/v1/bom/material/${id}`),
  getMaterialByCode: (code: string) => api.get('/api/v1/bom/material', { params: { code, page: 1, size: 1 } }),
  createMaterial: (data: Material) => api.post('/api/v1/bom/material', data),
  updateMaterial: (id: number | string, data: Material) => api.put(`/api/v1/bom/material/${id}`, data),
  deleteMaterial: (id: number | string) => api.delete(`/api/v1/bom/material/${id}`),
  exportMaterials: (params: any) => api.get('/api/v1/bom/material/export', { params, responseType: 'blob' }),
  importMaterials: (formData: FormData) => api.post('/api/v1/bom/material/import', formData, { headers: { 'Content-Type': 'multipart/form-data' } }),
  // 物料主数据只读配置：返回 materialWriteEnabled/authorityHint，前端据此禁用写操作
  getMaterialConfig: () => api.get('/api/v1/bom/material/config'),

  // 物料分类
  getCategoryTree: () => api.get('/api/v1/bom/category/tree'),
  createCategory: (data: MaterialCategory) => api.post('/api/v1/bom/category', data),
  updateCategory: (id: number | string, data: MaterialCategory) => api.put(`/api/v1/bom/category/${id}`, data),
  deleteCategory: (id: number | string) => api.delete(`/api/v1/bom/category/${id}`),

  // BOM 结构管理
  getBomTree: (materialId: number | string, version: string = 'V1.0') =>
    api.get(`/api/v1/bom/structure/tree/${materialId}`, { params: { version } }),
  getWhereUsed: (materialId: number | string) => 
    api.get(`/api/v1/bom/structure/where-used/${materialId}`),
  createBomHeader: (data: BomHeader) => api.post('/api/v1/bom/structure', data),
  updateBomHeader: (id: number | string, data: BomHeader) => api.put(`/api/v1/bom/structure/${id}`, data),
  deleteBomHeader: (id: number | string) => api.delete(`/api/v1/bom/structure/${id}`),
  getBomHeaderDetail: (id: number | string) => api.get(`/api/v1/bom/structure/header/${id}`),
  getBomHeadersByMaterialId: (materialId: number | string) => api.get(`/api/v1/bom/structure/material/${materialId}`),
  getDefaultBomByMaterialId: (materialId: number | string) => api.get(`/api/v1/bom/structure/material/${materialId}/default`),
  getBomLinesByHeaderId: (headerId: number | string) => api.get(`/api/v1/bom/structure/header/${headerId}/lines`),
  addBomLine: (headerId: number | string, data: BomLine) => api.post(`/api/v1/bom/structure/${headerId}/lines`, data),
  updateBomLine: (lineId: number | string, data: BomLine) => api.put(`/api/v1/bom/structure/lines/${lineId}`, data),
  deleteBomLine: (lineId: number | string) => api.delete(`/api/v1/bom/structure/lines/${lineId}`),
  replaceBomLines: (headerId: number | string, lines: BomLine[]) =>
    api.put(`/api/v1/bom/structure/${headerId}/lines/bulk`, lines),

  // 替代料管理
  getSubstituteList: (params: any) => api.get('/api/v1/bom/substitute', { params }),
  getSubstituteDetail: (id: number | string) => api.get(`/api/v1/bom/substitute/${id}`),
  getSubstitutesByMaterial: (materialId: number | string) => api.get(`/api/v1/bom/substitute/query/${materialId}`),
  createSubstitute: (data: Substitute) => api.post('/api/v1/bom/substitute', data),
  updateSubstitute: (id: number | string, data: Substitute) => api.put(`/api/v1/bom/substitute/${id}`, data),
  deleteSubstitute: (id: number | string) => api.delete(`/api/v1/bom/substitute/${id}`),
  enableSubstitute: (id: number | string) => api.put(`/api/v1/bom/substitute/${id}/enable`),
  disableSubstitute: (id: number | string) => api.put(`/api/v1/bom/substitute/${id}/disable`),

  // BOM 版本管理
  getBomVersions: (params: any) => api.get('/api/v1/bom/version', { params }),
  getBomVersionDetail: (id: number | string) => api.get(`/api/v1/bom/version/${id}`),
  createBomVersion: (data: any) => api.post('/api/v1/bom/version', data),
  updateBomVersion: (id: number | string, data: any) => api.put(`/api/v1/bom/version/${id}`, data),
  activateVersion: (id: number | string) => api.put(`/api/v1/bom/version/${id}/activate`),
  deactivateVersion: (id: number | string) => api.put(`/api/v1/bom/version/${id}/deactivate`),
  setDefaultVersion: (id: number | string) => api.put(`/api/v1/bom/version/${id}/set-default`),
  compareBoms: (v1: number | string, v2: number | string) => 
    api.get('/api/v1/bom/structure/compare', { params: { id1: v1, id2: v2 } }),
  // 变更历史管理
  getBomChangeHistory: (id: number | string) => api.get(`/api/v1/bom/version/${id}/change-history`),

  // 替代规则配置
  getSubstituteRules: (params: any) => api.get('/api/v1/bom/substitute-rules', { params }),
  getSubstituteRuleDetail: (id: number | string) => api.get(`/api/v1/bom/substitute-rules/${id}`),
  createSubstituteRule: (data: any) => api.post('/api/v1/bom/substitute-rules', data),
  updateSubstituteRule: (id: number | string, data: any) => api.put(`/api/v1/bom/substitute-rules/${id}`, data),
  deleteSubstituteRule: (id: number | string) => api.delete(`/api/v1/bom/substitute-rules/${id}`),
  enableSubstituteRule: (id: number | string) => api.put(`/api/v1/bom/substitute-rules/${id}/enable`),
  disableSubstituteRule: (id: number | string) => api.put(`/api/v1/bom/substitute-rules/${id}/disable`)
}
