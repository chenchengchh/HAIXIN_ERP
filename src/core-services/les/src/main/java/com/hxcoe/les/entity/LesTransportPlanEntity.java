package com.hxcoe.les.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "les_transport_plan")
@Data
public class LesTransportPlanEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "plan_no", nullable = false, unique = true, length = 50)
    private String planNo;

    @Column(name = "sales_order_no", length = 50)
    private String salesOrderNo;

    @Column(name = "source_type", length = 20)
    private String sourceType;

    @Column(name = "source_no", length = 50)
    private String sourceNo;

    @Column(name = "vehicle_id")
    private Long vehicleId;

    @Column(name = "driver_id")
    private Long driverId;

    @Column(name = "route_id")
    private Long routeId;

    @Column(name = "status")
    private Integer status;

    @Column(name = "cost_estimated")
    private Double costEstimated;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;
}
