package com.hxcoe.plm.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "plm_product")
@Data
public class ProductEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_code", nullable = false, unique = true, length = 50)
    private String productCode;

    @Column(name = "product_name", nullable = false, length = 100)
    private String productName;

    @Column(name = "product_model", length = 50)
    private String productModel;

    @Column(name = "product_spec", length = 200)
    private String productSpec;

    @Column(name = "product_type", length = 50)
    private String productType;

    @Column(name = "product_category", length = 50)
    private String productCategory;

    @Column(name = "unit", length = 20)
    private String unit;

    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "version", length = 20)
    private String version;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "created_by", length = 50)
    private String createdBy;

    @Column(name = "created_time")
    private LocalDateTime createdTime;

    @Column(name = "updated_by", length = 50)
    private String updatedBy;

    @Column(name = "updated_time")
    private LocalDateTime updatedTime;
}
