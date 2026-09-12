package com.hxcoe.crm.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 合同附件实体类
 */
@Entity
@Table(name = "crm_contract_attachment")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContractAttachmentEntity {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 关联合同ID
     */
    @Column(name = "contract_id", nullable = false)
    private Long contractId;

    /**
     * 文件名
     */
    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    /**
     * 文件访问路径
     */
    @Column(name = "file_url", nullable = false, length = 512)
    private String fileUrl;

    /**
     * 文件大小（字节）
     */
    @Column(name = "file_size")
    private Long fileSize;

    /**
     * 上传时间
     */
    @Column(name = "upload_time")
    private LocalDateTime uploadTime;

    /**
     * 上传人ID
     */
    @Column(name = "upload_user_id")
    private Long uploadUserId;

    /**
     * 上传人姓名
     */
    @Column(name = "upload_user_name", length = 64)
    private String uploadUserName;

    /**
     * 持久化前自动填充上传时间
     */
    @PrePersist
    public void prePersist() {
        if (this.uploadTime == null) {
            this.uploadTime = LocalDateTime.now();
        }
    }
}
