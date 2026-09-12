package com.hxcoe.scm.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PurchaseReconciliationQueryService {

    @PersistenceContext
    private EntityManager entityManager;

    public long countMismatchOrders(String orderNo, String supplierCode, String supplierName, String materialCode, LocalDateTime start, LocalDateTime end) {
        String where = buildWhere(orderNo, supplierCode, supplierName, materialCode, start, end);
        String sql = """
                SELECT COUNT(*)
                FROM (
                  SELECT po.order_no
                  FROM scm_purchase_order po
                  LEFT JOIN scm_purchase_order_item i ON i.order_no = po.order_no
                """ + where + """
                  GROUP BY po.order_no
                  HAVING ABS(COALESCE(MAX(po.order_amount), 0) - COALESCE(SUM(i.amount), 0)) > 0.01
                     OR MAX(CASE WHEN COALESCE(i.received_quantity, 0) > COALESCE(i.quantity, 0) THEN 1 ELSE 0 END) = 1
                ) t
                """;
        var q = entityManager.createNativeQuery(sql);
        bindParams(q, orderNo, supplierCode, supplierName, materialCode, start, end);
        Object v = q.getSingleResult();
        if (v instanceof Number n) return n.longValue();
        return 0L;
    }

    public List<String> findMismatchOrderNos(int page, int size, String orderNo, String supplierCode, String supplierName, String materialCode, LocalDateTime start, LocalDateTime end) {
        int p = Math.max(1, page);
        int s = Math.max(1, size);
        int offset = (p - 1) * s;
        String where = buildWhere(orderNo, supplierCode, supplierName, materialCode, start, end);
        String sql = """
                SELECT t.order_no
                FROM (
                  SELECT po.order_no AS order_no,
                         MAX(po.updated_time) AS ut,
                         MAX(po.id) AS pid
                  FROM scm_purchase_order po
                  LEFT JOIN scm_purchase_order_item i ON i.order_no = po.order_no
                """ + where + """
                  GROUP BY po.order_no
                  HAVING ABS(COALESCE(MAX(po.order_amount), 0) - COALESCE(SUM(i.amount), 0)) > 0.01
                     OR MAX(CASE WHEN COALESCE(i.received_quantity, 0) > COALESCE(i.quantity, 0) THEN 1 ELSE 0 END) = 1
                ) t
                ORDER BY t.ut DESC, t.pid DESC
                LIMIT :limit OFFSET :offset
                """;
        @SuppressWarnings("unchecked")
        var q = entityManager.createNativeQuery(sql)
                .setParameter("limit", s)
                .setParameter("offset", offset);
        bindParams(q, orderNo, supplierCode, supplierName, materialCode, start, end);
        List<Object> rows = q.getResultList();
        return rows.stream().map(String::valueOf).toList();
    }

    public Map<String, Object> getReasonSummary(String orderNo, String supplierCode, String supplierName, String materialCode, LocalDateTime start, LocalDateTime end) {
        String where = buildWhere(orderNo, supplierCode, supplierName, materialCode, start, end);
        String orderAgg = """
                SELECT po.order_no AS order_no,
                       MAX(po.order_status) AS order_status,
                       COALESCE(MAX(po.order_amount), 0) AS head_amount,
                       COALESCE(SUM(i.amount), 0) AS item_amount_sum,
                       MAX(CASE WHEN COALESCE(i.received_quantity, 0) > COALESCE(i.quantity, 0) THEN 1 ELSE 0 END) AS over_received
                FROM scm_purchase_order po
                LEFT JOIN scm_purchase_order_item i ON i.order_no = po.order_no
                """ + where + """
                GROUP BY po.order_no
                """;
        String sql = """
                SELECT
                  COUNT(*) AS total_orders,
                  SUM(CASE WHEN t.order_status = 58 THEN 1 ELSE 0 END) AS qc_hold_orders,
                  SUM(CASE WHEN ABS(t.head_amount - t.item_amount_sum) > 0.01 THEN 1 ELSE 0 END) AS amount_mismatch_orders,
                  SUM(CASE WHEN t.over_received = 1 THEN 1 ELSE 0 END) AS over_received_orders,
                  SUM(CASE WHEN (t.order_status = 58 OR ABS(t.head_amount - t.item_amount_sum) > 0.01 OR t.over_received = 1) THEN 1 ELSE 0 END) AS mismatch_orders
                FROM (
                """ + orderAgg + """
                ) t
                """;
        var q = entityManager.createNativeQuery(sql);
        bindParams(q, orderNo, supplierCode, supplierName, materialCode, start, end);
        Object row = q.getSingleResult();
        Object[] arr = row instanceof Object[] a ? a : new Object[]{row};
        Map<String, Object> data = new HashMap<>();
        data.put("totalOrders", toLong(arr, 0));
        data.put("qcHoldOrders", toLong(arr, 1));
        data.put("amountMismatchOrders", toLong(arr, 2));
        data.put("overReceivedOrders", toLong(arr, 3));
        data.put("mismatchOrders", toLong(arr, 4));
        return data;
    }

    public long countSupplierAgg(String orderNo, String supplierCode, String supplierName, String materialCode, LocalDateTime start, LocalDateTime end) {
        String where = buildWhere(orderNo, supplierCode, supplierName, materialCode, start, end);
        String sql = """
                SELECT COUNT(*)
                FROM (
                  SELECT po.supplier_code
                  FROM scm_purchase_order po
                  LEFT JOIN scm_purchase_order_item i ON i.order_no = po.order_no
                """ + where + """
                  GROUP BY po.supplier_code
                ) t
                """;
        var q = entityManager.createNativeQuery(sql);
        bindParams(q, orderNo, supplierCode, supplierName, materialCode, start, end);
        Object v = q.getSingleResult();
        if (v instanceof Number n) return n.longValue();
        return 0L;
    }

    public List<Map<String, Object>> findSupplierAgg(int page, int size, String orderNo, String supplierCode, String supplierName, String materialCode, LocalDateTime start, LocalDateTime end) {
        int p = Math.max(1, page);
        int s = Math.max(1, size);
        int offset = (p - 1) * s;
        String where = buildWhere(orderNo, supplierCode, supplierName, materialCode, start, end);
        String orderAgg = """
                SELECT po.order_no AS order_no,
                       po.supplier_code AS supplier_code,
                       MAX(po.supplier_name) AS supplier_name,
                       MAX(po.order_status) AS order_status,
                       COALESCE(MAX(po.order_amount), 0) AS head_amount,
                       COALESCE(SUM(i.amount), 0) AS item_amount_sum,
                       MAX(CASE WHEN COALESCE(i.received_quantity, 0) > COALESCE(i.quantity, 0) THEN 1 ELSE 0 END) AS over_received
                FROM scm_purchase_order po
                LEFT JOIN scm_purchase_order_item i ON i.order_no = po.order_no
                """ + where + """
                GROUP BY po.order_no, po.supplier_code
                """;
        String sql = """
                SELECT
                  t.supplier_code,
                  MAX(t.supplier_name) AS supplier_name,
                  COUNT(*) AS total_orders,
                  SUM(CASE WHEN t.order_status = 58 THEN 1 ELSE 0 END) AS qc_hold_orders,
                  SUM(CASE WHEN ABS(t.head_amount - t.item_amount_sum) > 0.01 THEN 1 ELSE 0 END) AS amount_mismatch_orders,
                  SUM(CASE WHEN t.over_received = 1 THEN 1 ELSE 0 END) AS over_received_orders,
                  SUM(CASE WHEN (t.order_status = 58 OR ABS(t.head_amount - t.item_amount_sum) > 0.01 OR t.over_received = 1) THEN 1 ELSE 0 END) AS mismatch_orders
                FROM (
                """ + orderAgg + """
                ) t
                GROUP BY t.supplier_code
                ORDER BY mismatch_orders DESC, total_orders DESC
                LIMIT :limit OFFSET :offset
                """;
        @SuppressWarnings("unchecked")
        var q = entityManager.createNativeQuery(sql)
                .setParameter("limit", s)
                .setParameter("offset", offset);
        bindParams(q, orderNo, supplierCode, supplierName, materialCode, start, end);
        List<Object[]> rows = q.getResultList();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] r : rows) {
            Map<String, Object> m = new HashMap<>();
            m.put("supplierCode", r[0] == null ? "" : String.valueOf(r[0]));
            m.put("supplierName", r[1] == null ? "" : String.valueOf(r[1]));
            m.put("totalOrders", toLong(r, 2));
            m.put("qcHoldOrders", toLong(r, 3));
            m.put("amountMismatchOrders", toLong(r, 4));
            m.put("overReceivedOrders", toLong(r, 5));
            m.put("mismatchOrders", toLong(r, 6));
            result.add(m);
        }
        return result;
    }

    public long countMaterialAgg(String orderNo, String supplierCode, String supplierName, String materialCode, LocalDateTime start, LocalDateTime end) {
        String where = buildWhereForMaterial(orderNo, supplierCode, supplierName, materialCode, start, end);
        String sql = """
                SELECT COUNT(*)
                FROM (
                  SELECT i.material_code
                  FROM scm_purchase_order_item i
                  JOIN scm_purchase_order po ON po.order_no = i.order_no
                """ + where + """
                  GROUP BY i.material_code
                ) t
                """;
        var q = entityManager.createNativeQuery(sql);
        bindParamsForMaterial(q, orderNo, supplierCode, supplierName, materialCode, start, end);
        Object v = q.getSingleResult();
        if (v instanceof Number n) return n.longValue();
        return 0L;
    }

    public List<Map<String, Object>> findMaterialAgg(int page, int size, String orderNo, String supplierCode, String supplierName, String materialCode, LocalDateTime start, LocalDateTime end) {
        int p = Math.max(1, page);
        int s = Math.max(1, size);
        int offset = (p - 1) * s;
        String where = buildWhereForMaterial(orderNo, supplierCode, supplierName, materialCode, start, end);
        String sql = """
                SELECT
                  i.material_code,
                  MAX(i.material_name) AS material_name,
                  COUNT(DISTINCT i.order_no) AS order_count,
                  COALESCE(SUM(i.quantity), 0) AS ordered_qty_sum,
                  COALESCE(SUM(i.received_quantity), 0) AS received_qty_sum,
                  SUM(CASE WHEN COALESCE(i.received_quantity, 0) > COALESCE(i.quantity, 0) THEN 1 ELSE 0 END) AS over_received_lines
                FROM scm_purchase_order_item i
                JOIN scm_purchase_order po ON po.order_no = i.order_no
                """ + where + """
                GROUP BY i.material_code
                ORDER BY over_received_lines DESC, received_qty_sum DESC
                LIMIT :limit OFFSET :offset
                """;
        @SuppressWarnings("unchecked")
        var q = entityManager.createNativeQuery(sql)
                .setParameter("limit", s)
                .setParameter("offset", offset);
        bindParamsForMaterial(q, orderNo, supplierCode, supplierName, materialCode, start, end);
        List<Object[]> rows = q.getResultList();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] r : rows) {
            Map<String, Object> m = new HashMap<>();
            m.put("materialCode", r[0] == null ? "" : String.valueOf(r[0]));
            m.put("materialName", r[1] == null ? "" : String.valueOf(r[1]));
            m.put("orderCount", toLong(r, 2));
            m.put("orderedQtySum", toBigDecimal(r[3]));
            m.put("receivedQtySum", toBigDecimal(r[4]));
            m.put("overReceivedLines", toLong(r, 5));
            result.add(m);
        }
        return result;
    }

    private String buildWhere(String orderNo, String supplierCode, String supplierName, String materialCode, LocalDateTime start, LocalDateTime end) {
        StringBuilder sb = new StringBuilder();
        sb.append(" WHERE 1=1 ");
        if (orderNo != null && !orderNo.isBlank()) {
            sb.append(" AND po.order_no LIKE :orderNo ");
        }
        if (supplierCode != null && !supplierCode.isBlank()) {
            sb.append(" AND po.supplier_code LIKE :supplierCode ");
        }
        if (supplierName != null && !supplierName.isBlank()) {
            sb.append(" AND po.supplier_name LIKE :supplierName ");
        }
        if (start != null) {
            sb.append(" AND po.created_time >= :startTime ");
        }
        if (end != null) {
            sb.append(" AND po.created_time <= :endTime ");
        }
        if (materialCode != null && !materialCode.isBlank()) {
            sb.append(" AND EXISTS (SELECT 1 FROM scm_purchase_order_item x WHERE x.order_no = po.order_no AND x.material_code = :materialCode) ");
        }
        return sb.toString();
    }

    private String buildWhereForMaterial(String orderNo, String supplierCode, String supplierName, String materialCode, LocalDateTime start, LocalDateTime end) {
        StringBuilder sb = new StringBuilder();
        sb.append(" WHERE 1=1 ");
        if (orderNo != null && !orderNo.isBlank()) {
            sb.append(" AND po.order_no LIKE :orderNo ");
        }
        if (supplierCode != null && !supplierCode.isBlank()) {
            sb.append(" AND po.supplier_code LIKE :supplierCode ");
        }
        if (supplierName != null && !supplierName.isBlank()) {
            sb.append(" AND po.supplier_name LIKE :supplierName ");
        }
        if (start != null) {
            sb.append(" AND po.created_time >= :startTime ");
        }
        if (end != null) {
            sb.append(" AND po.created_time <= :endTime ");
        }
        if (materialCode != null && !materialCode.isBlank()) {
            sb.append(" AND i.material_code = :materialCode ");
        }
        return sb.toString();
    }

    private void bindParams(jakarta.persistence.Query q, String orderNo, String supplierCode, String supplierName, String materialCode, LocalDateTime start, LocalDateTime end) {
        if (orderNo != null && !orderNo.isBlank()) {
            q.setParameter("orderNo", "%" + orderNo.trim() + "%");
        }
        if (supplierCode != null && !supplierCode.isBlank()) {
            q.setParameter("supplierCode", "%" + supplierCode.trim() + "%");
        }
        if (supplierName != null && !supplierName.isBlank()) {
            q.setParameter("supplierName", "%" + supplierName.trim() + "%");
        }
        if (materialCode != null && !materialCode.isBlank()) {
            q.setParameter("materialCode", materialCode.trim());
        }
        if (start != null) {
            q.setParameter("startTime", start);
        }
        if (end != null) {
            q.setParameter("endTime", end);
        }
    }

    private void bindParamsForMaterial(jakarta.persistence.Query q, String orderNo, String supplierCode, String supplierName, String materialCode, LocalDateTime start, LocalDateTime end) {
        bindParams(q, orderNo, supplierCode, supplierName, materialCode, start, end);
    }

    private long toLong(Object[] arr, int idx) {
        if (arr == null || idx >= arr.length) return 0L;
        Object v = arr[idx];
        if (v instanceof Number n) return n.longValue();
        if (v == null) return 0L;
        try {
            return Long.parseLong(String.valueOf(v));
        } catch (Exception e) {
            return 0L;
        }
    }

    private BigDecimal toBigDecimal(Object v) {
        if (v == null) return BigDecimal.ZERO;
        if (v instanceof BigDecimal b) return b;
        if (v instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
        try {
            return new BigDecimal(String.valueOf(v));
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }
}
