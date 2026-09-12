package com.hxcoe.scm.client.dto.bom;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class BomStructureDTO {
    private Long id; // Header ID
    private Long materialId;
    private String materialCode;
    private String materialName;
    private String version;
    private List<BomLineDTO> lines;
}
