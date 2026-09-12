package com.hxcoe.plm.repository;

import com.hxcoe.plm.entity.ResourceLoadEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ResourceLoadRepository extends JpaRepository<ResourceLoadEntity, Long>, JpaSpecificationExecutor<ResourceLoadEntity> {
}

