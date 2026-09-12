package com.hxcoe.srm.service;

import com.hxcoe.srm.entity.SupplierQualificationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

public interface SupplierQualificationService {
    SupplierQualificationEntity createQualification(SupplierQualificationEntity qualification);
    Page<SupplierQualificationEntity> getQualifications(Pageable pageable);
    List<SupplierQualificationEntity> getQualificationsBySupplierId(Long supplierId);
    Optional<SupplierQualificationEntity> getQualificationById(Long id);
    SupplierQualificationEntity updateQualification(Long id, SupplierQualificationEntity qualification);
    void deleteQualification(Long id);
}
