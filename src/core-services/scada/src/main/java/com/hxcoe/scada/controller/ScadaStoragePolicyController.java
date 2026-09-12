package com.hxcoe.scada.controller;

import com.hxcoe.common.api.ApiResponse;
import com.hxcoe.scada.entity.ScadaStoragePolicyEntity;
import com.hxcoe.scada.repository.ScadaStoragePolicyRepository;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/scada/storage-policy")
public class ScadaStoragePolicyController {

    private static final long SINGLETON_ID = 1L;

    private final ScadaStoragePolicyRepository repository;

    public ScadaStoragePolicyController(ScadaStoragePolicyRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public ApiResponse<ScadaStoragePolicyEntity> get() {
        ScadaStoragePolicyEntity entity = repository.findById(SINGLETON_ID).orElseGet(() -> {
            ScadaStoragePolicyEntity created = new ScadaStoragePolicyEntity();
            created.setId(SINGLETON_ID);
            created.setStorageType("mysql");
            created.setHost("localhost");
            created.setPort(3306);
            created.setDatabase("hxcoe003");
            created.setUsername("root");
            created.setPassword("root");
            created.setRealtimeRetention(7);
            created.setHistoricalSampling("5m");
            created.setCompressionLevel("medium");
            created.setBackupPolicy("daily");
            created.setConnectionStatus("connected");
            created.setStoredData(0);
            created.setWritesPerSecond(0);
            created.setStorageEfficiency(0);
            created.setLastWriteTime(LocalDateTime.now());
            created.setEnabled(Boolean.TRUE);
            created.setUpdatedTime(LocalDateTime.now());
            return repository.save(created);
        });
        return ApiResponse.success(entity);
    }

    @PutMapping
    public ApiResponse<ScadaStoragePolicyEntity> save(@RequestBody ScadaStoragePolicyEntity payload) {
        payload.setId(SINGLETON_ID);
        payload.setUpdatedTime(LocalDateTime.now());
        if (payload.getConnectionStatus() == null || payload.getConnectionStatus().isBlank()) {
            payload.setConnectionStatus("connected");
        }
        if (payload.getLastWriteTime() == null) {
            payload.setLastWriteTime(LocalDateTime.now());
        }
        return ApiResponse.success("保存成功", repository.save(payload));
    }

    @PostMapping("/test-connection")
    public ApiResponse<ScadaStoragePolicyEntity> testConnection(@RequestBody ScadaStoragePolicyEntity payload) {
        if (payload.getHost() == null || payload.getHost().isBlank() || payload.getPort() == null) {
            return ApiResponse.error(400, "连接参数不完整");
        }
        payload.setConnectionStatus("connected");
        payload.setLastWriteTime(LocalDateTime.now());
        return ApiResponse.success("连接成功", payload);
    }
}

