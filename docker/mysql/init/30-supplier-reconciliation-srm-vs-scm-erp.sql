USE system_db;

CREATE OR REPLACE VIEW `vw_supplier_reconciliation_srm_vs_scm_erp` AS
SELECT
  s.supplier_code AS supplier_code,
  s.supplier_name AS srm_supplier_name,
  sc.supplier_name AS scm_supplier_name,
  e.supplier_name AS erp_supplier_name,
  s.status AS srm_status,
  sc.status AS scm_status,
  e.status AS erp_status,
  CONCAT_WS(';',
    IF(sc.supplier_code IS NULL, 'MISSING_IN_SCM', NULL),
    IF(e.supplier_code IS NULL, 'MISSING_IN_ERP', NULL),
    IF(sc.supplier_code IS NOT NULL AND sc.supplier_name <> s.supplier_name, 'SCM_NAME_MISMATCH', NULL),
    IF(e.supplier_code IS NOT NULL AND e.supplier_name <> s.supplier_name, 'ERP_NAME_MISMATCH', NULL),
    IF(sc.supplier_code IS NOT NULL AND sc.status IS NOT NULL AND sc.status <> s.status, 'SCM_STATUS_MISMATCH', NULL),
    IF(e.supplier_code IS NOT NULL AND e.status IS NOT NULL AND CAST(e.status AS CHAR) <> CAST(s.status AS CHAR), 'ERP_STATUS_MISMATCH', NULL)
  ) AS mismatch_reasons
FROM srm_db.srm_supplier s
LEFT JOIN scm_db.scm_supplier sc ON sc.supplier_code = s.supplier_code
LEFT JOIN erp_db.erp_supplier e ON e.supplier_code = s.supplier_code
UNION ALL
SELECT
  sc.supplier_code AS supplier_code,
  NULL AS srm_supplier_name,
  sc.supplier_name AS scm_supplier_name,
  e.supplier_name AS erp_supplier_name,
  NULL AS srm_status,
  sc.status AS scm_status,
  e.status AS erp_status,
  CONCAT_WS(';',
    'MISSING_IN_SRM',
    IF(e.supplier_code IS NULL, 'MISSING_IN_ERP', NULL)
  ) AS mismatch_reasons
FROM scm_db.scm_supplier sc
LEFT JOIN srm_db.srm_supplier s ON s.supplier_code = sc.supplier_code
LEFT JOIN erp_db.erp_supplier e ON e.supplier_code = sc.supplier_code
WHERE s.supplier_code IS NULL
UNION ALL
SELECT
  e.supplier_code AS supplier_code,
  NULL AS srm_supplier_name,
  sc.supplier_name AS scm_supplier_name,
  e.supplier_name AS erp_supplier_name,
  NULL AS srm_status,
  sc.status AS scm_status,
  e.status AS erp_status,
  CONCAT_WS(';',
    'MISSING_IN_SRM',
    IF(sc.supplier_code IS NULL, 'MISSING_IN_SCM', NULL)
  ) AS mismatch_reasons
FROM erp_db.erp_supplier e
LEFT JOIN srm_db.srm_supplier s ON s.supplier_code = e.supplier_code
LEFT JOIN scm_db.scm_supplier sc ON sc.supplier_code = e.supplier_code
WHERE s.supplier_code IS NULL;

CREATE TABLE IF NOT EXISTS `system_supplier_reconciliation_diff` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `supplier_code` varchar(64) NOT NULL,
  `mismatch_reasons` varchar(1024) NOT NULL,
  `detail_json` json DEFAULT NULL,
  `status` varchar(16) NOT NULL DEFAULT 'OPEN',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_system_supplier_diff_supplier_code` (`supplier_code`),
  KEY `idx_system_supplier_diff_status` (`status`),
  KEY `idx_system_supplier_diff_updated_time` (`updated_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商主数据对账差异队列（SRM vs SCM/ERP）';

INSERT INTO system_supplier_reconciliation_diff (supplier_code, mismatch_reasons, detail_json, status, created_time, updated_time)
SELECT
  v.supplier_code,
  v.mismatch_reasons,
  JSON_OBJECT(
    'srmSupplierName', v.srm_supplier_name,
    'scmSupplierName', v.scm_supplier_name,
    'erpSupplierName', v.erp_supplier_name,
    'srmStatus', v.srm_status,
    'scmStatus', v.scm_status,
    'erpStatus', v.erp_status
  ),
  'OPEN',
  NOW(),
  NOW()
FROM vw_supplier_reconciliation_srm_vs_scm_erp v
WHERE v.mismatch_reasons IS NOT NULL AND v.mismatch_reasons <> ''
ON DUPLICATE KEY UPDATE
  mismatch_reasons = VALUES(mismatch_reasons),
  detail_json = VALUES(detail_json),
  status = IF(status = 'IGNORED', 'IGNORED', 'OPEN'),
  updated_time = NOW();

UPDATE system_supplier_reconciliation_diff d
LEFT JOIN (
  SELECT supplier_code
  FROM vw_supplier_reconciliation_srm_vs_scm_erp
  WHERE mismatch_reasons IS NOT NULL AND mismatch_reasons <> ''
) v ON v.supplier_code = d.supplier_code
SET d.status = 'RESOLVED', d.updated_time = NOW()
WHERE v.supplier_code IS NULL AND d.status = 'OPEN';

