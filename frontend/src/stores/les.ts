/**
 * LES（物流执行系统）状态管理
 */

import { defineStore } from 'pinia'
import * as types from '../types/les'
import { unwrapListResponse, unwrapResponseData } from '@/api'
import { transportApi, monitoringApi, signApi, analysisApi } from '../api/les'

const unwrapApiData = <T = any>(response: any): T => {
  return (unwrapResponseData<T>(response) ?? null) as T
}

const unwrapApiList = <T = any>(response: any): T[] => {
  return unwrapListResponse<T>(response)
}

// 定义LES状态类型
interface LesState {
  // 运输管理
  transportPlans: types.TransportPlan[]
  vehicles: types.Vehicle[]
  drivers: types.Driver[]
  routes: types.Route[]
  transportTasks: types.TransportTask[]
  salesOrders: types.SalesOrder[]
  
  // 在途监控
  monitorLogs: types.MonitorLog[]
  anomalyEvents: types.AnomalyEvent[]
  vehicleLocations: types.VehicleLocation[]
  
  // 签收管理
  signVouchers: types.SignVoucher[]
  
  // 物流分析
  transportCosts: types.TransportCost[]
  logisticsAnalysis: types.LogisticsAnalysis[]
  serviceQualities: types.ServiceQuality[]
  transportStats: types.TransportStats
  
  // 加载状态
  loading: {
    transport: boolean
    monitoring: boolean
    sign: boolean
    analysis: boolean
  }
}

// 初始状态
const initialState: LesState = {
  // 运输管理
  transportPlans: [],
  vehicles: [],
  drivers: [],
  routes: [],
  transportTasks: [],
  salesOrders: [],
  
  // 在途监控
  monitorLogs: [],
  anomalyEvents: [],
  vehicleLocations: [],
  
  // 签收管理
  signVouchers: [],
  
  // 物流分析
  transportCosts: [],
  logisticsAnalysis: [],
  serviceQualities: [],
  transportStats: {
    totalPlans: 0,
    inTransitPlans: 0,
    completedPlans: 0,
    delayedPlans: 0,
    totalDistance: 0,
    totalCost: 0,
    averageOnTimeRate: 0,
    averageSignSuccessRate: 0
  },
  
  // 加载状态
  loading: {
    transport: false,
    monitoring: false,
    sign: false,
    analysis: false
  }
}

// 定义LES状态管理
const useLesStore = defineStore('les', {
  state: () => initialState,
  
  getters: {
    // 运输管理
    activeVehicles: (state) => state.vehicles.filter(v => v.status === 'active'),
    availableDrivers: (state) => state.drivers.filter(d => d.status === 'available'),
    planningPlans: (state) => state.transportPlans.filter(p => p.status === types.TransportPlanStatus.PLANNING),
    inTransitPlans: (state) => state.transportPlans.filter(p => p.status === types.TransportPlanStatus.IN_TRANSIT),
    completedPlans: (state) => state.transportPlans.filter(p => p.status === types.TransportPlanStatus.SIGNED),
    
    // 在途监控
    ongoingAnomalies: (state) => state.anomalyEvents.filter(e => e.handlingStatus === 'pending'),
    
    // 签收管理
    onTimeDeliveries: (state) => state.signVouchers.filter(v => v.onTimeStatus === types.OnTimeStatus.ON_TIME),
    delayedDeliveries: (state) => state.signVouchers.filter(v => v.onTimeStatus === types.OnTimeStatus.DELAYED),
    
    // 物流分析
    totalCost: (state) => state.transportCosts.reduce((sum, cost) => sum + cost.totalCost, 0),
    averageCostPerKm: (state) => {
      const totalCost = state.transportCosts.reduce((sum, cost) => sum + cost.totalCost, 0)
      const totalDistance = state.transportStats.totalDistance
      return totalDistance > 0 ? totalCost / totalDistance : 0
    }
  },
  
  actions: {
    // 运输管理
    async fetchTransportPlans() {
      this.loading.transport = true
      try {
        const response = await transportApi.fetchTransportPlans({ page: 1, size: 200 })
        this.transportPlans = unwrapApiList<types.TransportPlan>(response)
      } catch (error) {
        console.error('Failed to fetch transport plans:', error)
      } finally {
        this.loading.transport = false
      }
    },

    async getTransportPlanDetail(id: number) {
      this.loading.transport = true
      try {
        const response = await transportApi.getTransportPlanDetail(id)
        const detail = unwrapApiData<types.TransportPlan>(response)
        const index = this.transportPlans.findIndex(p => p.id === id)
        if (index >= 0) this.transportPlans.splice(index, 1, detail)
        return detail
      } catch (error) {
        console.error('Failed to get transport plan detail:', error)
        throw error
      } finally {
        this.loading.transport = false
      }
    },
    
    async fetchVehicles() {
      this.loading.transport = true
      try {
        const response = await transportApi.fetchVehicles({ page: 1, size: 200 })
        this.vehicles = unwrapApiList<types.Vehicle>(response)
      } catch (error) {
        console.error('Failed to fetch vehicles:', error)
      } finally {
        this.loading.transport = false
      }
    },

    async createVehicle(vehicle: Omit<types.Vehicle, 'id' | 'createTime'>) {
      this.loading.transport = true
      try {
        const response = await transportApi.createVehicle(vehicle)
        const created = unwrapApiData<types.Vehicle>(response)
        this.vehicles.unshift(created)
        return created
      } catch (error) {
        console.error('Failed to create vehicle:', error)
        throw error
      } finally {
        this.loading.transport = false
      }
    },

    async updateVehicle(id: number, vehicle: Partial<types.Vehicle>) {
      this.loading.transport = true
      try {
        const response = await transportApi.updateVehicle(id, vehicle as any)
        const updated = unwrapApiData<any>(response)
        const index = this.vehicles.findIndex(v => v.id === id)
        if (index >= 0) this.vehicles.splice(index, 1, updated)
        return updated
      } catch (error) {
        console.error('Failed to update vehicle:', error)
        throw error
      } finally {
        this.loading.transport = false
      }
    },

    async deleteVehicle(id: number) {
      this.loading.transport = true
      try {
        await transportApi.deleteVehicle(id)
        const index = this.vehicles.findIndex(v => v.id === id)
        if (index >= 0) this.vehicles.splice(index, 1)
        return true
      } catch (error) {
        console.error('Failed to delete vehicle:', error)
        throw error
      } finally {
        this.loading.transport = false
      }
    },
    
    async fetchDrivers() {
      this.loading.transport = true
      try {
        const response = await transportApi.fetchDrivers({ page: 1, size: 200 })
        this.drivers = unwrapApiList<types.Driver>(response)
      } catch (error) {
        console.error('Failed to fetch drivers:', error)
      } finally {
        this.loading.transport = false
      }
    },

    async createDriver(driver: Omit<types.Driver, 'id' | 'createTime'>) {
      this.loading.transport = true
      try {
        const response = await transportApi.createDriver(driver)
        const created = unwrapApiData<types.Driver>(response)
        this.drivers.unshift(created)
        return created
      } catch (error) {
        console.error('Failed to create driver:', error)
        throw error
      } finally {
        this.loading.transport = false
      }
    },

    async updateDriver(id: number, driver: Partial<types.Driver>) {
      this.loading.transport = true
      try {
        const response = await transportApi.updateDriver(id, driver as any)
        const updated = unwrapApiData<any>(response)
        const index = this.drivers.findIndex(d => d.id === id)
        if (index >= 0) this.drivers.splice(index, 1, updated)
        return updated
      } catch (error) {
        console.error('Failed to update driver:', error)
        throw error
      } finally {
        this.loading.transport = false
      }
    },

    async deleteDriver(id: number) {
      this.loading.transport = true
      try {
        await transportApi.deleteDriver(id)
        const index = this.drivers.findIndex(d => d.id === id)
        if (index >= 0) this.drivers.splice(index, 1)
        return true
      } catch (error) {
        console.error('Failed to delete driver:', error)
        throw error
      } finally {
        this.loading.transport = false
      }
    },
    
    async fetchRoutes() {
      this.loading.transport = true
      try {
        const response = await transportApi.fetchRoutes({ page: 1, size: 200 })
        this.routes = unwrapApiList<types.Route>(response)
      } catch (error) {
        console.error('Failed to fetch routes:', error)
      } finally {
        this.loading.transport = false
      }
    },

    async createRoute(route: Omit<types.Route, 'id' | 'createTime'>) {
      this.loading.transport = true
      try {
        const response = await transportApi.createRoute(route)
        const created = unwrapApiData<types.Route>(response)
        this.routes.unshift(created)
        return created
      } catch (error) {
        console.error('Failed to create route:', error)
        throw error
      } finally {
        this.loading.transport = false
      }
    },

    async updateRoute(id: number, route: Partial<types.Route>) {
      this.loading.transport = true
      try {
        const response = await transportApi.updateRoute(id, route as any)
        const updated = unwrapApiData<any>(response)
        const index = this.routes.findIndex(r => r.id === id)
        if (index >= 0) this.routes.splice(index, 1, updated)
        return updated
      } catch (error) {
        console.error('Failed to update route:', error)
        throw error
      } finally {
        this.loading.transport = false
      }
    },

    async deleteRoute(id: number) {
      this.loading.transport = true
      try {
        await transportApi.deleteRoute(id)
        const index = this.routes.findIndex(r => r.id === id)
        if (index >= 0) this.routes.splice(index, 1)
        return true
      } catch (error) {
        console.error('Failed to delete route:', error)
        throw error
      } finally {
        this.loading.transport = false
      }
    },
    
    async createTransportPlan(plan: Omit<types.TransportPlan, 'id' | 'createTime'>) {
      this.loading.transport = true
      try {
        const response = await transportApi.createTransportPlan(plan as any)
        const created = unwrapApiData<types.TransportPlan>(response)
        this.transportPlans.unshift(created)
        return created
      } catch (error) {
        console.error('Failed to create transport plan:', error)
        throw error
      } finally {
        this.loading.transport = false
      }
    },

    async updateTransportPlan(id: number, plan: Partial<types.TransportPlan>) {
      this.loading.transport = true
      try {
        const response = await transportApi.updateTransportPlan(id, plan as any)
        const updated = unwrapApiData<types.TransportPlan>(response)
        const index = this.transportPlans.findIndex(p => p.id === id)
        if (index >= 0) this.transportPlans.splice(index, 1, updated)
        return updated
      } catch (error) {
        console.error('Failed to update transport plan:', error)
        throw error
      } finally {
        this.loading.transport = false
      }
    },

    async deleteTransportPlan(id: number) {
      this.loading.transport = true
      try {
        await transportApi.deleteTransportPlan(id)
        const index = this.transportPlans.findIndex(p => p.id === id)
        if (index >= 0) this.transportPlans.splice(index, 1)
        return true
      } catch (error) {
        console.error('Failed to delete transport plan:', error)
        throw error
      } finally {
        this.loading.transport = false
      }
    },

    async fetchSalesOrders() {
      this.loading.transport = true
      try {
        const response = await transportApi.fetchSalesOrders({ page: 1, size: 200 })
        this.salesOrders = unwrapApiList<types.SalesOrder>(response)
      } catch (error) {
        console.error('Failed to fetch sales orders:', error)
      } finally {
        this.loading.transport = false
      }
    },

    async createTransportTask(task: Omit<types.TransportTask, 'id' | 'createTime'>) {
      this.loading.transport = true
      try {
        const response = await transportApi.createTransportTask(task as any)
        const created = unwrapApiData<types.TransportTask>(response)
        this.transportTasks.unshift(created)
        return created
      } catch (error) {
        console.error('Failed to create transport task:', error)
        throw error
      } finally {
        this.loading.transport = false
      }
    },

    async fetchTransportTasks(params?: any) {
      this.loading.transport = true
      try {
        const response = await transportApi.fetchTransportTasks({ page: 1, size: 200, ...(params || {}) })
        this.transportTasks = unwrapApiList<types.TransportTask>(response)
      } catch (error) {
        console.error('Failed to fetch transport tasks:', error)
      } finally {
        this.loading.transport = false
      }
    },
    
    // 在途监控
    async fetchMonitorLogs(planId?: number) {
      this.loading.monitoring = true
      try {
        const response = planId
          ? await monitoringApi.fetchPlanMonitorLogs(planId, { page: 1, size: 200 })
          : await monitoringApi.fetchMonitorLogs({ page: 1, size: 200 })
        this.monitorLogs = unwrapApiList<types.MonitorLog>(response)
      } catch (error) {
        console.error('Failed to fetch monitor logs:', error)
      } finally {
        this.loading.monitoring = false
      }
    },
    
    async fetchAnomalyEvents() {
      this.loading.monitoring = true
      try {
        const response = await monitoringApi.fetchAnomalyEvents({ page: 1, size: 200 })
        this.anomalyEvents = unwrapApiList<types.AnomalyEvent>(response)
      } catch (error) {
        console.error('Failed to fetch anomaly events:', error)
      } finally {
        this.loading.monitoring = false
      }
    },

    async handleAnomalyEvent(id: number, data: { handlingResult: string; handlingStatus: string }) {
      this.loading.monitoring = true
      try {
        const response = await monitoringApi.handleAnomalyEvent(id, data)
        const updated = unwrapApiData<any>(response)
        const index = this.anomalyEvents.findIndex(e => e.id === id)
        if (index >= 0) this.anomalyEvents.splice(index, 1, updated)
        return updated
      } catch (error) {
        console.error('Failed to handle anomaly event:', error)
        throw error
      } finally {
        this.loading.monitoring = false
      }
    },
    
    async fetchVehicleLocations() {
      this.loading.monitoring = true
      try {
        const response = await monitoringApi.fetchVehicleLocations({})
        this.vehicleLocations = unwrapApiList<types.VehicleLocation>(response)
      } catch (error) {
        console.error('Failed to fetch vehicle locations:', error)
      } finally {
        this.loading.monitoring = false
      }
    },
    
    // 签收管理
    async fetchSignVouchers() {
      this.loading.sign = true
      try {
        const response = await signApi.fetchSignVouchers({ page: 1, size: 200 })
        this.signVouchers = unwrapApiList<types.SignVoucher>(response)
      } catch (error) {
        console.error('Failed to fetch sign vouchers:', error)
      } finally {
        this.loading.sign = false
      }
    },

    async fetchSignAnomalies(params?: any) {
      this.loading.sign = true
      try {
        const response = await signApi.fetchSignAnomalies({ page: 1, size: 200, ...(params || {}) })
        return unwrapApiList<any>(response)
      } catch (error) {
        console.error('Failed to fetch sign anomalies:', error)
        return []
      } finally {
        this.loading.sign = false
      }
    },
    
    async createSignVoucher(voucher: Omit<types.SignVoucher, 'id'>) {
      this.loading.sign = true
      try {
        const response = await signApi.createSignVoucher(voucher as any)
        const created = unwrapApiData<types.SignVoucher>(response)
        this.signVouchers.unshift(created)
        return created
      } catch (error) {
        console.error('Failed to create sign voucher:', error)
        throw error
      } finally {
        this.loading.sign = false
      }
    },

    async updateSignVoucher(id: number, voucher: Partial<types.SignVoucher> & { status?: string }) {
      this.loading.sign = true
      try {
        const response = await signApi.updateSignVoucher(id, voucher as any)
        const updated = unwrapApiData<any>(response)
        const index = this.signVouchers.findIndex(v => v.id === id)
        if (index >= 0) this.signVouchers.splice(index, 1, updated)
        return updated
      } catch (error) {
        console.error('Failed to update sign voucher:', error)
        throw error
      } finally {
        this.loading.sign = false
      }
    },

    async uploadSignImage(file: File) {
      this.loading.sign = true
      try {
        const response = await signApi.uploadSignImage(file)
        return unwrapApiData<any>(response)
      } catch (error) {
        console.error('Failed to upload sign image:', error)
        throw error
      } finally {
        this.loading.sign = false
      }
    },
    
    // 物流分析
    async fetchTransportCosts(params?: any) {
      this.loading.analysis = true
      try {
        const response = await analysisApi.fetchTransportCosts({ page: 1, size: 200, ...(params || {}) })
        this.transportCosts = unwrapApiList<types.TransportCost>(response)
      } catch (error) {
        console.error('Failed to fetch transport costs:', error)
      } finally {
        this.loading.analysis = false
      }
    },

    async getPlanCost(planId: number) {
      this.loading.analysis = true
      try {
        const response = await analysisApi.getPlanCost(planId)
        return unwrapApiData<any>(response)
      } catch (error) {
        console.error('Failed to get plan cost:', error)
        throw error
      } finally {
        this.loading.analysis = false
      }
    },
    
    async fetchLogisticsAnalysis() {
      this.loading.analysis = true
      try {
        const response = await analysisApi.fetchLogisticsAnalysis({ page: 1, size: 200 })
        this.logisticsAnalysis = unwrapApiList<types.LogisticsAnalysis>(response)
      } catch (error) {
        console.error('Failed to fetch logistics analysis:', error)
      } finally {
        this.loading.analysis = false
      }
    },
    
    async fetchServiceQualities() {
      this.loading.analysis = true
      try {
        const response = await analysisApi.fetchServiceQualities({ page: 1, size: 200 })
        this.serviceQualities = unwrapApiList<types.ServiceQuality>(response)
      } catch (error) {
        console.error('Failed to fetch service qualities:', error)
      } finally {
        this.loading.analysis = false
      }
    },
    
    async fetchTransportStats() {
      this.loading.analysis = true
      try {
        const response = await analysisApi.fetchTransportStats({})
        this.transportStats = unwrapApiData<types.TransportStats>(response) || this.transportStats
      } catch (error) {
        console.error('Failed to fetch transport stats:', error)
      } finally {
        this.loading.analysis = false
      }
    }
  }
})

export default useLesStore
