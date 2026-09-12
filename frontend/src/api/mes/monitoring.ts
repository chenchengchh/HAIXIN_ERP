import api, { unwrapListResponse, unwrapResponseData } from '../index'

// 虚拟数据（化妆品行业示例）
const mockProductionProgress: ProductionProgress[] = [
  {
    id: '1',
    workOrderId: 'WO-001',
    workOrderNo: 'WO-001',
    materialId: 'MAT-001',
    materialName: '粉底液',
    totalQty: 1000,
    completedQty: 500,
    progress: 50,
    status: 'in_progress',
    updateTime: '2025-01-01T12:00:00'
  },
  {
    id: '2',
    workOrderId: 'WO-002',
    workOrderNo: 'WO-002',
    materialId: 'MAT-002',
    materialName: '口红',
    totalQty: 2000,
    completedQty: 2000,
    progress: 100,
    status: 'completed',
    updateTime: '2025-01-02T18:00:00'
  }
]

const mockEquipmentStatus: EquipmentStatus[] = [
  {
    id: '1',
    equipmentId: 'EQ-001',
    equipmentName: '混合机',
    equipmentType: '搅拌机',
    status: 'running',
    uptime: 3600,
    downtime: 0,
    efficiency: 100,
    temperature: 30,
    pressure: 0.3,
    speed: 1500,
    updateTime: '2025-01-01T12:00:00'
  },
  {
    id: '2',
    equipmentId: 'EQ-002',
    equipmentName: '灌装机',
    equipmentType: '灌装设备',
    status: 'idle',
    uptime: 2700,
    downtime: 900,
    efficiency: 75,
    temperature: 25,
    pressure: 0.5,
    speed: 0,
    updateTime: '2025-01-01T17:00:00'
  },
  {
    id: '3',
    equipmentId: 'EQ-003',
    equipmentName: '包装机',
    equipmentType: '包装设备',
    status: 'maintenance',
    uptime: 1800,
    downtime: 1800,
    efficiency: 50,
    temperature: 22,
    pressure: 0,
    speed: 0,
    updateTime: '2025-01-01T10:00:00'
  }
]

const mockProcessParams: ProcessParam[] = [
  {
    id: '1',
    workstationId: 'WS-001',
    workstationName: '混合车间',
    parameterName: '温度',
    parameterValue: 50,
    unit: '°C',
    upperLimit: 60,
    lowerLimit: 40,
    status: 'normal',
    timestamp: '2025-01-01T12:00:00'
  },
  {
    id: '2',
    workstationId: 'WS-001',
    workstationName: '混合车间',
    parameterName: '搅拌速度',
    parameterValue: 1500,
    unit: 'rpm',
    upperLimit: 2000,
    lowerLimit: 1000,
    status: 'normal',
    timestamp: '2025-01-01T12:00:00'
  },
  {
    id: '3',
    workstationId: 'WS-002',
    workstationName: '灌装车间',
    parameterName: '压力',
    parameterValue: 0.8,
    unit: 'MPa',
    upperLimit: 0.6,
    lowerLimit: 0.3,
    status: 'alarm',
    timestamp: '2025-01-01T17:00:00'
  }
]

/**
 * 过程监控模块API
 */

// 生产进度数据类型定义
export interface ProductionProgress {
  id: string
  workOrderId: string
  workOrderNo: string
  materialId: string
  materialName: string
  totalQty: number
  completedQty: number
  progress: number
  status: 'in_progress' | 'completed'
  updateTime: string
}

// 设备状态数据类型定义
export interface EquipmentStatus {
  id: string
  equipmentId: string
  equipmentName: string
  equipmentType: string
  status: 'running' | 'idle' | 'down' | 'maintenance'
  uptime: number
  downtime: number
  efficiency: number
  temperature?: number
  pressure?: number
  speed?: number
  updateTime: string
}

// 工艺参数数据类型定义
export interface ProcessParam {
  id: string
  workstationId: string
  workstationName: string
  parameterName: string
  parameterValue: number
  unit: string
  upperLimit: number
  lowerLimit: number
  status: 'normal' | 'warning' | 'alarm'
  timestamp: string
}

const extractList = <T>(response: any): T[] => {
  return unwrapListResponse<T>(response)
}

const extractItem = <T>(response: any): T => {
  return unwrapResponseData<T>(response) as T
}

/**
 * 获取生产进度列表
 * @returns 生产进度列表
 */
export const getProductionProgress = async () => {
  try {
    const response = await api.get<any>('/api/v1/mes/production-progress')
    return { data: extractList<ProductionProgress>(response) }
  } catch (error) {
    // 后端无法连接时返回虚拟数据
    console.log('后端无法连接，返回虚拟数据（生产进度）')
    return { data: mockProductionProgress }
  }
}

/**
 * 获取设备状态列表
 * @returns 设备状态列表
 */
export const getEquipmentStatus = async () => {
  try {
    const response = await api.get<any>('/api/v1/mes/equipment-status')
    return { data: extractList<EquipmentStatus>(response) }
  } catch (error) {
    // 后端无法连接时返回虚拟数据
    console.log('后端无法连接，返回虚拟数据（设备状态）')
    return { data: mockEquipmentStatus }
  }
}

/**
 * 获取工艺参数列表
 * @returns 工艺参数列表
 */
export const getProcessParams = async () => {
  try {
    const response = await api.get<any>('/api/v1/mes/process-params')
    return { data: extractList<ProcessParam>(response) }
  } catch (error) {
    // 后端无法连接时返回虚拟数据
    console.log('后端无法连接，返回虚拟数据（工艺参数）')
    return { data: mockProcessParams }
  }
}

/**
 * 获取设备详情
 * @param id 设备ID
 * @returns 设备详情
 */
export const getEquipmentDetail = async (id: string) => {
  try {
    const response = await api.get<any>(`/api/v1/mes/equipment/${id}`)
    return { data: extractItem<EquipmentStatus>(response) }
  } catch (error) {
    // 后端无法连接时返回虚拟数据
    console.log('后端无法连接，返回虚拟数据（设备详情）')
    const equipment = mockEquipmentStatus.find(item => item.equipmentId === id)
    return { data: equipment || mockEquipmentStatus[0] }
  }
}

/**
 * 获取实时工艺参数
 * @param workstationId 工站ID
 * @returns 实时工艺参数
 */
export const getRealTimeProcessParams = async (workstationId: string) => {
  try {
    const response = await api.get<any>(`/api/v1/mes/workstations/${workstationId}/process-params/realtime`)
    return { data: extractList<ProcessParam>(response) }
  } catch (error) {
    // 后端无法连接时返回虚拟数据
    console.log('后端无法连接，返回虚拟数据（实时工艺参数）')
    return { data: mockProcessParams.filter(item => item.workstationId === workstationId) }
  }
}

export const getProcessParamAdvice = (paramId: string) => {
  return api.get<any>(`/api/v1/mes/process-params/${paramId}/advice`).then(res => ({ data: extractItem<any>(res) }))
}

export const getEquipmentFaultHistory = (equipmentId: string) => {
  return api.get<any>(`/api/v1/mes/equipment/${equipmentId}/faults`).then(res => ({ data: extractList<any>(res) }))
}
