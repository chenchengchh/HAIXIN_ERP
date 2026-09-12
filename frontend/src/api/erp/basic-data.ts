import api from '../index'
import type {
  Customer,
  Supplier,
  Material,
  Account,
  Organization,
  CustomerQueryParams,
  SupplierQueryParams,
  MaterialQueryParams,
  AccountQueryParams,
  OrganizationQueryParams,
  PaginationResponse
} from '../../types/erp/basic-data'

/**
 * 基础数据模块API
 */
export const basicDataApi = {
  // 组织架构
  /**
   * 获取组织架构列表
   * @param params 查询参数
   * @param params.page 页码
   * @param params.size 每页条数
   * @param params.name 组织名称
   * @param params.code 组织代码
   * @param params.status 状态（1-启用，0-禁用）
   * @returns 组织架构列表数据
   */
  getOrganization: (params: any) => api.get('/api/v1/erp/basic-data/organization', { params }),
  
  /**
   * 创建组织
   * @param data 组织数据
   * @returns 创建结果
   */
  createOrganization: (data: any) => api.post('/api/v1/erp/basic-data/organization', data),
  
  /**
   * 更新组织
   * @param id 组织ID
   * @param data 更新数据
   * @returns 更新结果
   */
  updateOrganization: (id: number, data: any) => api.put(`/api/v1/erp/basic-data/organization/${id}`, data),
  
  /**
   * 删除组织
   * @param id 组织ID
   * @returns 删除结果
   */
  deleteOrganization: (id: number) => api.delete(`/api/v1/erp/basic-data/organization/${id}`),
  
  /**
   * 创建部门
   * @param data 部门数据
   * @returns 创建结果
   */
  createDepartment: (data: any) => api.post('/api/v1/erp/basic-data/departments', data),

  /**
   * 获取部门列表
   */
  getDepartments: (params: any) => api.get('/api/v1/erp/basic-data/departments', { params }),

  /**
   * 更新部门
   */
  updateDepartment: (id: number, data: any) => api.put(`/api/v1/erp/basic-data/departments/${id}`, data),

  /**
   * 删除部门
   */
  deleteDepartment: (id: number) => api.delete(`/api/v1/erp/basic-data/departments/${id}`),
  
  /**
   * 创建岗位
   * @param data 岗位数据
   * @returns 创建结果
   */
  createPosition: (data: any) => api.post('/api/v1/erp/basic-data/positions', data),

  /**
   * 获取岗位列表
   */
  getPositions: (params: any) => api.get('/api/v1/erp/basic-data/positions', { params }),

  /**
   * 更新岗位
   */
  updatePosition: (id: number, data: any) => api.put(`/api/v1/erp/basic-data/positions/${id}`, data),

  /**
   * 删除岗位
   */
  deletePosition: (id: number) => api.delete(`/api/v1/erp/basic-data/positions/${id}`),
  
  /**
   * 获取人员列表
   * @param params 查询参数
   * @returns 人员列表数据
   */
  getEmployees: (params: any) => api.get('/api/v1/erp/basic-data/employees', { params }),
  
  /**
   * 创建人员
   * @param data 人员数据
   * @returns 创建结果
   */
  createEmployee: (data: any) => api.post('/api/v1/erp/basic-data/employees', data),

  updateEmployee: (id: number, data: any) => api.put(`/api/v1/erp/basic-data/employees/${id}`, data),

  deleteEmployee: (id: number) => api.delete(`/api/v1/erp/basic-data/employees/${id}`),

  // 客户管理
  /**
   * 获取客户列表
   * @param params 查询参数
   * @returns 客户列表数据
   */
  getCustomers: (params: CustomerQueryParams) => api.get<PaginationResponse<Customer>>('/api/v1/erp/basic-data/customers', { params }),
  
  /**
   * 创建客户
   * @param data 客户数据
   * @returns 创建结果
   */
  createCustomer: (data: Omit<Customer, 'id' | 'create_time' | 'update_time'>) => api.post<Customer>('/api/v1/erp/basic-data/customers', data),
  
  /**
   * 更新客户
   * @param id 客户ID
   * @param data 更新数据
   * @returns 更新结果
   */
  updateCustomer: (id: number, data: Partial<Customer>) => api.put<Customer>(`/api/v1/erp/basic-data/customers/${id}`, data),
  
  /**
   * 获取客户详情
   * @param id 客户ID
   * @returns 客户详情数据
   */
  getCustomerDetail: (id: number) => api.get<Customer>(`/api/v1/erp/basic-data/customers/${id}`),
  
  /**
   * 删除客户
   * @param id 客户ID
   * @returns 删除结果
   */
  deleteCustomer: (id: number) => api.delete<void>(`/api/v1/erp/basic-data/customers/${id}`),

  // 供应商管理
  /**
   * 获取供应商列表
   * @param params 查询参数
   * @returns 供应商列表数据
   */
  getSuppliers: (params: SupplierQueryParams) => api.get<PaginationResponse<Supplier>>('/api/v1/erp/basic-data/suppliers', { params }),
  
  /**
   * 创建供应商
   * @param data 供应商数据
   * @returns 创建结果
   */
  createSupplier: (data: Omit<Supplier, 'id' | 'create_time' | 'update_time'>) => api.post<Supplier>('/api/v1/erp/basic-data/suppliers', data),
  
  /**
   * 更新供应商
   * @param id 供应商ID
   * @param data 更新数据
   * @returns 更新结果
   */
  updateSupplier: (id: number, data: Partial<Supplier>) => api.put<Supplier>(`/api/v1/erp/basic-data/suppliers/${id}`, data),
  
  /**
   * 获取供应商详情
   * @param id 供应商ID
   * @returns 供应商详情数据
   */
  getSupplierDetail: (id: number) => api.get<Supplier>(`/api/v1/erp/basic-data/suppliers/${id}`),
  
  /**
   * 删除供应商
   * @param id 供应商ID
   * @returns 删除结果
   */
  deleteSupplier: (id: number) => api.delete<void>(`/api/v1/erp/basic-data/suppliers/${id}`),

  // 物料管理
  /**
   * 获取物料列表
   * @param params 查询参数
   * @returns 物料列表数据
   */
  getMaterials: (params: MaterialQueryParams) => api.get<PaginationResponse<Material>>('/api/v1/erp/basic-data/materials', { params }),
  
  /**
   * 创建物料
   * @param data 物料数据
   * @returns 创建结果
   */
  createMaterial: (data: Omit<Material, 'id' | 'create_time' | 'update_time'>) => api.post<Material>('/api/v1/erp/basic-data/materials', data),
  
  /**
   * 更新物料
   * @param id 物料ID
   * @param data 更新数据
   * @returns 更新结果
   */
  updateMaterial: (id: number, data: Partial<Material>) => api.put<Material>(`/api/v1/erp/basic-data/materials/${id}`, data),
  
  /**
   * 删除物料
   * @param id 物料ID
   * @returns 删除结果
   */
  deleteMaterial: (id: number) => api.delete<void>(`/api/v1/erp/basic-data/materials/${id}`),

  approveMaterial: (id: number) => api.post(`/api/v1/erp/basic-data/materials/${id}/approve`),

  batchApproveMaterials: (ids: number[]) => api.post(`/api/v1/erp/basic-data/materials/batch-approve`, { ids }),
  
  /**
   * 获取物料分类列表
   * @param params 查询参数
   * @returns 物料分类列表数据
   */
  getMaterialCategories: (params: any) => api.get('/api/v1/erp/basic-data/material-categories', { params }),
  
  /**
   * 获取BOM列表
   * @param params 查询参数
   * @returns BOM列表数据
   */
  getBOMs: (params: any) => api.get('/api/v1/erp/basic-data/boms', { params }),
  
  /**
   * 创建BOM
   * @param data BOM数据
   * @returns 创建结果
   */
  createBOM: (data: any) => api.post('/api/v1/erp/basic-data/boms', data),

  // 会计科目
  /**
   * 获取会计科目列表
   * @param params 查询参数
   * @returns 会计科目列表数据
   */
  getAccounts: (params: AccountQueryParams) => api.get<PaginationResponse<Account>>('/api/v1/erp/basic-data/accounts', { params }),
  
  /**
   * 创建会计科目
   * @param data 会计科目数据
   * @returns 创建结果
   */
  createAccount: (data: Omit<Account, 'id' | 'create_time' | 'update_time'>) => api.post<Account>('/api/v1/erp/basic-data/accounts', data),
  
  /**
   * 更新会计科目
   * @param id 会计科目ID
   * @param data 更新数据
   * @returns 更新结果
   */
  updateAccount: (id: number, data: Partial<Account>) => api.put<Account>(`/api/v1/erp/basic-data/accounts/${id}`, data),
  
  /**
   * 删除会计科目
   * @param id 会计科目ID
   * @returns 删除结果
   */
  deleteAccount: (id: number) => api.delete<void>(`/api/v1/erp/basic-data/accounts/${id}`),
  
  /**
   * 获取仓库列表
   * @param params 查询参数
   * @returns 仓库列表数据
   */
  getWarehouses: (params: any = {}) => api.get('/api/v1/erp/basic-data/warehouses', { params }),

  createWarehouse: (data: any) => api.post('/api/v1/erp/basic-data/warehouses', data),

  updateWarehouse: (id: number, data: any) => api.put(`/api/v1/erp/basic-data/warehouses/${id}`, data),

  deleteWarehouse: (id: number) => api.delete(`/api/v1/erp/basic-data/warehouses/${id}`)
}
