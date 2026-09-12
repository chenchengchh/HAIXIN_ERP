package com.hxcoe.qms.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.persistence.*;
import lombok.Data;

import com.hxcoe.qms.util.JsonUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 检验标准实体
 */
@Entity
@Table(name = "qms_inspection_standard")
@Data
public class InspectionStandardEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "standard_no", nullable = false, unique = true, length = 64)
    private String standardNo;

    @Column(name = "material_code", length = 64)
    private String materialCode;

    @Column(name = "material_name", length = 128)
    private String materialName;

    @Column(name = "version", length = 32)
    private String version;

    @Column(name = "aql_level", length = 32)
    private String aqlLevel;

    @Column(name = "status", length = 16)
    private String status;

    @Lob
    @JsonIgnore
    @Column(name = "inspection_items_json", columnDefinition = "LONGTEXT")
    private String inspectionItemsJson;

    @Column(name = "creator", length = 64)
    private String creator;

    @Column(name = "created_time")
    private LocalDateTime createdTime;

    @Column(name = "updated_time")
    private LocalDateTime updatedTime;

    /**
     * 获取检验项目列表
     *
     * @return 检验项目列表
     */
    public List<Map<String, Object>> getInspectionItems() {
        return JsonUtils.fromJson(this.inspectionItemsJson, new TypeReference<List<Map<String, Object>>>() {});
    }

    public void setInspectionItems(List<Map<String, Object>> inspectionItems) {
        this.inspectionItemsJson = JsonUtils.toJson(inspectionItems);
    }
}
