import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '../stores/auth'

// 定义路由规则
const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'login',
    component: () => import('../views/LoginView.vue'),
    meta: {
      requiresAuth: false
    }
  },
  {
    path: '/',
    redirect: '/home'
  },
  // 为所有顶级模块路径添加重定向，解决直接访问 /erp 等路径时的路由匹配问题
  {
    path: '/erp',
    redirect: '/home/erp'
  },
  // ERP子模块路径重定向
  { path: '/erp/finance',
    redirect: '/home/erp/finance'
  },
  { path: '/erp/supply-chain',
    redirect: '/home/erp/supply-chain'
  },
  { path: '/erp/production',
    redirect: '/home/erp/production'
  },
  { path: '/erp/basic-data',
    redirect: '/home/erp/basic-data'
  },
  { path: '/hr',
    redirect: '/home/hr' },
  // HR子模块路径重定向
  { path: '/hr/org-employee',
    redirect: '/home/hr/org-employee' },
  { path: '/hr/recruitment',
    redirect: '/home/hr/recruitment' },
  { path: '/hr/attendance-leave',
    redirect: '/home/hr/attendance-leave' },
  { path: '/hr/compensation',
    redirect: '/home/hr/compensation' },
  { path: '/aps',
    redirect: '/home/aps' },
  // APS子模块路径重定向
  { path: '/aps/basic-data',
    redirect: '/home/aps/basic-data' },
  { path: '/aps/plan-generation',
    redirect: '/home/aps/plan-generation' },
  { path: '/aps/dynamic-optimization',
    redirect: '/home/aps/dynamic-optimization' },
  { path: '/aps/plan-monitoring',
    redirect: '/home/aps/plan-monitoring' },
  { path: '/aps/schedule-visualization',
    redirect: '/home/aps/schedule-visualization' },
  { path: '/oa',
    redirect: '/home/oa' },
  // OA子模块路径重定向
  { path: '/oa/approval',
    redirect: '/home/oa/approval' },
  { path: '/oa/document',
    redirect: '/home/oa/document' },
  { path: '/oa/collaboration',
    redirect: '/home/oa/collaboration' },
  { path: '/oa/mobile',
    redirect: '/home/oa/mobile' },
  { path: '/oa/admin',
    redirect: '/home/oa/admin' },
  { path: '/scm',
    redirect: '/home/scm' },
  { path: '/plm',
    redirect: '/home/plm' },
  { path: '/bi',
    redirect: '/home/bi' },
  { path: '/ai',
    redirect: '/home/ai' },
  {
    path: '/home',
    component: () => import('../layouts/MainLayout.vue'),
    meta: {
      requiresAuth: true
    },
    children: [
      {
        path: '',
        name: 'home',
        component: () => import('../views/HomeView.vue')
      },
      // 主页面路由
      // 主页面路由已移除

      // ERP模块
      {
        path: 'erp',
        name: 'erp',
        component: () => import('../views/erp/ErpView.vue'),
        children: [
          // 财务模块
          { path: 'finance', name: 'erp-finance', component: () => import('../views/erp/finance/FinanceView.vue') as any },
          
          // 供应链模块
          { path: 'supply-chain', name: 'erp-supply-chain', component: () => import('../views/erp/supply-chain/SupplyChainView.vue') as any },
          
          // 生产模块
          { path: 'production', name: 'erp-production', component: () => import('../views/erp/production/ProductionView.vue') as any },
          
          // 基础数据模块
          { path: 'basic-data', name: 'erp-basic-data', component: () => import('../views/erp/basic-data/BasicDataView.vue') as any },
          
          // 通配符路由（必须放在最后）
          { path: ':feature', component: () => import('../views/UnderConstruction.vue') }
        ]
      },
      // HR模块
      {
        path: 'hr',
        name: 'hr',
        component: () => import('../views/hr/HrView.vue'),
        children: [
          // 组织与员工管理模块
          { path: 'org-employee', name: 'hr-org-employee', component: () => import('../views/hr/org-employee/OrgEmployeeView.vue') as any },
          
          // 招聘管理模块
          { path: 'recruitment', name: 'hr-recruitment', component: () => import('../views/hr/recruitment/RecruitmentView.vue') as any },
          
          // 考勤与假期模块
          { path: 'attendance-leave', name: 'hr-attendance-leave', component: () => import('../views/hr/attendance-leave/AttendanceLeaveView.vue') as any },
          
          // 薪酬福利模块
          { path: 'compensation', name: 'hr-compensation', component: () => import('../views/hr/compensation/CompensationView.vue') as any },

          // HR集成事件查询模块（F5 前端配套）
          { path: 'integration', name: 'hr-integration', component: () => import('../views/hr/integration/IntegrationEventView.vue') as any, meta: { title: 'HR集成事件', backTo: '/home/hr' } },
          
          // 通配符路由（必须放在最后）
          { path: ':feature', component: () => import('../views/UnderConstruction.vue') }
        ]
      },
      // APS模块
      {
        path: 'aps',
        name: 'aps',
        component: () => import('../views/aps/ApsView.vue'),
        children: [
          // 基础数据配置模块
          { path: 'basic-data', name: 'aps-basic-data', component: () => import('../views/aps/basic-data/BasicDataView.vue') as any },
          
          // 计划生成模块
          { path: 'plan-generation', name: 'aps-plan-generation', component: () => import('../views/aps/plan-generation/PlanGenerationView.vue') as any },
          
          // 动态优化模块
          { path: 'dynamic-optimization', name: 'aps-dynamic-optimization', component: () => import('../views/aps/dynamic-optimization/DynamicOptimizationView.vue') as any },
          
          // 计划执行监控模块
          { path: 'plan-monitoring', name: 'aps-plan-monitoring', component: () => import('../views/aps/plan-monitoring/PlanMonitoringView.vue') as any },
          
          // 排程可视化（甘特图）页面：支持通过 query.planId 定位计划
          { path: 'schedule-visualization', name: 'aps-schedule-visualization', component: () => import('../views/aps/ScheduleVisualization.vue') as any },
          
          // 通配符路由（必须放在最后）
          { path: ':feature', component: () => import('../views/UnderConstruction.vue') }
        ]
      },
      // OA模块
      {
        path: 'oa',
        name: 'oa',
        component: () => import('../views/oa/OaView.vue'),
        children: [
          // 流程审批
          { path: 'approval', name: 'oa-approval', component: () => import('../views/oa/approval/ApprovalView.vue') },
          { path: 'approval/detail/:id', name: 'oa-approval-detail', component: () => import('../views/oa/approval/ApprovalDetailView.vue') },
          // 文档管理
          { path: 'document', name: 'oa-document', component: () => import('../views/oa/document/DocumentView.vue') },
          // 协同办公
          { path: 'collaboration', name: 'oa-collaboration', component: () => import('../views/oa/collaboration/CollaborationView.vue') },
          // 移动办公
          { path: 'mobile', name: 'oa-mobile', component: () => import('../views/oa/mobile/MobileOfficeView.vue') },
          // 行政后勤
          { path: 'admin', name: 'oa-admin', component: () => import('../views/oa/admin/AdminView.vue') },
          // 通配符路由（必须放在最后）
          { path: ':feature', component: () => import('../views/UnderConstruction.vue') }
        ]
      },
      // CRM模块
      {
        path: 'crm',
        name: 'crm',
        component: () => import('../views/crm/CrmView.vue'),
        children: [
          // 客户管理模块
          { path: 'customer', name: 'crm-customer', component: () => import('../views/crm/customer/CustomerView.vue') },
          // 销售管理模块
          { path: 'sales', name: 'crm-sales', component: () => import('../views/crm/sales/SalesView.vue') },
          // 订单管理模块
          { path: 'order', name: 'crm-order', component: () => import('../views/crm/order/OrderView.vue') },
          // 客户服务模块
          { path: 'service', name: 'crm-service', component: () => import('../views/crm/service/ServiceView.vue') },
          // 通配符路由（必须放在最后）
          { path: ':feature', component: () => import('../views/UnderConstruction.vue') }
        ]
      },
      // SRM模块
      {
        path: 'srm',
        name: 'srm',
        component: () => import('../views/srm/SrmView.vue'),
        children: [
          // 订单管理
          { path: 'order', redirect: '/home/srm/order/list' },
          { path: 'order/list', name: 'srm-order-list', component: () => import('../views/srm/order/list.vue') },
          { path: 'order/detail/:id?', name: 'srm-order-detail', component: () => import('../views/srm/order/detail.vue') },

          // 供应商准入管理
          { path: 'supplier-access', name: 'srm-supplier-access', component: () => import('../views/srm/supplier/SupplierAccessView.vue') },
          { path: 'supplier', redirect: '/home/srm/supplier/list' },
          { path: 'supplier/list', name: 'srm-supplier-list', component: () => import('../views/srm/supplier/SupplierListView.vue') },
          { path: 'supplier/qualification', name: 'srm-supplier-qualification', component: () => import('../views/srm/supplier/SupplierQualificationView.vue') },
          { path: 'supplier/credit', name: 'srm-supplier-credit', component: () => import('../views/srm/supplier/SupplierCreditView.vue') },
          { path: 'supplier/blacklist', name: 'srm-supplier-blacklist', component: () => import('../views/srm/supplier/SupplierBlacklistView.vue') },

          // 采购流程自动化
          { path: 'purchase-automation', name: 'srm-purchase-automation', component: () => import('../views/srm/purchase/PurchaseAutomationView.vue') },
          { path: 'purchase', redirect: '/home/srm/purchase/request' },
          { path: 'purchase/request', name: 'srm-purchase-request', component: () => import('../views/srm/purchase/purchase/PurchaseRequestView.vue') },
          { path: 'purchase/inquiry', name: 'srm-purchase-inquiry', component: () => import('../views/srm/purchase/purchase/InquiryView.vue') },
          { path: 'purchase/quotation', name: 'srm-purchase-quotation', component: () => import('../views/srm/purchase/purchase/QuotationView.vue') },
          { path: 'purchase/comparison', name: 'srm-purchase-comparison', component: () => import('../views/srm/purchase/purchase/PriceComparisonView.vue') },
          
          // 供应商协同管理
          { path: 'collaboration', name: 'srm-collaboration', component: () => import('../views/srm/collaboration/CollaborationView.vue') },

          // 绩效与风险管理
          { path: 'performance-risk', name: 'srm-performance-risk', component: () => import('../views/srm/performance/PerformanceRiskView.vue') },
          
          // 通配符路由（必须放在最后）
          { path: ':feature', component: () => import('../views/UnderConstruction.vue') }
        ]
      },
      // MES模块
      {
        path: 'mes',
        name: 'mes',
        component: () => import('../views/mes/MesView.vue'),
        children: [
          // 生产执行模块
          { path: 'execution', name: 'mes-execution', component: () => import('../views/mes/execution/ProductionExecutionView.vue') as any },
          // 生产报工模块
          { path: 'reporting', name: 'mes-reporting', component: () => import('../views/mes/reporting/ProductionReportingView.vue') as any },
          // 过程监控模块
          { path: 'monitoring', name: 'mes-monitoring', component: () => import('../views/mes/monitoring/ProcessMonitoringView.vue') as any },
          // 在制品管理模块
          { path: 'wip', name: 'mes-wip', component: () => import('../views/mes/wip/WipManagementView.vue') as any },
          // 数据采集模块
          { path: 'data-collection', name: 'mes-data-collection', component: () => import('../views/mes/data-collection/DataCollectionView.vue') as any },
          // 通配符路由（必须放在最后）
          { path: ':feature', component: () => import('../views/UnderConstruction.vue') }
        ]
      },
      // WMS模块
      {
        path: 'wms',
        name: 'wms',
        component: () => import('../views/wms/WmsView.vue'),
        children: [
          // 基础设置模块
          { path: 'base', name: 'wms-base', component: () => import('../views/wms/base/BaseSettingView.vue') as any },
          
          // 入库管理模块
          { path: 'inbound', name: 'wms-inbound', component: () => import('../views/wms/inbound/InboundManagementView.vue') as any },
          
          // 出库管理模块
          { path: 'outbound', name: 'wms-outbound', component: () => import('../views/wms/outbound/OutboundManagementView.vue') as any },
          
          // 库存管理模块
          { path: 'inventory', name: 'wms-inventory', component: () => import('../views/wms/inventory/InventoryListView.vue') as any },
          
          // 库存中心模块
          { path: 'stock', name: 'wms-stock', component: () => import('../views/wms/stock/StockCenterView.vue') as any },
          
          // PDA作业模块
          { path: 'mobile', name: 'wms-mobile', component: () => import('../views/wms/mobile/MobileOperationView.vue') as any },
          
          // 通配符路由（必须放在最后）
          { path: ':feature', component: () => import('../views/UnderConstruction.vue') }
        ]
      },
      // SCRM模块
      {
        path: 'scrm',
        name: 'scrm',
        component: () => import('../views/scrm/ScrmView.vue'),
        children: [
          { path: 'acquisition-active', name: 'scrm-acquisition-active', component: () => import('../views/scrm/acquisition-active/ActiveAcquisitionView.vue') },
          { path: 'acquisition-passive', name: 'scrm-acquisition-passive', component: () => import('../views/scrm/acquisition-passive/PassiveAcquisitionView.vue') },
          { path: 'private-traffic', name: 'scrm-private-traffic', component: () => import('../views/scrm/private-traffic/PrivateTrafficView.vue') },
          { path: 'customer-fission', name: 'scrm-customer-fission', component: () => import('../views/scrm/customer-fission/CustomerFissionView.vue') },
          { path: 'precision-marketing', name: 'scrm-precision-marketing', component: () => import('../views/scrm/precision-marketing/PrecisionMarketingView.vue') },
          { path: 'data-sync', name: 'scrm-data-sync', component: () => import('../views/scrm/data-sync/DataSyncView.vue') },
          { path: ':feature', component: () => import('../views/UnderConstruction.vue') }
        ]
      },
      // 商机管理模块
      {
        path: 'business-opportunity',
        name: 'business-opportunity',
        component: () => import('../views/business-opportunity/BusinessOpportunityView.vue'),
        children: [
          { path: 'opportunity-entry', name: 'bo-opportunity-entry', component: () => import('../views/business-opportunity/opportunity-entry/OpportunityEntryView.vue') },
          { path: 'opportunity-tracking', name: 'bo-opportunity-tracking', component: () => import('../views/business-opportunity/opportunity-tracking/OpportunityTrackingView.vue') },
          { path: 'opportunity-analysis', name: 'bo-opportunity-analysis', component: () => import('../views/business-opportunity/opportunity-analysis/OpportunityAnalysisView.vue') },
          { path: 'opportunity-forecast', name: 'bo-opportunity-forecast', component: () => import('../views/business-opportunity/opportunity-forecast/OpportunityForecastView.vue') },
          { path: ':feature', component: () => import('../views/UnderConstruction.vue') }
        ]
      },
      // AGV模块
      {
        path: 'agv',
        name: 'agv',
        component: () => import('../views/agv/AgvView.vue'),
        children: [
          // 路径规划模块
          { path: 'path-planning', name: 'agv-path-planning', component: () => import('../views/agv/path-planning/PathPlanningView.vue') as any },
          
          // 任务管理模块
          { path: 'task-management', name: 'agv-task-management', component: () => import('../views/agv/task-management/TaskManagementView.vue') as any },
          
          // 状态监控模块
          { path: 'status-monitoring', name: 'agv-status-monitoring', component: () => import('../views/agv/status-monitoring/StatusMonitoringView.vue') as any },
          
          // 调度管理模块
          { path: 'dispatch-management', name: 'agv-dispatch-management', component: () => import('../views/agv/dispatch-management/DispatchManagementView.vue') as any },
          
          // 通配符路由（必须放在最后）
          { path: ':feature', component: () => import('../views/UnderConstruction.vue') }
        ]
      },
      // LES模块
      {
        path: 'les',
        name: 'les',
        component: () => import('../views/les/LesView.vue'),
        children: [
          // 运输管理模块
          { path: 'transport-management', name: 'les-transport-management', component: () => import('../views/les/transport-management/TransportManagementView.vue') as any },
          
          // 在途监控模块
          { path: 'in-transit-monitoring', name: 'les-in-transit-monitoring', component: () => import('../views/les/in-transit-monitoring/InTransitMonitoringView.vue') as any },
          
          // 签收管理模块
          { path: 'sign-management', name: 'les-sign-management', component: () => import('../views/les/sign-management/SignManagementView.vue') as any },
          
          // 物流分析模块
          { path: 'logistics-analysis', name: 'les-logistics-analysis', component: () => import('../views/les/logistics-analysis/LogisticsAnalysisView.vue') as any },
          
          // 通配符路由（必须放在最后）
          { path: ':feature', component: () => import('../views/UnderConstruction.vue') }
        ]
      },
      // SCADA模块
      {
        path: 'scada',
        name: 'scada',
        component: () => import('../views/scada/ScadaView.vue'),
        children: [
          // 数据采集模块
          { path: 'data-collection', name: 'scada-data-collection', component: () => import('../views/scada/data-collection/DataCollectionView.vue') as any },
          { path: 'data-collection/:tab', name: 'scada-data-collection-tab', component: () => import('../views/scada/data-collection/DataCollectionView.vue') as any },
          
          // 实时监控模块
          { path: 'realtime-monitoring', name: 'scada-realtime-monitoring', component: () => import('../views/scada/realtime-monitoring/RealtimeMonitoringView.vue') as any },
          { path: 'realtime-monitoring/:tab', name: 'scada-realtime-monitoring-tab', component: () => import('../views/scada/realtime-monitoring/RealtimeMonitoringView.vue') as any },
          
          // 报警管理模块
          { path: 'alarm-management', name: 'scada-alarm-management', component: () => import('../views/scada/alarm-management/AlarmManagementView.vue') as any },
          { path: 'alarm-management/:tab', name: 'scada-alarm-management-tab', component: () => import('../views/scada/alarm-management/AlarmManagementView.vue') as any },
          
          // 历史数据查询模块
          { path: 'historical-data', name: 'scada-historical-data', component: () => import('../views/scada/historical-data/HistoricalDataView.vue') as any },
          { path: 'historical-data/:tab', name: 'scada-historical-data-tab', component: () => import('../views/scada/historical-data/HistoricalDataView.vue') as any },
          
          // 通配符路由（必须放在最后）
          { path: ':feature', component: () => import('../views/UnderConstruction.vue') }
        ]
      },
      // EMS模块
      {
        path: 'ems',
        name: 'ems',
        component: () => import('../views/ems/EmsView.vue'),
        children: [
          // 能源采集模块
          { path: 'energy-collection', name: 'ems-energy-collection', component: () => import('../views/ems/energy-collection/EnergyCollectionView.vue') as any },
          { path: 'energy-collection/:tab', name: 'ems-energy-collection-tab', component: () => import('../views/ems/energy-collection/EnergyCollectionView.vue') as any },
          
          // 能耗分析模块
          { path: 'energy-analysis', name: 'ems-energy-analysis', component: () => import('../views/ems/energy-analysis/EnergyAnalysisView.vue') as any },
          { path: 'energy-analysis/:tab', name: 'ems-energy-analysis-tab', component: () => import('../views/ems/energy-analysis/EnergyAnalysisView.vue') as any },
          
          // 能源优化模块
          { path: 'energy-optimization', name: 'ems-energy-optimization', component: () => import('../views/ems/energy-optimization/EnergyOptimizationView.vue') as any },
          { path: 'energy-optimization/:tab', name: 'ems-energy-optimization-tab', component: () => import('../views/ems/energy-optimization/EnergyOptimizationView.vue') as any },
          
          // 报表管理模块
          { path: 'report-management', name: 'ems-report-management', component: () => import('../views/ems/report-management/ReportManagementView.vue') as any },
          { path: 'report-management/:tab', name: 'ems-report-management-tab', component: () => import('../views/ems/report-management/ReportManagementView.vue') as any },
          
          // 通配符路由（必须放在最后）
          { path: ':feature', component: () => import('../views/UnderConstruction.vue') }
        ]
      },
      // EAM模块
      {
        path: 'eam',
        name: 'eam',
        component: () => import('../views/eam/EamView.vue'),
        children: [
          // 设备台账模块
          { path: 'asset-ledger', name: 'eam-asset-ledger', component: () => import('../views/eam/asset-ledger/AssetLedgerView.vue') as any },
          { path: 'asset-ledger/:tab', name: 'eam-asset-ledger-tab', component: () => import('../views/eam/asset-ledger/AssetLedgerView.vue') as any },
          
          // 维护管理模块
          { path: 'maintenance-management', name: 'eam-maintenance-management', component: () => import('../views/eam/maintenance-management/MaintenanceManagementView.vue') as any },
          { path: 'maintenance-management/:tab', name: 'eam-maintenance-management-tab', component: () => import('../views/eam/maintenance-management/MaintenanceManagementView.vue') as any },
          
          // 备件管理模块
          { path: 'spare-parts-management', name: 'eam-spare-parts-management', component: () => import('../views/eam/spare-parts-management/SparePartsManagementView.vue') as any },
          { path: 'spare-parts-management/:tab', name: 'eam-spare-parts-management-tab', component: () => import('../views/eam/spare-parts-management/SparePartsManagementView.vue') as any },
          
          // 设备绩效分析模块
          { path: 'asset-performance', name: 'eam-asset-performance', component: () => import('../views/eam/asset-performance-analysis/AssetPerformanceAnalysisView.vue') as any },
          { path: 'asset-performance/:tab', name: 'eam-asset-performance-tab', component: () => import('../views/eam/asset-performance-analysis/AssetPerformanceAnalysisView.vue') as any },
          
          // 通配符路由（必须放在最后）
          { path: ':feature', component: () => import('../views/UnderConstruction.vue') }
        ]
      },
      // BOM模块
      {
        path: 'bom',
        name: 'bom',
        component: () => import('../views/bom/BomView.vue'),
        children: [
          // 物料管理
          {
            path: 'material', 
            component: () => import('../views/bom/material/index.vue') as any,
            children: [
              { path: '', redirect: '/home/bom/material/list' },
              { path: 'list', name: 'bom-material', component: () => import('../views/bom/material/list.vue') as any },
              { path: 'category', name: 'bom-material-category', component: () => import('../views/bom/material/category.vue') as any },
              { path: 'detail/:id?', name: 'bom-material-detail', component: () => import('../views/bom/material/detail.vue') as any }
            ]
          },
          
          // BOM结构管理
          {
            path: 'structure', 
            component: () => import('../views/bom/structure/index.vue') as any,
            children: [
              { path: '', redirect: '/home/bom/structure/editor' },
              { path: 'editor', name: 'bom-structure', component: () => import('../views/bom/structure/editor.vue') as any },
              { path: 'compare', name: 'bom-structure-compare', component: () => import('../views/bom/structure/compare.vue') as any },
              { path: 'visualization', name: 'bom-structure-visualization', component: () => import('../views/bom/structure/visualization.vue') as any }
            ]
          },
          
          // BOM版本管理
          {
            path: 'version', 
            component: () => import('../views/bom/version/index.vue') as any,
            children: [
              { path: '', redirect: '/home/bom/version/list' },
              { path: 'list', name: 'bom-version', component: () => import('../views/bom/version/list.vue') as any },
              { path: 'detail/:id?', name: 'bom-version-detail', component: () => import('../views/bom/version/detail.vue') as any },
              { path: 'history', name: 'bom-version-history', component: () => import('../views/bom/version/history.vue') as any }
            ]
          },
          
          // 替代料管理
          {
            path: 'substitute', 
            component: () => import('../views/bom/substitute/index.vue') as any,
            children: [
              { path: '', redirect: '/home/bom/substitute/list' },
              { path: 'list', name: 'bom-substitute', component: () => import('../views/bom/substitute/list.vue') as any },
              { path: 'rules', name: 'bom-substitute-rules', component: () => import('../views/bom/substitute/rules.vue') as any },
              { path: 'detail/:id?', name: 'bom-substitute-detail', component: () => import('../views/bom/substitute/detail.vue') as any }
            ]
          },
          
          // BOM分析
          {
            path: 'analysis', 
            component: () => import('../views/bom/analysis/index.vue') as any,
            children: [
              { path: '', redirect: '/home/bom/analysis/where-used' },
              { path: 'where-used', name: 'bom-analysis-where-used', component: () => import('../views/bom/analysis/where-used.vue') as any },
              { path: 'cost', name: 'bom-analysis-cost', component: () => import('../views/bom/cost/index.vue') as any },
              { path: 'compare', name: 'bom-analysis-compare', component: () => import('../views/bom/structure/compare.vue') as any }
            ]
          },
          
          // 通配符路由（必须放在最后）
          { path: ':feature', component: () => import('../views/UnderConstruction.vue') }
        ]
      },
          // QMS模块
      {
        path: 'qms',
        name: 'qms',
        component: () => import('../views/qms/QmsView.vue'),
        children: [
          // 质量检验计划
          {
            path: 'quality-inspection',
            component: () => import('../views/qms/quality-inspection/QualityInspectionView.vue') as any,
            children: [
              { path: '', redirect: '/home/qms/quality-inspection/standard-management' },
              { path: 'standard-management', name: 'qms-quality-inspection', component: () => import('../views/qms/quality-inspection/standard-management/index.vue') as any },
              { path: 'plan-management', name: 'qms-plan-management', component: () => import('../views/qms/quality-inspection/plan-management/index.vue') as any },
              { path: 'task-management', name: 'qms-task-management', component: () => import('../views/qms/quality-inspection/task-management/index.vue') as any },
              { path: 'result-recording', name: 'qms-result-recording', component: () => import('../views/qms/quality-inspection/result-recording/index.vue') as any }
            ]
          },
          // 不合格品管理
          {
            path: 'non-conforming',
            component: () => import('../views/qms/non-conforming/NonConformingView.vue') as any,
            children: [
              { path: '', redirect: '/home/qms/non-conforming/registration' },
              { path: 'registration', name: 'qms-non-conforming', component: () => import('../views/qms/non-conforming/registration/index.vue') as any },
              { path: 'review', name: 'qms-nc-review', component: () => import('../views/qms/non-conforming/review/index.vue') as any },
              { path: 'disposal', name: 'qms-nc-disposal', component: () => import('../views/qms/non-conforming/disposal/index.vue') as any },
              { path: 'tracking', name: 'qms-nc-tracking', component: () => import('../views/qms/non-conforming/tracking/index.vue') as any }
            ]
          },
          // 质量异常管理
          {
            path: 'quality-anomaly',
            component: () => import('../views/qms/quality-anomaly/QualityAnomalyView.vue') as any,
            children: [
              { path: '', redirect: '/home/qms/quality-anomaly/report' },
              { path: 'report', name: 'qms-quality-anomaly', component: () => import('../views/qms/quality-anomaly/report/index.vue') as any },
              { path: 'analysis', name: 'qms-anomaly-analysis', component: () => import('../views/qms/quality-anomaly/analysis/index.vue') as any },
              { path: 'disposal', name: 'qms-anomaly-disposal', component: () => import('../views/qms/quality-anomaly/disposal/index.vue') as any },
              { path: 'capa', name: 'qms-anomaly-capa', component: () => import('../views/qms/quality-anomaly/capa/index.vue') as any }
            ]
          },
          // 质量数据分析
          {
            path: 'quality-analysis',
            component: () => import('../views/qms/quality-analysis/QualityAnalysisView.vue') as any,
            children: [
              { path: '', redirect: '/home/qms/quality-analysis/data-collection' },
              { path: 'data-collection', name: 'qms-quality-analysis', component: () => import('../views/qms/quality-analysis/data-collection/index.vue') as any },
              { path: 'statistical-analysis', name: 'qms-statistical-analysis', component: () => import('../views/qms/quality-analysis/statistical-analysis/index.vue') as any },
              { path: 'report-generation', name: 'qms-report-generation', component: () => import('../views/qms/quality-analysis/report-generation/index.vue') as any },
              { path: 'trend-forecast', name: 'qms-trend-forecast', component: () => import('../views/qms/quality-analysis/trend-forecast/index.vue') as any }
            ]
          },
          // 通配符路由（必须放在最后）
          { path: ':feature', component: () => import('../views/UnderConstruction.vue') }
        ]
      },
      // SCM模块
      {
        path: 'scm',
        name: 'scm',
        component: () => import('../views/scm/ScmView.vue'),
        children: [
          { path: 'demand-forecast', name: 'scm-demand-forecast', component: () => import('../views/scm/demand-forecast/index.vue') as any, meta: { title: '需求预测', backTo: '/home/scm' } },
          { path: 'planning', name: 'scm-planning', component: () => import('../views/scm/planning/index.vue') as any, meta: { title: '供应计划', backTo: '/home/scm' } },
          { path: 'supply-planning', redirect: '/home/scm/planning' },
          // 兼容旧链接：MRP运算页已合并为供应计划页内嵌标签，直接重定向避免No match警告
          { path: 'planning/mrp-run', redirect: '/home/scm/planning' },
          {
            path: 'planning/result',
            name: 'scm-planning-plan-result',
            redirect: (to: any) => ({
              path: '/home/scm/planning',
              query: { tab: 'result', ...to.query }
            }),
            meta: { title: '计划结果', backTo: '/home/scm/planning' }
          },
          { path: 'inventory-optimization', name: 'scm-inventory-optimization', component: () => import('../views/scm/inventory-optimization/index.vue') as any, meta: { title: '库存优化', backTo: '/home/scm' } },
          { path: 'logistics-collaboration', name: 'scm-logistics-collaboration', component: () => import('../views/scm/logistics-collaboration/index.vue') as any, meta: { title: '物流协同', backTo: '/home/scm' } },
          { path: 'supply-chain-visualization', name: 'scm-supply-chain-visualization', component: () => import('../views/scm/supply-chain-visualization/index.vue') as any, meta: { title: '供应链可视化', backTo: '/home/scm' } },
          { path: 'control-tower', name: 'scm-control-tower', component: () => import('../views/scm/control-tower/index.vue') as any, meta: { title: '供应链塔台', backTo: '/home/scm' } },
          { path: 'production-completion', name: 'scm-production-completion', component: () => import('../views/scm/production-completion/index.vue') as any, meta: { title: '生产完工事实', backTo: '/home/scm' } },
          { path: ':feature', component: () => import('../views/UnderConstruction.vue') }
        ]
      },
      // PLM模块
      {
        path: 'plm',
        name: 'plm',
        component: () => import('../views/plm/PlmView.vue'),
        children: [
          // 项目管理子模块
          {
            path: 'project-management', 
            component: () => import('../views/plm/project-management/index.vue') as any,
            children: [
              { path: '', redirect: '/home/plm/project-management/dashboard' },
              { path: 'dashboard', name: 'plm-project-dashboard', component: () => import('../views/plm/project-management/dashboard.vue') as any },
              { path: 'gantt-view', name: 'plm-project-gantt', component: () => import('../views/plm/project-management/gantt-view.vue') as any },
              { path: 'resource', name: 'plm-project-resource', component: () => import('../views/plm/project-management/resource.vue') as any },
              { path: 'project-detail/:id?', name: 'plm-project-detail', component: () => import('../views/plm/project-management/project-detail.vue') as any },
            ]
          },
          // 产品数据子模块
          {
            path: 'product-data', 
            component: () => import('../views/plm/product-data/index.vue') as any,
            children: [
              { path: '', redirect: '/home/plm/product-data/item-list' },
              { path: 'item-list', name: 'plm-product-item-list', component: () => import('../views/plm/product-data/item-list.vue') as any },
              { path: 'bom-editor', name: 'plm-product-bom-editor', component: () => import('../views/plm/product-data/bom-editor.vue') as any },
              { path: 'doc-viewer', name: 'plm-product-doc-viewer', component: () => import('../views/plm/product-data/doc-viewer.vue') as any },
            ]
          },
          // 工艺协同子模块
          {
            path: 'process-collaboration', 
            component: () => import('../views/plm/process-collaboration/index.vue') as any,
            children: [
              { path: '', redirect: '/home/plm/process-collaboration/process-route' },
              { path: 'process-route', name: 'plm-process-route', component: () => import('../views/plm/process-collaboration/process-route.vue') as any },
              { path: 'process-change', name: 'plm-process-change', component: () => import('../views/plm/process-collaboration/process-change.vue') as any },
              { path: 'process-file', name: 'plm-process-file', component: () => import('../views/plm/process-collaboration/process-file.vue') as any },
            ]
          },
          // 试制管理子模块
          {
            path: 'trial-production', 
            component: () => import('../views/plm/trial-production/index.vue') as any,
            children: [
              { path: '', redirect: '/home/plm/trial-production/trial-plan' },
              { path: 'trial-plan', name: 'plm-trial-plan', component: () => import('../views/plm/trial-production/trial-plan.vue') as any },
              { path: 'quality-track', name: 'plm-trial-quality-track', component: () => import('../views/plm/trial-production/quality-track.vue') as any },
              { path: 'trial-report', name: 'plm-trial-report', component: () => import('../views/plm/trial-production/trial-report.vue') as any },
            ]
          },
          { path: ':feature', component: () => import('../views/UnderConstruction.vue') }
        ]
      },
      // BI模块
      {
        path: 'bi',
        name: 'bi',
        component: () => import('../views/bi/BiView.vue'),
        children: [
          // 数据集成模块
          { path: 'data-integration', name: 'bi-data-integration', component: () => import('../views/bi/data-integration/DataIntegrationView.vue') as any },
          // 数据分析模块
          { path: 'data-analysis', name: 'bi-data-analysis', component: () => import('../views/bi/data-analysis/DataAnalysisView.vue') as any },
          // 可视化模块
          { path: 'visualization', name: 'bi-visualization', component: () => import('../views/bi/visualization/VisualizationView.vue') as any },
          // 决策支持模块
          { path: 'decision-support', name: 'bi-decision-support', component: () => import('../views/bi/decision-support/DecisionSupportView.vue') as any },
          // 通配符路由（必须放在最后）
          { path: ':feature', component: () => import('../views/UnderConstruction.vue') }
        ]
      },
      // AI模块
      {
        path: 'ai',
        name: 'ai',
        component: () => import('../views/ai/AiView.vue'),
        children: [
          // 决策建议中心（建议列表 + 反馈学习环 + 扫描触发）
          { path: 'decision-center', name: 'ai-decision-center', component: () => import('../views/ai/decision-center/DecisionCenterView.vue') as any },
          // AI 晨会简报（六域巡检）
          { path: 'morning-briefing', name: 'ai-morning-briefing', component: () => import('../views/ai/morning-briefing/MorningBriefingView.vue') as any },
          // 跨域根因会诊
          { path: 'root-cause', name: 'ai-root-cause', component: () => import('../views/ai/root-cause/RootCauseView.vue') as any },
          // 能力矩阵（自动化刻度盘 + 技能清单）
          { path: 'capabilities', name: 'ai-capabilities', component: () => import('../views/ai/capabilities/CapabilitiesView.vue') as any },
          // 通配符路由（必须放在最后）
          { path: ':feature', component: () => import('../views/UnderConstruction.vue') }
        ]
      },
      // 403页面
      {
        path: '403',
        name: '403',
        component: () => import('../views/Error403View.vue')
      }
    ]
  }
]

// 创建路由实例
const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  const authStore = useAuthStore()
  authStore.initAuth()

  const requiresAuth = to.matched.some((record) => record.meta.requiresAuth !== false)
  const isLoginRoute = to.path === '/login'

  if (!requiresAuth) {
    if (isLoginRoute && authStore.isLoggedIn) {
      const redirect = typeof to.query.redirect === 'string' && to.query.redirect.startsWith('/')
        ? to.query.redirect
        : '/'
      return redirect
    }
    return true
  }

  if (!authStore.isLoggedIn) {
    return {
      path: '/login',
      query: {
        redirect: to.fullPath || '/'
      }
    }
  }

  return true
})

export default router
