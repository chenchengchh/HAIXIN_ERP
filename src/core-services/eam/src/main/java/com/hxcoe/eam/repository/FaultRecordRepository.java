package com.hxcoe.eam.repository;

import com.hxcoe.eam.entity.FaultRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FaultRecordRepository extends JpaRepository<FaultRecordEntity, Long> {
    List<FaultRecordEntity> findByReportTimeBetween(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 按设备名称+上报时间+故障类型组合查询（用于 MES 故障推送的幂等去重）。
     *
     * @param equipmentName 设备名称
     * @param reportTime    上报时间
     * @param type          故障类型
     * @return 匹配的故障记录列表
     */
    List<FaultRecordEntity> findByEquipmentNameAndReportTimeAndType(String equipmentName, LocalDateTime reportTime, String type);
}
