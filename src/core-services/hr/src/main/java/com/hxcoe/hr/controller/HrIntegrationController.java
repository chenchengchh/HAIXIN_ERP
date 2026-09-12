package com.hxcoe.hr.controller;

import com.hxcoe.common.api.ApiResponse;
import com.hxcoe.common.result.PageResult;
import com.hxcoe.hr.entity.IntegrationOutboxEntity;
import com.hxcoe.hr.repository.IntegrationOutboxRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * HR 集成事件查询控制器（P2-C 闭环前端查询入口）。
 *
 * <p>提供对 hr_integration_outbox 表的只读分页查询，供前端 F5（HR事件查询页）调用。
 * 支持按事件类型、投递状态、业务引用号筛选，结果按创建时间倒序返回。
 *
 * <p>路径遵循 RESTful 规范：{@code GET /api/v1/hr/integration/events}
 */
@RestController
@RequestMapping({"/api/v1/hr/integration", "/hr/integration"})
public class HrIntegrationController {

    @Autowired
    private IntegrationOutboxRepository integrationOutboxRepository;

    /**
     * 分页查询 HR 集成事件（员工入职/调动/离职 Outbox 记录）。
     *
     * <p>所有筛选条件均为可选；未传任何条件时返回全量分页（按创建时间倒序）。
     *
     * @param eventType 事件类型（精确匹配，可空，如 HR_EMPLOYEE_ONBOARDED_OA）
     * @param status    投递状态（精确匹配，可空，PENDING/SENT/FAILED）
     * @param refNo     业务引用号（模糊匹配，可空，员工编号或记录ID）
     * @param page      页码（从 1 开始，默认 1）
     * @param size      每页条数（默认 10）
     * @return 集成事件分页结果
     */
    @GetMapping("/events")
    public ApiResponse<PageResult<IntegrationOutboxEntity>> listEvents(
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String refNo,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        // 前端 page 从 1 开始，Spring Data Pageable 从 0 开始，需 -1 转换
        int safePage = page == null || page < 1 ? 0 : page - 1;
        int safeSize = size == null || size < 1 ? 10 : size;
        // 创建时间倒序，便于查看最新事件
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdTime"));

        // 动态拼接查询条件
        Specification<IntegrationOutboxEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (eventType != null && !eventType.isBlank()) {
                predicates.add(cb.equal(root.get("eventType"), eventType.trim()));
            }
            if (status != null && !status.isBlank()) {
                predicates.add(cb.equal(root.get("status"), status.trim()));
            }
            if (refNo != null && !refNo.isBlank()) {
                predicates.add(cb.like(root.get("refNo"), "%" + refNo.trim() + "%"));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<IntegrationOutboxEntity> pageResult = integrationOutboxRepository.findAll(spec, pageable);

        // Pageable 的 page 从 0 开始，前端从 1 开始，需 +1 转换
        PageResult<IntegrationOutboxEntity> result = PageResult.build(
                pageResult.getTotalElements(),
                pageResult.getSize(),
                pageResult.getNumber() + 1,
                pageResult.getContent());

        return ApiResponse.success("成功", result);
    }
}
