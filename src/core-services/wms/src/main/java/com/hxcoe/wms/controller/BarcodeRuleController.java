package com.hxcoe.wms.controller;

import com.hxcoe.common.api.ApiResponse;
import com.hxcoe.common.result.PageResult;
import com.hxcoe.wms.entity.BarcodeRuleEntity;
import com.hxcoe.wms.repository.BarcodeRuleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.*;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/api/v1/wms/base/barcode-rules", "/wms/base/barcode-rules", "/api/wms/base/barcode-rules"})
public class BarcodeRuleController {

    @Autowired
    private BarcodeRuleRepository barcodeRuleRepository;

    @GetMapping
    public ApiResponse<PageResult<Map<String, Object>>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status
    ) {
        Pageable pageable = PageRequest.of(Math.max(page, 1) - 1, Math.max(size, 1), Sort.by(Sort.Direction.DESC, "updatedTime"));
        Specification<BarcodeRuleEntity> spec = (root, query, cb) -> {
            // Hibernate 6下cb.conjunction()+getExpressions().add()会静默失效，使用List<Predicate>+cb.and()
            List<Predicate> predicates = new ArrayList<>();
            if (keyword != null && !keyword.isBlank()) {
                String k = "%" + keyword.trim() + "%";
                predicates.add(cb.or(
                        cb.like(root.get("ruleCode"), k),
                        cb.like(root.get("ruleName"), k)
                ));
            }
            if (status != null && !status.isBlank()) {
                predicates.add(cb.equal(root.get("status"), status.trim()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<BarcodeRuleEntity> result = barcodeRuleRepository.findAll(spec, pageable);
        List<Map<String, Object>> rows = result.getContent().stream().map(this::toRow).toList();
        return success("条码规则列表查询成功", PageResult.build(result.getTotalElements(), result.getSize(), result.getNumber() + 1, rows));
    }

    @PostMapping
    public ApiResponse<Map<String, Object>> create(@RequestBody BarcodeRuleEntity entity) {
        BarcodeRuleEntity saved = barcodeRuleRepository.save(entity);
        return success("条码规则创建成功", toRow(saved));
    }

    @PutMapping("/{id}")
    public ApiResponse<Map<String, Object>> update(@PathVariable Long id, @RequestBody BarcodeRuleEntity body) {
        BarcodeRuleEntity entity = barcodeRuleRepository.findById(id).orElse(null);
        if (entity == null) {
            return notFound("条码规则不存在");
        }
        entity.setRuleName(body.getRuleName());
        entity.setRuleType(body.getRuleType());
        entity.setRuleFormat(body.getRuleFormat());
        entity.setDescription(body.getDescription());
        entity.setStatus(body.getStatus());
        BarcodeRuleEntity saved = barcodeRuleRepository.save(entity);
        return success("条码规则更新成功", toRow(saved));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        barcodeRuleRepository.deleteById(id);
        return success("条码规则删除成功", null);
    }

    @PutMapping("/{id}/status")
    public ApiResponse<Map<String, Object>> updateStatus(@PathVariable Long id, @RequestParam String status) {
        BarcodeRuleEntity entity = barcodeRuleRepository.findById(id).orElse(null);
        if (entity == null) {
            return notFound("条码规则不存在");
        }
        entity.setStatus(status);
        BarcodeRuleEntity saved = barcodeRuleRepository.save(entity);
        return success("条码规则状态更新成功", toRow(saved));
    }

    private Map<String, Object> toRow(BarcodeRuleEntity r) {
        Map<String, Object> row = new HashMap<>();
        row.put("id", r.getId());
        row.put("ruleCode", r.getRuleCode());
        row.put("ruleName", r.getRuleName());
        row.put("ruleType", r.getRuleType());
        row.put("ruleFormat", r.getRuleFormat());
        row.put("description", r.getDescription());
        row.put("status", r.getStatus());
        row.put("createdAt", format(r.getCreatedTime()));
        row.put("updatedAt", format(r.getUpdatedTime()));
        return row;
    }

    private static String format(LocalDateTime t) {
        // 统一输出"yyyy-MM-dd HH:mm:ss"，避免toString()微秒非0时格式不一致
        return com.hxcoe.wms.util.WmsDateTimes.format(t);
    }

    private static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.success(message, data);
    }

    private static <T> ApiResponse<T> notFound(String message) {
        return ApiResponse.error(404, message);
    }
}

