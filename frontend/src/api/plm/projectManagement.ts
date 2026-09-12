/**
 * PLM项目管理API服务
 * @author author
 * @date 2025-12-30
 */

import api from '../index'
import type { Project, GanttTask, ResourceLoad } from '@/stores/plm/projectManagement'

// 定义API路径前缀
const API_PREFIX = '/api/v1/plm'

// 项目管理相关接口
export const projectManagementApi = {
  /**
   * 获取项目列表
   * @param params 查询参数（支持page, size分页参数）
   * @returns 项目列表
   */
  getProjects: (params?: { page?: number; size?: number; keyword?: string; status?: string }) => {
    return api.get(`${API_PREFIX}/projects`, { params })
  },
  
  /**
   * 获取项目详情
   * @param id 项目ID
   * @returns 项目详情
   */
  getProjectById: (id: string) => {
    return api.get(`${API_PREFIX}/projects/${id}`)
  },
  
  /**
   * 创建项目
   * @param data 项目数据
   * @returns 创建后的项目
   */
  createProject: (data: Omit<Project, 'id'>) => {
    return api.post(`${API_PREFIX}/projects`, data)
  },
  
  /**
   * 更新项目
   * @param id 项目ID
   * @param data 更新数据
   * @returns 更新后的项目
   */
  updateProject: (id: string, data: Partial<Project>) => {
    return api.put(`${API_PREFIX}/projects/${id}`, data)
  },
  
  /**
   * 删除项目
   * @param id 项目ID
   * @returns 删除结果
   */
  deleteProject: (id: string) => {
    return api.delete(`${API_PREFIX}/projects/${id}`)
  },
  
  /**
   * 获取项目甘特图数据
   * @param projectId 项目ID
   * @returns 甘特图数据（任务和链接）
   */
  getGanttData: (projectId: string) => {
    return api.get(`${API_PREFIX}/projects/${projectId}/gantt`)
  },
  
  /**
   * 更新项目任务
   * @param projectId 项目ID
   * @param data 任务数据
   * @returns 更新结果
   */
  updateTasks: (projectId: string, data: { tasks: GanttTask[]; links: Array<{ id: number; source: number; target: number; type: string }> }) => {
    return api.post(`${API_PREFIX}/projects/${projectId}/tasks`, data)
  },
  
  /**
   * 更新任务进度
   * @param taskId 任务ID
   * @param data 进度数据
   * @returns 更新结果
   */
  updateTaskStatus: (taskId: string, data: { progress: number; status: string }) => {
    return api.put(`${API_PREFIX}/tasks/${taskId}/status`, data)
  },
  
  /**
   * 获取资源负载数据
   * @param params 查询参数
   * @returns 资源负载数据
   */
  getResourceLoad: (params?: { projectId?: string; resourceId?: string; startDate?: string; endDate?: string }) => {
    return api.get(`${API_PREFIX}/resource-load`, { params })
  }
}
