package com.hxcoe.mes.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.hxcoe.mes.util.JsonUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "mes_quality_inspection")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QualityInspectionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "inspection_id", length = 64)
    private String inspectionId;

    @Column(name = "inspection_name", length = 128)
    private String inspectionName;

    @Column(name = "work_order_no", length = 64)
    private String workOrderNo;

    @Column(name = "sn_code", length = 64)
    private String snCode;

    @Column(name = "step_id", length = 64)
    private String stepId;

    @Column(name = "step_name", length = 128)
    private String stepName;

    @Column(name = "inspector_id", length = 64)
    private String inspectorId;

    @Column(name = "inspector_name", length = 64)
    private String inspectorName;

    @Column(name = "inspection_time")
    private LocalDateTime inspectionTime;

    @Column(name = "result", length = 16)
    private String result;

    @Column(name = "defect_type", length = 64)
    private String defectType;

    @Column(name = "defect_description", length = 255)
    private String defectDescription;

    @Lob
    @JsonIgnore
    @Column(name = "inspection_items_json")
    private String inspectionItemsJson;

    @Column(name = "status", length = 32)
    private String status;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    public List<Map<String, Object>> getInspectionItems() {
        return JsonUtils.fromJson(this.inspectionItemsJson, new TypeReference<List<Map<String, Object>>>() {});
    }

    public void setInspectionItems(List<Map<String, Object>> inspectionItems) {
        this.inspectionItemsJson = JsonUtils.toJson(inspectionItems);
    }
}
