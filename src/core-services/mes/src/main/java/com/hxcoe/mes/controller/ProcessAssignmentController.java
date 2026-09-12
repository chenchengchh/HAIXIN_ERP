package com.hxcoe.mes.controller;

import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import com.hxcoe.mes.dto.ProcessAssignmentStatusUpdateRequest;
import com.hxcoe.mes.entity.ProcessAssignmentEntity;
import com.hxcoe.mes.repository.ProcessAssignmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping({"/mes/process-assignments", "/mes/v1/process-assignments", "/api/v1/mes/process-assignments", "/api/mes/process-assignments"})
public class ProcessAssignmentController {

    @Autowired
    private ProcessAssignmentRepository processAssignmentRepository;

    @PostMapping
    public Result<ProcessAssignmentEntity> create(@RequestBody ProcessAssignmentEntity entity) {
        LocalDateTime now = LocalDateTime.now();
        entity.setCreateTime(now);
        entity.setUpdateTime(now);
        if (entity.getStatus() == null || entity.getStatus().isBlank()) {
            entity.setStatus("assigned");
        }
        return Result.success("工序派工创建成功", processAssignmentRepository.save(entity));
    }

    @GetMapping("/list")
    public Result<PageResult<ProcessAssignmentEntity>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String workOrderNo,
            @RequestParam(required = false) String status) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Specification<ProcessAssignmentEntity> spec = (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
            if (workOrderNo != null && !workOrderNo.isBlank()) {
                predicates.add(cb.equal(root.get("workOrderNo"), workOrderNo));
            }
            if (status != null && !status.isBlank()) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
        Page<ProcessAssignmentEntity> assignmentPage = processAssignmentRepository.findAll(spec, pageable);
        return Result.success("工序派工列表查询成功", PageResult.build(assignmentPage.getTotalElements(), size, page, assignmentPage.getContent()));
    }

    @PutMapping("/{id}/status")
    public Result<ProcessAssignmentEntity> updateStatus(
            @PathVariable("id") Long id,
            @RequestBody(required = false) ProcessAssignmentStatusUpdateRequest body,
            @RequestParam(value = "status", required = false) String status) {
        String nextStatus = body != null && body.getStatus() != null ? body.getStatus() : status;
        if (nextStatus == null || nextStatus.isBlank()) {
            return Result.fail("status不能为空");
        }
        ProcessAssignmentEntity entity = processAssignmentRepository.findById(id).orElse(null);
        if (entity == null) {
            return Result.fail("工序派工不存在");
        }
        entity.setStatus(nextStatus);
        LocalDateTime now = LocalDateTime.now();
        if ("in_progress".equalsIgnoreCase(nextStatus) && entity.getStartTime() == null) {
            entity.setStartTime(now);
        }
        if ("completed".equalsIgnoreCase(nextStatus)) {
            entity.setEndTime(now);
        }
        entity.setUpdateTime(now);
        return Result.success("工序派工状态更新成功", processAssignmentRepository.save(entity));
    }
}
