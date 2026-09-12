package com.hxcoe.scm.entity;

import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "scm_mrp_plan")
public class MrpPlanEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String runNo;

    @Column(name = "run_name")
    private String runName;
    private LocalDateTime runDate;
    
    // FORECAST, ORDER, HYBRID
    private String demandSource;
    
    // 安全库存, 在途库存, 在制库存 (逗号分隔)
    private String considerFactors;
    
    private Integer planHorizon; // 天数
    
    // PENDING, RUNNING, COMPLETED, FAILED
    private String status;
    
    private Integer resultCount;

    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
