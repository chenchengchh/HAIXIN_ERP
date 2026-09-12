package com.hxcoe.srm.repository;

import com.hxcoe.srm.entity.DeliveryNoteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DeliveryNoteRepository extends JpaRepository<DeliveryNoteEntity, Long> {
    DeliveryNoteEntity findByDeliveryNoteNo(String deliveryNoteNo);
    List<DeliveryNoteEntity> findByPurchaseOrderNo(String purchaseOrderNo);
}
