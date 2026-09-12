package com.hxcoe.srm.controller;

import com.hxcoe.srm.entity.SupplierQualificationEntity;
import com.hxcoe.srm.service.SupplierQualificationService;
import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/srm")
public class SupplierQualificationController {

    @Autowired
    private SupplierQualificationService supplierQualificationService;

    @PostMapping("/qualifications")
    public Result<SupplierQualificationEntity> createQualification(@RequestBody SupplierQualificationEntity qualification) {
        SupplierQualificationEntity result = supplierQualificationService.createQualification(qualification);
        return Result.success("资质创建成功", result);
    }

    @GetMapping("/qualifications")
    public Result<PageResult<SupplierQualificationEntity>> getQualifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<SupplierQualificationEntity> result = supplierQualificationService.getQualifications(pageable);
        PageResult<SupplierQualificationEntity> pageResult = PageResult.build(
                result.getTotalElements(),
                result.getSize(),
                result.getNumber() + 1,
                result.getContent()
        );
        return Result.success("资质列表查询成功", pageResult);
    }

    @GetMapping("/suppliers/{supplierId}/qualifications")
    public Result<List<SupplierQualificationEntity>> getQualificationsBySupplierId(@PathVariable Long supplierId) {
        List<SupplierQualificationEntity> result = supplierQualificationService.getQualificationsBySupplierId(supplierId);
        return Result.success("供应商资质查询成功", result);
    }

    @GetMapping("/qualifications/{id}")
    public Result<SupplierQualificationEntity> getQualificationById(@PathVariable Long id) {
        Optional<SupplierQualificationEntity> result = supplierQualificationService.getQualificationById(id);
        return result.map(entity -> Result.success("资质查询成功", entity)).orElseGet(() -> Result.fail("资质不存在"));
    }

    @PutMapping("/qualifications/{id}")
    public Result<SupplierQualificationEntity> updateQualification(@PathVariable Long id, @RequestBody SupplierQualificationEntity qualification) {
        SupplierQualificationEntity result = supplierQualificationService.updateQualification(id, qualification);
        if (result != null) {
            return Result.success("资质更新成功", result);
        } else {
            return Result.fail("资质不存在");
        }
    }

    @DeleteMapping("/qualifications/{id}")
    public Result<String> deleteQualification(@PathVariable Long id) {
        supplierQualificationService.deleteQualification(id);
        return Result.success("资质删除成功");
    }

    @PutMapping("/supplier-qualifications/{id}/audit")
    public Result<SupplierQualificationEntity> auditQualification(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestParam(required = false) String auditOpinion) {
        Optional<SupplierQualificationEntity> optional = supplierQualificationService.getQualificationById(id);
        if (optional.isEmpty()) {
            return Result.fail("资质不存在");
        }
        SupplierQualificationEntity entity = optional.get();
        entity.setStatus(status);
        entity.setAuditOpinion(auditOpinion);
        entity.setAuditedTime(LocalDateTime.now());
        SupplierQualificationEntity saved = supplierQualificationService.updateQualification(id, entity);
        return Result.success("资质审核成功", saved);
    }
}
