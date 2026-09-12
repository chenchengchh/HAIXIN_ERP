package com.hxcoe.crm.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 销售线索实体类
 */
@Data
@Entity
@Table(name = "crm_lead")
@EntityListeners(AuditingEntityListener.class)
public class Lead {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 线索编号
     */
    @Column(name = "lead_no")
    private String leadNo;

    /**
     * 线索名称
     */
    @Column(name = "lead_name")
    private String leadName;

    /**
     * 公司名称
     */
    @Column(name = "company_name")
    private String companyName;

    /**
     * 联系人
     */
    @Column(name = "contact_name")
    private String contactName;

    /**
     * 电话
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 来源
     */
    private String source;

    /**
     * 行业
     */
    private String industry;

    /**
     * 意向产品/服务
     */
    private String intent;

    /**
     * 评级：hot/warm/cold
     */
    private String rating;

    /**
     * 状态：new/contacted/qualified/converted/lost
     */
    private String status;

    /**
     * 负责人ID
     */
    @Column(name = "owner_id")
    private Long ownerId;

    /**
     * 负责人姓名
     */
    @Column(name = "owner_name")
    private String ownerName;

    /**
     * 转化时间
     */
    @Column(name = "convert_time")
    private LocalDateTime convertTime;

    /**
     * 创建时间
     */
    @CreatedDate
    @Column(name = "create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @LastModifiedDate
    @Column(name = "update_time")
    private LocalDateTime updateTime;

    /**
     * 创建人
     */
    @CreatedBy
    @Column(name = "create_by")
    private Long createBy;

    /**
     * 更新人
     */
    @LastModifiedBy
    @Column(name = "update_by")
    private Long updateBy;

    /**
     * 是否删除：0-未删除，1-已删除
     */
    @Column(name = "is_deleted")
    private Integer isDeleted = 0;
}
