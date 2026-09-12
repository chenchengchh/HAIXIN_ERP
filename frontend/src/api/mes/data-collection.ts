import api, { unwrapListResponse, unwrapResponseData } from '../index'

// 虚拟数据（化妆品行业示例）
const mockManualReportings: ManualReporting[] = [
  {
    id: '1',
    operatorId: 'OP-001',
    operatorName: '张三',
    workOrderId: 'WO-001',
    workOrderNo: 'WO-001',
    stepId: 'STEP-001',
    stepName: '原料混合',
    workstationId: 'WS-001',
    workstationName: '混合车间',
    startTime: '2025-01-01T08:00:00',
    endTime: '2025-01-01T12:00:00',
    goodQty: 500,
    scrapQty: 10,
    reworkQty: 5,
    workingHours: 4,
    status: 'verified',
    createTime: '2025-01-01T12:00:00',
    updateTime: '2025-01-01T13:00:00'
  },
  {
    id: '2',
    operatorId: 'OP-002',
    operatorName: '李四',
    workOrderId: 'WO-001',
    workOrderNo: 'WO-001',
    stepId: 'STEP-002',
    stepName: '灌装',
    workstationId: 'WS-002',
    workstationName: '灌装车间',
    startTime: '2025-01-01T13:00:00',
    endTime: '2025-01-01T17:00:00',
    goodQty: 485,
    scrapQty: 8,
    reworkQty: 2,
    workingHours: 4,
    status: 'submitted',
    createTime: '2025-01-01T17:00:00',
    updateTime: '2025-01-01T17:00:00'
  }
]

const mockEquipmentData: EquipmentData[] = [
  {
    id: '1',
    equipmentId: 'EQ-001',
    equipmentName: '混合机',
    equipmentType: '搅拌机',
    parameterName: '转速',
    parameterValue: 1500,
    unit: 'rpm',
    timestamp: '2025-01-01T12:00:00',
    status: 'normal'
  },
  {
    id: '2',
    equipmentId: 'EQ-002',
    equipmentName: '灌装机',
    equipmentType: '灌装设备',
    parameterName: '压力',
    parameterValue: 0.5,
    unit: 'MPa',
    timestamp: '2025-01-01T17:00:00',
    status: 'warning'
  }
]

const mockQualityInspections: QualityInspection[] = [
  {
    id: '1',
    inspectionId: 'INS-001',
    inspectionName: '半成品检验',
    workOrderId: 'WO-001',
    workOrderNo: 'WO-001',
    snCode: 'SN-00001',
    stepId: 'STEP-001',
    stepName: '原料混合',
    inspectorId: 'IN-001',
    inspectorName: '王五',
    inspectionTime: '2025-01-01T12:00:00',
    result: 'pass',
    inspectionItems: [
      {
        itemId: 'ITEM-001',
        itemName: '粘度',
        standardValue: '5000-6000',
        actualValue: '5500',
        result: 'pass'
      },
      {
        itemId: 'ITEM-002',
        itemName: 'pH值',
        standardValue: '6.5-7.5',
        actualValue: '7.0',
        result: 'pass'
      }
    ],
    status: 'completed',
    createTime: '2025-01-01T12:00:00'
  },
  {
    id: '2',
    inspectionId: 'INS-002',
    inspectionName: '成品检验',
    workOrderId: 'WO-001',
    workOrderNo: 'WO-001',
    snCode: 'SN-00002',
    stepId: 'STEP-003',
    stepName: '包装',
    inspectorId: 'IN-001',
    inspectorName: '王五',
    inspectionTime: '2025-01-01T18:00:00',
    result: 'fail',
    defectType: '外观缺陷',
    defectDescription: '包装有划痕',
    inspectionItems: [
      {
        itemId: 'ITEM-003',
        itemName: '外观',
        standardValue: '无划痕',
        actualValue: '有划痕',
        result: 'fail'
      },
      {
        itemId: 'ITEM-004',
        itemName: '重量',
        standardValue: '30g±0.5g',
        actualValue: '30.2g',
        result: 'pass'
      }
    ],
    status: 'completed',
    createTime: '2025-01-01T18:00:00'
  }
]

/**
 * 数据采集模块API
 */

// 人工报工采集数据类型定义
export interface ManualReporting {
  id: string
  operatorId: string
  operatorName: string
  workOrderId: string
  workOrderNo: string
  stepId: string
  stepName: string
  workstationId: string
  workstationName: string
  startTime: string
  endTime: string
  goodQty: number
  scrapQty: number
  reworkQty: number
  workingHours: number
  status: 'submitted' | 'verified' | 'approved'
  createTime: string
  updateTime: string
  remarks?: string
}

// 设备数据自动采集数据类型定义
export interface EquipmentData {
  id: string
  equipmentId: string
  equipmentName: string
  equipmentType: string
  parameterName: string
  parameterValue: number
  unit: string
  timestamp: string
  status: 'normal' | 'warning' | 'alarm'
}

// 质量检验数据采集数据类型定义
export interface QualityInspection {
  id: string
  inspectionId: string
  inspectionName: string
  workOrderId: string
  workOrderNo: string
  snCode: string
  stepId: string
  stepName: string
  inspectorId: string
  inspectorName: string
  inspectionTime: string
  result: 'pass' | 'fail'
  defectType?: string
  defectDescription?: string
  inspectionItems: Array<{
    itemId: string
    itemName: string
    standardValue: string
    actualValue: string
    result: 'pass' | 'fail'
  }>
  status: 'completed' | 'pending' | 'rejected'
  createTime: string
}

const extractList = <T>(response: any): T[] => {
  return unwrapListResponse<T>(response)
}

const extractItem = <T>(response: any): T => {
  return unwrapResponseData<T>(response) as T
}

/**
 * 获取人工报工采集列表
 * @returns 人工报工采集列表
 */
export const getManualReportings = async () => {
  try {
    const response = await api.get<any>('/api/v1/mes/data-collection/manual')
    return { data: extractList<ManualReporting>(response) }
  } catch (error) {
    // 后端无法连接时返回虚拟数据
    console.log('后端无法连接，返回虚拟数据（人工报工）')
    return { data: mockManualReportings }
  }
}

/**
 * 创建人工报工采集
 * @param data 报工数据
 * @returns 创建结果
 */
export const createManualReporting = (data: Partial<ManualReporting>) => {
  return api.post<any>('/api/v1/mes/data-collection/manual', data).then(res => ({ data: extractItem<ManualReporting>(res) }))
}

export const updateManualReportingStatus = (id: string, status: string) => {
  return api.put<any>(`/api/v1/mes/data-collection/manual/${id}/status`, { status }).then(res => ({ data: extractItem<ManualReporting>(res) }))
}

/**
 * 获取设备数据自动采集列表
 * @returns 设备数据自动采集列表
 */
export const getEquipmentData = async () => {
  try {
    const response = await api.get<any>('/api/v1/mes/data-collection/equipment')
    return { data: extractList<EquipmentData>(response) }
  } catch (error) {
    // 后端无法连接时返回虚拟数据
    console.log('后端无法连接，返回虚拟数据（设备数据）')
    return { data: mockEquipmentData }
  }
}

/**
 * 获取实时设备数据
 * @param equipmentId 设备ID
 * @returns 实时设备数据
 */
export const getRealTimeEquipmentData = async (equipmentId: string) => {
  try {
    const response = await api.get<any>(`/api/v1/mes/data-collection/equipment/${equipmentId}/realtime`)
    return { data: extractList<EquipmentData>(response) }
  } catch (error) {
    // 后端无法连接时返回虚拟数据
    console.log('后端无法连接，返回虚拟数据（实时设备数据）')
    return { data: mockEquipmentData.filter(item => item.equipmentId === equipmentId) }
  }
}

/**
 * 获取质量检验数据采集列表
 * @returns 质量检验数据采集列表
 */
export const getQualityInspections = async () => {
  try {
    const response = await api.get<any>('/api/v1/mes/data-collection/quality')
    return { data: extractList<QualityInspection>(response) }
  } catch (error) {
    // 后端无法连接时返回虚拟数据
    console.log('后端无法连接，返回虚拟数据（质量检验）')
    return { data: mockQualityInspections }
  }
}

/**
 * 创建质量检验数据采集
 * @param data 检验数据
 * @returns 创建结果
 */
export const createQualityInspection = (data: Partial<QualityInspection>) => {
  return api.post<any>('/api/v1/mes/data-collection/quality', data).then(res => ({ data: extractItem<QualityInspection>(res) }))
}

/**
 * 获取质量检验详情
 * @param id 检验ID
 * @returns 检验详情
 */
export const getQualityInspectionDetail = (id: string) => {
  return api.get<any>(`/api/v1/mes/data-collection/quality/${id}`).then(res => ({ data: extractItem<QualityInspection>(res) }))
}

/**
 * 更新质量检验状态
 * @param id 检验ID
 * @param status 新状态
 * @returns 更新结果
 */
export const updateQualityInspectionStatus = (id: string, status: string) => {
  return api.put<any>(`/api/v1/mes/data-collection/quality/${id}/status`, { status }).then(res => ({ data: extractItem<QualityInspection>(res) }))
}
