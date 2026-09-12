package com.hxcoe.hr.repository;

import com.hxcoe.hr.entity.IntegrationOutboxEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * HR 集成 Outbox 仓储。
 *
 * <p>继承 {@link JpaSpecificationExecutor} 以支持多条件动态分页查询，
 * 供前端 F5（HR事件查询页）按事件类型、状态、业务引用号筛选。
 */
public interface IntegrationOutboxRepository
        extends JpaRepository<IntegrationOutboxEntity, Long>,
                JpaSpecificationExecutor<IntegrationOutboxEntity> {

    /**
     * 按事件类型和业务引用号查询（幂等查重）。
     *
     * @param eventType 事件类型
     * @param refNo     业务引用号
     * @return Outbox 实体（存在时）
     */
    Optional<IntegrationOutboxEntity> findByEventTypeAndRefNo(String eventType, String refNo);

    /**
     * 查询待重试任务（PENDING/FAILED 且到下次重试时间）。
     *
     * @param status 状态列表
     * @param now    当前时间
     * @return 待处理任务列表（最多 50 条）
     */
    List<IntegrationOutboxEntity> findTop50ByStatusInAndNextRetryAtBeforeOrderByNextRetryAtAsc(List<String> status, LocalDateTime now);
}
