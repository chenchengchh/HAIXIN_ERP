package com.hxcoe.erp.job;

import com.hxcoe.erp.service.MesWorkOrderOutboxService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * ERP→MES 工单创建 Outbox 重试 Job。
 *
 * <p>复刻 SCM 的 ErpPoFactRetryJob。定时扫描 erp_integration_task 表中
 * actionType=MES_WORK_ORDER_CREATE 且状态为 PENDING/FAILED 的任务，推送 MES。
 *
 * <p>可通过配置 erp.integration.mes-work-order.enabled=false 关闭。
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "erp.integration.mes-work-order.enabled", havingValue = "true", matchIfMissing = true)
public class MesWorkOrderOutboxRetryJob {

    @Autowired
    private MesWorkOrderOutboxService outboxService;

    /**
     * 定时扫描并推送待处理任务。
     */
    @Scheduled(fixedDelayString = "${erp.integration.mes-work-order.retry-delay-ms:10000}")
    public void retry() {
        int n = outboxService.trySendPendingBatch();
        if (n > 0) {
            log.info("ERP->MES 工单创建事件推送处理条数={}", n);
        }
    }
}
