package com.hxcoe.srm.repository;

import com.hxcoe.srm.entity.PurchaseRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PurchaseRequestRepository extends JpaRepository<PurchaseRequestEntity, Long> {
    Optional<PurchaseRequestEntity> findByRequestCode(String requestCode);
}
