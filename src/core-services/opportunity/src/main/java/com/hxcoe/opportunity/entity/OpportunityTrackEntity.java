package com.hxcoe.opportunity.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "opportunity_tracks")
public class OpportunityTrackEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "opportunity_id", nullable = false)
    private OpportunityEntity opportunity;

    @Column(name = "track_time", nullable = false)
    private LocalDateTime trackTime;

    @Column(name = "content", nullable = false, length = 500)
    private String content;

    @Column(name = "track_type", nullable = false, length = 20)
    private String trackType;

    @Column(name = "tracker", nullable = false, length = 50)
    private String tracker;

    @Column(name = "next_follow_time")
    private LocalDateTime nextFollowTime;

    @Column(name = "remark", length = 500)
    private String remark;

    @Column(name = "created_time", nullable = false, updatable = false)
    private LocalDateTime createdTime;

    @Column(name = "updated_time")
    private LocalDateTime updatedTime;

    @PrePersist
    public void prePersist() {
        this.createdTime = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedTime = LocalDateTime.now();
    }
}