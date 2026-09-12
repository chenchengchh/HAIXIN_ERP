/**
 * 销售管理模块类型定义
 */

/**
 * 销售线索
 */
export interface LeadEntity {
  id: number;
  leadNo: string;            // 线索编号
  leadName: string;          // 线索名称
  companyName: string;       // 公司名称
  contactName: string;       // 联系人
  phone: string;             // 电话
  email: string;             // 邮箱
  source: string;            // 来源
  industry: string;          // 行业
  intent: string;            // 意向产品/服务
  rating: 'hot' | 'warm' | 'cold';  // 评级
  status: 'new' | 'contacted' | 'qualified' | 'converted' | 'lost';  // 状态
  ownerId: number;           // 负责人ID
  ownerName: string;         // 负责人姓名
  createTime: string;        // 创建时间
  convertTime?: string;      // 转化时间
}

/**
 * 商机
 */
export interface OpportunityEntity {
  id: number;
  opportunityNo: string;     // 商机编号
  opportunityName: string;   // 商机名称
  customerId: number;        // 客户ID
  customerName: string;      // 客户名称
  leadId?: number;           // 来源线索ID
  stage: 'initial' | '需求确认' | '方案报价' | '谈判' | '成交' | '失败';  // 阶段
  winProbability: number;    // 赢单概率（%）
  estimatedAmount: number;   // 预计金额
  actualAmount?: number;     // 实际金额
  expectedCloseDate: string; // 预计成交日期
  actualCloseDate?: string;  // 实际成交日期
  ownerId: number;           // 负责人ID
  lossReason?: string;       // 失败原因
  status: 'ongoing' | 'won' | 'lost';  // 状态
  competitors: string;       // 竞争对手
  products: string;          // 关联产品（JSON字符串）
}

/**
 * 商机阶段记录
 */
export interface OpportunityStageHistoryEntity {
  id: number;
  opportunityId: number;       // 商机ID
  fromStage: string;         // 原阶段
  toStage: string;           // 新阶段
  operatorId: number;        // 操作人ID
  remark: string;            // 备注
  changeTime: string;        // 变更时间
}

/**
 * 销售漏斗分析数据
 */
export interface SalesFunnelAnalysisDTO {
  stage: string;             // 阶段
  count: number;            // 数量
  totalAmount: number;       // 总金额
  conversionRate: number;    // 转化率
  avgDaysInStage: number;   // 平均停留天数
}

/**
 * 销售预测
 */
export interface SalesForecastEntity {
  id: number;
  period: string;            // 预测周期：2024-Q1
  forecastType: 'conservative' | 'most_likely' | 'aggressive';  // 预测类型
  forecastAmount: number;    // 预测金额
  productCategory: string;   // 产品类别
  region: string;            // 地区
  basedOn: 'pipeline' | 'historical' | 'quota';  // 基于
  createTime: string;        // 创建时间
}

/**
 * 销售目标
 */
export interface SalesTargetEntity {
  id: number;
  period: string;            // 目标周期
  targetType: 'team' | 'individual';  // 类型：团队/个人
  targetUserId: number;        // 目标对象（个人/团队ID）
  targetAmount: number;        // 目标金额
  targetCount: number;         // 目标单数
  achievedAmount: number;      // 完成金额
  achievedCount: number;       // 完成单数
  achievementRate: number;     // 完成率
  status: 'ongoing' | 'completed';  // 状态
}

/**
 * 销售线索查询参数
 */
export interface LeadQueryParams {
  page?: number;
  size?: number;
  keyword?: string;
  status?: string;
  rating?: string;
  source?: string;
  ownerId?: number;
}

/**
 * 商机查询参数
 */
export interface OpportunityQueryParams {
  page?: number;
  size?: number;
  keyword?: string;
  stage?: string;
  status?: string;
  customerId?: number;
  ownerId?: number;
}
