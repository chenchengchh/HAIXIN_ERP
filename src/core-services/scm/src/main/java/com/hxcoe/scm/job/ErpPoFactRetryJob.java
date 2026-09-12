package com.hxcoe.scm.job;

import com.hxcoe.scm.service.ErpPoFactOutboxService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ErpPoFactRetryJob {

    @Autowired
    private ErpPoFactOutboxService outboxService;

    @Scheduled(fixedDelayString = "${integration.erpFact.retryDelayMs:60000}")
    public void retry() {
        int n = outboxService.trySendPendingBatch();
        if (n > 0) {
            log.info("SCM->ERP 采购事实推送处理条数={}", n);
        }
    }
}

