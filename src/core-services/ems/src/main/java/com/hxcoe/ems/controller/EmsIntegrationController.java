package com.hxcoe.ems.controller;

import com.hxcoe.common.result.Result;
import com.hxcoe.ems.entity.RealTimeDataEntity;
import com.hxcoe.ems.job.EnergyCostReportJob;
import com.hxcoe.ems.repository.RealTimeDataRepository;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * EMS集成控制器
 * 接收SCADA等外部系统推送的能源数据，幂等落入实时数据表
 *
 * @author author
 * @date 2026-07-25
 */
@RestController
@RequestMapping({"/api/v1/ems/integration", "/ems/integration"})
public class EmsIntegrationController {

    /**
     * 默认采集区域
     */
    private static final String DEFAULT_AREA = "SCADA采集";

    private final RealTimeDataRepository realTimeDataRepository;

    private final EnergyCostReportJob energyCostReportJob;

    @Autowired
    public EmsIntegrationController(RealTimeDataRepository realTimeDataRepository,
                                    EnergyCostReportJob energyCostReportJob) {
        this.realTimeDataRepository = realTimeDataRepository;
        this.energyCostReportJob = energyCostReportJob;
    }

    /**
     * 接收SCADA能源数据批量推送
     * POST /api/v1/ems/integration/scada/energy-data
     * 按 能源类型+设备ID+采集时间 做幂等查重，已存在则跳过
     *
     * @param request 推送请求体（eventId + items）
     * @return 接收与跳过条数统计
     */
    @PostMapping("/scada/energy-data")
    public Result<Map<String, Object>> receiveScadaEnergyData(@RequestBody ScadaEnergyDataPushRequest request) {
        if (request == null || request.getItems() == null || request.getItems().isEmpty()) {
            return Result.fail("推送数据不能为空");
        }

        int received = 0;
        int skipped = 0;
        for (ScadaEnergyDataItem item : request.getItems()) {
            // 必填字段校验：能源类型、采集值、采集时间缺失时跳过该条
            if (item == null || !StringUtils.hasText(item.getEnergyType())
                    || item.getActualValue() == null || item.getCollectionTime() == null) {
                skipped++;
                continue;
            }
            // 幂等查重：存在相同 能源类型+设备ID+采集时间 的记录则跳过
            if (isDuplicate(item)) {
                skipped++;
                continue;
            }

            RealTimeDataEntity entity = new RealTimeDataEntity();
            entity.setEnergyType(item.getEnergyType());
            entity.setArea(StringUtils.hasText(item.getArea()) ? item.getArea() : DEFAULT_AREA);
            entity.setActualValue(item.getActualValue());
            entity.setUnit(StringUtils.hasText(item.getUnit()) ? item.getUnit() : "");
            entity.setCollectionTime(item.getCollectionTime());
            entity.setDeviceId(item.getDeviceId());
            entity.setStatus(resolveStatus(item.getQuality()));
            realTimeDataRepository.save(entity);
            received++;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("received", received);
        data.put("skipped", skipped);
        return Result.success("接收成功", data);
    }

    /**
     * 手动触发昨日能源成本汇总推送ERP（EMS→ERP 能源成本闭环，便于联调测试）
     * POST /api/v1/ems/integration/erp/report-cost-now
     *
     * @return 推送统计结果（period/groups/pushed/failed）
     */
    @PostMapping("/erp/report-cost-now")
    public Result<Map<String, Object>> reportEnergyCostNow() {
        return Result.success("触发成功", energyCostReportJob.reportNow());
    }

    /**
     * 判断推送数据是否已存在（幂等键：能源类型+设备ID+采集时间）
     *
     * @param item 推送数据项
     * @return true-已存在
     */
    private boolean isDuplicate(ScadaEnergyDataItem item) {
        if (item.getDeviceId() != null) {
            return realTimeDataRepository.existsByEnergyTypeAndDeviceIdAndCollectionTime(
                    item.getEnergyType(), item.getDeviceId(), item.getCollectionTime());
        }
        return realTimeDataRepository.existsByEnergyTypeAndDeviceIdIsNullAndCollectionTime(
                item.getEnergyType(), item.getCollectionTime());
    }

    /**
     * 根据SCADA点位质量位解析数据状态
     *
     * @param quality 质量位
     * @return normal-正常，abnormal-异常
     */
    private String resolveStatus(String quality) {
        if ("bad".equalsIgnoreCase(quality) || "abnormal".equalsIgnoreCase(quality)) {
            return "abnormal";
        }
        return "normal";
    }

    /**
     * SCADA能源数据推送请求体
     */
    @Data
    public static class ScadaEnergyDataPushRequest {
        /**
         * 事件ID（UUID，用于链路追踪）
         */
        private String eventId;
        /**
         * 能源数据项列表
         */
        private List<ScadaEnergyDataItem> items;
    }

    /**
     * SCADA能源数据项
     */
    @Data
    public static class ScadaEnergyDataItem {
        /**
         * 能源类型（电力、水、燃气、热能）
         */
        private String energyType;
        /**
         * 采集区域
         */
        private String area;
        /**
         * 采集值
         */
        private Double actualValue;
        /**
         * 单位
         */
        private String unit;
        /**
         * 采集时间
         */
        private LocalDateTime collectionTime;
        /**
         * 设备ID（可空）
         */
        private Long deviceId;
        /**
         * 设备编码（仅作透传参考，不参与落库）
         */
        private String deviceCode;
        /**
         * 质量位（good/bad/abnormal）
         */
        private String quality;
    }
}
