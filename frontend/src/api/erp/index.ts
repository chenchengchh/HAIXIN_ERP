import { financeApi } from './finance'
import { supplyChainApi } from './supply-chain'
import { productionApi } from './production'
import { basicDataApi } from './basic-data'
import { dashboardApi } from './dashboard'

/**
 * ERP模块API入口
 */
export const erpApi = {
  /**
   * 财务模块API
   */
  finance: financeApi,
  
  /**
   * 供应链模块API
   */
  supplyChain: supplyChainApi,
  
  /**
   * 生产模块API
   */
  production: productionApi,
  
  /**
   * 基础数据模块API
   */
  basicData: basicDataApi,

  /**
   * 仪表盘模块API
   */
  dashboard: dashboardApi
}

