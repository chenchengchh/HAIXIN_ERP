package com.hxcoe.mes.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "mes_batch")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatchEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "batch_no", unique = true, nullable = false, length = 64)
    private String batchNo;

    @Column(name = "work_order_no", length = 64)
    private String workOrderNo;

    @Column(name = "material_id", length = 64)
    private String materialId;

    @Column(name = "material_name", length = 128)
    private String materialName;

    @Column(name = "qty")
    private Integer qty;

    @Column(name = "status", length = 32)
    private String status;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;
}
