import api, { unwrapListResponse, unwrapResponseData } from '../index'

// 虚拟数据（化妆品行业示例）
const mockWipLocations: WipLocation[] = [
  {
    id: '1',
    snCode: 'SN-00001',
    workOrderId: 'WO-001',
    workOrderNo: 'WO-001',
    currentStepId: 'STEP-002',
    currentStepName: '灌装',
    currentStationId: 'WS-002',
    currentStationName: '灌装车间',
    status: 'processing',
    updateTime: '2025-01-01T13:00:00',
    locationHistory: [
      {
        stationId: 'WS-001',
        stationName: '混合车间',
        stepId: 'STEP-001',
        stepName: '原料混合',
        startTime: '2025-01-01T08:00:00',
        endTime: '2025-01-01T12:00:00'
      }
    ]
  },
  {
    id: '2',
    snCode: 'SN-00002',
    workOrderId: 'WO-001',
    workOrderNo: 'WO-001',
    currentStepId: 'STEP-003',
    currentStepName: '包装',
    currentStationId: 'WS-003',
    currentStationName: '包装车间',
    status: 'completed',
    updateTime: '2025-01-01T18:00:00',
    locationHistory: [
      {
        stationId: 'WS-001',
        stationName: '混合车间',
        stepId: 'STEP-001',
        stepName: '原料混合',
        startTime: '2025-01-01T08:00:00',
        endTime: '2025-01-01T12:00:00'
      },
      {
        stationId: 'WS-002',
        stationName: '灌装车间',
        stepId: 'STEP-002',
        stepName: '灌装',
        startTime: '2025-01-01T13:00:00',
        endTime: '2025-01-01T17:00:00'
      }
    ]
  }
]

const mockBatches: Batch[] = [
  {
    id: '1',
    batchNo: 'BATCH-001',
    workOrderId: 'WO-001',
    workOrderNo: 'WO-001',
    materialId: 'MAT-001',
    materialName: '粉底液',
    qty: 1000,
    status: 'in_process',
    createTime: '2025-01-01T00:00:00',
    updateTime: '2025-01-01T12:00:00'
  },
  {
    id: '2',
    batchNo: 'BATCH-002',
    workOrderId: 'WO-002',
    workOrderNo: 'WO-002',
    materialId: 'MAT-002',
    materialName: '口红',
    qty: 2000,
    status: 'completed',
    createTime: '2025-01-02T00:00:00',
    updateTime: '2025-01-03T18:00:00'
  }
]

const mockFlowRecords: FlowRecord[] = [
  {
    id: '1',
    snCode: 'SN-00001',
    batchNo: 'BATCH-001',
    fromStepId: 'STEP-001',
    fromStepName: '原料混合',
    toStepId: 'STEP-002',
    toStepName: '灌装',
    fromStationId: 'WS-001',
    fromStationName: '混合车间',
    toStationId: 'WS-002',
    toStationName: '灌装车间',
    operatorId: 'OP-001',
    operatorName: '张三',
    timestamp: '2025-01-01T12:00:00',
    status: 'success',
    remarks: '流转成功'
  },
  {
    id: '2',
    snCode: 'SN-00002',
    batchNo: 'BATCH-001',
    fromStepId: 'STEP-002',
    fromStepName: '灌装',
    toStepId: 'STEP-003',
    toStepName: '包装',
    fromStationId: 'WS-002',
    fromStationName: '灌装车间',
    toStationId: 'WS-003',
    toStationName: '包装车间',
    operatorId: 'OP-002',
    operatorName: '李四',
    timestamp: '2025-01-01T17:00:00',
    status: 'success',
    remarks: '流转成功'
  }
]

/**
 * 在制品管理模块API
 */

// 在制品位置跟踪数据类型定义
export interface WipLocation {
  id: string
  snCode: string
  workOrderId: string
  workOrderNo: string
  currentStepId: string
  currentStepName: string
  currentStationId: string
  currentStationName: string
  status: 'queuing' | 'processing' | 'completed' | 'scrapped'
  updateTime: string
  locationHistory: Array<{
    stationId: string
    stationName: string
    stepId: string
    stepName: string
    startTime: string
    endTime: string
  }>
}

// 批次管理数据类型定义
export interface Batch {
  id: string
  batchNo: string
  workOrderId: string
  workOrderNo: string
  materialId: string
  materialName: string
  qty: number
  status: 'in_process' | 'completed' | 'scrapped'
  createTime: string
  updateTime: string
}

// 流转记录数据类型定义
export interface FlowRecord {
  id: string
  snCode: string
  batchNo?: string
  fromStepId: string
  fromStepName: string
  toStepId: string
  toStepName: string
  fromStationId: string
  fromStationName: string
  toStationId: string
  toStationName: string
  operatorId: string
  operatorName: string
  timestamp: string
  status: 'success' | 'failed'
  remarks?: string
}

const extractList = <T>(response: any): T[] => {
  return unwrapListResponse<T>(response)
}

const extractItem = <T>(response: any): T => {
  return unwrapResponseData<T>(response) as T
}

/**
 * 获取在制品位置列表
 * @returns 在制品位置列表
 */
export const getWipLocations = async () => {
  try {
    const response = await api.get<any>('/api/v1/mes/wip-locations')
    return { data: extractList<WipLocation>(response) }
  } catch (error) {
    // 后端无法连接时返回虚拟数据
    console.log('后端无法连接，返回虚拟数据（在制品位置）')
    return { data: mockWipLocations }
  }
}

/**
 * 获取在制品详情
 * @param snCode 产品序列号
 * @returns 在制品详情
 */
export const getWipDetail = async (snCode: string) => {
  try {
    const response = await api.get<any>(`/api/v1/mes/wip/${snCode}`)
    return { data: extractItem<WipLocation>(response) }
  } catch (error) {
    // 后端无法连接时返回虚拟数据
    console.log('后端无法连接，返回虚拟数据（在制品详情）')
    const wip = mockWipLocations.find(item => item.snCode === snCode)
    return { data: wip || mockWipLocations[0] }
  }
}

/**
 * 更新在制品位置
 * @param data 在制品位置数据
 * @returns 更新结果
 */
export const updateWipLocation = async (data: { snCode: string; stationId: string; stepId: string; status: string }) => {
  const response = await api.put<any>('/api/v1/mes/wip-locations', data)
  return { data: extractItem<WipLocation>(response) }
}

/**
 * 获取批次列表
 * @returns 批次列表
 */
export const getBatches = async () => {
  try {
    const response = await api.get<any>('/api/v1/mes/batches')
    return { data: extractList<Batch>(response) }
  } catch (error) {
    // 后端无法连接时返回虚拟数据
    console.log('后端无法连接，返回虚拟数据（批次）')
    return { data: mockBatches }
  }
}

/**
 * 创建批次
 * @param batch 批次数据
 * @returns 创建结果
 */
export const createBatch = (batch: Partial<Batch>) => {
  return api.post<any>('/api/v1/mes/batches', batch).then(res => ({ data: extractItem<Batch>(res) }))
}

/**
 * 获取批次详情
 * @param batchNo 批次号
 * @returns 批次详情
 */
export const getBatchDetail = async (batchNo: string) => {
  try {
    const response = await api.get<any>(`/api/v1/mes/batches/${batchNo}`)
    return { data: extractItem<Batch>(response) }
  } catch (error) {
    // 后端无法连接时返回虚拟数据
    console.log('后端无法连接，返回虚拟数据（批次详情）')
    const batch = mockBatches.find(item => item.batchNo === batchNo)
    return { data: batch || mockBatches[0] }
  }
}

/**
 * 获取流转记录列表
 * @returns 流转记录列表
 */
export const getFlowRecords = async () => {
  try {
    const response = await api.get<any>('/api/v1/mes/flow-records')
    return { data: extractList<FlowRecord>(response) }
  } catch (error) {
    // 后端无法连接时返回虚拟数据
    console.log('后端无法连接，返回虚拟数据（流转记录）')
    return { data: mockFlowRecords }
  }
}

/**
 * 获取产品流转历史
 * @param snCode 产品序列号
 * @returns 流转历史
 */
export const getProductFlowHistory = async (snCode: string) => {
  try {
    const response = await api.get<any>(`/api/v1/mes/flow-records/product/${snCode}`)
    return { data: extractList<FlowRecord>(response) }
  } catch (error) {
    // 后端无法连接时返回虚拟数据
    console.log('后端无法连接，返回虚拟数据（产品流转历史）')
    return { data: mockFlowRecords.filter(item => item.snCode === snCode) }
  }
}
