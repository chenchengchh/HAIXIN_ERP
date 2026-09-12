USE erp_db;

INSERT INTO `erp_finance_task_rule` (`fact_type`, `result`, `action_type`, `voucher_type`, `enabled`)
SELECT 'IQC_COMPLETED', 'PASS', 'CREATE_VOUCHER', 'journal', 1
WHERE NOT EXISTS (
  SELECT 1 FROM `erp_finance_task_rule`
  WHERE `fact_type`='IQC_COMPLETED' AND `result`='PASS' AND `action_type`='CREATE_VOUCHER'
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
