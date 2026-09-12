package com.hxcoe.wms.repository;

import com.hxcoe.wms.entity.AsnEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface AsnRepository extends JpaRepository<AsnEntity, Long>, JpaSpecificationExecutor<AsnEntity> {
    AsnEntity findByDeliveryNoteNo(String deliveryNoteNo);
    AsnEntity findByAsnNo(String asnNo);
}
