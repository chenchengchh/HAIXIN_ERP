// 企业管理系统知识库
// 包含所有模块的详细信息，用于AI语音导航和智能问答

// 模块类型定义
export interface ModuleInfo {
  name: string;          // 模块名称
  fullName: string;      // 模块全称
  category: string;      // 模块类别
  description: string;   // 模块描述
  features: string[];    // 主要功能列表
  keywords: string[];    // 语音关键词
  routePath: string;     // 路由路径
  parentModule?: string; // 父模块名称
}

// 模块类别定义
export const ModuleCategories = {
  SUPPLY_CHAIN: '供应链与采购',
  DATA_DECISION: '数据与决策',
  PRODUCT_RD: '产品与研发',
  ENTERPRISE_RESOURCE: '企业资源',
  CUSTOMER_SALES: '客户与销售',
  PRODUCTION_QUALITY: '生产与质量',
  WAREHOUSE_LOGISTICS: '仓储与物流',
  EQUIPMENT_ENERGY: '设备与能源'
};

// 完整的系统知识库
export const knowledgeBase: ModuleInfo[] = [
  // 企业资源类模块
  {
    name: 'ERP',
    fullName: '企业资源计划',
    category: ModuleCategories.ENTERPRISE_RESOURCE,
    description: '整合企业核心资源，优化业务流程，提升运营效率',
    features: [
      '财务管理：总账、应收应付、成本核算、固定资产',
      '供应链管理：采购、销售、库存、MRP计划',
      '生产管理：生产订单、车间管理、产能规划',
      '基础数据：组织架构、客户/供应商、物料主数据'
    ],
    keywords: ['ERP', 'ERP系统', '企业资源计划'],
    routePath: '/home/erp'
  },
  {
    name: 'ERP-Finance',
    fullName: 'ERP财务模块',
    category: ModuleCategories.ENTERPRISE_RESOURCE,
    description: '全方位财务核算与资金管控中心',
    features: [
      '总账管理',
      '应收管理',
      '应付管理',
      '成本核算',
      '固定资产管理'
    ],
    keywords: ['财务', '财务管理', '总账', '总账管理', '应收', '应收管理', '应付', '应付管理', '成本核算', '固定资产'],
    routePath: '/home/erp/finance',
    parentModule: 'ERP'
  },
  {
    name: 'ERP-SupplyChain',
    fullName: 'ERP供应链模块',
    category: ModuleCategories.ENTERPRISE_RESOURCE,
    description: '采购销售库存一体化协同管理',
    features: [
      '采购管理',
      '销售管理',
      '库存管理',
      'MRP计划'
    ],
    keywords: ['供应链', '供应链管理', '采购管理', '销售管理', '库存管理', 'MRP', 'MRP计划'],
    routePath: '/home/erp/supply-chain',
    parentModule: 'ERP'
  },
  {
    name: 'ERP-Production',
    fullName: 'ERP生产模块',
    category: ModuleCategories.ENTERPRISE_RESOURCE,
    description: '生产计划与执行全过程精细管控',
    features: [
      '生产订单管理',
      '车间管理',
      '产能管理',
      '产能规划'
    ],
    keywords: ['生产', '生产管理', '生产订单', '车间管理', '产能管理', '产能规划'],
    routePath: '/home/erp/production',
    parentModule: 'ERP'
  },
  {
    name: 'ERP-BasicData',
    fullName: 'ERP基础数据模块',
    category: ModuleCategories.ENTERPRISE_RESOURCE,
    description: '企业主数据标准化与规范化管理',
    features: [
      '组织架构管理',
      '客户管理',
      '供应商管理',
      '物料主数据管理',
      '会计科目管理'
    ],
    keywords: ['基础数据', '基础数据管理', '组织架构', '客户管理', '供应商管理', '物料管理', '物料主数据', '会计科目'],
    routePath: '/home/erp/basic-data',
    parentModule: 'ERP'
  },
  {
    name: 'HR',
    fullName: '人力资源管理',
    category: ModuleCategories.ENTERPRISE_RESOURCE,
    description: '管理员工信息、考勤、薪酬、招聘等',
    features: [
      '员工管理',
      '考勤管理',
      '薪酬管理',
      '招聘管理',
      '绩效管理'
    ],
    keywords: ['HR', '人力资源', '人事', '人力资源管理', '人事管理'],
    routePath: '/home/hr'
  },
  {
    name: 'HR-OrgEmployee',
    fullName: '组织与员工管理',
    category: ModuleCategories.ENTERPRISE_RESOURCE,
    description: '管理企业组织架构和员工信息',
    features: [
      '组织架构管理',
      '员工信息管理',
      '员工档案管理',
      '个人中心'
    ],
    keywords: ['组织架构', '员工管理', '员工', '个人中心', '员工档案', '人事档案'],
    routePath: '/home/hr/org-employee',
    parentModule: 'HR'
  },
  {
    name: 'HR-Recruitment',
    fullName: '招聘管理',
    category: ModuleCategories.ENTERPRISE_RESOURCE,
    description: '管理企业招聘流程和人才储备',
    features: [
      '招聘需求管理',
      '简历管理',
      '面试流程管理',
      '人才库管理'
    ],
    keywords: ['招聘', '招聘管理', '简历', '面试', '人才库', '招聘需求'],
    routePath: '/home/hr/recruitment',
    parentModule: 'HR'
  },
  {
    name: 'HR-Attendance',
    fullName: '考勤与假期管理',
    category: ModuleCategories.ENTERPRISE_RESOURCE,
    description: '管理员工考勤记录和假期申请',
    features: [
      '考勤记录管理',
      '请假申请审批',
      '加班管理',
      '考勤统计分析'
    ],
    keywords: ['考勤', '请假', '加班', '考勤记录', '假期管理', '考勤统计'],
    routePath: '/home/hr/attendance-leave',
    parentModule: 'HR'
  },
  {
    name: 'HR-Compensation',
    fullName: '薪酬福利管理',
    category: ModuleCategories.ENTERPRISE_RESOURCE,
    description: '管理员工薪酬核算和福利政策',
    features: [
      '薪资核算',
      '社保公积金管理',
      '福利政策管理',
      '薪酬报表分析'
    ],
    keywords: ['薪酬', '薪酬管理', '工资', '社保', '公积金', '福利', '薪资核算'],
    routePath: '/home/hr/compensation',
    parentModule: 'HR'
  },
  {
    name: 'APS',
    fullName: '高级计划与排程',
    category: ModuleCategories.ENTERPRISE_RESOURCE,
    description: '制定生产计划、排程优化、资源分配等',
    features: [
      '计划生成',
      '动态优化',
      '计划监控',
      '基础数据管理'
    ],
    keywords: ['APS', '高级计划与排程', '计划排程', '生产计划'],
    routePath: '/home/aps'
  },
  {
    name: 'APS-BasicData',
    fullName: 'APS基础数据配置',
    category: ModuleCategories.ENTERPRISE_RESOURCE,
    description: '配置APS系统所需的基础数据',
    features: [
      '资源数据配置',
      '工艺路线配置',
      '约束条件配置',
      '日历数据配置'
    ],
    keywords: ['APS基础数据', '基础数据配置', 'APS数据配置', '资源配置'],
    routePath: '/home/aps/basic-data',
    parentModule: 'APS'
  },
  {
    name: 'APS-PlanGeneration',
    fullName: 'APS计划生成',
    category: ModuleCategories.ENTERPRISE_RESOURCE,
    description: '生成优化的生产计划和排程',
    features: [
      '生产计划生成',
      '产能规划',
      '资源分配',
      '排程优化'
    ],
    keywords: ['计划生成', '生产计划生成', '排程生成', '产能规划'],
    routePath: '/home/aps/plan-generation',
    parentModule: 'APS'
  },
  {
    name: 'APS-DynamicOptimization',
    fullName: 'APS动态优化',
    category: ModuleCategories.ENTERPRISE_RESOURCE,
    description: '对生产计划进行动态调整和优化',
    features: [
      '计划调整',
      '动态排程',
      '冲突解决',
      '优化建议'
    ],
    keywords: ['动态优化', '计划调整', '动态排程', '冲突解决'],
    routePath: '/home/aps/dynamic-optimization',
    parentModule: 'APS'
  },
  {
    name: 'APS-PlanMonitoring',
    fullName: 'APS计划执行监控',
    category: ModuleCategories.ENTERPRISE_RESOURCE,
    description: '监控生产计划的执行情况',
    features: [
      '计划执行跟踪',
      '进度监控',
      '异常预警',
      '执行分析'
    ],
    keywords: ['计划监控', '计划执行监控', '进度监控', '异常预警'],
    routePath: '/home/aps/plan-monitoring',
    parentModule: 'APS'
  },
  {
    name: 'OA',
    fullName: '办公自动化',
    category: ModuleCategories.ENTERPRISE_RESOURCE,
    description: '管理流程审批、文档、会议、协同办公等',
    features: [
      '流程审批',
      '公文管理',
      '协同办公',
      '移动办公',
      '行政后勤'
    ],
    keywords: ['OA', '办公自动化', 'OA系统', '办公系统'],
    routePath: '/home/oa'
  },
  {
    name: 'OA-Approval',
    fullName: 'OA流程审批',
    category: ModuleCategories.ENTERPRISE_RESOURCE,
    description: '管理企业各种审批流程，包括请假、报销、采购申请等',
    features: [
      '审批流程管理',
      '待办审批任务',
      '已办审批任务',
      '审批历史记录',
      '审批统计分析'
    ],
    keywords: ['审批', '流程审批', '待办审批', '已办审批', '审批流程', '请假审批', '报销审批', '采购审批'],
    routePath: '/home/oa/approval',
    parentModule: 'OA'
  },
  {
    name: 'OA-Document',
    fullName: 'OA文档管理',
    category: ModuleCategories.ENTERPRISE_RESOURCE,
    description: '管理企业各类文档，包括公文、制度、知识库等',
    features: [
      '文档上传下载',
      '文档分类管理',
      '文档版本控制',
      '文档权限管理',
      '文档搜索功能'
    ],
    keywords: ['文档', '文档管理', '公文管理', '知识库', '文档上传', '文档下载'],
    routePath: '/home/oa/document',
    parentModule: 'OA'
  },
  {
    name: 'OA-Collaboration',
    fullName: 'OA协同办公',
    category: ModuleCategories.ENTERPRISE_RESOURCE,
    description: '提供企业内部协同办公功能，包括会议管理、日程安排、任务分配等',
    features: [
      '会议管理',
      '日程安排',
      '任务分配',
      '即时通讯',
      '团队协作'
    ],
    keywords: ['协同办公', '会议管理', '日程安排', '任务分配', '团队协作', '即时通讯'],
    routePath: '/home/oa/collaboration',
    parentModule: 'OA'
  },
  {
    name: 'OA-Mobile',
    fullName: 'OA移动办公',
    category: ModuleCategories.ENTERPRISE_RESOURCE,
    description: '提供移动端办公功能，支持随时随地处理办公事务',
    features: [
      '移动审批',
      '移动文档',
      '移动协同',
      '消息推送',
      '移动端考勤'
    ],
    keywords: ['移动办公', '移动端', '移动审批', '移动文档', '手机办公'],
    routePath: '/home/oa/mobile',
    parentModule: 'OA'
  },
  {
    name: 'OA-Admin',
    fullName: 'OA行政后勤',
    category: ModuleCategories.ENTERPRISE_RESOURCE,
    description: '管理企业行政后勤事务，包括固定资产、车辆管理、办公用品等',
    features: [
      '固定资产管理',
      '车辆管理',
      '办公用品管理',
      '会议室预订',
      '行政费用管理'
    ],
    keywords: ['行政后勤', '固定资产', '车辆管理', '办公用品', '会议室预订', '行政费用'],
    routePath: '/home/oa/admin',
    parentModule: 'OA'
  },
  
  // 客户与销售类模块
  {
    name: 'CRM',
    fullName: '客户关系管理',
    category: ModuleCategories.CUSTOMER_SALES,
    description: '管理客户信息、销售订单、合同等',
    features: [
      '客户管理',
      '销售管理',
      '订单管理',
      '客户服务',
      '销售机会管理'
    ],
    keywords: ['CRM', '客户关系管理', 'CRM系统', '客户关系系统'],
    routePath: '/home/crm'
  },
  {
    name: 'CRM-Customer',
    fullName: 'CRM客户管理',
    category: ModuleCategories.CUSTOMER_SALES,
    description: '管理客户信息、客户分类、客户跟进等',
    features: [
      '客户信息管理',
      '客户分类管理',
      '客户跟进记录',
      '客户360视图',
      '客户标签管理'
    ],
    keywords: ['客户管理', '客户信息', '客户分类', '客户跟进', '客户360', '客户标签'],
    routePath: '/home/crm/customer',
    parentModule: 'CRM'
  },
  {
    name: 'CRM-Sales',
    fullName: 'CRM销售管理',
    category: ModuleCategories.CUSTOMER_SALES,
    description: '管理销售线索、销售机会、销售流程等',
    features: [
      '销售线索管理',
      '销售机会管理',
      '销售流程管理',
      '销售预测',
      '销售报表分析'
    ],
    keywords: ['销售管理', '销售线索', '销售机会', '销售流程', '销售预测', '销售报表'],
    routePath: '/home/crm/sales',
    parentModule: 'CRM'
  },
  {
    name: 'CRM-Order',
    fullName: 'CRM订单管理',
    category: ModuleCategories.CUSTOMER_SALES,
    description: '管理销售订单、订单执行、订单跟踪等',
    features: [
      '销售订单管理',
      '订单执行跟踪',
      '订单状态管理',
      '订单报表分析',
      '订单收款管理'
    ],
    keywords: ['订单管理', '销售订单', '订单跟踪', '订单状态', '订单报表', '订单收款'],
    routePath: '/home/crm/order',
    parentModule: 'CRM'
  },
  {
    name: 'CRM-Service',
    fullName: 'CRM客户服务',
    category: ModuleCategories.CUSTOMER_SALES,
    description: '管理客户服务请求、投诉处理、服务工单等',
    features: [
      '客户服务请求管理',
      '投诉处理',
      '服务工单管理',
      '服务质量评价',
      '服务报表分析'
    ],
    keywords: ['客户服务', '服务请求', '投诉处理', '服务工单', '服务质量', '服务报表'],
    routePath: '/home/crm/service',
    parentModule: 'CRM'
  },
  {
    name: 'SCRM',
    fullName: '社会化客户关系管理',
    category: ModuleCategories.CUSTOMER_SALES,
    description: '管理社交媒体客户互动、营销活动等',
    features: [
      '私域流量管理',
      '客户裂变',
      '精准营销',
      '数据同步'
    ],
    keywords: ['SCRM', '私域流量', '客户裂变', '精准营销', '数据同步'],
    routePath: '/home/scrm'
  },
  {
    name: 'BusinessOpportunity',
    fullName: '商机管理',
    category: ModuleCategories.CUSTOMER_SALES,
    description: '管理销售线索、商机跟踪、转化分析等',
    features: [
      '商机录入',
      '商机跟踪',
      '商机分析',
      '商机预测'
    ],
    keywords: ['商机', '商机录入', '商机跟踪', '商机分析', '商机预测'],
    routePath: '/home/business-opportunity'
  },
  
  // 供应链与采购类模块
  {
    name: 'SRM',
    fullName: '供应商关系管理',
    category: ModuleCategories.SUPPLY_CHAIN,
    description: '管理供应商信息、采购订单、供应商评估等',
    features: [
      '供应商管理',
      '采购订单管理',
      '询报价管理',
      '供应商评估'
    ],
    keywords: ['SRM', '供应商关系管理', '供应链管理', '采购管理', '供应商系统'],
    routePath: '/home/srm'
  },
  {
    name: 'SRM-Order',
    fullName: 'SRM订单管理',
    category: ModuleCategories.SUPPLY_CHAIN,
    description: '管理采购订单的创建、审核、执行和跟踪',
    features: [
      '采购订单创建',
      '订单状态跟踪',
      '订单审核流程',
      '订单历史记录',
      '订单详情查看'
    ],
    keywords: ['订单', '采购订单', '订单管理', '订单列表', '订单详情', '订单跟踪'],
    routePath: '/home/srm/order',
    parentModule: 'SRM'
  },
  {
    name: 'SRM-Supplier',
    fullName: 'SRM供应商管理',
    category: ModuleCategories.SUPPLY_CHAIN,
    description: '管理供应商准入、资质审核、信用评估和黑名单管理',
    features: [
      '供应商准入流程',
      '供应商列表管理',
      '供应商资质审核',
      '供应商信用评估',
      '供应商黑名单管理'
    ],
    keywords: ['供应商', '供应商列表', '供应商准入', '供应商资质', '供应商信用', '供应商黑名单', '供应商管理'],
    routePath: '/home/srm/supplier-access',
    parentModule: 'SRM'
  },
  {
    name: 'SRM-Purchase',
    fullName: 'SRM采购流程自动化',
    category: ModuleCategories.SUPPLY_CHAIN,
    description: '自动化采购流程，包括采购申请、询价、报价和价格对比',
    features: [
      '采购自动化管理',
      '采购申请流程',
      '询价管理',
      '报价管理',
      '价格对比分析'
    ],
    keywords: ['采购自动化', '采购申请', '询价', '报价', '价格对比', '采购流程', '采购管理'],
    routePath: '/home/srm/purchase-automation',
    parentModule: 'SRM'
  },
  {
    name: 'SRM-Collaboration',
    fullName: 'SRM供应商协同管理',
    category: ModuleCategories.SUPPLY_CHAIN,
    description: '实现与供应商的协同管理，包括信息共享、文档协作等',
    features: [
      '供应商信息共享',
      '文档协同管理',
      '实时沟通协作',
      '协同任务管理'
    ],
    keywords: ['供应商协同', '协同管理', '信息共享', '文档协作', '供应商沟通'],
    routePath: '/home/srm/collaboration',
    parentModule: 'SRM'
  },
  {
    name: 'SRM-Performance',
    fullName: 'SRM绩效与风险管理',
    category: ModuleCategories.SUPPLY_CHAIN,
    description: '管理供应商绩效评估和风险管理',
    features: [
      '供应商绩效评估',
      '风险预警管理',
      '绩效报表分析',
      '风险评估模型'
    ],
    keywords: ['绩效风险', '供应商绩效', '风险评估', '绩效分析', '风险预警'],
    routePath: '/home/srm/performance-risk',
    parentModule: 'SRM'
  },
  {
    name: 'SCM',
    fullName: '供应链管理',
    category: ModuleCategories.SUPPLY_CHAIN,
    description: '管理供应链计划、需求预测、物流协同等',
    features: [
      '需求预测',
      '供应计划',
      '库存优化',
      '物流协同'
    ],
    keywords: ['SCM', '供应链管理', '供应链管理系统', '供应链计划'],
    routePath: '/home/scm'
  },
  {
    name: 'SCM-DemandForecast',
    fullName: 'SCM需求预测',
    category: ModuleCategories.SUPPLY_CHAIN,
    description: '预测市场需求，优化供应链计划',
    features: [
      '需求预测分析',
      '预测仪表板',
      '预测工作表',
      '预测准确性评估'
    ],
    keywords: ['需求预测', '预测仪表板', '预测工作表', '预测准确性'],
    routePath: '/home/scm/demand-forecast',
    parentModule: 'SCM'
  },
  {
    name: 'SCM-SupplyPlanning',
    fullName: 'SCM供应计划',
    category: ModuleCategories.SUPPLY_CHAIN,
    description: '制定优化的供应计划，确保物料及时供应',
    features: [
      'MRP运算',
      '供应计划生成',
      '计划结果分析',
      '计划调整与优化'
    ],
    keywords: ['供应计划', 'MRP运算', '计划结果'],
    routePath: '/home/scm/planning',
    parentModule: 'SCM'
  },
  {
    name: 'SCM-InventoryOptimization',
    fullName: 'SCM库存优化',
    category: ModuleCategories.SUPPLY_CHAIN,
    description: '优化库存水平，降低库存成本，提高库存周转率',
    features: [
      '库存策略制定',
      '库存健康检查',
      '库存优化分析',
      '安全库存管理'
    ],
    keywords: ['库存优化', '库存策略', '库存健康'],
    routePath: '/home/scm/inventory-optimization',
    parentModule: 'SCM'
  },
  {
    name: 'SCM-LogisticsCollaboration',
    fullName: 'SCM物流协同',
    category: ModuleCategories.SUPPLY_CHAIN,
    description: '实现供应链各环节的物流协同与优化',
    features: [
      '物流计划制定',
      '物流执行跟踪',
      '物流协同平台',
      '物流成本分析'
    ],
    keywords: ['物流协同'],
    routePath: '/home/scm/logistics-collaboration',
    parentModule: 'SCM'
  },
  {
    name: 'SCM-Visualization',
    fullName: 'SCM供应链可视化',
    category: ModuleCategories.SUPPLY_CHAIN,
    description: '可视化展示供应链运行情况，提高供应链透明度',
    features: [
      '供应链数据可视化',
      '运行状态监控',
      '异常预警',
      '数据分析报告'
    ],
    keywords: ['供应链可视化'],
    routePath: '/home/scm/supply-chain-visualization',
    parentModule: 'SCM'
  },
  {
    name: 'SCM-ControlTower',
    fullName: 'SCM供应链塔台',
    category: ModuleCategories.SUPPLY_CHAIN,
    description: '提供供应链全局视图，实现供应链集中管控',
    features: [
      '全局视图监控',
      '塔台地图展示',
      '异常事件处理',
      '决策支持分析'
    ],
    keywords: ['供应链塔台', '塔台地图'],
    routePath: '/home/scm/control-tower',
    parentModule: 'SCM'
  },
  
  // 产品与研发类模块
  {
    name: 'PLM',
    fullName: '产品全生命周期管理',
    category: ModuleCategories.PRODUCT_RD,
    description: '管理产品研发过程、文档、版本等',
    features: [
      '研发项目管理',
      '产品数据管理',
      '工艺协同',
      '试产管理'
    ],
    keywords: ['PLM', '产品全生命周期管理', '产品生命周期', '产品研发管理'],
    routePath: '/home/plm'
  },
  {
    name: 'PLM-Project',
    fullName: 'PLM研发项目管理',
    category: ModuleCategories.PRODUCT_RD,
    description: '管理研发项目的全生命周期，包括项目规划、执行、监控和收尾',
    features: [
      '项目看板管理',
      '甘特图计划',
      '资源分配管理',
      '项目进度跟踪',
      '项目详情管理'
    ],
    keywords: ['研发项目', '项目管理', '项目看板', '甘特图', '资源管理'],
    routePath: '/home/plm/project-management',
    parentModule: 'PLM'
  },
  {
    name: 'PLM-ProductData',
    fullName: 'PLM产品数据管理',
    category: ModuleCategories.PRODUCT_RD,
    description: '管理产品数据，包括产品列表、产品BOM、文档查看等',
    features: [
      '产品数据管理',
      '产品列表管理',
      '产品BOM管理',
      '文档查看与管理'
    ],
    keywords: ['产品数据', '产品列表', '产品BOM', '文档查看'],
    routePath: '/home/plm/product-data',
    parentModule: 'PLM'
  },
  {
    name: 'PLM-Process',
    fullName: 'PLM工艺协同',
    category: ModuleCategories.PRODUCT_RD,
    description: '管理产品工艺路线、工艺文件、工艺变更等',
    features: [
      '工艺路线管理',
      '工艺文件管理',
      '工艺变更管理'
    ],
    keywords: ['工艺协同', '工艺路线', '工艺文件', '工艺变更'],
    routePath: '/home/plm/process-collaboration',
    parentModule: 'PLM'
  },
  {
    name: 'PLM-TrialProduction',
    fullName: 'PLM试产管理',
    category: ModuleCategories.PRODUCT_RD,
    description: '管理产品试产计划、试产报告、质量跟踪等',
    features: [
      '试产计划管理',
      '试产报告管理',
      '质量跟踪管理'
    ],
    keywords: ['试产管理', '试产计划', '试产报告', '质量跟踪'],
    routePath: '/home/plm/trial-production',
    parentModule: 'PLM'
  },
  {
    name: 'BOM',
    fullName: '物料清单管理',
    category: ModuleCategories.PRODUCT_RD,
    description: '管理物料结构、工艺路线、替代物料等',
    features: [
      '物料管理',
      'BOM结构管理',
      'BOM版本管理',
      '替代料管理',
      'BOM分析'
    ],
    keywords: ['BOM', '物料', '物料清单', '配方', 'BOM编辑器', 'BOM结构', 'BOM比较', '物料分类', '物料详情', 'BOM版本', '版本管理', '替代料', '替代料管理', 'BOM分析', 'BOM成本', '成本分析'],
    routePath: '/home/bom'
  },
  { // BOM子模块 - 物料管理
    name: 'BOM-Material',
    fullName: 'BOM物料管理',
    category: ModuleCategories.PRODUCT_RD,
    description: '管理物料基本信息、分类和详情',
    features: [
      '物料列表管理',
      '物料分类管理',
      '物料详情管理'
    ],
    keywords: ['物料管理', '物料列表', '物料分类', '物料详情', '物料信息', 'BOM物料'],
    routePath: '/home/bom/material',
    parentModule: 'BOM'
  },
  { // BOM子模块 - BOM结构管理
    name: 'BOM-Structure',
    fullName: 'BOM结构管理',
    category: ModuleCategories.PRODUCT_RD,
    description: '管理BOM结构编辑和比较',
    features: [
      'BOM结构编辑',
      'BOM结构比较'
    ],
    keywords: ['BOM结构', 'BOM编辑器', '结构编辑', '结构比较', 'BOM对比'],
    routePath: '/home/bom/structure',
    parentModule: 'BOM'
  },
  { // BOM子模块 - BOM版本管理
    name: 'BOM-Version',
    fullName: 'BOM版本管理',
    category: ModuleCategories.PRODUCT_RD,
    description: '管理BOM版本列表、详情和历史记录',
    features: [
      'BOM版本列表',
      'BOM版本详情',
      'BOM版本历史'
    ],
    keywords: ['BOM版本', '版本管理', '版本列表', '版本详情', '版本历史', 'BOM变更历史'],
    routePath: '/home/bom/version',
    parentModule: 'BOM'
  },
  { // BOM子模块 - 替代料管理
    name: 'BOM-Substitute',
    fullName: 'BOM替代料管理',
    category: ModuleCategories.PRODUCT_RD,
    description: '管理替代物料列表和详情',
    features: [
      '替代料列表',
      '替代料详情'
    ],
    keywords: ['替代料', '替代料管理', '替代物料', 'BOM替代料', '物料替代'],
    routePath: '/home/bom/substitute',
    parentModule: 'BOM'
  },
  { // BOM子模块 - BOM分析
    name: 'BOM-Analysis',
    fullName: 'BOM分析',
    category: ModuleCategories.PRODUCT_RD,
    description: '管理BOM使用位置、成本分析和比较',
    features: [
      'BOM使用位置分析',
      'BOM成本分析',
      'BOM比较分析'
    ],
    keywords: ['BOM分析', 'BOM成本', '成本分析', '使用位置', 'BOM位置分析', 'BOM成本核算'],
    routePath: '/home/bom/analysis',
    parentModule: 'BOM'
  },
  
  // 生产与质量类模块
  {
    name: 'MES',
    fullName: '制造执行系统',
    category: ModuleCategories.PRODUCTION_QUALITY,
    description: '管理生产执行、数据采集、设备监控等',
    features: [
      '生产执行',
      '生产报工',
      '过程监控',
      '在制品管理',
      '数据采集'
    ],
    keywords: ['MES', '制造执行', '生产执行', '生产报工', '报工', '过程监控', '在制品', '在制品管理', '生产数据采集'],
    routePath: '/home/mes'
  },
  { // MES子模块 - 生产执行模块
    name: 'MES-Execution',
    fullName: 'MES生产执行',
    category: ModuleCategories.PRODUCTION_QUALITY,
    description: '管理生产订单执行和生产过程控制',
    features: [
      '生产订单管理',
      '工序管理',
      '生产调度',
      '生产状态监控'
    ],
    keywords: ['生产执行', '生产订单', '工序管理', '生产调度', '生产状态'],
    routePath: '/home/mes/execution',
    parentModule: 'MES'
  },
  { // MES子模块 - 生产报工模块
    name: 'MES-Reporting',
    fullName: 'MES生产报工',
    category: ModuleCategories.PRODUCTION_QUALITY,
    description: '管理生产进度报工和产量统计',
    features: [
      '生产报工录入',
      '报工审核',
      '产量统计',
      '工时统计'
    ],
    keywords: ['生产报工', '报工', '产量统计', '工时统计', '报工录入'],
    routePath: '/home/mes/reporting',
    parentModule: 'MES'
  },
  { // MES子模块 - 过程监控模块
    name: 'MES-Monitoring',
    fullName: 'MES过程监控',
    category: ModuleCategories.PRODUCTION_QUALITY,
    description: '实时监控生产过程和设备状态',
    features: [
      '实时数据监控',
      '设备状态监控',
      '异常报警',
      '生产参数监控'
    ],
    keywords: ['过程监控', '实时监控', '设备状态', '异常报警', '生产参数'],
    routePath: '/home/mes/monitoring',
    parentModule: 'MES'
  },
  { // MES子模块 - 在制品管理模块
    name: 'MES-WIP',
    fullName: 'MES在制品管理',
    category: ModuleCategories.PRODUCTION_QUALITY,
    description: '管理生产过程中的在制品跟踪和库存',
    features: [
      '在制品跟踪',
      'WIP库存管理',
      '物料流转记录',
      '在制品质量追溯'
    ],
    keywords: ['在制品', '在制品管理', 'WIP', '物料流转', '质量追溯'],
    routePath: '/home/mes/wip',
    parentModule: 'MES'
  },
  { // MES子模块 - 数据采集模块
    name: 'MES-DataCollection',
    fullName: 'MES数据采集',
    category: ModuleCategories.PRODUCTION_QUALITY,
    description: '采集生产过程中的各种数据和参数',
    features: [
      '生产数据采集',
      '设备数据采集',
      '质量数据采集',
      '数据存储和管理'
    ],
    keywords: ['数据采集', '生产数据', '设备数据', '质量数据', '数据管理'],
    routePath: '/home/mes/data-collection',
    parentModule: 'MES'
  },
  {
    name: 'QMS',
    fullName: '质量管理系统',
    category: ModuleCategories.PRODUCTION_QUALITY,
    description: '管理质量检验、不合格品处理、质量分析等',
    features: [
      '质量检验',
      '不合格品管理',
      '质量异常管理',
      '质量数据分析'
    ],
    keywords: ['QMS', '质量', '质量管理', '检验', '质量检验', '检验标准', '检验计划', '检验任务', '检验结果', '不合格品', '不合格品管理', '质量异常', '异常报告', '异常分析', '异常处理', '质量数据分析', '质量数据采集', '统计分析', '质量报告', '趋势预测'],
    routePath: '/home/qms'
  },
  { // QMS子模块 - 质量检验计划
    name: 'QMS-QualityInspection',
    fullName: 'QMS质量检验计划',
    category: ModuleCategories.PRODUCTION_QUALITY,
    description: '管理质量检验标准、计划、任务和结果记录',
    features: [
      '检验标准管理',
      '检验计划管理',
      '检验任务管理',
      '检验结果记录'
    ],
    keywords: ['质量检验', '检验标准', '检验计划', '检验任务', '检验结果', '检验记录'],
    routePath: '/home/qms/quality-inspection',
    parentModule: 'QMS'
  },
  { // QMS子模块 - 不合格品管理
    name: 'QMS-NonConforming',
    fullName: 'QMS不合格品管理',
    category: ModuleCategories.PRODUCTION_QUALITY,
    description: '管理不合格品的登记、评审、处置和跟踪',
    features: [
      '不合格品登记',
      '不合格品评审',
      '不合格品处置',
      '不合格品跟踪'
    ],
    keywords: ['不合格品', '不合格品管理', '不合格品登记', '不合格品评审', '不合格品处置', '不合格品跟踪'],
    routePath: '/home/qms/non-conforming',
    parentModule: 'QMS'
  },
  { // QMS子模块 - 质量异常管理
    name: 'QMS-QualityAnomaly',
    fullName: 'QMS质量异常管理',
    category: ModuleCategories.PRODUCTION_QUALITY,
    description: '管理质量异常的报告、分析、处置和纠正预防措施',
    features: [
      '质量异常报告',
      '质量异常分析',
      '质量异常处置',
      '纠正预防措施'
    ],
    keywords: ['质量异常', '异常报告', '异常分析', '异常处理', '纠正预防', 'CAPA'],
    routePath: '/home/qms/quality-anomaly',
    parentModule: 'QMS'
  },
  { // QMS子模块 - 质量数据分析
    name: 'QMS-QualityAnalysis',
    fullName: 'QMS质量数据分析',
    category: ModuleCategories.PRODUCTION_QUALITY,
    description: '管理质量数据的采集、统计分析、报告生成和趋势预测',
    features: [
      '质量数据采集',
      '质量统计分析',
      '质量报告生成',
      '质量趋势预测'
    ],
    keywords: ['质量数据分析', '质量数据采集', '统计分析', '质量报告', '趋势预测', '数据统计'],
    routePath: '/home/qms/quality-analysis',
    parentModule: 'QMS'
  },
  
  // 仓储与物流类模块
  {
    name: 'WMS',
    fullName: '仓储管理系统',
    category: ModuleCategories.WAREHOUSE_LOGISTICS,
    description: '管理库存、出入库、储位、盘点等',
    features: [
      '基础设置',
      '入库管理',
      '出库管理',
      '库存管理',
      '移动作业'
    ],
    keywords: ['WMS', '库存', '仓库', '入库', '出库', 'WMS基础设置', '移动作业', 'PDA作业'],
    routePath: '/home/wms'
  },
  { // WMS子模块 - 基础设置模块
    name: 'WMS-Base',
    fullName: 'WMS基础设置',
    category: ModuleCategories.WAREHOUSE_LOGISTICS,
    description: '配置WMS系统的基础数据和参数',
    features: [
      '仓库信息管理',
      '储位管理',
      '物料主数据管理',
      '规则配置'
    ],
    keywords: ['WMS基础设置', '基础设置', '仓库配置', '储位管理', '物料主数据'],
    routePath: '/home/wms/base',
    parentModule: 'WMS'
  },
  { // WMS子模块 - 入库管理模块
    name: 'WMS-Inbound',
    fullName: 'WMS入库管理',
    category: ModuleCategories.WAREHOUSE_LOGISTICS,
    description: '管理物料入库流程和操作',
    features: [
      '入库订单管理',
      '到货接收',
      '质检管理',
      '入库上架'
    ],
    keywords: ['入库管理', '到货接收', '质检管理', '入库上架', '入库订单'],
    routePath: '/home/wms/inbound',
    parentModule: 'WMS'
  },
  { // WMS子模块 - 出库管理模块
    name: 'WMS-Outbound',
    fullName: 'WMS出库管理',
    category: ModuleCategories.WAREHOUSE_LOGISTICS,
    description: '管理物料出库流程和操作',
    features: [
      '出库订单管理',
      '拣货管理',
      '打包管理',
      '出库发货'
    ],
    keywords: ['出库管理', '拣货管理', '打包管理', '出库发货', '出库订单'],
    routePath: '/home/wms/outbound',
    parentModule: 'WMS'
  },
  { // WMS子模块 - 库存中心模块
    name: 'WMS-Stock',
    fullName: 'WMS库存中心',
    category: ModuleCategories.WAREHOUSE_LOGISTICS,
    description: '管理库存查询、盘点和库存调整',
    features: [
      '库存查询',
      '库存盘点',
      '库存调整',
      '库存分析'
    ],
    keywords: ['库存中心', '库存查询', '库存盘点', '库存调整', '库存分析'],
    routePath: '/home/wms/stock',
    parentModule: 'WMS'
  },
  { // WMS子模块 - PDA作业模块
    name: 'WMS-Mobile',
    fullName: 'WMS移动作业',
    category: ModuleCategories.WAREHOUSE_LOGISTICS,
    description: '通过PDA设备进行移动作业操作',
    features: [
      'PDA入库作业',
      'PDA出库作业',
      'PDA盘点作业',
      'PDA移库作业'
    ],
    keywords: ['移动作业', 'PDA作业', 'PDA入库', 'PDA出库', 'PDA盘点'],
    routePath: '/home/wms/mobile',
    parentModule: 'WMS'
  },
  {
    name: 'AGV',
    fullName: '自动导引车系统',
    category: ModuleCategories.WAREHOUSE_LOGISTICS,
    description: '管理AGV设备、任务调度、路径规划等',
    features: [
      '路径规划',
      '任务管理',
      '状态监控',
      '调度管理'
    ],
    keywords: ['AGV', '机器人', '路径规划', '任务管理', '状态监控', '调度管理'],
    routePath: '/home/agv'
  },
  { // AGV子模块 - 路径规划模块
    name: 'AGV-PathPlanning',
    fullName: 'AGV路径规划',
    category: ModuleCategories.WAREHOUSE_LOGISTICS,
    description: '规划AGV行驶路径和路线优化',
    features: [
      '地图管理',
      '路径规划',
      '路线优化',
      '避障策略'
    ],
    keywords: ['路径规划', '地图管理', '路线优化', '避障策略', 'AGV路径'],
    routePath: '/home/agv/path-planning',
    parentModule: 'AGV'
  },
  { // AGV子模块 - 任务管理模块
    name: 'AGV-TaskManagement',
    fullName: 'AGV任务管理',
    category: ModuleCategories.WAREHOUSE_LOGISTICS,
    description: '管理AGV任务的创建、分配和执行',
    features: [
      '任务创建',
      '任务分配',
      '任务执行跟踪',
      '任务历史查询'
    ],
    keywords: ['任务管理', '任务创建', '任务分配', '任务跟踪', 'AGV任务'],
    routePath: '/home/agv/task-management',
    parentModule: 'AGV'
  },
  { // AGV子模块 - 状态监控模块
    name: 'AGV-StatusMonitoring',
    fullName: 'AGV状态监控',
    category: ModuleCategories.WAREHOUSE_LOGISTICS,
    description: '实时监控AGV设备的运行状态',
    features: [
      '设备状态监控',
      '电池状态监控',
      '运行数据统计',
      '异常报警'
    ],
    keywords: ['状态监控', '设备状态', '电池监控', '运行数据', '异常报警', 'AGV监控'],
    routePath: '/home/agv/status-monitoring',
    parentModule: 'AGV'
  },
  { // AGV子模块 - 调度管理模块
    name: 'AGV-DispatchManagement',
    fullName: 'AGV调度管理',
    category: ModuleCategories.WAREHOUSE_LOGISTICS,
    description: '优化AGV资源调度和任务分配',
    features: [
      '资源调度',
      '冲突避免',
      '优先级管理',
      '负载均衡'
    ],
    keywords: ['调度管理', '资源调度', '冲突避免', '优先级管理', '负载均衡', 'AGV调度'],
    routePath: '/home/agv/dispatch-management',
    parentModule: 'AGV'
  },
  {
    name: 'LES',
    fullName: '物流执行系统',
    category: ModuleCategories.WAREHOUSE_LOGISTICS,
    description: '管理运输、配送、在途监控等',
    features: [
      '运输管理',
      '在途监控',
      '签收管理',
      '物流分析'
    ],
    keywords: ['LES', '物流', '条码', '运输管理', '在途监控', '签收管理', '物流分析'],
    routePath: '/home/les'
  },
  { // LES子模块 - 运输管理模块
    name: 'LES-TransportManagement',
    fullName: 'LES运输管理',
    category: ModuleCategories.WAREHOUSE_LOGISTICS,
    description: '管理运输订单、车辆调度和运输路线规划',
    features: [
      '运输订单管理',
      '车辆调度管理',
      '运输路线规划',
      '运输成本核算'
    ],
    keywords: ['运输管理', '运输订单', '车辆调度', '路线规划', '运输成本'],
    routePath: '/home/les/transport-management',
    parentModule: 'LES'
  },
  { // LES子模块 - 在途监控模块
    name: 'LES-InTransitMonitoring',
    fullName: 'LES在途监控',
    category: ModuleCategories.WAREHOUSE_LOGISTICS,
    description: '实时监控货物运输状态和位置信息',
    features: [
      '车辆实时定位',
      '运输状态跟踪',
      '异常事件预警',
      '运输轨迹回放'
    ],
    keywords: ['在途监控', '实时定位', '状态跟踪', '异常预警', '轨迹回放'],
    routePath: '/home/les/in-transit-monitoring',
    parentModule: 'LES'
  },
  { // LES子模块 - 签收管理模块
    name: 'LES-SignManagement',
    fullName: 'LES签收管理',
    category: ModuleCategories.WAREHOUSE_LOGISTICS,
    description: '管理货物签收流程和签收信息',
    features: [
      '签收单管理',
      '电子签名采集',
      '签收状态查询',
      '签收异常处理'
    ],
    keywords: ['签收管理', '签收单', '电子签名', '签收状态', '签收异常'],
    routePath: '/home/les/sign-management',
    parentModule: 'LES'
  },
  { // LES子模块 - 物流分析模块
    name: 'LES-LogisticsAnalysis',
    fullName: 'LES物流分析',
    category: ModuleCategories.WAREHOUSE_LOGISTICS,
    description: '分析物流数据和生成物流报表',
    features: [
      '运输数据分析',
      '成本分析',
      '服务质量评估',
      '报表生成'
    ],
    keywords: ['物流分析', '数据分析', '成本分析', '服务质量', '报表生成'],
    routePath: '/home/les/logistics-analysis',
    parentModule: 'LES'
  },
  
  // 设备与能源类模块
  {
    name: 'SCADA',
    fullName: '监控与数据采集',
    category: ModuleCategories.EQUIPMENT_ENERGY,
    description: '实时监控设备状态、采集生产数据等',
    features: [
      '数据采集',
      '实时监控',
      '报警管理',
      '历史数据查询'
    ],
    keywords: ['SCADA', '中控', '大屏', '实时监控', '报警管理', '历史数据'],
    routePath: '/home/scada'
  },
  { // SCADA子模块 - 数据采集模块
    name: 'SCADA-DataCollection',
    fullName: 'SCADA数据采集',
    category: ModuleCategories.EQUIPMENT_ENERGY,
    description: '采集和处理设备运行数据和生产过程数据',
    features: [
      '实时数据采集',
      '数据预处理',
      '数据存储',
      '采集点配置'
    ],
    keywords: ['数据采集', '实时采集', '数据处理', '采集点配置'],
    routePath: '/home/scada/data-collection',
    parentModule: 'SCADA'
  },
  { // SCADA子模块 - 实时监控模块
    name: 'SCADA-RealtimeMonitoring',
    fullName: 'SCADA实时监控',
    category: ModuleCategories.EQUIPMENT_ENERGY,
    description: '实时监控设备状态和生产过程参数',
    features: [
      '设备状态监控',
      '生产参数监控',
      '监控画面展示',
      '数据可视化'
    ],
    keywords: ['实时监控', '设备监控', '参数监控', '监控画面', '数据可视化'],
    routePath: '/home/scada/realtime-monitoring',
    parentModule: 'SCADA'
  },
  { // SCADA子模块 - 报警管理模块
    name: 'SCADA-AlarmManagement',
    fullName: 'SCADA报警管理',
    category: ModuleCategories.EQUIPMENT_ENERGY,
    description: '管理设备异常报警和事件通知',
    features: [
      '报警配置',
      '实时报警处理',
      '报警历史查询',
      '报警统计分析'
    ],
    keywords: ['报警管理', '实时报警', '报警配置', '报警历史', '报警统计'],
    routePath: '/home/scada/alarm-management',
    parentModule: 'SCADA'
  },
  { // SCADA子模块 - 历史数据查询模块
    name: 'SCADA-HistoricalData',
    fullName: 'SCADA历史数据查询',
    category: ModuleCategories.EQUIPMENT_ENERGY,
    description: '查询和分析历史生产数据',
    features: [
      '历史数据查询',
      '数据趋势分析',
      '数据报表生成',
      '数据导出'
    ],
    keywords: ['历史数据', '数据查询', '趋势分析', '报表生成', '数据导出'],
    routePath: '/home/scada/historical-data',
    parentModule: 'SCADA'
  },
  {
    name: 'EMS',
    fullName: '能源管理系统',
    category: ModuleCategories.EQUIPMENT_ENERGY,
    description: '管理能耗监控、能源分析、节能优化等',
    features: [
      '能源采集',
      '能耗分析',
      '能源优化',
      '报表管理'
    ],
    keywords: ['EMS', '能源', '能源采集', '能耗分析', '能源优化', '能源报表'],
    routePath: '/home/ems'
  },
  { // EMS子模块 - 能源采集模块
    name: 'EMS-EnergyCollection',
    fullName: 'EMS能源采集',
    category: ModuleCategories.EQUIPMENT_ENERGY,
    description: '采集和处理各类能源消耗数据',
    features: [
      '实时能源采集',
      '能源数据预处理',
      '采集点管理',
      '能源数据存储'
    ],
    keywords: ['能源采集', '实时采集', '采集点管理', '能源数据'],
    routePath: '/home/ems/energy-collection',
    parentModule: 'EMS'
  },
  { // EMS子模块 - 能耗分析模块
    name: 'EMS-EnergyAnalysis',
    fullName: 'EMS能耗分析',
    category: ModuleCategories.EQUIPMENT_ENERGY,
    description: '分析能源消耗情况，识别节能潜力',
    features: [
      '能耗数据分析',
      '能耗趋势分析',
      '能耗对比分析',
      '节能潜力识别'
    ],
    keywords: ['能耗分析', '能耗数据', '趋势分析', '节能潜力'],
    routePath: '/home/ems/energy-analysis',
    parentModule: 'EMS'
  },
  { // EMS子模块 - 能源优化模块
    name: 'EMS-EnergyOptimization',
    fullName: 'EMS能源优化',
    category: ModuleCategories.EQUIPMENT_ENERGY,
    description: '制定和实施能源优化策略，降低能源消耗',
    features: [
      '能源优化策略制定',
      '优化方案实施',
      '优化效果评估',
      '节能措施管理'
    ],
    keywords: ['能源优化', '节能策略', '优化方案', '节能措施'],
    routePath: '/home/ems/energy-optimization',
    parentModule: 'EMS'
  },
  { // EMS子模块 - 报表管理模块
    name: 'EMS-ReportManagement',
    fullName: 'EMS报表管理',
    category: ModuleCategories.EQUIPMENT_ENERGY,
    description: '生成和管理各类能源报表',
    features: [
      '能源报表生成',
      '报表模板管理',
      '报表查询分析',
      '报表导出分享'
    ],
    keywords: ['能源报表', '报表管理', '报表生成', '报表分析'],
    routePath: '/home/ems/report-management',
    parentModule: 'EMS'
  },
  {
    name: 'EAM',
    fullName: '设备全生命周期管理',
    category: ModuleCategories.EQUIPMENT_ENERGY,
    description: '管理设备台账、维护、备件、绩效等',
    features: [
      '设备台账管理',
      '维护管理',
      '备件管理',
      '设备绩效分析'
    ],
    keywords: ['EAM', '设备管理', '设备台账', '维护管理', '备件管理', '设备绩效'],
    routePath: '/home/eam'
  },
  { // EAM子模块 - 设备台账模块
    name: 'EAM-AssetLedger',
    fullName: 'EAM设备台账',
    category: ModuleCategories.EQUIPMENT_ENERGY,
    description: '管理设备基础信息和台账数据',
    features: [
      '设备信息管理',
      '设备分类管理',
      '设备台账查询',
      '设备档案管理'
    ],
    keywords: ['设备台账', '设备信息', '设备分类', '设备档案'],
    routePath: '/home/eam/asset-ledger',
    parentModule: 'EAM'
  },
  { // EAM子模块 - 维护管理模块
    name: 'EAM-MaintenanceManagement',
    fullName: 'EAM维护管理',
    category: ModuleCategories.EQUIPMENT_ENERGY,
    description: '管理设备维护计划和执行过程',
    features: [
      '维护计划管理',
      '工单管理',
      '维护记录管理',
      '预防性维护'
    ],
    keywords: ['维护管理', '维护计划', '工单管理', '预防性维护'],
    routePath: '/home/eam/maintenance-management',
    parentModule: 'EAM'
  },
  { // EAM子模块 - 备件管理模块
    name: 'EAM-SparePartsManagement',
    fullName: 'EAM备件管理',
    category: ModuleCategories.EQUIPMENT_ENERGY,
    description: '管理设备备件的采购、库存和使用',
    features: [
      '备件需求计划',
      '备件库存管理',
      '备件采购管理',
      '备件消耗统计'
    ],
    keywords: ['备件管理', '备件库存', '备件采购', '备件消耗'],
    routePath: '/home/eam/spare-parts-management',
    parentModule: 'EAM'
  },
  { // EAM子模块 - 设备绩效分析模块
    name: 'EAM-AssetPerformance',
    fullName: 'EAM设备绩效分析',
    category: ModuleCategories.EQUIPMENT_ENERGY,
    description: '分析设备运行绩效和可靠性',
    features: [
      '设备绩效指标监控',
      '设备利用率分析',
      '设备故障分析',
      '绩效报告生成'
    ],
    keywords: ['设备绩效', '绩效分析', '设备利用率', '故障分析'],
    routePath: '/home/eam/asset-performance',
    parentModule: 'EAM'
  },
  
  // 数据与决策类模块
  {
    name: 'BI',
    fullName: '商业智能',
    category: ModuleCategories.DATA_DECISION,
    description: '数据集成、数据分析、可视化展示',
    features: [
      '数据集成',
      '数据分析',
      '可视化报表',
      '决策支持'
    ],
    keywords: ['BI', '数据分析', '数据集成', '可视化', '可视化报表', '决策支持'],
    routePath: '/home/bi'
  },
  {
    name: 'AI',
    fullName: '人工智能',
    category: ModuleCategories.DATA_DECISION,
    description: '模型训练、智能分析、优化建议',
    features: [
      '模型管理',
      '智能分析',
      '流程自动化',
      '知识图谱'
    ],
    keywords: ['AI', '人工智能', '模型管理', '智能分析', '流程自动化', '知识图谱'],
    routePath: '/home/ai'
  }
];

// 根据关键词查找模块信息
export function findModuleByKeyword(keyword: string): ModuleInfo | undefined {
  const lowerKeyword = keyword.toLowerCase();
  
  // 计算匹配得分的接口
  interface MatchScore {
    module: ModuleInfo;
    score: number;
  }
  
  // 计算每个模块的匹配得分
  const scoredModules = knowledgeBase.map(module => {
    let maxScore = 0;
    
    module.keywords.forEach(k => {
      const lowerKey = k.toLowerCase();
      let score = 0;
      
      // 1. 精确匹配 - 最高优先级 (100分)
      if (lowerKey === lowerKeyword) {
        score = 100;
      }
      // 2. 关键词完全包含查询词 - 高优先级 (70分)
      else if (lowerKey.includes(lowerKeyword)) {
        score = 70 + lowerKeyword.length * 0.5; // 更长的查询词得分更高
      }
      // 3. 查询词完全包含关键词 - 中优先级 (50分)
      else if (lowerKeyword.includes(lowerKey)) {
        score = 50 + lowerKey.length * 0.5; // 更长的关键词得分更高
      }
      // 4. 部分匹配 - 低优先级 (20分)
      else if (lowerKey.includes(lowerKeyword.substring(0, Math.floor(lowerKeyword.length / 2)))) {
        score = 20;
      }
      
      // 更新最大得分
      if (score > maxScore) {
        maxScore = score;
      }
    });
    
    return { module, score: maxScore };
  });
  
  // 过滤掉得分为0的模块
  const matchedModules = scoredModules.filter(m => m.score > 0);
  
  // 按得分降序排序
  matchedModules.sort((a, b) => b.score - a.score);
  
  // 返回得分最高的模块，如果没有匹配的模块则返回undefined
  if (matchedModules.length > 0) {
    const topMatch = matchedModules[0];
    return topMatch ? topMatch.module : undefined;
  }
  return undefined;
}

// 根据路由路径查找模块信息
export function findModuleByPath(path: string): ModuleInfo | undefined {
  return knowledgeBase.find(module => module.routePath === path);
}

// 获取所有模块的关键词映射
export function getAllKeywordsMap(): Record<string, string> {
  const map: Record<string, string> = {};
  knowledgeBase.forEach(module => {
    module.keywords.forEach(keyword => {
      map[keyword] = module.routePath;
    });
  });
  return map;
}

// 获取模块的详细描述
export function getModuleDescription(moduleName: string): string {
  const module = knowledgeBase.find(m => m.name === moduleName || m.fullName === moduleName);
  if (module) {
    return `${module.fullName}：${module.description}\n主要功能：\n${module.features.map(f => `- ${f}`).join('\n')}`;
  }
  return `${moduleName}模块信息未找到`;
}
