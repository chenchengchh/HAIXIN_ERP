package com.hxcoe.srm.service.impl;

import com.hxcoe.srm.entity.SupplierEntity;
import com.hxcoe.srm.repository.SupplierRepository;
import com.hxcoe.srm.service.SupplierEventOutboxService;
import com.hxcoe.srm.service.SupplierService;
import com.hxcoe.common.result.Result;
import com.hxcoe.common.result.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class SupplierServiceImpl implements SupplierService {

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private SupplierEventOutboxService supplierEventOutboxService;

    @Override
    public Result<SupplierEntity> createSupplier(SupplierEntity supplier) {
        if (supplierRepository.findBySupplierCode(supplier.getSupplierCode()) != null) {
            return Result.error("供应商编码已存在");
        }
        supplier.setCreatedTime(LocalDateTime.now());
        supplier.setUpdatedTime(LocalDateTime.now());
        SupplierEntity saved = supplierRepository.save(supplier);
        supplierEventOutboxService.enqueueSupplierEvent(saved, "SUPPLIER_CREATED");
        return Result.success(saved);
    }

    @Override
    public Result<SupplierEntity> updateSupplier(Long id, SupplierEntity supplier) {
        SupplierEntity existing = supplierRepository.findById(id).orElse(null);
        if (existing == null) {
            return Result.error("供应商不存在");
        }
        supplier.setId(id);
        supplier.setCreatedTime(existing.getCreatedTime());
        supplier.setUpdatedTime(LocalDateTime.now());
        SupplierEntity updated = supplierRepository.save(supplier);
        supplierEventOutboxService.enqueueSupplierEvent(updated, "SUPPLIER_UPDATED");
        return Result.success(updated);
    }

    @Override
    public Result<Void> deleteSupplier(Long id) {
        SupplierEntity existing = supplierRepository.findById(id).orElse(null);
        if (existing == null) {
            return Result.error("供应商不存在");
        }
        existing.setStatus("DELETED");
        existing.setUpdatedTime(LocalDateTime.now());
        supplierEventOutboxService.enqueueSupplierEvent(existing, "SUPPLIER_DELETED");
        supplierRepository.deleteById(id);
        return Result.success();
    }

    @Override
    public Result<SupplierEntity> getSupplierById(Long id) {
        SupplierEntity supplier = supplierRepository.findById(id).orElse(null);
        if (supplier == null) {
            return Result.error("供应商不存在");
        }
        return Result.success(supplier);
    }

    @Override
    public Result<PageResult<SupplierEntity>> getSuppliersByPage(Pageable pageable) {
        Page<SupplierEntity> page = supplierRepository.findAll(pageable);
        PageResult<SupplierEntity> pageResult = PageResult.build(
            page.getTotalElements(), 
            Math.toIntExact(page.getSize()), 
            page.getNumber() + 1, 
            page.getContent()
        );
        return Result.success(pageResult);
    }
}
