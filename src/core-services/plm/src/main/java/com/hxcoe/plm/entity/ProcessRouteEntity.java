package com.hxcoe.plm.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "plm_process_route")
@Data
public class ProcessRouteEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "route_code", length = 64, unique = true)
    private String routeCode;

    @Column(name = "route_name", nullable = false, length = 128)
    private String routeName;

    @Column(name = "product_code", length = 64)
    private String productCode;

    @Column(name = "product_name", length = 128)
    private String productName;

    @Column(name = "version", length = 32)
    private String version;

    @Column(name = "route_type", length = 32)
    private String routeType;

    @Column(name = "status", length = 32)
    private String status;

    @Column(name = "create_user", length = 64)
    private String createUser;

    @Lob
    @Column(name = "steps_json", columnDefinition = "TEXT")
    private String stepsJson;

    @Column(name = "created_time")
    private LocalDateTime createdTime;

    @Column(name = "updated_time")
    private LocalDateTime updatedTime;
}
