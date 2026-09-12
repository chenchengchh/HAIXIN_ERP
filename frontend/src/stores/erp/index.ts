import { defineStore } from 'pinia'
import type { UserInfo } from '../../types/erp'
import { erpApi } from '../../api/erp'
import api from '../../api/index'
import { ErrorHandler } from '../../utils/error-handler'
import { redirectToLoginOnce } from '../../utils/auth-redirect'

// ERP系统主状态
export const useErpStore = defineStore('erp', {
  state: () => ({
    // 用户信息
    userInfo: null as UserInfo | null,
    // 菜单权限
    menuPermissions: [] as string[],
    // 按钮权限
    buttonPermissions: [] as string[],
    // 当前激活的模块
    activeModule: 'finance' as string,
    // 当前激活的子模块
    activeSubModule: 'general-ledger' as string,
    // 系统配置
    systemConfig: {
      // 是否显示面包屑
      showBreadcrumb: true,
      // 是否显示侧边栏
      showSidebar: true,
      // 是否显示标签页
      showTabs: true,
      // 主题颜色
      themeColor: '#1890ff'
    },
    // 全局加载状态
    loading: false,
    // 消息提示
    message: null as { type: 'success' | 'warning' | 'error' | 'info'; content: string } | null,
    // 高级搜索状态
    advancedSearch: {
      // 当前搜索条件
      currentFilters: {} as Record<string, any>,
      // 搜索历史记录
      history: [] as Array<{
        module: string
        filters: Record<string, any>
        timestamp: number
      }>,
      // 保存的搜索条件
      saved: [] as Array<{
        id: string
        name: string
        module: string
        filters: Record<string, any>
        createdAt: number
      }>
    },
    // 批量操作状态
    batchOperation: {
      // 当前选中的记录ID
      selectedIds: [] as number[],
      // 当前操作类型
      currentOperation: '' as string,
      // 操作状态
      status: 'idle' as 'idle' | 'processing' | 'success' | 'error',
      // 操作结果
      result: null as any
    },
    // 数据可视化状态
    visualization: {
      // 图表配置
      chartConfigs: {} as Record<string, any>,
      // 图表数据缓存
      chartDataCache: {} as Record<string, any>,
      // 仪表盘配置
      dashboardConfig: {
        // 显示的组件列表
        components: [] as string[],
        // 组件布局配置
        layout: {} as any
      }
    }
  }),

  getters: {
    // 是否已登录
    isLoggedIn: (state) => !!state.userInfo,
    // 是否有权限访问某个菜单
    hasMenuPermission: (state) => (menuKey: string) => {
      return state.menuPermissions.includes(menuKey)
    },
    // 是否有权限执行某个按钮操作
    hasButtonPermission: (state) => (buttonKey: string) => {
      return state.buttonPermissions.includes(buttonKey)
    }
  },

  actions: {
    // 设置用户信息
    setUserInfo(userInfo: UserInfo | null) {
      this.userInfo = userInfo
    },
    
    // 设置权限
    setPermissions(menuPermissions: string[], buttonPermissions: string[]) {
      this.menuPermissions = menuPermissions
      this.buttonPermissions = buttonPermissions
    },
    
    // 设置当前模块
    setActiveModule(module: string, subModule: string) {
      this.activeModule = module
      this.activeSubModule = subModule
    },
    
    // 设置系统配置
    setSystemConfig(config: Partial<typeof this.systemConfig>) {
      this.systemConfig = { ...this.systemConfig, ...config }
    },
    
    // 显示消息
    showMessage(type: 'success' | 'warning' | 'error' | 'info', content: string) {
      this.message = { type, content }
      // 3秒后自动隐藏
      setTimeout(() => {
        this.message = null
      }, 3000)
    },
    
    // 清除消息
    clearMessage() {
      this.message = null
    },
    
    // 显示加载状态
    showLoading() {
      this.loading = true
    },
    
    // 隐藏加载状态
    hideLoading() {
      this.loading = false
    },
    
    // 获取当前用户信息
    async fetchCurrentUserInfo() {
      try {
        // 使用直接的API调用，而不是erpApi.get
        const result = await api.get('/user/info')
        // Axios响应直接包含data属性，无需检查code
        this.userInfo = result.data
        return result.data
      } catch (error) {
        ErrorHandler.handleApiError(error)
        return null
      }
    },
    
    // 登出
    async logout() {
      try {
        // 使用直接的API调用，而不是erpApi.post
        await api.post('/logout')
        
        // 清除状态管理中的用户信息和权限
        this.userInfo = null
        this.menuPermissions = []
        this.buttonPermissions = []
        
        // 清除localStorage中的所有相关信息
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
        localStorage.removeItem('userPermissions')
        localStorage.removeItem('loginMethod')
        
        // 清除sessionStorage中的所有相关信息
        sessionStorage.removeItem('token')
        sessionStorage.removeItem('userInfo')
        sessionStorage.removeItem('userPermissions')
        sessionStorage.removeItem('loginMethod')
        
        // 跳转到登录页面
        await redirectToLoginOnce({ replace: true })
      } catch (error) {
        // 即使API调用失败，也需要清除本地存储的登录信息
        this.userInfo = null
        this.menuPermissions = []
        this.buttonPermissions = []
        
        // 清除localStorage中的所有相关信息
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
        localStorage.removeItem('userPermissions')
        localStorage.removeItem('loginMethod')
        
        // 清除sessionStorage中的所有相关信息
        sessionStorage.removeItem('token')
        sessionStorage.removeItem('userInfo')
        sessionStorage.removeItem('userPermissions')
        sessionStorage.removeItem('loginMethod')
        
        // 跳转到登录页面
        await redirectToLoginOnce({ replace: true })
        
        ErrorHandler.handleApiError(error)
      }
    },
    
    // ------------------------ 高级搜索相关 actions ------------------------
    
    // 设置当前搜索条件
    setCurrentFilters(module: string, filters: Record<string, any>) {
      this.advancedSearch.currentFilters = { ...filters }
      // 自动添加到搜索历史
      this.addSearchHistory(module, filters)
    },
    
    // 添加搜索历史记录
    addSearchHistory(module: string, filters: Record<string, any>) {
      // 限制历史记录数量
      const maxHistory = 10
      // 移除重复记录
      this.advancedSearch.history = this.advancedSearch.history.filter(item => 
        item.module !== module || JSON.stringify(item.filters) !== JSON.stringify(filters)
      )
      // 添加新记录到开头
      this.advancedSearch.history.unshift({
        module,
        filters,
        timestamp: Date.now()
      })
      // 限制历史记录数量
      if (this.advancedSearch.history.length > maxHistory) {
        this.advancedSearch.history = this.advancedSearch.history.slice(0, maxHistory)
      }
    },
    
    // 保存搜索条件
    saveSearch(name: string, module: string, filters: Record<string, any>) {
      const savedSearch = {
        id: `saved_${Date.now()}`,
        name,
        module,
        filters,
        createdAt: Date.now()
      }
      this.advancedSearch.saved.push(savedSearch)
      return savedSearch
    },
    
    // 加载保存的搜索条件
    loadSavedSearch(id: string) {
      const savedSearch = this.advancedSearch.saved.find(item => item.id === id)
      if (savedSearch) {
        this.advancedSearch.currentFilters = { ...savedSearch.filters }
        return savedSearch
      }
      return null
    },
    
    // 删除保存的搜索条件
    deleteSavedSearch(id: string) {
      this.advancedSearch.saved = this.advancedSearch.saved.filter(item => item.id !== id)
    },
    
    // 清除搜索历史
    clearSearchHistory() {
      this.advancedSearch.history = []
    },
    
    // ------------------------ 批量操作相关 actions ------------------------
    
    // 设置选中的记录ID
    setSelectedIds(ids: number[]) {
      this.batchOperation.selectedIds = [...ids]
    },
    
    // 清空选中的记录ID
    clearSelectedIds() {
      this.batchOperation.selectedIds = []
    },
    
    // 设置当前操作类型
    setCurrentOperation(operation: string) {
      this.batchOperation.currentOperation = operation
    },
    
    // 开始批量操作
    startBatchOperation(operation: string, ids: number[]) {
      this.batchOperation = {
        selectedIds: [...ids],
        currentOperation: operation,
        status: 'processing',
        result: null
      }
    },
    
    // 完成批量操作
    completeBatchOperation(result: any, status: 'success' | 'error' = 'success') {
      this.batchOperation.status = status
      this.batchOperation.result = result
    },
    
    // 清空批量操作状态
    clearBatchOperation() {
      this.batchOperation = {
        selectedIds: [],
        currentOperation: '',
        status: 'idle',
        result: null
      }
    },
    
    // ------------------------ 数据可视化相关 actions ------------------------
    
    // 保存图表配置
    saveChartConfig(chartId: string, config: any) {
      this.visualization.chartConfigs[chartId] = { ...config }
    },
    
    // 加载图表配置
    loadChartConfig(chartId: string) {
      return this.visualization.chartConfigs[chartId] || null
    },
    
    // 缓存图表数据
    cacheChartData(chartId: string, data: any, expireTime: number = 5 * 60 * 1000) {
      this.visualization.chartDataCache[chartId] = {
        data,
        timestamp: Date.now(),
        expireTime
      }
    },
    
    // 获取图表数据（优先从缓存获取）
    getChartData(chartId: string) {
      const cached = this.visualization.chartDataCache[chartId]
      if (cached) {
        const isExpired = Date.now() - cached.timestamp > cached.expireTime
        if (!isExpired) {
          return cached.data
        } else {
          // 缓存已过期，清除
          delete this.visualization.chartDataCache[chartId]
        }
      }
      return null
    },
    
    // 清除图表数据缓存
    clearChartDataCache(chartId?: string) {
      if (chartId) {
        delete this.visualization.chartDataCache[chartId]
      } else {
        this.visualization.chartDataCache = {}
      }
    },
    
    // 保存仪表盘配置
    saveDashboardConfig(config: any) {
      this.visualization.dashboardConfig = { ...config }
    },
    
    // 加载仪表盘配置
    loadDashboardConfig() {
      return { ...this.visualization.dashboardConfig }
    }
  },
  

})

// 财务模块状态
export const useFinanceStore = defineStore('erp-finance', {
  state: () => ({
    // 凭证数据
    vouchers: [] as any[],
    // 收款单数据
    receipts: [] as any[],
    // 付款单数据
    payments: [] as any[],
    // 固定资产数据
    fixedAssets: [] as any[],
    // 当前选中的凭证
    currentVoucher: null as any,
    // 当前选中的收款单
    currentReceipt: null as any,
    // 当前选中的付款单
    currentPayment: null as any,
    // 当前选中的固定资产
    currentFixedAsset: null as any
  }),
  
  getters: {
    // 未审核凭证数量
    pendingVoucherCount: (state) => {
      return state.vouchers.filter((voucher) => voucher.approval_status === 'pending').length
    },
    // 本月收款总额
    currentMonthReceiptTotal: (state) => {
      return state.receipts.reduce((total: number, receipt) => {
        // 简单实现，实际应根据日期过滤
        return total + receipt.amount
      }, 0)
    }
  },
  
  actions: {
    // 设置凭证数据
    setVouchers(vouchers: any[]) {
      this.vouchers = vouchers
    },
    
    // 设置收款单数据
    setReceipts(receipts: any[]) {
      this.receipts = receipts
    },
    
    // 设置付款单数据
    setPayments(payments: any[]) {
      this.payments = payments
    },
    
    // 设置固定资产数据
    setFixedAssets(assets: any[]) {
      this.fixedAssets = assets
    },
    
    // 设置当前选中的凭证
    setCurrentVoucher(voucher: any) {
      this.currentVoucher = voucher
    },
    
    // 清空当前选中的凭证
    clearCurrentVoucher() {
      this.currentVoucher = null
    },
    
    // 保存凭证
    async saveVoucher(voucher: any) {
      try {
        let result
        if (voucher.id) {
          result = await erpApi.finance.updateVoucher(voucher.id, voucher)
        } else {
          result = await erpApi.finance.createVoucher(voucher)
        }
        return result
      } catch (error) {
        ErrorHandler.handleApiError(error)
        return null
      }
    },
    
    // 审核凭证
    async approveVoucher(id: number) {
      try {
        const result = await erpApi.finance.approveVoucher(id)
        // 更新本地数据
        const index = this.vouchers.findIndex((voucher) => voucher.id === id)
        if (index > -1) {
          this.vouchers[index].approval_status = 'approved'
        }
        return result
      } catch (error) {
        ErrorHandler.handleApiError(error)
        return null
      }
    },
    
    // 过账凭证
    async postVoucher(id: number) {
      try {
        const result = await erpApi.finance.postVoucher(id)
        // 更新本地数据
        const index = this.vouchers.findIndex((voucher) => voucher.id === id)
        if (index > -1) {
          this.vouchers[index].status = 'posted'
        }
        return result
      } catch (error) {
        ErrorHandler.handleApiError(error)
        return null
      }
    }
  }
})

// 供应链模块状态
export const useSupplyChainStore = defineStore('erp-supply-chain', {
  state: () => ({
    // 采购订单数据
    purchaseOrders: [] as any[],
    // 销售订单数据
    salesOrders: [] as any[],
    // 库存数据
    inventory: [] as any[],
    // MRP运算结果
    mrpResults: [] as any[],
    // 当前选中的采购订单
    currentPurchaseOrder: null as any,
    // 当前选中的销售订单
    currentSalesOrder: null as any
  }),
  
  getters: {
    // 待发货订单数量
    pendingShipmentCount: (state) => {
      return state.salesOrders.filter((order) => order.status === 'confirmed' || order.status === 'processing').length
    },
    // 库存预警数量
    inventoryAlertCount: (state) => {
      return state.inventory.filter((item) => item.current_qty < item.safety_stock).length
    }
  },
  
  actions: {
    // 设置采购订单数据
    setPurchaseOrders(orders: any[]) {
      this.purchaseOrders = orders
    },
    
    // 设置销售订单数据
    setSalesOrders(orders: any[]) {
      this.salesOrders = orders
    },
    
    // 设置库存数据
    setInventory(inventory: any[]) {
      this.inventory = inventory
    },
    
    // 设置MRP运算结果
    setMRPResults(results: any[]) {
      this.mrpResults = results
    },
    
    // 保存采购订单
    async savePurchaseOrder(order: any) {
      try {
        let result
        if (order.id) {
          result = await erpApi.supplyChain.updatePurchaseOrder(order.id, order)
        } else {
          result = await erpApi.supplyChain.createPurchaseOrder(order)
        }
        return result
      } catch (error) {
        ErrorHandler.handleApiError(error)
        return null
      }
    },
    
    // 审核采购订单
    async approvePurchaseOrder(id: number) {
      try {
        const result = await erpApi.supplyChain.approvePurchaseOrder(id)
        // 更新本地数据
        const index = this.purchaseOrders.findIndex((order) => order.id === id)
        if (index > -1) {
          this.purchaseOrders[index].status = 'approved'
        }
        return result
      } catch (error) {
        ErrorHandler.handleApiError(error)
        return null
      }
    }
  }
})

// 生产模块状态
export const useProductionStore = defineStore('erp-production', {
  state: () => ({
    // 生产订单数据
    productionOrders: [] as any[],
    // 车间工单数据
    workshopOrders: [] as any[],
    // 产能数据
    capacityData: [] as any[],
    // 当前选中的生产订单
    currentProductionOrder: null as any,
    // 当前选中的车间工单
    currentWorkshopOrder: null as any
  }),
  
  getters: {
    // 生产中订单数量
    inProductionCount: (state) => {
      return state.productionOrders.filter((order) => order.status === 'in_production').length
    },
    // 平均产能利用率
    avgCapacityUtilization: (state) => {
      if (state.capacityData.length === 0) return 0
      const total = state.capacityData.reduce((sum: number, item) => sum + item.utilization_rate, 0)
      return Math.round((total / state.capacityData.length) * 100) / 100
    }
  },
  
  actions: {
    // 设置生产订单数据
    setProductionOrders(orders: any[]) {
      this.productionOrders = orders
    },
    
    // 设置车间工单数据
    setWorkshopOrders(orders: any[]) {
      this.workshopOrders = orders
    },
    
    // 设置产能数据
    setCapacityData(data: any[]) {
      this.capacityData = data
    },
    
    // 保存生产订单
    async saveProductionOrder(order: any) {
      try {
        let result
        if (order.id) {
          result = await erpApi.production.updateProductionOrder(order.id, order)
        } else {
          result = await erpApi.production.createProductionOrder(order)
        }
        return result
      } catch (error) {
        ErrorHandler.handleApiError(error)
        return null
      }
    },
    
    
    // 注意：production API 中暂未实现 approveProductionOrder 方法
    // 如需审核功能，请先在 api/erp/production.ts 中添加相应的 API 定义
    /*
    async approveProductionOrder(id: number) {
      try {
        const result = await erpApi.production.approveProductionOrder(id)
        // 更新本地数据
        const index = this.productionOrders.findIndex((order) => order.id === id)
        if (index > -1) {
          this.productionOrders[index].status = 'approved'
        }
        return result
      } catch (error) {
        ErrorHandler.handleApiError(error)
        return null
      }
    }
    */

  }
})

// 基础数据模块状态
export const useBasicDataStore = defineStore('erp-basic-data', {
  state: () => ({
    // 客户数据
    customers: [] as any[],
    // 供应商数据
    suppliers: [] as any[],
    // 物料数据
    materials: [] as any[],
    // 会计科目数据
    accounts: [] as any[],
    // 组织数据
    organizations: [] as any[],
    // 当前选中的客户
    currentCustomer: null as any,
    // 当前选中的供应商
    currentSupplier: null as any,
    // 当前选中的物料
    currentMaterial: null as any,
    // 当前选中的会计科目
    currentAccount: null as any,
    // 当前选中的组织
    currentOrganization: null as any
  }),
  
  getters: {
    // 活跃客户数量
    activeCustomerCount: (state) => {
      return state.customers.filter((customer) => customer.status === 'active').length
    },
    // 活跃供应商数量
    activeSupplierCount: (state) => {
      return state.suppliers.filter((supplier) => supplier.status === 'active').length
    },
    // 活跃组织数量
    activeOrganizationCount: (state) => {
      return state.organizations.filter((org) => org.status === 1).length
    }
  },
  
  actions: {
    // 设置客户数据
    setCustomers(customers: any[]) {
      this.customers = customers
    },
    
    // 设置供应商数据
    setSuppliers(suppliers: any[]) {
      this.suppliers = suppliers
    },
    
    // 设置物料数据
    setMaterials(materials: any[]) {
      this.materials = materials
    },
    
    // 设置会计科目数据
    setAccounts(accounts: any[]) {
      this.accounts = accounts
    },
    
    // 设置组织数据
    setOrganizations(organizations: any[]) {
      this.organizations = organizations
    },
    
    // 设置当前选中的组织
    setCurrentOrganization(organization: any) {
      this.currentOrganization = organization
    },
    
    // 清空当前选中的组织
    clearCurrentOrganization() {
      this.currentOrganization = null
    },
    
    // 保存客户数据
    async saveCustomer(customer: any) {
      try {
        let result
        if (customer.id) {
          result = await erpApi.basicData.updateCustomer(customer.id, customer)
        } else {
          result = await erpApi.basicData.createCustomer(customer)
        }
        return result
      } catch (error) {
        ErrorHandler.handleApiError(error)
        return null
      }
    },
    
    // 保存供应商数据
    async saveSupplier(supplier: any) {
      try {
        let result
        if (supplier.id) {
          result = await erpApi.basicData.updateSupplier(supplier.id, supplier)
        } else {
          result = await erpApi.basicData.createSupplier(supplier)
        }
        return result
      } catch (error) {
        ErrorHandler.handleApiError(error)
        return null
      }
    },
    
    // 保存物料数据
    async saveMaterial(material: any) {
      try {
        let result
        if (material.id) {
          result = await erpApi.basicData.updateMaterial(material.id, material)
        } else {
          result = await erpApi.basicData.createMaterial(material)
        }
        return result
      } catch (error) {
        ErrorHandler.handleApiError(error)
        return null
      }
    },
    
    // 保存组织数据
    async saveOrganization(organization: any) {
      try {
        let result
        if (organization.id) {
          // 更新组织
          result = await api.put(`/erp/basic-data/organization/${organization.id}`, organization)
        } else {
          // 创建组织
          result = await api.post('/erp/basic-data/organization', organization)
        }
        return result
      } catch (error) {
        ErrorHandler.handleApiError(error)
        return null
      }
    },
    
    // 删除组织
    async deleteOrganization(id: number) {
      try {
        const result = await api.delete(`/erp/basic-data/organization/${id}`)
        // 从本地状态中移除
        this.organizations = this.organizations.filter(org => org.id !== id)
        return result
      } catch (error) {
        ErrorHandler.handleApiError(error)
        return null
      }
    }
  }
})
