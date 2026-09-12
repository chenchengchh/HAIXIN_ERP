package com.hxcoe.crm.service.impl;

import com.hxcoe.crm.entity.ContractAttachmentEntity;
import com.hxcoe.crm.repository.ContractAttachmentRepository;
import com.hxcoe.crm.service.ContractAttachmentService;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 合同附件服务实现类
 */
@Service
public class ContractAttachmentServiceImpl implements ContractAttachmentService {

    @Autowired
    private ContractAttachmentRepository attachmentRepository;

    /**
     * 登记附件元数据
     */
    @Override
    public ContractAttachmentEntity createAttachment(ContractAttachmentEntity attachment) {
        return attachmentRepository.save(attachment);
    }

    /**
     * 分页查询附件列表，支持文件名模糊匹配和合同ID过滤
     */
    @Override
    public Page<ContractAttachmentEntity> getAttachmentList(String keyword, Long contractId, Pageable pageable) {
        Specification<ContractAttachmentEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (keyword != null && !keyword.isEmpty()) {
                predicates.add(cb.like(root.get("fileName"), "%" + keyword + "%"));
            }
            if (contractId != null) {
                predicates.add(cb.equal(root.get("contractId"), contractId));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return attachmentRepository.findAll(spec, pageable);
    }

    /**
     * 根据ID查询附件
     */
    @Override
    public ContractAttachmentEntity getAttachmentById(Long id) {
        return attachmentRepository.findById(id).orElse(null);
    }

    /**
     * 删除附件，不存在时返回 false
     */
    @Override
    public boolean deleteAttachment(Long id) {
        ContractAttachmentEntity existing = attachmentRepository.findById(id).orElse(null);
        if (existing == null) {
            return false;
        }
        attachmentRepository.delete(existing);
        return true;
    }
}
