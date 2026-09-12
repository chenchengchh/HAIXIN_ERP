-- =====================================================
-- OA审批流程模板初始化脚本
-- 为各业务模块预置审批流程定义，实现OA贯穿全系统
-- =====================================================

USE oa_db;

-- 清理已有模板（幂等执行）
DELETE FROM oa_approval_process WHERE code IN (
    'CRM_SALES_ORDER_APPROVAL', 'CRM_CONTRACT_APPROVAL', 'CRM_RETURN_APPROVAL',
    'SCM_PURCHASE_ORDER_APPROVAL', 'SCM_SUPPLIER_APPROVAL',
    'ERP_PRODUCTION_ORDER_APPROVAL', 'ERP_VOUCHER_APPROVAL', 'ERP_SUPPLY_CHAIN_APPROVAL',
    'WMS_OUTBOUND_APPROVAL', 'WMS_INBOUND_APPROVAL',
    'MES_WORK_ORDER_APPROVAL',
    'LES_TRANSPORT_PLAN_APPROVAL',
    'QMS_INSPECTION_APPROVAL',
    'SRM_PURCHASE_ORDER_APPROVAL',
    'GENERAL_APPROVAL'
);

-- ========== CRM模块审批流程 ==========
INSERT INTO oa_approval_process (name, code, description, status, process_type, process_definition, form_config, creator_id, create_time, update_time)
VALUES ('CRM销售订单审批', 'CRM_SALES_ORDER_APPROVAL', 'CRM销售订单提交审批流程，审批通过后自动确认订单', 1, 'crm',
    '{"nodes":[{"id":"node_1","name":"销售经理审批","assigneeRole":"sales_manager","action":"approve"},{"id":"node_2","name":"财务审批","assigneeRole":"finance_manager","action":"approve"}]}',
    '{"fields":[{"name":"orderNo","label":"订单编号","type":"text"},{"name":"customerName","label":"客户名称","type":"text"},{"name":"amount","label":"订单金额","type":"number"}]}',
    1, NOW(), NOW());

INSERT INTO oa_approval_process (name, code, description, status, process_type, process_definition, form_config, creator_id, create_time, update_time)
VALUES ('CRM合同审批', 'CRM_CONTRACT_APPROVAL', 'CRM合同签订审批流程', 1, 'crm',
    '{"nodes":[{"id":"node_1","name":"法务审批","assigneeRole":"legal","action":"approve"},{"id":"node_2","name":"总经理审批","assigneeRole":"gm","action":"approve"}]}',
    '{"fields":[{"name":"contractNo","label":"合同编号","type":"text"},{"name":"contractAmount","label":"合同金额","type":"number"},{"name":"startDate","label":"开始日期","type":"date"},{"name":"endDate","label":"结束日期","type":"date"}]}',
    1, NOW(), NOW());

INSERT INTO oa_approval_process (name, code, description, status, process_type, process_definition, form_config, creator_id, create_time, update_time)
VALUES ('CRM退货审批', 'CRM_RETURN_APPROVAL', 'CRM退货申请审批流程', 1, 'crm',
    '{"nodes":[{"id":"node_1","name":"客服主管审批","assigneeRole":"service_manager","action":"approve"},{"id":"node_2","name":"财务审批","assigneeRole":"finance_manager","action":"approve"}]}',
    '{"fields":[{"name":"returnNo","label":"退货单号","type":"text"},{"name":"reason","label":"退货原因","type":"textarea"}]}',
    1, NOW(), NOW());

-- ========== SCM模块审批流程 ==========
INSERT INTO oa_approval_process (name, code, description, status, process_type, process_definition, form_config, creator_id, create_time, update_time)
VALUES ('SCM采购订单审批', 'SCM_PURCHASE_ORDER_APPROVAL', 'SCM采购订单审批流程，审批通过后自动下达采购', 1, 'scm',
    '{"nodes":[{"id":"node_1","name":"采购经理审批","assigneeRole":"purchase_manager","action":"approve"},{"id":"node_2","name":"财务审批","assigneeRole":"finance_manager","action":"approve"}]}',
    '{"fields":[{"name":"poNo","label":"采购单号","type":"text"},{"name":"supplierName","label":"供应商","type":"text"},{"name":"totalAmount","label":"采购金额","type":"number"}]}',
    1, NOW(), NOW());

INSERT INTO oa_approval_process (name, code, description, status, process_type, process_definition, form_config, creator_id, create_time, update_time)
VALUES ('SCM供应商准入审批', 'SCM_SUPPLIER_APPROVAL', '新供应商准入审批流程', 1, 'scm',
    '{"nodes":[{"id":"node_1","name":"采购经理审批","assigneeRole":"purchase_manager","action":"approve"},{"id":"node_2","name":"总经理审批","assigneeRole":"gm","action":"approve"}]}',
    '{"fields":[{"name":"supplierName","label":"供应商名称","type":"text"},{"name":"supplierType","label":"供应商类型","type":"text"},{"name":"contactPerson","label":"联系人","type":"text"}]}',
    1, NOW(), NOW());

-- ========== ERP模块审批流程 ==========
INSERT INTO oa_approval_process (name, code, description, status, process_type, process_definition, form_config, creator_id, create_time, update_time)
VALUES ('ERP生产订单审批', 'ERP_PRODUCTION_ORDER_APPROVAL', 'ERP生产订单审批流程，审批通过后自动下达MES工单', 1, 'erp',
    '{"nodes":[{"id":"node_1","name":"生产经理审批","assigneeRole":"production_manager","action":"approve"}]}',
    '{"fields":[{"name":"productionNo","label":"生产单号","type":"text"},{"name":"productName","label":"产品名称","type":"text"},{"name":"quantity","label":"生产数量","type":"number"},{"name":"workshop","label":"生产车间","type":"text"}]}',
    1, NOW(), NOW());

INSERT INTO oa_approval_process (name, code, description, status, process_type, process_definition, form_config, creator_id, create_time, update_time)
VALUES ('ERP凭证审批', 'ERP_VOUCHER_APPROVAL', 'ERP财务凭证审批流程', 1, 'erp',
    '{"nodes":[{"id":"node_1","name":"财务主管审批","assigneeRole":"finance_manager","action":"approve"}]}',
    '{"fields":[{"name":"voucherNo","label":"凭证编号","type":"text"},{"name":"voucherType","label":"凭证类型","type":"text"},{"name":"debitTotal","label":"借方合计","type":"number"},{"name":"creditTotal","label":"贷方合计","type":"number"}]}',
    1, NOW(), NOW());

INSERT INTO oa_approval_process (name, code, description, status, process_type, process_definition, form_config, creator_id, create_time, update_time)
VALUES ('ERP供应链单据审批', 'ERP_SUPPLY_CHAIN_APPROVAL', 'ERP供应链单据审批流程', 1, 'erp',
    '{"nodes":[{"id":"node_1","name":"供应链经理审批","assigneeRole":"scm_manager","action":"approve"}]}',
    '{"fields":[{"name":"scNo","label":"单据编号","type":"text"},{"name":"businessType","label":"业务类型","type":"text"},{"name":"totalAmount","label":"总金额","type":"number"}]}',
    1, NOW(), NOW());

-- ========== WMS模块审批流程 ==========
INSERT INTO oa_approval_process (name, code, description, status, process_type, process_definition, form_config, creator_id, create_time, update_time)
VALUES ('WMS出库审批', 'WMS_OUTBOUND_APPROVAL', 'WMS出库单审批流程', 1, 'wms',
    '{"nodes":[{"id":"node_1","name":"仓库主管审批","assigneeRole":"warehouse_manager","action":"approve"}]}',
    '{"fields":[{"name":"outboundNo","label":"出库单号","type":"text"},{"name":"warehouseCode","label":"仓库编码","type":"text"},{"name":"totalQty","label":"出库总数","type":"number"}]}',
    1, NOW(), NOW());

INSERT INTO oa_approval_process (name, code, description, status, process_type, process_definition, form_config, creator_id, create_time, update_time)
VALUES ('WMS入库审批', 'WMS_INBOUND_APPROVAL', 'WMS入库单审批流程', 1, 'wms',
    '{"nodes":[{"id":"node_1","name":"仓库主管审批","assigneeRole":"warehouse_manager","action":"approve"}]}',
    '{"fields":[{"name":"inboundNo","label":"入库单号","type":"text"},{"name":"warehouseCode","label":"仓库编码","type":"text"},{"name":"totalQty","label":"入库总数","type":"number"}]}',
    1, NOW(), NOW());

-- ========== MES模块审批流程 ==========
INSERT INTO oa_approval_process (name, code, description, status, process_type, process_definition, form_config, creator_id, create_time, update_time)
VALUES ('MES工单审批', 'MES_WORK_ORDER_APPROVAL', 'MES工单审批流程', 1, 'mes',
    '{"nodes":[{"id":"node_1","name":"生产经理审批","assigneeRole":"production_manager","action":"approve"}]}',
    '{"fields":[{"name":"workOrderNo","label":"工单编号","type":"text"},{"name":"productCode","label":"产品编码","type":"text"},{"name":"planQuantity","label":"计划数量","type":"number"}]}',
    1, NOW(), NOW());

-- ========== LES模块审批流程 ==========
INSERT INTO oa_approval_process (name, code, description, status, process_type, process_definition, form_config, creator_id, create_time, update_time)
VALUES ('LES运输计划审批', 'LES_TRANSPORT_PLAN_APPROVAL', 'LES运输计划审批流程', 1, 'les',
    '{"nodes":[{"id":"node_1","name":"物流经理审批","assigneeRole":"logistics_manager","action":"approve"}]}',
    '{"fields":[{"name":"transportNo","label":"运输单号","type":"text"},{"name":"origin","label":"始发地","type":"text"},{"name":"destination","label":"目的地","type":"text"}]}',
    1, NOW(), NOW());

-- ========== QMS模块审批流程 ==========
INSERT INTO oa_approval_process (name, code, description, status, process_type, process_definition, form_config, creator_id, create_time, update_time)
VALUES ('QMS检验审批', 'QMS_INSPECTION_APPROVAL', 'QMS检验任务审批流程', 1, 'qms',
    '{"nodes":[{"id":"node_1","name":"质量经理审批","assigneeRole":"quality_manager","action":"approve"}]}',
    '{"fields":[{"name":"inspectionNo","label":"检验单号","type":"text"},{"name":"inspectionType","label":"检验类型","type":"text"},{"name":"sampleSize","label":"抽样数量","type":"number"}]}',
    1, NOW(), NOW());

-- ========== SRM模块审批流程 ==========
INSERT INTO oa_approval_process (name, code, description, status, process_type, process_definition, form_config, creator_id, create_time, update_time)
VALUES ('SRM采购订单审批', 'SRM_PURCHASE_ORDER_APPROVAL', 'SRM采购订单审批流程', 1, 'srm',
    '{"nodes":[{"id":"node_1","name":"采购经理审批","assigneeRole":"purchase_manager","action":"approve"},{"id":"node_2","name":"总经理审批","assigneeRole":"gm","action":"approve"}]}',
    '{"fields":[{"name":"poNo","label":"采购单号","type":"text"},{"name":"supplierName","label":"供应商","type":"text"},{"name":"totalAmount","label":"采购金额","type":"number"}]}',
    1, NOW(), NOW());

-- ========== 通用审批流程 ==========
INSERT INTO oa_approval_process (name, code, description, status, process_type, process_definition, form_config, creator_id, create_time, update_time)
VALUES ('通用审批流程', 'GENERAL_APPROVAL', '通用审批流程，适用于未匹配到专用流程的审批申请', 1, 'general',
    '{"nodes":[{"id":"node_1","name":"直属上级审批","assigneeRole":"direct_manager","action":"approve"}]}',
    '{"fields":[{"name":"title","label":"审批标题","type":"text"},{"name":"description","label":"审批描述","type":"textarea"}]}',
    1, NOW(), NOW());

-- 验证
SELECT code, name, process_type, status FROM oa_approval_process ORDER BY process_type, code;
