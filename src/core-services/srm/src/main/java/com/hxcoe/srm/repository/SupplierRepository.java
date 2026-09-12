package com.hxcoe.srm.repository;

import com.hxcoe.srm.entity.SupplierEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SupplierRepository extends JpaRepository<SupplierEntity, Long> {
    SupplierEntity findBySupplierCode(String supplierCode);
}
