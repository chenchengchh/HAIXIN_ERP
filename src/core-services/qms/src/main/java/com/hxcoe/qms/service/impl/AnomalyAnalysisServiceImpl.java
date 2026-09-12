package com.hxcoe.qms.service.impl;

import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import com.hxcoe.qms.entity.AnomalyAnalysisEntity;
import com.hxcoe.qms.repository.AnomalyAnalysisRepository;
import com.hxcoe.qms.service.AnomalyAnalysisService;
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
 * 质量异常分析服务实现
 */
@Service
public class AnomalyAnalysisServiceImpl implements AnomalyAnalysisService {

    @Autowired
    private AnomalyAnalysisRepository anomalyAnalysisRepository;

    /**
     * 分页查询异常分析
     *
     * @param reportNo 报告编号（模糊）
     * @param analysisStatus 分析状态
     * @param pageable 分页参数
     * @return 分页结果
     */
    @Override
    public Result<PageResult<AnomalyAnalysisEntity>> page(String reportNo, String analysisStatus, Pageable pageable) {
        Specification<AnomalyAnalysisEntity> specification = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (reportNo != null && !reportNo.isBlank()) {
                predicates.add(cb.like(root.get("reportNo"), "%" + reportNo.trim() + "%"));
            }
            if (analysisStatus != null && !analysisStatus.isBlank()) {
                predicates.add(cb.equal(root.get("analysisStatus"), analysisStatus.trim()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<AnomalyAnalysisEntity> page = anomalyAnalysisRepository.findAll(specification, pageable);
        PageResult<AnomalyAnalysisEntity> pageResult = PageResult.build(page.getTotalElements(), page.getSize(), page.getNumber() + 1, page.getContent());
        return Result.success(pageResult);
    }

    /**
     * 获取异常分析详情
     *
     * @param id 分析ID
     * @return 分析详情
     */
    @Override
    public Result<AnomalyAnalysisEntity> getById(Long id) {
        AnomalyAnalysisEntity entity = anomalyAnalysisRepository.findById(id).orElse(null);
        if (entity == null) {
            return Result.error("异常分析不存在");
        }
        return Result.success(entity);
    }

    /**
     * 创建异常分析
     *
     * @param entity 分析数据
     * @return 创建结果
     */
    @Override
    public Result<AnomalyAnalysisEntity> create(AnomalyAnalysisEntity entity) {
        if (entity.getAnalysisStatus() == null || entity.getAnalysisStatus().isBlank()) {
            entity.setAnalysisStatus("in_progress");
        }
        if (entity.getAnalysisTime() == null) {
            entity.setAnalysisTime(LocalDateTime.now());
        }
        entity.setCreatedTime(LocalDateTime.now());
        entity.setUpdatedTime(LocalDateTime.now());
        AnomalyAnalysisEntity saved = anomalyAnalysisRepository.save(entity);
        return Result.success(saved);
    }

    /**
     * 更新异常分析
     *
     * @param id 分析ID
     * @param entity 更新数据
     * @return 更新结果
     */
    @Override
    public Result<AnomalyAnalysisEntity> update(Long id, AnomalyAnalysisEntity entity) {
        AnomalyAnalysisEntity existing = anomalyAnalysisRepository.findById(id).orElse(null);
        if (existing == null) {
            return Result.error("异常分析不存在");
        }
        entity.setId(id);
        entity.setCreatedTime(existing.getCreatedTime());
        entity.setUpdatedTime(LocalDateTime.now());
        AnomalyAnalysisEntity saved = anomalyAnalysisRepository.save(entity);
        return Result.success(saved);
    }

    /**
     * 完成异常分析
     *
     * @param id 分析ID
     * @return 处理结果
     */
    @Override
    public Result<Void> complete(Long id) {
        AnomalyAnalysisEntity entity = anomalyAnalysisRepository.findById(id).orElse(null);
        if (entity == null) {
            return Result.error("异常分析不存在");
        }
        entity.setAnalysisStatus("completed");
        entity.setUpdatedTime(LocalDateTime.now());
        anomalyAnalysisRepository.save(entity);
        return Result.success();
    }
}
