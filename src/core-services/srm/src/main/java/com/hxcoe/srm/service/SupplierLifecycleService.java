package com.hxcoe.srm.service;

import com.hxcoe.srm.entity.SupplierEntity;

public interface SupplierLifecycleService {
    SupplierEntity registerSupplier(SupplierEntity supplier);
    SupplierEntity promoteToPotential(Long supplierId);
    SupplierEntity promoteToQualified(Long supplierId);
    SupplierEntity demoteSupplier(Long supplierId, String reason);
    SupplierEntity blacklistSupplier(Long supplierId, String reason);
    SupplierEntity removeFromBlacklist(Long supplierId, String reason);
}
