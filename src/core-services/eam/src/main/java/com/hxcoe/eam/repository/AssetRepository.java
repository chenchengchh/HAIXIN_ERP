package com.hxcoe.eam.repository;

import com.hxcoe.eam.entity.AssetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AssetRepository extends JpaRepository<AssetEntity, Long> {

    /**
     * 按设备名称查询第一条资产记录（用于回填故障记录的资产ID）。
     *
     * @param name 设备名称
     * @return 匹配的资产记录
     */
    Optional<AssetEntity> findFirstByName(String name);
}
