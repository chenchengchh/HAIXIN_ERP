package com.hxcoe.scm.controller;

import com.hxcoe.common.api.ApiResponse;
import com.hxcoe.scm.entity.LogisticsProviderEntity;
import com.hxcoe.scm.repository.LogisticsProviderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/scm/logistics/providers", "/api/scm/logistics/providers"})
public class LogisticsProviderController {

    @Autowired
    private LogisticsProviderRepository logisticsProviderRepository;

    @GetMapping
    public ApiResponse<List<LogisticsProviderEntity>> list() {
        return success("成功", logisticsProviderRepository.findAll());
    }

    @PostMapping
    public ApiResponse<LogisticsProviderEntity> create(@RequestBody LogisticsProviderEntity entity) {
        return success("成功", logisticsProviderRepository.save(entity));
    }

    @PutMapping("/{id}")
    public ApiResponse<LogisticsProviderEntity> update(@PathVariable Long id, @RequestBody LogisticsProviderEntity input) {
        LogisticsProviderEntity entity = logisticsProviderRepository.findById(id).orElse(null);
        if (entity == null) {
            return notFound("物流商不存在");
        }
        entity.setProviderId(input.getProviderId());
        entity.setProviderName(input.getProviderName());
        entity.setServiceRange(input.getServiceRange());
        entity.setContactPerson(input.getContactPerson());
        entity.setContactPhone(input.getContactPhone());
        entity.setPerformanceRating(input.getPerformanceRating());
        return success("成功", logisticsProviderRepository.save(entity));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        logisticsProviderRepository.deleteById(id);
        return success("成功", null);
    }

    private static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.success(message, data);
    }

    private static <T> ApiResponse<T> notFound(String message) {
        return ApiResponse.error(404, message);
    }
}

