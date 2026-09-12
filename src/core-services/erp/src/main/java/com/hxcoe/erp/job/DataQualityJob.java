package com.hxcoe.erp.job;

import com.hxcoe.erp.service.ErpDataQualityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DataQualityJob {

    @Autowired
    private ErpDataQualityService erpDataQualityService;

    @Scheduled(
            fixedDelayString = "${erp.data-quality.refresh-delay-ms:86400000}",
            initialDelayString = "${erp.data-quality.refresh-initial-delay-ms:60000}"
    )
    public void refresh() {
        try {
            erpDataQualityService.refresh();
        } catch (Exception ex) {
            log.warn("ERP 数据质量刷新失败 error={}", ex.getMessage());
        }
    }
}

