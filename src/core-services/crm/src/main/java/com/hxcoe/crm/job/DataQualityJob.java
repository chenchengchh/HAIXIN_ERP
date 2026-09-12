package com.hxcoe.crm.job;

import com.hxcoe.crm.service.CrmDataQualityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DataQualityJob {

    @Autowired
    private CrmDataQualityService crmDataQualityService;

    @Scheduled(
            fixedDelayString = "${crm.data-quality.refresh-delay-ms:86400000}",
            initialDelayString = "${crm.data-quality.refresh-initial-delay-ms:60000}"
    )
    public void refresh() {
        try {
            crmDataQualityService.refresh();
        } catch (Exception ex) {
            log.warn("CRM 数据质量刷新失败 error={}", ex.getMessage());
        }
    }
}

