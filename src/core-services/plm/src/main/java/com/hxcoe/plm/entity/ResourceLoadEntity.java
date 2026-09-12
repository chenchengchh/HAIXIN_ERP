package com.hxcoe.plm.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "plm_resource_load")
@Data
public class ResourceLoadEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id")
    private Long projectId;

    @Column(name = "resource_id", nullable = false, length = 64)
    private String resourceId;

    @Column(name = "resource_name", nullable = false, length = 64)
    private String resourceName;

    @Column(name = "load_value", nullable = false)
    private Integer loadValue;

    @Column(name = "load_date", nullable = false)
    private LocalDate loadDate;

    @Column(name = "created_time")
    private LocalDateTime createdTime;
}
