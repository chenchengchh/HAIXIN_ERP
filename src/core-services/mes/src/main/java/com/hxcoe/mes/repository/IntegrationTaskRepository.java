package com.hxcoe.mes.repository;

import com.hxcoe.mes.entity.IntegrationTaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * MES 集成任务 Repository（Outbox 表，B6 推送端）。
 */
@Repository
public interface IntegrationTaskRepository extends JpaRepository<IntegrationTaskEntity, Long> {

    /**
     * 按幂等键查询（用于入队前去重）。
     *
     * @param idempotencyKey 幂等键
     * @return 集成任务实体
     */
    Optional<IntegrationTaskEntity> findByIdempotencyKey(String idempotencyKey);

    /**
     * 按状态与动作类型查询前 50 条（按创建时间升序），用于重试 Job 批量推送。
     *
     * @param status     状态列表（PENDING/FAILED）
     * @param actionType 动作类型
     * @return 集成任务列表
     */
    List<IntegrationTaskEntity> findTop50ByStatusInAndActionTypeOrderByCreatedTimeAsc(List<String> status, String actionType);
}
