package com.hxcoe.aibrain.controller;

import com.hxcoe.aibrain.entity.AiSuggestionEntity;
import com.hxcoe.aibrain.service.DecisionLogService;
import com.hxcoe.aibrain.service.event.EventTriggerService;
import com.hxcoe.aibrain.service.inventory.DynamicSafetyStockService;
import com.hxcoe.aibrain.service.inventory.StockShortageService;
import com.hxcoe.aibrain.service.preaudit.ApprovalPreauditService;
import com.hxcoe.aibrain.service.prediction.DeliveryRiskService;
import com.hxcoe.aibrain.service.prediction.EnergyLoadService;
import com.hxcoe.aibrain.service.prediction.FaultPredictionService;
import com.hxcoe.aibrain.service.quality.EightDService;
import com.hxcoe.aibrain.service.rca.RootCauseService;
import com.hxcoe.aibrain.service.sentinel.DataQualitySentinelService;
import com.hxcoe.common.result.Result;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI 决策引擎统一端点
 * 各决策技能的扫描/生成入口（AUTO 级建议自动落库，CONFIRM 级动作草稿待人工确认）
 */
@RestController
@RequestMapping("/api/v1/ai-brain/decision")
public class DecisionController {

    @Autowired
    private FaultPredictionService faultPredictionService;

    @Autowired
    private DeliveryRiskService deliveryRiskService;

    @Autowired
    private EnergyLoadService energyLoadService;

    @Autowired
    private ApprovalPreauditService approvalPreauditService;

    @Autowired
    private RootCauseService rootCauseService;

    @Autowired
    private DynamicSafetyStockService dynamicSafetyStockService;

    @Autowired
    private StockShortageService stockShortageService;

    @Autowired
    private EightDService eightDService;

    @Autowired
    private DataQualitySentinelService dataQualitySentinelService;

    @Autowired
    private DecisionLogService decisionLogService;

    @Autowired
    private EventTriggerService eventTriggerService;

    /** P4-3：手动触发事件驱动轮询（增量消费各模块新增事件并路由技能） */
    @PostMapping("/event-trigger/run")
    public Result<Integer> runEventTrigger() {
        return Result.success("事件轮询完成",
                decisionLogService.record("EVENT_TRIGGER", "MANUAL", () -> eventTriggerService.pollAll()));
    }

    /** S01：手动触发设备故障预测扫描 */
    @PostMapping("/fault-prediction/run")
    public Result<List<AiSuggestionEntity>> runFaultPrediction() {
        return Result.success("扫描完成",
                decisionLogService.record("FAULT_PREDICTION", "MANUAL", () -> faultPredictionService.predict()));
    }

    /** S02：手动触发交期风险扫描 */
    @PostMapping("/delivery-risk/run")
    public Result<Integer> runDeliveryRisk() {
        return Result.success("扫描完成",
                decisionLogService.record("DELIVERY_RISK", "MANUAL", () -> deliveryRiskService.scan()));
    }

    /** S04：手动触发能耗负荷预测 */
    @PostMapping("/energy-load/run")
    public Result<AiSuggestionEntity> runEnergyLoad() {
        return Result.success("预测完成",
                decisionLogService.record("ENERGY_LOAD", "MANUAL", () -> energyLoadService.predictTomorrow()));
    }

    /** S07：手动触发审批预审扫描 */
    @PostMapping("/preaudit/scan")
    public Result<Integer> runPreaudit() {
        return Result.success("扫描完成",
                decisionLogService.record("PREAUDIT", "MANUAL", () -> approvalPreauditService.scanPendingTasks()));
    }

    /** S07：查询指定审批实例的预审意见（供 OA 审批详情页调用） */
    @GetMapping("/preaudit/{instanceId}")
    public Result<AiSuggestionEntity> getPreaudit(@PathVariable Long instanceId) {
        return Result.success("查询成功", approvalPreauditService.getByInstanceId(instanceId));
    }

    /** S06：以故障记录为起点的跨域根因追溯 */
    @GetMapping("/root-cause/fault/{faultId}")
    public Result<Map<String, Object>> rootCauseByFault(@PathVariable Long faultId) {
        return Result.success("追溯完成", rootCauseService.traceByFault(faultId));
    }

    /** S03：动态安全库存扫描 */
    @PostMapping("/safety-stock/run")
    public Result<Integer> runSafetyStock() {
        return Result.success("扫描完成",
                decisionLogService.record("SAFETY_STOCK", "MANUAL", () -> dynamicSafetyStockService.scan()));
    }

    /** 备件缺货决策闭环扫描（EAM→SRM 草稿） */
    @PostMapping("/stock-shortage/run")
    public Result<Integer> runStockShortage() {
        return Result.success("扫描完成",
                decisionLogService.record("STOCK_SHORTAGE", "MANUAL", () -> stockShortageService.scan()));
    }

    /** S09：为不合格品登记单生成 8D 骨架并落建议 */
    @PostMapping("/eight-d/{ncId}")
    public Result<AiSuggestionEntity> generateEightD(@PathVariable Long ncId) throws Exception {
        return Result.success("生成完成",
                decisionLogService.record("EIGHT_D", "MANUAL", () -> {
                    try {
                        return eightDService.generateAndPublish(ncId);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }));
    }

    /** S12：数据质量哨兵全模块对账扫描（EAM/ERP/WMS/MES） */
    @PostMapping("/dq-sentinel/run")
    public Result<Integer> runDqSentinel() {
        return Result.success("扫描完成",
                decisionLogService.record("DQ_SENTINEL", "MANUAL", () -> dataQualitySentinelService.scan()));
    }
}
