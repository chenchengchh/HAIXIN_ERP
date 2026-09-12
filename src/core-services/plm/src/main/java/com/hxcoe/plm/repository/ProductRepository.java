package com.hxcoe.plm.repository;

import com.hxcoe.plm.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<ProductEntity, Long>, JpaSpecificationExecutor<ProductEntity> {

    /**
     * 按产品编码查询产品。
     */
    Optional<ProductEntity> findByProductCode(String productCode);
}