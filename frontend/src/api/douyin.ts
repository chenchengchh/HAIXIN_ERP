import type { DouyinTask } from '../views/scrm/acquisition-active/douyin/types'
import api, { unwrapListResponse, unwrapResponseData } from './index'

const apiPrefix = '/api/v1/douyin'

export interface LoginStatus {
  isLogged: boolean
  qrCodeUrl?: string
  nickname?: string
}

export const douyinApi = {
  // 获取任务列表
  getTasks: async (): Promise<DouyinTask[]> => {
    const res = await api.get(`${apiPrefix}/tasks`)
    return unwrapListResponse<DouyinTask>(res)
  },

  // 创建任务
  createTask: async (task: Partial<DouyinTask>): Promise<DouyinTask> => {
    const res = await api.post(`${apiPrefix}/tasks`, task)
    return unwrapResponseData<DouyinTask>(res) as DouyinTask
  },

  // 更新任务
  updateTask: async (id: string, task: Partial<DouyinTask>): Promise<DouyinTask> => {
    const res = await api.put(`${apiPrefix}/tasks/${id}`, task)
    return unwrapResponseData<DouyinTask>(res) as DouyinTask
  },

  // 删除任务
  deleteTask: async (id: string): Promise<void> => {
    await api.delete(`${apiPrefix}/tasks/${id}`)
  },

  // 启动任务
  startTask: async (id: string): Promise<void> => {
    await api.post(`${apiPrefix}/tasks/${id}/start`)
  },

  // 停止任务
  stopTask: async (id: string): Promise<void> => {
    await api.post(`${apiPrefix}/tasks/${id}/stop`)
  },
  
  // 获取任务视频
  getTaskVideos: async (id: string): Promise<any[]> => {
    const res = await api.get(`${apiPrefix}/tasks/${id}/videos`)
    return unwrapListResponse<any>(res)
  },

  // 获取任务客户
  getTaskCustomers: async (id: string): Promise<any[]> => {
    const res = await api.get(`${apiPrefix}/tasks/${id}/customers`)
    return unwrapListResponse<any>(res)
  },

  // 检查登录状态
  checkLoginStatus: async (): Promise<LoginStatus> => {
    const res = await api.get(`${apiPrefix}/login/status`)
    return unwrapResponseData<LoginStatus>(res, { isLogged: false }) as LoginStatus
  },

  // 客户转化
  convertCustomer: async (id: string): Promise<void> => {
    await api.post(`${apiPrefix}/customers/${id}/convert`)
  },

  // 更新客户状态
  updateCustomerStatus: async (id: string, status: string): Promise<void> => {
    await api.put(`${apiPrefix}/customers/${id}/status`, { status })
  }
}
