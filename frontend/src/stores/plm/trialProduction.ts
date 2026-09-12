import { defineStore } from 'pinia'
import { unwrapPageResponse, unwrapResponseData } from '@/api'
import { plmApi } from '@/api/plm'

// 试产计划类型定义
interface TrialPlan {
  id: string
  name: string
  code: string
  productCode: string
  productName: string
  status: 'pending' | 'in-progress' | 'completed'
  trialType: string
  trialQty: number
  version: string
  departments: string[]
  stages: {
    id: string
    name: string
    status: 'completed' | 'in-progress' | 'pending'
    startTime: string
    endTime: string
  }[]
}

// 试产报告类型定义
interface TrialReport {
  id: string
  planName: string
  code: string
  status: 'draft' | 'approved'
  planCode?: string
  productCode?: string
  productName?: string
  version?: string
  trialQty?: number
  passQty?: number
  yield?: number
  createTime?: string
  createUser?: string
  approveTime?: string
  approveUser?: string
  mainIssues?: string[]
  improvements?: string[]
}

// 质量跟踪类型定义
interface QualityTrack {
  id: string
  issue: string
  code: string
  reportCode?: string
  productCode?: string
  productName?: string
  version?: string
  status: 'in-progress' | 'resolved'
  severity: 'high' | 'medium' | 'low'
  resolution?: string
  createTime?: string
  createUser?: string
  resolveTime?: string
  resolveUser?: string
}

// 试产管理状态管理
// 管理试产计划、试产报告、质量跟踪等状态
export const useTrialProductionStore = defineStore('trialProduction', {
  state: () => ({
    // 试产计划列表
    trialPlans: [] as TrialPlan[],
    // 试产报告列表
    trialReports: [] as TrialReport[],
    // 质量跟踪列表
    qualityTracks: [] as QualityTrack[],
    // 选中的试产计划ID
    selectedPlanId: null as string | null,
    // 选中的试产报告ID
    selectedReportId: null as string | null,
    // 试产状态过滤
    statusFilter: 'all',
    // 试产搜索关键词
    searchKeyword: '',
    // 分页信息
    pagination: {
      page: 1,
      pageSize: 10,
      total: 0
    }
  }),

  getters: {
    // 获取筛选后的试产计划列表
    filteredTrialPlans: (state) => {
      return state.trialPlans.filter(plan => {
        const matchesKeyword = state.searchKeyword === '' || 
          plan.name.toLowerCase().includes(state.searchKeyword.toLowerCase()) ||
          plan.code.toLowerCase().includes(state.searchKeyword.toLowerCase())
        const matchesStatus = state.statusFilter === 'all' || plan.status === state.statusFilter
        return matchesKeyword && matchesStatus
      })
    },

    // 获取当前选中的试产计划
    currentPlan: (state) => {
      if (!state.selectedPlanId) return null
      return state.trialPlans.find(plan => plan.id === state.selectedPlanId) || null
    },

    // 获取当前选中的试产报告
    currentReport: (state) => {
      if (!state.selectedReportId) return null
      return state.trialReports.find(report => report.id === state.selectedReportId) || null
    }
  },

  actions: {
    // 设置试产计划列表
    setTrialPlans(plans: any[], total?: number) {
      this.trialPlans = plans
      this.pagination.total = typeof total === 'number' ? total : plans.length
    },

    // 设置试产报告列表
    setTrialReports(reports: any[], total?: number) {
      this.trialReports = reports
      if (typeof total === 'number') this.pagination.total = total
    },

    // 设置质量跟踪列表
    setQualityTracks(tracks: any[], total?: number) {
      this.qualityTracks = tracks
      if (typeof total === 'number') this.pagination.total = total
    },

    // 设置选中的试产计划ID
    setSelectedPlanId(planId: string) {
      this.selectedPlanId = planId
    },

    // 设置选中的试产报告ID
    setSelectedReportId(reportId: string) {
      this.selectedReportId = reportId
    },

    // 设置试产状态过滤
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

    /**
     * 新增试产计划
     * @param plan 试产计划数据
     */
    async addTrialPlan(plan: any) {
      try {
        const response: any = await plmApi.trial.createTrialPlan(plan)
        const created = unwrapResponseData<any>(response)
        if (created) {
          this.trialPlans.unshift(created)
          this.pagination.total = this.pagination.total + 1
          return created
        }
        return null
      } catch (error) {
        return null
      }
    },

    /**
     * 更新试产计划
     * @param plan 试产计划数据
     */
    async updateTrialPlan(plan: any) {
      try {
        const response: any = await plmApi.trial.updateTrialPlan(plan.id, plan)
        const updated = unwrapResponseData<any>(response)
        if (updated) {
          const index = this.trialPlans.findIndex(p => p.id === String(plan.id))
          if (index !== -1) this.trialPlans[index] = updated
          return updated
        }
        return null
      } catch (error) {
        return null
      }
    },

    /**
     * 删除试产计划
     * @param planId 计划ID
     */
    async deleteTrialPlan(planId: string) {
      try {
        await plmApi.trial.deleteTrialPlan(planId)
        this.trialPlans = this.trialPlans.filter(p => p.id !== planId)
        this.pagination.total = Math.max(this.pagination.total - 1, 0)
        if (this.selectedPlanId === planId) this.selectedPlanId = null
        return true
      } catch (error) {
        return false
      }
    },

    // 批量删除试产计划
    batchDeleteTrialPlans(planIds: string[]) {
      this.trialPlans = this.trialPlans.filter(p => !planIds.includes(p.id))
      this.pagination.total = this.trialPlans.length
    },

    /**
     * 开始试产计划（本地更新后同步到后端）
     * @param planId 计划ID
     */
    async startTrialPlan(planId: string) {
      const plan = this.trialPlans.find(p => p.id === planId)
      if (plan) {
        const stages = (plan.stages || []).map((s: any) => ({ ...s }))
        const pendingIndex = stages.findIndex((s: any) => s.status === 'pending')
        if (pendingIndex !== -1) {
          stages[pendingIndex].status = 'in-progress'
        }
        const updated = await this.updateTrialPlan({ ...plan, status: 'in-progress', stages })
        return Boolean(updated)
      }
      return false
    },

    /**
     * 完成试产计划（本地更新后同步到后端）
     * @param planId 计划ID
     */
    async completeTrialPlan(planId: string) {
      const plan = this.trialPlans.find(p => p.id === planId)
      if (plan) {
        const stages = (plan.stages || []).map((stage: any) => ({ ...stage, status: 'completed' }))
        const updated = await this.updateTrialPlan({ ...plan, status: 'completed', stages })
        return Boolean(updated)
      }
      return false
    },

    /**
     * 新增试产报告
     * @param report 报告数据
     */
    async addTrialReport(report: any) {
      try {
        const response: any = await plmApi.trial.createTrialReport(report)
        const created = unwrapResponseData<any>(response)
        if (created) {
          this.trialReports.unshift(created)
          this.pagination.total = this.pagination.total + 1
          return created
        }
        return null
      } catch (error) {
        return null
      }
    },

    /**
     * 更新试产报告
     * @param report 报告数据
     */
    async updateTrialReport(report: any) {
      try {
        const response: any = await plmApi.trial.updateTrialReport(report.id, report)
        const updated = unwrapResponseData<any>(response)
        if (updated) {
          const index = this.trialReports.findIndex(r => r.id === String(report.id))
          if (index !== -1) this.trialReports[index] = updated
          return updated
        }
        return null
      } catch (error) {
        return null
      }
    },

    /**
     * 删除试产报告
     * @param reportId 报告ID
     */
    async deleteTrialReport(reportId: string) {
      try {
        await plmApi.trial.deleteTrialReport(reportId)
        this.trialReports = this.trialReports.filter(r => r.id !== reportId)
        this.pagination.total = Math.max(this.pagination.total - 1, 0)
        if (this.selectedReportId === reportId) this.selectedReportId = null
        return true
      } catch (error) {
        return false
      }
    },

    // 批量删除试产报告
    batchDeleteTrialReports(reportIds: string[]) {
      this.trialReports = this.trialReports.filter(r => !reportIds.includes(r.id))
    },

    /**
     * 审批试产报告（本地更新后同步到后端）
     * @param reportId 报告ID
     */
    async approveTrialReport(reportId: string) {
      const report = this.trialReports.find(r => r.id === reportId)
      if (report) {
        const updated = await this.updateTrialReport({
          ...report,
          status: 'approved',
          approveTime: new Date().toISOString().split('T')[0],
          approveUser: '当前用户'
        })
        return Boolean(updated)
      }
      return false
    },

    /**
     * 新增质量问题
     * @param issue 质量问题数据
     */
    async addQualityIssue(issue: any) {
      try {
        const response: any = await plmApi.trial.createQualityIssue(issue)
        const created = unwrapResponseData<any>(response)
        if (created) {
          this.qualityTracks.unshift(created)
          this.pagination.total = this.pagination.total + 1
          return created
        }
        return null
      } catch (error) {
        return null
      }
    },

    /**
     * 更新质量问题
     * @param issue 质量问题数据
     */
    async updateQualityIssue(issue: any) {
      try {
        const response: any = await plmApi.trial.updateQualityIssue(issue.id, issue)
        const updated = unwrapResponseData<any>(response)
        if (updated) {
          const index = this.qualityTracks.findIndex(i => i.id === String(issue.id))
          if (index !== -1) this.qualityTracks[index] = updated
          return updated
        }
        return null
      } catch (error) {
        return null
      }
    },

    /**
     * 删除质量问题
     * @param issueId 问题ID
     */
    async deleteQualityIssue(issueId: string) {
      try {
        await plmApi.trial.deleteQualityIssue(issueId)
        this.qualityTracks = this.qualityTracks.filter(i => i.id !== issueId)
        this.pagination.total = Math.max(this.pagination.total - 1, 0)
        return true
      } catch (error) {
        return false
      }
    },

    // 批量删除质量问题
    batchDeleteQualityIssues(issueIds: string[]) {
      this.qualityTracks = this.qualityTracks.filter(i => !issueIds.includes(i.id))
    },

    /**
     * 解决质量问题（本地更新后同步到后端）
     * @param issueId 问题ID
     * @param resolution 解决方案
     */
    async resolveQualityIssue(issueId: string, resolution: string) {
      const issue = this.qualityTracks.find(i => i.id === issueId)
      if (issue) {
        const updated = await this.updateQualityIssue({
          ...issue,
          status: 'resolved',
          resolution,
          resolveTime: new Date().toISOString().split('T')[0],
          resolveUser: '当前用户'
        })
        return Boolean(updated)
      }
      return false
    },

    /**
     * 异步获取试产计划列表
     */
    async fetchTrialPlans() {
      try {
        const response: any = await plmApi.trial.getTrialPlans({
          page: this.pagination.page,
          size: this.pagination.pageSize,
          keyword: this.searchKeyword,
          status: this.statusFilter === 'all' ? '' : this.statusFilter
        })
        const page = unwrapPageResponse<any>(response)
        if (Array.isArray(page.list)) {
          this.setTrialPlans(page.list, page.total || page.list.length)
        } else {
          this.setTrialPlans([], 0)
        }
      } catch (error) {
        console.error('获取试产计划列表失败:', error)
        this.setTrialPlans([], 0)
      }
    },

    /**
     * 异步获取试产报告列表
     */
    async fetchTrialReports() {
      try {
        const response: any = await plmApi.trial.getTrialReports({
          page: this.pagination.page,
          size: this.pagination.pageSize,
          keyword: this.searchKeyword,
          status: this.statusFilter === 'all' ? '' : this.statusFilter
        })
        const page = unwrapPageResponse<any>(response)
        if (Array.isArray(page.list)) {
          this.setTrialReports(page.list, page.total || page.list.length)
        } else {
          this.setTrialReports([], 0)
        }
      } catch (error) {
        console.error('获取试产报告列表失败:', error)
        this.setTrialReports([], 0)
      }
    },

    /**
     * 异步获取质量问题列表
     */
    async fetchQualityTracks() {
      try {
        const response: any = await plmApi.trial.getQualityIssues({
          page: this.pagination.page,
          size: this.pagination.pageSize,
          keyword: this.searchKeyword,
          status: this.statusFilter === 'all' ? '' : this.statusFilter
        })
        const page = unwrapPageResponse<any>(response)
        if (Array.isArray(page.list)) {
          this.setQualityTracks(page.list, page.total || page.list.length)
        } else {
          this.setQualityTracks([], 0)
        }
      } catch (error) {
        console.error('获取质量跟踪列表失败:', error)
        this.setQualityTracks([], 0)
      }
    }
  }
})
