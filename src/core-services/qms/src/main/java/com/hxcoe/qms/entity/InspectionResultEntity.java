package com.hxcoe.qms.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.type.TypeReference;
import com.hxcoe.qms.dto.inspection.InspectionResultItemDTO;
import com.hxcoe.qms.util.JsonUtils;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 检验结果实体
 */
@Entity
@Table(name = "qms_inspection_result")
@Data
public class InspectionResultEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "result_no", nullable = false, unique = true, length = 64)
    private String resultNo;

    @Column(name = "task_id")
    private Long taskId;

    @Column(name = "task_no", length = 64)
    private String taskNo;

    @Column(name = "material_code", length = 64)
    private String materialCode;

    @Column(name = "material_name", length = 128)
    private String materialName;

    @Column(name = "batch_no", length = 64)
    private String batchNo;

    @Column(name = "inspector", length = 64)
    private String inspector;

    @Column(name = "inspection_time")
    private LocalDateTime inspectionTime;

    @JsonIgnore
    @Lob
    @Column(name = "inspection_items_json", columnDefinition = "LONGTEXT")
    private String inspectionItemsJson;

    @Column(name = "inspection_result", length = 16)
    private String inspectionResult;

    @Column(name = "audit_status", length = 16)
    private String auditStatus;

    @Column(name = "auditor", length = 64)
    private String auditor;

    @Column(name = "audit_time")
    private LocalDateTime auditTime;

    @Column(name = "audit_remark", length = 500)
    private String auditRemark;

    @Column(name = "inspection_quantity")
    private BigDecimal inspectionQuantity;

    @Column(name = "qualified_quantity")
    private BigDecimal qualifiedQuantity;

    @Column(name = "unqualified_quantity")
    private BigDecimal unqualifiedQuantity;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "created_time")
    private LocalDateTime createdTime;

    @Column(name = "updated_time")
    private LocalDateTime updatedTime;

    /**
     * 获取检验项目列表
     *
     * @return 检验项目列表
     */
    public List<InspectionResultItemDTO> getInspectionItems() {
        return JsonUtils.fromJson(this.inspectionItemsJson, new TypeReference<List<InspectionResultItemDTO>>() {});
    }

    /**
     * 设置检验项目列表
     *
     * @param inspectionItems 检验项目列表
     */
    public void setInspectionItems(List<InspectionResultItemDTO> inspectionItems) {
        this.inspectionItemsJson = JsonUtils.toJson(inspectionItems);
    }
}
