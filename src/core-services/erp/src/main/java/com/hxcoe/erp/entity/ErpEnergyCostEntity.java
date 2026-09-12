package com.hxcoe.erp.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 能源成本实体类（EMS→ERP 能源成本闭环）
 * 用于接收并落库 EMS 定时汇总推送的能耗成本数据
 */
@Data
@Entity
@Table(name = "erp_energy_cost")
public class ErpEnergyCostEntity {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 统计周期（如 2026-07）
     */
    @Column(name = "period", nullable = false, length = 16)
    private String period;

    /**
     * 能源类型（电力、水、燃气、热能）
     */
    @Column(name = "energy_type", nullable = false, length = 50)
    private String energyType;

    /**
     * 采集区域
     */
    @Column(name = "area", nullable = false, length = 100)
    private String area;

    /**
     * 能耗用量
     */
    @Column(name = "consumption", nullable = false, precision = 18, scale = 4)
    private BigDecimal consumption;

    /**
     * 计量单位（如 kWh）
     */
    @Column(name = "unit", length = 20)
    private String unit;

    /**
     * 单价（元/单位）
     */
    @Column(name = "unit_price", nullable = false, precision = 18, scale = 4)
    private BigDecimal unitPrice;

    /**
     * 总成本（元）
     */
    @Column(name = "total_cost", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalCost;

    /**
     * 事件ID（UUID，幂等键，唯一约束防重复落库）
     */
    @Column(name = "event_id", unique = true, nullable = false, length = 64)
    private String eventId;

    /**
     * ERP 接收时间
     */
    @Column(name = "receive_time", nullable = false)
    private LocalDateTime receiveTime;
}
