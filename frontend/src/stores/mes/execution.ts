import { defineStore } from 'pinia'
import * as executionApi from '../../api/mes/execution'
import type { Order, WorkOrder, ProcessAssignment } from '../../api/mes/execution'

export const useMesExecutionStore = defineStore('mesExecution', {
  state: () => ({
    orders: [] as Order[],
    workOrders: [] as WorkOrder[],
    processAssignments: [] as ProcessAssignment[],
    loading: {
      orders: false,
      workOrders: false,
      processAssignments: false
    },
    error: ''
  }),

  getters: {
    // 获取所有待处理订单
    pendingOrders: (state) => state.orders.filter(order => order.status === 'pending'),
    // 获取所有处理中工单
    inProcessWorkOrders: (state) => state.workOrders.filter(workOrder => workOrder.status === 'in_process'),
    // 获取所有已分配的工序
    assignedProcesses: (state) => state.processAssignments.filter(pa => pa.status === 'assigned')
  },

  actions: {
    // 获取生产订单列表
    async fetchOrders() {
      this.loading.orders = true
      this.error = ''
      try {
        const response = await executionApi.getOrders()
        this.orders = response.data || []
      } catch (err: any) {
        this.error = err.message || '获取订单列表失败'
      } finally {
        this.loading.orders = false
      }
    },

    // 获取订单详情
    async fetchOrderDetail(id: string) {
      try {
        const response = await executionApi.getOrderDetail(id)
        if (response && response.data) {
          const index = this.orders.findIndex(order => order.id === id)
          if (index !== -1) {
            this.orders[index] = response.data as Order
          } else {
            this.orders.push(response.data as Order)
          }
        }
        return response.data
      } catch (err: any) {
        this.error = err.message || '获取订单详情失败'
        throw err
      }
    },

    // 接收ERP订单
    async receiveErpOrder(orderData: Partial<Order>) {
      try {
        const response = await executionApi.receiveErpOrder(orderData)
        this.orders.push(response.data)
        return response.data
      } catch (err: any) {
        this.error = err.message || '接收ERP订单失败'
        throw err
      }
    },

    // 获取工单列表
    async fetchWorkOrders() {
      this.loading.workOrders = true
      this.error = ''
      try {
        const response = await executionApi.getWorkOrders()
        this.workOrders = response.data || []
      } catch (err: any) {
        this.error = err.message || '获取工单列表失败'
      } finally {
        this.loading.workOrders = false
      }
    },

    // 创建工单
    async createWorkOrder(workOrderData: Partial<WorkOrder>) {
      try {
        const response = await executionApi.createWorkOrder(workOrderData)
        this.workOrders.push(response.data)
        return response.data
      } catch (err: any) {
        this.error = err.message || '创建工单失败'
        throw err
      }
    },

    // 释放工单
    async releaseWorkOrder(id: string) {
      try {
        const response = await executionApi.releaseWorkOrder(id)
        const index = this.workOrders.findIndex(wo => wo.id === id)
        if (index !== -1) {
          this.workOrders[index] = response.data
        }
        return response.data
      } catch (err: any) {
        this.error = err.message || '释放工单失败'
        throw err
      }
    },

    // 获取工单详情
    async fetchWorkOrderDetail(id: string) {
      try {
        const response = await executionApi.getWorkOrderDetail(id)
        return response.data
      } catch (err: any) {
        this.error = err.message || '获取工单详情失败'
        throw err
      }
    },

    // 获取工序派工列表
    async fetchProcessAssignments(workOrderNo?: string) {
      this.loading.processAssignments = true
      this.error = ''
      try {
        const response = await executionApi.getProcessAssignments(workOrderNo)
        this.processAssignments = response.data || []
      } catch (err: any) {
        this.error = err.message || '获取工序派工列表失败'
      } finally {
        this.loading.processAssignments = false
      }
    },

    // 创建工序派工
    async createProcessAssignment(assignmentData: Partial<ProcessAssignment>) {
      try {
        const response = await executionApi.createProcessAssignment(assignmentData)
        this.processAssignments.push(response.data)
        return response.data
      } catch (err: any) {
        this.error = err.message || '创建工序派工失败'
        throw err
      }
    },

    // 更新工序派工状态
    async updateProcessAssignmentStatus(id: string, status: string) {
      try {
        const response = await executionApi.updateProcessAssignmentStatus(id, status)
        const index = this.processAssignments.findIndex(pa => pa.id === id)
        if (index !== -1) {
          this.processAssignments[index] = response.data
        }
        return response.data
      } catch (err: any) {
        this.error = err.message || '更新工序派工状态失败'
        throw err
      }
    },

    // 更新生产执行数量
    async updateProductionQuantities(id: string, updateData: {
      actualQuantity?: number;
      qualifiedQuantity?: number;
      unqualifiedQuantity?: number;
    }) {
      try {
        const response = await executionApi.updateProductionQuantities(id, updateData)
        // 更新工单列表中的数量
        const workOrderIndex = this.workOrders.findIndex(wo => wo.id === id)
        if (workOrderIndex !== -1) {
          this.workOrders[workOrderIndex] = response.data
        }
        return response.data
      } catch (err: any) {
        this.error = err.message || '更新生产数量失败'
        throw err
      }
    }
  }
})
