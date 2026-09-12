package com.hxcoe.plm.client.dto.bom;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class BomHeaderDTO {
    private Long id;
    private Long materialId; // 需要先根据产品查询物料ID
    private String materialCode;
    private String materialName;
    private String bomCode;
    private String version;
    private Integer type; // 1:EBOM
    private Integer status; // 1:启用
    private Boolean isDefault;
    private List<BomLineDTO> lines;
}
