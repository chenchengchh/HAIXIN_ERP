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
 * 不合格品评审实体
 */
@Entity
@Table(name = "qms_nc_review")
@Data
public class NcReviewEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "registration_id")
    private Long registrationId;

    @Column(name = "registration_no", length = 64)
    private String registrationNo;

    @JsonIgnore
    @Lob
    @Column(name = "review_team_json", columnDefinition = "LONGTEXT")
    private String reviewTeamJson;

    @Column(name = "review_date")
    private LocalDate reviewDate;

    @Column(name = "review_opinion", length = 1000)
    private String reviewOpinion;

    @Column(name = "disposal_plan", length = 32)
    private String disposalPlan;

    @Column(name = "review_status", length = 16)
    private String reviewStatus;

    @Column(name = "reviewer", length = 64)
    private String reviewer;

    @Column(name = "review_time")
    private LocalDateTime reviewTime;

    @Column(name = "created_time")
    private LocalDateTime createdTime;

    @Column(name = "updated_time")
    private LocalDateTime updatedTime;

    /**
     * 获取评审小组成员列表
     *
     * @return 成员列表
     */
    public List<String> getReviewTeam() {
        return JsonUtils.fromJson(this.reviewTeamJson, new TypeReference<List<String>>() {});
    }

    /**
     * 设置评审小组成员列表
     *
     * @param reviewTeam 成员列表
     */
    public void setReviewTeam(List<String> reviewTeam) {
        this.reviewTeamJson = JsonUtils.toJson(reviewTeam);
    }
}
