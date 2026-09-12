package com.hxcoe.wms.client.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class ErpOutboundShippedRequest {
    private String eventId;
    private String traceId;
    private String eventType;
    private String eventKey;
    private Integer eventVersion;
    private String producer;
    // ERP侧无统一JacksonConfig，LocalDateTime需按默认ISO格式序列化
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime eventTime;
    private String idempotencyKey;
    private String partitionKey;
    private OutboundOrderPayload outboundOrder;

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
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

    public LocalDateTime getEventTime() {
        return eventTime;
    }

    public void setEventTime(LocalDateTime eventTime) {
        this.eventTime = eventTime;
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

    public OutboundOrderPayload getOutboundOrder() {
        return outboundOrder;
    }

    public void setOutboundOrder(OutboundOrderPayload outboundOrder) {
        this.outboundOrder = outboundOrder;
    }

    public static class OutboundOrderPayload {
        private Long outboundOrderId;
        private String orderNo;
        private String orderType;
        private String sourceNo;
        private String customerName;
        private String address;
        private String status;
        // ERP侧无统一JacksonConfig，LocalDateTime需按默认ISO格式序列化
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime shippedTime;
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime updatedTime;
        private List<OutboundOrderItemPayload> items;

        public Long getOutboundOrderId() {
            return outboundOrderId;
        }

        public void setOutboundOrderId(Long outboundOrderId) {
            this.outboundOrderId = outboundOrderId;
        }

        public String getOrderNo() {
            return orderNo;
        }

        public void setOrderNo(String orderNo) {
            this.orderNo = orderNo;
        }

        public String getOrderType() {
            return orderType;
        }

        public void setOrderType(String orderType) {
            this.orderType = orderType;
        }

        public String getSourceNo() {
            return sourceNo;
        }

        public void setSourceNo(String sourceNo) {
            this.sourceNo = sourceNo;
        }

        public String getCustomerName() {
            return customerName;
        }

        public void setCustomerName(String customerName) {
            this.customerName = customerName;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public LocalDateTime getShippedTime() {
            return shippedTime;
        }

        public void setShippedTime(LocalDateTime shippedTime) {
            this.shippedTime = shippedTime;
        }

        public LocalDateTime getUpdatedTime() {
            return updatedTime;
        }

        public void setUpdatedTime(LocalDateTime updatedTime) {
            this.updatedTime = updatedTime;
        }

        public List<OutboundOrderItemPayload> getItems() {
            return items;
        }

        public void setItems(List<OutboundOrderItemPayload> items) {
            this.items = items;
        }
    }

    public static class OutboundOrderItemPayload {
        private String materialCode;
        private String materialName;
        private BigDecimal quantity;
        private String unit;
        private String locationCode;
        private String batchNo;

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

        public BigDecimal getQuantity() {
            return quantity;
        }

        public void setQuantity(BigDecimal quantity) {
            this.quantity = quantity;
        }

        public String getUnit() {
            return unit;
        }

        public void setUnit(String unit) {
            this.unit = unit;
        }

        public String getLocationCode() {
            return locationCode;
        }

        public void setLocationCode(String locationCode) {
            this.locationCode = locationCode;
        }

        public String getBatchNo() {
            return batchNo;
        }

        public void setBatchNo(String batchNo) {
            this.batchNo = batchNo;
        }
    }
}
