package com.hxcoe.agv.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "agv_path_point")
public class AgvPathPointEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "plan_id", nullable = false, length = 64)
    private String planId;

    @Column(name = "seq_no", nullable = false)
    private Integer seqNo;

    @Column(name = "node_code", length = 128)
    private String nodeCode;

    @Column(name = "x", precision = 19, scale = 6)
    private BigDecimal x;

    @Column(name = "y", precision = 19, scale = 6)
    private BigDecimal y;

    @Column(name = "heading", precision = 19, scale = 6)
    private BigDecimal heading;

    @Column(name = "speed_limit", precision = 10, scale = 2)
    private BigDecimal speedLimit;

    @Column(name = "remark", length = 512)
    private String remark;

    @PrePersist
    protected void onCreate() {
        if (nodeCode == null) nodeCode = "";
        if (x == null) x = BigDecimal.ZERO;
        if (y == null) y = BigDecimal.ZERO;
        if (heading == null) heading = BigDecimal.ZERO;
        if (speedLimit == null) speedLimit = BigDecimal.ZERO;
        if (remark == null) remark = "";
    }
}
