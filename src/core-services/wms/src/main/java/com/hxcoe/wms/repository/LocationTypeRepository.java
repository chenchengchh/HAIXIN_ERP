package com.hxcoe.wms.repository;

import com.hxcoe.wms.entity.LocationTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface LocationTypeRepository extends JpaRepository<LocationTypeEntity, Long>, JpaSpecificationExecutor<LocationTypeEntity> {
    LocationTypeEntity findByTypeCode(String typeCode);
}
