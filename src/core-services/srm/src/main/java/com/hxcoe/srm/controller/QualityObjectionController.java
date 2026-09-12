package com.hxcoe.srm.controller;

import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import com.hxcoe.srm.entity.QualityObjectionEntity;
import com.hxcoe.srm.repository.QualityObjectionRepository;
import com.hxcoe.srm.repository.SupplierRepository;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/srm")
public class QualityObjectionController {

    @Autowired
    private QualityObjectionRepository qualityObjectionRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @GetMapping("/quality-objections")
    public Result<PageResult<QualityObjectionEntity>> getObjections(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String objectionNo,
            @RequestParam(required = false) String supplierName,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) String status) {
        Pageable pageable = PageRequest.of(page, size);
        Specification<QualityObjectionEntity> spec = (root, query, cb) -> {
            var predicate = cb.conjunction();
            if (objectionNo != null && !objectionNo.isBlank()) {
                predicate = cb.and(predicate, cb.like(root.get("objectionNo"), "%" + objectionNo + "%"));
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
        Page<QualityObjectionEntity> result = qualityObjectionRepository.findAll(spec, pageable);
        PageResult<QualityObjectionEntity> pageResult = PageResult.build(
                result.getTotalElements(),
                result.getSize(),
                result.getNumber() + 1,
                result.getContent()
        );
        return Result.success("质量异议列表查询成功", pageResult);
    }

    @GetMapping("/quality-objections/{id}")
    public Result<QualityObjectionEntity> getObjectionById(@PathVariable Long id) {
        return qualityObjectionRepository.findById(id)
                .map(entity -> Result.success("质量异议查询成功", entity))
                .orElseGet(() -> Result.fail("质量异议不存在"));
    }

    @PostMapping("/quality-objections")
    public Result<QualityObjectionEntity> createObjection(@RequestBody QualityObjectionEntity objection) {
        attachSupplierName(objection);
        QualityObjectionEntity saved = qualityObjectionRepository.save(objection);
        return Result.success("质量异议创建成功", saved);
    }

    @PutMapping("/quality-objections/{id}")
    public Result<QualityObjectionEntity> updateObjection(@PathVariable Long id, @RequestBody QualityObjectionEntity objection) {
        return qualityObjectionRepository.findById(id).map(existing -> {
            existing.setObjectionNo(objection.getObjectionNo());
            existing.setSupplierId(objection.getSupplierId());
            existing.setSupplierName(objection.getSupplierName());
            existing.setOrderId(objection.getOrderId());
            existing.setOrderNo(objection.getOrderNo());
            existing.setObjectionDate(objection.getObjectionDate());
            existing.setMaterialId(objection.getMaterialId());
            existing.setMaterialName(objection.getMaterialName());
            existing.setObjectionType(objection.getObjectionType());
            existing.setDescription(objection.getDescription());
            existing.setQuantity(objection.getQuantity());
            existing.setLossAmount(objection.getLossAmount());
            existing.setProcessingResult(objection.getProcessingResult());
            existing.setStatus(objection.getStatus());
            existing.setRemark(objection.getRemark());
            attachSupplierName(existing);
            QualityObjectionEntity saved = qualityObjectionRepository.save(existing);
            return Result.success("质量异议更新成功", saved);
        }).orElseGet(() -> Result.fail("质量异议不存在"));
    }

    @PutMapping("/quality-objections/{id}/process")
    public Result<QualityObjectionEntity> processObjection(@PathVariable Long id, @RequestBody ProcessDTO payload) {
        return qualityObjectionRepository.findById(id).map(existing -> {
            existing.setStatus(payload.getStatus() == null || payload.getStatus().isBlank() ? "PROCESSING" : payload.getStatus());
            existing.setProcessingResult(payload.getProcessingResult());
            QualityObjectionEntity saved = qualityObjectionRepository.save(existing);
            return Result.success("质量异议处理成功", saved);
        }).orElseGet(() -> Result.fail("质量异议不存在"));
    }

    @PutMapping("/quality-objections/{id}/close")
    public Result<QualityObjectionEntity> closeObjection(@PathVariable Long id) {
        return qualityObjectionRepository.findById(id).map(existing -> {
            existing.setStatus("CLOSED");
            QualityObjectionEntity saved = qualityObjectionRepository.save(existing);
            return Result.success("质量异议关闭成功", saved);
        }).orElseGet(() -> Result.fail("质量异议不存在"));
    }

    @DeleteMapping("/quality-objections/{id}")
    public Result<String> deleteObjection(@PathVariable Long id) {
        qualityObjectionRepository.deleteById(id);
        return Result.success("质量异议删除成功");
    }

    private void attachSupplierName(QualityObjectionEntity objection) {
        if (objection.getSupplierId() == null) return;
        Optional<com.hxcoe.srm.entity.SupplierEntity> supplier = supplierRepository.findById(objection.getSupplierId());
        supplier.ifPresent(s -> objection.setSupplierName(s.getSupplierName()));
    }

    @Data
    public static class ProcessDTO {
        private String processingResult;
        private String status;
    }
}

