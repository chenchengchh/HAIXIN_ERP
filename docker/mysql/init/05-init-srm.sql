-- 初始化 SRM 数据库
CREATE DATABASE IF NOT EXISTS `srm_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `srm_db`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for srm_supplier
-- ----------------------------
DROP TABLE IF EXISTS `srm_supplier`;
CREATE TABLE `srm_supplier` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `supplier_code` varchar(64) NOT NULL COMMENT '供应商编码',
  `supplier_name` varchar(128) NOT NULL COMMENT '供应商名称',
  `contact_person` varchar(64) DEFAULT NULL COMMENT '联系人',
  `contact_phone` varchar(32) DEFAULT NULL COMMENT '联系电话',
  `email` varchar(128) DEFAULT NULL COMMENT '邮箱',
  `address` varchar(255) DEFAULT NULL COMMENT '地址',
  `category` varchar(32) DEFAULT NULL COMMENT '类别',
  `status` varchar(32) DEFAULT 'ACTIVE' COMMENT '状态',
  `type` varchar(32) DEFAULT 'POTENTIAL' COMMENT '类型',
  `rating` double DEFAULT NULL COMMENT '评分',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_supplier_code` (`supplier_code`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='供应商表';

-- ----------------------------
-- Table structure for srm_supplier_qualification
-- ----------------------------
DROP TABLE IF EXISTS `srm_supplier_qualification`;
CREATE TABLE `srm_supplier_qualification` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `supplier_id` bigint(20) DEFAULT NULL COMMENT '供应商ID',
  `qualification_type` varchar(64) DEFAULT NULL COMMENT '资质类型',
  `certificate_name` varchar(128) DEFAULT NULL COMMENT '证书名称',
  `certificate_no` varchar(64) DEFAULT NULL COMMENT '证书编号',
  `issue_date` date DEFAULT NULL COMMENT '发证日期',
  `expiry_date` date DEFAULT NULL COMMENT '有效期至',
  `attachment_url` varchar(255) DEFAULT NULL COMMENT '附件地址',
  `status` varchar(32) DEFAULT NULL COMMENT '状态',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_supplier_id` (`supplier_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商资质表';

-- ----------------------------
-- Table structure for srm_supplier_performance
-- ----------------------------
DROP TABLE IF EXISTS `srm_supplier_performance`;
CREATE TABLE `srm_supplier_performance` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `supplier_id` bigint(20) DEFAULT NULL COMMENT '供应商ID',
  `period` varchar(32) DEFAULT NULL COMMENT '考核周期',
  `quality_score` decimal(10,2) DEFAULT NULL COMMENT '质量得分',
  `delivery_score` decimal(10,2) DEFAULT NULL COMMENT '交期得分',
  `price_score` decimal(10,2) DEFAULT NULL COMMENT '价格得分',
  `service_score` decimal(10,2) DEFAULT NULL COMMENT '服务得分',
  `total_score` decimal(10,2) DEFAULT NULL COMMENT '总分',
  `level` varchar(8) DEFAULT NULL COMMENT '等级',
  `evaluator` varchar(64) DEFAULT NULL COMMENT '考评人',
  `evaluate_time` datetime DEFAULT NULL COMMENT '考评时间',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_perf_supplier_id` (`supplier_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商绩效表';

-- ----------------------------
-- Table structure for srm_supplier_credit
-- ----------------------------
DROP TABLE IF EXISTS `srm_supplier_credit`;
CREATE TABLE `srm_supplier_credit` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `supplier_id` bigint(20) NOT NULL COMMENT '供应商ID',
  `supplier_name` varchar(128) DEFAULT NULL COMMENT '供应商名称',
  `credit_score` int(11) DEFAULT NULL COMMENT '信用评分',
  `credit_level` varchar(16) DEFAULT NULL COMMENT '信用等级',
  `risk_level` varchar(16) DEFAULT NULL COMMENT '风险等级',
  `evaluation_date` date DEFAULT NULL COMMENT '评估日期',
  `next_evaluation_date` date DEFAULT NULL COMMENT '下次评估日期',
  `evaluation_comment` text DEFAULT NULL COMMENT '评估意见',
  `risk_description` text DEFAULT NULL COMMENT '风险描述',
  `improvement_suggestion` text DEFAULT NULL COMMENT '改进建议',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_supplier_credit_supplier_id` (`supplier_id`),
  KEY `idx_supplier_credit_supplier_id` (`supplier_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商信用评估表';

-- ----------------------------
-- Table structure for srm_purchase_order
-- ----------------------------
DROP TABLE IF EXISTS `srm_purchase_order`;
CREATE TABLE `srm_purchase_order` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `order_no` varchar(64) NOT NULL COMMENT '订单号',
  `supplier_id` bigint(20) DEFAULT NULL COMMENT '供应商ID',
  `supplier_code` varchar(64) DEFAULT NULL COMMENT '供应商编码',
  `supplier_name` varchar(128) DEFAULT NULL COMMENT '供应商名称',
  `total_amount` decimal(18,2) DEFAULT NULL COMMENT '总金额',
  `currency` varchar(16) DEFAULT 'CNY' COMMENT '币种',
  `status` varchar(32) DEFAULT 'CREATED' COMMENT '状态',
  `order_date` datetime DEFAULT NULL COMMENT '订单日期',
  `expected_delivery_date` datetime DEFAULT NULL COMMENT '预计交货日期',
  `remarks` varchar(512) DEFAULT NULL COMMENT '备注',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='采购订单表';

-- ----------------------------
-- Table structure for srm_purchase_order_item
-- ----------------------------
DROP TABLE IF EXISTS `srm_purchase_order_item`;
CREATE TABLE `srm_purchase_order_item` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `purchase_order_id` bigint(20) NOT NULL COMMENT '采购订单ID',
  `material_code` varchar(64) NOT NULL COMMENT '物料编码',
  `material_name` varchar(128) DEFAULT NULL COMMENT '物料名称',
  `quantity` decimal(18,4) DEFAULT NULL COMMENT '数量',
  `unit` varchar(16) DEFAULT NULL COMMENT '单位',
  `unit_price` decimal(18,4) DEFAULT NULL COMMENT '单价',
  `subtotal` decimal(18,2) DEFAULT NULL COMMENT '小计',
  `received_quantity` decimal(18,4) DEFAULT '0.0000' COMMENT '已收货数量',
  PRIMARY KEY (`id`),
  KEY `idx_purchase_order_id` (`purchase_order_id`),
  CONSTRAINT `fk_po_item_po` FOREIGN KEY (`purchase_order_id`) REFERENCES `srm_purchase_order` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='采购订单明细表';

-- ----------------------------
-- Table structure for srm_integration_inbox（SCM→SRM 事件幂等去重）
-- ----------------------------
DROP TABLE IF EXISTS `srm_integration_inbox`;
CREATE TABLE `srm_integration_inbox` (
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
  UNIQUE KEY `uk_srm_inbox_event_key` (`event_key`),
  UNIQUE KEY `uk_srm_inbox_event_id` (`event_id`),
  KEY `idx_srm_inbox_trace_id` (`trace_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='SRM 集成事件 Inbox（幂等去重）';

-- ----------------------------
-- Table structure for srm_integration_task（SRM Outbox/集成任务队列）
-- ----------------------------
DROP TABLE IF EXISTS `srm_integration_task`;
CREATE TABLE `srm_integration_task` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `action_type` varchar(64) DEFAULT NULL COMMENT '动作类型',
  `event_id` varchar(64) DEFAULT NULL COMMENT '事件ID（用于追踪）',
  `trace_id` varchar(64) DEFAULT NULL COMMENT '链路追踪ID（用于检索）',
  `producer` varchar(64) DEFAULT NULL COMMENT '生产者服务名',
  `event_version` int(11) DEFAULT '1' COMMENT '事件版本',
  `partition_key` varchar(128) DEFAULT NULL COMMENT '分区键（主业务键）',
  `idempotency_key` varchar(200) DEFAULT NULL COMMENT '幂等键',
  `request_body` longtext DEFAULT NULL COMMENT '请求体',
  `status` varchar(32) DEFAULT NULL COMMENT '状态',
  `external_ref_no` varchar(128) DEFAULT NULL COMMENT '外部引用编号',
  `retry_count` int(11) DEFAULT '0' COMMENT '重试次数',
  `last_error` text DEFAULT NULL COMMENT '最后错误',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_srm_task_idempotency_key` (`idempotency_key`),
  KEY `idx_srm_task_status` (`status`),
  KEY `idx_srm_task_action_type` (`action_type`),
  KEY `idx_srm_task_event_id` (`event_id`),
  KEY `idx_srm_task_trace_id` (`trace_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='SRM 集成任务表（Outbox）';

-- ----------------------------
-- Write gate：SRM PO 镜像表仅允许同步账号写入（防止双主/双写）
-- 允许账号：srm_sync、root
-- ----------------------------
DROP TRIGGER IF EXISTS `trg_srm_po_write_gate_bi`;
DROP TRIGGER IF EXISTS `trg_srm_po_write_gate_bu`;
DROP TRIGGER IF EXISTS `trg_srm_po_write_gate_bd`;
DROP TRIGGER IF EXISTS `trg_srm_po_item_write_gate_bi`;
DROP TRIGGER IF EXISTS `trg_srm_po_item_write_gate_bu`;
DROP TRIGGER IF EXISTS `trg_srm_po_item_write_gate_bd`;
DROP TRIGGER IF EXISTS `trg_srm_inbox_write_gate_bi`;

DELIMITER $$
CREATE TRIGGER `trg_srm_po_write_gate_bi` BEFORE INSERT ON `srm_purchase_order`
FOR EACH ROW
BEGIN
  IF USER() NOT LIKE 'srm_sync@%' AND USER() NOT LIKE 'root@%' THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'SRM PO 镜像表只允许同步账号写入';
  END IF;
END$$

CREATE TRIGGER `trg_srm_po_write_gate_bu` BEFORE UPDATE ON `srm_purchase_order`
FOR EACH ROW
BEGIN
  IF USER() NOT LIKE 'srm_sync@%' AND USER() NOT LIKE 'root@%' THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'SRM PO 镜像表只允许同步账号写入';
  END IF;
END$$

CREATE TRIGGER `trg_srm_po_write_gate_bd` BEFORE DELETE ON `srm_purchase_order`
FOR EACH ROW
BEGIN
  IF USER() NOT LIKE 'srm_sync@%' AND USER() NOT LIKE 'root@%' THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'SRM PO 镜像表只允许同步账号写入';
  END IF;
END$$

CREATE TRIGGER `trg_srm_po_item_write_gate_bi` BEFORE INSERT ON `srm_purchase_order_item`
FOR EACH ROW
BEGIN
  IF USER() NOT LIKE 'srm_sync@%' AND USER() NOT LIKE 'root@%' THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'SRM PO 明细镜像表只允许同步账号写入';
  END IF;
END$$

CREATE TRIGGER `trg_srm_po_item_write_gate_bu` BEFORE UPDATE ON `srm_purchase_order_item`
FOR EACH ROW
BEGIN
  IF USER() NOT LIKE 'srm_sync@%' AND USER() NOT LIKE 'root@%' THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'SRM PO 明细镜像表只允许同步账号写入';
  END IF;
END$$

CREATE TRIGGER `trg_srm_po_item_write_gate_bd` BEFORE DELETE ON `srm_purchase_order_item`
FOR EACH ROW
BEGIN
  IF USER() NOT LIKE 'srm_sync@%' AND USER() NOT LIKE 'root@%' THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'SRM PO 明细镜像表只允许同步账号写入';
  END IF;
END$$

CREATE TRIGGER `trg_srm_inbox_write_gate_bi` BEFORE INSERT ON `srm_integration_inbox`
FOR EACH ROW
BEGIN
  IF USER() NOT LIKE 'srm_sync@%' AND USER() NOT LIKE 'root@%' THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'SRM Inbox 只允许同步账号写入';
  END IF;
END$$
DELIMITER ;

-- ----------------------------
-- Table structure for srm_delivery_note
-- ----------------------------
DROP TABLE IF EXISTS `srm_delivery_note`;
CREATE TABLE `srm_delivery_note` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `delivery_note_no` varchar(64) NOT NULL COMMENT '送货单号',
  `purchase_order_no` varchar(64) DEFAULT NULL COMMENT '采购订单号',
  `supplier_id` bigint(20) DEFAULT NULL COMMENT '供应商ID',
  `supplier_name` varchar(128) DEFAULT NULL COMMENT '供应商名称',
  `logistics_company` varchar(128) DEFAULT NULL COMMENT '物流公司',
  `tracking_number` varchar(64) DEFAULT NULL COMMENT '物流单号',
  `delivery_date` datetime DEFAULT NULL COMMENT '送货日期',
  `status` varchar(32) DEFAULT NULL COMMENT '状态',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_delivery_note_no` (`delivery_note_no`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='送货单表';

-- ----------------------------
-- Table structure for srm_delivery_note_item
-- ----------------------------
DROP TABLE IF EXISTS `srm_delivery_note_item`;
CREATE TABLE `srm_delivery_note_item` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `delivery_note_id` bigint(20) NOT NULL COMMENT '送货单ID',
  `purchase_order_no` varchar(64) DEFAULT NULL COMMENT '采购订单号',
  `purchase_order_item_id` varchar(64) DEFAULT NULL COMMENT '采购订单明细ID',
  `material_code` varchar(64) NOT NULL COMMENT '物料编码',
  `material_name` varchar(128) DEFAULT NULL COMMENT '物料名称',
  `quantity` decimal(18,4) DEFAULT NULL COMMENT '数量',
  `unit` varchar(16) DEFAULT NULL COMMENT '单位',
  `batch_no` varchar(64) DEFAULT NULL COMMENT '批次号',
  PRIMARY KEY (`id`),
  KEY `idx_delivery_note_id` (`delivery_note_id`),
  CONSTRAINT `fk_dn_item_dn` FOREIGN KEY (`delivery_note_id`) REFERENCES `srm_delivery_note` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='送货单明细表';

-- ----------------------------
-- Table structure for srm_contract
-- ----------------------------
DROP TABLE IF EXISTS `srm_contract`;
CREATE TABLE `srm_contract` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `contract_no` varchar(64) DEFAULT NULL,
  `title` varchar(128) DEFAULT NULL,
  `supplier_id` bigint(20) DEFAULT NULL,
  `start_date` datetime DEFAULT NULL,
  `end_date` datetime DEFAULT NULL,
  `status` varchar(32) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for srm_inquiry
-- ----------------------------
DROP TABLE IF EXISTS `srm_inquiry`;
CREATE TABLE `srm_inquiry` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `inquiry_no` varchar(64) DEFAULT NULL,
  `title` varchar(128) DEFAULT NULL,
  `type` varchar(32) DEFAULT NULL COMMENT '公开/邀请',
  `status` varchar(32) DEFAULT NULL,
  `start_time` datetime DEFAULT NULL,
  `deadline` datetime DEFAULT NULL,
  `remarks` varchar(512) DEFAULT NULL,
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_inquiry_no` (`inquiry_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for srm_inquiry_item
-- ----------------------------
DROP TABLE IF EXISTS `srm_inquiry_item`;
CREATE TABLE `srm_inquiry_item` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `inquiry_id` bigint(20) DEFAULT NULL,
  `material_code` varchar(64) DEFAULT NULL,
  `material_name` varchar(128) DEFAULT NULL,
  `quantity` decimal(18,4) DEFAULT NULL,
  `unit` varchar(16) DEFAULT NULL,
  `target_price` decimal(18,4) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_inquiry_id` (`inquiry_id`),
  CONSTRAINT `fk_inquiry_item_inquiry` FOREIGN KEY (`inquiry_id`) REFERENCES `srm_inquiry` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for srm_quotation
-- ----------------------------
DROP TABLE IF EXISTS `srm_quotation`;
CREATE TABLE `srm_quotation` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `inquiry_id` bigint(20) DEFAULT NULL,
  `supplier_id` bigint(20) DEFAULT NULL,
  `supplier_name` varchar(128) DEFAULT NULL,
  `total_amount` decimal(18,2) DEFAULT NULL,
  `currency` varchar(16) DEFAULT NULL,
  `quote_time` datetime DEFAULT NULL,
  `is_recommend` tinyint(1) DEFAULT NULL,
  `status` varchar(32) DEFAULT NULL,
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_quotation_inquiry_id` (`inquiry_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for srm_quotation_item
-- ----------------------------
DROP TABLE IF EXISTS `srm_quotation_item`;
CREATE TABLE `srm_quotation_item` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `quotation_id` bigint(20) DEFAULT NULL,
  `inquiry_item_id` bigint(20) DEFAULT NULL,
  `price` decimal(18,4) DEFAULT NULL,
  `quantity` decimal(18,4) DEFAULT NULL,
  `delivery_date` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_quotation_item_quotation_id` (`quotation_id`),
  CONSTRAINT `fk_quotation_item_quotation` FOREIGN KEY (`quotation_id`) REFERENCES `srm_quotation` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

SET FOREIGN_KEY_CHECKS = 1;

-- ----------------------------
-- Mock Data
-- ----------------------------

-- Suppliers
INSERT INTO `srm_supplier` (`supplier_code`, `supplier_name`, `contact_person`, `contact_phone`, `email`, `address`, `category`, `status`, `type`, `rating`) VALUES
('SUP001', '华为技术有限公司', '张三', '13800138000', 'zhangsan@huawei.com', '深圳市龙岗区坂田华为基地', 'A', 'ACTIVE', 'QUALIFIED', 4.9),
('SUP002', '联想集团', '李四', '13900139000', 'lisi@lenovo.com', '北京市海淀区上地信息产业基地', 'A', 'ACTIVE', 'QUALIFIED', 4.8),
('SUP003', '小米科技有限责任公司', '王五', '13700137000', 'wangwu@xiaomi.com', '北京市海淀区清河中街68号', 'B', 'ACTIVE', 'POTENTIAL', 4.5),
('SUP004', '宁德时代新能源科技股份有限公司', '赵六', '13600136000', 'zhaoliu@catl.com', '福建省宁德市蕉城区', 'A', 'ACTIVE', 'QUALIFIED', 4.9),
('SUP005', '比亚迪股份有限公司', '孙七', '13500135000', 'sunqi@byd.com', '深圳市坪山区比亚迪路3009号', 'B', 'ACTIVE', 'POTENTIAL', 4.7);

-- Supplier Qualifications
INSERT INTO `srm_supplier_qualification` (`supplier_id`, `qualification_type`, `certificate_name`, `certificate_no`, `issue_date`, `expiry_date`, `status`) VALUES
(1, 'ISO9001', '质量管理体系认证', 'ISO9001-2023-001', '2023-01-01', '2026-01-01', 'VALID'),
(1, 'BUSINESS_LICENSE', '营业执照', '91440300MA5F123456', '2010-01-01', '2050-01-01', 'VALID');

-- Purchase Orders
INSERT INTO `srm_purchase_order` (`order_no`, `supplier_id`, `supplier_name`, `total_amount`, `currency`, `status`, `order_date`, `expected_delivery_date`, `remarks`) VALUES
('PO20231001001', 1, '华为技术有限公司', 500000.00, 'CNY', 'APPROVED', '2023-10-01 10:00:00', '2023-10-15 10:00:00', '紧急采购'),
('PO20231002002', 2, '联想集团', 120000.00, 'CNY', 'CREATED', '2023-10-02 14:30:00', '2023-10-20 10:00:00', '季度办公设备采购'),
('PO20231003003', 4, '宁德时代新能源科技股份有限公司', 2000000.00, 'CNY', 'SENT', '2023-10-03 09:00:00', '2023-11-01 10:00:00', '电池模组采购');

-- Purchase Order Items
INSERT INTO `srm_purchase_order_item` (`purchase_order_id`, `material_code`, `material_name`, `quantity`, `unit`, `unit_price`, `subtotal`, `received_quantity`) VALUES
(1, 'MAT001', '5G通信模组', 1000.0000, 'PCS', 500.0000, 500000.00, 0.0000),
(2, 'MAT002', 'ThinkPad X1 Carbon', 10.0000, 'PCS', 12000.0000, 120000.00, 0.0000),
(3, 'MAT003', '动力电池包', 50.0000, 'SET', 40000.0000, 2000000.00, 0.0000);
