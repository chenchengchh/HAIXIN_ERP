package com.hxcoe.qms.service.impl;

import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import com.hxcoe.qms.entity.InspectionPlanEntity;
import com.hxcoe.qms.repository.InspectionPlanRepository;
import com.hxcoe.qms.service.InspectionPlanService;
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
 * 检验计划服务实现
 */
@Service
public class InspectionPlanServiceImpl implements InspectionPlanService {

    @Autowired
    private InspectionPlanRepository inspectionPlanRepository;

    /**
     * 分页查询检验计划
     *
     * @param planNo 计划编号（模糊）
     * @param planName 计划名称（模糊）
     * @param materialName 物料名称（模糊）
     * @param planType 计划类型
     * @param status 状态
     * @param pageable 分页参数
     * @return 分页结果
     */
    @Override
    public Result<PageResult<InspectionPlanEntity>> page(String planNo, String planName, String materialName, String planType, String status, Pageable pageable) {
        Specification<InspectionPlanEntity> specification = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (planNo != null && !planNo.isBlank()) {
                predicates.add(cb.like(root.get("planNo"), "%" + planNo.trim() + "%"));
            }
            if (planName != null && !planName.isBlank()) {
                predicates.add(cb.like(root.get("planName"), "%" + planName.trim() + "%"));
            }
            if (materialName != null && !materialName.isBlank()) {
                predicates.add(cb.like(root.get("materialName"), "%" + materialName.trim() + "%"));
            }
            if (planType != null && !planType.isBlank()) {
                predicates.add(cb.equal(root.get("planType"), planType.trim()));
            }
            if (status != null && !status.isBlank()) {
                predicates.add(cb.equal(root.get("status"), status.trim()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<InspectionPlanEntity> page = inspectionPlanRepository.findAll(specification, pageable);
        PageResult<InspectionPlanEntity> pageResult = PageResult.build(page.getTotalElements(), page.getSize(), page.getNumber() + 1, page.getContent());
        return Result.success(pageResult);
    }

    /**
     * 获取检验计划详情
     *
     * @param id 计划ID
     * @return 计划详情
     */
    @Override
    public Result<InspectionPlanEntity> getById(Long id) {
        InspectionPlanEntity entity = inspectionPlanRepository.findById(id).orElse(null);
        if (entity == null) {
            return Result.error("检验计划不存在");
        }
        return Result.success(entity);
    }

    /**
     * 创建检验计划
     *
     * @param entity 计划数据
     * @return 创建结果
     */
    @Override
    public Result<InspectionPlanEntity> create(InspectionPlanEntity entity) {
        if (entity.getPlanNo() == null || entity.getPlanNo().isBlank()) {
            entity.setPlanNo("PLAN-" + System.currentTimeMillis());
        } else if (inspectionPlanRepository.findByPlanNo(entity.getPlanNo().trim()).isPresent()) {
            return Result.error("计划编号已存在");
        }

        if (entity.getStatus() == null || entity.getStatus().isBlank()) {
            entity.setStatus("draft");
        }
        entity.setCreatedTime(LocalDateTime.now());
        entity.setUpdatedTime(LocalDateTime.now());
        InspectionPlanEntity saved = inspectionPlanRepository.save(entity);
        return Result.success(saved);
    }

    /**
     * 更新检验计划
     *
     * @param id 计划ID
     * @param entity 更新数据
     * @return 更新结果
     */
    @Override
    public Result<InspectionPlanEntity> update(Long id, InspectionPlanEntity entity) {
        InspectionPlanEntity existing = inspectionPlanRepository.findById(id).orElse(null);
        if (existing == null) {
            return Result.error("检验计划不存在");
        }

        entity.setId(id);
        entity.setCreatedTime(existing.getCreatedTime());
        entity.setUpdatedTime(LocalDateTime.now());
        InspectionPlanEntity saved = inspectionPlanRepository.save(entity);
        return Result.success(saved);
    }

    /**
     * 删除检验计划
     *
     * @param id 计划ID
     * @return 删除结果
     */
    @Override
    public Result<Void> delete(Long id) {
        if (!inspectionPlanRepository.existsById(id)) {
            return Result.error("检验计划不存在");
        }
        inspectionPlanRepository.deleteById(id);
        return Result.success();
    }

    /**
     * 激活检验计划
     *
     * @param id 计划ID
     * @return 处理结果
     */
    @Override
    public Result<Void> activate(Long id) {
        InspectionPlanEntity entity = inspectionPlanRepository.findById(id).orElse(null);
        if (entity == null) {
            return Result.error("检验计划不存在");
        }
        entity.setStatus("effective");
        entity.setUpdatedTime(LocalDateTime.now());
        inspectionPlanRepository.save(entity);
        return Result.success();
    }

    /**
     * 失效检验计划
     *
     * @param id 计划ID
     * @return 处理结果
     */
    @Override
    public Result<Void> invalidate(Long id) {
        InspectionPlanEntity entity = inspectionPlanRepository.findById(id).orElse(null);
        if (entity == null) {
            return Result.error("检验计划不存在");
        }
        entity.setStatus("invalid");
        entity.setUpdatedTime(LocalDateTime.now());
        inspectionPlanRepository.save(entity);
        return Result.success();
    }
}
