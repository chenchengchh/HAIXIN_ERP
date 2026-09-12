package com.hxcoe.les.repository;

import com.hxcoe.les.entity.LogisticsOrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface LogisticsOrderRepository extends JpaRepository<LogisticsOrderEntity, Long>, JpaSpecificationExecutor<LogisticsOrderEntity> {
}