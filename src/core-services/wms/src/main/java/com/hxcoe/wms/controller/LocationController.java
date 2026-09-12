package com.hxcoe.wms.controller;

import com.hxcoe.common.api.ApiResponse;
import com.hxcoe.common.result.PageResult;
import com.hxcoe.wms.entity.LocationEntity;
import com.hxcoe.wms.entity.WarehouseEntity;
import com.hxcoe.wms.entity.ZoneEntity;
import com.hxcoe.wms.repository.LocationRepository;
import com.hxcoe.wms.repository.WarehouseRepository;
import com.hxcoe.wms.repository.ZoneRepository;
import com.hxcoe.wms.service.MasterDataEventOutboxService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.*;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/api/v1/wms/base/locations", "/wms/base/locations", "/api/wms/base/locations"})
public class LocationController {

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    private ZoneRepository zoneRepository;

    @Autowired
    private MasterDataEventOutboxService masterDataEventOutboxService;

    @GetMapping
    public ApiResponse<PageResult<Map<String, Object>>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String warehouseCode,
            @RequestParam(required = false) String zoneCode,
            @RequestParam(required = false) String locationTypeCode,
            @RequestParam(required = false) String status
    ) {
        Pageable pageable = PageRequest.of(Math.max(page, 1) - 1, Math.max(size, 1), Sort.by(Sort.Direction.DESC, "updatedTime"));
        Specification<LocationEntity> spec = (root, query, cb) -> {
            // Hibernate 6下cb.conjunction()+getExpressions().add()会静默失效，使用List<Predicate>+cb.and()
            List<Predicate> predicates = new ArrayList<>();
            if (keyword != null && !keyword.isBlank()) {
                String k = "%" + keyword.trim() + "%";
                predicates.add(cb.or(
                        cb.like(root.get("locationCode"), k),
                        cb.like(root.get("locationName"), k)
                ));
            }
            if (warehouseCode != null && !warehouseCode.isBlank()) {
                predicates.add(cb.equal(root.get("warehouseCode"), warehouseCode.trim()));
            }
            if (zoneCode != null && !zoneCode.isBlank()) {
                predicates.add(cb.equal(root.get("zoneCode"), zoneCode.trim()));
            }
            if (locationTypeCode != null && !locationTypeCode.isBlank()) {
                predicates.add(cb.equal(root.get("locationTypeCode"), locationTypeCode.trim()));
            }
            if (status != null && !status.isBlank()) {
                predicates.add(cb.equal(root.get("status"), status.trim()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<LocationEntity> result = locationRepository.findAll(spec, pageable);
        List<Map<String, Object>> rows = result.getContent().stream().map(this::toRow).toList();
        return success("库位列表查询成功", PageResult.build(result.getTotalElements(), result.getSize(), result.getNumber() + 1, rows));
    }

    @GetMapping("/{id}")
    public ApiResponse<Map<String, Object>> detail(@PathVariable Long id) {
        LocationEntity entity = locationRepository.findById(id).orElse(null);
        if (entity == null) {
            return notFound("库位不存在");
        }
        return success("库位查询成功", toRow(entity));
    }

    @PostMapping
    public ApiResponse<Map<String, Object>> create(@RequestBody LocationEntity entity) {
        LocationEntity saved = locationRepository.save(entity);
        masterDataEventOutboxService.enqueueLocationCreatedOrUpdated(saved, "LOCATION_CREATED");
        return success("库位创建成功", toRow(saved));
    }

    @PostMapping("/batch")
    public ApiResponse<List<Map<String, Object>>> batchCreate(@RequestBody List<LocationEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            return success("批量创建成功", List.of());
        }
        List<LocationEntity> saved = locationRepository.saveAll(entities);
        for (LocationEntity e : saved) {
            masterDataEventOutboxService.enqueueLocationCreatedOrUpdated(e, "LOCATION_CREATED");
        }
        List<Map<String, Object>> rows = saved.stream().map(this::toRow).toList();
        return success("批量创建成功", rows);
    }

    @PutMapping("/{id}")
    public ApiResponse<Map<String, Object>> update(@PathVariable Long id, @RequestBody LocationEntity body) {
        LocationEntity entity = locationRepository.findById(id).orElse(null);
        if (entity == null) {
            return notFound("库位不存在");
        }
        entity.setWarehouseCode(body.getWarehouseCode());
        entity.setZoneCode(body.getZoneCode());
        entity.setLocationName(body.getLocationName());
        entity.setLocationTypeCode(body.getLocationTypeCode());
        entity.setStatus(body.getStatus());
        entity.setRemark(body.getRemark());
        LocationEntity saved = locationRepository.save(entity);
        masterDataEventOutboxService.enqueueLocationCreatedOrUpdated(saved, "LOCATION_UPDATED");
        return success("库位更新成功", toRow(saved));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        LocationEntity existing = locationRepository.findById(id).orElse(null);
        locationRepository.deleteById(id);
        if (existing != null) {
            masterDataEventOutboxService.enqueueLocationDeleted(existing.getLocationCode());
        }
        return success("库位删除成功", null);
    }

    @PutMapping("/{id}/status")
    public ApiResponse<Map<String, Object>> updateStatus(@PathVariable Long id, @RequestParam String status) {
        LocationEntity entity = locationRepository.findById(id).orElse(null);
        if (entity == null) {
            return notFound("库位不存在");
        }
        entity.setStatus(status);
        LocationEntity saved = locationRepository.save(entity);
        masterDataEventOutboxService.enqueueLocationCreatedOrUpdated(saved, "LOCATION_STATUS_CHANGED");
        return success("库位状态更新成功", toRow(saved));
    }

    @GetMapping("/generate-codes")
    public ApiResponse<List<String>> generateCodes(
            @RequestParam(required = false) String prefix,
            @RequestParam(defaultValue = "10") int count
    ) {
        int n = count <= 0 ? 10 : Math.min(count, 200);
        String p = prefix == null ? "LOC" : prefix.trim();
        List<String> codes = new ArrayList<>();
        for (int i = 1; i <= n; i++) {
            codes.add(p + "-" + String.format("%04d", i));
        }
        return success("生成库位码成功", codes);
    }

    @PostMapping("/print-codes")
    public ApiResponse<Map<String, Object>> printCodes(@RequestBody Map<String, Object> body) {
        Object codesObj = body == null ? null : body.get("codes");
        int count = 0;
        if (codesObj instanceof List<?> list) {
            count = list.size();
        }
        Map<String, Object> data = new HashMap<>();
        data.put("printed", count);
        data.put("time", format(LocalDateTime.now()));
        return success("打印任务已提交", data);
    }

    private Map<String, Object> toRow(LocationEntity l) {
        WarehouseEntity wh = l.getWarehouseCode() == null ? null : warehouseRepository.findByWarehouseCode(l.getWarehouseCode());
        String warehouseName = wh == null ? (l.getWarehouseCode() == null ? "" : l.getWarehouseCode()) : wh.getWarehouseName();
        ZoneEntity zone = l.getZoneCode() == null ? null : zoneRepository.findByZoneCode(l.getZoneCode());
        String zoneName = zone == null ? (l.getZoneCode() == null ? "" : l.getZoneCode()) : zone.getZoneName();
        Map<String, Object> row = new HashMap<>();
        row.put("id", l.getId());
        row.put("locationCode", l.getLocationCode());
        row.put("locationName", l.getLocationName());
        row.put("warehouseId", l.getWarehouseCode());
        row.put("warehouseCode", l.getWarehouseCode());
        row.put("warehouseName", warehouseName);
        row.put("zoneCode", l.getZoneCode());
        row.put("zoneName", zoneName);
        row.put("locationTypeCode", l.getLocationTypeCode());
        row.put("status", l.getStatus());
        row.put("remark", l.getRemark());
        row.put("createdAt", format(l.getCreatedTime()));
        row.put("updatedAt", format(l.getUpdatedTime()));
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
