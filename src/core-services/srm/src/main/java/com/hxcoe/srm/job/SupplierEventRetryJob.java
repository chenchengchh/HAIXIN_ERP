package com.hxcoe.srm.job;

import com.hxcoe.srm.service.SupplierEventOutboxService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SupplierEventRetryJob {

    @Autowired
    private SupplierEventOutboxService supplierEventOutboxService;

    @Scheduled(fixedDelayString = "${srm.integration.supplier-event-retry-delay-ms:60000}")
    public void retrySupplierEvents() {
        supplierEventOutboxService.trySendPendingBatch();
    }
}

