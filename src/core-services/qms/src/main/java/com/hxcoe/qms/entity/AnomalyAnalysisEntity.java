package com.hxcoe.qms.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.type.TypeReference;
import com.hxcoe.qms.util.JsonUtils;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 质量异常分析实体
 */
@Entity
@Table(name = "qms_anomaly_analysis")
@Data
public class AnomalyAnalysisEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "report_id")
    private Long reportId;

    @Column(name = "report_no", length = 64)
    private String reportNo;

    @Column(name = "analysis_method", length = 64)
    private String analysisMethod;

    @JsonIgnore
    @Lob
    @Column(name = "analysis_team_json", columnDefinition = "LONGTEXT")
    private String analysisTeamJson;

    @Column(name = "analysis_date")
    private LocalDate analysisDate;

    @Column(name = "man_factor", length = 1000)
    private String manFactor;

    @Column(name = "machine_factor", length = 1000)
    private String machineFactor;

    @Column(name = "material_factor", length = 1000)
    private String materialFactor;

    @Column(name = "method_factor", length = 1000)
    private String methodFactor;

    @Column(name = "environment_factor", length = 1000)
    private String environmentFactor;

    @Column(name = "measurement_factor", length = 1000)
    private String measurementFactor;

    @Column(name = "root_cause", length = 2000)
    private String rootCause;

    @Column(name = "analysis_status", length = 16)
    private String analysisStatus;

    @Column(name = "analyzer", length = 64)
    private String analyzer;

    @Column(name = "analysis_time")
    private LocalDateTime analysisTime;

    @Column(name = "created_time")
    private LocalDateTime createdTime;

    @Column(name = "updated_time")
    private LocalDateTime updatedTime;

    /**
     * 获取分析小组成员列表
     *
     * @return 成员列表
     */
    public List<String> getAnalysisTeam() {
        return JsonUtils.fromJson(this.analysisTeamJson, new TypeReference<List<String>>() {});
    }

    /**
     * 设置分析小组成员列表
     *
     * @param analysisTeam 成员列表
     */
    public void setAnalysisTeam(List<String> analysisTeam) {
        this.analysisTeamJson = JsonUtils.toJson(analysisTeam);
    }
}
