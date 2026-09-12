package com.hxcoe.qms.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.type.TypeReference;
import com.hxcoe.qms.util.JsonUtils;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 质量数据采集实体
 */
@Entity
@Table(name = "qms_data_collection")
@Data
public class DataCollectionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "collection_no", nullable = false, unique = true, length = 64)
    private String collectionNo;

    @Column(name = "collection_name", length = 128)
    private String collectionName;

    @Column(name = "collection_date")
    private LocalDate collectionDate;

    @Column(name = "data_type", length = 64)
    private String dataType;

    @Column(name = "source", length = 128)
    private String source;

    @JsonIgnore
    @Lob
    @Column(name = "data_items_json", columnDefinition = "LONGTEXT")
    private String dataItemsJson;

    @Column(name = "collector", length = 64)
    private String collector;

    @Column(name = "status", length = 16)
    private String status;

    @Column(name = "created_time")
    private LocalDateTime createdTime;

    @Column(name = "updated_time")
    private LocalDateTime updatedTime;

    /**
     * 获取数据项
     *
     * @return 数据项列表
     */
    public List<Map<String, Object>> getDataItems() {
        return JsonUtils.fromJson(this.dataItemsJson, new TypeReference<List<Map<String, Object>>>() {});
    }

    /**
     * 设置数据项
     *
     * @param dataItems 数据项列表
     */
    public void setDataItems(List<Map<String, Object>> dataItems) {
        this.dataItemsJson = JsonUtils.toJson(dataItems);
    }
}
