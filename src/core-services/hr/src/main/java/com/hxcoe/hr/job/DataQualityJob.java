package com.hxcoe.hr.job;

import com.hxcoe.hr.service.HrDataQualityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DataQualityJob {

    @Autowired
    private HrDataQualityService hrDataQualityService;

    @Scheduled(
            fixedDelayString = "${hr.data-quality.refresh-delay-ms:86400000}",
            initialDelayString = "${hr.data-quality.refresh-initial-delay-ms:60000}"
    )
    public void refresh() {
        try {
            hrDataQualityService.refresh();
        } catch (Exception ex) {
            log.warn("HR 数据质量刷新失败 error={}", ex.getMessage());
        }
    }
}

