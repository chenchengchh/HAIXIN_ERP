package com.hxcoe.hr.job;

import com.hxcoe.hr.service.HrEventOutboxService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * HR 集成事件重试任务。
 * <p>定时扫描 PENDING/FAILED 的员工事件，投递到 OA/ERP。</p>
 */
@Component
public class IntegrationRetryJob {

    @Autowired
    private HrEventOutboxService hrEventOutboxService;

    @Scheduled(fixedDelayString = "${hr.integration.retry-delay-ms:60000}")
    public void retry() {
        hrEventOutboxService.trySendPendingBatch();
    }
}
