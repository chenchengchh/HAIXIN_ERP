package com.hxcoe.les.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "les_driver")
@Data
public class LesDriverEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "license_no", length = 50)
    private String licenseNo;

    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "create_time")
    private LocalDateTime createTime;
}

