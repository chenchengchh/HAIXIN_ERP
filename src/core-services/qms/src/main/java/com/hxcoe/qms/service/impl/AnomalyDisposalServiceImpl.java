package com.hxcoe.qms.service.impl;

import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import com.hxcoe.qms.entity.AnomalyDisposalEntity;
import com.hxcoe.qms.repository.AnomalyDisposalRepository;
import com.hxcoe.qms.service.AnomalyDisposalService;
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
 * 质量异常处置服务实现
 */
@Service
public class AnomalyDisposalServiceImpl implements AnomalyDisposalService {

    @Autowired
    private AnomalyDisposalRepository anomalyDisposalRepository;

    /**
     * 分页查询异常处置
     *
     * @param reportNo 报告编号（模糊）
     * @param disposalStatus 处置状态
     * @param pageable 分页参数
     * @return 分页结果
     */
    @Override
    public Result<PageResult<AnomalyDisposalEntity>> page(String reportNo, String disposalStatus, Pageable pageable) {
        Specification<AnomalyDisposalEntity> specification = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (reportNo != null && !reportNo.isBlank()) {
                predicates.add(cb.like(root.get("reportNo"), "%" + reportNo.trim() + "%"));
            }
            if (disposalStatus != null && !disposalStatus.isBlank()) {
                predicates.add(cb.equal(root.get("disposalStatus"), disposalStatus.trim()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<AnomalyDisposalEntity> page = anomalyDisposalRepository.findAll(specification, pageable);
        PageResult<AnomalyDisposalEntity> pageResult = PageResult.build(page.getTotalElements(), page.getSize(), page.getNumber() + 1, page.getContent());
        return Result.success(pageResult);
    }

    /**
     * 获取异常处置详情
     *
     * @param id 处置ID
     * @return 处置详情
     */
    @Override
    public Result<AnomalyDisposalEntity> getById(Long id) {
        AnomalyDisposalEntity entity = anomalyDisposalRepository.findById(id).orElse(null);
        if (entity == null) {
            return Result.error("异常处置不存在");
        }
        return Result.success(entity);
    }

    /**
     * 创建异常处置
     *
     * @param entity 处置数据
     * @return 创建结果
     */
    @Override
    public Result<AnomalyDisposalEntity> create(AnomalyDisposalEntity entity) {
        if (entity.getDisposalStatus() == null || entity.getDisposalStatus().isBlank()) {
            entity.setDisposalStatus("pending");
        }
        entity.setCreatedTime(LocalDateTime.now());
        entity.setUpdatedTime(LocalDateTime.now());
        AnomalyDisposalEntity saved = anomalyDisposalRepository.save(entity);
        return Result.success(saved);
    }

    /**
     * 更新异常处置
     *
     * @param id 处置ID
     * @param entity 更新数据
     * @return 更新结果
     */
    @Override
    public Result<AnomalyDisposalEntity> update(Long id, AnomalyDisposalEntity entity) {
        AnomalyDisposalEntity existing = anomalyDisposalRepository.findById(id).orElse(null);
        if (existing == null) {
            return Result.error("异常处置不存在");
        }
        entity.setId(id);
        entity.setCreatedTime(existing.getCreatedTime());
        entity.setUpdatedTime(LocalDateTime.now());
        AnomalyDisposalEntity saved = anomalyDisposalRepository.save(entity);
        return Result.success(saved);
    }

    /**
     * 验证异常处置
     *
     * @param id 处置ID
     * @param verifier 验证人
     * @param verificationResult 验证结果
     * @return 处理结果
     */
    @Override
    public Result<Void> verify(Long id, String verifier, String verificationResult) {
        AnomalyDisposalEntity entity = anomalyDisposalRepository.findById(id).orElse(null);
        if (entity == null) {
            return Result.error("异常处置不存在");
        }
        entity.setVerifier(verifier);
        entity.setVerificationResult(verificationResult);
        entity.setVerificationTime(LocalDateTime.now());
        entity.setUpdatedTime(LocalDateTime.now());
        anomalyDisposalRepository.save(entity);
        return Result.success();
    }
}
