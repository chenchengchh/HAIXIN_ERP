USE erp_db;

CREATE TABLE IF NOT EXISTS `erp_finance_task_rule` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `fact_type` varchar(32) NOT NULL,
  `result` varchar(32) NOT NULL DEFAULT '*',
  `action_type` varchar(32) NOT NULL,
  `voucher_type` varchar(16) DEFAULT NULL,
  `enabled` int(11) NOT NULL DEFAULT '1',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_erp_finance_task_rule` (`fact_type`, `result`, `action_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP 财务待办处理规则';

CREATE TABLE IF NOT EXISTS `erp_finance_task_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `task_id` bigint(20) NOT NULL,
  `action` varchar(32) NOT NULL,
  `message` text DEFAULT NULL,
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_erp_finance_task_log_task_id` (`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP 财务待办处理日志';

INSERT INTO `erp_finance_task_rule` (`fact_type`, `result`, `action_type`, `voucher_type`, `enabled`)
SELECT 'IQC_COMPLETED', 'PASS', 'CREATE_VOUCHER', 'journal', 1
WHERE NOT EXISTS (
  SELECT 1 FROM `erp_finance_task_rule`
  WHERE `fact_type`='IQC_COMPLETED' AND `result`='PASS' AND `action_type`='CREATE_VOUCHER'
);

INSERT INTO `erp_finance_task_rule` (`fact_type`, `result`, `action_type`, `voucher_type`, `enabled`)
SELECT 'RECEIPT_COMPLETED', 'PASS', 'CREATE_VOUCHER', 'journal', 1
WHERE NOT EXISTS (
  SELECT 1 FROM `erp_finance_task_rule`
  WHERE `fact_type`='RECEIPT_COMPLETED' AND `result`='PASS' AND `action_type`='CREATE_VOUCHER'
);

INSERT INTO `erp_finance_task_rule` (`fact_type`, `result`, `action_type`, `voucher_type`, `enabled`)
SELECT 'RECEIPT_COMPLETED', '*', 'CREATE_VOUCHER', 'journal', 1
WHERE NOT EXISTS (
  SELECT 1 FROM `erp_finance_task_rule`
  WHERE `fact_type`='RECEIPT_COMPLETED' AND `result`='*' AND `action_type`='CREATE_VOUCHER'
);

INSERT INTO `erp_finance_task_rule` (`fact_type`, `result`, `action_type`, `voucher_type`, `enabled`)
SELECT 'IQC_COMPLETED', 'FAIL', 'CONFIRM_ONLY', NULL, 1
WHERE NOT EXISTS (
  SELECT 1 FROM `erp_finance_task_rule`
  WHERE `fact_type`='IQC_COMPLETED' AND `result`='FAIL' AND `action_type`='CONFIRM_ONLY'
);
