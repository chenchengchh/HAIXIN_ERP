package com.hxcoe.oa.iam.job;

import com.hxcoe.oa.iam.service.OaAccountStatusSyncService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(name = "oa.iam.account-status-sync.enabled", havingValue = "true", matchIfMissing = true)
public class OaAccountStatusSyncJob {

    @Autowired
    private OaAccountStatusSyncService oaAccountStatusSyncService;

    @Scheduled(
            fixedDelayString = "${oa.iam.account-status-sync.delay-ms:300000}",
            initialDelayString = "${oa.iam.account-status-sync.initial-delay-ms:60000}"
    )
    public void sync() {
        try {
            oaAccountStatusSyncService.syncFromHrEmployees();
        } catch (Exception ex) {
            log.warn("OA 账号启停同步失败 error={}", ex.getMessage());
        }
    }
}

