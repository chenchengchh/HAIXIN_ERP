package com.hxcoe.erp.service;

import com.hxcoe.common.result.PageResult;
import com.hxcoe.erp.entity.SupplierEntity;

public interface SupplierService {
    PageResult<SupplierEntity> getSupplierList(Integer page, Integer size, String name, String code);
    SupplierEntity createSupplier(SupplierEntity supplier);
    SupplierEntity updateSupplier(SupplierEntity supplier);
    boolean deleteSupplier(Long id);
    SupplierEntity getSupplierById(Long id);
}
