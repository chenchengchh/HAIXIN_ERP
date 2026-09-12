import api from '../index'

export interface ScadaDevice {
  id?: number
  deviceCode: string
  deviceName: string
  deviceType?: string
  deviceCategory?: string
  deviceStatus?: string
  location?: string
  ipAddress?: string
  port?: string
  protocol?: string
  collectInterval?: string
  lastOnlineTime?: string
  remark?: string
  createdBy?: string
  createdTime?: string
  updatedBy?: string
  updatedTime?: string
}

export interface ScadaCollectPoint {
  id?: number
  tagCode: string
  deviceName?: string
  protocol?: string
  address?: string
  dataType?: string
  unit?: string
  value?: number
  status?: string
  enabled?: boolean
  remark?: string
  createdTime?: string
  updatedTime?: string
}

export interface ScadaProtocol {
  id?: number
  name: string
  type: string
  deviceCount?: number
  status?: string
  config?: Record<string, any>
  createdTime?: string
  updatedTime?: string
}

export interface ScadaPreprocessSetting {
  rangeConversion: boolean
  deadbandFilter: boolean
  deadbandValue: number
  dataValidation: boolean
}

export interface ScadaStoragePolicy {
  id?: number
  storageType: string
  host: string
  port: number
  database: string
  username?: string
  password?: string
  realtimeRetention: number
  historicalSampling: string
  compressionLevel: string
  backupPolicy: string
  enabled?: boolean
  connectionStatus?: string
  storedData?: number
  writesPerSecond?: number
  storageEfficiency?: number
  lastWriteTime?: string
}

export interface ScadaAlarmRule {
  id?: number
  tagCode: string
  alarmName?: string
  severity: number
  enabled: boolean
  highHigh?: number
  high?: number
  low?: number
  lowLow?: number
  deadband?: number
  remark?: string
  createdTime?: string
  updatedTime?: string
}

export interface ScadaActiveAlarm {
  id: string
  tagCode: string
  alarmName: string
  level: string
  status: string
  triggerTime: string
  currentValue?: number
}

export interface ScadaAlarmHistory {
  id?: number
  tagCode: string
  alarmType: string
  severity: number
  triggerTime: string
  confirmTime?: string
  recoveryTime?: string
  handlerId?: number
  memo?: string
}

export interface ScadaTagValue {
  tagCode: string
  ts: string
  value: number
  quality?: string
}

export interface ScadaReportDef {
  id?: number
  reportName: string
  templatePath?: string
  cronExpression?: string
  exportFormat: string
  associatedTags?: string
}

const API_PREFIX = '/api/v1/scada'

export const scadaDeviceApi = {
  getByPage: (params?: any) => api.get(`${API_PREFIX}/devices/page`, { params }),
  getById: (id: number) => api.get(`${API_PREFIX}/devices/${id}`),
  create: (data: ScadaDevice) => api.post(`${API_PREFIX}/devices`, data),
  update: (id: number, data: ScadaDevice) => api.put(`${API_PREFIX}/devices/${id}`, data),
  remove: (id: number) => api.delete(`${API_PREFIX}/devices/${id}`)
}

export const scadaCollectPointApi = {
  list: (params?: any) => api.get(`${API_PREFIX}/collect-points`, { params }),
  create: (data: ScadaCollectPoint) => api.post(`${API_PREFIX}/collect-points`, data),
  update: (id: number, data: ScadaCollectPoint) => api.put(`${API_PREFIX}/collect-points/${id}`, data),
  remove: (id: number) => api.delete(`${API_PREFIX}/collect-points/${id}`)
}

export const scadaProtocolApi = {
  list: (params?: any) => api.get(`${API_PREFIX}/protocols`, { params }),
  create: (data: ScadaProtocol) => api.post(`${API_PREFIX}/protocols`, data),
  update: (id: number, data: ScadaProtocol) => api.put(`${API_PREFIX}/protocols/${id}`, data),
  updateConfig: (id: number, config: Record<string, any>) => api.put(`${API_PREFIX}/protocols/${id}/config`, config),
  toggle: (id: number, enabled: boolean) => api.patch(`${API_PREFIX}/protocols/${id}/enabled`, { enabled }),
  remove: (id: number) => api.delete(`${API_PREFIX}/protocols/${id}`)
}

export const scadaPreprocessApi = {
  get: () => api.get(`${API_PREFIX}/preprocess-settings`),
  save: (data: ScadaPreprocessSetting) => api.put(`${API_PREFIX}/preprocess-settings`, data)
}

export const scadaStorageApi = {
  get: () => api.get(`${API_PREFIX}/storage-policy`),
  save: (data: ScadaStoragePolicy) => api.put(`${API_PREFIX}/storage-policy`, data),
  testConnection: (data: ScadaStoragePolicy) => api.post(`${API_PREFIX}/storage-policy/test-connection`, data)
}

export const scadaRealtimeApi = {
  getTrend: (params: any) => api.get(`${API_PREFIX}/realtime/trend`, { params }),
  exportTrend: (params: any) => api.get(`${API_PREFIX}/realtime/trend/export`, { params, responseType: 'blob' }),
  /** 人工抄表补录（S11 仪表视觉识别配套，CONFIRM 级，quality=manual） */
  createManualReading: (data: { tagCode: string; value: number; unit?: string }) =>
    api.post(`${API_PREFIX}/realtime/manual`, data)
}

export const scadaAlarmApi = {
  listActive: (params?: any) => api.get(`${API_PREFIX}/alarms/active`, { params }),
  acknowledge: (alarmId: string) => api.post(`${API_PREFIX}/alarms/${alarmId}/ack`, {}),
  mute: (alarmId: string) => api.post(`${API_PREFIX}/alarms/${alarmId}/mute`, {}),
  clear: (alarmId: string) => api.post(`${API_PREFIX}/alarms/${alarmId}/clear`, {}),
  acknowledgeAll: () => api.post(`${API_PREFIX}/alarms/ack-all`, {}),
  clearAll: () => api.post(`${API_PREFIX}/alarms/clear-all`, {}),
  listRules: (params?: any) => api.get(`${API_PREFIX}/alarm-rules`, { params }),
  createRule: (data: ScadaAlarmRule) => api.post(`${API_PREFIX}/alarm-rules`, data),
  updateRule: (id: number, data: ScadaAlarmRule) => api.put(`${API_PREFIX}/alarm-rules/${id}`, data),
  removeRule: (id: number) => api.delete(`${API_PREFIX}/alarm-rules/${id}`),
  testTrigger: (data: any) => api.post(`${API_PREFIX}/alarms/test-trigger`, data),
  saveTriggerConfig: (data: any) => api.put(`${API_PREFIX}/alarms/trigger-config`, data),
  getTriggerConfig: () => api.get(`${API_PREFIX}/alarms/trigger-config`),
  getStats: (params?: any) => api.get(`${API_PREFIX}/alarms/stats`, { params }),
  getHistory: (params?: any) => api.get(`${API_PREFIX}/alarms/history`, { params })
}

export const scadaHistoryApi = {
  retrieve: (params: any) => api.get(`${API_PREFIX}/history`, { params }),
  export: (params: any) => api.get(`${API_PREFIX}/history/export`, { params, responseType: 'blob' })
}

export const scadaReportApi = {
  list: (params?: any) => api.get(`${API_PREFIX}/reports`, { params }),
  create: (data: ScadaReportDef) => api.post(`${API_PREFIX}/reports`, data),
  update: (id: number, data: ScadaReportDef) => api.put(`${API_PREFIX}/reports/${id}`, data),
  remove: (id: number) => api.delete(`${API_PREFIX}/reports/${id}`),
  generate: (data: any) => api.post(`${API_PREFIX}/reports/generate`, data, { responseType: 'blob' })
}

export const scadaOverviewApi = {
  stats: () => api.get(`${API_PREFIX}/overview/stats`)
}
