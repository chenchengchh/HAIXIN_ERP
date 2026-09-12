package com.hxcoe.srm.service.impl;

import com.hxcoe.srm.entity.SupplierQualificationEntity;
import com.hxcoe.srm.repository.SupplierQualificationRepository;
import com.hxcoe.srm.service.SupplierQualificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SupplierQualificationServiceImpl implements SupplierQualificationService {

    @Autowired
    private SupplierQualificationRepository supplierQualificationRepository;

    @Override
    public SupplierQualificationEntity createQualification(SupplierQualificationEntity qualification) {
        if (qualification.getStatus() == null || qualification.getStatus().isBlank()) {
            qualification.setStatus("PENDING");
        }
        return supplierQualificationRepository.save(qualification);
    }

    @Override
    public Page<SupplierQualificationEntity> getQualifications(Pageable pageable) {
        return supplierQualificationRepository.findAll(pageable);
    }

    @Override
    public List<SupplierQualificationEntity> getQualificationsBySupplierId(Long supplierId) {
        return supplierQualificationRepository.findBySupplierId(supplierId);
    }

    @Override
    public Optional<SupplierQualificationEntity> getQualificationById(Long id) {
        return supplierQualificationRepository.findById(id);
    }

    @Override
    public SupplierQualificationEntity updateQualification(Long id, SupplierQualificationEntity qualification) {
        return supplierQualificationRepository.findById(id).map(existing -> {
            existing.setSupplierId(qualification.getSupplierId());
            existing.setQualificationType(qualification.getQualificationType());
            existing.setCertificateName(qualification.getCertificateName());
            existing.setCertificateNo(qualification.getCertificateNo());
            existing.setIssueDate(qualification.getIssueDate());
            existing.setExpiryDate(qualification.getExpiryDate());
            if (qualification.getStatus() != null && !qualification.getStatus().isBlank()) {
                existing.setStatus(qualification.getStatus());
            }
            if (qualification.getAttachmentUrl() != null) {
                existing.setAttachmentUrl(qualification.getAttachmentUrl());
            }
            if (qualification.getAuditOpinion() != null) {
                existing.setAuditOpinion(qualification.getAuditOpinion());
            }
            if (qualification.getAuditedTime() != null) {
                existing.setAuditedTime(qualification.getAuditedTime());
            }
            return supplierQualificationRepository.save(existing);
        }).orElse(null);
    }

    @Override
    public void deleteQualification(Long id) {
        supplierQualificationRepository.deleteById(id);
    }
}
