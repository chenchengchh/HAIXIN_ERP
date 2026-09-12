package com.hxcoe.scada.controller;

import com.hxcoe.common.api.ApiResponse;
import com.hxcoe.scada.entity.ScadaCollectPointEntity;
import com.hxcoe.scada.repository.ScadaCollectPointRepository;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/scada/collect-points")
public class ScadaCollectPointController {

    private final ScadaCollectPointRepository repository;

    public ScadaCollectPointController(ScadaCollectPointRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public ApiResponse<List<ScadaCollectPointEntity>> list() {
        return ApiResponse.success(repository.findAll());
    }

    @PostMapping
    public ApiResponse<ScadaCollectPointEntity> create(@RequestBody ScadaCollectPointEntity payload) {
        payload.setId(null);
        payload.setCreatedTime(LocalDateTime.now());
        payload.setUpdatedTime(LocalDateTime.now());
        if (payload.getEnabled() == null) {
            payload.setEnabled(Boolean.TRUE);
        }
        if (payload.getStatus() == null || payload.getStatus().isBlank()) {
            payload.setStatus("online");
        }
        return ApiResponse.success("创建成功", repository.save(payload));
    }

    @PutMapping("/{id}")
    public ApiResponse<ScadaCollectPointEntity> update(@PathVariable Long id, @RequestBody ScadaCollectPointEntity payload) {
        ScadaCollectPointEntity existing = repository.findById(id).orElse(null);
        if (existing == null) {
            return ApiResponse.error(404, "采集点不存在");
        }
        payload.setId(id);
        payload.setCreatedTime(existing.getCreatedTime());
        payload.setUpdatedTime(LocalDateTime.now());
        return ApiResponse.success("更新成功", repository.save(payload));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            return ApiResponse.error(404, "采集点不存在");
        }
        repository.deleteById(id);
        return ApiResponse.success(null);
    }
}

