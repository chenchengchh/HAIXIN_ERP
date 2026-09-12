package com.hxcoe.les.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "les_route")
@Data
public class LesRouteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "route_name", nullable = false, length = 100)
    private String routeName;

    @Column(name = "start_location", length = 100)
    private String startLocation;

    @Column(name = "end_location", length = 100)
    private String endLocation;

    @Column(name = "distance")
    private Double distance;

    @Column(name = "estimated_time")
    private Integer estimatedTime;

    @Column(name = "create_time")
    private LocalDateTime createTime;
}
