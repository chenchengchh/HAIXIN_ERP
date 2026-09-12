import api, { unwrapListResponse, unwrapResponseData } from '../index'

// 虚拟数据（化妆品行业示例）
const mockProductionReports: ProductionReport[] = [
  {
    id: '1',
    workOrderId: 'WO-001',
    workOrderNo: 'WO-001',
    stepId: 'STEP-001',
    stepName: '原料混合',
    workstationId: 'WS-001',
    workstationName: '混合车间',
    operatorId: 'OP-001',
    operatorName: '张三',
    startTime: '2025-01-01T08:00:00',
    endTime: '2025-01-01T12:00:00',
    goodQty: 500,
    scrapQty: 10,
    reworkQty: 5,
    workingHours: 4,
    machineHours: 4,
    status: 'approved',
    createTime: '2025-01-01T12:00:00',
    updateTime: '2025-01-01T13:00:00'
  },
  {
    id: '2',
    workOrderId: 'WO-001',
    workOrderNo: 'WO-001',
    stepId: 'STEP-002',
    stepName: '灌装',
    workstationId: 'WS-002',
    workstationName: '灌装车间',
    operatorId: 'OP-002',
    operatorName: '李四',
    startTime: '2025-01-01T13:00:00',
    endTime: '2025-01-01T17:00:00',
    goodQty: 485,
    scrapQty: 8,
    reworkQty: 2,
    workingHours: 4,
    machineHours: 4,
    status: 'verified',
    createTime: '2025-01-01T17:00:00',
    updateTime: '2025-01-01T18:00:00'
  }
]

const mockProductionReportStats = {
  totalReports: 2,
  totalGoodQty: 985,
  totalScrapQty: 18,
  totalReworkQty: 7,
  totalWorkingHours: 8,
  totalMachineHours: 8,
  yieldRate: 97.7,
  efficiency: 95
}

/**
 * 生产报工模块API
 */

// 生产报工数据类型定义
export interface ProductionReport {
  id: string
  workOrderId: string
  workOrderNo: string
  stepId: string
  stepName: string
  workstationId: string
  workstationName: string
  operatorId: string
  operatorName: string
  startTime: string
  endTime: string
  goodQty: number
  scrapQty: number
  reworkQty: number
  workingHours: number
  machineHours: number
  status: 'reported' | 'verified' | 'approved'
  createTime: string
  updateTime: string
}

const extractList = <T>(response: any): T[] => {
  return unwrapListResponse<T>(response)
}

const extractItem = <T>(response: any): T => {
  return unwrapResponseData<T>(response) as T
}

/**
 * 获取生产报工列表
 * @returns 生产报工列表
 */
export const getProductionReports = async () => {
  try {
    const response = await api.get<any>('/api/v1/mes/reporting/list', {
      params: {
        page: 1,
        size: 100
      }
    })
    const rawData = extractList<any>(response)

    const formattedData: ProductionReport[] = rawData.map((item: any) => ({
      id: String(item?.id ?? ''),
      workOrderId: '',
      workOrderNo: String(item?.workOrderNo ?? ''),
      stepId: '',
      stepName: String(item?.stepName ?? ''),
      workstationId: '',
      workstationName: String(item?.workstationName ?? ''),
      operatorId: '',
      operatorName: String(item?.operatorName ?? ''),
      startTime: String(item?.startTime ?? ''),
      endTime: String(item?.endTime ?? ''),
      goodQty: Number(item?.goodQty ?? 0),
      scrapQty: Number(item?.scrapQty ?? 0),
      reworkQty: Number(item?.reworkQty ?? 0),
      workingHours: Number(item?.workingHours ?? 0),
      machineHours: Number(item?.machineHours ?? 0),
      status: (item?.status || 'reported') as any,
      createTime: String(item?.createTime ?? ''),
      updateTime: String(item?.updateTime ?? '')
    }))
    return { data: formattedData }
  } catch (error) {
    // 后端连接失败时抛出错误，由上层统一处理
    console.error('获取生产报工列表失败:', error)
    throw error
  }
}

/**
 * 创建生产报工
 * @param report 报工数据
 * @returns 创建结果
 */
export const createProductionReport = async (report: Partial<ProductionReport>) => {
  try {
    const payload = {
      reportNo: `REP-${Date.now()}`,
      workOrderNo: report.workOrderNo,
      stepName: report.stepName,
      workstationName: report.workstationName,
      operatorName: report.operatorName,
      startTime: report.startTime,
      endTime: report.endTime,
      goodQty: report.goodQty ?? 0,
      scrapQty: report.scrapQty ?? 0,
      reworkQty: report.reworkQty ?? 0,
      workingHours: report.workingHours ?? 0,
      machineHours: report.machineHours ?? 0,
      status: 'reported',
      remark: ''
    }
    const response = await api.post<any>('/api/v1/mes/reporting', payload)
    const created = extractItem<any>(response)
    return getProductionReportDetail(String(created?.id ?? ''))
  } catch (error) {
    // 后端连接失败时抛出错误，由上层统一处理
    console.error('创建生产报工失败:', error)
    throw error
  }
}

/**
 * 获取报工详情
 * @param id 报工ID
 * @returns 报工详情
 */
export const getProductionReportDetail = async (id: string) => {
  try {
    const response = await api.get<any>(`/api/v1/mes/reporting/${id}`)
    const item = extractItem<any>(response)
    const formattedData: ProductionReport = {
      id: String(item?.id ?? ''),
      workOrderId: '',
      workOrderNo: String(item?.workOrderNo ?? ''),
      stepId: '',
      stepName: String(item?.stepName ?? ''),
      workstationId: '',
      workstationName: String(item?.workstationName ?? ''),
      operatorId: '',
      operatorName: String(item?.operatorName ?? ''),
      startTime: String(item?.startTime ?? ''),
      endTime: String(item?.endTime ?? ''),
      goodQty: Number(item?.goodQty ?? 0),
      scrapQty: Number(item?.scrapQty ?? 0),
      reworkQty: Number(item?.reworkQty ?? 0),
      workingHours: Number(item?.workingHours ?? 0),
      machineHours: Number(item?.machineHours ?? 0),
      status: (item?.status || 'reported') as any,
      createTime: String(item?.createTime ?? ''),
      updateTime: String(item?.updateTime ?? '')
    }
    return { data: formattedData }
  } catch (error) {
    // 后端无法连接时返回虚拟数据
    console.log('后端无法连接，返回虚拟数据（报工详情）')
    const report = mockProductionReports.find(item => item.id === id)
    return { data: report || mockProductionReports[0] }
  }
}

/**
 * 更新报工状态
 * @param id 报工ID
 * @param status 新状态
 * @returns 更新结果
 */
export const updateProductionReportStatus = async (id: string, status: string) => {
  try {
    await api.put(`/api/v1/mes/reporting/${id}/status`, { status })
    return getProductionReportDetail(id)
  } catch (error) {
    // 后端无法连接时返回虚拟数据
    console.log('后端无法连接，返回虚拟数据（更新报工状态）')
    const report = mockProductionReports.find(item => item.id === id) || mockProductionReports[0]
    const updatedReport = { ...report, status: status as any, updateTime: new Date().toISOString() }
    return { data: updatedReport }
  }
}

/**
 * 获取报工统计数据
 * @param params 查询参数
 * @returns 报工统计数据
 */
export const getProductionReportStats = async (params?: any) => {
  try {
    const listRes = await getProductionReports()
    const list = listRes.data || []
    const totalReports = list.length
    const totalGoodQty = list.reduce((sum, r) => sum + (r.goodQty || 0), 0)
    const totalScrapQty = list.reduce((sum, r) => sum + (r.scrapQty || 0), 0)
    const totalReworkQty = list.reduce((sum, r) => sum + (r.reworkQty || 0), 0)
    const totalWorkingHours = list.reduce((sum, r) => sum + (r.workingHours || 0), 0)
    const totalMachineHours = list.reduce((sum, r) => sum + (r.machineHours || 0), 0)
    const inputQty = totalGoodQty + totalScrapQty + totalReworkQty
    const yieldRate = inputQty > 0 ? Number(((totalGoodQty / inputQty) * 100).toFixed(2)) : 0
    const efficiency = totalWorkingHours > 0 ? Number(((totalGoodQty / totalWorkingHours) * 100).toFixed(2)) : 0
    return { data: { totalReports, totalGoodQty, totalScrapQty, totalReworkQty, totalWorkingHours, totalMachineHours, yieldRate, efficiency } }
  } catch (error) {
    // 后端无法连接时返回虚拟数据
    console.log('后端无法连接，返回虚拟数据（报工统计）')
    return { data: mockProductionReportStats }
  }
}
