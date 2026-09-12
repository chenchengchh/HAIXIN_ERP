package com.hxcoe.srm.controller;

import com.hxcoe.srm.entity.SupplierEntity;
import com.hxcoe.srm.service.SupplierService;
import com.hxcoe.common.result.Result;
import com.hxcoe.common.result.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/srm/suppliers")
public class SupplierController {

    @Autowired
    private SupplierService supplierService;

    @PostMapping
    public Result<SupplierEntity> createSupplier(@RequestBody SupplierEntity supplier) {
        return supplierService.createSupplier(supplier);
    }

    @PutMapping("/{id}")
    public Result<SupplierEntity> updateSupplier(@PathVariable Long id, @RequestBody SupplierEntity supplier) {
        return supplierService.updateSupplier(id, supplier);
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteSupplier(@PathVariable Long id) {
        return supplierService.deleteSupplier(id);
    }

    @GetMapping("/{id}")
    public Result<SupplierEntity> getSupplierById(@PathVariable Long id) {
        return supplierService.getSupplierById(id);
    }

    @GetMapping
    public Result<PageResult<SupplierEntity>> getSuppliers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return supplierService.getSuppliersByPage(pageable);
    }
}
