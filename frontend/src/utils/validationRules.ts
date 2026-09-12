/**
 * CRM系统统一验证规则配置
 */

import type { RuleItem } from 'async-validator'

export const validationRules = {
  /**
   * 客户管理验证规则
   */
  customer: {
    customerName: [
      { required: true, message: '请输入客户名称', trigger: 'blur' },
      { min: 2, max: 50, message: '客户名称长度必须在2到50个字符之间', trigger: 'blur' }
    ],
    customerNo: [
      { required: true, message: '请输入客户编号', trigger: 'blur' },
      { pattern: /^[A-Za-z0-9_\-]+$/, message: '客户编号只能包含字母、数字、下划线和连字符', trigger: 'blur' },
      { min: 3, max: 20, message: '客户编号长度必须在3到20个字符之间', trigger: 'blur' }
    ],
    customerType: [
      { required: true, message: '请选择客户类型', trigger: 'change' },
      { type: 'enum', enum: ['enterprise', 'individual'], message: '请选择有效的客户类型', trigger: 'change' }
    ],
    industry: [
      { required: true, message: '请输入所属行业', trigger: 'blur' },
      { min: 1, max: 30, message: '所属行业长度必须在1到30个字符之间', trigger: 'blur' }
    ],
    scale: [
      { required: true, message: '请选择企业规模', trigger: 'change' },
      { type: 'enum', enum: ['large', 'medium', 'small'], message: '请选择有效的企业规模', trigger: 'change' }
    ],
    level: [
      { required: true, message: '请选择客户级别', trigger: 'change' },
      { type: 'enum', enum: ['A', 'B', 'C'], message: '请选择有效的客户级别', trigger: 'change' }
    ],
    status: [
      { required: true, message: '请选择客户状态', trigger: 'change' },
      { type: 'enum', enum: ['potential', 'active', 'inactive', 'lost'], message: '请选择有效的客户状态', trigger: 'change' }
    ],
    source: [
      { min: 1, max: 30, message: '客户来源长度必须在1到30个字符之间', trigger: 'blur' }
    ],
    website: [
      { type: 'url', message: '请输入有效的网址', trigger: 'blur' }
    ],
    region: [
      { min: 1, max: 50, message: '地区长度必须在1到50个字符之间', trigger: 'blur' }
    ],
    address: [
      { min: 5, max: 200, message: '详细地址长度必须在5到200个字符之间', trigger: 'blur' }
    ],
    ownerId: [
      { required: true, message: '请输入负责人ID', trigger: 'blur' },
      { type: 'number', message: '负责人ID必须为数字', trigger: 'blur' },
      { type: 'integer', message: '负责人ID必须为整数', trigger: 'blur' },
      { min: 1, message: '负责人ID必须大于0', trigger: 'blur' }
    ],
    ownerName: [
      { required: true, message: '请输入负责人姓名', trigger: 'blur' },
      { min: 2, max: 20, message: '负责人姓名长度必须在2到20个字符之间', trigger: 'blur' }
    ]
  },

  /**
   * 销售管理验证规则
   */
  sales: {
    // 商机验证规则
    opportunity: {
      opportunityName: [
        { required: true, message: '请输入商机名称', trigger: 'blur' },
        { min: 2, max: 100, message: '商机名称长度必须在2到100个字符之间', trigger: 'blur' }
      ],
      customerId: [
        { required: true, message: '请输入客户ID', trigger: 'blur' },
        { type: 'number', message: '客户ID必须为数字', trigger: 'blur' },
        { type: 'integer', message: '客户ID必须为整数', trigger: 'blur' },
        { min: 1, message: '客户ID必须大于0', trigger: 'blur' }
      ],
      customerName: [
        { required: true, message: '请输入客户名称', trigger: 'blur' },
        { min: 2, max: 50, message: '客户名称长度必须在2到50个字符之间', trigger: 'blur' }
      ],
      stage: [
        { required: true, message: '请选择当前阶段', trigger: 'change' },
        { type: 'enum', enum: ['initial', '需求确认', '方案报价', '谈判', '成交', '失败'], message: '请选择有效的销售阶段', trigger: 'change' }
      ],
      winProbability: [
        { required: true, message: '请输入赢单概率', trigger: 'blur' },
        { type: 'number', message: '赢单概率必须为数字', trigger: 'blur' },
        { min: 0, max: 100, message: '赢单概率必须在0到100之间', trigger: 'blur' }
      ],
      estimatedAmount: [
        { required: true, message: '请输入预计金额', trigger: 'blur' },
        { type: 'number', message: '预计金额必须为数字', trigger: 'blur' },
        { min: 0, message: '预计金额必须大于等于0', trigger: 'blur' }
      ],
      expectedCloseDate: [
        { required: true, message: '请选择预计成交日期', trigger: 'change' },
        { type: 'date', message: '请选择有效的日期', trigger: 'change' }
      ],
      status: [
        { required: true, message: '请选择商机状态', trigger: 'change' },
        { type: 'enum', enum: ['ongoing', 'won', 'lost'], message: '请选择有效的商机状态', trigger: 'change' }
      ],
      competitors: [
        { min: 1, max: 200, message: '竞争对手长度必须在1到200个字符之间', trigger: 'blur' }
      ],
      products: [
        { type: 'string', message: '关联产品必须为字符串', trigger: 'blur' }
      ]
    },

    // 线索验证规则
    lead: {
      leadName: [
        { required: true, message: '请输入线索名称', trigger: 'blur' },
        { min: 2, max: 50, message: '线索名称长度必须在2到50个字符之间', trigger: 'blur' }
      ],
      companyName: [
        { required: true, message: '请输入公司名称', trigger: 'blur' },
        { min: 2, max: 50, message: '公司名称长度必须在2到50个字符之间', trigger: 'blur' }
      ],
      contactName: [
        { required: true, message: '请输入联系人', trigger: 'blur' },
        { min: 2, max: 20, message: '联系人长度必须在2到20个字符之间', trigger: 'blur' }
      ],
      phone: [
        { required: true, message: '请输入电话', trigger: 'blur' },
        { pattern: /^1[3-9]\d{9}$/, message: '请输入有效的手机号码', trigger: 'blur' }
      ],
      email: [
        { type: 'email', message: '请输入有效的邮箱地址', trigger: 'blur' }
      ],
      source: [
        { required: true, message: '请输入线索来源', trigger: 'blur' },
        { min: 1, max: 30, message: '线索来源长度必须在1到30个字符之间', trigger: 'blur' }
      ],
      rating: [
        { required: true, message: '请选择线索评级', trigger: 'change' },
        { type: 'enum', enum: ['hot', 'warm', 'cold'], message: '请选择有效的线索评级', trigger: 'change' }
      ],
      status: [
        { required: true, message: '请选择线索状态', trigger: 'change' },
        { type: 'enum', enum: ['new', 'contacted', 'qualified', 'converted', 'lost'], message: '请选择有效的线索状态', trigger: 'change' }
      ]
    }
  },

  /**
   * 订单管理验证规则
   */
  order: {
    // 销售订单验证规则
    salesOrder: {
      orderNo: [
        { required: true, message: '请输入订单编号', trigger: 'blur' },
        { pattern: /^[A-Za-z0-9_\-]+$/, message: '订单编号只能包含字母、数字、下划线和连字符', trigger: 'blur' },
        { min: 3, max: 20, message: '订单编号长度必须在3到20个字符之间', trigger: 'blur' }
      ],
      customerId: [
        { required: true, message: '请输入客户ID', trigger: 'blur' },
        { type: 'number', message: '客户ID必须为数字', trigger: 'blur' },
        { type: 'integer', message: '客户ID必须为整数', trigger: 'blur' },
        { min: 1, message: '客户ID必须大于0', trigger: 'blur' }
      ],
      customerName: [
        { required: true, message: '请输入客户名称', trigger: 'blur' },
        { min: 2, max: 50, message: '客户名称长度必须在2到50个字符之间', trigger: 'blur' }
      ],
      totalAmount: [
        { required: true, message: '请输入订单总额', trigger: 'blur' },
        { type: 'number', message: '订单总额必须为数字', trigger: 'blur' },
        { min: 0, message: '订单总额必须大于等于0', trigger: 'blur' }
      ],
      discountAmount: [
        { type: 'number', message: '折扣金额必须为数字', trigger: 'blur' },
        { min: 0, message: '折扣金额必须大于等于0', trigger: 'blur' }
      ],
      finalAmount: [
        { required: true, message: '请输入最终金额', trigger: 'blur' },
        { type: 'number', message: '最终金额必须为数字', trigger: 'blur' },
        { min: 0, message: '最终金额必须大于等于0', trigger: 'blur' }
      ],
      orderDate: [
        { required: true, message: '请选择订单日期', trigger: 'change' },
        { type: 'date', message: '请选择有效的日期', trigger: 'change' }
      ],
      deliveryDate: [
        { required: true, message: '请选择交付日期', trigger: 'change' },
        { type: 'date', message: '请选择有效的日期', trigger: 'change' }
      ],
      status: [
        { required: true, message: '请选择订单状态', trigger: 'change' },
        { type: 'enum', enum: ['draft', 'submitted', 'approved', 'in_progress', 'completed', 'cancelled'], message: '请选择有效的订单状态', trigger: 'change' }
      ]
    },

    // 合同验证规则
    contract: {
      contractNo: [
        { required: true, message: '请输入合同编号', trigger: 'blur' },
        { pattern: /^[A-Za-z0-9_\-]+$/, message: '合同编号只能包含字母、数字、下划线和连字符', trigger: 'blur' },
        { min: 3, max: 20, message: '合同编号长度必须在3到20个字符之间', trigger: 'blur' }
      ],
      contractName: [
        { required: true, message: '请输入合同名称', trigger: 'blur' },
        { min: 2, max: 100, message: '合同名称长度必须在2到100个字符之间', trigger: 'blur' }
      ],
      contractAmount: [
        { required: true, message: '请输入合同金额', trigger: 'blur' },
        { type: 'number', message: '合同金额必须为数字', trigger: 'blur' },
        { min: 0, message: '合同金额必须大于等于0', trigger: 'blur' }
      ],
      signDate: [
        { required: true, message: '请选择签订日期', trigger: 'change' },
        { type: 'date', message: '请选择有效的日期', trigger: 'change' }
      ],
      startDate: [
        { required: true, message: '请选择开始日期', trigger: 'change' },
        { type: 'date', message: '请选择有效的日期', trigger: 'change' }
      ],
      endDate: [
        { required: true, message: '请选择结束日期', trigger: 'change' },
        { type: 'date', message: '请选择有效的日期', trigger: 'change' }
      ],
      status: [
        { required: true, message: '请选择合同状态', trigger: 'change' },
        { type: 'enum', enum: ['drafting', 'reviewing', 'signed', 'executing', 'completed', 'terminated'], message: '请选择有效的合同状态', trigger: 'change' }
      ]
    }
  },

  /**
   * 客户服务验证规则
   */
  service: {
    // 工单验证规则
    ticket: {
      ticketNo: [
        { required: true, message: '请输入工单编号', trigger: 'blur' },
        { pattern: /^[A-Za-z0-9_\-]+$/, message: '工单编号只能包含字母、数字、下划线和连字符', trigger: 'blur' },
        { min: 3, max: 20, message: '工单编号长度必须在3到20个字符之间', trigger: 'blur' }
      ],
      customerId: [
        { required: true, message: '请输入客户ID', trigger: 'blur' },
        { type: 'number', message: '客户ID必须为数字', trigger: 'blur' },
        { type: 'integer', message: '客户ID必须为整数', trigger: 'blur' },
        { min: 1, message: '客户ID必须大于0', trigger: 'blur' }
      ],
      customerName: [
        { required: true, message: '请输入客户名称', trigger: 'blur' },
        { min: 2, max: 50, message: '客户名称长度必须在2到50个字符之间', trigger: 'blur' }
      ],
      ticketType: [
        { required: true, message: '请选择工单类型', trigger: 'change' },
        { type: 'enum', enum: ['complaint', 'consultation', 'repair', 'return'], message: '请选择有效的工单类型', trigger: 'change' }
      ],
      priority: [
        { required: true, message: '请选择优先级', trigger: 'change' },
        { type: 'enum', enum: ['low', 'medium', 'high', 'urgent'], message: '请选择有效的优先级', trigger: 'change' }
      ],
      title: [
        { required: true, message: '请输入工单标题', trigger: 'blur' },
        { min: 5, max: 100, message: '工单标题长度必须在5到100个字符之间', trigger: 'blur' }
      ],
      description: [
        { required: true, message: '请输入工单描述', trigger: 'blur' },
        { min: 10, max: 500, message: '工单描述长度必须在10到500个字符之间', trigger: 'blur' }
      ],
      status: [
        { required: true, message: '请选择工单状态', trigger: 'change' },
        { type: 'enum', enum: ['open', 'in_progress', 'pending', 'resolved', 'closed'], message: '请选择有效的工单状态', trigger: 'change' }
      ]
    },

    // 知识库验证规则
    knowledge: {
      title: [
        { required: true, message: '请输入文章标题', trigger: 'blur' },
        { min: 5, max: 100, message: '文章标题长度必须在5到100个字符之间', trigger: 'blur' }
      ],
      content: [
        { required: true, message: '请输入文章内容', trigger: 'blur' },
        { min: 50, message: '文章内容长度必须至少为50个字符', trigger: 'blur' }
      ],
      category: [
        { required: true, message: '请选择文章分类', trigger: 'change' },
        { min: 1, max: 30, message: '文章分类长度必须在1到30个字符之间', trigger: 'blur' }
      ],
      keywords: [
        { min: 1, max: 100, message: '关键词长度必须在1到100个字符之间', trigger: 'blur' }
      ],
      status: [
        { required: true, message: '请选择文章状态', trigger: 'change' },
        { type: 'enum', enum: ['draft', 'published'], message: '请选择有效的文章状态', trigger: 'change' }
      ]
    }
  }
}
