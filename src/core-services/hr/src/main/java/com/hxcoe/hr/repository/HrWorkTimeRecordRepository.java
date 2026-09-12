package com.hxcoe.hr.repository;

import com.hxcoe.hr.entity.HrWorkTimeRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * MES报工工时记录仓储
 */
@Repository
public interface HrWorkTimeRecordRepository extends JpaRepository<HrWorkTimeRecordEntity, Long> {

    /**
     * 按事件ID查询报工工时记录（幂等判重）
     *
     * @param eventId 集成事件ID
     * @return 报工工时记录
     */
    Optional<HrWorkTimeRecordEntity> findByEventId(String eventId);
}
