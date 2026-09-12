/**
 * PLM系统API服务入口
 * @author author
 * @date 2025-12-30
 */

import { projectManagementApi } from './projectManagement'
import { projectApi } from './project'
import { productDataApi } from './productData'
import { processApi } from './process'
import { trialApi } from './trial'

/**
 * PLM模块统一API对象
 */
export const plmApi = {
  /**
   * 项目管理API
   */
  projectManagement: projectManagementApi,
  project: projectApi,
  productData: productDataApi,
  process: processApi,
  trial: trialApi
}

export default plmApi
