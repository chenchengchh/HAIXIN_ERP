import { defineStore } from 'pinia'
import { procurementApi } from '../../api/srm/index'
import type { PurchaseOrder, DeliveryNote } from '../../types/srm/index'
import { PurchaseOrderStatus } from '../../types/srm/index'

// 定义订单管理状态存储
export const useOrderStore = defineStore('srm-order', {
  state: () => ({
    // 采购订单列表
    purchaseOrderList: [] as PurchaseOrder[],
    // 采购订单详情
    purchaseOrderDetail: {} as PurchaseOrder,
    // 发货单列表
    deliveryNoteList: [] as DeliveryNote[],
    // 发货单详情
    deliveryNoteDetail: {} as DeliveryNote,
    // 分页信息
    pagination: {
      currentPage: 1,
      pageSize: 10,
      total: 0
    },
    // 加载状态
    loading: false,
    // 搜索条件
    searchParams: {
      orderNo: '',
      supplierName: '',
      status: ''
    }
  }),

  getters: {
    // 过滤后的采购订单列表
    filteredPurchaseOrderList: (state) => {
      return state.purchaseOrderList
    }
  },

  actions: {
    // 获取采购订单列表
    async fetchPurchaseOrderList(params?: any) {
      this.loading = true
      try {
        const mergedParams = {
          page: this.pagination.currentPage,
          pageSize: this.pagination.pageSize,
          ...this.searchParams,
          ...params
        }
        
        const res = await procurementApi.getPurchaseOrderList(mergedParams)
        this.purchaseOrderList = res.data.list
        this.pagination.total = res.data.total
      } catch (error) {
        console.error('获取采购订单列表失败:', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    // 获取采购订单详情
    async fetchPurchaseOrderDetail(id: number) {
      this.loading = true
      try {
        const res = await procurementApi.getPurchaseOrderDetail(id)
        this.purchaseOrderDetail = res.data
      } catch (error) {
        console.error('获取采购订单详情失败:', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    // 获取发货单列表
    async fetchDeliveryNoteList(params?: any) {
      this.loading = true
      try {
        const res = await procurementApi.getDeliveryNoteList(params)
        this.deliveryNoteList = res.data.list
      } catch (error) {
        console.error('获取发货单列表失败:', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    // 获取发货单详情
    async fetchDeliveryNoteDetail(id: number) {
      this.loading = true
      try {
        const res = await procurementApi.getDeliveryNoteDetail(id)
        this.deliveryNoteDetail = res.data
      } catch (error) {
        console.error('获取发货单详情失败:', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    // 创建采购订单
    async createPurchaseOrder(data: Omit<PurchaseOrder, 'id' | 'orderNo' | 'createTime' | 'updateTime'>) {
      this.loading = true
      try {
        const res = await procurementApi.createPurchaseOrder(data)
        // 重新获取采购订单列表
        await this.fetchPurchaseOrderList()
        return res.data
      } catch (error) {
        console.error('创建采购订单失败:', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    // 更新采购订单
    async updatePurchaseOrder(id: number, data: Partial<PurchaseOrder>) {
      this.loading = true
      try {
        await procurementApi.updatePurchaseOrder(id, data)
        // 重新获取采购订单列表
        await this.fetchPurchaseOrderList()
        // 更新当前详情
        if (this.purchaseOrderDetail.id === id) {
          await this.fetchPurchaseOrderDetail(id)
        }
      } catch (error) {
        console.error('更新采购订单失败:', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    // 供应商确认订单
    async confirmPurchaseOrder(id: number) {
      this.loading = true
      try {
        await procurementApi.confirmPurchaseOrder(id)
        // 重新获取采购订单列表
        await this.fetchPurchaseOrderList()
        // 更新当前详情
        if (this.purchaseOrderDetail.id === id) {
          await this.fetchPurchaseOrderDetail(id)
        }
      } catch (error) {
        console.error('确认采购订单失败:', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    // 创建发货单
    async createDeliveryNote(data: Omit<DeliveryNote, 'id' | 'deliveryNo' | 'createTime'>) {
      this.loading = true
      try {
        const res = await procurementApi.createDeliveryNote(data)
        // 重新获取发货单列表
        await this.fetchDeliveryNoteList()
        return res.data
      } catch (error) {
        console.error('创建发货单失败:', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    // 设置搜索条件
    setSearchParams(params: any) {
      this.searchParams = { ...this.searchParams, ...params }
      this.pagination.currentPage = 1
    },

    // 设置分页信息
    setPagination(page: number, pageSize: number) {
      this.pagination.currentPage = page
      this.pagination.pageSize = pageSize
    },

    // 重置状态
    resetState() {
      this.purchaseOrderList = []
      this.purchaseOrderDetail = {} as PurchaseOrder
      this.deliveryNoteList = []
      this.deliveryNoteDetail = {} as DeliveryNote
      this.pagination = {
        currentPage: 1,
        pageSize: 10,
        total: 0
      }
      this.searchParams = {
        orderNo: '',
        supplierName: '',
        status: ''
      }
    }
  }
})
