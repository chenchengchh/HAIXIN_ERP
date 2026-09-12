package com.hxcoe.mes.repository;

import com.hxcoe.mes.entity.FlowRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FlowRecordRepository extends JpaRepository<FlowRecordEntity, Long> {
    List<FlowRecordEntity> findBySnCodeOrderByTimestampDesc(String snCode);
}
