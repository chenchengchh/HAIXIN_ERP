package com.hxcoe.erp.job;

import com.hxcoe.erp.service.MaterialEventOutboxService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class MaterialEventRetryJob {

    @Autowired
    private MaterialEventOutboxService materialEventOutboxService;

    @Scheduled(fixedDelayString = "${erp.integration.material-event-retry-delay-ms:60000}")
    public void retryMaterialEvents() {
        materialEventOutboxService.trySendPendingBatch();
    }
}

