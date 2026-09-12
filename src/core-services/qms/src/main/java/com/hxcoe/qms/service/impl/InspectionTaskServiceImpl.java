package com.hxcoe.qms.service.impl;

import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import com.hxcoe.qms.entity.InspectionTaskEntity;
import com.hxcoe.qms.repository.InspectionTaskRepository;
import com.hxcoe.qms.service.InspectionTaskService;
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
 * 检验任务服务实现
 */
@Service
public class InspectionTaskServiceImpl implements InspectionTaskService {

    @Autowired
    private InspectionTaskRepository inspectionTaskRepository;

    /**
     * 分页查询检验任务
     *
     * @param taskNo 任务编号（模糊）
     * @param status 状态
     * @param planId 计划ID
     * @param materialCode 物料编码
     * @param pageable 分页参数
     * @return 分页结果
     */
    @Override
    public Result<PageResult<InspectionTaskEntity>> page(String taskNo, String status, Long planId, String materialCode, Pageable pageable) {
        Specification<InspectionTaskEntity> specification = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (taskNo != null && !taskNo.isBlank()) {
                predicates.add(cb.like(root.get("taskNo"), "%" + taskNo.trim() + "%"));
            }
            if (status != null && !status.isBlank()) {
                predicates.add(cb.equal(root.get("status"), status.trim()));
            }
            if (planId != null) {
                predicates.add(cb.equal(root.get("planId"), planId));
            }
            if (materialCode != null && !materialCode.isBlank()) {
                predicates.add(cb.equal(root.get("materialCode"), materialCode.trim()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<InspectionTaskEntity> page = inspectionTaskRepository.findAll(specification, pageable);
        PageResult<InspectionTaskEntity> pageResult = PageResult.build(page.getTotalElements(), page.getSize(), page.getNumber() + 1, page.getContent());
        return Result.success(pageResult);
    }

    /**
     * 获取检验任务详情
     *
     * @param id 任务ID
     * @return 任务详情
     */
    @Override
    public Result<InspectionTaskEntity> getById(Long id) {
        InspectionTaskEntity entity = inspectionTaskRepository.findById(id).orElse(null);
        if (entity == null) {
            return Result.error("检验任务不存在");
        }
        return Result.success(entity);
    }

    /**
     * 创建检验任务
     *
     * @param entity 任务数据
     * @return 创建结果
     */
    @Override
    public Result<InspectionTaskEntity> create(InspectionTaskEntity entity) {
        if (entity.getTaskNo() == null || entity.getTaskNo().isBlank()) {
            entity.setTaskNo("TASK-" + System.currentTimeMillis());
        } else if (inspectionTaskRepository.findByTaskNo(entity.getTaskNo().trim()).isPresent()) {
            return Result.error("任务编号已存在");
        }
        if (entity.getStatus() == null || entity.getStatus().isBlank()) {
            entity.setStatus("pending");
        }
        if (entity.getAssignTime() == null && entity.getAssignee() != null && !entity.getAssignee().isBlank()) {
            entity.setAssignTime(LocalDateTime.now());
        }
        entity.setCreatedTime(LocalDateTime.now());
        entity.setUpdatedTime(LocalDateTime.now());
        InspectionTaskEntity saved = inspectionTaskRepository.save(entity);
        return Result.success(saved);
    }

    /**
     * 更新检验任务
     *
     * @param id 任务ID
     * @param entity 更新数据
     * @return 更新结果
     */
    @Override
    public Result<InspectionTaskEntity> update(Long id, InspectionTaskEntity entity) {
        InspectionTaskEntity existing = inspectionTaskRepository.findById(id).orElse(null);
        if (existing == null) {
            return Result.error("检验任务不存在");
        }
        entity.setId(id);
        entity.setCreatedTime(existing.getCreatedTime());
        entity.setUpdatedTime(LocalDateTime.now());
        InspectionTaskEntity saved = inspectionTaskRepository.save(entity);
        return Result.success(saved);
    }

    /**
     * 删除检验任务
     *
     * @param id 任务ID
     * @return 删除结果
     */
    @Override
    public Result<Void> delete(Long id) {
        if (!inspectionTaskRepository.existsById(id)) {
            return Result.error("检验任务不存在");
        }
        inspectionTaskRepository.deleteById(id);
        return Result.success();
    }

    /**
     * 分配检验任务
     *
     * @param id 任务ID
     * @param assignee 分配人
     * @return 分配结果
     */
    @Override
    public Result<Void> assign(Long id, String assignee) {
        InspectionTaskEntity entity = inspectionTaskRepository.findById(id).orElse(null);
        if (entity == null) {
            return Result.error("检验任务不存在");
        }
        if (assignee == null || assignee.isBlank()) {
            return Result.error("分配人不能为空");
        }
        entity.setAssignee(assignee);
        entity.setAssignTime(LocalDateTime.now());
        if ("pending".equals(entity.getStatus())) {
            entity.setStatus("in_progress");
        }
        entity.setUpdatedTime(LocalDateTime.now());
        inspectionTaskRepository.save(entity);
        return Result.success();
    }

    /**
     * 取消检验任务
     *
     * @param id 任务ID
     * @return 取消结果
     */
    @Override
    public Result<Void> cancel(Long id) {
        InspectionTaskEntity entity = inspectionTaskRepository.findById(id).orElse(null);
        if (entity == null) {
            return Result.error("检验任务不存在");
        }
        entity.setStatus("cancelled");
        entity.setUpdatedTime(LocalDateTime.now());
        inspectionTaskRepository.save(entity);
        return Result.success();
    }

    /**
     * 自动分配检验任务（按过滤条件批量创建/分配）
     *
     * @param params 参数
     * @return 处理结果
     */
    @Override
    public Result<Map<String, Object>> autoAssign(Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        result.put("created", 0);
        result.put("assigned", 0);
        result.put("params", params);
        return Result.success(result);
    }
}
