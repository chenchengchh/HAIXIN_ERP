import { defineStore } from 'pinia'
import * as dataCollectionApi from '../../api/mes/data-collection'
import type { ManualReporting, EquipmentData, QualityInspection } from '../../api/mes/data-collection'

export const useMesDataCollectionStore = defineStore('mesDataCollection', {
  state: () => ({
    manualReportings: [] as ManualReporting[],
    equipmentData: [] as EquipmentData[],
    qualityInspections: [] as QualityInspection[],
    loading: {
      manualReportings: false,
      equipmentData: false,
      qualityInspections: false
    },
    error: ''
  }),

  getters: {
    // 获取所有已提交的人工报工
    submittedManualReportings: (state) => state.manualReportings.filter(mr => mr.status === 'submitted'),
    // 获取所有异常设备数据
    abnormalEquipmentData: (state) => state.equipmentData.filter(ed => ed.status === 'warning' || ed.status === 'alarm'),
    // 获取所有不合格质量检验
    failedQualityInspections: (state) => state.qualityInspections.filter(qi => qi.result === 'fail')
  },

  actions: {
    // 获取人工报工采集列表
    async fetchManualReportings() {
      this.loading.manualReportings = true
      this.error = ''
      try {
        const response = await dataCollectionApi.getManualReportings()
        this.manualReportings = response.data || []
      } catch (err: any) {
        this.error = err.message || '获取人工报工采集列表失败'
      } finally {
        this.loading.manualReportings = false
      }
    },

    // 创建人工报工采集
    async createManualReporting(reportingData: Partial<ManualReporting>) {
      try {
        const response = await dataCollectionApi.createManualReporting(reportingData)
        this.manualReportings.push(response.data)
        return response.data
      } catch (err: any) {
        this.error = err.message || '创建人工报工采集失败'
        throw err
      }
    },

    async updateManualReportingStatus(id: string, status: string) {
      try {
        const response = await dataCollectionApi.updateManualReportingStatus(id, status)
        const index = this.manualReportings.findIndex(mr => mr.id === id)
        if (index !== -1) {
          this.manualReportings[index] = response.data
        }
        return response.data
      } catch (err: any) {
        this.error = err.message || '更新人工报工状态失败'
        throw err
      }
    },

    // 获取设备数据自动采集列表
    async fetchEquipmentData() {
      this.loading.equipmentData = true
      this.error = ''
      try {
        const response = await dataCollectionApi.getEquipmentData()
        this.equipmentData = response.data || []
      } catch (err: any) {
        this.error = err.message || '获取设备数据自动采集列表失败'
      } finally {
        this.loading.equipmentData = false
      }
    },

    // 获取实时设备数据
    async fetchRealTimeEquipmentData(equipmentId: string) {
      try {
        const response = await dataCollectionApi.getRealTimeEquipmentData(equipmentId)
        // 更新或添加实时设备数据
        response.data.forEach(data => {
          const index = this.equipmentData.findIndex(ed => ed.id === data.id)
          if (index !== -1) {
            this.equipmentData[index] = data
          } else {
            this.equipmentData.push(data)
          }
        })
        return response.data
      } catch (err: any) {
        this.error = err.message || '获取实时设备数据失败'
        throw err
      }
    },

    // 获取质量检验数据采集列表
    async fetchQualityInspections() {
      this.loading.qualityInspections = true
      this.error = ''
      try {
        const response = await dataCollectionApi.getQualityInspections()
        this.qualityInspections = response.data || []
      } catch (err: any) {
        this.error = err.message || '获取质量检验数据采集列表失败'
      } finally {
        this.loading.qualityInspections = false
      }
    },

    // 创建质量检验数据采集
    async createQualityInspection(inspectionData: Partial<QualityInspection>) {
      try {
        const response = await dataCollectionApi.createQualityInspection(inspectionData)
        this.qualityInspections.push(response.data)
        return response.data
      } catch (err: any) {
        this.error = err.message || '创建质量检验数据采集失败'
        throw err
      }
    },

    // 获取质量检验详情
    async fetchQualityInspectionDetail(id: string) {
      try {
        const response = await dataCollectionApi.getQualityInspectionDetail(id)
        const index = this.qualityInspections.findIndex(qi => qi.id === id)
        if (index !== -1) {
          this.qualityInspections[index] = response.data
        } else {
          this.qualityInspections.push(response.data)
        }
      } catch (err: any) {
        this.error = err.message || '获取质量检验详情失败'
      }
    },

    // 更新质量检验状态
    async updateQualityInspectionStatus(id: string, status: string) {
      try {
        const response = await dataCollectionApi.updateQualityInspectionStatus(id, status)
        const index = this.qualityInspections.findIndex(qi => qi.id === id)
        if (index !== -1) {
          this.qualityInspections[index] = response.data
        }
        return response.data
      } catch (err: any) {
        this.error = err.message || '更新质量检验状态失败'
        throw err
      }
    }
  }
})
