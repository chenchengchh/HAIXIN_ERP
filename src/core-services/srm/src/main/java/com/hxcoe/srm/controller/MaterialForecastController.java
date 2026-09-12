package com.hxcoe.srm.controller;

import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import com.hxcoe.srm.entity.MaterialForecastEntity;
import com.hxcoe.srm.entity.MaterialForecastItemEntity;
import com.hxcoe.srm.repository.MaterialForecastRepository;
import com.hxcoe.srm.repository.SupplierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/srm")
public class MaterialForecastController {

    @Autowired
    private MaterialForecastRepository materialForecastRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @GetMapping("/material-forecasts")
    public Result<PageResult<MaterialForecastEntity>> getForecasts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String forecastNo,
            @RequestParam(required = false) String supplierName,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) String status) {
        Pageable pageable = PageRequest.of(page, size);
        Specification<MaterialForecastEntity> spec = (root, query, cb) -> {
            var predicate = cb.conjunction();
            if (forecastNo != null && !forecastNo.isBlank()) {
                predicate = cb.and(predicate, cb.like(root.get("forecastNo"), "%" + forecastNo + "%"));
            }
            if (supplierName != null && !supplierName.isBlank()) {
                predicate = cb.and(predicate, cb.like(root.get("supplierName"), "%" + supplierName + "%"));
            }
            if (orderNo != null && !orderNo.isBlank()) {
                predicate = cb.and(predicate, cb.like(root.get("orderNo"), "%" + orderNo + "%"));
            }
            if (status != null && !status.isBlank()) {
                predicate = cb.and(predicate, cb.equal(root.get("status"), status));
            }
            return predicate;
        };
        Page<MaterialForecastEntity> result = materialForecastRepository.findAll(spec, pageable);
        PageResult<MaterialForecastEntity> pageResult = PageResult.build(
                result.getTotalElements(),
                result.getSize(),
                result.getNumber() + 1,
                result.getContent()
        );
        return Result.success("来料预报列表查询成功", pageResult);
    }

    @GetMapping("/material-forecasts/{id}")
    public Result<MaterialForecastEntity> getForecastById(@PathVariable Long id) {
        return materialForecastRepository.findById(id)
                .map(entity -> Result.success("来料预报查询成功", entity))
                .orElseGet(() -> Result.fail("来料预报不存在"));
    }

    @PostMapping("/material-forecasts")
    public Result<MaterialForecastEntity> createForecast(@RequestBody MaterialForecastEntity forecast) {
        normalizeForecast(forecast);
        MaterialForecastEntity saved = materialForecastRepository.save(forecast);
        return Result.success("来料预报创建成功", saved);
    }

    @PutMapping("/material-forecasts/{id}")
    public Result<MaterialForecastEntity> updateForecast(@PathVariable Long id, @RequestBody MaterialForecastEntity forecast) {
        return materialForecastRepository.findById(id).map(existing -> {
            existing.setForecastNo(forecast.getForecastNo());
            existing.setSupplierId(forecast.getSupplierId());
            existing.setSupplierName(forecast.getSupplierName());
            existing.setOrderId(forecast.getOrderId());
            existing.setOrderNo(forecast.getOrderNo());
            existing.setExpectedArrivalDate(forecast.getExpectedArrivalDate());
            existing.setActualArrivalDate(forecast.getActualArrivalDate());
            existing.setStatus(forecast.getStatus());
            existing.setLogisticsInfo(forecast.getLogisticsInfo());
            existing.setRemark(forecast.getRemark());
            if (existing.getItems() == null) {
                existing.setItems(new ArrayList<>());
            }
            existing.getItems().clear();
            if (forecast.getItems() != null) {
                for (MaterialForecastItemEntity item : forecast.getItems()) {
                    item.setId(null);
                    item.setForecast(existing);
                    existing.getItems().add(item);
                }
            }
            existing.setTotalQuantity(calculateTotalQuantity(existing.getItems()));
            attachSupplierName(existing);
            MaterialForecastEntity saved = materialForecastRepository.save(existing);
            return Result.success("来料预报更新成功", saved);
        }).orElseGet(() -> Result.fail("来料预报不存在"));
    }

    @PutMapping("/material-forecasts/{id}/confirm")
    public Result<MaterialForecastEntity> confirmForecast(@PathVariable Long id) {
        return materialForecastRepository.findById(id).map(existing -> {
            existing.setStatus("CONFIRMED");
            MaterialForecastEntity saved = materialForecastRepository.save(existing);
            return Result.success("来料预报确认成功", saved);
        }).orElseGet(() -> Result.fail("来料预报不存在"));
    }

    @PutMapping("/material-forecasts/{id}/cancel")
    public Result<MaterialForecastEntity> cancelForecast(@PathVariable Long id) {
        return materialForecastRepository.findById(id).map(existing -> {
            existing.setStatus("CANCELLED");
            MaterialForecastEntity saved = materialForecastRepository.save(existing);
            return Result.success("来料预报取消成功", saved);
        }).orElseGet(() -> Result.fail("来料预报不存在"));
    }

    private void normalizeForecast(MaterialForecastEntity forecast) {
        if (forecast.getItems() != null) {
            for (MaterialForecastItemEntity item : forecast.getItems()) {
                item.setForecast(forecast);
            }
        }
        attachSupplierName(forecast);
        forecast.setTotalQuantity(calculateTotalQuantity(forecast.getItems()));
    }

    private int calculateTotalQuantity(List<MaterialForecastItemEntity> items) {
        if (items == null) return 0;
        int total = 0;
        for (MaterialForecastItemEntity item : items) {
            if (item != null && item.getQuantity() != null) {
                total += item.getQuantity();
            }
        }
        return total;
    }

    private void attachSupplierName(MaterialForecastEntity forecast) {
        if (forecast.getSupplierId() == null) return;
        Optional<com.hxcoe.srm.entity.SupplierEntity> supplier = supplierRepository.findById(forecast.getSupplierId());
        supplier.ifPresent(s -> forecast.setSupplierName(s.getSupplierName()));
    }
}
