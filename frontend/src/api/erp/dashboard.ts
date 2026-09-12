import api from '../index'

/**
 * 仪表盘API
 */
export const dashboardApi = {
  /**
   * 获取仪表盘统计数据
   * @returns 统计数据
   */
  getStats: () => api.get('/api/v1/erp/dashboard/stats'),
  
  /**
   * 获取收支趋势
   * @param range 时间范围
   * @returns 趋势数据
   */
  getIncomeExpenseTrend: (range: string) => api.get('/api/v1/erp/dashboard/income-expense', { params: { range } }),
  
  /**
   * 获取订单状态分布
   * @returns 分布数据
   */
  getOrderStatusDistribution: () => api.get('/api/v1/erp/dashboard/order-status'),
  
  /**
   * 获取销售趋势
   * @returns 销售趋势数据
   */
  getSalesTrend: () => api.get('/api/v1/erp/dashboard/sales-trend'),
  
  /**
   * 获取库存周转率
   * @returns 周转率数据
   */
  getInventoryTurnover: () => api.get('/api/v1/erp/dashboard/inventory-turnover')
}
