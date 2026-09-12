import { customerApi } from './customer'
import { salesApi } from './sales'
import { orderApi } from './order'
import { serviceApi } from './service'

/**
 * CRM模块API入口
 */
export const crmApi = {
  /**
   * 客户管理模块API
   */
  customer: customerApi,
  
  /**
   * 销售管理模块API
   */
  sales: salesApi,
  
  /**
   * 订单管理模块API
   */
  order: orderApi,
  
  /**
   * 客户服务模块API
   */
  service: serviceApi
}
