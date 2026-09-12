package com.hxcoe.plm.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "plm_gantt_link")
@Data
public class GanttLinkEntity {
    @Id
    private Long id;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "source_task_id", nullable = false)
    private Long sourceTaskId;

    @Column(name = "target_task_id", nullable = false)
    private Long targetTaskId;

    @Column(name = "link_type", nullable = false, length = 32)
    private String linkType;

    @Column(name = "created_time")
    private LocalDateTime createdTime;
}
