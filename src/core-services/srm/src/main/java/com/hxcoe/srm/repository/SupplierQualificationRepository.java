package com.hxcoe.srm.repository;

import com.hxcoe.srm.entity.SupplierQualificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SupplierQualificationRepository extends JpaRepository<SupplierQualificationEntity, Long> {
    List<SupplierQualificationEntity> findBySupplierId(Long supplierId);
}
