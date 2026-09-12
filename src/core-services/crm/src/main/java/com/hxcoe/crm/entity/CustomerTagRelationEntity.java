package com.hxcoe.crm.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 客户标签关系实体类
 */
@Entity
@Table(name = "crm_customer_tag_relation")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerTagRelationEntity {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 客户ID
     */
    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    /**
     * 标签ID
     */
    @Column(name = "tag_id", nullable = false)
    private Long tagId;

    /**
     * 创建时间
     */
    @Column(name = "created_time", nullable = false, updatable = false)
    private LocalDateTime createTime;
}