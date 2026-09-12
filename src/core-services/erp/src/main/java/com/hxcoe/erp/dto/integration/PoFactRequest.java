package com.hxcoe.erp.dto.integration;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * SCM→ERP 采购订单事实事件请求 DTO。
 * <p>跨服务事件契约固定使用小驼峰命名，通过 @JsonNaming 显式声明，
 * 避免受本服务全局 Jackson SNAKE_CASE 策略影响；@JsonAlias 保留向前兼容。</p>
 */
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public class PoFactRequest {
    @JsonAlias("eventId")
    private String eventId;
    @JsonAlias("eventType")
    private String eventType;
    @JsonAlias("eventKey")
    private String eventKey;
    @JsonAlias("idempotencyKey")
    private String idempotencyKey;
    @JsonAlias("partitionKey")
    private String partitionKey;
    @JsonAlias("eventVersion")
    private Integer eventVersion;
    @JsonAlias("producer")
    private String producer;
    @JsonAlias("traceId")
    private String traceId;
    @JsonAlias("eventTime")
    private LocalDateTime eventTime;
    @JsonAlias("fact")
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

    @JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
    public static class PoFactPayload {
        @JsonAlias("factType")
        private String factType;
        @JsonAlias("poNo")
        private String poNo;
        @JsonAlias("refNo")
        private String refNo;
        @JsonAlias("result")
        private String result;
        @JsonAlias("scmOrderStatus")
        private Integer scmOrderStatus;
        @JsonAlias("receivedQtySum")
        private BigDecimal receivedQtySum;
        @JsonAlias("lines")
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

    @JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
    public static class ReceiptLine {
        @JsonAlias("materialCode")
        private String materialCode;
        @JsonAlias("qty")
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
