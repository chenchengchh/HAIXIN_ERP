package com.hxcoe.wms.repository;

import com.hxcoe.wms.entity.MaterialTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface MaterialTypeRepository extends JpaRepository<MaterialTypeEntity, Long>, JpaSpecificationExecutor<MaterialTypeEntity> {
    MaterialTypeEntity findByTypeCode(String typeCode);
}
