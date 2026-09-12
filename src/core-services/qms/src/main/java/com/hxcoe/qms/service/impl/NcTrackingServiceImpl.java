package com.hxcoe.qms.service.impl;

import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import com.hxcoe.qms.entity.NcTrackingEntity;
import com.hxcoe.qms.repository.NcTrackingRepository;
import com.hxcoe.qms.service.NcTrackingService;
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
 * 不合格品追踪服务实现
 */
@Service
public class NcTrackingServiceImpl implements NcTrackingService {

    @Autowired
    private NcTrackingRepository ncTrackingRepository;

    /**
     * 分页查询不合格品追踪
     *
     * @param registrationNo 登记编号（模糊）
     * @param trackingStatus 追踪状态
     * @param pageable 分页参数
     * @return 分页结果
     */
    @Override
    public Result<PageResult<NcTrackingEntity>> page(String registrationNo, String trackingStatus, Pageable pageable) {
        Specification<NcTrackingEntity> specification = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (registrationNo != null && !registrationNo.isBlank()) {
                predicates.add(cb.like(root.get("registrationNo"), "%" + registrationNo.trim() + "%"));
            }
            if (trackingStatus != null && !trackingStatus.isBlank()) {
                predicates.add(cb.equal(root.get("trackingStatus"), trackingStatus.trim()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<NcTrackingEntity> page = ncTrackingRepository.findAll(specification, pageable);
        PageResult<NcTrackingEntity> pageResult = PageResult.build(page.getTotalElements(), page.getSize(), page.getNumber() + 1, page.getContent());
        return Result.success(pageResult);
    }

    /**
     * 获取追踪详情
     *
     * @param id 追踪ID
     * @return 追踪详情
     */
    @Override
    public Result<NcTrackingEntity> getById(Long id) {
        NcTrackingEntity entity = ncTrackingRepository.findById(id).orElse(null);
        if (entity == null) {
            return Result.error("不合格品追踪不存在");
        }
        return Result.success(entity);
    }

    /**
     * 创建追踪
     *
     * @param entity 追踪数据
     * @return 创建结果
     */
    @Override
    public Result<NcTrackingEntity> create(NcTrackingEntity entity) {
        if (entity.getTrackingStatus() == null || entity.getTrackingStatus().isBlank()) {
            entity.setTrackingStatus("pending");
        }
        if (entity.getEffectiveness() == null || entity.getEffectiveness().isBlank()) {
            entity.setEffectiveness("pending");
        }
        if (entity.getTrackingTime() == null) {
            entity.setTrackingTime(LocalDateTime.now());
        }
        entity.setCreatedTime(LocalDateTime.now());
        entity.setUpdatedTime(LocalDateTime.now());
        NcTrackingEntity saved = ncTrackingRepository.save(entity);
        return Result.success(saved);
    }

    /**
     * 更新追踪
     *
     * @param id 追踪ID
     * @param entity 更新数据
     * @return 更新结果
     */
    @Override
    public Result<NcTrackingEntity> update(Long id, NcTrackingEntity entity) {
        NcTrackingEntity existing = ncTrackingRepository.findById(id).orElse(null);
        if (existing == null) {
            return Result.error("不合格品追踪不存在");
        }
        entity.setId(id);
        entity.setCreatedTime(existing.getCreatedTime());
        entity.setUpdatedTime(LocalDateTime.now());
        NcTrackingEntity saved = ncTrackingRepository.save(entity);
        return Result.success(saved);
    }

    /**
     * 开始追踪
     *
     * @param id 追踪ID
     * @return 处理结果
     */
    @Override
    public Result<Void> start(Long id) {
        NcTrackingEntity entity = ncTrackingRepository.findById(id).orElse(null);
        if (entity == null) {
            return Result.error("不合格品追踪不存在");
        }
        entity.setTrackingStatus("tracking");
        entity.setUpdatedTime(LocalDateTime.now());
        ncTrackingRepository.save(entity);
        return Result.success();
    }

    /**
     * 完成追踪
     *
     * @param id 追踪ID
     * @param effectiveness 效果评估
     * @param improvementSuggestions 改进建议
     * @return 处理结果
     */
    @Override
    public Result<Void> complete(Long id, String effectiveness, String improvementSuggestions) {
        NcTrackingEntity entity = ncTrackingRepository.findById(id).orElse(null);
        if (entity == null) {
            return Result.error("不合格品追踪不存在");
        }
        entity.setTrackingStatus("completed");
        entity.setEffectiveness(effectiveness);
        entity.setImprovementSuggestions(improvementSuggestions);
        entity.setUpdatedTime(LocalDateTime.now());
        ncTrackingRepository.save(entity);
        return Result.success();
    }
}
