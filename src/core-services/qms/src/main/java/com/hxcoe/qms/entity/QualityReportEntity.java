package com.hxcoe.qms.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.type.TypeReference;
import com.hxcoe.qms.util.JsonUtils;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 质量报告实体
 */
@Entity
@Table(name = "qms_quality_report")
@Data
public class QualityReportEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "report_no", nullable = false, unique = true, length = 64)
    private String reportNo;

    @Column(name = "report_name", length = 128)
    private String reportName;

    @Column(name = "report_type", length = 16)
    private String reportType;

    @Column(name = "period", length = 64)
    private String period;

    @JsonIgnore
    @Lob
    @Column(name = "content_json", columnDefinition = "LONGTEXT")
    private String contentJson;

    @Column(name = "creator", length = 64)
    private String creator;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "status", length = 16)
    private String status;

    @Column(name = "created_time")
    private LocalDateTime createdTime;

    @Column(name = "updated_time")
    private LocalDateTime updatedTime;

    /**
     * 获取报告内容
     *
     * @return 报告内容
     */
    public Map<String, Object> getContent() {
        return JsonUtils.fromJson(this.contentJson, new TypeReference<Map<String, Object>>() {});
    }

    /**
     * 设置报告内容
     *
     * @param content 报告内容
     */
    public void setContent(Map<String, Object> content) {
        this.contentJson = JsonUtils.toJson(content);
    }
}
