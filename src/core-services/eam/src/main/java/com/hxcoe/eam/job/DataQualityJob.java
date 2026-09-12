package com.hxcoe.eam.job;

import com.hxcoe.eam.service.EamDataQualityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DataQualityJob {

    @Autowired
    private EamDataQualityService eamDataQualityService;

    @Scheduled(
            fixedDelayString = "${eam.data-quality.refresh-delay-ms:86400000}",
            initialDelayString = "${eam.data-quality.refresh-initial-delay-ms:60000}"
    )
    public void refresh() {
        try {
            eamDataQualityService.refresh();
        } catch (Exception ex) {
            log.warn("EAM 数据质量刷新失败 error={}", ex.getMessage());
        }
    }
}

