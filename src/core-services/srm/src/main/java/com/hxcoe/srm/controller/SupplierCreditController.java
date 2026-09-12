package com.hxcoe.srm.controller;

import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import com.hxcoe.srm.entity.SupplierCreditEntity;
import com.hxcoe.srm.entity.SupplierEntity;
import com.hxcoe.srm.repository.SupplierCreditRepository;
import com.hxcoe.srm.repository.SupplierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/srm")
public class SupplierCreditController {

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private SupplierCreditRepository supplierCreditRepository;

    @GetMapping("/supplier-credits")
    public Result<PageResult<SupplierCreditEntity>> getSupplierCredits(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String supplierName,
            @RequestParam(required = false) String creditLevel,
            @RequestParam(required = false) String riskLevel) {

        List<SupplierEntity> suppliers = supplierRepository.findAll();
        List<SupplierCreditEntity> allCredits = new ArrayList<>();

        for (SupplierEntity supplier : suppliers) {
            SupplierCreditEntity credit = supplierCreditRepository.findBySupplierId(supplier.getId())
                    .orElseGet(() -> supplierCreditRepository.save(buildDefaultCredit(supplier)));
            allCredits.add(credit);
        }

        List<SupplierCreditEntity> filtered = allCredits.stream()
                .filter(c -> supplierName == null || supplierName.isBlank() || (c.getSupplierName() != null && c.getSupplierName().contains(supplierName)))
                .filter(c -> creditLevel == null || creditLevel.isBlank() || creditLevel.equals(c.getCreditLevel()))
                .filter(c -> riskLevel == null || riskLevel.isBlank() || riskLevel.equals(c.getRiskLevel()))
                .sorted(Comparator.comparing(SupplierCreditEntity::getSupplierId, Comparator.nullsLast(Long::compareTo)))
                .collect(Collectors.toList());

        Pageable pageable = PageRequest.of(page, size);
        int fromIndex = Math.min(page * size, filtered.size());
        int toIndex = Math.min(fromIndex + size, filtered.size());
        List<SupplierCreditEntity> pageContent = filtered.subList(fromIndex, toIndex);

        Page<SupplierCreditEntity> result = new PageImpl<>(pageContent, pageable, filtered.size());
        PageResult<SupplierCreditEntity> pageResult = PageResult.build(
                result.getTotalElements(),
                result.getSize(),
                result.getNumber() + 1,
                result.getContent()
        );
        return Result.success("供应商信用列表查询成功", pageResult);
    }

    @PutMapping("/supplier-credits")
    public Result<SupplierCreditEntity> upsertSupplierCredit(@RequestBody SupplierCreditEntity payload) {
        if (payload.getSupplierId() == null) {
            return Result.fail("supplierId不能为空");
        }

        SupplierEntity supplier = supplierRepository.findById(payload.getSupplierId()).orElse(null);
        if (supplier != null && (payload.getSupplierName() == null || payload.getSupplierName().isBlank())) {
            payload.setSupplierName(supplier.getSupplierName());
        }

        if (payload.getEvaluationDate() == null) payload.setEvaluationDate(LocalDate.now());
        if (payload.getNextEvaluationDate() == null) payload.setNextEvaluationDate(LocalDate.now().plusMonths(3));

        SupplierCreditEntity existing = supplierCreditRepository.findBySupplierId(payload.getSupplierId()).orElse(null);
        if (existing != null) {
            payload.setId(existing.getId());
        }
        SupplierCreditEntity saved = supplierCreditRepository.save(payload);
        return Result.success("供应商信用更新成功", saved);
    }

    @GetMapping("/suppliers/{supplierId}/credit")
    public Result<SupplierCreditEntity> getSupplierCredit(@PathVariable Long supplierId) {
        SupplierEntity supplier = supplierRepository.findById(supplierId).orElse(null);
        if (supplier == null) {
            return Result.fail("供应商不存在");
        }
        SupplierCreditEntity entity = supplierCreditRepository.findBySupplierId(supplierId).orElseGet(() -> supplierCreditRepository.save(buildDefaultCredit(supplier)));
        return Result.success("供应商信用查询成功", entity);
    }

    @PostMapping("/suppliers/{supplierId}/credit/calculate")
    public Result<SupplierCreditEntity> calculateSupplierCredit(@PathVariable Long supplierId) {
        SupplierEntity supplier = supplierRepository.findById(supplierId).orElse(null);
        if (supplier == null) {
            return Result.fail("供应商不存在");
        }

        SupplierCreditEntity entity = buildDefaultCredit(supplier);
        SupplierCreditEntity existing = supplierCreditRepository.findBySupplierId(supplierId).orElse(null);
        if (existing != null) {
            entity.setId(existing.getId());
        }
        SupplierCreditEntity saved = supplierCreditRepository.save(entity);
        return Result.success("供应商信用重新评估成功", saved);
    }

    private SupplierCreditEntity buildDefaultCredit(SupplierEntity supplier) {
        SupplierCreditEntity entity = new SupplierCreditEntity();
        entity.setSupplierId(supplier.getId());
        entity.setSupplierName(supplier.getSupplierName());

        int score = 80;
        if (supplier.getRating() != null) {
            score = Math.max(0, Math.min(100, (int) Math.round(supplier.getRating() * 20)));
            if (score == 0) {
                score = 80;
            }
        }
        entity.setCreditScore(score);

        String level;
        if (score >= 95) level = "AAA";
        else if (score >= 90) level = "AA";
        else if (score >= 80) level = "A";
        else if (score >= 70) level = "B";
        else level = "C";
        entity.setCreditLevel(level);

        String risk;
        if (score >= 85) risk = "LOW";
        else if (score >= 70) risk = "MEDIUM";
        else risk = "HIGH";
        entity.setRiskLevel(risk);

        entity.setEvaluationDate(LocalDate.now());
        entity.setNextEvaluationDate(LocalDate.now().plusMonths(3));
        entity.setEvaluationComment("");
        entity.setRiskDescription("");
        entity.setImprovementSuggestion("");
        return entity;
    }
}
