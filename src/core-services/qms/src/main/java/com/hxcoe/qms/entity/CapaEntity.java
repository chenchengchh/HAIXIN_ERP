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
 * CAPA（纠正与预防措施）实体
 */
@Entity
@Table(name = "qms_capa")
@Data
public class CapaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "report_id")
    private Long reportId;

    @Column(name = "report_no", length = 64)
    private String reportNo;

    @Column(name = "preventive_measure", length = 2000)
    private String preventiveMeasure;

    @Column(name = "corrective_measure", length = 2000)
    private String correctiveMeasure;

    @JsonIgnore
    @Lob
    @Column(name = "implementation_team_json", columnDefinition = "LONGTEXT")
    private String implementationTeamJson;

    @Column(name = "implementation_deadline")
    private LocalDate implementationDeadline;

    @Column(name = "actual_completion_date")
    private LocalDate actualCompletionDate;

    @Column(name = "implementation_status", length = 16)
    private String implementationStatus;

    @Column(name = "verify_status", length = 16)
    private String verifyStatus;

    @Column(name = "verify_result", length = 2000)
    private String verifyResult;

    @Column(name = "verify_date")
    private LocalDate verifyDate;

    @Column(name = "reviewer", length = 64)
    private String reviewer;

    @Column(name = "review_date")
    private LocalDate reviewDate;

    @Column(name = "review_result", length = 16)
    private String reviewResult;

    @Column(name = "created_time")
    private LocalDateTime createdTime;

    @Column(name = "updated_time")
    private LocalDateTime updatedTime;

    /**
     * 获取实施团队成员列表
     *
     * @return 成员列表
     */
    public List<String> getImplementationTeam() {
        return JsonUtils.fromJson(this.implementationTeamJson, new TypeReference<List<String>>() {});
    }

    /**
     * 设置实施团队成员列表
     *
     * @param implementationTeam 成员列表
     */
    public void setImplementationTeam(List<String> implementationTeam) {
        this.implementationTeamJson = JsonUtils.toJson(implementationTeam);
    }
}
