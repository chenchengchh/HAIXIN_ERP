package com.hxcoe.aibrain.controller;

import com.hxcoe.aibrain.service.DecisionLogService;
import com.hxcoe.aibrain.service.llm.LlmService;
import com.hxcoe.aibrain.service.ontology.OntologyQueryService;
import com.hxcoe.common.result.Result;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI 决策中枢状态端点（P0 骨架）
 * 提供服务能力清单与健康状态，供前端决策中心页与网关探活使用
 */
@RestController
@RequestMapping("/api/v1/ai-brain")
public class BrainStatusController {

    @Autowired
    private LlmService llmService;

    @Autowired
    private DecisionLogService decisionLogService;

    @Autowired
    private OntologyQueryService ontologyQueryService;

    /**
     * 本体注册表查询（P4-4 决策本体层）
     * 返回全部已注册对象及其可信度状态（trusted=0 的对象已被 S12 哨兵暂停对 AI 开放）
     *
     * @return 本体对象清单
     */
    @GetMapping("/ontology/objects")
    public Result<List<Map<String, Object>>> ontologyObjects() {
        return Result.success("查询成功", ontologyQueryService.listRegistered());
    }

    /**
     * 技能健康看板（P4-8 决策可观测性）
     * 各技能近 24h/7d 执行次数、成功率、平均耗时、建议产出数；连续 3 次 ERROR 标红
     *
     * @return 技能健康聚合数据
     */
    @GetMapping("/skills/health")
    public Result<Map<String, Object>> skillHealth() {
        return Result.success("查询成功", decisionLogService.skillHealth());
    }

    /**
     * 服务状态与能力清单
     *
     * @return 统一信封 {success, evidence, capabilities, automationDial}
     */
    @GetMapping("/status")
    public Result<Map<String, Object>> status() {
        Map<String, Object> data = new HashMap<>();
        data.put("success", true);
        data.put("service", "ai-brain");
        // 当前最高已上线期次（P4-1/P4-2/P4-8 最小启动包上线后进入 P4 阶段）
        data.put("phase", "P4");
        // LLM 翻译层当前提供者（rule=模板降级实现，未来可切换 Spring AI 实现）
        data.put("llmProvider", llmService.provider());

        // 自动化刻度盘分级说明（治理原则对外可见）
        Map<String, String> dial = new HashMap<>();
        dial.put("AUTO", "只读分析与告警，自动执行");
        dial.put("CONFIRM", "生成单据草稿，人工确认后经现有端点执行");
        dial.put("FORBIDDEN", "删除/直改库存金额，永不开放");
        data.put("automationDial", dial);

        // 已上线能力（随期次演进）
        data.put("capabilities", List.of(
                Map.of("code", "S12-DQ-SENTINEL", "name", "数据质量哨兵(EAM)", "phase", "P0", "status", "ONLINE",
                        "endpoint", "/api/v1/eam/admin/data-quality/tasks"),
                Map.of("code", "S08-MAINT-COPILOT", "name", "维修Copilot(规则版)", "phase", "P0", "status", "ONLINE",
                        "endpoint", "/api/v1/eam/copilot/fault-advice"),
                Map.of("code", "S05-MORNING-BRIEFING", "name", "AI晨会简报(六域巡检)", "phase", "P1", "status", "ONLINE",
                        "endpoint", "/api/v1/ai-brain/briefing/generate"),
                Map.of("code", "S01-FAULT-PREDICTION", "name", "设备故障预测(时序漂移)", "phase", "P1", "status", "ONLINE",
                        "endpoint", "/api/v1/ai-brain/decision/fault-prediction/run"),
                Map.of("code", "S07-APPROVAL-PREAUDIT", "name", "审批预审员(预算/资质核查)", "phase", "P1", "status", "ONLINE",
                        "endpoint", "/api/v1/ai-brain/decision/preaudit/scan"),
                Map.of("code", "S06-ROOT-CAUSE", "name", "跨域根因会诊(本体图谱)", "phase", "P2", "status", "ONLINE",
                        "endpoint", "/api/v1/ai-brain/decision/root-cause/fault/{faultId}"),
                Map.of("code", "S02-DELIVERY-RISK", "name", "交期风险预测(SRM+LES+MES)", "phase", "P2", "status", "ONLINE",
                        "endpoint", "/api/v1/ai-brain/decision/delivery-risk/run"),
                Map.of("code", "S13-FEEDBACK-LOOP", "name", "决策反馈学习环", "phase", "P2", "status", "ONLINE",
                        "endpoint", "/api/v1/ai-brain/suggestions/adoption-stats"),
                Map.of("code", "S04-ENERGY-LOAD", "name", "能耗负荷预测(EMS+MES)", "phase", "P2", "status", "ONLINE",
                        "endpoint", "/api/v1/ai-brain/decision/energy-load/run"),
                Map.of("code", "S03-SAFETY-STOCK", "name", "动态安全库存", "phase", "P3", "status", "ONLINE",
                        "endpoint", "/api/v1/ai-brain/decision/safety-stock/run"),
                Map.of("code", "S09-EIGHT-D", "name", "质量8D骨架生成", "phase", "P3", "status", "ONLINE",
                        "endpoint", "/api/v1/ai-brain/decision/eight-d/{ncId}"),
                Map.of("code", "S10-STOCK-SHORTAGE", "name", "备件缺货闭环(EAM→SRM草稿)", "phase", "P2", "status", "ONLINE",
                        "endpoint", "/api/v1/ai-brain/decision/stock-shortage/run"),
                Map.of("code", "S12-DQ-SENTINEL-ALL", "name", "数据质量哨兵(全模块)", "phase", "P2", "status", "ONLINE",
                        "endpoint", "/api/v1/ai-brain/decision/dq-sentinel/run"),
                Map.of("code", "S10-VOICE-WORKSHOP", "name", "语音车间(备件查询+异常记录)", "phase", "P3", "status", "ONLINE",
                        "endpoint", "/ai-voice/ws/"),
                Map.of("code", "S11-DOC-VISION", "name", "单据/仪表视觉理解(规则版)", "phase", "P3", "status", "ONLINE",
                        "endpoint", "/api/v1/ai-brain/vision/parse-document"),
                Map.of("code", "P4-1-ACTION-ENGINE", "name", "动作执行引擎(一键确认闭环)", "phase", "P4", "status", "ONLINE",
                        "endpoint", "/api/v1/ai-brain/suggestions/{id}/execute"),
                Map.of("code", "P4-2-FEEDBACK-TUNER", "name", "反馈学习环(参数自动调优)", "phase", "P4", "status", "ONLINE",
                        "endpoint", "/api/v1/ai-brain/suggestions/adoption-stats"),
                Map.of("code", "P4-8-DECISION-OBSERVE", "name", "决策可观测性(技能健康看板)", "phase", "P4", "status", "ONLINE",
                        "endpoint", "/api/v1/ai-brain/skills/health"),
                Map.of("code", "P4-3-EVENT-TRIGGER", "name", "事件驱动触发(Outbox水位消费)", "phase", "P4", "status", "ONLINE",
                        "endpoint", "/api/v1/ai-brain/decision/event-trigger/run"),
                Map.of("code", "P4-4-ONTOLOGY", "name", "决策本体层(对象注册+可信闸门)", "phase", "P4", "status", "ONLINE",
                        "endpoint", "/api/v1/ai-brain/ontology/objects"),
                Map.of("code", "P4-5-EVIDENCE-GRAPH", "name", "结构化证据链(行级引用下钻)", "phase", "P4", "status", "ONLINE",
                        "endpoint", "/api/v1/ai-brain/suggestions"),
                Map.of("code", "P4-6-ORCHESTRATION", "name", "多Agent编排(跨域会诊+组合建议)", "phase", "P4", "status", "ONLINE",
                        "endpoint", "/api/v1/ai-brain/suggestions/{id}/execute?actionIndex="),
                Map.of("code", "P4-7-LLM-GATEWAY", "name", "本地LLM接入(Ollama语义增强+静默降级)", "phase", "P4", "status", "ONLINE",
                        "endpoint", "/api/v1/ai-brain/llm/skill-chat")
        ));

        Map<String, Object> evidence = new HashMap<>();
        evidence.put("queryTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        data.put("evidence", evidence);

        return Result.success("查询成功", data);
    }
}
