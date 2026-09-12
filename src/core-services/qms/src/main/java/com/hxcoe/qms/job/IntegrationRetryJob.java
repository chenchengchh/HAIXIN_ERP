package com.hxcoe.qms.job;

import com.hxcoe.qms.service.ScmIqcSyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class IntegrationRetryJob {

    @Autowired
    private ScmIqcSyncService scmIqcSyncService;

    @Scheduled(fixedDelayString = "${qms.integration.retry-delay-ms:60000}")
    public void retry() {
        scmIqcSyncService.trySendPendingBatch();
    }
}

