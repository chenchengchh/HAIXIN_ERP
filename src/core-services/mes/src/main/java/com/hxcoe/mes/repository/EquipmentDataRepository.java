package com.hxcoe.mes.repository;

import com.hxcoe.mes.entity.EquipmentDataEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EquipmentDataRepository extends JpaRepository<EquipmentDataEntity, Long> {
    List<EquipmentDataEntity> findTop50ByEquipmentIdOrderByTimestampDesc(String equipmentId);

    /**
     * 查询指定设备最新一条采集数据（按时间倒序取第一条）。
     *
     * @param equipmentId 设备编号
     * @return 最新一条设备数据
     */
    Optional<EquipmentDataEntity> findTopByEquipmentIdOrderByTimestampDesc(String equipmentId);
}
