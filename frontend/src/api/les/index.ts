/**
 * LES（物流执行系统）API服务
 */

import request from '../index'
import * as types from '../../types/les'

/**
 * 运输管理API
 */
export const transportApi = {
  /**
   * 获取运输计划列表
   * @param params 查询参数
   */
  fetchTransportPlans(params?: any) {
    return request.get('/les/transport/plans', { params })
  },

  /**
   * 获取运输计划详情
   * @param id 计划ID
   */
  getTransportPlanDetail(id: number) {
    return request.get(`/les/transport/plans/${id}`)
  },

  /**
   * 创建运输计划
   * @param data 运输计划数据
   */
  createTransportPlan(data: Omit<types.TransportPlan, 'id' | 'createTime'>) {
    return request.post('/les/transport/plans', data)
  },

  /**
   * 更新运输计划
   * @param id 计划ID
   * @param data 更新数据
   */
  updateTransportPlan(id: number, data: Partial<types.TransportPlan>) {
    return request.put(`/les/transport/plans/${id}`, data)
  },

  /**
   * 删除运输计划
   * @param id 计划ID
   */
  deleteTransportPlan(id: number) {
    return request.delete(`/les/transport/plans/${id}`)
  },

  /**
   * 获取车辆列表
   * @param params 查询参数
   */
  fetchVehicles(params?: any) {
    return request.get('/les/transport/vehicles', { params })
  },

  /**
   * 创建车辆
   * @param data 车辆数据
   */
  createVehicle(data: Omit<types.Vehicle, 'id' | 'createTime'>) {
    return request.post('/les/transport/vehicles', data)
  },

  /**
   * 更新车辆
   * @param id 车辆ID
   * @param data 更新数据
   */
  updateVehicle(id: number, data: Partial<types.Vehicle>) {
    return request.put(`/les/transport/vehicles/${id}`, data)
  },

  /**
   * 删除车辆
   * @param id 车辆ID
   */
  deleteVehicle(id: number) {
    return request.delete(`/les/transport/vehicles/${id}`)
  },

  /**
   * 获取司机列表
   * @param params 查询参数
   */
  fetchDrivers(params?: any) {
    return request.get('/les/transport/drivers', { params })
  },

  /**
   * 创建司机
   * @param data 司机数据
   */
  createDriver(data: Omit<types.Driver, 'id' | 'createTime'>) {
    return request.post('/les/transport/drivers', data)
  },

  /**
   * 更新司机
   * @param id 司机ID
   * @param data 更新数据
   */
  updateDriver(id: number, data: Partial<types.Driver>) {
    return request.put(`/les/transport/drivers/${id}`, data)
  },

  /**
   * 删除司机
   * @param id 司机ID
   */
  deleteDriver(id: number) {
    return request.delete(`/les/transport/drivers/${id}`)
  },

  /**
   * 获取路线列表
   * @param params 查询参数
   */
  fetchRoutes(params?: any) {
    return request.get('/les/transport/routes', { params })
  },

  /**
   * 创建路线
   * @param data 路线数据
   */
  createRoute(data: Omit<types.Route, 'id' | 'createTime'>) {
    return request.post('/les/transport/routes', data)
  },

  /**
   * 更新路线
   * @param id 路线ID
   * @param data 更新数据
   */
  updateRoute(id: number, data: Partial<types.Route>) {
    return request.put(`/les/transport/routes/${id}`, data)
  },

  /**
   * 删除路线
   * @param id 路线ID
   */
  deleteRoute(id: number) {
    return request.delete(`/les/transport/routes/${id}`)
  },

  /**
   * 获取销售订单列表
   * @param params 查询参数
   */
  fetchSalesOrders(params?: any) {
    return request.get('/les/transport/sales-orders', { params })
  },

  /**
   * 创建运输任务
   * @param data 任务数据
   */
  createTransportTask(data: Omit<types.TransportTask, 'id' | 'createTime'>) {
    return request.post('/les/transport/tasks', data)
  },

  /**
   * 获取运输任务列表
   * @param params 查询参数
   */
  fetchTransportTasks(params?: any) {
    return request.get('/les/transport/tasks', { params })
  }
}

/**
 * 在途监控API
 */
export const monitoringApi = {
  /**
   * 获取在途监控日志
   * @param params 查询参数
   */
  fetchMonitorLogs(params?: any) {
    return request.get('/les/monitoring/logs', { params })
  },

  /**
   * 获取运输计划的监控日志
   * @param planId 计划ID
   * @param params 查询参数
   */
  fetchPlanMonitorLogs(planId: number, params?: any) {
    return request.get(`/les/monitoring/plans/${planId}/logs`, { params })
  },

  /**
   * 获取异常事件列表
   * @param params 查询参数
   */
  fetchAnomalyEvents(params?: any) {
    return request.get('/les/monitoring/anomalies', { params })
  },

  /**
   * 处理异常事件
   * @param id 事件ID
   * @param data 处理数据
   */
  handleAnomalyEvent(id: number, data: { handlingResult: string; handlingStatus: string }) {
    return request.put(`/les/monitoring/anomalies/${id}`, data)
  },

  /**
   * 获取车辆实时位置
   * @param params 查询参数
   */
  fetchVehicleLocations(params?: any) {
    return request.get('/les/monitoring/vehicle-locations', { params })
  },

  /**
   * 获取车辆历史轨迹
   * @param vehicleId 车辆ID
   * @param params 查询参数
   */
  fetchVehicleHistory(vehicleId: number, params?: any) {
    return request.get(`/les/monitoring/vehicles/${vehicleId}/history`, { params })
  },

  /**
   * 获取运输计划轨迹回放
   * @param planId 计划ID
   * @param params 查询参数
   */
  fetchPlanTrack(planId: number, params?: any) {
    return request.get(`/les/monitoring/plans/${planId}/track`, { params })
  }
}

/**
 * 签收管理API
 */
export const signApi = {
  /**
   * 获取签收凭证列表
   * @param params 查询参数
   */
  fetchSignVouchers(params?: any) {
    return request.get('/les/sign/vouchers', { params })
  },

  /**
   * 获取运输计划的签收凭证
   * @param planId 计划ID
   */
  getPlanSignVoucher(planId: number) {
    return request.get(`/les/sign/plans/${planId}/voucher`)
  },

  /**
   * 创建签收凭证
   * @param data 签收凭证数据
   */
  createSignVoucher(data: Omit<types.SignVoucher, 'id'>) {
    return request.post('/les/sign/vouchers', data)
  },

  /**
   * 更新签收凭证
   * @param id 凭证ID
   * @param data 更新数据
   */
  updateSignVoucher(id: number, data: Partial<types.SignVoucher>) {
    return request.put(`/les/sign/vouchers/${id}`, data)
  },

  /**
   * 上传签收图片
   * @param file 文件对象
   */
  uploadSignImage(file: File) {
    const formData = new FormData()
    formData.append('file', file)
    return request.post('/les/sign/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  /**
   * 获取签收统计数据
   * @param params 查询参数
   */
  getSignStats(params?: any) {
    return request.get('/les/sign/stats', { params })
  },

  /**
   * 获取签收异常列表
   * @param params 查询参数
   */
  fetchSignAnomalies(params?: any) {
    return request.get('/les/sign/anomalies', { params })
  }
}

/**
 * 物流分析API
 */
export const analysisApi = {
  /**
   * 获取运输成本列表
   * @param params 查询参数
   */
  fetchTransportCosts(params?: any) {
    return request.get('/les/analysis/costs', { params })
  },

  /**
   * 获取运输计划成本
   * @param planId 计划ID
   */
  getPlanCost(planId: number) {
    return request.get(`/les/analysis/plans/${planId}/cost`)
  },

  /**
   * 获取物流分析数据
   * @param params 查询参数
   */
  fetchLogisticsAnalysis(params?: any) {
    return request.get('/les/analysis/logistics', { params })
  },

  /**
   * 获取服务质量评估列表
   * @param params 查询参数
   */
  fetchServiceQualities(params?: any) {
    return request.get('/les/analysis/service-qualities', { params })
  },

  /**
   * 获取运输统计数据
   * @param params 查询参数
   */
  fetchTransportStats(params?: any) {
    return request.get('/les/analysis/stats', { params })
  },

  /**
   * 获取运输效率分析
   * @param params 查询参数
   */
  fetchEfficiencyAnalysis(params?: any) {
    return request.get('/les/analysis/efficiency', { params })
  },

  /**
   * 获取资源利用率分析
   * @param params 查询参数
   */
  fetchUtilizationAnalysis(params?: any) {
    return request.get('/les/analysis/utilization', { params })
  },

  /**
   * 获取异常事件分析
   * @param params 查询参数
   */
  fetchAnomalyAnalysis(params?: any) {
    return request.get('/les/analysis/anomalies', { params })
  },

  /**
   * 导出分析报告
   * @param params 查询参数
   */
  exportAnalysisReport(params?: any) {
    return request.get('/les/analysis/export', { 
      params,
      responseType: 'blob' // 导出文件需要设置响应类型为blob
    })
  }
}

/**
 * 导出默认API对象，包含所有模块的API
 */
const lesApi = {
  transport: transportApi,
  monitoring: monitoringApi,
  sign: signApi,
  analysis: analysisApi
}

export default lesApi
