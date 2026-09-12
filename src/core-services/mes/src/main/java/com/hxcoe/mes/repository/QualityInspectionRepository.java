package com.hxcoe.mes.repository;

import com.hxcoe.mes.entity.QualityInspectionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QualityInspectionRepository extends JpaRepository<QualityInspectionEntity, Long> {
}
