package com.hxcoe.scada.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hxcoe.common.api.ApiResponse;
import com.hxcoe.scada.entity.ScadaProtocolEntity;
import com.hxcoe.scada.repository.ScadaProtocolRepository;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/scada/protocols")
public class ScadaProtocolController {

    private final ScadaProtocolRepository repository;
    private final ObjectMapper objectMapper;

    public ScadaProtocolController(ScadaProtocolRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    public ApiResponse<List<ScadaProtocolEntity>> list() {
        return ApiResponse.success(repository.findAll());
    }

    @PostMapping
    public ApiResponse<ScadaProtocolEntity> create(@RequestBody ScadaProtocolEntity payload) {
        payload.setId(null);
        payload.setCreatedTime(LocalDateTime.now());
        payload.setUpdatedTime(LocalDateTime.now());
        if (payload.getEnabled() == null) {
            payload.setEnabled(Boolean.TRUE);
        }
        if (payload.getStatus() == null || payload.getStatus().isBlank()) {
            payload.setStatus(Boolean.TRUE.equals(payload.getEnabled()) ? "running" : "stopped");
        }
        return ApiResponse.success("创建成功", repository.save(payload));
    }

    @PutMapping("/{id}")
    public ApiResponse<ScadaProtocolEntity> update(@PathVariable Long id, @RequestBody ScadaProtocolEntity payload) {
        ScadaProtocolEntity existing = repository.findById(id).orElse(null);
        if (existing == null) {
            return ApiResponse.error(404, "协议不存在");
        }
        payload.setId(id);
        payload.setCreatedTime(existing.getCreatedTime());
        payload.setUpdatedTime(LocalDateTime.now());
        if (payload.getEnabled() == null) {
            payload.setEnabled(existing.getEnabled());
        }
        if (payload.getStatus() == null || payload.getStatus().isBlank()) {
            payload.setStatus(Boolean.TRUE.equals(payload.getEnabled()) ? "running" : "stopped");
        }
        if (payload.getConfigJson() == null) {
            payload.setConfigJson(existing.getConfigJson());
        }
        return ApiResponse.success("更新成功", repository.save(payload));
    }

    @PutMapping("/{id}/config")
    public ApiResponse<ScadaProtocolEntity> updateConfig(@PathVariable Long id, @RequestBody Map<String, Object> config) {
        ScadaProtocolEntity existing = repository.findById(id).orElse(null);
        if (existing == null) {
            return ApiResponse.error(404, "协议不存在");
        }
        try {
            existing.setConfigJson(objectMapper.writeValueAsString(config));
        } catch (JsonProcessingException e) {
            return ApiResponse.error(400, "配置格式无效");
        }
        existing.setUpdatedTime(LocalDateTime.now());
        return ApiResponse.success("保存成功", repository.save(existing));
    }

    @PatchMapping("/{id}/enabled")
    public ApiResponse<ScadaProtocolEntity> toggle(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        ScadaProtocolEntity existing = repository.findById(id).orElse(null);
        if (existing == null) {
            return ApiResponse.error(404, "协议不存在");
        }
        Object enabledRaw = body.get("enabled");
        boolean enabled = enabledRaw instanceof Boolean ? (Boolean) enabledRaw : Boolean.parseBoolean(String.valueOf(enabledRaw));
        existing.setEnabled(enabled);
        existing.setStatus(enabled ? "running" : "stopped");
        existing.setUpdatedTime(LocalDateTime.now());
        return ApiResponse.success("更新成功", repository.save(existing));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            return ApiResponse.error(404, "协议不存在");
        }
        repository.deleteById(id);
        return ApiResponse.success(null);
    }
}

