package com.hxcoe.srm.repository;

import com.hxcoe.srm.entity.SupplierCreditEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SupplierCreditRepository extends JpaRepository<SupplierCreditEntity, Long>, JpaSpecificationExecutor<SupplierCreditEntity> {
    Optional<SupplierCreditEntity> findBySupplierId(Long supplierId);
}

