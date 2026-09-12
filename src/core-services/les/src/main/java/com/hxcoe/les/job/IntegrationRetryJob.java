package com.hxcoe.les.job;

import com.hxcoe.les.service.LesSignOutboxService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * LES 集成事件重试任务。
 * <p>定时扫描 PENDING/FAILED 的签收回写事件，投递到 ERP/CRM。</p>
 */
@Component
public class IntegrationRetryJob {

    @Autowired
    private LesSignOutboxService lesSignOutboxService;

    @Scheduled(fixedDelayString = "${les.integration.retry-delay-ms:60000}")
    public void retry() {
        lesSignOutboxService.trySendPendingBatch();
    }
}
