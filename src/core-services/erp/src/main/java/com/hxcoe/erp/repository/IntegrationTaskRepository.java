package com.hxcoe.erp.repository;

import com.hxcoe.erp.entity.IntegrationTaskEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IntegrationTaskRepository extends JpaRepository<IntegrationTaskEntity, Long> {
    Optional<IntegrationTaskEntity> findByIdempotencyKey(String idempotencyKey);

    List<IntegrationTaskEntity> findTop50ByStatusInOrderByCreatedTimeAsc(List<String> statuses);

    /**
     * 按 status 和 actionType 查询待处理任务（用于分类型 Outbox 扫描）。
     *
     * @param statuses   状态集合（如 PENDING/FAILED）
     * @param actionType 动作类型（如 MES_WORK_ORDER_CREATE）
     * @return 待处理任务列表（按创建时间升序，最多 50 条）
     */
    List<IntegrationTaskEntity> findTop50ByStatusInAndActionTypeOrderByCreatedTimeAsc(List<String> statuses, String actionType);
}

