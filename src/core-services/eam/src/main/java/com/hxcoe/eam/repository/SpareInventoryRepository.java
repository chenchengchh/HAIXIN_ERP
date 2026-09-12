package com.hxcoe.eam.repository;

import com.hxcoe.eam.entity.SpareInventoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpareInventoryRepository extends JpaRepository<SpareInventoryEntity, Long> {
}
