package com.hxcoe.srm.controller;

import com.hxcoe.srm.entity.SupplierEntity;
import com.hxcoe.srm.service.SupplierLifecycleService;
import com.hxcoe.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import lombok.Data;

@RestController
@RequestMapping("/api/v1/srm/suppliers")
public class SupplierLifecycleController {

    @Autowired
    private SupplierLifecycleService supplierLifecycleService;

    @PostMapping("/register")
    public Result<SupplierEntity> registerSupplier(@RequestBody SupplierEntity supplier) {
        SupplierEntity result = supplierLifecycleService.registerSupplier(supplier);
        return Result.success("供应商注册成功", result);
    }

    @PutMapping("/{id}/promote-potential")
    public Result<SupplierEntity> promoteToPotential(@PathVariable Long id) {
        SupplierEntity result = supplierLifecycleService.promoteToPotential(id);
        if (result != null) {
            return Result.success("供应商已晋升为潜在供应商", result);
        } else {
            return Result.fail("供应商不存在");
        }
    }

    @PutMapping("/{id}/promote-qualified")
    public Result<SupplierEntity> promoteToQualified(@PathVariable Long id) {
        try {
            SupplierEntity result = supplierLifecycleService.promoteToQualified(id);
            if (result != null) {
                return Result.success("供应商已晋升为合格供应商", result);
            } else {
                return Result.fail("供应商不存在");
            }
        } catch (RuntimeException e) {
            return Result.fail(e.getMessage());
        }
    }

    @PutMapping("/{id}/demote")
    public Result<SupplierEntity> demoteSupplier(@PathVariable Long id, @RequestParam(required = false) String reason) {
        SupplierEntity result = supplierLifecycleService.demoteSupplier(id, reason);
        if (result != null) {
            return Result.success("供应商已降级", result);
        } else {
            return Result.fail("供应商不存在");
        }
    }

    @PutMapping("/{id}/blacklist")
    public Result<SupplierEntity> blacklistSupplier(@PathVariable Long id, @RequestParam(required = false) String reason) {
        SupplierEntity result = supplierLifecycleService.blacklistSupplier(id, reason);
        if (result != null) {
            return Result.success("供应商已加入黑名单", result);
        } else {
            return Result.fail("供应商不存在");
        }
    }

    @PutMapping("/{id}/blacklist/add")
    public Result<SupplierEntity> blacklistSupplierCompat(@PathVariable Long id, @RequestBody(required = false) BlacklistReasonDTO body) {
        String reason = body == null ? null : body.getReason();
        SupplierEntity result = supplierLifecycleService.blacklistSupplier(id, reason);
        if (result != null) {
            return Result.success("供应商已加入黑名单", result);
        } else {
            return Result.fail("供应商不存在");
        }
    }

    @PutMapping("/{id}/blacklist/remove")
    public Result<SupplierEntity> removeFromBlacklist(@PathVariable Long id, @RequestBody(required = false) BlacklistReasonDTO body) {
        String reason = body == null ? null : body.getReason();
        SupplierEntity result = supplierLifecycleService.removeFromBlacklist(id, reason);
        if (result != null) {
            return Result.success("供应商已移出黑名单", result);
        } else {
            return Result.fail("供应商不存在");
        }
    }

    @Data
    public static class BlacklistReasonDTO {
        private String reason;
    }
}
