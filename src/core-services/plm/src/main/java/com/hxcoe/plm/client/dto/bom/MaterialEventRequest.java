package com.hxcoe.plm.client.dto.bom;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * BOM服务物料集成事件请求体，与BOM端 /bom/integration/erp/material-events 入参结构一致。
 */
@Data
public class MaterialEventRequest {
    private String eventId;
    private String eventType;
    private String eventKey;
    private String idempotencyKey;
    private String partitionKey;
    private Integer eventVersion;
    private String producer;
    private String traceId;
    /** 事件发生时间：序列化为ISO格式，与BOM端默认LocalDateTime反序列化格式兼容 */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime eventTime;
    private MaterialPayload material;

    /**
     * 物料事件负载。
     */
    @Data
    public static class MaterialPayload {
        private Long materialId;
        private String materialCode;
        private String materialName;
        private String materialType;
        private String specification;
        private String unit;
        private BigDecimal unitPrice;
        private String approvalStatus;
        private Integer status;
        private Integer isDeleted;
        /** 物料更新时间：序列化为ISO格式，与BOM端默认LocalDateTime反序列化格式兼容 */
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime updatedTime;
    }
}
