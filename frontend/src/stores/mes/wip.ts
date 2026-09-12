import { defineStore } from 'pinia'
import * as wipApi from '../../api/mes/wip'
import type { WipLocation, Batch, FlowRecord } from '../../api/mes/wip'

export const useMesWipStore = defineStore('mesWip', {
  state: () => ({
    wipLocations: [] as WipLocation[],
    batches: [] as Batch[],
    flowRecords: [] as FlowRecord[],
    loading: {
      wipLocations: false,
      batches: false,
      flowRecords: false
    },
    error: ''
  }),

  getters: {
    // 获取所有处理中在制品
    processingWip: (state) => state.wipLocations.filter(wip => wip.status === 'processing'),
    // 获取所有已完成批次
    completedBatches: (state) => state.batches.filter(batch => batch.status === 'completed'),
    // 获取所有成功流转记录
    successfulFlowRecords: (state) => state.flowRecords.filter(fr => fr.status === 'success')
  },

  actions: {
    // 获取在制品位置列表
    async fetchWipLocations() {
      this.loading.wipLocations = true
      this.error = ''
      try {
        const response = await wipApi.getWipLocations()
        this.wipLocations = response.data || []
      } catch (err: any) {
        this.error = err.message || '获取在制品位置列表失败'
      } finally {
        this.loading.wipLocations = false
      }
    },

    // 获取在制品详情
    async fetchWipDetail(snCode: string) {
      try {
        const response = await wipApi.getWipDetail(snCode)
        if (response.data) {
          const index = this.wipLocations.findIndex(wip => wip.snCode === snCode)
          if (index !== -1) {
            this.wipLocations[index] = response.data
          } else {
            this.wipLocations.push(response.data)
          }
        }
      } catch (err: any) {
        this.error = err.message || '获取在制品详情失败'
      }
    },

    // 更新在制品位置
    async updateWipLocation(data: { snCode: string; stationId: string; stepId: string; status: string }) {
      try {
        const response = await wipApi.updateWipLocation(data)
        if (response.data) {
          const index = this.wipLocations.findIndex(wip => wip.snCode === data.snCode)
          if (index !== -1) {
            this.wipLocations[index] = response.data
          } else {
            this.wipLocations.push(response.data)
          }
          return response.data
        }
        return null
      } catch (err: any) {
        this.error = err.message || '更新在制品位置失败'
        throw err
      }
    },

    // 获取批次列表
    async fetchBatches() {
      this.loading.batches = true
      this.error = ''
      try {
        const response = await wipApi.getBatches()
        this.batches = response.data || []
      } catch (err: any) {
        this.error = err.message || '获取批次列表失败'
      } finally {
        this.loading.batches = false
      }
    },

    // 创建批次
    async createBatch(batchData: Partial<Batch>) {
      try {
        const response = await wipApi.createBatch(batchData)
        if (response.data) {
          this.batches.push(response.data)
          return response.data
        }
        return null
      } catch (err: any) {
        this.error = err.message || '创建批次失败'
        throw err
      }
    },

    // 获取批次详情
    async fetchBatchDetail(batchNo: string) {
      try {
        const response = await wipApi.getBatchDetail(batchNo)
        if (response.data) {
          const index = this.batches.findIndex(batch => batch.batchNo === batchNo)
          if (index !== -1) {
            this.batches[index] = response.data
          } else {
            this.batches.push(response.data)
          }
        }
      } catch (err: any) {
        this.error = err.message || '获取批次详情失败'
      }
    },

    // 获取流转记录列表
    async fetchFlowRecords() {
      this.loading.flowRecords = true
      this.error = ''
      try {
        const response = await wipApi.getFlowRecords()
        this.flowRecords = response.data || []
      } catch (err: any) {
        this.error = err.message || '获取流转记录列表失败'
      } finally {
        this.loading.flowRecords = false
      }
    },

    // 获取产品流转历史
    async fetchProductFlowHistory(snCode: string) {
      try {
        const response = await wipApi.getProductFlowHistory(snCode)
        // 更新或添加流转记录
        response.data.forEach(record => {
          const index = this.flowRecords.findIndex(fr => fr.id === record.id)
          if (index !== -1) {
            this.flowRecords[index] = record
          } else {
            this.flowRecords.push(record)
          }
        })
        return response.data
      } catch (err: any) {
        this.error = err.message || '获取产品流转历史失败'
        throw err
      }
    }
  }
})
