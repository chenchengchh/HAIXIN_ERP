/**
 * 客户服务模块类型定义
 */

/**
 * 服务工单
 */
export interface ServiceTicketEntity {
  id: number;
  ticketNo: string;          // 工单编号
  customerId: number;        // 客户ID
  customerName: string;      // 客户名称
  ticketType: 'complaint' | 'consultation' | 'repair' | 'return';  // 类型
  priority: 'low' | 'medium' | 'high' | 'urgent';  // 优先级
  title: string;             // 标题
  description: string;       // 描述
  channel: 'phone' | 'email' | 'wechat' | 'website';  // 渠道
  reporterId: number;        // 报告人ID
  assigneeId?: number;       // 处理人ID
  status: 'open' | 'in_progress' | 'pending' | 'resolved' | 'closed';  // 状态
  openTime: string;          // 提交时间
  resolveTime?: string;      // 解决时间
  closeTime?: string;        // 关闭时间
  satisfactionScore?: number; // 满意度评分
}

/**
 * 工单回复
 */
export interface TicketReplyEntity {
  id: number;
  ticketId: number;          // 工单ID
  userId: number;            // 回复人ID
  content: string;           // 回复内容
  attachments: string[];     // 附件列表
  isInternal: boolean;       // 是否内部备注
  replyTime: string;         // 回复时间
}

/**
 * 知识库文章
 */
export interface KnowledgeArticleEntity {
  id: number;
  category: string;          // 分类
  title: string;             // 标题
  content: string;           // 内容（富文本）
  keywords: string;          // 关键词
  viewCount: number;         // 查看次数
  usefulCount: number;       // 有用次数
  status: 'draft' | 'published';  // 状态
  authorId: number;          // 作者ID
  publishTime: string;       // 发布时间
}

/**
 * 满意度调查
 */
export interface SatisfactionSurveyEntity {
  id: number;
  surveyType: 'after_sale' | 'periodic' | 'nps';  // 类型
  customerId: number;        // 客户ID
  ticketId?: number;         // 关联工单ID
  score: number;             // 评分（1-5）
  feedback: string;          // 反馈意见
  improvement: string;       // 改进建议
  surveyTime: string;        // 调查时间
}

/**
 * NPS调查
 */
export interface NPSSurveyEntity {
  id: number;
  customerId: number;        // 客户ID
  score: number;             // 评分（0-10）
  reason: string;            // 原因
  customerType: 'promoter' | 'passive' | 'detractor';  // 客户类型
  surveyTime: string;        // 调查时间
}

/**
 * 工单查询参数
 */
export interface TicketQueryParams {
  page?: number;
  size?: number;
  keyword?: string;
  ticketNo?: string;
  customerId?: number;
  ticketType?: string;
  priority?: string;
  status?: string;
  assigneeId?: number;
  reporterId?: number;
  dateRange?: string[];      // 时间范围 [start, end]
}

/**
 * 知识库查询参数
 */
export interface KnowledgeQueryParams {
  page?: number;
  size?: number;
  keyword?: string;
  category?: string;
  status?: string;
}
