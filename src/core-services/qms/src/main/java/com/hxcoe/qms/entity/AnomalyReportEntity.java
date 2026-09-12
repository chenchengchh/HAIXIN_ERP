package com.hxcoe.qms.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.type.TypeReference;
import com.hxcoe.qms.util.JsonUtils;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 质量异常报告实体
 */
@Entity
@Table(name = "qms_anomaly_report")
@Data
public class AnomalyReportEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "report_no", nullable = false, unique = true, length = 64)
    private String reportNo;

    @Column(name = "title", length = 200)
    private String title;

    @Column(name = "anomaly_type", length = 64)
    private String anomalyType;

    @Column(name = "severity", length = 16)
    private String severity;

    @Column(name = "occurrence_time")
    private LocalDateTime occurrenceTime;

    @Column(name = "occurrence_location", length = 128)
    private String occurrenceLocation;

    @Column(name = "description", length = 2000)
    private String description;

    @Column(name = "reporter", length = 64)
    private String reporter;

    @Column(name = "report_time")
    private LocalDateTime reportTime;

    @Column(name = "status", length = 16)
    private String status;

    @JsonIgnore
    @Lob
    @Column(name = "related_products_json", columnDefinition = "LONGTEXT")
    private String relatedProductsJson;

    @Column(name = "impact_assessment", length = 2000)
    private String impactAssessment;

    @Column(name = "created_time")
    private LocalDateTime createdTime;

    @Column(name = "updated_time")
    private LocalDateTime updatedTime;

    /**
     * 获取关联产品列表
     *
     * @return 关联产品列表
     */
    public List<String> getRelatedProducts() {
        return JsonUtils.fromJson(this.relatedProductsJson, new TypeReference<List<String>>() {});
    }

    /**
     * 设置关联产品列表
     *
     * @param relatedProducts 关联产品列表
     */
    public void setRelatedProducts(List<String> relatedProducts) {
        this.relatedProductsJson = JsonUtils.toJson(relatedProducts);
    }
}
