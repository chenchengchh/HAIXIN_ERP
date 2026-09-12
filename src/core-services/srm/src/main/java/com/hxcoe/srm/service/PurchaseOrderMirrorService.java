package com.hxcoe.srm.service;

import com.hxcoe.srm.dto.integration.PurchaseOrderEventRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PurchaseOrderMirrorService {

    @Autowired
    @Qualifier("srmSyncJdbcTemplate")
    private NamedParameterJdbcTemplate srmSyncJdbcTemplate;

    /**
     * 消费 SCM 推送的 PO 事件并落 SRM 镜像（通过 Inbox 幂等去重）。
     */
    @Transactional(transactionManager = "srmSyncTxManager")
    public void applyScmEvent(PurchaseOrderEventRequest req) {
        if (req == null) {
            return;
        }
        String eventKey = (req.getEventKey() == null || req.getEventKey().isBlank()) ? req.getIdempotencyKey() : req.getEventKey();
        if (eventKey == null || eventKey.isBlank()) {
            return;
        }
        if (req.getPurchaseOrder() == null || req.getPurchaseOrder().getOrderNo() == null || req.getPurchaseOrder().getOrderNo().isBlank()) {
            return;
        }

        PurchaseOrderEventRequest.PurchaseOrderPayload po = req.getPurchaseOrder();
        if (!tryAcquireInbox(eventKey, req)) {
            return;
        }

        upsertPurchaseOrder(po);
    }

    /**
     * 以 Inbox（event_key 唯一）实现消费者幂等：插入成功才继续处理；重复则直接跳过。
     */
    private boolean tryAcquireInbox(String eventKey, PurchaseOrderEventRequest req) {
        Map<String, Object> params = new HashMap<>();
        params.put("eventKey", eventKey);
        params.put("eventId", req.getEventId());
        params.put("traceId", (req.getTraceId() == null || req.getTraceId().isBlank()) ? req.getEventId() : req.getTraceId());
        params.put("producer", req.getProducer());
        params.put("eventVersion", req.getEventVersion() == null ? 1 : req.getEventVersion());
        params.put("partitionKey", req.getPartitionKey());
        params.put("idempotencyKey", (req.getIdempotencyKey() == null || req.getIdempotencyKey().isBlank()) ? eventKey : req.getIdempotencyKey());
        params.put("eventType", req.getEventType() == null || req.getEventType().isBlank() ? "UNKNOWN" : req.getEventType());
        try {
            srmSyncJdbcTemplate.update(
                    "INSERT INTO srm_integration_inbox (event_key, event_id, trace_id, producer, event_version, partition_key, idempotency_key, event_type, received_time) "
                            + "VALUES (:eventKey, :eventId, :traceId, :producer, :eventVersion, :partitionKey, :idempotencyKey, :eventType, NOW())",
                    params
            );
            return true;
        } catch (DuplicateKeyException ignored) {
            return false;
        }
    }

    /**
     * 将 SCM 的 PO payload 写入 SRM 镜像表（头+行），以 order_no 为幂等业务键。
     */
    private void upsertPurchaseOrder(PurchaseOrderEventRequest.PurchaseOrderPayload po) {
        String orderNo = po.getOrderNo();
        Long orderId = findPurchaseOrderId(orderNo);

        Map<String, Object> headerParams = new HashMap<>();
        headerParams.put("orderNo", orderNo);
        headerParams.put("supplierId", po.getSupplierId());
        headerParams.put("supplierCode", po.getSupplierCode());
        headerParams.put("supplierName", po.getSupplierName());
        headerParams.put("totalAmount", po.getOrderAmount() == null ? BigDecimal.ZERO : po.getOrderAmount());
        headerParams.put("status", mapScmStatus(po.getOrderStatus()));
        headerParams.put("orderDate", po.getUpdatedTime() == null ? LocalDateTime.now() : po.getUpdatedTime());
        headerParams.put("expectedDeliveryDate", po.getExpectedDeliveryDate());
        headerParams.put("remarks", po.getRemark());

        if (orderId == null) {
            srmSyncJdbcTemplate.update(
                    "INSERT INTO srm_purchase_order (order_no, supplier_id, supplier_code, supplier_name, total_amount, currency, status, order_date, expected_delivery_date, remarks) "
                            + "VALUES (:orderNo, :supplierId, :supplierCode, :supplierName, :totalAmount, 'CNY', :status, :orderDate, :expectedDeliveryDate, :remarks)",
                    headerParams
            );
            orderId = findPurchaseOrderId(orderNo);
        } else {
            srmSyncJdbcTemplate.update(
                    "UPDATE srm_purchase_order SET supplier_id=:supplierId, supplier_code=:supplierCode, supplier_name=:supplierName, total_amount=:totalAmount, status=:status, "
                            + "order_date=:orderDate, expected_delivery_date=:expectedDeliveryDate, remarks=:remarks WHERE order_no=:orderNo",
                    headerParams
            );
        }

        if (orderId != null) {
            replaceItems(orderId, po.getItems());
        }
    }

    /**
     * 根据业务唯一键 order_no 查找镜像主键 id。
     */
    private Long findPurchaseOrderId(String orderNo) {
        List<Long> ids = srmSyncJdbcTemplate.query(
                "SELECT id FROM srm_purchase_order WHERE order_no=:orderNo LIMIT 1",
                new MapSqlParameterSource("orderNo", orderNo),
                (rs, rowNum) -> rs.getLong("id")
        );
        return ids.isEmpty() ? null : ids.get(0);
    }

    /**
     * 以“先删后插”方式替换行项目，避免迁移期 partial update 形成脏数据。
     */
    private void replaceItems(Long purchaseOrderId, List<PurchaseOrderEventRequest.PurchaseOrderItemPayload> items) {
        srmSyncJdbcTemplate.update(
                "DELETE FROM srm_purchase_order_item WHERE purchase_order_id=:purchaseOrderId",
                new MapSqlParameterSource("purchaseOrderId", purchaseOrderId)
        );

        if (items == null || items.isEmpty()) {
            return;
        }

        List<MapSqlParameterSource> batch = new ArrayList<>();
        for (PurchaseOrderEventRequest.PurchaseOrderItemPayload it : items) {
            if (it == null || it.getMaterialCode() == null || it.getMaterialCode().isBlank()) {
                continue;
            }
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("purchaseOrderId", purchaseOrderId);
            params.addValue("materialCode", it.getMaterialCode());
            params.addValue("materialName", it.getMaterialName());
            params.addValue("quantity", it.getQuantity());
            params.addValue("unit", it.getUnit());
            params.addValue("unitPrice", it.getUnitPrice());
            params.addValue("subtotal", it.getAmount());
            params.addValue("receivedQuantity", it.getReceivedQuantity());
            batch.add(params);
        }

        if (batch.isEmpty()) {
            return;
        }

        srmSyncJdbcTemplate.batchUpdate(
                "INSERT INTO srm_purchase_order_item (purchase_order_id, material_code, material_name, quantity, unit, unit_price, subtotal, received_quantity) "
                        + "VALUES (:purchaseOrderId, :materialCode, :materialName, :quantity, :unit, :unitPrice, :subtotal, :receivedQuantity)",
                batch.toArray(new MapSqlParameterSource[0])
        );
    }

    private String mapScmStatus(Integer status) {
        if (status == null) return "CREATED";
        if (status == 10) return "CREATED";
        if (status == 20) return "SUBMITTED";
        if (status == 30) return "APPROVED";
        if (status == 40) return "CONFIRMED";
        if (status == 50) return "PARTIAL_RECEIVED";
        if (status == 58) return "QC_HOLD";
        if (status == 60) return "COMPLETED";
        if (status == 70) return "CLOSED";
        if (status == 90) return "CANCELLED";
        return String.valueOf(status);
    }
}
