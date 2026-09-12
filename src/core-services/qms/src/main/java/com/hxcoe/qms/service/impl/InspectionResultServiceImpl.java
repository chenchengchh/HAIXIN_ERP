package com.hxcoe.qms.service.impl;

import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import com.hxcoe.qms.entity.InspectionResultEntity;
import com.hxcoe.qms.repository.InspectionResultRepository;
import com.hxcoe.qms.service.InspectionResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 检验结果服务实现
 */
@Service
public class InspectionResultServiceImpl implements InspectionResultService {

    @Autowired
    private InspectionResultRepository inspectionResultRepository;

    /**
     * 分页查询检验结果
     *
     * @param resultNo 结果编号（模糊）
     * @param taskNo 任务编号（模糊）
     * @param auditStatus 审核状态
     * @param pageable 分页参数
     * @return 分页结果
     */
    @Override
    public Result<PageResult<InspectionResultEntity>> page(String resultNo, String taskNo, String auditStatus, Pageable pageable) {
        Specification<InspectionResultEntity> specification = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (resultNo != null && !resultNo.isBlank()) {
                predicates.add(cb.like(root.get("resultNo"), "%" + resultNo.trim() + "%"));
            }
            if (taskNo != null && !taskNo.isBlank()) {
                predicates.add(cb.like(root.get("taskNo"), "%" + taskNo.trim() + "%"));
            }
            if (auditStatus != null && !auditStatus.isBlank()) {
                predicates.add(cb.equal(root.get("auditStatus"), auditStatus.trim()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<InspectionResultEntity> page = inspectionResultRepository.findAll(specification, pageable);
        PageResult<InspectionResultEntity> pageResult = PageResult.build(page.getTotalElements(), page.getSize(), page.getNumber() + 1, page.getContent());
        return Result.success(pageResult);
    }

    /**
     * 获取检验结果详情
     *
     * @param id 结果ID
     * @return 结果详情
     */
    @Override
    public Result<InspectionResultEntity> getById(Long id) {
        InspectionResultEntity entity = inspectionResultRepository.findById(id).orElse(null);
        if (entity == null) {
            return Result.error("检验结果不存在");
        }
        return Result.success(entity);
    }

    /**
     * 创建检验结果
     *
     * @param entity 结果数据
     * @return 创建结果
     */
    @Override
    public Result<InspectionResultEntity> create(InspectionResultEntity entity) {
        if (entity.getResultNo() == null || entity.getResultNo().isBlank()) {
            entity.setResultNo("RES-" + System.currentTimeMillis());
        } else if (inspectionResultRepository.findByResultNo(entity.getResultNo().trim()).isPresent()) {
            return Result.error("结果编号已存在");
        }
        if (entity.getAuditStatus() == null || entity.getAuditStatus().isBlank()) {
            entity.setAuditStatus("pending");
        }
        if (entity.getInspectionTime() == null) {
            entity.setInspectionTime(LocalDateTime.now());
        }
        entity.setCreatedTime(LocalDateTime.now());
        entity.setUpdatedTime(LocalDateTime.now());
        InspectionResultEntity saved = inspectionResultRepository.save(entity);
        return Result.success(saved);
    }

    /**
     * 更新检验结果
     *
     * @param id 结果ID
     * @param entity 更新数据
     * @return 更新结果
     */
    @Override
    public Result<InspectionResultEntity> update(Long id, InspectionResultEntity entity) {
        InspectionResultEntity existing = inspectionResultRepository.findById(id).orElse(null);
        if (existing == null) {
            return Result.error("检验结果不存在");
        }
        entity.setId(id);
        entity.setCreatedTime(existing.getCreatedTime());
        entity.setUpdatedTime(LocalDateTime.now());
        InspectionResultEntity saved = inspectionResultRepository.save(entity);
        return Result.success(saved);
    }

    /**
     * 审核检验结果
     *
     * @param id 结果ID
     * @param auditStatus 审核状态
     * @param auditRemark 审核备注
     * @param auditor 审核人
     * @return 审核结果
     */
    @Override
    public Result<Void> audit(Long id, String auditStatus, String auditRemark, String auditor) {
        InspectionResultEntity entity = inspectionResultRepository.findById(id).orElse(null);
        if (entity == null) {
            return Result.error("检验结果不存在");
        }
        if (auditStatus == null || auditStatus.isBlank()) {
            return Result.error("审核状态不能为空");
        }
        entity.setAuditStatus(auditStatus);
        entity.setAuditRemark(auditRemark);
        entity.setAuditor(auditor);
        entity.setAuditTime(LocalDateTime.now());
        entity.setUpdatedTime(LocalDateTime.now());
        inspectionResultRepository.save(entity);
        return Result.success();
    }

    /**
     * 批量审核检验结果
     *
     * @param ids 结果ID列表
     * @param auditStatus 审核状态
     * @param auditRemark 审核备注
     * @return 处理结果
     */
    @Override
    public Result<Void> batchAudit(List<Long> ids, String auditStatus, String auditRemark) {
        if (ids == null || ids.isEmpty()) {
            return Result.error("审核ID列表不能为空");
        }
        for (Long id : ids) {
            audit(id, auditStatus, auditRemark, null);
        }
        return Result.success();
    }
}
