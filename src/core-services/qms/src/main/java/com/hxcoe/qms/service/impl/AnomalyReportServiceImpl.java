package com.hxcoe.qms.service.impl;

import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import com.hxcoe.qms.entity.AnomalyReportEntity;
import com.hxcoe.qms.repository.AnomalyReportRepository;
import com.hxcoe.qms.service.AnomalyReportService;
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
 * 质量异常报告服务实现
 */
@Service
public class AnomalyReportServiceImpl implements AnomalyReportService {

    @Autowired
    private AnomalyReportRepository anomalyReportRepository;

    /**
     * 分页查询异常报告
     *
     * @param reportNo 报告编号（模糊）
     * @param status 状态
     * @param pageable 分页参数
     * @return 分页结果
     */
    @Override
    public Result<PageResult<AnomalyReportEntity>> page(String reportNo, String status, Pageable pageable) {
        Specification<AnomalyReportEntity> specification = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (reportNo != null && !reportNo.isBlank()) {
                predicates.add(cb.like(root.get("reportNo"), "%" + reportNo.trim() + "%"));
            }
            if (status != null && !status.isBlank()) {
                predicates.add(cb.equal(root.get("status"), status.trim()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<AnomalyReportEntity> page = anomalyReportRepository.findAll(specification, pageable);
        PageResult<AnomalyReportEntity> pageResult = PageResult.build(page.getTotalElements(), page.getSize(), page.getNumber() + 1, page.getContent());
        return Result.success(pageResult);
    }

    /**
     * 获取异常报告详情
     *
     * @param id 报告ID
     * @return 报告详情
     */
    @Override
    public Result<AnomalyReportEntity> getById(Long id) {
        AnomalyReportEntity entity = anomalyReportRepository.findById(id).orElse(null);
        if (entity == null) {
            return Result.error("异常报告不存在");
        }
        return Result.success(entity);
    }

    /**
     * 创建异常报告
     *
     * @param entity 报告数据
     * @return 创建结果
     */
    @Override
    public Result<AnomalyReportEntity> create(AnomalyReportEntity entity) {
        if (entity.getReportNo() == null || entity.getReportNo().isBlank()) {
            entity.setReportNo("AR-" + System.currentTimeMillis());
        } else if (anomalyReportRepository.findByReportNo(entity.getReportNo().trim()).isPresent()) {
            return Result.error("报告编号已存在");
        }
        if (entity.getStatus() == null || entity.getStatus().isBlank()) {
            entity.setStatus("pending");
        }
        if (entity.getOccurrenceTime() == null) {
            entity.setOccurrenceTime(LocalDateTime.now());
        }
        if (entity.getReportTime() == null) {
            entity.setReportTime(LocalDateTime.now());
        }
        entity.setCreatedTime(LocalDateTime.now());
        entity.setUpdatedTime(LocalDateTime.now());
        AnomalyReportEntity saved = anomalyReportRepository.save(entity);
        return Result.success(saved);
    }

    /**
     * 更新异常报告
     *
     * @param id 报告ID
     * @param entity 更新数据
     * @return 更新结果
     */
    @Override
    public Result<AnomalyReportEntity> update(Long id, AnomalyReportEntity entity) {
        AnomalyReportEntity existing = anomalyReportRepository.findById(id).orElse(null);
        if (existing == null) {
            return Result.error("异常报告不存在");
        }
        entity.setId(id);
        entity.setCreatedTime(existing.getCreatedTime());
        entity.setUpdatedTime(LocalDateTime.now());
        AnomalyReportEntity saved = anomalyReportRepository.save(entity);
        return Result.success(saved);
    }

    /**
     * 删除异常报告
     *
     * @param id 报告ID
     * @return 删除结果
     */
    @Override
    public Result<Void> delete(Long id) {
        if (!anomalyReportRepository.existsById(id)) {
            return Result.error("异常报告不存在");
        }
        anomalyReportRepository.deleteById(id);
        return Result.success();
    }

    /**
     * 开始调查
     *
     * @param id 报告ID
     * @return 处理结果
     */
    @Override
    public Result<Void> startInvestigation(Long id) {
        AnomalyReportEntity entity = anomalyReportRepository.findById(id).orElse(null);
        if (entity == null) {
            return Result.error("异常报告不存在");
        }
        entity.setStatus("investigating");
        entity.setUpdatedTime(LocalDateTime.now());
        anomalyReportRepository.save(entity);
        return Result.success();
    }

    /**
     * 解决异常
     *
     * @param id 报告ID
     * @return 处理结果
     */
    @Override
    public Result<Void> resolve(Long id) {
        AnomalyReportEntity entity = anomalyReportRepository.findById(id).orElse(null);
        if (entity == null) {
            return Result.error("异常报告不存在");
        }
        entity.setStatus("resolved");
        entity.setUpdatedTime(LocalDateTime.now());
        anomalyReportRepository.save(entity);
        return Result.success();
    }

    /**
     * 关闭异常
     *
     * @param id 报告ID
     * @return 处理结果
     */
    @Override
    public Result<Void> close(Long id) {
        AnomalyReportEntity entity = anomalyReportRepository.findById(id).orElse(null);
        if (entity == null) {
            return Result.error("异常报告不存在");
        }
        entity.setStatus("closed");
        entity.setUpdatedTime(LocalDateTime.now());
        anomalyReportRepository.save(entity);
        return Result.success();
    }
}
