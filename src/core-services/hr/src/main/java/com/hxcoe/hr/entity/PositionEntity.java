package com.hxcoe.hr.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 岗位实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "hr_position")
@JsonIgnoreProperties({"employees", "department"})
public class PositionEntity {

    /**
     * 岗位ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 岗位名称
     */
    @Column(name = "name", nullable = false, length = 50)
    @NotBlank(message = "岗位名称不能为空")
    @Size(max = 50, message = "岗位名称长度不能超过50个字符")
    private String name;

    /**
     * 岗位编号
     */
    @Column(name = "position_code", unique = true, nullable = false, length = 20)
    @NotBlank(message = "岗位编号不能为空")
    @Size(max = 20, message = "岗位编号长度不能超过20个字符")
    private String positionCode;

    /**
     * 岗位等级
     */
    @Column(name = "level", length = 20)
    private String level;

    /**
     * 岗位描述
     */
    @Column(name = "description", length = 200)
    @Size(max = 200, message = "岗位描述长度不能超过200个字符")
    private String description;

    /**
     * 岗位状态
     */
    @Column(name = "status", length = 20)
    @Size(max = 20, message = "岗位状态长度不能超过20个字符")
    private String status;

    /**
     * 创建时间
     */
    @Column(name = "created_time", nullable = false, updatable = false)
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    @Column(name = "updated_time")
    private LocalDateTime updatedTime;

    /**
     * 创建人
     */
    @Column(name = "created_by", length = 50)
    private String createdBy;

    /**
     * 更新人
     */
    @Column(name = "updated_by", length = 50)
    private String updatedBy;

    /**
     * 备注
     */
    @Column(name = "remark", length = 500)
    private String remark;
    
    /**
     * 乐观锁版本号
     */
    @Version
    @Column(name = "version")
    private Integer version;

    /**
     * 部门ID
     */
    @Column(name = "department_id")
    private Long departmentId;

    /**
     * 部门关联
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", insertable = false, updatable = false)
    @JsonIgnore
    private DepartmentEntity department;

    /**
     * 员工关联 - 反向关联，使用JsonIgnore忽略
     */
    @OneToMany(mappedBy = "position", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<EmployeeEntity> employees;

    /**
     * 自动设置创建时间
     */
    @PrePersist
    public void prePersist() {
        this.createdTime = LocalDateTime.now();
    }

    /**
     * 自动更新时间
     */
    @PreUpdate
    public void preUpdate() {
        this.updatedTime = LocalDateTime.now();
    }
}