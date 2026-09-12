package com.hxcoe.scada.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "scada_device")
@Data
public class ScadaDeviceEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "device_code", nullable = false, unique = true, length = 50)
    private String deviceCode;

    @Column(name = "device_name", nullable = false, length = 100)
    private String deviceName;

    @Column(name = "device_type", length = 20)
    private String deviceType;

    @Column(name = "device_category", length = 50)
    private String deviceCategory;

    @Column(name = "device_status", length = 20)
    private String deviceStatus;

    @Column(name = "location", length = 100)
    private String location;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(name = "port", length = 10)
    private String port;

    @Column(name = "protocol", length = 20)
    private String protocol;

    @Column(name = "collect_interval", length = 20)
    private String collectInterval;

    @Column(name = "last_online_time")
    private LocalDateTime lastOnlineTime;

    @Column(name = "remark", length = 500)
    private String remark;

    @Column(name = "created_by", length = 50)
    private String createdBy;

    @Column(name = "created_time")
    private LocalDateTime createdTime;

    @Column(name = "updated_by", length = 50)
    private String updatedBy;

    @Column(name = "updated_time")
    private LocalDateTime updatedTime;
}