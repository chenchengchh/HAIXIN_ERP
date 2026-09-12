package com.hxcoe.qms.service.impl;

import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import com.hxcoe.qms.entity.CapaEntity;
import com.hxcoe.qms.repository.CapaRepository;
import com.hxcoe.qms.service.CapaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * CAPA服务实现
 */
@Service
public class CapaServiceImpl implements CapaService {

    @Autowired
    private CapaRepository capaRepository;

    /**
     * 分页查询CAPA
     *
     * @param reportNo 报告编号（模糊）
     * @param pageable 分页参数
     * @return 分页结果
     */
    @Override
    public Result<PageResult<CapaEntity>> page(String reportNo, Pageable pageable) {
        Specification<CapaEntity> specification = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (reportNo != null && !reportNo.isBlank()) {
                predicates.add(cb.like(root.get("reportNo"), "%" + reportNo.trim() + "%"));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<CapaEntity> page = capaRepository.findAll(specification, pageable);
        PageResult<CapaEntity> pageResult = PageResult.build(page.getTotalElements(), page.getSize(), page.getNumber() + 1, page.getContent());
        return Result.success(pageResult);
    }

    /**
     * 获取CAPA详情
     *
     * @param id 措施ID
     * @return 详情
     */
    @Override
    public Result<CapaEntity> getById(Long id) {
        CapaEntity entity = capaRepository.findById(id).orElse(null);
        if (entity == null) {
            return Result.error("CAPA不存在");
        }
        return Result.success(entity);
    }

    /**
     * 创建CAPA
     *
     * @param entity 措施数据
     * @return 创建结果
     */
    @Override
    public Result<CapaEntity> create(CapaEntity entity) {
        if (entity.getImplementationStatus() == null || entity.getImplementationStatus().isBlank()) {
            entity.setImplementationStatus("pending");
        }
        if (entity.getVerifyStatus() == null || entity.getVerifyStatus().isBlank()) {
            entity.setVerifyStatus("pending");
        }
        entity.setCreatedTime(LocalDateTime.now());
        entity.setUpdatedTime(LocalDateTime.now());
        CapaEntity saved = capaRepository.save(entity);
        return Result.success(saved);
    }

    /**
     * 更新CAPA
     *
     * @param id 措施ID
     * @param entity 更新数据
     * @return 更新结果
     */
    @Override
    public Result<CapaEntity> update(Long id, CapaEntity entity) {
        CapaEntity existing = capaRepository.findById(id).orElse(null);
        if (existing == null) {
            return Result.error("CAPA不存在");
        }
        entity.setId(id);
        entity.setCreatedTime(existing.getCreatedTime());
        entity.setUpdatedTime(LocalDateTime.now());
        CapaEntity saved = capaRepository.save(entity);
        return Result.success(saved);
    }

    /**
     * 审核CAPA
     *
     * @param id 措施ID
     * @param reviewer 审核人
     * @param reviewResult 审核结果
     * @return 处理结果
     */
    @Override
    public Result<Void> review(Long id, String reviewer, String reviewResult) {
        CapaEntity entity = capaRepository.findById(id).orElse(null);
        if (entity == null) {
            return Result.error("CAPA不存在");
        }
        entity.setReviewer(reviewer);
        entity.setReviewResult(reviewResult);
        entity.setReviewDate(LocalDate.now());
        entity.setUpdatedTime(LocalDateTime.now());
        capaRepository.save(entity);
        return Result.success();
    }

    /**
     * 验证CAPA
     *
     * @param id 措施ID
     * @param verifyResult 验证结果描述
     * @param verifyStatus 验证状态
     * @return 处理结果
     */
    @Override
    public Result<Void> verify(Long id, String verifyResult, String verifyStatus) {
        CapaEntity entity = capaRepository.findById(id).orElse(null);
        if (entity == null) {
            return Result.error("CAPA不存在");
        }
        entity.setVerifyResult(verifyResult);
        entity.setVerifyStatus(verifyStatus);
        entity.setVerifyDate(LocalDate.now());
        entity.setUpdatedTime(LocalDateTime.now());
        capaRepository.save(entity);
        return Result.success();
    }
}
