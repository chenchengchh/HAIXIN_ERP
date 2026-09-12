package com.hxcoe.scm.client.dto.erp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class PoFactRequest {
    private String eventId;
    private String eventType;
    private String eventKey;
    private String idempotencyKey;
    private String partitionKey;
    private Integer eventVersion;
    private String producer;
    private String traceId;
    private LocalDateTime eventTime;
    private PoFactPayload fact;

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

    public PoFactPayload getFact() {
        return fact;
    }

    public void setFact(PoFactPayload fact) {
        this.fact = fact;
    }

    public static class PoFactPayload {
        private String factType;
        private String poNo;
        private String refNo;
        private String result;
        private Integer scmOrderStatus;
        private BigDecimal receivedQtySum;
        private List<ReceiptLine> lines;

        public String getFactType() {
            return factType;
        }

        public void setFactType(String factType) {
            this.factType = factType;
        }

        public String getPoNo() {
            return poNo;
        }

        public void setPoNo(String poNo) {
            this.poNo = poNo;
        }

        public String getRefNo() {
            return refNo;
        }

        public void setRefNo(String refNo) {
            this.refNo = refNo;
        }

        public String getResult() {
            return result;
        }

        public void setResult(String result) {
            this.result = result;
        }

        public Integer getScmOrderStatus() {
            return scmOrderStatus;
        }

        public void setScmOrderStatus(Integer scmOrderStatus) {
            this.scmOrderStatus = scmOrderStatus;
        }

        public BigDecimal getReceivedQtySum() {
            return receivedQtySum;
        }

        public void setReceivedQtySum(BigDecimal receivedQtySum) {
            this.receivedQtySum = receivedQtySum;
        }

        public List<ReceiptLine> getLines() {
            return lines;
        }

        public void setLines(List<ReceiptLine> lines) {
            this.lines = lines;
        }
    }

    public static class ReceiptLine {
        private String materialCode;
        private BigDecimal qty;

        public String getMaterialCode() {
            return materialCode;
        }

        public void setMaterialCode(String materialCode) {
            this.materialCode = materialCode;
        }

        public BigDecimal getQty() {
            return qty;
        }

        public void setQty(BigDecimal qty) {
            this.qty = qty;
        }
    }
}

