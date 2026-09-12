package com.hxcoe.wms.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "wms_stock_count_job")
public class StockCountJobEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "count_no", unique = true, nullable = false, length = 64)
    private String countNo;

    @Column(name = "warehouse_code", length = 64)
    private String warehouseCode;

    @Column(name = "count_type", length = 8)
    private String countType;

    @Column(name = "status", length = 8)
    private String status;

    @Column(name = "create_user", length = 64)
    private String createUser;

    private LocalDateTime createTime;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private Integer totalItemCount;
    private Integer finishedItemCount;
    private Integer diffCount;

    private LocalDateTime updatedTime;

    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StockCountItemEntity> items;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createTime == null) {
            createTime = now;
        }
        updatedTime = now;
        if (status == null || status.isBlank()) {
            status = "0";
        }
        if (countType == null || countType.isBlank()) {
            countType = "1";
        }
        if (createUser == null || createUser.isBlank()) {
            createUser = "admin";
        }
        if (totalItemCount == null) totalItemCount = 0;
        if (finishedItemCount == null) finishedItemCount = 0;
        if (diffCount == null) diffCount = 0;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedTime = LocalDateTime.now();
    }
}

