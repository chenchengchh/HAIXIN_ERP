import api, { unwrapListResponse, unwrapResponseData } from '../index'

// 类型定义
export interface Order {
  id: string
  orderNo: string
  erpOrderNo: string
  materialId: string
  materialName: string
  qty: number
  priority: number
  planStartTime: string
  planEndTime: string
  status: 'pending' | 'processing' | 'completed' | 'closed'
  createTime: string
  updateTime: string
}

export interface WorkOrder {
  id: string
  workOrderNo: string
  orderId: string
  orderNo: string
  materialId: string
  materialName: string
  qty: number
  status: 'created' | 'released' | 'in_process' | 'finished' | 'closed'
  createTime: string
  updateTime: string
}

// 虚拟数据（化妆品行业示例）
const mockOrders: Order[] = [
  {
    id: '1',
    orderNo: 'MES-001',
    erpOrderNo: 'ERP-001',
    materialId: 'MAT-001',
    materialName: '粉底液',
    qty: 1000,
    priority: 1,
    planStartTime: '2025-01-01T08:00:00',
    planEndTime: '2025-01-02T18:00:00',
    status: 'pending',
    createTime: '2025-01-01T00:00:00',
    updateTime: '2025-01-01T00:00:00'
  },
  {
    id: '2',
    orderNo: 'MES-002',
    erpOrderNo: 'ERP-002',
    materialId: 'MAT-002',
    materialName: '口红',
    qty: 2000,
    priority: 2,
    planStartTime: '2025-01-03T08:00:00',
    planEndTime: '2025-01-04T18:00:00',
    status: 'processing',
    createTime: '2025-01-02T00:00:00',
    updateTime: '2025-01-02T12:00:00'
  }
]

const mockWorkOrders: WorkOrder[] = [
  {
    id: '1',
    workOrderNo: 'WO-001',
    orderId: '1',
    orderNo: 'MES-001',
    materialId: 'MAT-001',
    materialName: '粉底液',
    qty: 500,
    status: 'released',
    createTime: '2025-01-01T01:00:00',
    updateTime: '2025-01-01T01:00:00'
  },
  {
    id: '2',
    workOrderNo: 'WO-002',
    orderId: '1',
    orderNo: 'MES-001',
    materialId: 'MAT-001',
    materialName: '粉底液',
    qty: 500,
    status: 'created',
    createTime: '2025-01-01T01:00:00',
    updateTime: '2025-01-01T01:00:00'
  }
]

const mockProcessAssignments: ProcessAssignment[] = [
  {
    id: '1',
    workOrderId: '1',
    workOrderNo: 'WO-001',
    stepId: 'STEP-001',
    stepName: '原料混合',
    workstationId: 'WS-001',
    workstationName: '混合车间',
    operatorId: 'OP-001',
    operatorName: '张三',
    startTime: '2025-01-01T08:00:00',
    endTime: '2025-01-01T12:00:00',
    status: 'completed',
    createTime: '2025-01-01T00:00:00',
    updateTime: '2025-01-01T12:00:00'
  },
  {
    id: '2',
    workOrderId: '1',
    workOrderNo: 'WO-001',
    stepId: 'STEP-002',
    stepName: '灌装',
    workstationId: 'WS-002',
    workstationName: '灌装车间',
    operatorId: 'OP-002',
    operatorName: '李四',
    startTime: '2025-01-01T13:00:00',
    status: 'in_progress',
    createTime: '2025-01-01T12:00:00',
    updateTime: '2025-01-01T13:00:00'
  }
]

/**
 * 生产执行模块API
 */

// 订单数据类型定义
export interface Order {
  id: string
  orderNo: string
  erpOrderNo: string
  materialId: string
  materialName: string
  qty: number
  priority: number
  planStartTime: string
  planEndTime: string
  status: 'pending' | 'processing' | 'completed' | 'closed'
  createTime: string
  updateTime: string
}

// 工单数据类型定义
export interface WorkOrder {
  id: string
  workOrderNo: string
  /** ERP 生产单号（跨服务业务键，B4 闭环） */
  erpProductionNo?: string
  orderId: string
  orderNo: string
  materialId: string
  materialName: string
  qty: number
  status: 'created' | 'released' | 'in_process' | 'finished' | 'closed'
  createTime: string
  updateTime: string
}

// 工序派工数据类型定义
export interface ProcessAssignment {
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
  endTime?: string
  status: 'assigned' | 'in_progress' | 'completed'
  createTime: string
  updateTime: string
}

const extractRecords = (response: any) => {
  return unwrapListResponse<any>(response)
}

const extractPayload = (response: any) => {
  return unwrapResponseData<any>(response)
}

/**
 * 获取生产执行列表
 * @returns 生产执行列表
 */
export const getOrders = async () => {
  try {
    const response = await api.get<any>('/api/v1/mes/production/list', {
      params: {
        page: 1,
        size: 100 // 获取较多数据用于显示
      }
    })
    // 转换后端数据格式为前端所需格式
    const rawData = extractRecords(response)
    const formattedData = rawData.map((item: any) => ({
      id: item.id.toString(),
      orderNo: item.productionOrderNo,
      erpOrderNo: 'ERP-' + item.productionOrderNo,
      materialId: item.productCode,
      materialName: item.productName,
      qty: item.planQuantity,
      priority: 1,
      planStartTime: item.startTime?.toString() || '',
      planEndTime: item.endTime?.toString() || '',
      status: mapExecutionStatus(item.executionStatus),
      createTime: item.createdTime?.toString() || '',
      updateTime: item.updatedTime?.toString() || ''
    }))
    return { data: formattedData }
  } catch (error) {
    // 后端无法连接时返回虚拟数据
    console.log('后端无法连接，返回虚拟数据（订单）')
    return { data: mockOrders }
  }
}

/**
 * 将后端执行状态转换为前端状态
 * @param status 后端执行状态
 * @returns 前端状态
 */
const mapExecutionStatus = (status: number): 'pending' | 'processing' | 'completed' | 'closed' => {
  const statusMap: Record<number, 'pending' | 'processing' | 'completed' | 'closed'> = {
    1: 'pending',      // 待执行
    2: 'processing',   // 执行中
    3: 'completed',    // 已完成
    4: 'processing',   // 已暂停 -> 处理中
    5: 'closed'        // 已取消 -> 已关闭
  }
  return statusMap[status] || 'pending'
}

/**
 * 获取订单详情
 * @param id 订单ID
 * @returns 订单详情
 */
export const getOrderDetail = async (id: string) => {
  try {
    const response = await api.get<any>(`/api/v1/mes/production/${id}`)
    const payload = extractPayload(response)
    // 转换后端数据格式为前端所需格式
    const formattedData = {
      id: payload.id.toString(),
      orderNo: payload.productionOrderNo,
      erpOrderNo: 'ERP-' + payload.productionOrderNo,
      materialId: payload.productCode,
      materialName: payload.productName,
      qty: payload.planQuantity,
      priority: 1,
      planStartTime: payload.startTime?.toString() || '',
      planEndTime: payload.endTime?.toString() || '',
      status: mapExecutionStatus(payload.executionStatus),
      createTime: payload.createdTime?.toString() || '',
      updateTime: payload.updatedTime?.toString() || ''
    }
    return { data: formattedData }
  } catch (error) {
    // 后端无法连接时返回虚拟数据
    console.log('后端无法连接，返回虚拟数据（订单详情）')
    const mockDetail = mockOrders.find(o => o.id === id) || mockOrders[0]
    return { data: mockDetail }
  }
}

/**
 * 接收ERP订单
 * @param order ERP订单数据
 * @returns 接收结果
 */
export const receiveErpOrder = async (order: Partial<Order>) => {
  // 转换为后端所需格式
  const productionExecution = {
    executionNo: 'MES-' + Date.now(),
    productionOrderNo: order.erpOrderNo || '',
    productCode: 'PROD-' + Math.random().toString(36).substring(2, 10),
    productName: order.materialName || '',
    planQuantity: order.qty || 0,
    actualQuantity: 0,
    qualifiedQuantity: 0,
    unqualifiedQuantity: 0,
    executionStatus: 1, // 1-待执行
    workshop: '默认车间',
    productionLine: '默认生产线'
  }
  const response = await api.post<any>('/api/v1/mes/production', productionExecution)
  const payload = extractPayload(response)
  const formattedData = {
    id: payload.id.toString(),
    orderNo: payload.productionOrderNo,
    erpOrderNo: 'ERP-' + payload.productionOrderNo,
    materialId: payload.productCode,
    materialName: payload.productName,
    qty: payload.planQuantity,
    priority: 1,
    planStartTime: payload.startTime?.toString() || '',
    planEndTime: payload.endTime?.toString() || '',
    status: mapExecutionStatus(payload.executionStatus),
    createTime: payload.createdTime?.toString() || '',
    updateTime: payload.updatedTime?.toString() || ''
  }
  return { data: formattedData }
}

/**
 * 创建工单
 * @param workOrder 工单数据
 * @returns 创建结果
 */
export const createWorkOrder = async (workOrder: Partial<WorkOrder>) => {
  try {
    // 转换为后端所需格式
    const productionExecution = {
      executionNo: 'MES-' + Date.now(),
      productionOrderNo: workOrder.orderNo || '',
      productCode: workOrder.materialId || '',
      productName: workOrder.materialName || '',
      planQuantity: workOrder.qty || 0,
      actualQuantity: 0,
      qualifiedQuantity: 0,
      unqualifiedQuantity: 0,
      executionStatus: 1, // 1-待执行
      workshop: '默认车间',
      productionLine: '默认生产线'
    }
    const response = await api.post<any>('/api/v1/mes/production', productionExecution)
    const payload = extractPayload(response)
    // 转换后端数据格式为前端所需格式
    const formattedData = {
      id: payload.id.toString(),
      workOrderNo: payload.executionNo,
      erpProductionNo: payload.erpProductionNo || '',
      orderId: workOrder.orderId || payload.id.toString(),
      orderNo: payload.productionOrderNo,
      materialId: payload.productCode,
      materialName: payload.productName,
      qty: payload.planQuantity,
      status: mapExecutionStatusToWorkOrder(payload.executionStatus),
      createTime: payload.createdTime?.toString() || '',
      updateTime: payload.updatedTime?.toString() || ''
    }
    return { data: formattedData }
  } catch (error) {
    // 后端无法连接时返回模拟数据
    console.log('后端无法连接，返回模拟数据（创建工单）')
    const mockWorkOrder: WorkOrder = {
      id: Date.now().toString(),
      workOrderNo: 'WO-MOCK-' + Date.now(),
      orderId: workOrder.orderId || '1',
      orderNo: workOrder.orderNo || 'MES-001',
      materialId: workOrder.materialId || 'MAT-001',
      materialName: workOrder.materialName || '模拟产品',
      qty: workOrder.qty || 100,
      status: 'created',
      createTime: new Date().toISOString(),
      updateTime: new Date().toISOString()
    }
    return { data: mockWorkOrder }
  }
}

/**
 * 获取工单列表
 * @returns 工单列表
 */
export const getWorkOrders = async () => {
  try {
    const response = await api.get<any>('/api/v1/mes/production/list', {
      params: {
        page: 1,
        size: 100 // 获取较多数据用于显示
      }
    })
    const rawData = extractRecords(response)
    const formattedData = rawData.map((item: any) => ({
      id: item.id.toString(),
      workOrderNo: item.executionNo,
      erpProductionNo: item.erpProductionNo || '',
      orderId: item.id.toString(),
      orderNo: item.productionOrderNo,
      materialId: item.productCode,
      materialName: item.productName,
      qty: item.planQuantity,
      status: mapExecutionStatusToWorkOrder(item.executionStatus),
      createTime: item.createdTime?.toString() || '',
      updateTime: item.updatedTime?.toString() || ''
    }))
    return { data: formattedData }
  } catch (error) {
    // 后端无法连接时返回虚拟数据
    console.log('后端无法连接，返回虚拟数据（工单）')
    return { data: mockWorkOrders }
  }
}

/**
 * 将后端执行状态转换为前端工单状态
 * @param status 后端执行状态
 * @returns 前端工单状态
 */
const mapExecutionStatusToWorkOrder = (status: number): 'created' | 'released' | 'in_process' | 'finished' | 'closed' => {
  const statusMap: Record<number, 'created' | 'released' | 'in_process' | 'finished' | 'closed'> = {
    1: 'created',     // 待执行 -> 已创建
    2: 'in_process',  // 执行中 -> 处理中
    3: 'finished',    // 已完成 -> 已完成
    4: 'in_process',  // 已暂停 -> 处理中
    5: 'closed'       // 已取消 -> 已关闭
  }
  return statusMap[status] || 'created'
}

/**
 * 获取工单详情
 * @param id 工单ID
 * @returns 工单详情
 */
export const getWorkOrderDetail = async (id: string) => {
  try {
    const response = await api.get<any>(`/api/v1/mes/production/${id}`)
    const payload = extractPayload(response)
    // 转换后端数据格式为前端所需格式
    const formattedData = {
      id: payload.id.toString(),
      workOrderNo: payload.executionNo,
      orderId: payload.id.toString(),
      orderNo: payload.productionOrderNo,
      materialId: payload.productCode,
      materialName: payload.productName,
      qty: payload.planQuantity,
      status: mapExecutionStatusToWorkOrder(payload.executionStatus),
      createTime: payload.createdTime?.toString() || '',
      updateTime: payload.updatedTime?.toString() || ''
    }
    return { data: formattedData }
  } catch (error) {
    // 后端无法连接时返回虚拟数据
    console.log('后端无法连接，返回虚拟数据（工单详情）')
    const mockDetail: WorkOrder = mockWorkOrders.find(wo => wo.id === id) ?? mockWorkOrders[0] ?? {
      id,
      workOrderNo: `WO-${id}`,
      orderId: id,
      orderNo: `MES-${id}`,
      materialId: '',
      materialName: '',
      qty: 0,
      status: 'created',
      createTime: new Date().toISOString(),
      updateTime: new Date().toISOString()
    }
    return { data: mockDetail }
  }
}

/**
 * 释放工单
 * @param id 工单ID
 * @returns 释放结果
 */
export const releaseWorkOrder = (id: string) => {
  return api.post(`/api/v1/mes/production/${id}/start`, {}).then(() => getWorkOrderDetail(id))
}

/**
 * 创建工序派工
 * @param assignment 派工数据
 * @returns 创建结果
 */
export const createProcessAssignment = (assignment: Partial<ProcessAssignment>) => {
  return api.post<any>('/api/v1/mes/process-assignments', assignment).then(res => {
    const payload = extractPayload(res)
    const formatted = {
      id: payload.id.toString(),
      workOrderId: payload.workOrderId?.toString() || '',
      workOrderNo: payload.workOrderNo,
      stepId: payload.stepId,
      stepName: payload.stepName,
      workstationId: payload.workstationId,
      workstationName: payload.workstationName,
      operatorId: payload.operatorId,
      operatorName: payload.operatorName,
      startTime: payload.startTime?.toString() || '',
      endTime: payload.endTime?.toString() || '',
      status: payload.status,
      createTime: payload.createTime?.toString() || '',
      updateTime: payload.updateTime?.toString() || ''
    } as ProcessAssignment
    return { data: formatted }
  })
}

/**
 * 获取工序派工列表
 * @returns 派工列表
 */
export const getProcessAssignments = async (workOrderNo?: string) => {
  try {
    const response = await api.get<any>('/api/v1/mes/process-assignments/list', {
      params: {
        page: 1,
        size: 200,
        workOrderNo: workOrderNo || undefined
      }
    })
    const rawData = extractRecords(response)
    const formatted = rawData.map((item: any) => ({
      id: item.id.toString(),
      workOrderId: item.workOrderId?.toString() || '',
      workOrderNo: item.workOrderNo,
      stepId: item.stepId,
      stepName: item.stepName,
      workstationId: item.workstationId,
      workstationName: item.workstationName,
      operatorId: item.operatorId,
      operatorName: item.operatorName,
      startTime: item.startTime?.toString() || '',
      endTime: item.endTime?.toString() || '',
      status: item.status,
      createTime: item.createTime?.toString() || '',
      updateTime: item.updateTime?.toString() || ''
    })) as ProcessAssignment[]
    return { data: formatted }
  } catch (error) {
    console.log('后端无法连接，返回虚拟数据（工序派工）')
    return { data: mockProcessAssignments }
  }
}

/**
 * 更新生产执行数量
 * @param id 生产执行ID
 * @param updateData 数量更新数据
 * @returns 更新结果
 */
export const updateProductionQuantities = (id: string, updateData: {
  actualQuantity?: number;
  qualifiedQuantity?: number;
  unqualifiedQuantity?: number;
}) => {
  return api.put(`/api/v1/mes/production/${id}/quantity`, updateData).then(() => getWorkOrderDetail(id))
}

/**
 * 更新工序派工状态
 * @param id 派工ID
 * @param status 新状态
 * @returns 更新结果
 */
export const updateProcessAssignmentStatus = (id: string, status: string) => {
  return api.put<any>(`/api/v1/mes/process-assignments/${id}/status`, { status }).then(res => {
    const payload = extractPayload(res)
    return { data: { id: payload.id.toString(), status: payload.status } as ProcessAssignment }
  })
}
