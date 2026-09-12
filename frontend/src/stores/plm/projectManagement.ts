import { defineStore } from 'pinia'
import { unwrapListResponse, unwrapPageResponse, unwrapResponseData } from '@/api'
import { plmApi } from '@/api/plm'

// 项目类型定义
export interface Project {
  id: string
  name: string
  code: string
  status: string
  progress: number
  startDate: string
  endDate: string
  manager: string
  type: string
}

// 甘特图任务类型定义
export interface GanttTask {
  id: number
  text: string
  start_date: string
  duration: number
  progress: number
  parent: number
  type?: string
}

// 资源负载类型定义
export interface ResourceLoad {
  resourceId: string
  resourceName: string
  load: number
  date: string
}

// 任务类型定义
export interface Task {
  id: number
  name: string
  startDate: string
  endDate: string
  progress: number
  assignee: string
  status: string
  projectId: string
}


// 项目管理状态管理
// 管理研发项目的状态，包括项目列表、项目详情、甘特图数据等
export const useProjectManagementStore = defineStore('projectManagement', {
  state: () => ({
    // 项目列表数据
    projects: [] as Project[],
    // 选中的项目ID
    selectedProjectId: null as string | null,
    // 甘特图数据
    ganttData: { data: [] as GanttTask[], links: [] as Array<{ id: number; source: number; target: number; type: string }> },
    // 资源负载数据
    resourceLoad: [] as ResourceLoad[],
    // 项目任务数据
    tasks: [] as Task[],
    // 项目状态过滤
    statusFilter: 'all',
    // 项目搜索关键词
    searchKeyword: '',
    // 分页信息
    pagination: {
      page: 1,
      pageSize: 10,
      total: 0
    }
  }),

  getters: {
    // 获取筛选后的项目列表
    filteredProjects: (state) => {
      return state.projects.filter(project => {
        const matchesStatus = state.statusFilter === 'all' || state.statusFilter === '' || project.status === state.statusFilter
        const matchesKeyword = state.searchKeyword === '' || 
          project.name.toLowerCase().includes(state.searchKeyword.toLowerCase()) ||
          project.code.toLowerCase().includes(state.searchKeyword.toLowerCase())
        return matchesStatus && matchesKeyword
      })
    },

    // 获取当前选中的项目
    currentProject: (state) => {
      if (!state.selectedProjectId) return null
      return state.projects.find(project => project.id === state.selectedProjectId) || null
    },
    
    // 根据项目ID获取任务列表
    getTasksByProjectId: (state) => (projectId: string) => {
      return state.tasks.filter(task => task.projectId === projectId)
    },
    
    // 根据任务ID获取任务
    getTaskById: (state) => (taskId: number) => {
      return state.tasks.find(task => task.id === taskId) || null
    }
  },

  actions: {
    // 设置项目列表数据
    setProjects(projects: any[]) {
      this.projects = projects
      this.pagination.total = projects.length
    },

    // 设置选中的项目ID
    setSelectedProjectId(projectId: string) {
      this.selectedProjectId = projectId
    },

    // 设置甘特图数据
    setGanttData(data: { data: any[]; links: any[] }) {
      this.ganttData = data
    },

    // 设置资源负载数据
    setResourceLoad(data: any[]) {
      this.resourceLoad = data
    },

    // 设置状态过滤
    setStatusFilter(status: string) {
      this.statusFilter = status
    },

    // 设置搜索关键词
    setSearchKeyword(keyword: string) {
      this.searchKeyword = keyword
    },

    // 设置分页
    setPagination(page: number, pageSize: number) {
      this.pagination.page = page
      this.pagination.pageSize = pageSize
    },

    // 重置筛选条件
    resetFilters() {
      this.statusFilter = 'all'
      this.searchKeyword = ''
      this.pagination.page = 1
    },
    
    // 获取项目详情
    getProjectById(projectId: string) {
      return this.projects.find(project => project.id === projectId) || null
    },

    // 异步获取项目列表
    async fetchProjects() {
      try {
        const response: any = await plmApi.project.getProjects({
          page: this.pagination.page,
          size: this.pagination.pageSize,
          keyword: this.searchKeyword,
          status: this.statusFilter === 'all' ? '' : this.statusFilter
        })
        const page = unwrapPageResponse<any>(response)
        if (Array.isArray(page.list)) {
          this.setProjects(page.list)
          this.pagination.total = page.total || page.list.length
          return
        }
      } catch (error) {
      }
      this.setProjects([])
    },
    
    // 异步获取甘特图数据
    async fetchGanttData(projectId: string) {
      try {
        const response: any = await plmApi.project.getGanttData(projectId)
        const data = unwrapResponseData<any>(response)
        if (data?.data && data?.links) {
          this.setGanttData(data)
          return
        }
      } catch (error) {
      }
      this.setGanttData({ data: [], links: [] })
    },

    /**
     * 保存甘特图数据（任务+连线）
     * @param projectId 项目ID
     * @param ganttData 甘特图序列化数据
     */
    async saveGanttData(projectId: string, ganttData: { data: GanttTask[]; links: Array<{ id: number; source: number; target: number; type: string }> }) {
      try {
        const payload = {
          tasks: ganttData.data,
          links: ganttData.links
        }
        const response: any = await plmApi.project.updateTasks(projectId, payload)
        const data = unwrapResponseData<any>(response)
        if (data?.data && data?.links) {
          this.setGanttData(data)
        }
        return true
      } catch (error) {
        return false
      }
    },
    
    // 异步获取资源负载数据
    async fetchResourceLoad(params?: { resourceId?: string; startDate?: string; endDate?: string }) {
      try {
        const response: any = await plmApi.project.getResourceLoad({
          projectId: this.selectedProjectId || undefined,
          resourceId: params?.resourceId || undefined,
          startDate: params?.startDate || undefined,
          endDate: params?.endDate || undefined
        })
        const rows = unwrapListResponse<any>(response)
        if (Array.isArray(rows)) {
          this.setResourceLoad(rows)
          return
        }
      } catch (error) {
      }
      this.setResourceLoad([])
    },
    
    // 新增项目
    async addProject(projectData: Omit<Project, 'id'>) {
      try {
        const response: any = await plmApi.projectManagement.createProject(projectData)
        const newProject = unwrapResponseData<any>(response)
        if (newProject) {
          this.projects.push(newProject)
          this.pagination.total = this.projects.length
          return newProject
        }
        return null
      } catch (error) {
        console.error('新增项目失败:', error)
        return null
      }
    },
    
    // 更新项目
    async updateProject(projectId: string, projectData: Partial<Project>) {
      try {
        const response: any = await plmApi.projectManagement.updateProject(projectId, projectData)
        const updatedProject = unwrapResponseData<any>(response)
        if (updatedProject) {
          const projectIndex = this.projects.findIndex(project => project.id === projectId)
          if (projectIndex !== -1) {
            this.projects[projectIndex] = updatedProject
          }
          return updatedProject
        }
        return null
      } catch (error) {
        console.error('更新项目失败:', error)
        return null
      }
    },
    
    // 删除项目
    async deleteProject(projectId: string) {
      try {
        await plmApi.projectManagement.deleteProject(projectId)
        
        const projectIndex = this.projects.findIndex(project => project.id === projectId)
        if (projectIndex !== -1) {
          this.projects.splice(projectIndex, 1)
          this.pagination.total = this.projects.length
          
          // 如果删除的是当前选中的项目，重置选中项目ID
          if (this.selectedProjectId === projectId) {
            this.selectedProjectId = null
          }
        }
        return true
      } catch (error) {
        console.error('删除项目失败:', error)
        return false
      }
    },
    
    // 获取项目任务列表
    async fetchTasks(projectId: string) {
      try {
        await this.fetchGanttData(projectId)
        const addDays = (dateText: string, days: number) => {
          const date = new Date(dateText)
          date.setDate(date.getDate() + days)
          return date.toISOString().slice(0, 10)
        }
        const mapped = (this.ganttData.data || []).map(t => {
          const duration = Number(t.duration || 1)
          const start = t.start_date
          const end = addDays(start, Math.max(duration - 1, 0))
          const progress = Number(t.progress || 0)
          const status = progress >= 100 ? 'completed' : progress > 0 ? 'in-progress' : 'planning'
          return {
            id: t.id,
            name: t.text,
            startDate: start,
            endDate: end,
            progress,
            assignee: '',
            status,
            projectId
          } as Task
        })
        this.tasks = mapped
        return mapped
      } catch (error) {
        console.error('获取任务列表失败:', error)
        this.tasks = []
        return []
      }
    },
    
    // 添加任务
    async addTask(taskData: Omit<Task, 'id'>) {
      try {
        const projectId = taskData.projectId
        await this.fetchGanttData(projectId)
        const start = taskData.startDate
        const end = taskData.endDate
        const startDate = new Date(start)
        const endDate = new Date(end)
        const diffDays = Math.max(Math.round((endDate.getTime() - startDate.getTime()) / (24 * 60 * 60 * 1000)) + 1, 1)
        const id = Date.now()
        const newGanttTask: GanttTask = {
          id,
          text: taskData.name,
          start_date: start,
          duration: diffDays,
          progress: taskData.progress,
          parent: 0
        }
        const nextData = {
          data: [...(this.ganttData.data || []), newGanttTask],
          links: [...(this.ganttData.links || [])]
        }
        const saved = await this.saveGanttData(projectId, nextData)
        if (!saved) return null
        await this.fetchTasks(projectId)
        return this.tasks.find(t => t.id === id) || null
      } catch (error) {
        console.error('添加任务失败:', error)
        return null
      }
    },
    
    // 更新任务
    async updateTask(taskId: number, taskData: Partial<Task>) {
      try {
        const existing = this.tasks.find(t => t.id === taskId)
        const projectId = String(taskData.projectId || existing?.projectId || this.selectedProjectId || '')
        if (!projectId) return null
        await this.fetchGanttData(projectId)
        const ganttIndex = (this.ganttData.data || []).findIndex(t => t.id === taskId)
        if (ganttIndex === -1) return null
        const current = this.ganttData.data?.[ganttIndex]
        if (!current) return null
        const start = taskData.startDate || current.start_date
        const end = taskData.endDate || start
        const startDate = new Date(start)
        const endDate = new Date(end)
        const diffDays = Math.max(Math.round((endDate.getTime() - startDate.getTime()) / (24 * 60 * 60 * 1000)) + 1, 1)
        const next = [...this.ganttData.data]
        next[ganttIndex] = {
          ...current,
          id: current.id,
          parent: current.parent,
          text: taskData.name ?? current.text,
          start_date: start,
          duration: diffDays,
          progress: taskData.progress ?? current.progress
        }
        const saved = await this.saveGanttData(projectId, { data: next, links: this.ganttData.links })
        if (!saved) return null
        await this.fetchTasks(projectId)
        return this.tasks.find(t => t.id === taskId) || null
      } catch (error) {
        console.error('更新任务失败:', error)
        return null
      }
    },
    
    // 更新任务状态
    async updateTaskStatus(taskId: number, status: string) {
      try {
        const taskIndex = this.tasks.findIndex(task => task.id === taskId)
        const progress = taskIndex !== -1 ? (this.tasks[taskIndex]?.progress ?? 0) : 0
        await plmApi.project.updateTaskStatus(String(taskId), { status, progress })
        if (taskIndex !== -1) {
          const updatedTask = {
            ...this.tasks[taskIndex],
            status
          } as Task
          this.tasks[taskIndex] = updatedTask
          return updatedTask
        }
        return null
      } catch (error) {
        console.error('更新任务状态失败:', error)
        return null
      }
    },
    
    // 删除任务
    async deleteTask(taskId: number) {
      try {
        const existing = this.tasks.find(t => t.id === taskId)
        const projectId = String(existing?.projectId || this.selectedProjectId || '')
        if (!projectId) return false
        await this.fetchGanttData(projectId)
        const nextTasks = (this.ganttData.data || []).filter(t => t.id !== taskId)
        const nextLinks = (this.ganttData.links || []).filter(l => l.source !== taskId && l.target !== taskId)
        const saved = await this.saveGanttData(projectId, { data: nextTasks, links: nextLinks })
        if (!saved) return false
        await this.fetchTasks(projectId)
        return true
      } catch (error) {
        console.error('删除任务失败:', error)
        return false
      }
    }
  }
})
