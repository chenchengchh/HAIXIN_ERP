package com.hxcoe.ems.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "ems_custom_report")
public class EmsCustomReportEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", length = 180, nullable = false)
    private String name;

    @Column(name = "creator", length = 80, nullable = false)
    private String creator;

    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @Column(name = "last_modified", nullable = false)
    private LocalDateTime lastModified;

    @Column(name = "sql_query", columnDefinition = "TEXT")
    private String sqlQuery;

    @Column(name = "template_config", columnDefinition = "TEXT")
    private String templateConfig;

    @PrePersist
    public void onCreate() {
        createdDate = LocalDateTime.now();
        lastModified = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        lastModified = LocalDateTime.now();
    }
}

