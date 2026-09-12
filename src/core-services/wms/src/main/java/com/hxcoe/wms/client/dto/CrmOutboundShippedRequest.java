package com.hxcoe.wms.client.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public class CrmOutboundShippedRequest {
    private String eventId;
    private String traceId;
    private String eventType;
    private String eventKey;
    private Integer eventVersion;
    private String producer;
    private LocalDateTime eventTime;
    private String idempotencyKey;
    private String partitionKey;
    private OutboundOrderPayload outboundOrder;

    @Data
    @JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
    public static class OutboundOrderPayload {
        private Long outboundOrderId;
        private String orderNo;
        private String orderType;
        private String sourceNo;
        private String customerName;
        private String address;
        private String status;
        private LocalDateTime shippedTime;
        private LocalDateTime updatedTime;
        private List<OutboundOrderItemPayload> items;
    }

    @Data
    @JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
    public static class OutboundOrderItemPayload {
        private String materialCode;
        private String materialName;
        private BigDecimal quantity;
        private String unit;
        private String locationCode;
        private String batchNo;
    }
}
