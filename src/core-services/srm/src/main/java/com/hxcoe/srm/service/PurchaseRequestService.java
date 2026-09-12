package com.hxcoe.srm.service;

import com.hxcoe.srm.entity.PurchaseRequestEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

public interface PurchaseRequestService {
    PurchaseRequestEntity createPurchaseRequest(PurchaseRequestEntity purchaseRequestEntity);
    Page<PurchaseRequestEntity> getAllPurchaseRequests(Pageable pageable);
    Optional<PurchaseRequestEntity> getPurchaseRequestById(Long id);
    Optional<PurchaseRequestEntity> getPurchaseRequestByCode(String requestCode);
    PurchaseRequestEntity updatePurchaseRequest(Long id, PurchaseRequestEntity purchaseRequestEntity);
    void deletePurchaseRequest(Long id);
    PurchaseRequestEntity approvePurchaseRequest(Long id, String status, String approver, String approvalOpinion);
}
