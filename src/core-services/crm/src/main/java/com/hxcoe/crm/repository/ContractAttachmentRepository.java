package com.hxcoe.crm.repository;

import com.hxcoe.crm.entity.ContractAttachmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 合同附件Repository接口
 */
@Repository
public interface ContractAttachmentRepository extends JpaRepository<ContractAttachmentEntity, Long>, JpaSpecificationExecutor<ContractAttachmentEntity> {

    /**
     * 根据合同ID查询附件列表
     *
     * @param contractId 合同ID
     * @return 附件列表
     */
    List<ContractAttachmentEntity> findByContractId(Long contractId);
}
