package com.hxcoe.qms.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.type.TypeReference;
import com.hxcoe.qms.util.JsonUtils;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 质量趋势预测结果实体
 */
@Entity
@Table(name = "qms_forecast_result")
@Data
public class ForecastResultEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "forecast_type", length = 64)
    private String forecastType;

    @Column(name = "period", length = 64)
    private String period;

    @JsonIgnore
    @Lob
    @Column(name = "forecast_data_json", columnDefinition = "LONGTEXT")
    private String forecastDataJson;

    @Column(name = "confidence_interval")
    private Double confidenceInterval;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "status", length = 16)
    private String status;

    @Column(name = "created_time")
    private LocalDateTime createdTime;

    @Column(name = "updated_time")
    private LocalDateTime updatedTime;

    /**
     * 获取预测数据
     *
     * @return 预测数据列表
     */
    public List<Map<String, Object>> getForecastData() {
        return JsonUtils.fromJson(this.forecastDataJson, new TypeReference<List<Map<String, Object>>>() {});
    }

    /**
     * 设置预测数据
     *
     * @param forecastData 预测数据列表
     */
    public void setForecastData(List<Map<String, Object>> forecastData) {
        this.forecastDataJson = JsonUtils.toJson(forecastData);
    }
}
