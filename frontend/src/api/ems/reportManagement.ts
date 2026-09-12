/**
 * EMS报表管理模块API服务
 * @author author
 * @date 2025-12-31
 */

import api from '../index'

// 定义API路径前缀
const API_PREFIX = '/api/v1/ems'

// 标准报表类型定义
export interface StandardReport {
  id: number
  name: string
  type: string
  frequency: string
  lastGenerated: string
  status: 'active' | 'inactive'
  createdAt: string
  updatedAt: string
}

// 自定义报表类型定义
export interface CustomReport {
  id: number
  name: string
  creator: string
  createdDate: string
  lastModified: string
  sqlQuery?: string
  templateConfig?: any
}

// 自动生成任务类型定义
export interface AutoGenerationTask {
  id: number
  reportName: string
  frequency: string
  nextExecution: string
  recipients: string
  status: 'enabled' | 'disabled'
  createdAt: string
  updatedAt: string
}

// 报表导出历史类型定义
export interface ExportHistory {
  id: number
  reportName: string
  format: 'excel' | 'pdf' | 'csv'
  exportTime: string
  status: 'success' | 'failed'
  downloadUrl?: string
}

// 报表管理相关API
export const reportManagementApi = {
  /**
   * 获取标准报表列表
   * @param params 查询参数
   * @returns 标准报表列表
   */
  getStandardReports: (params?: { status?: 'active' | 'inactive' }) => {
    return api.get(`${API_PREFIX}/reports/standard`, { params })
  },
  
  /**
   * 生成标准报表
   * @param id 报表ID
   * @param params 生成参数
   * @returns 生成结果
   */
  generateStandardReport: (id: number, params?: { dateRange?: string[]; format?: string }) => {
    return api.post(`${API_PREFIX}/reports/standard/${id}/generate`, params)
  },
  
  /**
   * 下载报表
   * @param id 报表ID
   * @param format 下载格式
   * @returns 报表文件流
   */
  downloadReport: (id: number, format: 'excel' | 'pdf' | 'csv') => {
    return api.get(`${API_PREFIX}/reports/${id}/download`, { params: { format }, responseType: 'blob' })
  },
  
  /**
   * 获取自定义报表列表
   * @param params 查询参数
   * @returns 自定义报表列表
   */
  getCustomReports: (params?: { creator?: string; page?: number; size?: number }) => {
    return api.get(`${API_PREFIX}/reports/custom`, { params })
  },
  
  /**
   * 创建自定义报表
   * @param data 报表数据
   * @returns 创建后的报表
   */
  createCustomReport: (data: Omit<CustomReport, 'id' | 'createdDate' | 'lastModified'>) => {
    return api.post(`${API_PREFIX}/reports/custom`, data)
  },
  
  /**
   * 更新自定义报表
   * @param id 报表ID
   * @param data 更新数据
   * @returns 更新后的报表
   */
  updateCustomReport: (id: number, data: Partial<CustomReport>) => {
    return api.put(`${API_PREFIX}/reports/custom/${id}`, data)
  },
  
  /**
   * 删除自定义报表
   * @param id 报表ID
   * @returns 删除结果
   */
  deleteCustomReport: (id: number) => {
    return api.delete(`${API_PREFIX}/reports/custom/${id}`)
  },
  
  /**
   * 执行自定义报表
   * @param id 报表ID
   * @param params 执行参数
   * @returns 执行结果
   */
  executeCustomReport: (id: number, params?: { dateRange?: string[]; format?: string }) => {
    return api.post(`${API_PREFIX}/reports/custom/${id}/execute`, params, { responseType: 'blob' })
  },
  
  /**
   * 获取自动生成任务列表
   * @param params 查询参数
   * @returns 自动生成任务列表
   */
  getAutoGenerationTasks: (params?: { status?: 'enabled' | 'disabled'; page?: number; size?: number }) => {
    return api.get(`${API_PREFIX}/reports/auto-tasks`, { params })
  },
  
  /**
   * 创建自动生成任务
   * @param data 任务数据
   * @returns 创建后的任务
   */
  createAutoGenerationTask: (data: Omit<AutoGenerationTask, 'id' | 'createdAt' | 'updatedAt'>) => {
    return api.post(`${API_PREFIX}/reports/auto-tasks`, data)
  },
  
  /**
   * 更新自动生成任务
   * @param id 任务ID
   * @param data 更新数据
   * @returns 更新后的任务
   */
  updateAutoGenerationTask: (id: number, data: Partial<AutoGenerationTask>) => {
    return api.put(`${API_PREFIX}/reports/auto-tasks/${id}`, data)
  },
  
  /**
   * 切换自动生成任务状态
   * @param id 任务ID
   * @param status 切换状态
   * @returns 切换结果
   */
  toggleTaskStatus: (id: number, status: 'enabled' | 'disabled') => {
    return api.put(`${API_PREFIX}/reports/auto-tasks/${id}/status`, { status })
  },
  
  /**
   * 获取报表导出历史
   * @param params 查询参数
   * @returns 报表导出历史列表
   */
  getExportHistory: (params?: { reportName?: string; status?: 'success' | 'failed'; dateRange?: string[]; page?: number; size?: number }) => {
    return api.get(`${API_PREFIX}/reports/export-history`, { params })
  },
  
  /**
   * 立即导出报表
   * @param data 导出参数
   * @returns 导出结果
   */
  exportReport: (data: { reportId: number; format: 'excel' | 'pdf' | 'csv'; dateRange?: string[]; range?: 'all' | 'specific' }) => {
    return api.post(`${API_PREFIX}/reports/export`, data)
  },
  
  /**
   * 从导出历史下载报表
   * @param id 导出历史ID
   * @returns 报表文件流
   */
  downloadFromHistory: (id: number) => {
    return api.get(`${API_PREFIX}/reports/export-history/${id}/download`, { responseType: 'blob' })
  }
}

// 导入共享虚拟数据配置
import { generateRandomTime } from './mockDataConfig'

// 模拟数据生成器
const generateMockStandardReports = (): StandardReport[] => {
  const reportTypes = [
    { name: '能耗日报', type: '日报', frequency: '每日' },
    { name: '能耗月报', type: '月报', frequency: '每月' },
    { name: '能耗年报', type: '年报', frequency: '每年' },
    { name: '设备能耗分析报告', type: '分析报告', frequency: '每月' },
    { name: '区域能耗对比报告', type: '对比报告', frequency: '每月' },
    { name: '节能潜力分析报告', type: '分析报告', frequency: '每月' },
    { name: '优化效果评估报告', type: '评估报告', frequency: '季度' },
    { name: '异常能耗分析报告', type: '分析报告', frequency: '每周' }
  ]
  
  return reportTypes.map((report, index) => {
    // 计算最后生成时间
    let lastGenerated: string
    if (report.frequency === '每日') {
      lastGenerated = new Date().toISOString().split('T')[0] + ' 08:00:00' // 今天
    } else if (report.frequency === '每周') {
      lastGenerated = new Date(Date.now() - (new Date().getDay() || 7) * 86400000).toISOString().split('T')[0] + ' 09:00:00' // 本周一
    } else if (report.frequency === '每月') {
      lastGenerated = new Date(new Date().getFullYear(), new Date().getMonth(), 1).toISOString().split('T')[0] + ' 10:00:00' // 本月1日
    } else {
      lastGenerated = new Date(new Date().getFullYear(), 0, 1).toISOString().split('T')[0] + ' 10:00:00' // 本年1月1日
    }
    
    return {
      id: index + 1,
      name: report.name,
      type: report.type,
      frequency: report.frequency,
      lastGenerated,
      status: Math.random() > 0.1 ? 'active' : 'inactive', // 90%激活，10%未激活
      createdAt: '2025-01-01',
      updatedAt: new Date().toISOString().split('T')[0] || '2025-12-31' // 今天
    }
  })
}

const generateMockCustomReports = (): CustomReport[] => {
  const customReportNames = [
    '车间能耗分析',
    '设备能耗统计',
    '部门能耗对比',
    '生产班组能耗排名',
    '重点设备能耗监控',
    '节能改造效果跟踪',
    '异常能耗趋势分析'
  ]
  
  const creators = ['管理员', '工程师', '能源主管', '生产经理']
  
  return customReportNames.map((name, index) => {
    // 生成创建日期和最后修改日期
    const createdDate = new Date(Date.now() - (index * 15 + Math.random() * 10) * 86400000).toISOString().split('T')[0] || '2025-12-31'
    const lastModified = new Date(Date.now() - Math.random() * 10 * 86400000).toISOString().split('T')[0] || '2025-12-31'
    
    return {
      id: index + 1,
      name,
      creator: creators[Math.floor(Math.random() * creators.length)] || '管理员',
      createdDate,
      lastModified
    }
  })
}

const generateMockAutoGenerationTasks = (standardReports: StandardReport[]): AutoGenerationTask[] => {
  const tasks: AutoGenerationTask[] = []
  
  // 为部分标准报表创建自动生成任务
  standardReports.slice(0, 5).forEach((report, index) => {
    // 生成执行时间
    let frequency: string
    let nextExecution: string
    
    if (report.frequency === '每日') {
      frequency = '每日 08:00'
      nextExecution = new Date(Date.now() + 86400000).toISOString().split('T')[0] + ' 08:00:00' // 明天
    } else if (report.frequency === '每周') {
      frequency = '每周一 09:00'
      const daysUntilMonday = (8 - new Date().getDay()) % 7 || 7
      nextExecution = new Date(Date.now() + daysUntilMonday * 86400000).toISOString().split('T')[0] + ' 09:00:00' // 下周一
    } else if (report.frequency === '每月') {
      frequency = '每月1日 10:00'
      nextExecution = new Date(new Date().getFullYear(), new Date().getMonth() + 1, 1).toISOString().split('T')[0] + ' 10:00:00' // 下月1日
    } else {
      frequency = '每年1月1日 10:00'
      nextExecution = new Date(new Date().getFullYear() + 1, 0, 1).toISOString().split('T')[0] + ' 10:00:00' // 明年1月1日
    }
    
    // 生成收件人
    const recipients = ['admin@example.com', 'engineer@example.com', 'energy@example.com']
      .slice(0, Math.floor(Math.random() * 3) + 1)
      .join(', ')
    
    tasks.push({
      id: index + 1,
      reportName: report.name,
      frequency,
      nextExecution,
      recipients,
      status: Math.random() > 0.2 ? 'enabled' : 'disabled', // 80%启用，20%禁用
      createdAt: '2025-01-01',
      updatedAt: new Date().toISOString().split('T')[0] || '2025-12-31' // 今天
    })
  })
  
  return tasks
}

const generateMockExportHistory = (standardReports: StandardReport[]): ExportHistory[] => {
  const history: ExportHistory[] = []
  const formats: ('excel' | 'pdf' | 'csv')[] = ['excel', 'pdf', 'csv']
  
  // 生成最近30天的导出历史
  for (let i = 0; i < 20; i++) {
    // 随机选择报表，确保不为undefined
    const report = standardReports[Math.floor(Math.random() * standardReports.length)] || standardReports[0]
    // 随机选择格式，确保不为undefined
    const format = formats[Math.floor(Math.random() * formats.length)] || 'excel'
    
    history.push({
      id: i + 1,
      reportName: report?.name || '能耗日报',
      format,
      exportTime: generateRandomTime(new Date(Date.now() - i * 86400000).toISOString().split('T')[0]),
      status: Math.random() > 0.05 ? 'success' : 'failed' // 95%成功，5%失败
    })
  }
  
  return history
}

// 生成模拟数据
const mockStandardReports: StandardReport[] = generateMockStandardReports()
const mockCustomReports: CustomReport[] = generateMockCustomReports()
const mockAutoGenerationTasks: AutoGenerationTask[] = generateMockAutoGenerationTasks(mockStandardReports)
const mockExportHistory: ExportHistory[] = generateMockExportHistory(mockStandardReports)

// 导出模拟数据，用于前端模拟
export { mockStandardReports, mockCustomReports, mockAutoGenerationTasks, mockExportHistory }
