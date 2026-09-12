package com.hxcoe.srm.repository;

import com.hxcoe.srm.entity.QualityObjectionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface QualityObjectionRepository extends JpaRepository<QualityObjectionEntity, Long>, JpaSpecificationExecutor<QualityObjectionEntity> {
}

