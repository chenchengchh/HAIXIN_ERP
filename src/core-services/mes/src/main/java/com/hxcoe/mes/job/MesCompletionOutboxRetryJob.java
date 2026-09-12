package com.hxcoe.mes.job;

import com.hxcoe.mes.service.MesCompletionOutboxService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * MES→SCM 工单完工事实 Outbox 重试 Job（B6 闭环推送端）。
 *
 * <p>复刻 ERP 的 {@code MesWorkOrderOutboxRetryJob}。定时扫描 mes_integration_task 表中
 * actionType=SCM_COMPLETION_FACT 且状态为 PENDING/FAILED 的任务，推送 SCM。
 *
 * <p>可通过配置 mes.integration.scm-completion.enabled=false 关闭。
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "mes.integration.scm-completion.enabled", havingValue = "true", matchIfMissing = true)
public class MesCompletionOutboxRetryJob {

    @Autowired
    private MesCompletionOutboxService outboxService;

    /**
     * 定时扫描并推送待处理任务。
     */
    @Scheduled(fixedDelayString = "${mes.integration.scm-completion.retry-delay-ms:10000}")
    public void retry() {
        int n = outboxService.trySendPendingBatch();
        if (n > 0) {
            log.info("MES->SCM 完工事件推送处理条数={}", n);
        }
    }
}
