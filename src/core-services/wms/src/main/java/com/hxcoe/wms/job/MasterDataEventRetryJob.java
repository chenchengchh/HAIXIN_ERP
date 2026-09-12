package com.hxcoe.wms.job;

import com.hxcoe.wms.service.MasterDataEventOutboxService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class MasterDataEventRetryJob {

    @Autowired
    private MasterDataEventOutboxService masterDataEventOutboxService;

    @Scheduled(fixedDelayString = "${wms.integration.masterdata-retry-delay-ms:60000}")
    public void retry() {
        masterDataEventOutboxService.trySendPendingBatch();
    }
}

