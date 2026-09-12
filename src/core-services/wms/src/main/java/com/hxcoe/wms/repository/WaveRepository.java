package com.hxcoe.wms.repository;

import com.hxcoe.wms.entity.WaveEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface WaveRepository extends JpaRepository<WaveEntity, Long>, JpaSpecificationExecutor<WaveEntity> {
}
