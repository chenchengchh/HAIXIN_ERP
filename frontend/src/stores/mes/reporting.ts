import { defineStore } from 'pinia'
import * as reportingApi from '../../api/mes/reporting'
import type { ProductionReport } from '../../api/mes/reporting'

export const useMesReportingStore = defineStore('mesReporting', {
  state: () => ({
    productionReports: [] as ProductionReport[],
    loading: {
      productionReports: false
    },
    error: ''
  }),

  getters: {
    // 获取所有已上报的报工记录
    reportedReports: (state) => state.productionReports.filter(report => report.status === 'reported'),
    // 获取所有已验证的报工记录
    verifiedReports: (state) => state.productionReports.filter(report => report.status === 'verified'),
    // 获取所有已批准的报工记录
    approvedReports: (state) => state.productionReports.filter(report => report.status === 'approved')
  },

  actions: {
    // 获取生产报工列表
    async fetchProductionReports() {
      this.loading.productionReports = true
      this.error = ''
      try {
        const response = await reportingApi.getProductionReports()
        this.productionReports = response.data || []
      } catch (err: any) {
        this.error = err.message || '获取生产报工列表失败'
      } finally {
        this.loading.productionReports = false
      }
    },

    // 创建生产报工
    async createProductionReport(reportData: Partial<ProductionReport>) {
      try {
        const response = await reportingApi.createProductionReport(reportData)
        if (response.data) {
          this.productionReports.push(response.data)
          return response.data
        }
        return null
      } catch (err: any) {
        this.error = err.message || '创建生产报工失败'
        throw err
      }
    },

    // 获取报工详情
    async fetchProductionReportDetail(id: string) {
      try {
        const response = await reportingApi.getProductionReportDetail(id)
        if (response.data) {
          const index = this.productionReports.findIndex(report => report.id === id)
          if (index !== -1) {
            this.productionReports[index] = response.data
          } else {
            this.productionReports.push(response.data)
          }
        }
      } catch (err: any) {
        this.error = err.message || '获取报工详情失败'
      }
    },

    // 更新报工状态
    async updateProductionReportStatus(id: string, status: string) {
      try {
        // 验证状态流转规则：已上报 → 已验证 → 已批准
        const report = this.productionReports.find(item => item.id === id)
        if (report) {
          // 检查状态流转是否合法
          const validTransitions: Record<string, string[]> = {
            reported: ['verified'], // 已上报可以转为已验证
            verified: ['approved'], // 已验证可以转为已批准
            approved: [] // 已批准不能再转换
          }
          
          // 获取当前状态的有效转换
          const transitions = validTransitions[report.status]
          // 确保转换存在且包含目标状态
          if (transitions && !transitions.includes(status)) {
            throw new Error(`非法的状态流转：${report.status} → ${status}`)
          }
        }
        
        const response = await reportingApi.updateProductionReportStatus(id, status)
        if (response.data && response.data.id) {
          const index = this.productionReports.findIndex(report => report.id === id)
          if (index !== -1) {
            this.productionReports[index] = response.data as ProductionReport
          }
          return response.data
        }
        return null
      } catch (err: any) {
        this.error = err.message || '更新报工状态失败'
        throw err
      }
    }
  }
})
