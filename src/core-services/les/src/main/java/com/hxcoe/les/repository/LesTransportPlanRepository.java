package com.hxcoe.les.repository;

import com.hxcoe.les.entity.LesTransportPlanEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LesTransportPlanRepository extends JpaRepository<LesTransportPlanEntity, Long> {

    boolean existsByPlanNo(String planNo);

    Page<LesTransportPlanEntity> findByStatus(Integer status, Pageable pageable);

    long countByStatus(Integer status);

    Page<LesTransportPlanEntity> findByPlanNoContainingIgnoreCaseOrSalesOrderNoContainingIgnoreCase(
            String planNoKeyword,
            String salesOrderNoKeyword,
            Pageable pageable
    );

    Page<LesTransportPlanEntity> findByStatusAndPlanNoContainingIgnoreCaseOrStatusAndSalesOrderNoContainingIgnoreCase(
            Integer status1,
            String planNoKeyword,
            Integer status2,
            String salesOrderNoKeyword,
            Pageable pageable
    );
}
