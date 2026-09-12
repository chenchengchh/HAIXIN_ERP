package com.hxcoe.wms.dto.integration;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class PurchaseOrderEventRequest {
    private String eventId;
    private String traceId;
    private String eventType;
    private String eventKey;
    private Integer eventVersion;
    private String producer;
    private LocalDateTime eventTime;
    private String idempotencyKey;
    private String partitionKey;

    private PurchaseOrderPayload purchaseOrder;

    @Data
    public static class PurchaseOrderPayload {
        private String orderNo;
        private Long supplierId;
        private String supplierCode;
        private String supplierName;
        private Integer orderStatus;
        private BigDecimal orderAmount;
        private LocalDateTime expectedDeliveryDate;
        private LocalDateTime actualDeliveryDate;
        private String remark;
        private LocalDateTime updatedTime;
        private List<PurchaseOrderItemPayload> items;
    }

    @Data
    public static class PurchaseOrderItemPayload {
        private String materialCode;
        private String materialName;
        private String materialSpec;
        private String unit;
        private BigDecimal quantity;
        private BigDecimal unitPrice;
        private BigDecimal amount;
        private BigDecimal receivedQuantity;
    }
}

