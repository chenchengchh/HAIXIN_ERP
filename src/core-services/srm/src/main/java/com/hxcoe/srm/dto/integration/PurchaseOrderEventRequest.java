package com.hxcoe.srm.dto.integration;

import java.time.LocalDateTime;
import java.util.List;

public class PurchaseOrderEventRequest {
    private String eventId;
    private String eventType;
    private String eventKey;
    private String idempotencyKey;
    private String partitionKey;
    private Integer eventVersion;
    private String producer;
    private String traceId;
    private LocalDateTime eventTime;
    private PurchaseOrderPayload purchaseOrder;

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getEventKey() {
        return eventKey;
    }

    public void setEventKey(String eventKey) {
        this.eventKey = eventKey;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public String getPartitionKey() {
        return partitionKey;
    }

    public void setPartitionKey(String partitionKey) {
        this.partitionKey = partitionKey;
    }

    public Integer getEventVersion() {
        return eventVersion;
    }

    public void setEventVersion(Integer eventVersion) {
        this.eventVersion = eventVersion;
    }

    public String getProducer() {
        return producer;
    }

    public void setProducer(String producer) {
        this.producer = producer;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public LocalDateTime getEventTime() {
        return eventTime;
    }

    public void setEventTime(LocalDateTime eventTime) {
        this.eventTime = eventTime;
    }

    public PurchaseOrderPayload getPurchaseOrder() {
        return purchaseOrder;
    }

    public void setPurchaseOrder(PurchaseOrderPayload purchaseOrder) {
        this.purchaseOrder = purchaseOrder;
    }

    public static class PurchaseOrderPayload {
        private String orderNo;
        private Long supplierId;
        private String supplierCode;
        private String supplierName;
        private Integer orderStatus;
        private java.math.BigDecimal orderAmount;
        private LocalDateTime expectedDeliveryDate;
        private LocalDateTime actualDeliveryDate;
        private String remark;
        private LocalDateTime updatedTime;
        private List<PurchaseOrderItemPayload> items;

        public String getOrderNo() {
            return orderNo;
        }

        public void setOrderNo(String orderNo) {
            this.orderNo = orderNo;
        }

        public Long getSupplierId() {
            return supplierId;
        }

        public void setSupplierId(Long supplierId) {
            this.supplierId = supplierId;
        }

        public String getSupplierCode() {
            return supplierCode;
        }

        public void setSupplierCode(String supplierCode) {
            this.supplierCode = supplierCode;
        }

        public String getSupplierName() {
            return supplierName;
        }

        public void setSupplierName(String supplierName) {
            this.supplierName = supplierName;
        }

        public Integer getOrderStatus() {
            return orderStatus;
        }

        public void setOrderStatus(Integer orderStatus) {
            this.orderStatus = orderStatus;
        }

        public java.math.BigDecimal getOrderAmount() {
            return orderAmount;
        }

        public void setOrderAmount(java.math.BigDecimal orderAmount) {
            this.orderAmount = orderAmount;
        }

        public LocalDateTime getExpectedDeliveryDate() {
            return expectedDeliveryDate;
        }

        public void setExpectedDeliveryDate(LocalDateTime expectedDeliveryDate) {
            this.expectedDeliveryDate = expectedDeliveryDate;
        }

        public LocalDateTime getActualDeliveryDate() {
            return actualDeliveryDate;
        }

        public void setActualDeliveryDate(LocalDateTime actualDeliveryDate) {
            this.actualDeliveryDate = actualDeliveryDate;
        }

        public String getRemark() {
            return remark;
        }

        public void setRemark(String remark) {
            this.remark = remark;
        }

        public LocalDateTime getUpdatedTime() {
            return updatedTime;
        }

        public void setUpdatedTime(LocalDateTime updatedTime) {
            this.updatedTime = updatedTime;
        }

        public List<PurchaseOrderItemPayload> getItems() {
            return items;
        }

        public void setItems(List<PurchaseOrderItemPayload> items) {
            this.items = items;
        }
    }

    public static class PurchaseOrderItemPayload {
        private String materialCode;
        private String materialName;
        private String materialSpec;
        private String unit;
        private java.math.BigDecimal quantity;
        private java.math.BigDecimal unitPrice;
        private java.math.BigDecimal amount;
        private java.math.BigDecimal receivedQuantity;

        public String getMaterialCode() {
            return materialCode;
        }

        public void setMaterialCode(String materialCode) {
            this.materialCode = materialCode;
        }

        public String getMaterialName() {
            return materialName;
        }

        public void setMaterialName(String materialName) {
            this.materialName = materialName;
        }

        public String getMaterialSpec() {
            return materialSpec;
        }

        public void setMaterialSpec(String materialSpec) {
            this.materialSpec = materialSpec;
        }

        public String getUnit() {
            return unit;
        }

        public void setUnit(String unit) {
            this.unit = unit;
        }

        public java.math.BigDecimal getQuantity() {
            return quantity;
        }

        public void setQuantity(java.math.BigDecimal quantity) {
            this.quantity = quantity;
        }

        public java.math.BigDecimal getUnitPrice() {
            return unitPrice;
        }

        public void setUnitPrice(java.math.BigDecimal unitPrice) {
            this.unitPrice = unitPrice;
        }

        public java.math.BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(java.math.BigDecimal amount) {
            this.amount = amount;
        }

        public java.math.BigDecimal getReceivedQuantity() {
            return receivedQuantity;
        }

        public void setReceivedQuantity(java.math.BigDecimal receivedQuantity) {
            this.receivedQuantity = receivedQuantity;
        }
    }
}
