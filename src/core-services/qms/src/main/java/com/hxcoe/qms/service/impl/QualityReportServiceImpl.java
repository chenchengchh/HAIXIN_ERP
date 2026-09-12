package com.hxcoe.qms.service.impl;

import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import com.hxcoe.qms.entity.QualityReportEntity;
import com.hxcoe.qms.repository.QualityReportRepository;
import com.hxcoe.qms.service.QualityReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 质量报告服务实现
 */
@Service
public class QualityReportServiceImpl implements QualityReportService {

    @Autowired
    private QualityReportRepository qualityReportRepository;

    /**
     * 分页查询质量报告
     *
     * @param reportNo 报告编号（模糊）
     * @param status 状态
     * @param pageable 分页参数
     * @return 分页结果
     */
    @Override
    public Result<PageResult<QualityReportEntity>> page(String reportNo, String status, Pageable pageable) {
        Specification<QualityReportEntity> specification = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (reportNo != null && !reportNo.isBlank()) {
                predicates.add(cb.like(root.get("reportNo"), "%" + reportNo.trim() + "%"));
            }
            if (status != null && !status.isBlank()) {
                predicates.add(cb.equal(root.get("status"), status.trim()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<QualityReportEntity> page = qualityReportRepository.findAll(specification, pageable);
        PageResult<QualityReportEntity> pageResult = PageResult.build(page.getTotalElements(), page.getSize(), page.getNumber() + 1, page.getContent());
        return Result.success(pageResult);
    }

    /**
     * 获取质量报告详情
     *
     * @param id 报告ID
     * @return 报告详情
     */
    @Override
    public Result<QualityReportEntity> getById(Long id) {
        QualityReportEntity entity = qualityReportRepository.findById(id).orElse(null);
        if (entity == null) {
            return Result.error("质量报告不存在");
        }
        return Result.success(entity);
    }

    /**
     * 创建质量报告
     *
     * @param entity 报告数据
     * @return 创建结果
     */
    @Override
    public Result<QualityReportEntity> create(QualityReportEntity entity) {
        if (entity.getReportNo() == null || entity.getReportNo().isBlank()) {
            entity.setReportNo("QR-" + System.currentTimeMillis());
        } else if (qualityReportRepository.findByReportNo(entity.getReportNo().trim()).isPresent()) {
            return Result.error("报告编号已存在");
        }
        if (entity.getStatus() == null || entity.getStatus().isBlank()) {
            entity.setStatus("draft");
        }
        if (entity.getCreateTime() == null) {
            entity.setCreateTime(LocalDateTime.now());
        }
        entity.setCreatedTime(LocalDateTime.now());
        entity.setUpdatedTime(LocalDateTime.now());
        QualityReportEntity saved = qualityReportRepository.save(entity);
        return Result.success(saved);
    }

    /**
     * 更新质量报告
     *
     * @param id 报告ID
     * @param entity 更新数据
     * @return 更新结果
     */
    @Override
    public Result<QualityReportEntity> update(Long id, QualityReportEntity entity) {
        QualityReportEntity existing = qualityReportRepository.findById(id).orElse(null);
        if (existing == null) {
            return Result.error("质量报告不存在");
        }
        entity.setId(id);
        entity.setCreatedTime(existing.getCreatedTime());
        entity.setUpdatedTime(LocalDateTime.now());
        QualityReportEntity saved = qualityReportRepository.save(entity);
        return Result.success(saved);
    }

    /**
     * 删除质量报告
     *
     * @param id 报告ID
     * @return 删除结果
     */
    @Override
    public Result<Void> delete(Long id) {
        if (!qualityReportRepository.existsById(id)) {
            return Result.error("质量报告不存在");
        }
        qualityReportRepository.deleteById(id);
        return Result.success();
    }

    /**
     * 生成质量报告
     *
     * @param params 生成参数
     * @return 生成结果
     */
    @Override
    public Result<QualityReportEntity> generate(Map<String, Object> params) {
        QualityReportEntity entity = new QualityReportEntity();
        entity.setReportNo("QR-" + System.currentTimeMillis());
        entity.setReportType(params == null ? null : String.valueOf(params.getOrDefault("reportType", "daily")));
        entity.setPeriod(params == null ? null : (params.get("period") == null ? null : String.valueOf(params.get("period"))));
        entity.setReportName("质量报告-" + entity.getReportType());
        entity.setStatus("draft");
        entity.setCreateTime(LocalDateTime.now());

        Map<String, Object> content = new HashMap<>();
        content.put("params", params);
        content.put("generatedAt", LocalDateTime.now().toString());
        entity.setContent(content);

        entity.setCreatedTime(LocalDateTime.now());
        entity.setUpdatedTime(LocalDateTime.now());
        QualityReportEntity saved = qualityReportRepository.save(entity);
        return Result.success(saved);
    }
}
