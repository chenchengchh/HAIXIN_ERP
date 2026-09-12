/**
 * EMS模块共享虚拟数据配置
 * 定义各子模块共用的基础数据，确保数据一致性和关联性
 * @author author
 * @date 2025-12-31
 */

// 共享的能源类型定义
export const energyTypes = {
  ELECTRICITY: { name: '电力', code: 'electricity', unit: 'kW' },
  WATER: { name: '水资源', code: 'water', unit: 'm³/h' },
  GAS: { name: '燃气', code: 'gas', unit: 'm³/h' },
  HEAT: { name: '热能', code: 'heat', unit: 'GJ/h' }
}

// 共享的区域定义
export const areas = [
  { id: 1, name: '车间1', code: 'workshop-1' },
  { id: 2, name: '车间2', code: 'workshop-2' },
  { id: 3, name: '车间3', code: 'workshop-3' },
  { id: 4, name: '办公室', code: 'office' }
]

// 共享的设备定义
export const meterDevices = [
  { id: 1, name: '智能电表-001', type: '电表', ipAddress: '192.168.1.101', status: 'online', areaId: 1 },
  { id: 2, name: '智能水表-001', type: '水表', ipAddress: '192.168.1.102', status: 'online', areaId: 2 },
  { id: 3, name: '智能气表-001', type: '气表', ipAddress: '192.168.1.103', status: 'offline', areaId: 3 },
  { id: 4, name: '智能热表-001', type: '热表', ipAddress: '192.168.1.104', status: 'online', areaId: 3 },
  { id: 5, name: '智能电表-002', type: '电表', ipAddress: '192.168.1.105', status: 'online', areaId: 4 },
  { id: 6, name: '智能电表-003', type: '电表', ipAddress: '192.168.1.106', status: 'online', areaId: 1 },
  { id: 7, name: '智能水表-002', type: '水表', ipAddress: '192.168.1.107', status: 'online', areaId: 1 },
  { id: 8, name: '智能气表-002', type: '气表', ipAddress: '192.168.1.108', status: 'online', areaId: 2 }
]

// 共享的日期范围定义
export const dateRanges = {
  TODAY: { start: new Date().toISOString().split('T')[0] + ' 00:00:00', end: new Date().toISOString().split('T')[0] + ' 23:59:59' },
  YESTERDAY: {
    start: new Date(Date.now() - 86400000).toISOString().split('T')[0] + ' 00:00:00',
    end: new Date(Date.now() - 86400000).toISOString().split('T')[0] + ' 23:59:59'
  },
  THIS_WEEK: {
    start: new Date(Date.now() - (new Date().getDay() || 7) * 86400000).toISOString().split('T')[0] + ' 00:00:00',
    end: new Date().toISOString().split('T')[0] + ' 23:59:59'
  },
  THIS_MONTH: {
    start: new Date(new Date().getFullYear(), new Date().getMonth(), 1).toISOString().split('T')[0] + ' 00:00:00',
    end: new Date().toISOString().split('T')[0] + ' 23:59:59'
  }
}

// 生成随机时间函数
export const generateRandomTime = (baseDate: string = new Date().toISOString().split('T')[0] || '2025-12-31', hoursRange: number[] = [0, 23]) => {
  const dateParts = baseDate.split('-')
  const year = dateParts[0] || '2025'
  const month = dateParts[1] || '12'
  const day = dateParts[2] || '31'
  
  const minHour = hoursRange[0] || 0
  const maxHour = hoursRange[1] || 23
  const hours = String(Math.floor(Math.random() * (maxHour - minHour + 1)) + minHour).padStart(2, '0')
  const minutes = String(Math.floor(Math.random() * 60)).padStart(2, '0')
  const seconds = String(Math.floor(Math.random() * 60)).padStart(2, '0')
  
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

// 生成随机数值函数
export const generateRandomValue = (min: number, max: number, decimalPlaces: number = 1) => {
  return parseFloat((Math.random() * (max - min) + min).toFixed(decimalPlaces))
}

// 导出所有共享数据
export default {
  energyTypes,
  areas,
  meterDevices,
  dateRanges,
  generateRandomTime,
  generateRandomValue
}