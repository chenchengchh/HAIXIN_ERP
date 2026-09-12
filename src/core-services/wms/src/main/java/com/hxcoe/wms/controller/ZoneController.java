package com.hxcoe.wms.controller;

import com.hxcoe.common.api.ApiResponse;
import com.hxcoe.common.result.PageResult;
import com.hxcoe.wms.entity.WarehouseEntity;
import com.hxcoe.wms.entity.ZoneEntity;
import com.hxcoe.wms.repository.WarehouseRepository;
import com.hxcoe.wms.repository.ZoneRepository;
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
@RequestMapping({"/api/v1/wms/base/zones", "/wms/base/zones", "/api/wms/base/zones"})
public class ZoneController {

    @Autowired
    private ZoneRepository zoneRepository;

    @Autowired
    private WarehouseRepository warehouseRepository;

    @GetMapping
    public ApiResponse<PageResult<Map<String, Object>>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String warehouseCode,
            @RequestParam(required = false) String status
    ) {
        Pageable pageable = PageRequest.of(Math.max(page, 1) - 1, Math.max(size, 1), Sort.by(Sort.Direction.DESC, "updatedTime"));
        Specification<ZoneEntity> spec = (root, query, cb) -> {
            // Hibernate 6下cb.conjunction()+getExpressions().add()会静默失效，使用List<Predicate>+cb.and()
            List<Predicate> predicates = new ArrayList<>();
            if (keyword != null && !keyword.isBlank()) {
                String k = "%" + keyword.trim() + "%";
                predicates.add(cb.or(
                        cb.like(root.get("zoneCode"), k),
                        cb.like(root.get("zoneName"), k)
                ));
            }
            if (warehouseCode != null && !warehouseCode.isBlank()) {
                predicates.add(cb.equal(root.get("warehouseCode"), warehouseCode.trim()));
            }
            if (status != null && !status.isBlank()) {
                predicates.add(cb.equal(root.get("status"), status.trim()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<ZoneEntity> result = zoneRepository.findAll(spec, pageable);
        List<Map<String, Object>> rows = result.getContent().stream().map(this::toRow).toList();
        return success("库区列表查询成功", PageResult.build(result.getTotalElements(), result.getSize(), result.getNumber() + 1, rows));
    }

    @GetMapping("/{id}")
    public ApiResponse<Map<String, Object>> detail(@PathVariable Long id) {
        ZoneEntity entity = zoneRepository.findById(id).orElse(null);
        if (entity == null) {
            return notFound("库区不存在");
        }
        return success("库区查询成功", toRow(entity));
    }

    @PostMapping
    public ApiResponse<Map<String, Object>> create(@RequestBody ZoneEntity entity) {
        ZoneEntity saved = zoneRepository.save(entity);
        return success("库区创建成功", toRow(saved));
    }

    @PutMapping("/{id}")
    public ApiResponse<Map<String, Object>> update(@PathVariable Long id, @RequestBody ZoneEntity body) {
        ZoneEntity entity = zoneRepository.findById(id).orElse(null);
        if (entity == null) {
            return notFound("库区不存在");
        }
        entity.setWarehouseCode(body.getWarehouseCode());
        entity.setZoneName(body.getZoneName());
        entity.setZoneType(body.getZoneType());
        entity.setDescription(body.getDescription());
        entity.setStatus(body.getStatus());
        ZoneEntity saved = zoneRepository.save(entity);
        return success("库区更新成功", toRow(saved));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        zoneRepository.deleteById(id);
        return success("库区删除成功", null);
    }

    @PutMapping("/{id}/status")
    public ApiResponse<Map<String, Object>> updateStatus(@PathVariable Long id, @RequestParam String status) {
        ZoneEntity entity = zoneRepository.findById(id).orElse(null);
        if (entity == null) {
            return notFound("库区不存在");
        }
        entity.setStatus(status);
        ZoneEntity saved = zoneRepository.save(entity);
        return success("库区状态更新成功", toRow(saved));
    }

    private Map<String, Object> toRow(ZoneEntity z) {
        WarehouseEntity wh = z.getWarehouseCode() == null ? null : warehouseRepository.findByWarehouseCode(z.getWarehouseCode());
        String warehouseName = wh == null ? (z.getWarehouseCode() == null ? "" : z.getWarehouseCode()) : wh.getWarehouseName();
        Map<String, Object> row = new HashMap<>();
        row.put("id", z.getId());
        row.put("zoneCode", z.getZoneCode());
        row.put("zoneName", z.getZoneName());
        row.put("warehouseId", z.getWarehouseCode());
        row.put("warehouseCode", z.getWarehouseCode());
        row.put("warehouseName", warehouseName);
        row.put("zoneType", z.getZoneType());
        row.put("description", z.getDescription());
        row.put("status", z.getStatus());
        row.put("createdAt", format(z.getCreatedTime()));
        row.put("updatedAt", format(z.getUpdatedTime()));
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
