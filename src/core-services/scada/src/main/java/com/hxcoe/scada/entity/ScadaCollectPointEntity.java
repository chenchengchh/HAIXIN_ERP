package com.hxcoe.scada.entity;

import jakarta.persistence.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

@Entity
@Table(name = "scada_collect_point")
@Data
public class ScadaCollectPointEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tag_code", nullable = false, unique = true, length = 50)
    private String tagCode;

    @Column(name = "device_name", length = 100)
    private String deviceName;

    @Column(name = "protocol", length = 20)
    private String protocol;

    @Column(name = "address", length = 100)
    private String address;

    @Column(name = "data_type", length = 30)
    private String dataType;

    @Column(name = "unit", length = 20)
    private String unit;

    @Column(name = "latest_value")
    @JsonProperty("value")
    private Double latestValue;

    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "enabled")
    private Boolean enabled;

    @Column(name = "remark", length = 500)
    private String remark;

    @Column(name = "created_time")
    private LocalDateTime createdTime;

    @Column(name = "updated_time")
    private LocalDateTime updatedTime;
}
