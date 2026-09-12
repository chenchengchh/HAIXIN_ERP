package com.hxcoe.scada.controller;

import com.hxcoe.common.api.ApiResponse;
import com.hxcoe.scada.entity.ScadaAlarmRuleEntity;
import com.hxcoe.scada.repository.ScadaAlarmRuleRepository;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/scada/alarm-rules")
public class ScadaAlarmRuleController {

    private final ScadaAlarmRuleRepository repository;

    public ScadaAlarmRuleController(ScadaAlarmRuleRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public ApiResponse<List<ScadaAlarmRuleEntity>> list() {
        return ApiResponse.success(repository.findAll());
    }

    @PostMapping
    public ApiResponse<ScadaAlarmRuleEntity> create(@RequestBody ScadaAlarmRuleEntity payload) {
        payload.setId(null);
        payload.setCreatedTime(LocalDateTime.now());
        payload.setUpdatedTime(LocalDateTime.now());
        if (payload.getEnabled() == null) {
            payload.setEnabled(Boolean.TRUE);
        }
        if (payload.getSeverity() == null) {
            payload.setSeverity(2);
        }
        return ApiResponse.success("创建成功", repository.save(payload));
    }

    @PutMapping("/{id}")
    public ApiResponse<ScadaAlarmRuleEntity> update(@PathVariable Long id, @RequestBody ScadaAlarmRuleEntity payload) {
        ScadaAlarmRuleEntity existing = repository.findById(id).orElse(null);
        if (existing == null) {
            return ApiResponse.error(404, "报警规则不存在");
        }
        payload.setId(id);
        payload.setCreatedTime(existing.getCreatedTime());
        payload.setUpdatedTime(LocalDateTime.now());
        if (payload.getEnabled() == null) {
            payload.setEnabled(existing.getEnabled());
        }
        if (payload.getSeverity() == null) {
            payload.setSeverity(existing.getSeverity());
        }
        return ApiResponse.success("更新成功", repository.save(payload));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            return ApiResponse.error(404, "报警规则不存在");
        }
        repository.deleteById(id);
        return ApiResponse.success(null);
    }
}

