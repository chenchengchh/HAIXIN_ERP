/**
 * EMS能源采集模块API服务
 * @author author
 * @date 2025-12-31
 */

import api from '../index'

// 定义API路径前缀
const API_PREFIX = '/api/v1/ems'

// 实时数据采集类型定义
export interface RealTimeData {
  id: number
  energyType: string
  area: string
  actualValue: number
  unit: string
  collectionTime: string
  status: 'normal' | 'abnormal'
}

// 采集设备类型定义
export interface MeterDevice {
  id: number
  name: string
  type: string
  ipAddress: string
  status: 'online' | 'offline'
  lastUpdate: string
}

// 数据校准记录类型定义
export interface CalibrationRecord {
  id: number
  meterName: string
  rawValue: number
  calibratedValue: number
  coefficient: number
  calibrationTime: string
}

// 能源采集相关API
export const energyCollectionApi = {
  /**
   * 获取实时数据采集列表
   * @param params 查询参数
   * @returns 实时数据列表
   */
  getRealTimeData: (params?: { energyType?: string; area?: string; limit?: number }) => {
    return api.get(`${API_PREFIX}/collection/real-time`, { params })
  },
  
  /**
   * 获取采集设备列表
   * @param params 查询参数
   * @returns 采集设备列表
   */
  getMeterDevices: (params?: { type?: string; status?: string; page?: number; size?: number }) => {
    return api.get(`${API_PREFIX}/collection/devices`, { params })
  },
  
  /**
   * 获取采集设备详情
   * @param id 设备ID
   * @returns 设备详情
   */
  getMeterDeviceById: (id: number) => {
    return api.get(`${API_PREFIX}/collection/devices/${id}`)
  },
  
  /**
   * 添加采集设备
   * @param data 设备数据
   * @returns 添加后的设备
   */
  addMeterDevice: (data: Omit<MeterDevice, 'id' | 'lastUpdate'>) => {
    return api.post(`${API_PREFIX}/collection/devices`, data)
  },
  
  /**
   * 更新采集设备
   * @param id 设备ID
   * @param data 更新数据
   * @returns 更新后的设备
   */
  updateMeterDevice: (id: number, data: Partial<MeterDevice>) => {
    return api.put(`${API_PREFIX}/collection/devices/${id}`, data)
  },
  
  /**
   * 删除采集设备
   * @param id 设备ID
   * @returns 删除结果
   */
  deleteMeterDevice: (id: number) => {
    return api.delete(`${API_PREFIX}/collection/devices/${id}`)
  },
  
  /**
   * 执行数据校准
   * @param data 校准数据
   * @returns 校准结果
   */
  executeCalibration: (data: { meterId: number; coefficient: number; reason: string }) => {
    return api.post(`${API_PREFIX}/collection/calibrate`, data)
  },
  
  /**
   * 获取校准历史记录
   * @param params 查询参数
   * @returns 校准历史记录列表
   */
  getCalibrationHistory: (params?: { meterId?: number; page?: number; size?: number }) => {
    return api.get(`${API_PREFIX}/collection/calibration-history`, { params })
  }
}

// 导入共享虚拟数据配置
import { energyTypes, areas, meterDevices, dateRanges, generateRandomTime, generateRandomValue } from './mockDataConfig'

// 模拟数据生成器
const generateMockRealTimeData = (): RealTimeData[] => {
  const data: RealTimeData[] = []
  let id = 1
  
  // 为每个区域生成不同能源类型的实时数据
  areas.forEach(area => {
    Object.values(energyTypes).forEach(energyType => {
      // 生成当前时间的实时数据
      const now = new Date().toISOString().split('T')[0] + ' 10:00:00'
      
      data.push({
        id: id++,
        energyType: energyType.name,
        area: area.name,
        actualValue: generateRandomValue(20, 200),
        unit: energyType.unit,
        collectionTime: now,
        status: Math.random() > 0.1 ? 'normal' : 'abnormal' // 90%正常，10%异常
      })
    })
  })
  
  return data
}

const generateMockMeterDevices = (): MeterDevice[] => {
  return meterDevices.map(device => ({
    id: device.id,
    name: device.name,
    type: device.type,
    ipAddress: device.ipAddress,
    status: device.status as 'online' | 'offline',
    lastUpdate: generateRandomTime()
  }))
}

const generateMockCalibrationHistory = (): CalibrationRecord[] => {
  const history: CalibrationRecord[] = []
  
  // 为部分设备生成校准记录
  meterDevices.slice(0, 6).forEach((device, index) => {
    // 生成2-3条校准记录
    const recordCount = Math.floor(Math.random() * 2) + 2
    
    for (let i = 0; i < recordCount; i++) {
      const rawValue = generateRandomValue(50, 150)
      const coefficient = generateRandomValue(0.9, 1.1, 2)
      
      history.push({
        id: index * 10 + i + 1,
        meterName: device.name,
        rawValue: rawValue,
        calibratedValue: parseFloat((rawValue * coefficient).toFixed(1)),
        coefficient: coefficient,
        calibrationTime: generateRandomTime(new Date(Date.now() - i * 86400000).toISOString().split('T')[0])
      })
    }
  })
  
  return history
}

// 生成模拟数据
const mockRealTimeData: RealTimeData[] = generateMockRealTimeData()
const mockMeterDevices: MeterDevice[] = generateMockMeterDevices()
const mockCalibrationHistory: CalibrationRecord[] = generateMockCalibrationHistory()

// 导出模拟数据，用于前端模拟
export { mockRealTimeData, mockMeterDevices, mockCalibrationHistory }
