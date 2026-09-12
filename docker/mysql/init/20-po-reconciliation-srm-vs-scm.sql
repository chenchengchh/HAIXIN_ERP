USE scm_db;

CREATE OR REPLACE VIEW `vw_po_reconciliation_srm_vs_scm` AS
SELECT
  COALESCE(p.order_no, s.order_no) AS order_no,
  s.status AS srm_status,
  CASE UPPER(COALESCE(s.status, 'CREATED'))
    WHEN 'CREATED' THEN 10
    WHEN 'SUBMITTED' THEN 20
    WHEN 'APPROVED' THEN 30
    WHEN 'CONFIRMED' THEN 40
    WHEN 'PARTIALLY_RECEIVED' THEN 50
    WHEN 'PARTIAL_RECEIVED' THEN 50
    WHEN 'QC_HOLD' THEN 58
    WHEN 'HOLD' THEN 58
    WHEN 'COMPLETED' THEN 60
    WHEN 'RECEIVED' THEN 60
    WHEN 'CLOSED' THEN 70
    WHEN 'CANCELLED' THEN 90
    ELSE 10
  END AS srm_status_mapped,
  p.order_status AS scm_status,
  s.total_amount AS srm_head_amount,
  p.order_amount AS scm_head_amount,
  COALESCE(si.item_amount_sum, 0) AS srm_item_amount_sum,
  COALESCE(pi.item_amount_sum, 0) AS scm_item_amount_sum,
  COALESCE(si.item_count, 0) AS srm_item_count,
  COALESCE(pi.item_count, 0) AS scm_item_count,
  COALESCE(si.received_qty_sum, 0) AS srm_received_qty_sum,
  COALESCE(pi.received_qty_sum, 0) AS scm_received_qty_sum,
  CONCAT_WS(';',
    IF(s.order_no IS NULL, 'MISSING_IN_SRM', NULL),
    IF(p.order_no IS NULL, 'MISSING_IN_SCM', NULL),
    IF(p.order_no IS NOT NULL AND s.order_no IS NOT NULL AND p.order_status <> (
      CASE UPPER(COALESCE(s.status, 'CREATED'))
        WHEN 'CREATED' THEN 10
        WHEN 'SUBMITTED' THEN 20
        WHEN 'APPROVED' THEN 30
        WHEN 'CONFIRMED' THEN 40
        WHEN 'PARTIALLY_RECEIVED' THEN 50
        WHEN 'PARTIAL_RECEIVED' THEN 50
        WHEN 'QC_HOLD' THEN 58
        WHEN 'HOLD' THEN 58
        WHEN 'COMPLETED' THEN 60
        WHEN 'RECEIVED' THEN 60
        WHEN 'CLOSED' THEN 70
        WHEN 'CANCELLED' THEN 90
        ELSE 10
      END
    ), 'STATUS_MISMATCH', NULL),
    IF(ABS(COALESCE(p.order_amount, 0) - COALESCE(s.total_amount, 0)) > 0.01, 'HEAD_AMOUNT_MISMATCH', NULL),
    IF(ABS(COALESCE(pi.item_amount_sum, 0) - COALESCE(si.item_amount_sum, 0)) > 0.01, 'ITEM_AMOUNT_SUM_MISMATCH', NULL),
    IF(COALESCE(pi.item_count, 0) <> COALESCE(si.item_count, 0), 'ITEM_COUNT_MISMATCH', NULL),
    IF(ABS(COALESCE(pi.received_qty_sum, 0) - COALESCE(si.received_qty_sum, 0)) > 0.0001, 'RECEIVED_QTY_SUM_MISMATCH', NULL)
  ) AS mismatch_reasons
FROM srm_db.srm_purchase_order s
LEFT JOIN scm_purchase_order p ON p.order_no = s.order_no
LEFT JOIN (
  SELECT
    h.order_no AS order_no,
    COUNT(*) AS item_count,
    COALESCE(SUM(i.subtotal), 0) AS item_amount_sum,
    COALESCE(SUM(i.received_quantity), 0) AS received_qty_sum
  FROM srm_db.srm_purchase_order_item i
  JOIN srm_db.srm_purchase_order h ON h.id = i.purchase_order_id
  GROUP BY h.order_no
) si ON si.order_no = s.order_no
LEFT JOIN (
  SELECT
    order_no,
    COUNT(*) AS item_count,
    COALESCE(SUM(amount), 0) AS item_amount_sum,
    COALESCE(SUM(received_quantity), 0) AS received_qty_sum
  FROM scm_purchase_order_item
  GROUP BY order_no
) pi ON pi.order_no = p.order_no
UNION ALL
SELECT
  p.order_no AS order_no,
  NULL AS srm_status,
  NULL AS srm_status_mapped,
  p.order_status AS scm_status,
  NULL AS srm_head_amount,
  p.order_amount AS scm_head_amount,
  0 AS srm_item_amount_sum,
  COALESCE(pi.item_amount_sum, 0) AS scm_item_amount_sum,
  0 AS srm_item_count,
  COALESCE(pi.item_count, 0) AS scm_item_count,
  0 AS srm_received_qty_sum,
  COALESCE(pi.received_qty_sum, 0) AS scm_received_qty_sum,
  'MISSING_IN_SRM' AS mismatch_reasons
FROM scm_purchase_order p
LEFT JOIN srm_db.srm_purchase_order s ON s.order_no = p.order_no
LEFT JOIN (
  SELECT
    order_no,
    COUNT(*) AS item_count,
    COALESCE(SUM(amount), 0) AS item_amount_sum,
    COALESCE(SUM(received_quantity), 0) AS received_qty_sum
  FROM scm_purchase_order_item
  GROUP BY order_no
) pi ON pi.order_no = p.order_no
WHERE s.order_no IS NULL;

CREATE TABLE IF NOT EXISTS `scm_po_reconciliation_diff` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `order_no` varchar(64) NOT NULL,
  `mismatch_reasons` varchar(1024) NOT NULL,
  `detail_json` json DEFAULT NULL,
  `status` varchar(16) NOT NULL DEFAULT 'OPEN',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_scm_po_reconciliation_diff_order_no` (`order_no`),
  KEY `idx_scm_po_reconciliation_diff_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='PO 迁移对账差异队列（SRM vs SCM）';

INSERT INTO scm_po_reconciliation_diff (order_no, mismatch_reasons, detail_json, status, created_time, updated_time)
SELECT
  v.order_no,
  v.mismatch_reasons,
  JSON_OBJECT(
    'srmStatus', v.srm_status,
    'srmStatusMapped', v.srm_status_mapped,
    'scmStatus', v.scm_status,
    'srmHeadAmount', v.srm_head_amount,
    'scmHeadAmount', v.scm_head_amount,
    'srmItemAmountSum', v.srm_item_amount_sum,
    'scmItemAmountSum', v.scm_item_amount_sum,
    'srmItemCount', v.srm_item_count,
    'scmItemCount', v.scm_item_count,
    'srmReceivedQtySum', v.srm_received_qty_sum,
    'scmReceivedQtySum', v.scm_received_qty_sum
  ),
  'OPEN',
  NOW(),
  NOW()
FROM vw_po_reconciliation_srm_vs_scm v
WHERE v.mismatch_reasons IS NOT NULL AND v.mismatch_reasons <> ''
ON DUPLICATE KEY UPDATE
  mismatch_reasons = VALUES(mismatch_reasons),
  detail_json = VALUES(detail_json),
  updated_time = NOW();
