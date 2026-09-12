package com.hxcoe.aps.repository;

import com.hxcoe.aps.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, Long> {

    /**
     * 按产品编码查询产品：排程时获取产品所属生产线，用于原料准备/入库工序的资源匹配
     */
    Optional<ProductEntity> findByProductCode(String productCode);
}
