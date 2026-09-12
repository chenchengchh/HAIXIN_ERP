/**
 * 客户管理模块类型定义
 */

/**
 * 客户基础信息
 */
export interface CustomerEntity {
  id: number;
  customerNo: string;        // 客户编号
  customerName: string;      // 客户名称
  customerType: 'enterprise' | 'individual';  // 类型：企业/个人
  industry: string;          // 行业
  scale: 'large' | 'medium' | 'small';  // 规模
  level: 'A' | 'B' | 'C';    // 级别
  status: 'potential' | 'active' | 'inactive' | 'lost';  // 状态
  source: string;            // 来源
  tags: string[];            // 标签列表
  region: string;            // 地区
  address: string;           // 详细地址
  website: string;           // 网站
  ownerId: number;           // 责任人ID
  ownerName: string;         // 责任人姓名
  createTime: string;        // 创建时间
  updateTime: string;        // 更新时间
}

/**
 * 客户联系人
 */
export interface CustomerContactEntity {
  id: number;
  customerId: number;        // 客户ID
  contactName: string;       // 姓名
  position: string;          // 职位
  phone: string;             // 电话
  mobile: string;            // 手机
  email: string;             // 邮箱
  wechat: string;            // 微信
  isPrimary: boolean;        // 是否主要联系人
  remark: string;            // 备注
}

/**
 * 客户跟进记录
 */
export interface CustomerFollowUpEntity {
  id: number;
  customerId: number;        // 客户ID
  followUpUserId: number;    // 跟进人ID
  followUpType: 'call' | 'email' | 'visit' | 'wechat';  // 跟进方式
  content: string;           // 跟进内容
  nextPlan: string;          // 下次计划
  followUpTime: string;      // 跟进时间
  nextTime: string;          // 下次跟进时间
}

/**
 * 客户交易记录
 */
export interface CustomerTransactionEntity {
  id: number;
  customerId: number;        // 客户ID
  orderNo: string;           // 订单编号
  amount: number;            // 交易金额
  dealDate: string;          // 成交日期
  productInfo: string;       // 产品信息（JSON字符串）
  status: 'pending' | 'completed' | 'cancelled';  // 状态
}

/**
 * 客户标签
 */
export interface CustomerTagEntity {
  id: number;
  tagName: string;           // 标签名称
  tagType: 'system' | 'custom';  // 标签类型
  tagCategory: string;       // 标签分类
  color: string;             // 显示颜色
  sortOrder: number;        // 排序
}

/**
 * 客户360°视图数据
 */
export interface Customer360View {
  customer: CustomerEntity;                      // 客户基础信息
  contacts: CustomerContactEntity[];            // 联系人列表
  followUps: CustomerFollowUpEntity[];          // 跟进记录
  transactions: CustomerTransactionEntity[];    // 交易记录
  tags: CustomerTagEntity[];                    // 标签列表
}

/**
 * 客户查询参数
 */
export interface CustomerQueryParams {
  page?: number;
  size?: number;
  keyword?: string;
  customerType?: string;
  industry?: string;
  scale?: string;
  level?: string;
  status?: string;
  region?: string;
  ownerId?: number;
}
