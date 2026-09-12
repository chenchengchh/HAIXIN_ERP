package com.hxcoe.wms.controller;

import com.hxcoe.common.api.ApiResponse;
import com.hxcoe.common.result.PageResult;
import com.hxcoe.wms.entity.MaterialTypeEntity;
import com.hxcoe.wms.repository.MaterialTypeRepository;
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
@RequestMapping({"/api/v1/wms/base/material-types", "/wms/base/material-types", "/api/wms/base/material-types"})
public class MaterialTypeController {

    @Autowired
    private MaterialTypeRepository materialTypeRepository;

    @GetMapping
    public ApiResponse<PageResult<Map<String, Object>>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status
    ) {
        Pageable pageable = PageRequest.of(Math.max(page, 1) - 1, Math.max(size, 1), Sort.by(Sort.Direction.DESC, "updatedTime"));
        Specification<MaterialTypeEntity> spec = (root, query, cb) -> {
            // Hibernate 6下cb.conjunction()+getExpressions().add()会静默失效，使用List<Predicate>+cb.and()
            List<Predicate> predicates = new ArrayList<>();
            if (keyword != null && !keyword.isBlank()) {
                String k = "%" + keyword.trim() + "%";
                predicates.add(cb.or(
                        cb.like(root.get("typeCode"), k),
                        cb.like(root.get("typeName"), k)
                ));
            }
            if (status != null && !status.isBlank()) {
                predicates.add(cb.equal(root.get("status"), status.trim()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<MaterialTypeEntity> result = materialTypeRepository.findAll(spec, pageable);
        List<Map<String, Object>> rows = result.getContent().stream().map(this::toRow).toList();
        return success("物料类型列表查询成功", PageResult.build(result.getTotalElements(), result.getSize(), result.getNumber() + 1, rows));
    }

    @PostMapping
    public ApiResponse<Map<String, Object>> create(@RequestBody MaterialTypeEntity entity) {
        MaterialTypeEntity saved = materialTypeRepository.save(entity);
        return success("物料类型创建成功", toRow(saved));
    }

    @PutMapping("/{id}")
    public ApiResponse<Map<String, Object>> update(@PathVariable Long id, @RequestBody MaterialTypeEntity body) {
        MaterialTypeEntity entity = materialTypeRepository.findById(id).orElse(null);
        if (entity == null) {
            return notFound("物料类型不存在");
        }
        entity.setTypeName(body.getTypeName());
        entity.setDescription(body.getDescription());
        entity.setStatus(body.getStatus());
        MaterialTypeEntity saved = materialTypeRepository.save(entity);
        return success("物料类型更新成功", toRow(saved));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        materialTypeRepository.deleteById(id);
        return success("物料类型删除成功", null);
    }

    @PutMapping("/{id}/status")
    public ApiResponse<Map<String, Object>> updateStatus(@PathVariable Long id, @RequestParam String status) {
        MaterialTypeEntity entity = materialTypeRepository.findById(id).orElse(null);
        if (entity == null) {
            return notFound("物料类型不存在");
        }
        entity.setStatus(status);
        MaterialTypeEntity saved = materialTypeRepository.save(entity);
        return success("物料类型状态更新成功", toRow(saved));
    }

    private Map<String, Object> toRow(MaterialTypeEntity t) {
        Map<String, Object> row = new HashMap<>();
        row.put("id", t.getId());
        row.put("typeCode", t.getTypeCode());
        row.put("typeName", t.getTypeName());
        row.put("description", t.getDescription());
        row.put("status", t.getStatus());
        row.put("createdAt", format(t.getCreatedTime()));
        row.put("updatedAt", format(t.getUpdatedTime()));
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

