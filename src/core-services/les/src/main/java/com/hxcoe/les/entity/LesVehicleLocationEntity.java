package com.hxcoe.les.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "les_vehicle_location")
@Data
public class LesVehicleLocationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "plan_id")
    private Long planId;

    @Column(name = "vehicle_id")
    private Long vehicleId;

    @Column(name = "license_plate", length = 50)
    private String licensePlate;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "speed")
    private Double speed;

    @Column(name = "direction")
    private Double direction;

    @Column(name = "record_time")
    private LocalDateTime recordTime;
}
