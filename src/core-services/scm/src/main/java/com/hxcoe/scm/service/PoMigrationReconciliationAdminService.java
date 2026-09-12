package com.hxcoe.scm.service;

import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class PoMigrationReconciliationAdminService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Transactional
    public Map<String, Object> migrateFromSrmToScm() {
        int insertedOrders = jdbcTemplate.update("""
                INSERT INTO scm_purchase_order (
                  order_no, supplier_id, supplier_code, supplier_name,
                  purchase_type, order_status, order_amount,
                  expected_delivery_date, actual_delivery_date,
                  created_by, created_time, updated_by, updated_time, remark
                )
                SELECT
                  s.order_no,
                  COALESCE(s.supplier_id, 0),
                  COALESCE(s.supplier_code, ''),
                  COALESCE(s.supplier_name, ''),
                  0,
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
                  END,
                  COALESCE(s.total_amount, 0),
                  s.expected_delivery_date,
                  NULL,
                  'system-migration',
                  COALESCE(s.created_time, NOW()),
                  'system-migration',
                  COALESCE(s.updated_time, NOW()),
                  CONCAT('migrated_from_srm;', COALESCE(s.remarks, ''))
                FROM srm_db.srm_purchase_order s
                LEFT JOIN scm_purchase_order p ON p.order_no = s.order_no
                WHERE p.id IS NULL
                """);

        int deletedItems = jdbcTemplate.update("""
                DELETE i
                FROM scm_purchase_order_item i
                JOIN scm_purchase_order p ON p.id = i.order_id
                WHERE p.created_by = 'system-migration' AND p.remark LIKE 'migrated_from_srm;%'
                """);

        int insertedItems = jdbcTemplate.update("""
                INSERT INTO scm_purchase_order_item (
                  order_id, order_no,
                  material_code, material_name, material_spec, unit,
                  quantity, unit_price, amount, received_quantity,
                  created_by, created_time, updated_by, updated_time, remark
                )
                SELECT
                  p.id,
                  p.order_no,
                  si.material_code,
                  COALESCE(si.material_name, ''),
                  NULL,
                  si.unit,
                  COALESCE(si.quantity, 0),
                  COALESCE(si.unit_price, 0),
                  COALESCE(si.subtotal, 0),
                  COALESCE(si.received_quantity, 0),
                  'system-migration',
                  NOW(),
                  'system-migration',
                  NOW(),
                  'migrated_from_srm'
                FROM srm_db.srm_purchase_order_item si
                JOIN srm_db.srm_purchase_order s ON s.id = si.purchase_order_id
                JOIN scm_purchase_order p ON p.order_no = s.order_no
                WHERE p.created_by = 'system-migration' AND p.remark LIKE 'migrated_from_srm;%'
                """);

        Map<String, Object> res = new HashMap<>();
        res.put("insertedOrders", insertedOrders);
        res.put("deletedItems", deletedItems);
        res.put("insertedItems", insertedItems);
        return res;
    }

    @Transactional
    public Map<String, Object> refreshSrmVsScmDiffs() {
        ensureReconciliationViewAndTable();

        int upserted = jdbcTemplate.update("""
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
                  status = IF(status = 'IGNORED', 'IGNORED', 'OPEN'),
                  updated_time = NOW()
                """);

        int autoResolved = jdbcTemplate.update("""
                UPDATE scm_po_reconciliation_diff d
                LEFT JOIN (
                  SELECT v.order_no
                  FROM vw_po_reconciliation_srm_vs_scm v
                  WHERE v.mismatch_reasons IS NOT NULL AND v.mismatch_reasons <> ''
                ) v ON v.order_no = d.order_no
                SET d.status = 'RESOLVED', d.updated_time = NOW()
                WHERE v.order_no IS NULL AND d.status = 'OPEN'
                """);

        Map<String, Object> res = new HashMap<>();
        res.put("upserted", upserted);
        res.put("autoResolved", autoResolved);
        return res;
    }

    private void ensureReconciliationViewAndTable() {
        jdbcTemplate.execute("""
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
                WHERE s.order_no IS NULL
                """);

        jdbcTemplate.execute("""
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
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='PO 迁移对账差异队列（SRM vs SCM）'
                """);
    }
}

