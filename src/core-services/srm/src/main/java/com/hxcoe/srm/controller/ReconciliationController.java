package com.hxcoe.srm.controller;

import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import com.hxcoe.srm.entity.ReconciliationEntity;
import com.hxcoe.srm.repository.ReconciliationRepository;
import com.hxcoe.srm.repository.SupplierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/srm")
public class ReconciliationController {

    @Autowired
    private ReconciliationRepository reconciliationRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @GetMapping("/reconciliations")
    public Result<PageResult<ReconciliationEntity>> getReconciliations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String reconciliationNo,
            @RequestParam(required = false) String supplierName,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {
        Pageable pageable = PageRequest.of(page, size);
        Specification<ReconciliationEntity> spec = (root, query, cb) -> {
            var predicate = cb.conjunction();
            if (reconciliationNo != null && !reconciliationNo.isBlank()) {
                predicate = cb.and(predicate, cb.like(root.get("reconciliationNo"), "%" + reconciliationNo + "%"));
            }
            if (supplierName != null && !supplierName.isBlank()) {
                predicate = cb.and(predicate, cb.like(root.get("supplierName"), "%" + supplierName + "%"));
            }
            if (status != null && !status.isBlank()) {
                predicate = cb.and(predicate, cb.equal(root.get("status"), status));
            }
            if (startDate != null) {
                predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("startDate"), startDate));
            }
            if (endDate != null) {
                predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.get("endDate"), endDate));
            }
            return predicate;
        };
        Page<ReconciliationEntity> result = reconciliationRepository.findAll(spec, pageable);
        PageResult<ReconciliationEntity> pageResult = PageResult.build(
                result.getTotalElements(),
                result.getSize(),
                result.getNumber() + 1,
                result.getContent()
        );
        return Result.success("对账列表查询成功", pageResult);
    }

    @GetMapping("/reconciliations/{id}")
    public Result<ReconciliationEntity> getReconciliationById(@PathVariable Long id) {
        return reconciliationRepository.findById(id)
                .map(entity -> Result.success("对账查询成功", entity))
                .orElseGet(() -> Result.fail("对账不存在"));
    }

    @PostMapping("/reconciliations")
    public Result<ReconciliationEntity> createReconciliation(@RequestBody ReconciliationEntity reconciliation) {
        attachSupplierName(reconciliation);
        ReconciliationEntity saved = reconciliationRepository.save(reconciliation);
        return Result.success("对账创建成功", saved);
    }

    @PutMapping("/reconciliations/{id}")
    public Result<ReconciliationEntity> updateReconciliation(@PathVariable Long id, @RequestBody ReconciliationEntity reconciliation) {
        return reconciliationRepository.findById(id).map(existing -> {
            existing.setReconciliationNo(reconciliation.getReconciliationNo());
            existing.setSupplierId(reconciliation.getSupplierId());
            existing.setSupplierName(reconciliation.getSupplierName());
            existing.setReconciliationPeriod(reconciliation.getReconciliationPeriod());
            existing.setTotalAmount(reconciliation.getTotalAmount());
            existing.setStartDate(reconciliation.getStartDate());
            existing.setEndDate(reconciliation.getEndDate());
            existing.setStatus(reconciliation.getStatus());
            existing.setConfirmStatus(reconciliation.getConfirmStatus());
            existing.setRemark(reconciliation.getRemark());
            attachSupplierName(existing);
            ReconciliationEntity saved = reconciliationRepository.save(existing);
            return Result.success("对账更新成功", saved);
        }).orElseGet(() -> Result.fail("对账不存在"));
    }

    @PutMapping("/reconciliations/{id}/confirm")
    public Result<ReconciliationEntity> confirmReconciliation(@PathVariable Long id) {
        return reconciliationRepository.findById(id).map(existing -> {
            existing.setConfirmStatus("CONFIRMED");
            if ("PENDING".equals(existing.getStatus()) || "PROCESSING".equals(existing.getStatus())) {
                existing.setStatus("CONFIRMED");
            }
            ReconciliationEntity saved = reconciliationRepository.save(existing);
            return Result.success("对账确认成功", saved);
        }).orElseGet(() -> Result.fail("对账不存在"));
    }

    @PutMapping("/reconciliations/{id}/settle")
    public Result<ReconciliationEntity> settleReconciliation(@PathVariable Long id) {
        return reconciliationRepository.findById(id).map(existing -> {
            existing.setStatus("SETTLED");
            existing.setConfirmStatus("CONFIRMED");
            ReconciliationEntity saved = reconciliationRepository.save(existing);
            return Result.success("对账结算成功", saved);
        }).orElseGet(() -> Result.fail("对账不存在"));
    }

    @PutMapping("/reconciliations/{id}/cancel")
    public Result<ReconciliationEntity> cancelReconciliation(@PathVariable Long id) {
        return reconciliationRepository.findById(id).map(existing -> {
            existing.setStatus("CANCELLED");
            ReconciliationEntity saved = reconciliationRepository.save(existing);
            return Result.success("对账取消成功", saved);
        }).orElseGet(() -> Result.fail("对账不存在"));
    }

    @DeleteMapping("/reconciliations/{id}")
    public Result<String> deleteReconciliation(@PathVariable Long id) {
        reconciliationRepository.deleteById(id);
        return Result.success("对账删除成功");
    }

    private void attachSupplierName(ReconciliationEntity reconciliation) {
        if (reconciliation.getSupplierId() == null) return;
        Optional<com.hxcoe.srm.entity.SupplierEntity> supplier = supplierRepository.findById(reconciliation.getSupplierId());
        supplier.ifPresent(s -> reconciliation.setSupplierName(s.getSupplierName()));
    }
}

