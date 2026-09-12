package com.hxcoe.aibrain.repository;

import com.hxcoe.aibrain.entity.AiDecisionLogEntity;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * AI 决策执行日志仓储
 */
@Repository
public interface AiDecisionLogRepository extends JpaRepository<AiDecisionLogEntity, Long> {

    /** 查询指定时间之后的全部日志（健康看板聚合用，数据量小按天级窗口拉取） */
    List<AiDecisionLogEntity> findByCreatedTimeAfterOrderByCreatedTimeDesc(LocalDateTime after);
}
