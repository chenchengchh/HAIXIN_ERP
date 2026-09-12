package com.hxcoe.crm.service;

import com.hxcoe.crm.entity.ContractAttachmentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 合同附件服务接口
 */
public interface ContractAttachmentService {

    /**
     * 登记附件元数据
     *
     * @param attachment 附件实体
     * @return 创建后的附件
     */
    ContractAttachmentEntity createAttachment(ContractAttachmentEntity attachment);

    /**
     * 分页查询附件列表
     *
     * @param keyword    文件名关键词（可选）
     * @param contractId 合同ID（可选）
     * @param pageable   分页参数
     * @return 分页结果
     */
    Page<ContractAttachmentEntity> getAttachmentList(String keyword, Long contractId, Pageable pageable);

    /**
     * 根据ID查询附件
     *
     * @param id 附件ID
     * @return 附件
     */
    ContractAttachmentEntity getAttachmentById(Long id);

    /**
     * 删除附件
     *
     * @param id 附件ID
     * @return 是否删除成功
     */
    boolean deleteAttachment(Long id);
}
