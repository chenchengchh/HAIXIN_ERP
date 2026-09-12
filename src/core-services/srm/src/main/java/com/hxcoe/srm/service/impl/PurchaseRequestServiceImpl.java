package com.hxcoe.srm.service.impl;

import com.hxcoe.srm.entity.PurchaseRequestEntity;
import com.hxcoe.srm.repository.PurchaseRequestRepository;
import com.hxcoe.srm.service.PurchaseRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class PurchaseRequestServiceImpl implements PurchaseRequestService {

    @Autowired
    private PurchaseRequestRepository purchaseRequestRepository;

    @Override
    public PurchaseRequestEntity createPurchaseRequest(PurchaseRequestEntity purchaseRequestEntity) {
        return purchaseRequestRepository.save(purchaseRequestEntity);
    }

    @Override
    public Page<PurchaseRequestEntity> getAllPurchaseRequests(Pageable pageable) {
        return purchaseRequestRepository.findAll(pageable);
    }

    @Override
    public Optional<PurchaseRequestEntity> getPurchaseRequestById(Long id) {
        return purchaseRequestRepository.findById(id);
    }

    @Override
    public Optional<PurchaseRequestEntity> getPurchaseRequestByCode(String requestCode) {
        return purchaseRequestRepository.findByRequestCode(requestCode);
    }

    @Override
    public PurchaseRequestEntity updatePurchaseRequest(Long id, PurchaseRequestEntity purchaseRequestEntity) {
        if (purchaseRequestRepository.existsById(id)) {
            purchaseRequestEntity.setId(id);
            return purchaseRequestRepository.save(purchaseRequestEntity);
        }
        return null;
    }

    @Override
    public void deletePurchaseRequest(Long id) {
        purchaseRequestRepository.deleteById(id);
    }

    @Override
    public PurchaseRequestEntity approvePurchaseRequest(Long id, String status, String approver, String approvalOpinion) {
        Optional<PurchaseRequestEntity> optional = purchaseRequestRepository.findById(id);
        if (optional.isPresent()) {
            PurchaseRequestEntity entity = optional.get();
            entity.setStatus(status);
            entity.setApprover(approver);
            entity.setApprovalOpinion(approvalOpinion);
            entity.setApprovalTime(java.time.LocalDateTime.now());
            return purchaseRequestRepository.save(entity);
        }
        return null;
    }
}
