package com.hxcoe.scada.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "scada_storage_policy")
@Data
public class ScadaStoragePolicyEntity {
    @Id
    private Long id;

    @Column(name = "storage_type", length = 30)
    private String storageType;

    @Column(name = "host", length = 100)
    private String host;

    @Column(name = "port")
    private Integer port;

    @Column(name = "database_name", length = 100)
    private String database;

    @Column(name = "username", length = 100)
    private String username;

    @Column(name = "password", length = 200)
    private String password;

    @Column(name = "connection_status", length = 30)
    private String connectionStatus;

    @Column(name = "realtime_retention")
    private Integer realtimeRetention;

    @Column(name = "historical_sampling", length = 30)
    private String historicalSampling;

    @Column(name = "compression_level", length = 30)
    private String compressionLevel;

    @Column(name = "backup_policy", length = 30)
    private String backupPolicy;

    @Column(name = "stored_data")
    private Integer storedData;

    @Column(name = "writes_per_second")
    private Integer writesPerSecond;

    @Column(name = "storage_efficiency")
    private Integer storageEfficiency;

    @Column(name = "last_write_time")
    private LocalDateTime lastWriteTime;

    @Column(name = "enabled")
    private Boolean enabled;

    @Column(name = "updated_time")
    private LocalDateTime updatedTime;
}

