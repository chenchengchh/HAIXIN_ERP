package com.hxcoe.plm.client.dto.erp;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * ERP物料主数据DTO，字段与ERP端下划线序列化格式对应。
 */
@Data
public class ErpMaterialDTO {
    private Long id;

    @JsonProperty("material_code")
    private String materialCode;

    @JsonProperty("material_name")
    private String materialName;

    @JsonProperty("material_type")
    private String materialType;

    private String specification;

    private String unit;

    @JsonProperty("unit_price")
    private java.math.BigDecimal unitPrice;

    @JsonProperty("approval_status")
    private String approvalStatus;

    private Integer status;

    private String remark;
}
