package com.hxcoe.aibrain.job;

import com.hxcoe.aibrain.service.DecisionLogService;
import com.hxcoe.aibrain.service.briefing.MorningBriefingService;
import com.hxcoe.aibrain.service.event.EventTriggerService;
import com.hxcoe.aibrain.service.inventory.DynamicSafetyStockService;
import com.hxcoe.aibrain.service.inventory.StockShortageService;
import com.hxcoe.aibrain.service.prediction.DeliveryRiskService;
import com.hxcoe.aibrain.service.prediction.EnergyLoadService;
import com.hxcoe.aibrain.service.prediction.FaultPredictionService;
import com.hxcoe.aibrain.service.preaudit.ApprovalPreauditService;
import com.hxcoe.aibrain.service.sentinel.DataQualitySentinelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * AI 决策统一调度任务（自动化刻度盘 AUTO 级执行器）
 * <p>各技能按业务节奏错峰巡检，异常时仅记录日志不抛出，避免单点失败中断其他技能。</p>
 * <ul>
 *   <li>晨会简报：每日 08:30 六域巡检</li>
 *   <li>设备故障预测：每小时 SCADA 时序漂移扫描</li>
 *   <li>数据质量哨兵：每小时跨模块对账</li>
 *   <li>交期风险预测：每 2 小时 SRM+LES+MES 联合扫描</li>
 *   <li>能耗负荷预测：每 2 小时 EMS+MES 趋势分析</li>
 *   <li>安全库存：每日 09:00 WMS 库存水位校验</li>
 *   <li>备件缺货闭环：每日 09:30 EAM 备件阈值扫描</li>
 *   <li>审批预审：每 30 分钟待审批任务核查</li>
 * </ul>
 */
@Slf4j
@Component
public class DecisionJob {

    @Autowired
    private MorningBriefingService morningBriefingService;

    @Autowired
    private FaultPredictionService faultPredictionService;

    @Autowired
    private DataQualitySentinelService dataQualitySentinelService;

    @Autowired
    private DeliveryRiskService deliveryRiskService;

    @Autowired
    private EnergyLoadService energyLoadService;

    @Autowired
    private DynamicSafetyStockService safetyStockService;

    @Autowired
    private StockShortageService stockShortageService;

    @Autowired
    private ApprovalPreauditService approvalPreauditService;

    @Autowired
    private DecisionLogService decisionLogService;

    @Autowired
    private EventTriggerService eventTriggerService;

    /**
     * 事件驱动触发（P4-3）：每 60s 增量消费各模块新增业务记录/Outbox 事件，
     * 关键事件 2 分钟内触发对应技能；定时巡检降级为兜底补扫
     */
    @Scheduled(fixedDelay = 60000L, initialDelay = 60000L)
    public void eventTrigger() {
        try {
            decisionLogService.record("EVENT_TRIGGER", "EVENT", () -> eventTriggerService.pollAll());
        } catch (Exception e) {
            log.error("事件驱动触发轮询失败: {}", e.getMessage(), e);
        }
    }

    /**
     * AI 晨会简报：每日 08:30 六域巡检（AUTO 级，只读聚合+建议落库）
     */
    @Scheduled(cron = "0 30 8 * * ?")
    public void morningBriefing() {
        try {
            decisionLogService.record("MORNING_BRIEFING", "SCHEDULED", () -> morningBriefingService.generate());
        } catch (Exception e) {
            log.error("AI晨会简报巡检失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 设备故障预测：每小时扫描 SCADA 时序漂移（AUTO 级生成建议，工单创建走 CONFIRM）
     */
    @Scheduled(fixedDelay = 3600000L, initialDelay = 300000L)
    public void faultPrediction() {
        try {
            decisionLogService.record("FAULT_PREDICTION", "SCHEDULED", () -> faultPredictionService.predict());
        } catch (Exception e) {
            log.error("设备故障预测巡检失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 数据质量哨兵：每小时跨模块对账（AUTO 级只告警，EAM/ERP/WMS/MES 关键表）
     */
    @Scheduled(fixedDelay = 3600000L, initialDelay = 600000L)
    public void dataQualitySentinel() {
        try {
            decisionLogService.record("DQ_SENTINEL", "SCHEDULED", () -> dataQualitySentinelService.scan());
        } catch (Exception e) {
            log.error("数据质量哨兵巡检失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 交期风险预测：每 2 小时扫描 SRM 采购订单 + LES 运输 + MES 工单联合判定交期风险
     */
    @Scheduled(fixedDelay = 7200000L, initialDelay = 900000L)
    public void deliveryRisk() {
        try {
            decisionLogService.record("DELIVERY_RISK", "SCHEDULED", () -> deliveryRiskService.scan());
        } catch (Exception e) {
            log.error("交期风险预测巡检失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 能耗负荷预测：每 2 小时分析 EMS 能耗趋势 + MES 生产负荷，预测超限风险
     */
    @Scheduled(fixedDelay = 7200000L, initialDelay = 1200000L)
    public void energyLoad() {
        try {
            decisionLogService.record("ENERGY_LOAD", "SCHEDULED", () -> energyLoadService.predictTomorrow());
        } catch (Exception e) {
            log.error("能耗负荷预测巡检失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 安全库存校验：每日 09:00 扫描 WMS 库存水位，低于安全库存的生成补货建议
     */
    @Scheduled(cron = "0 0 9 * * ?")
    public void safetyStock() {
        try {
            decisionLogService.record("SAFETY_STOCK", "SCHEDULED", () -> safetyStockService.scan());
        } catch (Exception e) {
            log.error("安全库存巡检失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 备件缺货闭环：每日 09:30 扫描 EAM 备件库存，低于阈值的生成 SRM 采购申请草稿（CONFIRM 级）
     */
    @Scheduled(cron = "0 30 9 * * ?")
    public void stockShortage() {
        try {
            decisionLogService.record("STOCK_SHORTAGE", "SCHEDULED", () -> stockShortageService.scan());
        } catch (Exception e) {
            log.error("备件缺货闭环巡检失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 审批预审：每 30 分钟扫描待审批任务，生成预审意见（AUTO 级附加意见，审批权在人）
     */
    @Scheduled(fixedDelay = 1800000L, initialDelay = 1500000L)
    public void approvalPreaudit() {
        try {
            decisionLogService.record("PREAUDIT", "SCHEDULED", () -> approvalPreauditService.scanPendingTasks());
        } catch (Exception e) {
            log.error("审批预审巡检失败: {}", e.getMessage(), e);
        }
    }
}
