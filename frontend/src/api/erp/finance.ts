import api from '../index'
import type {
  GeneralLedger,
  AccountsReceivable,
  AccountsPayable,
  CostAccounting,
  FixedAsset,
  Voucher,
} from '../../types/erp/finance'
import type { PaginationResponse } from '../../types/erp/basic-data'

/**
 * 财务模块API
 */
export const financeApi = {
  // 总账管理
  /**
   * 获取凭证列表
   */
  getVouchers: (params: any) => 
    api.get<PaginationResponse<Voucher>>('/api/v1/erp/finance/vouchers', { params }),
  
  /**
   * 创建凭证
   */
  createVoucher: (data: Partial<Voucher>) => 
    api.post<Voucher>('/api/v1/erp/finance/vouchers', data),
  
  /**
   * 更新凭证
   */
  updateVoucher: (id: number, data: Partial<Voucher>) => 
    api.put<Voucher>(`/api/v1/erp/finance/vouchers/${id}`, data),
  
  /**
   * 删除凭证
   */
  deleteVoucher: (id: number) => 
    api.delete<void>(`/api/v1/erp/finance/vouchers/${id}`),
  
  /**
   * 审核凭证
   */
  approveVoucher: (id: number) => 
    api.post<Voucher>(`/api/v1/erp/finance/vouchers/${id}/approve`),
  
  /**
   * 过账凭证
   */
  postVoucher: (id: number) => 
    api.post<Voucher>(`/api/v1/erp/finance/vouchers/${id}/post`),

  /**
   * 提交凭证（发起OA统一审批）
   * <p>凭证状态置为submitted（审批中），同时向OA提交审批申请，
   * 审批完成后OA回调更新凭证状态为approved/rejected。</p>
   * @param id 凭证ID
   * @param initiatorId 发起人ID（当前登录用户ID）
   * @param initiatorName 发起人名称（当前登录用户名称）
   */
  submitVoucher: (id: number, initiatorId?: number, initiatorName?: string) =>
    api.post<Voucher>(`/api/v1/erp/finance/vouchers/${id}/submit`, null, {
      params: {
        ...(initiatorId != null ? { initiatorId } : {}),
        ...(initiatorName ? { initiatorName } : {}),
      },
    }),

  rejectVoucher: (id: number) =>
    api.post<Voucher>(`/api/v1/erp/finance/vouchers/${id}/reject`),

  sendReceivableCollectionNotice: (id: number) =>
    api.post(`/api/v1/erp/finance/receivable/${id}/collection-notice`),

  getReceivableAgingReport: () =>
    api.get(`/api/v1/erp/finance/receivable/reports/aging`),

  getReceivableForecastReport: () =>
    api.get(`/api/v1/erp/finance/receivable/reports/forecast`),

  getReceivableCreditReport: () =>
    api.get(`/api/v1/erp/finance/receivable/reports/credit`),

  getPayablePlanReport: () =>
    api.get(`/api/v1/erp/finance/payable/reports/plan`),

  getPayableReconciliationReport: () =>
    api.get(`/api/v1/erp/finance/payable/reports/reconciliation`),

  getPayableAgingReport: () =>
    api.get(`/api/v1/erp/finance/payable/reports/aging`),

  // 应收管理
  /**
   * 获取应收账款列表
   */
  getAccountsReceivable: (params: any) =>
    api.get<PaginationResponse<AccountsReceivable>>('/api/v1/erp/finance/list?transactionType=receivable', { params }),
  
  /**
   * 创建收款单
   */
  createReceipt: (data: Omit<AccountsReceivable, 'id' | 'create_time'>) => 
    api.post<AccountsReceivable>('/api/v1/erp/finance', data),
  
  /**
   * 核销应收账款
   */
  writeOffReceivable: (data: any) => 
    api.post<void>('/api/v1/erp/finance/write-off', data),

  // 应付管理
  /**
   * 获取应付账款列表
   */
  getAccountsPayable: (params: any) => 
    api.get<PaginationResponse<AccountsPayable>>('/api/v1/erp/finance/list?transactionType=payable', { params }),
  
  /**
   * 创建付款单
   */
  createPayment: (data: Omit<AccountsPayable, 'id' | 'create_time'>) => 
    api.post<AccountsPayable>('/api/v1/erp/finance', data),
  
  /**
   * 核销应付账款
   */
  writeOffPayable: (data: any) => 
    api.post<void>('/api/v1/erp/finance/write-off', data),

  // 成本核算
  /**
   * 计算产品成本
   */
  calculateCost: (params: any) => 
    api.post<void>('/api/v1/erp/finance/cost-calculation', params),
  
  /**
   * 获取成本核算结果
   */
  getCostCalculationResults: (params: any) => 
    api.get('/api/v1/erp/finance/cost-results', { params }),
  
  /**
   * 获取成本核算列表
   */
  getCostAccountingList: (params: any) => 
    api.get<PaginationResponse<CostAccounting>>('/api/v1/erp/finance/cost-accounting', { params }),
  
  /**
   * 创建成本核算记录
   */
  createCostAccounting: (data: Omit<CostAccounting, 'id' | 'create_time'>) => 
    api.post<CostAccounting>('/api/v1/erp/finance/cost-accounting', data),
  
  /**
   * 更新成本核算记录
   */
  updateCostAccounting: (id: number, data: Partial<CostAccounting>) => 
    api.put<CostAccounting>(`/api/v1/erp/finance/cost-accounting/${id}`, data),
  
  /**
   * 删除成本核算记录
   */
  deleteCostAccounting: (id: number) => 
    api.delete<void>(`/api/v1/erp/finance/cost-accounting/${id}`),

  // 固定资产管理
  /**
   * 获取固定资产列表
   */
  getFixedAssets: (params: any) => 
    api.get<PaginationResponse<FixedAsset>>('/api/v1/erp/finance/fixed-assets', { params }),
  
  /**
   * 创建固定资产
   */
  createFixedAsset: (data: Omit<FixedAsset, 'id' | 'create_time'>) => 
    api.post<FixedAsset>('/api/v1/erp/finance/fixed-assets', data),
  
  /**
   * 更新固定资产
   */
  updateFixedAsset: (id: number, data: Partial<FixedAsset>) => 
    api.put<FixedAsset>(`/api/v1/erp/finance/fixed-assets/${id}`, data),
  
  /**
   * 删除固定资产
   */
  deleteFixedAsset: (id: number) => 
    api.delete<void>(`/api/v1/erp/finance/fixed-assets/${id}`),
  
  /**
   * 单个资产折旧计提
   */
  calculateDepreciation: (id: number, params: any) => 
    api.post<FixedAsset>(`/api/v1/erp/finance/fixed-assets/${id}/depreciation`, null, { params }),
  
  /**
   * 批量资产折旧计提
   */
  batchCalculateDepreciation: (assetIds: number[], params: any) => 
    api.post<FixedAsset[]>('/api/v1/erp/finance/fixed-assets/depreciation', assetIds, { params }),
  
  /**
   * 自动折旧计提
   */
  autoCalculateDepreciation: (params: any) => 
    api.post<FixedAsset[]>('/api/v1/erp/finance/fixed-assets/depreciation/auto', null, { params }),
  
  /**
   * 处置固定资产
   */
  disposeFixedAsset: (id: number, data: any) => 
    api.post<FixedAsset>(`/api/v1/erp/finance/fixed-assets/${id}/dispose`, data),
  
  /**
   * 固定资产盘点
   */
  inventoryFixedAssets: (data: any) => 
    api.post<FixedAsset[]>('/api/v1/erp/finance/fixed-assets/inventory', data),
  
  /**
   * 获取资产类别列表
   */
  getAssetCategories: () => 
    api.get<string[]>('/api/v1/erp/finance/fixed-assets/categories'),
  
  /**
   * 统计固定资产数量
   */
  countFixedAssets: (status?: number) => 
    api.get<number>(`/api/v1/erp/finance/fixed-assets/count${status ? `?status=${status}` : ''}`),

  syncFixedAssetsFromEam: () =>
    api.post(`/api/v1/erp/finance/fixed-assets/eam/sync`),

  linkFixedAssetEam: (id: number, eam_asset_id: number) =>
    api.post(`/api/v1/erp/finance/fixed-assets/${id}/eam/link`, { eam_asset_id }),

  getFixedAssetEam: (id: number) =>
    api.get(`/api/v1/erp/finance/fixed-assets/${id}/eam`),
  
  // 总账数据
  /**
   * 获取总账数据
   */
  getGeneralLedger: (params: any) => 
    api.get<PaginationResponse<GeneralLedger>>('/api/v1/erp/finance/general-ledger', { params })
}
