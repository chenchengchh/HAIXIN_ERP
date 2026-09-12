import { defineStore } from 'pinia'
import { unwrapPageResponse, unwrapResponseData } from '@/api'
import { plmApi } from '@/api/plm'

// 工艺路线类型定义
interface ProcessRoute {
  id: string
  name: string
  code: string
  status: 'active' | 'draft' | 'inactive' | 'deleted'
  productName: string
  productCode: string
  version: string
  type: string
  createUser: string
  steps: {
    id: string
    name: string
    equipment: string
    operator: string
    time: number
  }[]
}

// 工艺文件类型定义
interface ProcessFile {
  id: string
  code: string
  title: string
  type: string
  category: string
  version: string
  status: string
  author: string
  createTime: string
  updateTime: string
  fileSize: string
  fileType: string
  filePath: string
  remark?: string
}

// 工艺变更类型定义
interface ProcessChange {
  id: string
  title: string
  code: string
  type: string
  processCode: string
  processName: string
  reason: string
  content: string
  status: string
  applyUser: string
  applyTime: string
  approveUser: string
  approveTime: string
  implementTime: string
}

// 工艺协同状态管理
// 管理工艺设计、工艺路线、工艺变更等状态
export const useProcessCollaborationStore = defineStore('processCollaboration', {
  state: () => ({
    // 工艺路线列表
    processRoutes: [] as ProcessRoute[],
    // 工艺文件列表
    processFiles: [] as ProcessFile[],
    // 工艺变更列表
    processChanges: [] as ProcessChange[],
    // 选中的工艺路线ID
    selectedRouteId: null as string | null,
    // 选中的工艺文件ID
    selectedFileId: null as string | null,
    // 工艺搜索关键词
    searchKeyword: '',
    // 工艺类型过滤
    typeFilter: 'all',
    // 工艺文件类型过滤
    fileTypeFilter: 'all',
    // 工艺变更状态过滤
    changeStatusFilter: 'all',
    // 分页信息
    pagination: {
      page: 1,
      pageSize: 10,
      total: 0
    }
  }),

  getters: {
    // 获取筛选后的工艺路线列表
    filteredProcessRoutes: (state) => {
      return state.processRoutes.filter(route => {
        const matchesKeyword = state.searchKeyword === '' || 
          route.name.toLowerCase().includes(state.searchKeyword.toLowerCase()) ||
          route.code.toLowerCase().includes(state.searchKeyword.toLowerCase())
        const matchesType = state.typeFilter === 'all' || route.type === state.typeFilter
        return matchesKeyword && matchesType
      })
    },

    // 获取当前选中的工艺路线
    currentRoute: (state) => {
      if (!state.selectedRouteId) return null
      return state.processRoutes.find(route => route.id === state.selectedRouteId) || null
    },

    // 获取当前选中的工艺文件
    currentFile: (state) => {
      if (!state.selectedFileId) return null
      return state.processFiles.find(file => file.id === state.selectedFileId) || null
    }
  },

  actions: {
    // 设置工艺路线列表
    setProcessRoutes(routes: any[]) {
      this.processRoutes = routes
      this.pagination.total = routes.length
    },

    // 设置工艺文件列表
    setProcessFiles(files: any[]) {
      this.processFiles = files
    },

    // 设置工艺变更列表
    setProcessChanges(changes: any[]) {
      this.processChanges = changes
    },

    // 设置选中的工艺路线ID
    setSelectedRouteId(routeId: string) {
      this.selectedRouteId = routeId
    },

    // 设置选中的工艺文件ID
    setSelectedFileId(fileId: string) {
      this.selectedFileId = fileId
    },

    // 设置搜索关键词
    setSearchKeyword(keyword: string) {
      this.searchKeyword = keyword
    },

    // 设置工艺类型过滤
    setTypeFilter(type: string) {
      this.typeFilter = type
    },

    // 设置工艺文件类型过滤
    setFileTypeFilter(type: string) {
      this.fileTypeFilter = type
    },

    // 设置工艺变更状态过滤
    setChangeStatusFilter(status: string) {
      this.changeStatusFilter = status
    },

    // 设置分页
    setPagination(page: number, pageSize: number) {
      this.pagination.page = page
      this.pagination.pageSize = pageSize
    },

    // 重置筛选条件
    resetFilters() {
      this.searchKeyword = ''
      this.typeFilter = 'all'
      this.fileTypeFilter = 'all'
      this.changeStatusFilter = 'all'
      this.pagination.page = 1
    },

    /**
     * 新增工艺路线
     * @param route 工艺路线数据
     */
    async addProcessRoute(route: any) {
      try {
        const response: any = await plmApi.process.createProcessRoute(route)
        const created = unwrapResponseData<any>(response)
        if (created) {
          this.processRoutes.unshift(created)
          this.pagination.total = this.processRoutes.length
          return created
        }
        return null
      } catch (error) {
        return null
      }
    },

    /**
     * 更新工艺路线
     * @param route 工艺路线数据
     */
    async updateProcessRoute(route: any) {
      try {
        const response: any = await plmApi.process.updateProcessRoute(route.id, route)
        const updated = unwrapResponseData<any>(response)
        if (updated) {
          const index = this.processRoutes.findIndex(r => r.id === String(route.id))
          if (index !== -1) {
            this.processRoutes[index] = updated
          }
          return updated
        }
        return null
      } catch (error) {
        return null
      }
    },

    /**
     * 删除工艺路线
     * @param routeId 工艺路线ID
     */
    async deleteProcessRoute(routeId: string) {
      try {
        await plmApi.process.deleteProcessRoute(routeId)
        this.processRoutes = this.processRoutes.filter(r => r.id !== routeId)
        this.pagination.total = Math.max(this.pagination.total - 1, 0)
        if (this.selectedRouteId === routeId) {
          this.selectedRouteId = null
        }
        return true
      } catch (error) {
        return false
      }
    },

    /**
     * 新增工艺文件（元数据）
     * @param file 工艺文件数据
     */
    async addProcessFile(file: any) {
      try {
        const response: any = await plmApi.process.createProcessFile(file)
        const created = unwrapResponseData<any>(response)
        if (created) {
          this.processFiles.unshift(created)
          return created
        }
        return null
      } catch (error) {
        return null
      }
    },

    /**
     * 更新工艺文件（元数据）
     * @param file 工艺文件数据
     */
    async updateProcessFile(file: any) {
      try {
        const response: any = await plmApi.process.updateProcessFile(file.id, file)
        const updated = unwrapResponseData<any>(response)
        if (updated) {
          const index = this.processFiles.findIndex(f => f.id === String(file.id))
          if (index !== -1) this.processFiles[index] = updated
          return updated
        }
        return null
      } catch (error) {
        return null
      }
    },

    /**
     * 删除工艺文件（元数据）
     * @param fileId 工艺文件ID
     */
    async deleteProcessFile(fileId: string) {
      try {
        await plmApi.process.deleteProcessFile(fileId)
        this.processFiles = this.processFiles.filter(f => f.id !== fileId)
        this.pagination.total = Math.max(this.pagination.total - 1, 0)
        if (this.selectedFileId === fileId) {
          this.selectedFileId = null
        }
        return true
      } catch (error) {
        return false
      }
    },

    /**
     * 上传工艺文件（二进制）
     * @param formData multipart表单数据
     */
    async uploadProcessFile(formData: FormData) {
      const response: any = await plmApi.process.uploadProcessFile(formData)
      return unwrapResponseData<any>(response)
    },

    /**
     * 下载工艺文件（二进制）
     * @param id 文件ID
     */
    async downloadProcessFile(id: string | number) {
      return plmApi.process.downloadProcessFile(id)
    },

    /**
     * 新增工艺变更
     * @param change 工艺变更数据
     */
    async addProcessChange(change: any) {
      try {
        const response: any = await plmApi.process.createProcessChange(change)
        const created = unwrapResponseData<any>(response)
        if (created) {
          this.processChanges.unshift(created)
          return created
        }
        return null
      } catch (error) {
        return null
      }
    },

    /**
     * 更新工艺变更
     * @param change 工艺变更数据
     */
    async updateProcessChange(change: any) {
      try {
        const response: any = await plmApi.process.updateProcessChange(change.id, change)
        const updated = unwrapResponseData<any>(response)
        if (updated) {
          const index = this.processChanges.findIndex(c => c.id === String(change.id))
          if (index !== -1) this.processChanges[index] = updated
          return updated
        }
        return null
      } catch (error) {
        return null
      }
    },

    /**
     * 删除工艺变更
     * @param changeId 工艺变更ID
     */
    async deleteProcessChange(changeId: string) {
      try {
        await plmApi.process.deleteProcessChange(changeId)
        this.processChanges = this.processChanges.filter(c => c.id !== changeId)
        this.pagination.total = Math.max(this.pagination.total - 1, 0)
        return true
      } catch (error) {
        return false
      }
    },

    // 异步获取工艺路线列表
    async fetchProcessRoutes() {
      try {
        const response: any = await plmApi.process.getProcessRoutes({
          page: this.pagination.page,
          size: this.pagination.pageSize,
          keyword: this.searchKeyword,
          type: this.typeFilter === 'all' ? '' : this.typeFilter
        })
        const page = unwrapPageResponse<any>(response)
        if (Array.isArray(page.list)) {
          this.setProcessRoutes(page.list)
          this.pagination.total = page.total || page.list.length
        } else {
          this.setProcessRoutes([])
          this.pagination.total = 0
        }
      } catch (error) {
        console.error('获取工艺路线列表失败:', error)
        this.setProcessRoutes([])
        this.pagination.total = 0
      }
    },

    // 异步获取工艺文件列表
    async fetchProcessFiles() {
      try {
        const response: any = await plmApi.process.getProcessFiles({
          page: this.pagination.page,
          size: this.pagination.pageSize,
          keyword: this.searchKeyword,
          type: this.fileTypeFilter === 'all' ? '' : this.fileTypeFilter
        })
        const page = unwrapPageResponse<any>(response)
        if (Array.isArray(page.list)) {
          this.setProcessFiles(page.list)
          this.pagination.total = page.total || page.list.length
        } else {
          this.setProcessFiles([])
          this.pagination.total = 0
        }
      } catch (error) {
        console.error('获取工艺文件列表失败:', error)
        this.setProcessFiles([])
        this.pagination.total = 0
      }
    },

    // 异步获取工艺变更列表
    async fetchProcessChanges() {
      try {
        const response: any = await plmApi.process.getProcessChanges({
          page: this.pagination.page,
          size: this.pagination.pageSize,
          keyword: this.searchKeyword,
          status: this.changeStatusFilter === 'all' ? '' : this.changeStatusFilter
        })
        const page = unwrapPageResponse<any>(response)
        if (Array.isArray(page.list)) {
          this.setProcessChanges(page.list)
          this.pagination.total = page.total || page.list.length
        } else {
          this.setProcessChanges([])
          this.pagination.total = 0
        }
      } catch (error) {
        console.error('获取工艺变更列表失败:', error)
        this.setProcessChanges([])
        this.pagination.total = 0
      }
    }
  }
})
