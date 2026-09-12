import { defineStore } from 'pinia'
import * as monitoringApi from '../../api/mes/monitoring'
import type { ProductionProgress, EquipmentStatus, ProcessParam } from '../../api/mes/monitoring'
import { useWebSocket } from '../../utils/websocket'
import { buildWsUrl } from '../../utils/runtime-url'

export const useMesMonitoringStore = defineStore('mesMonitoring', {
  state: () => ({
    productionProgress: [] as ProductionProgress[],
    equipmentStatus: [] as EquipmentStatus[],
    processParams: [] as ProcessParam[],
    loading: {
      productionProgress: false,
      equipmentStatus: false,
      processParams: false
    },
    error: '',
    wsConnected: false
  }),
  actions: {
    // 初始化WebSocket连接
    initWebSocket() {
      const wsUrl = buildWsUrl('/mes/monitoring')
      console.log(`WebSocket连接URL: ${wsUrl}`)
      
      // 重连配置
      const reconnectConfig = {
        maxReconnectAttempts: 3, // 减少重连次数
        reconnectInterval: 3000, // 减少重连间隔
        reconnectAttempts: 0
      }
      
      try {
        const ws = useWebSocket(wsUrl, {
          onOpen: () => {
            console.log('WebSocket连接已建立')
            this.wsConnected = true
            // 重置重连尝试次数
            reconnectConfig.reconnectAttempts = 0
          },
          onMessage: (message) => {
            try {
              const data = JSON.parse(message.data)
              this.handleWebSocketMessage(data)
            } catch (error) {
              console.error('解析WebSocket消息失败:', error)
            }
          },
          onClose: () => {
            console.log('WebSocket连接已关闭')
            this.wsConnected = false
            this.attemptReconnect(reconnectConfig)
          },
          onError: (error) => {
            console.error('WebSocket错误:', error)
            this.wsConnected = false
            this.attemptReconnect(reconnectConfig)
          }
        })
        return ws
      } catch (error) {
        console.error('WebSocket初始化失败:', error)
        this.wsConnected = false
        this.attemptReconnect(reconnectConfig)
        return null
      }
    },
    
    // WebSocket重连机制
    attemptReconnect(reconnectConfig: any) {
      if (reconnectConfig.reconnectAttempts < reconnectConfig.maxReconnectAttempts) {
        reconnectConfig.reconnectAttempts++
        console.log(`WebSocket尝试重连... (${reconnectConfig.reconnectAttempts}/${reconnectConfig.maxReconnectAttempts})`)
        
        setTimeout(() => {
          this.initWebSocket()
        }, reconnectConfig.reconnectInterval)
      } else {
        console.error('WebSocket重连失败，已达到最大尝试次数')
        // 1分钟后再次尝试连接，给服务器恢复时间
        setTimeout(() => {
          console.log('WebSocket尝试重新连接...')
          this.initWebSocket()
        }, 60000)
      }
    },

    // 处理WebSocket消息
    handleWebSocketMessage(data: any) {
      switch (data.type) {
        case 'production_progress_update':
          this.updateProductionProgress(data.payload)
          break
        case 'equipment_status_update':
          this.updateEquipmentStatus(data.payload)
          break
        case 'process_param_update':
          this.updateProcessParam(data.payload)
          break
        default:
          console.log('未知的WebSocket消息类型:', data.type)
      }
    },

    // 更新生产进度
    updateProductionProgress(progress: ProductionProgress) {
      const index = this.productionProgress.findIndex(p => p.id === progress.id)
      if (index !== -1) {
        this.productionProgress[index] = progress
      } else {
        this.productionProgress.push(progress)
      }
    },

    // 更新设备状态
    updateEquipmentStatus(equipment: EquipmentStatus) {
      const index = this.equipmentStatus.findIndex(e => e.equipmentId === equipment.equipmentId)
      if (index !== -1) {
        this.equipmentStatus[index] = equipment
      } else {
        this.equipmentStatus.push(equipment)
      }
    },

    // 更新工艺参数
    updateProcessParam(param: ProcessParam) {
      const index = this.processParams.findIndex(p => p.id === param.id)
      if (index !== -1) {
        this.processParams[index] = param
      } else {
        this.processParams.push(param)
      }
    },

    // 获取生产进度列表
    async fetchProductionProgress() {
      this.loading.productionProgress = true
      this.error = ''
      try {
        const response = await monitoringApi.getProductionProgress()
        this.productionProgress = response.data || []
      } catch (err: any) {
        this.error = err.message || '获取生产进度列表失败'
      } finally {
        this.loading.productionProgress = false
      }
    },

    // 获取设备状态列表
    async fetchEquipmentStatus() {
      this.loading.equipmentStatus = true
      this.error = ''
      try {
        const response = await monitoringApi.getEquipmentStatus()
        this.equipmentStatus = response.data || []
      } catch (err: any) {
        this.error = err.message || '获取设备状态列表失败'
      } finally {
        this.loading.equipmentStatus = false
      }
    },

    // 获取工艺参数列表
    async fetchProcessParams() {
      this.loading.processParams = true
      this.error = ''
      try {
        const response = await monitoringApi.getProcessParams()
        this.processParams = response.data || []
      } catch (err: any) {
        this.error = err.message || '获取工艺参数列表失败'
      } finally {
        this.loading.processParams = false
      }
    },

    // 获取设备详情
    async fetchEquipmentDetail(id: string) {
      try {
        const response = await monitoringApi.getEquipmentDetail(id)
        if (response.data) {
          const index = this.equipmentStatus.findIndex(es => es.equipmentId === id)
          if (index !== -1) {
            this.equipmentStatus[index] = response.data
          } else {
            this.equipmentStatus.push(response.data)
          }
        }
      } catch (err: any) {
        this.error = err.message || '获取设备详情失败'
      }
    },

    // 获取实时工艺参数
    async fetchRealTimeProcessParams(workstationId: string) {
      try {
        const response = await monitoringApi.getRealTimeProcessParams(workstationId)
        // 更新或添加实时工艺参数
        response.data.forEach(param => {
          const index = this.processParams.findIndex(p => p.id === param.id)
          if (index !== -1) {
            this.processParams[index] = param
          } else {
            this.processParams.push(param)
          }
        })
        return response.data
      } catch (err: any) {
        this.error = err.message || '获取实时工艺参数失败'
        throw err
      }
    }
  }
})
