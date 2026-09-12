package com.hxcoe.srm.service.impl;

import com.hxcoe.srm.entity.SupplierEntity;
import com.hxcoe.srm.entity.SupplierQualificationEntity;
import com.hxcoe.srm.repository.SupplierRepository;
import com.hxcoe.srm.repository.SupplierQualificationRepository;
import com.hxcoe.srm.service.SupplierEventOutboxService;
import com.hxcoe.srm.service.SupplierLifecycleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class SupplierLifecycleServiceImpl implements SupplierLifecycleService {

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private SupplierQualificationRepository supplierQualificationRepository;

    @Autowired
    private SupplierEventOutboxService supplierEventOutboxService;

    @Override
    public SupplierEntity registerSupplier(SupplierEntity supplier) {
        supplier.setStatus("ACTIVE");
        if (supplier.getType() == null || supplier.getType().isBlank()) {
            supplier.setType("POTENTIAL");
        }
        SupplierEntity saved = supplierRepository.save(supplier);
        supplierEventOutboxService.enqueueSupplierEvent(saved, "SUPPLIER_CREATED");
        return saved;
    }

    @Override
    public SupplierEntity promoteToPotential(Long supplierId) {
        return updateSupplierStatus(supplierId, "ACTIVE", "POTENTIAL");
    }

    @Override
    @Transactional
    public SupplierEntity promoteToQualified(Long supplierId) {
        // 检查是否有有效资质
        List<SupplierQualificationEntity> qualifications = supplierQualificationRepository.findBySupplierId(supplierId);
        LocalDate today = LocalDate.now();
        boolean hasValidQualification = qualifications.stream()
                .anyMatch(q -> "APPROVED".equals(q.getStatus()) && (q.getExpiryDate() == null || !q.getExpiryDate().isBefore(today)));
        
        if (!hasValidQualification) {
            throw new RuntimeException("供应商没有有效资质，无法晋升为合格供应商");
        }
        
        return updateSupplierStatus(supplierId, "ACTIVE", "QUALIFIED");
    }

    @Override
    public SupplierEntity demoteSupplier(Long supplierId, String reason) {
        // 可以在这里记录原因
        return updateSupplierStatus(supplierId, "ACTIVE", "POTENTIAL");
    }

    @Override
    public SupplierEntity blacklistSupplier(Long supplierId, String reason) {
        return updateSupplierStatus(supplierId, "BLACKLISTED", "BLACKLISTED");
    }

    @Override
    public SupplierEntity removeFromBlacklist(Long supplierId, String reason) {
        return updateSupplierStatus(supplierId, "ACTIVE", "POTENTIAL");
    }

    private SupplierEntity updateSupplierStatus(Long supplierId, String status, String type) {
        Optional<SupplierEntity> optional = supplierRepository.findById(supplierId);
        if (optional.isPresent()) {
            SupplierEntity supplier = optional.get();
            supplier.setStatus(status);
            supplier.setType(type);
            SupplierEntity saved = supplierRepository.save(supplier);
            supplierEventOutboxService.enqueueSupplierEvent(saved, "SUPPLIER_UPDATED");
            return saved;
        }
        return null;
    }
}
