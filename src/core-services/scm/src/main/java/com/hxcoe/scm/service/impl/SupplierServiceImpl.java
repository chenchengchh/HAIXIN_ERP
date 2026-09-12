package com.hxcoe.scm.service.impl;

import com.hxcoe.common.result.Result;
import com.hxcoe.common.result.PageResult;
import com.hxcoe.scm.entity.SupplierEntity;
import com.hxcoe.scm.repository.SupplierRepository;
import com.hxcoe.scm.service.SupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class SupplierServiceImpl implements SupplierService {

    @Autowired
    private SupplierRepository supplierRepository;

    @Value("${scm.master-data.supplier-write-enabled:false}")
    private boolean supplierWriteEnabled;

    @Override
    public Result<SupplierEntity> createSupplier(SupplierEntity supplier) {
        if (!supplierWriteEnabled) {
            return Result.error("供应商主数据权威归 SRM，SCM 禁止写入");
        }
        supplier.setCreatedTime(LocalDateTime.now());
        SupplierEntity savedSupplier = supplierRepository.save(supplier);
        return Result.success(savedSupplier);
    }

    @Override
    public Result<SupplierEntity> updateSupplier(Long id, SupplierEntity supplier) {
        if (!supplierWriteEnabled) {
            return Result.error("供应商主数据权威归 SRM，SCM 禁止写入");
        }
        SupplierEntity existingSupplier = supplierRepository.findById(id).orElse(null);
        if (existingSupplier == null) {
            return Result.error("供应商不存在");
        }
        supplier.setId(id);
        supplier.setUpdatedTime(LocalDateTime.now());
        SupplierEntity updatedSupplier = supplierRepository.save(supplier);
        return Result.success(updatedSupplier);
    }

    @Override
    public Result<Void> deleteSupplier(Long id) {
        if (!supplierWriteEnabled) {
            return Result.error("供应商主数据权威归 SRM，SCM 禁止写入");
        }
        if (!supplierRepository.existsById(id)) {
            return Result.error("供应商不存在");
        }
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
            (int) page.getSize(), 
            (int) (page.getNumber() + 1), 
            page.getContent()
        );
        return Result.success(pageResult);
    }
}
