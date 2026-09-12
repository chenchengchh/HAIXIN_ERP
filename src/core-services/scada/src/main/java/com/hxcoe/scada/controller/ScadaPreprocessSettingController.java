package com.hxcoe.scada.controller;

import com.hxcoe.common.api.ApiResponse;
import com.hxcoe.scada.entity.ScadaPreprocessSettingEntity;
import com.hxcoe.scada.repository.ScadaPreprocessSettingRepository;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/scada/preprocess-settings")
public class ScadaPreprocessSettingController {

    private static final long SINGLETON_ID = 1L;

    private final ScadaPreprocessSettingRepository repository;

    public ScadaPreprocessSettingController(ScadaPreprocessSettingRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public ApiResponse<ScadaPreprocessSettingEntity> get() {
        ScadaPreprocessSettingEntity entity = repository.findById(SINGLETON_ID).orElseGet(() -> {
            ScadaPreprocessSettingEntity created = new ScadaPreprocessSettingEntity();
            created.setId(SINGLETON_ID);
            created.setRangeConversion(Boolean.TRUE);
            created.setDeadbandFilter(Boolean.TRUE);
            created.setDeadbandValue(0.5);
            created.setDataValidation(Boolean.TRUE);
            created.setUpdatedTime(LocalDateTime.now());
            return repository.save(created);
        });
        return ApiResponse.success(entity);
    }

    @PutMapping
    public ApiResponse<ScadaPreprocessSettingEntity> save(@RequestBody ScadaPreprocessSettingEntity payload) {
        payload.setId(SINGLETON_ID);
        payload.setUpdatedTime(LocalDateTime.now());
        return ApiResponse.success("保存成功", repository.save(payload));
    }
}

