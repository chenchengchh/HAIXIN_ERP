package com.hxcoe.scm.job;

import com.hxcoe.scm.service.PurchaseOrderEventOutboxService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PurchaseOrderEventRetryJob {

    @Autowired
    private PurchaseOrderEventOutboxService purchaseOrderEventOutboxService;

    @Scheduled(fixedDelayString = "${scm.integration.po-event-retry-delay-ms:60000}")
    public void retry() {
        purchaseOrderEventOutboxService.trySendPendingBatch();
    }
}

