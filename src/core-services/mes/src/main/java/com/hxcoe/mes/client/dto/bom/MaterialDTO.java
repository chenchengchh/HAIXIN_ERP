package com.hxcoe.mes.client.dto.bom;

import lombok.Data;

@Data
public class MaterialDTO {
    private Long id;
    private String materialCode;
    private String materialName;
    private String materialSpec;
    private String unit;
}
