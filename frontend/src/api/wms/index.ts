import request, { unwrapResponseData } from '../index'

const unwrap = async <T = any>(p: Promise<any>): Promise<{ data: T }> => {
  const res = await p
  return { data: unwrapResponseData<T>(res) as T }
}

const WMS_V1 = '/api/v1/wms'

export const locationApi = {
  getList: (params?: any) => unwrap(request.get(`${WMS_V1}/base/locations`, { params })),
  getDetail: (id: string | number) => unwrap(request.get(`${WMS_V1}/base/locations/${id}`)),
  create: (data: any) => unwrap(request.post(`${WMS_V1}/base/locations`, data)),
  batchCreate: (data: any) => unwrap(request.post(`${WMS_V1}/base/locations/batch`, data)),
  update: (id: string | number, data: any) => unwrap(request.put(`${WMS_V1}/base/locations/${id}`, data)),
  delete: (id: string | number) => unwrap(request.delete(`${WMS_V1}/base/locations/${id}`)),
  updateStatus: (id: string | number, status: string) =>
    unwrap(request.put(`${WMS_V1}/base/locations/${id}/status`, null, { params: { status } })),
  generateCodes: (params?: any) => unwrap(request.get(`${WMS_V1}/base/locations/generate-codes`, { params })),
  printCodes: (data: { codes: string[] }) => unwrap(request.post(`${WMS_V1}/base/locations/print-codes`, data))
}

export const zoneApi = {
  getList: (params?: any) => unwrap(request.get(`${WMS_V1}/base/zones`, { params })),
  getDetail: (id: string | number) => unwrap(request.get(`${WMS_V1}/base/zones/${id}`)),
  create: (data: any) => unwrap(request.post(`${WMS_V1}/base/zones`, data)),
  update: (id: string | number, data: any) => unwrap(request.put(`${WMS_V1}/base/zones/${id}`, data)),
  delete: (id: string | number) => unwrap(request.delete(`${WMS_V1}/base/zones/${id}`)),
  updateStatus: (id: string | number, status: string) =>
    unwrap(request.put(`${WMS_V1}/base/zones/${id}/status`, null, { params: { status } }))
}

export const warehouseAreaApi = zoneApi

export const locationTypeApi = {
  getList: (params?: any) => unwrap(request.get(`${WMS_V1}/base/location-types`, { params })),
  create: (data: any) => unwrap(request.post(`${WMS_V1}/base/location-types`, data)),
  update: (id: string | number, data: any) => unwrap(request.put(`${WMS_V1}/base/location-types/${id}`, data)),
  delete: (id: string | number) => unwrap(request.delete(`${WMS_V1}/base/location-types/${id}`)),
  updateStatus: (id: string | number, status: string) =>
    unwrap(request.put(`${WMS_V1}/base/location-types/${id}/status`, null, { params: { status } }))
}

export const warehouseApi = {
  getList: (params?: any) => unwrap(request.get(`${WMS_V1}/warehouse/list`, { params })),
  getDetail: (id: string | number) => unwrap(request.get(`${WMS_V1}/warehouse/${id}`)),
  create: (data: any) => unwrap(request.post(`${WMS_V1}/warehouse/create`, data)),
  update: (id: string | number, data: any) => unwrap(request.put(`${WMS_V1}/warehouse/${id}`, data)),
  delete: (id: string | number) => unwrap(request.delete(`${WMS_V1}/warehouse/${id}`))
}

export const asnApi = {
  getList: (params?: any) => unwrap(request.get(`${WMS_V1}/inbound/asn`, { params })),
  getDetail: (id: string | number) => unwrap(request.get(`${WMS_V1}/inbound/asn/${id}`)),
  create: (data: any) => unwrap(request.post(`${WMS_V1}/inbound/asn`, data)),
  update: (id: string | number, data: any) => unwrap(request.put(`${WMS_V1}/inbound/asn/${id}`, data)),
  delete: (id: string | number) => unwrap(request.delete(`${WMS_V1}/inbound/asn/${id}`)),
  confirmArrival: (id: string | number, data?: any) => unwrap(request.put(`${WMS_V1}/inbound/asn/${id}/confirm`, data ?? {})),
  start: (id: string | number) => unwrap(request.post(`${WMS_V1}/inbound/asn/${id}/start`, {})),
  cancel: (id: string | number) => unwrap(request.post(`${WMS_V1}/inbound/asn/${id}/cancel`, {})),
  scan: (id: string | number, barcode: string, quantity?: number) =>
    unwrap(request.post(`${WMS_V1}/inbound/asn/${id}/scan`, { barcode, quantity })),
  complete: (id: string | number) => unwrap(request.post(`${WMS_V1}/inbound/asn/${id}/complete`, {}))
}

export const inventoryApi = {
  getList: (params?: any) => unwrap(request.get(`${WMS_V1}/inventory/list`, { params })),
  create: (data: any) => unwrap(request.post(`${WMS_V1}/inventory`, data)),
  getDetail: (id: string | number) => unwrap(request.get(`${WMS_V1}/inventory/${id}`)),
  getByMaterial: (materialCode: string, _warehouseCode?: string) =>
    unwrap(request.get(`${WMS_V1}/inventory/by-material/${encodeURIComponent(materialCode)}`)),
  getByLocation: (locationCode: string) => unwrap(request.get(`${WMS_V1}/inventory/by-location/${encodeURIComponent(locationCode)}`)),
  getAlertList: (params?: any) => unwrap(request.get(`${WMS_V1}/inventory/alerts`, { params })),
  freeze: (id: string | number, data?: any) => unwrap(request.put(`${WMS_V1}/inventory/${id}/freeze`, null, { params: data })),
  unfreeze: (id: string | number) => unwrap(request.put(`${WMS_V1}/inventory/${id}/unfreeze`)),
  adjust: (id: string | number, data?: any) => unwrap(request.put(`${WMS_V1}/inventory/${id}/adjust`, null, { params: data })),
  countAdjust: (countId: string | number) => unwrap(request.post(`${WMS_V1}/inventory/count/${countId}/adjust`, {})),
  check: (id: string | number, actualQuantity: number) => unwrap(request.put(`${WMS_V1}/inventory/${id}/check`, null, { params: { actualQuantity } }))
}

export const inventoryV1Api = inventoryApi

export const inventoryAnalyticsApi = {
  getSummary: () => unwrap(request.get(`${WMS_V1}/inventory/analytics/summary`)),
  getAlerts: (params?: any) => unwrap(request.get(`${WMS_V1}/inventory/analytics/alerts`, { params })),
  getTrend: (params?: any) => unwrap(request.get(`${WMS_V1}/inventory/analytics/trend`, { params })),
  getDistribution: (params?: any) => unwrap(request.get(`${WMS_V1}/inventory/analytics/distribution`, { params }))
}

export const barcodeRuleApi = {
  getList: (params?: any) => unwrap(request.get(`${WMS_V1}/base/barcode-rules`, { params })),
  create: (data: any) => unwrap(request.post(`${WMS_V1}/base/barcode-rules`, data)),
  update: (id: string | number, data: any) => unwrap(request.put(`${WMS_V1}/base/barcode-rules/${id}`, data)),
  delete: (id: string | number) => unwrap(request.delete(`${WMS_V1}/base/barcode-rules/${id}`)),
  updateStatus: (id: string | number, status: string) =>
    unwrap(request.put(`${WMS_V1}/base/barcode-rules/${id}/status`, null, { params: { status } }))
}

export const materialTypeApi = {
  getList: (params?: any) => unwrap(request.get(`${WMS_V1}/base/material-types`, { params })),
  create: (data: any) => unwrap(request.post(`${WMS_V1}/base/material-types`, data)),
  update: (id: string | number, data: any) => unwrap(request.put(`${WMS_V1}/base/material-types/${id}`, data)),
  delete: (id: string | number) => unwrap(request.delete(`${WMS_V1}/base/material-types/${id}`)),
  updateStatus: (id: string | number, status: string) =>
    unwrap(request.put(`${WMS_V1}/base/material-types/${id}/status`, null, { params: { status } }))
}

export const receivingOrderApi = {
  getList: (_params?: any) => Promise.resolve({ data: { list: [], total: 0, page: 1, size: 10 } }),
  getDetail: (id: string | number) => Promise.resolve({ data: { id } }),
  create: (data: any) => Promise.resolve({ data }),
  update: (id: string | number, data: any) => Promise.resolve({ data: { id, ...data } }),
  delete: (_id: string | number) => Promise.resolve({ data: true }),
  complete: (id: string | number) => Promise.resolve({ data: { id, status: 'completed' } } as any),
  generatePutawayTask: (id: string | number) => Promise.resolve({ data: { id, tasks: [] } } as any)
}

export const putawayTaskApi = {
  getList: (_params?: any) => Promise.resolve({ data: { list: [], total: 0, page: 1, size: 10 } }),
  getDetail: (id: string | number) => Promise.resolve({ data: { id } }),
  create: (data: any) => Promise.resolve({ data }),
  update: (id: string | number, data: any) => Promise.resolve({ data: { id, ...data } }),
  delete: (_id: string | number) => Promise.resolve({ data: true }),
  assign: (id: string | number, data?: any) => Promise.resolve({ data: { id, ...(data ?? {}), status: 'assigned' } } as any),
  start: (id: string | number) => Promise.resolve({ data: { id, status: 'working' } } as any),
  complete: (id: string | number) => Promise.resolve({ data: { id, status: 'done' } } as any),
  cancel: (id: string | number) => Promise.resolve({ data: { id, status: 'cancelled' } } as any),
  pause: (id: string | number) => Promise.resolve({ data: { id, status: 'paused' } } as any),
  resume: (id: string | number) => Promise.resolve({ data: { id, status: 'working' } } as any),
  updateItem: (itemId: string | number, data?: any) => Promise.resolve({ data: { id: itemId, ...(data ?? {}) } } as any)
}

export const outboundOrderApi = {
  getList: (params?: any) => unwrap(request.get(`${WMS_V1}/outbound/orders`, { params })),
  getDetail: (id: string | number) => unwrap(request.get(`${WMS_V1}/outbound/orders/${id}`)),
  create: (data: any) => unwrap(request.post(`${WMS_V1}/outbound/orders`, data)),
  update: (id: string | number, data: any) => unwrap(request.put(`${WMS_V1}/outbound/orders/${id}`, data)),
  delete: (_id: string | number) => Promise.resolve({ data: true }),
  approve: (id: string | number) => unwrap(request.put(`${WMS_V1}/outbound/orders/${id}/approve`, {})),
  cancel: (id: string | number) => unwrap(request.put(`${WMS_V1}/outbound/orders/${id}/cancel`, {})),
  ship: (id: string | number) => unwrap(request.post(`${WMS_V1}/outbound/orders/${id}/ship`, {})),
  complete: (id: string | number) => unwrap(request.post(`${WMS_V1}/outbound/orders/${id}/ship`, {}))
}

export const waveApi = {
  getList: (params?: any) => unwrap(request.get(`${WMS_V1}/outbound/waves`, { params })),
  getDetail: (id: string | number) => unwrap(request.get(`${WMS_V1}/outbound/waves/${id}`)),
  create: (data: any) => unwrap(request.post(`${WMS_V1}/outbound/waves`, data)),
  autoCreate: (data?: any) => unwrap(request.post(`${WMS_V1}/outbound/waves/auto-create`, data ?? {})),
  update: (id: string | number, data?: any) => unwrap(request.put(`${WMS_V1}/outbound/waves/${id}/allocate`, data ?? {})),
  delete: (id: string | number) => unwrap(request.delete(`${WMS_V1}/outbound/waves/${id}`)),
  allocate: (id: string | number, data?: any) => unwrap(request.put(`${WMS_V1}/outbound/waves/${id}/allocate`, data ?? {})),
  startPicking: (id: string | number) => unwrap(request.put(`${WMS_V1}/outbound/waves/${id}/start-picking`, {})),
  release: (id: string | number) => unwrap(request.put(`${WMS_V1}/outbound/waves/${id}/release`, {})),
  complete: (id: string | number) => unwrap(request.put(`${WMS_V1}/outbound/waves/${id}/complete`, {})),
  cancel: (id: string | number) => unwrap(request.put(`${WMS_V1}/outbound/waves/${id}/cancel`, {}))
}

export const pickingTaskApi = {
  getList: (params?: any) => unwrap(request.get(`${WMS_V1}/outbound/picking-tasks`, { params })),
  getDetail: (id: string | number) => unwrap(request.get(`${WMS_V1}/outbound/picking-tasks/${id}`)),
  operators: () => unwrap(request.get(`${WMS_V1}/outbound/picking-tasks/operators`)),
  stats: (params?: any) => unwrap(request.get(`${WMS_V1}/outbound/picking-tasks/stats`, { params })),
  performance: (params?: any) => unwrap(request.get(`${WMS_V1}/outbound/picking-tasks/performance`, { params })),
  bottlenecks: (params?: any) => unwrap(request.get(`${WMS_V1}/outbound/picking-tasks/bottlenecks`, { params })),
  assign: (id: string | number, data?: any) => unwrap(request.put(`${WMS_V1}/outbound/picking-tasks/${id}/assign`, data ?? {})),
  start: (id: string | number) => unwrap(request.put(`${WMS_V1}/outbound/picking-tasks/${id}/start`, {})),
  complete: (id: string | number) => unwrap(request.put(`${WMS_V1}/outbound/picking-tasks/${id}/complete`, {})),
  cancel: (id: string | number) => unwrap(request.put(`${WMS_V1}/outbound/picking-tasks/${id}/cancel`, {})),
  pause: (id: string | number) => Promise.resolve({ data: { id: String(id), status: 'paused' } } as any),
  resume: (id: string | number) => Promise.resolve({ data: { id: String(id), status: 'working' } } as any),
  updateItem: (...args: any[]) => {
    const taskId = args.length >= 3 ? args[0] : args[1]?.taskId
    const itemId = args.length >= 3 ? args[1] : args[0]
    const data = args.length >= 3 ? args[2] : args[1]
    if (taskId === undefined || taskId === null || taskId === '') {
      return Promise.resolve({ data: { id: itemId, ...data } })
    }
    return unwrap(request.put(`${WMS_V1}/outbound/picking-tasks/${taskId}/items/${itemId}/qty`, data ?? {}))
  }
}

export const pickingTaskV1Api = pickingTaskApi

export const inventoryCountApi = {
  getList: (params?: any) => unwrap(request.get(`${WMS_V1}/stock/count-jobs`, { params })),
  getDetail: (id: string | number) => unwrap(request.get(`${WMS_V1}/stock/count-jobs/${id}`)),
  create: (data: any) => unwrap(request.post(`${WMS_V1}/stock/count-jobs`, data)),
  update: (id: string | number, data: any) => Promise.resolve({ data: { id, ...data } }),
  delete: (_id: string | number) => Promise.resolve({ data: true }),
  start: (id: string | number) => unwrap(request.put(`${WMS_V1}/stock/count-jobs/${id}/start`, {})),
  scan: (...args: any[]) => {
    const id = args[0]
    const payload = typeof args[1] === 'object' && args[1] !== null ? args[1] : { barcode: args[1], quantity: args[2] ?? 1 }
    return unwrap(request.post(`${WMS_V1}/stock/count-jobs/${id}/scan`, payload))
  },
  updateItem: (...args: any[]) => {
    if (args.length >= 3) {
      const countJobId = args[0]
      const itemId = args[1]
      const data = args[2] ?? {}
      return unwrap(request.put(`${WMS_V1}/stock/count-jobs/${countJobId}/items/${itemId}`, data))
    }
    const itemId = args[0]
    const data = args[1] ?? {}
    return Promise.resolve({ data: { id: itemId, ...data } })
  },
  save: (id: string | number) => unwrap(request.post(`${WMS_V1}/stock/count-jobs/${id}/save`, {})),
  complete: (id: string | number) => unwrap(request.post(`${WMS_V1}/stock/count-jobs/${id}/complete`, {})),
  cancel: (id: string | number) => unwrap(request.post(`${WMS_V1}/stock/count-jobs/${id}/cancel`, {})),
  generateDiffReport: (id: string | number) => Promise.resolve({ data: { id, list: [] } })
}

export const stockCountApi = {
  getList: (params?: any) => unwrap(request.get(`${WMS_V1}/stock/count-jobs`, { params })),
  getDetail: (id: string | number) => unwrap(request.get(`${WMS_V1}/stock/count-jobs/${id}`)),
  create: (data: any) => unwrap(request.post(`${WMS_V1}/stock/count-jobs`, data)),
  start: (id: string | number) => unwrap(request.put(`${WMS_V1}/stock/count-jobs/${id}/start`, {})),
  scan: (id: string | number, payload: { barcode: string; quantity?: number }) =>
    unwrap(request.post(`${WMS_V1}/stock/count-jobs/${id}/scan`, payload)),
  updateItem: (countJobId: string | number, itemId: string | number, data: any) =>
    unwrap(request.put(`${WMS_V1}/stock/count-jobs/${countJobId}/items/${itemId}`, data)),
  save: (id: string | number) => unwrap(request.post(`${WMS_V1}/stock/count-jobs/${id}/save`, {})),
  complete: (id: string | number) => unwrap(request.post(`${WMS_V1}/stock/count-jobs/${id}/complete`, {})),
  cancel: (id: string | number) => unwrap(request.post(`${WMS_V1}/stock/count-jobs/${id}/cancel`, {}))
}

export const inventoryMoveApi = {
  getList: (_params?: any) => Promise.resolve({ data: { list: [], total: 0, page: 1, size: 10 } } as any),
  getDetail: (id: string | number) => Promise.resolve({ data: { id } } as any),
  create: (data: any) => Promise.resolve({ data } as any),
  update: (id: string | number, data: any) => Promise.resolve({ data: { id, ...data } } as any),
  delete: (_id: string | number) => Promise.resolve({ data: true } as any),
  approve: (id: string | number) => Promise.resolve({ data: { id, status: 'approved' } } as any),
  start: (id: string | number) => Promise.resolve({ data: { id, status: 'working' } } as any),
  complete: (id: string | number) => Promise.resolve({ data: { id, status: 'done' } } as any),
  cancel: (id: string | number) => Promise.resolve({ data: { id, status: 'cancelled' } } as any)
}

export const pdaApi = {
  queryInventory: (params?: any) => unwrap(request.get(`${WMS_V1}/pda/inventory/query`, { params }))
}
