/**
 * PLM项目管理API服务
 * - 统一网关前缀：/plm/**
 * @author author
 * @date 2026-02-01
 */

import api from '../index'
import type { Project, GanttTask, ResourceLoad } from '@/stores/plm/projectManagement'

const API_PREFIX = '/api/v1/plm'

export const projectApi = {
  /**
   * 获取项目列表
   * @param params 查询参数（支持page, size, keyword, status）
   */
  getProjects: (params?: { page?: number; size?: number; keyword?: string; status?: string }) => {
    return api.get(`${API_PREFIX}/projects`, { params })
  },

  /**
   * 获取项目详情
   * @param id 项目ID
   */
  getProjectById: (id: string) => {
    return api.get(`${API_PREFIX}/projects/${id}`)
  },

  /**
   * 创建项目
   * @param data 项目数据
   */
  createProject: (data: Omit<Project, 'id'>) => {
    return api.post(`${API_PREFIX}/projects`, data)
  },

  /**
   * 更新项目
   * @param id 项目ID
   * @param data 更新数据
   */
  updateProject: (id: string, data: Partial<Project>) => {
    return api.put(`${API_PREFIX}/projects/${id}`, data)
  },

  /**
   * 删除项目
   * @param id 项目ID
   */
  deleteProject: (id: string) => {
    return api.delete(`${API_PREFIX}/projects/${id}`)
  },

  /**
   * 获取项目甘特图数据
   * @param projectId 项目ID
   */
  getGanttData: (projectId: string) => {
    return api.get(`${API_PREFIX}/projects/${projectId}/gantt`)
  },

  /**
   * 批量更新项目任务（甘特图）
   * @param projectId 项目ID
   * @param data 任务与链接数据
   */
  updateTasks: (
    projectId: string,
    data: { tasks: GanttTask[]; links: Array<{ id: number; source: number; target: number; type: string }> }
  ) => {
    return api.post(`${API_PREFIX}/projects/${projectId}/tasks`, data)
  },

  /**
   * 更新任务进度与状态
   * @param taskId 任务ID
   * @param data 进度与状态
   */
  updateTaskStatus: (taskId: string, data: { progress: number; status: string }) => {
    return api.put(`${API_PREFIX}/tasks/${taskId}/status`, data)
  },

  /**
   * 获取资源负载
   * @param params 查询参数
   */
  getResourceLoad: (params?: { projectId?: string; resourceId?: string; startDate?: string; endDate?: string }) => {
    return api.get(`${API_PREFIX}/resource-load`, { params })
  }
}
