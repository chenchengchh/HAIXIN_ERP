import request, { unwrapResponseData } from '../index'
import type { PurchaseOrder, DeliveryNote } from '../../types/srm'
import { PurchaseOrderStatus } from '../../types/srm'

const unwrap = async <T = any>(p: Promise<any>): Promise<{ data: T }> => {
  const res = await p
  return { data: unwrapResponseData<T>(res) as T }
}

const SRM_V1 = '/api/v1/srm'

const mapScmOrderStatusToSrm = (v: unknown): PurchaseOrderStatus => {
  if (typeof v === 'string') {
    if ((Object.values(PurchaseOrderStatus) as string[]).includes(v)) {
      return v as PurchaseOrderStatus
    }
  }
  if (typeof v === 'number') {
    if (v === 10) return PurchaseOrderStatus.OPEN
    if (v === 30) return PurchaseOrderStatus.OPEN
    if (v === 40) return PurchaseOrderStatus.CONFIRMED
    if (v === 70) return PurchaseOrderStatus.CLOSED
    if (v === 90) return PurchaseOrderStatus.CLOSED
  }
  return PurchaseOrderStatus.DRAFT
}

const normalizePurchaseOrder = (raw: any): PurchaseOrder => {
  const totalAmount = Number(raw?.totalAmount ?? raw?.orderAmount ?? 0)
  return {
    id: raw?.id,
    orderNo: raw?.orderNo ?? '',
    supplierId: raw?.supplierId ?? 0,
    supplierName: raw?.supplierName ?? '',
    status: mapScmOrderStatusToSrm(raw?.status ?? raw?.orderStatus),
    purchaseDate: (raw?.purchaseDate ?? raw?.createdTime ?? raw?.created_time ?? '') as string,
    expectedDeliveryDate: (raw?.expectedDeliveryDate ?? raw?.expected_delivery_date ?? '') as string,
    totalAmount: Number.isFinite(totalAmount) ? totalAmount : 0,
    items: raw?.items ?? [],
    createTime: raw?.createTime ?? raw?.createdTime ?? raw?.created_time,
    updateTime: raw?.updateTime ?? raw?.updatedTime ?? raw?.updated_time
  }
}

// 采购执行相关API
export const procurementApi = {
  // 获取采购订单列表
  getPurchaseOrderList: (params?: any) => {
    const page = Math.max(0, Number(params?.page ?? 1) - 1)
    const size = Number(params?.size ?? params?.pageSize ?? 10)
    return unwrap<any>(request.get(`${SRM_V1}/purchase-orders`, { params: { ...params, page, size } })).then(res => {
      const list = (res.data?.list ?? res.data?.records ?? []) as any[]
      const total = Number(res.data?.total ?? list.length)
      return {
        data: {
          list: list.map(normalizePurchaseOrder),
          total: Number.isFinite(total) ? total : list.length
        }
      }
    })
  },
  
  // 获取采购订单详情
  getPurchaseOrderDetail: (id: number) => {
    return unwrap<any>(request.get(`${SRM_V1}/purchase-orders/${id}`)).then(res => ({
      data: normalizePurchaseOrder(res.data)
    }))
  },
  
  // 创建采购订单
  createPurchaseOrder: (data: Omit<PurchaseOrder, 'id' | 'orderNo' | 'createTime' | 'updateTime'>) => {
    return unwrap(request.post(`${SRM_V1}/purchase-orders`, data))
  },
  
  // 更新采购订单
  updatePurchaseOrder: (id: number, data: Partial<PurchaseOrder>) => {
    return unwrap(request.put(`${SRM_V1}/purchase-orders/${id}`, data))
  },
  
  // 供应商确认订单
  confirmPurchaseOrder: (id: number) => {
    return unwrap(request.post(`${SRM_V1}/purchase-orders/${id}/confirm`, {}))
  },

  // 获取采购申请列表
  getPurchaseRequestList: (params?: any) => {
    return unwrap(request.get(`${SRM_V1}/purchase-requests`, { params }))
  },

  // 获取采购申请详情
  getPurchaseRequestDetail: (id: number) => {
    return unwrap<any>(request.get(`${SRM_V1}/purchase-requests/${id}`))
  },

  // 创建采购申请
  createPurchaseRequest: (data: any) => {
    return unwrap(request.post(`${SRM_V1}/purchase-requests`, data))
  },

  // 更新采购申请
  updatePurchaseRequest: (id: number, data: any) => {
    return unwrap(request.put(`${SRM_V1}/purchase-requests/${id}`, data))
  },

  // 删除采购申请
  deletePurchaseRequest: (id: number) => {
    return unwrap(request.delete(`${SRM_V1}/purchase-requests/${id}`))
  },

  // 提交采购申请至OA审批
  submitPurchaseRequestApproval: (id: number) => {
    return unwrap(request.post(`${SRM_V1}/purchase-requests/${id}/submit-approval`, {}))
  },

  // 创建发货单(ASN)
  createDeliveryNote: (data: Omit<DeliveryNote, 'id' | 'deliveryNo' | 'createTime'>) => {
    return unwrap(request.post(`${SRM_V1}/delivery-notes`, data))
  },
  
  // 获取发货单列表
  getDeliveryNoteList: (params?: any) => {
    return unwrap(request.get(`${SRM_V1}/delivery-notes`, { params }))
  },
  
  // 获取发货单详情
  getDeliveryNoteDetail: (id: number) => {
    return unwrap<DeliveryNote>(request.get(`${SRM_V1}/delivery-notes/${id}`))
  }
}

// 询价管理API
export const inquiryApi = {
  // 获取询价单列表
  getInquiryList: (params?: any) => {
    return unwrap(request.get(`${SRM_V1}/inquiries`, { params }))
  },

  // 获取询价单详情
  getInquiryDetail: (id: number) => {
    return unwrap<any>(request.get(`${SRM_V1}/inquiries/${id}`))
  },

  // 创建询价单
  createInquiry: (data: any) => {
    return unwrap(request.post(`${SRM_V1}/inquiries`, data))
  },

  // 更新询价单
  updateInquiry: (id: number, data: any) => {
    return unwrap(request.put(`${SRM_V1}/inquiries/${id}`, data))
  },

  // 删除询价单
  deleteInquiry: (id: number) => {
    return unwrap(request.delete(`${SRM_V1}/inquiries/${id}`))
  },

  // 发布询价单
  publishInquiry: (id: number) => {
    return unwrap(request.put(`${SRM_V1}/inquiries/${id}/publish`, {}))
  },

  // 关闭询价单
  closeInquiry: (id: number) => {
    return unwrap(request.put(`${SRM_V1}/inquiries/${id}/close`, {}))
  }
}

// 报价管理API
export const quotationApi = {
  // 获取报价单列表
  getQuotationList: (params?: any) => {
    return unwrap(request.get(`${SRM_V1}/quotations`, { params }))
  },

  // 获取报价单详情
  getQuotationDetail: (id: number) => {
    return unwrap<any>(request.get(`${SRM_V1}/quotations/${id}`))
  },

  // 创建报价单
  createQuotation: (data: any) => {
    return unwrap(request.post(`${SRM_V1}/quotations`, data))
  },

  // 更新报价单
  updateQuotation: (id: number, data: any) => {
    return unwrap(request.put(`${SRM_V1}/quotations/${id}`, data))
  },

  // 删除报价单
  deleteQuotation: (id: number) => {
    return unwrap(request.delete(`${SRM_V1}/quotations/${id}`))
  },

  // 接受报价单（审核通过）
  acceptQuotation: (id: number) => {
    return unwrap(request.put(`${SRM_V1}/quotations/${id}/accept`, {}))
  },

  // 拒绝报价单（审核拒绝）
  rejectQuotation: (id: number) => {
    return unwrap(request.put(`${SRM_V1}/quotations/${id}/reject`, {}))
  },

  // 根据询价单ID获取报价单列表
  getQuotationsByInquiryId: (inquiryId: number, params?: any) => {
    return unwrap(request.get(`${SRM_V1}/inquiries/${inquiryId}/quotations`, { params }))
  },

  // 进行比价分析
  compareQuotations: (inquiryId: number) => {
    return unwrap<any[]>(request.get(`${SRM_V1}/inquiries/${inquiryId}/compare-quotes`))
  }
}

// 供应商管理相关API
export const supplierApi = {
  // 获取供应商列表
  getSupplierList: (params?: any) => {
    return unwrap<{ list: any[]; total: number }>(request.get('/api/v1/srm/suppliers', { params }))
  },

  // 获取供应商详情
  getSupplierDetail: (id: number) => {
    return unwrap<any>(request.get(`/api/v1/srm/suppliers/${id}`))
  },

  // 创建供应商
  createSupplier: (data: any) => {
    return unwrap(request.post('/api/v1/srm/suppliers', data))
  },

  // 更新供应商
  updateSupplier: (id: number, data: any) => {
    return unwrap(request.put(`/api/v1/srm/suppliers/${id}`, data))
  },

  // 获取供应商资质列表
  getSupplierQualifications: (supplierId: number) => {
    return unwrap<any[]>(request.get(`/api/v1/srm/suppliers/${supplierId}/qualifications`))
  },

  getQualificationList: (params?: any) => {
    return unwrap(request.get('/api/v1/srm/qualifications', { params }))
  },

  createQualification: (data: any) => {
    return unwrap(request.post('/api/v1/srm/qualifications', data))
  },

  updateQualification: (qualificationId: number, data: any) => {
    return unwrap(request.put(`/api/v1/srm/qualifications/${qualificationId}`, data))
  },

  deleteQualification: (qualificationId: number) => {
    return unwrap(request.delete(`/api/v1/srm/qualifications/${qualificationId}`))
  },

  // 更新供应商资质
  updateSupplierQualification: (supplierId: number, data: any) => {
    return unwrap(request.put(`/api/v1/srm/suppliers/${supplierId}/qualifications`, data))
  },

  // 加入黑名单
  addToBlacklist: (id: number, reason: string) => {
    return unwrap(request.put(`/api/v1/srm/suppliers/${id}/blacklist/add`, { reason }))
  },

  // 移出黑名单
  removeFromBlacklist: (id: number) => {
    return unwrap(request.put(`/api/v1/srm/suppliers/${id}/blacklist/remove`, {}))
  },

  promoteToQualified: (id: number) => {
    return unwrap(request.put(`/api/v1/srm/suppliers/${id}/promote-qualified`, {}))
  },

  promoteToPotential: (id: number) => {
    return unwrap(request.put(`/api/v1/srm/suppliers/${id}/promote-potential`, {}))
  },

  demoteSupplier: (id: number, reason?: string) => {
    return unwrap(request.put(`/api/v1/srm/suppliers/${id}/demote`, null, { params: { reason } }))
  },

  // 审核供应商资质
  auditQualification: (qualificationId: number, status: string, auditOpinion: string) => {
    return unwrap(request.put(`/api/v1/srm/supplier-qualifications/${qualificationId}/audit`, null, {
      params: { status, auditOpinion }
    }))
  },
  
  // 发货单管理
  getDeliveryNoteList: (params?: any) => {
    return unwrap(request.get('/api/v1/srm/delivery-notes', { params }))
  },
  
  getDeliveryNoteDetail: (id: number) => {
    return unwrap<any>(request.get(`/api/v1/srm/delivery-notes/${id}`))
  },
  
  createDeliveryNote: (data: any) => {
    return unwrap(request.post('/api/v1/srm/delivery-notes', data))
  },
  
  updateDeliveryNote: (id: number, data: any) => {
    return unwrap(request.put(`/api/v1/srm/delivery-notes/${id}`, data))
  },
 // 删除发货单
  deleteDeliveryNote: (id: number) => {
    return unwrap(request.delete(`/api/v1/srm/delivery-notes/${id}`))
  }
}

// 绩效考核API
export const scoreCardApi = {
  // 获取绩效考核列表
  getScoreCardList: (params?: any) => {
    return unwrap(request.get('/api/v1/srm/score-cards', { params }))
  },

  // 获取绩效考核详情
  getScoreCardDetail: (id: number) => {
    return unwrap<any>(request.get(`/api/v1/srm/score-cards/${id}`))
  },

  // 创建绩效考核
  createScoreCard: (data: any) => {
    const payload = {
      supplierId: data.supplierId,
      supplierName: data.supplierName,
      period: data.period,
      qualityScore: data.scoreQuality,
      deliveryScore: data.scoreDelivery,
      costScore: data.scorePrice,
      serviceScore: data.scoreService,
      totalScore: data.totalScore,
      grade: data.levelResult,
      comments: data.comments
    }
    return unwrap(request.post('/api/v1/srm/score-cards', payload))
  },

  // 根据供应商ID获取绩效考核
  getScoreCardsBySupplier: (supplierId: number, params?: any) => {
    return unwrap(request.get(`/api/v1/srm/suppliers/${supplierId}/score-cards`, { params }))
  }
}

// 供应商信用管理API
export const supplierCreditApi = {
  // 获取所有供应商信用列表
  getSupplierCreditList: (params?: any) => {
    return unwrap(request.get(`${SRM_V1}/supplier-credits`, { params }))
  },
  
  // 获取供应商信用详情
  getSupplierCredit: (supplierId: number) => {
    return unwrap<any>(request.get(`${SRM_V1}/suppliers/${supplierId}/credit`))
  },

  // 更新供应商信用
  updateSupplierCredit: (data: any) => {
    return unwrap(request.put(`${SRM_V1}/supplier-credits`, data))
  },

  // 计算供应商信用
  calculateSupplierCredit: (supplierId: number) => {
    return unwrap(request.post(`${SRM_V1}/suppliers/${supplierId}/credit/calculate`, {}))
  }
}

export const materialApi = {
  getMaterialCategoryList: () => {
    return unwrap<any[]>(request.get('/api/v1/srm/material-categories'))
  },
  getMaterialList: (params?: any) => {
    return unwrap(request.get('/api/v1/srm/materials', { params }))
  },
  getMaterialDetail: (id: number) => {
    return unwrap(request.get(`/api/v1/srm/materials/${id}`))
  },
  createMaterial: (data: any) => {
    return unwrap(request.post('/api/v1/srm/materials', data))
  },
  updateMaterial: (id: number, data: any) => {
    return unwrap(request.put(`/api/v1/srm/materials/${id}`, data))
  },
  deleteMaterial: (id: number) => {
    return unwrap(request.delete(`/api/v1/srm/materials/${id}`))
  }
}

export const materialForecastApi = {
  getForecastList: (params?: any) => {
    return unwrap(request.get('/api/v1/srm/material-forecasts', { params }))
  },
  getForecastDetail: (id: number) => {
    return unwrap(request.get(`/api/v1/srm/material-forecasts/${id}`))
  },
  createForecast: (data: any) => {
    return unwrap(request.post('/api/v1/srm/material-forecasts', data))
  },
  updateForecast: (id: number, data: any) => {
    return unwrap(request.put(`/api/v1/srm/material-forecasts/${id}`, data))
  },
  confirmForecast: (id: number) => {
    return unwrap(request.put(`/api/v1/srm/material-forecasts/${id}/confirm`, {}))
  },
  cancelForecast: (id: number) => {
    return unwrap(request.put(`/api/v1/srm/material-forecasts/${id}/cancel`, {}))
  }
}

export const reconciliationApi = {
  getReconciliationList: (params?: any) => {
    return unwrap(request.get('/api/v1/srm/reconciliations', { params }))
  },
  createReconciliation: (data: any) => {
    return unwrap(request.post('/api/v1/srm/reconciliations', data))
  },
  updateReconciliation: (id: number, data: any) => {
    return unwrap(request.put(`/api/v1/srm/reconciliations/${id}`, data))
  },
  confirmReconciliation: (id: number) => {
    return unwrap(request.put(`/api/v1/srm/reconciliations/${id}/confirm`, {}))
  },
  settleReconciliation: (id: number) => {
    return unwrap(request.put(`/api/v1/srm/reconciliations/${id}/settle`, {}))
  },
  cancelReconciliation: (id: number) => {
    return unwrap(request.put(`/api/v1/srm/reconciliations/${id}/cancel`, {}))
  }
}

export const qualityObjectionApi = {
  getObjectionList: (params?: any) => {
    return unwrap(request.get('/api/v1/srm/quality-objections', { params }))
  },
  createObjection: (data: any) => {
    return unwrap(request.post('/api/v1/srm/quality-objections', data))
  },
  updateObjection: (id: number, data: any) => {
    return unwrap(request.put(`/api/v1/srm/quality-objections/${id}`, data))
  },
  processObjection: (id: number, data: any) => {
    return unwrap(request.put(`/api/v1/srm/quality-objections/${id}/process`, data))
  },
  closeObjection: (id: number) => {
    return unwrap(request.put(`/api/v1/srm/quality-objections/${id}/close`, {}))
  }
}
