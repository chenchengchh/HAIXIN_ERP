package com.hxcoe.wms.repository;

import com.hxcoe.wms.entity.ZoneEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ZoneRepository extends JpaRepository<ZoneEntity, Long>, JpaSpecificationExecutor<ZoneEntity> {
    ZoneEntity findByZoneCode(String zoneCode);
}
