package com.hxcoe.wms.job;

import com.hxcoe.wms.service.WmsDataQualityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DataQualityJob {

    @Autowired
    private WmsDataQualityService wmsDataQualityService;

    @Scheduled(
            fixedDelayString = "${wms.data-quality.refresh-delay-ms:86400000}",
            initialDelayString = "${wms.data-quality.refresh-initial-delay-ms:60000}"
    )
    public void refresh() {
        try {
            wmsDataQualityService.refresh();
        } catch (Exception ex) {
            log.warn("WMS 数据质量刷新失败 error={}", ex.getMessage());
        }
    }
}

