package com.hxcoe.scm.service;

import com.hxcoe.common.result.Result;
import com.hxcoe.common.result.PageResult;
import com.hxcoe.scm.entity.SupplierEntity;
import org.springframework.data.domain.Pageable;

public interface SupplierService {
    Result<SupplierEntity> createSupplier(SupplierEntity supplier);
    Result<SupplierEntity> updateSupplier(Long id, SupplierEntity supplier);
    Result<Void> deleteSupplier(Long id);
    Result<SupplierEntity> getSupplierById(Long id);
    Result<PageResult<SupplierEntity>> getSuppliersByPage(Pageable pageable);
}