package com.hxcoe.srm.repository;

import com.hxcoe.srm.entity.SupplierPerformanceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierPerformanceRepository extends JpaRepository<SupplierPerformanceEntity, Long> {
    List<SupplierPerformanceEntity> findBySupplierId(Long supplierId);

    /**
     * 查询供应商最新一条绩效记录（按主键倒序取第一条）
     *
     * @param supplierId 供应商ID
     * @return 最新的绩效记录
     */
    Optional<SupplierPerformanceEntity> findFirstBySupplierIdOrderByIdDesc(Long supplierId);
}
