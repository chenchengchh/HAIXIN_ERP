package com.hxcoe.plm.client.dto.bom;

import lombok.Data;

/**
 * BOM服务物料创建DTO（用于PLM发布时同步物料库）。
 */
@Data
public class MaterialCreateDTO {
    private String materialCode;
    private String materialName;
    private String materialSpec;
    private String materialType;
    private String unit;
    private String status;
    private String createdBy;
    private String remark;
}

