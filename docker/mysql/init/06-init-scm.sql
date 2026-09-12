-- 初始化 SCM 数据库
CREATE DATABASE IF NOT EXISTS `scm_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `scm_db`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for scm_demand_forecast
-- ----------------------------
DROP TABLE IF EXISTS `scm_demand_forecast`;
CREATE TABLE `scm_demand_forecast` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `product_code` varchar(64) NOT NULL COMMENT '产品编码',
  `product_name` varchar(128) NOT NULL COMMENT '产品名称',
  `region` varchar(64) DEFAULT NULL COMMENT '区域',
  `period` varchar(16) NOT NULL COMMENT '预测周期(YYYY-MM)',
  `version_id` bigint(20) DEFAULT NULL COMMENT '版本ID',
  `version_no` int(11) DEFAULT NULL COMMENT '版本号',
  `history_sales` longtext DEFAULT NULL COMMENT '历史销量(JSON)',
  `baseline_forecast` decimal(18,2) DEFAULT '0.00' COMMENT '基准预测',
  `promotion_adjustment` decimal(18,2) DEFAULT '0.00' COMMENT '促销调整',
  `seasonal_adjustment` decimal(18,2) DEFAULT '0.00' COMMENT '季节调整',
  `manual_adjustment` decimal(18,2) DEFAULT '0.00' COMMENT '人工调整',
  `final_forecast` decimal(18,2) DEFAULT '0.00' COMMENT '最终预测',
  `status` varchar(32) DEFAULT 'DRAFT' COMMENT '状态',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='需求预测表';

-- ----------------------------
-- Table structure for scm_forecast_version
-- ----------------------------
DROP TABLE IF EXISTS `scm_forecast_version`;
CREATE TABLE `scm_forecast_version` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `period` varchar(16) NOT NULL COMMENT '预测周期(YYYY-MM)',
  `version_no` int(11) NOT NULL COMMENT '版本号',
  `status` varchar(32) NOT NULL COMMENT '状态(DRAFT/APPROVED/PUBLISHED)',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `published_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_period_version` (`period`, `version_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预测版本表';

-- ----------------------------
-- Table structure for scm_mrp_plan
-- ----------------------------
DROP TABLE IF EXISTS `scm_mrp_plan`;
CREATE TABLE `scm_mrp_plan` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `run_no` varchar(128) NOT NULL COMMENT '运行编号',
  `run_name` varchar(128) DEFAULT NULL COMMENT '运行名称',
  `run_date` datetime DEFAULT NULL COMMENT '运行时间',
  `demand_source` varchar(32) DEFAULT NULL COMMENT '需求来源',
  `consider_factors` varchar(255) DEFAULT NULL COMMENT '考虑因素',
  `plan_horizon` int(11) DEFAULT '30' COMMENT '计划展望期(天)',
  `status` varchar(32) DEFAULT 'PENDING' COMMENT '状态',
  `result_count` int(11) DEFAULT '0' COMMENT '结果数量',
  `start_time` datetime DEFAULT NULL,
  `end_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MRP计划表';

-- ----------------------------
-- Table structure for scm_mrp_result
-- ----------------------------
DROP TABLE IF EXISTS `scm_mrp_result`;
CREATE TABLE `scm_mrp_result` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `plan_id` bigint(20) NOT NULL COMMENT 'MRP计划ID',
  `type` varchar(32) NOT NULL COMMENT '建议类型(PURCHASE/PRODUCTION)',
  `material_code` varchar(64) NOT NULL COMMENT '物料编码',
  `material_name` varchar(128) DEFAULT NULL COMMENT '物料名称',
  `quantity` decimal(18,4) DEFAULT NULL COMMENT '建议数量',
  `suggest_date` date DEFAULT NULL COMMENT '建议日期',
  `required_date` date DEFAULT NULL COMMENT '需求日期',
  `source_id` varchar(128) DEFAULT NULL COMMENT '来源需求ID',
  `status` varchar(32) DEFAULT 'PENDING' COMMENT '状态',
  `external_ref_type` varchar(32) DEFAULT NULL COMMENT '外部引用类型',
  `external_ref_no` varchar(128) DEFAULT NULL COMMENT '外部引用编号',
  `confirmed_by` varchar(64) DEFAULT NULL COMMENT '确认人',
  `confirmed_time` datetime DEFAULT NULL COMMENT '确认时间',
  `rejected_reason` text DEFAULT NULL COMMENT '驳回原因',
  `released_time` datetime DEFAULT NULL COMMENT '下达时间',
  PRIMARY KEY (`id`),
  KEY `idx_plan_id` (`plan_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MRP运算结果表';

-- ----------------------------
-- Table structure for scm_inventory_strategy
-- ----------------------------
DROP TABLE IF EXISTS `scm_inventory_strategy`;
CREATE TABLE `scm_inventory_strategy` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `material_code` varchar(64) NOT NULL COMMENT '物料编码',
  `material_name` varchar(128) DEFAULT NULL COMMENT '物料名称',
  `abc_class` varchar(8) DEFAULT NULL COMMENT 'ABC分类',
  `safety_stock` decimal(18,4) DEFAULT '0.0000' COMMENT '安全库存',
  `reorder_point` decimal(18,4) DEFAULT '0.0000' COMMENT '再订货点',
  `eoq` decimal(18,4) DEFAULT '0.0000' COMMENT '经济订货批量',
  `min_stock` decimal(18,4) DEFAULT '0.0000' COMMENT '最低库存',
  `max_stock` decimal(18,4) DEFAULT '0.0000' COMMENT '最高库存',
  `service_level_target` decimal(5,2) DEFAULT NULL COMMENT '服务水平目标',
  `status` varchar(32) DEFAULT 'ACTIVE' COMMENT '状态',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_material_code` (`material_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存策略表';

-- ----------------------------
-- Table structure for scm_integration_task
-- ----------------------------
DROP TABLE IF EXISTS `scm_integration_task`;
CREATE TABLE `scm_integration_task` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `plan_id` bigint(20) DEFAULT NULL COMMENT 'MRP计划ID',
  `result_id` bigint(20) DEFAULT NULL COMMENT 'MRP结果ID',
  `action_type` varchar(64) DEFAULT NULL COMMENT '动作类型',
  `event_id` varchar(64) DEFAULT NULL COMMENT '事件ID（用于追踪）',
  `trace_id` varchar(64) DEFAULT NULL COMMENT '链路追踪ID（用于检索）',
  `producer` varchar(64) DEFAULT NULL COMMENT '生产者服务名',
  `event_version` int(11) DEFAULT '1' COMMENT '事件版本',
  `partition_key` varchar(128) DEFAULT NULL COMMENT '分区键（主业务键）',
  `idempotency_key` varchar(128) DEFAULT NULL COMMENT '幂等键',
  `request_body` longtext DEFAULT NULL COMMENT '请求体',
  `status` varchar(32) DEFAULT NULL COMMENT '状态',
  `external_ref_no` varchar(128) DEFAULT NULL COMMENT '外部引用编号',
  `retry_count` int(11) DEFAULT '0' COMMENT '重试次数',
  `last_error` text DEFAULT NULL COMMENT '最后错误',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_plan_result` (`plan_id`, `result_id`),
  KEY `idx_scm_task_event_id` (`event_id`),
  KEY `idx_scm_task_trace_id` (`trace_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='集成任务表';

-- ----------------------------
-- Table structure for scm_integration_inbox（SRM→SCM 事件幂等去重）
-- ----------------------------
DROP TABLE IF EXISTS `scm_integration_inbox`;
CREATE TABLE `scm_integration_inbox` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `event_key` varchar(200) NOT NULL COMMENT '幂等键（唯一）',
  `event_id` varchar(64) DEFAULT NULL COMMENT '事件ID（用于追踪；有值时全局唯一）',
  `trace_id` varchar(64) DEFAULT NULL COMMENT '链路追踪ID（用于检索）',
  `producer` varchar(64) DEFAULT NULL COMMENT '生产者服务名',
  `event_version` int(11) DEFAULT '1' COMMENT '事件版本',
  `partition_key` varchar(128) DEFAULT NULL COMMENT '分区键（主业务键）',
  `idempotency_key` varchar(200) DEFAULT NULL COMMENT '业务幂等键（兼容 event_key）',
  `event_type` varchar(50) NOT NULL COMMENT '事件类型',
  `received_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '接收时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_scm_inbox_event_key` (`event_key`),
  UNIQUE KEY `uk_scm_inbox_event_id` (`event_id`),
  KEY `idx_scm_inbox_trace_id` (`trace_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='SCM 集成事件 Inbox（幂等去重）';

-- ----------------------------
-- Table structure for scm_supplier
-- ----------------------------
DROP TABLE IF EXISTS `scm_supplier`;
CREATE TABLE `scm_supplier` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `supplier_code` varchar(50) NOT NULL,
  `supplier_name` varchar(100) NOT NULL,
  `supplier_type` varchar(20) DEFAULT NULL,
  `supplier_level` varchar(20) DEFAULT NULL,
  `status` varchar(20) NOT NULL,
  `contact_person` varchar(50) DEFAULT NULL,
  `contact_phone` varchar(20) DEFAULT NULL,
  `contact_email` varchar(100) DEFAULT NULL,
  `address` varchar(200) DEFAULT NULL,
  `business_license` varchar(64) DEFAULT NULL,
  `credit_level` varchar(20) DEFAULT NULL,
  `created_by` varchar(50) DEFAULT NULL,
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_by` varchar(50) DEFAULT NULL,
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `remark` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_supplier_code` (`supplier_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商表';

-- ----------------------------
-- Table structure for scm_purchase_order
-- ----------------------------
DROP TABLE IF EXISTS `scm_purchase_order`;
CREATE TABLE `scm_purchase_order` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `order_no` varchar(50) NOT NULL,
  `supplier_id` bigint(20) NOT NULL,
  `supplier_code` varchar(50) NOT NULL,
  `supplier_name` varchar(100) NOT NULL,
  `purchase_type` int(11) NOT NULL,
  `order_status` int(11) NOT NULL,
  `order_amount` decimal(18,2) NOT NULL,
  `expected_delivery_date` datetime DEFAULT NULL,
  `actual_delivery_date` datetime DEFAULT NULL,
  `created_by` varchar(50) DEFAULT NULL,
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_by` varchar(50) DEFAULT NULL,
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `remark` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_po_no` (`order_no`),
  KEY `idx_supplier` (`supplier_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购订单表';

-- ----------------------------
-- Table structure for scm_purchase_order_item
-- ----------------------------
DROP TABLE IF EXISTS `scm_purchase_order_item`;
CREATE TABLE `scm_purchase_order_item` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `order_id` bigint(20) NOT NULL,
  `order_no` varchar(50) NOT NULL,
  `material_code` varchar(50) NOT NULL,
  `material_name` varchar(100) NOT NULL,
  `material_spec` varchar(100) DEFAULT NULL,
  `unit` varchar(20) DEFAULT NULL,
  `quantity` decimal(18,4) NOT NULL,
  `unit_price` decimal(18,2) NOT NULL,
  `amount` decimal(18,2) NOT NULL,
  `received_quantity` decimal(18,4) DEFAULT NULL,
  `created_by` varchar(50) DEFAULT NULL,
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_by` varchar(50) DEFAULT NULL,
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `remark` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_po` (`order_id`),
  KEY `idx_po_no` (`order_no`),
  KEY `idx_material` (`material_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购订单明细表';

-- ----------------------------
-- Table structure for scm_shipment
-- ----------------------------
DROP TABLE IF EXISTS `scm_shipment`;
CREATE TABLE `scm_shipment` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `shipment_no` varchar(64) NOT NULL,
  `related_type` varchar(32) DEFAULT NULL,
  `related_id` bigint(20) DEFAULT NULL,
  `related_no` varchar(64) DEFAULT NULL,
  `supplier_id` bigint(20) DEFAULT NULL,
  `supplier_name` varchar(100) DEFAULT NULL,
  `origin` varchar(128) DEFAULT NULL,
  `destination` varchar(128) DEFAULT NULL,
  `transport_mode` varchar(32) DEFAULT NULL,
  `eta` datetime DEFAULT NULL,
  `arrived_time` datetime DEFAULT NULL,
  `status` varchar(32) DEFAULT NULL,
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_shipment_no` (`shipment_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='运单表';

-- ----------------------------
-- Table structure for scm_transport_event
-- ----------------------------
DROP TABLE IF EXISTS `scm_transport_event`;
CREATE TABLE `scm_transport_event` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `shipment_id` bigint(20) NOT NULL,
  `event_type` varchar(32) DEFAULT NULL,
  `event_time` datetime DEFAULT NULL,
  `location` varchar(128) DEFAULT NULL,
  `description` text DEFAULT NULL,
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_shipment_id` (`shipment_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='运输事件表';

-- ----------------------------
-- Table structure for scm_forecast_config
-- ----------------------------
DROP TABLE IF EXISTS `scm_forecast_config`;
CREATE TABLE `scm_forecast_config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `model_type` varchar(64) DEFAULT NULL,
  `history_months` int(11) DEFAULT NULL,
  `smoothing_alpha` decimal(8,6) DEFAULT NULL,
  `enabled` int(11) DEFAULT 1,
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='需求预测配置表';

-- ----------------------------
-- Table structure for scm_forecast_data_source
-- ----------------------------
DROP TABLE IF EXISTS `scm_forecast_data_source`;
CREATE TABLE `scm_forecast_data_source` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `source_type` varchar(64) DEFAULT NULL,
  `name` varchar(128) DEFAULT NULL,
  `enabled` int(11) DEFAULT 1,
  `status` varchar(32) DEFAULT NULL,
  `last_test_time` datetime DEFAULT NULL,
  `last_error` text DEFAULT NULL,
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='需求预测数据源表';

-- ----------------------------
-- Table structure for scm_kpi_snapshot
-- ----------------------------
DROP TABLE IF EXISTS `scm_kpi_snapshot`;
CREATE TABLE `scm_kpi_snapshot` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `snapshot_date` date NOT NULL,
  `integration_failed_count` bigint(20) DEFAULT 0,
  `mrp_release_failed_count` bigint(20) DEFAULT 0,
  `mrp_pending_release_count` bigint(20) DEFAULT 0,
  `low_stock_risk_count` bigint(20) DEFAULT 0,
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_snapshot_date` (`snapshot_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='控制塔KPI快照表';

-- ----------------------------
-- Table structure for scm_logistics_provider
-- ----------------------------
DROP TABLE IF EXISTS `scm_logistics_provider`;
CREATE TABLE `scm_logistics_provider` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `provider_id` varchar(64) DEFAULT NULL,
  `provider_name` varchar(128) DEFAULT NULL,
  `service_range` varchar(255) DEFAULT NULL,
  `contact_person` varchar(64) DEFAULT NULL,
  `contact_phone` varchar(32) DEFAULT NULL,
  `performance_rating` decimal(6,2) DEFAULT 0,
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物流商表';

SET FOREIGN_KEY_CHECKS = 1;
