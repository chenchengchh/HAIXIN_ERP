package com.hxcoe.srm.job;

import com.hxcoe.srm.service.SrmDataQualityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DataQualityJob {

    @Autowired
    private SrmDataQualityService srmDataQualityService;

    @Scheduled(
            fixedDelayString = "${srm.data-quality.refresh-delay-ms:86400000}",
            initialDelayString = "${srm.data-quality.refresh-initial-delay-ms:60000}"
    )
    public void refresh() {
        try {
            srmDataQualityService.refresh();
        } catch (Exception ex) {
            log.warn("SRM 数据质量刷新失败 error={}", ex.getMessage());
        }
    }
}

