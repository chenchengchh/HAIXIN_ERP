package com.hxcoe.les.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "les_transport_cost")
@Data
public class LesTransportCostEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "plan_id")
    private Long planId;

    @Column(name = "fuel_cost")
    private Double fuelCost;

    @Column(name = "toll_cost")
    private Double tollCost;

    @Column(name = "driver_salary")
    private Double driverSalary;

    @Column(name = "other_cost")
    private Double otherCost;

    @Column(name = "total_cost")
    private Double totalCost;

    @Column(name = "create_time")
    private LocalDateTime createTime;
}
