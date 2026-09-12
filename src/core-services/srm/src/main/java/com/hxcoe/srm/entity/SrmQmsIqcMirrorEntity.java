package com.hxcoe.srm.entity;

import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * QMS IQC检验结果幂等镜像实体
 *
 * <p>记录已处理过的QMS检验单号，用于防止同一检验结果重复累计供应商绩效。
 */
@Data
@Entity
@Table(name = "srm_qms_iqc_mirror")
public class SrmQmsIqcMirrorEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * QMS检验单号（唯一约束，幂等判重键）
     */
    @Column(name = "inspection_no", nullable = false, unique = true, length = 64)
    private String inspectionNo;

    /**
     * 集成事件ID（QMS侧生成的UUID）
     */
    @Column(name = "event_id", length = 64)
    private String eventId;

    /**
     * 供应商ID
     */
    @Column(name = "supplier_id")
    private Long supplierId;

    /**
     * 检验结果（PASS/FAIL）
     */
    @Column(name = "result", length = 16)
    private String result;

    @Column(name = "created_time")
    private LocalDateTime createdTime;

    @PrePersist
    protected void onCreate() {
        createdTime = LocalDateTime.now();
    }
}
