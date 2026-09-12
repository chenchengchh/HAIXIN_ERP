package com.hxcoe.aibrain.service.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hxcoe.aibrain.entity.AiEventWatermarkEntity;
import com.hxcoe.aibrain.entity.AiSuggestionEntity;
import com.hxcoe.aibrain.repository.AiEventWatermarkRepository;
import com.hxcoe.aibrain.service.SuggestionService;
import com.hxcoe.aibrain.service.inventory.DynamicSafetyStockService;
import com.hxcoe.aibrain.service.preaudit.ApprovalPreauditService;
import com.hxcoe.aibrain.service.prediction.DeliveryRiskService;
import com.hxcoe.aibrain.service.prediction.FaultPredictionService;
import com.hxcoe.aibrain.service.quality.EightDService;
import com.hxcoe.aibrain.service.rca.RootCauseService;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * 事件驱动触发服务（P4-3 核心）
 * 职责：以 60s 间隔增量消费各模块"新增业务记录/Outbox 事件"，按路由表即时触发对应 AI 技能，
 *       将关键事件响应时延从"定时轮询 30min~2h"压缩到 2 分钟以内；定时任务降级为兜底补扫。
 * 机制：每事件源一条水位记录（ai_event_watermark，按主键ID增量），首次消费初始化为当前 MAX(id)
 *       不回溯历史；单行处理失败仅记录日志并推进水位，避免毒消息阻塞后续事件。
 * 路由表（事件源 → 技能）：
 *   EAM 故障记录新增   → S06 根因分析（跨域链≥3环节才发建议）+ S01 故障预测复评
 *   SRM 采购订单新增   → S02 交期风险评估（技能内按 eventId 幂等）
 *   OA 审批任务新增    → S07 审批预审（技能内按实例幂等）
 *   QMS 不合格登记新增 → S09 8D 骨架预生成（CONFIRM 级）
 *   WMS/QMS/LES/HR Outbox 新增事件 → 按事件类型路由（未知类型仅推进水位）
 */
@Slf4j
@Service
public class EventTriggerService {

    /** 单轮每源最多消费行数（防突发洪峰拖垮轮询） */
    private static final int BATCH_LIMIT = 100;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private AiEventWatermarkRepository watermarkRepository;

    @Autowired
    private SuggestionService suggestionService;

    @Autowired
    private RootCauseService rootCauseService;

    @Autowired
    private FaultPredictionService faultPredictionService;

    @Autowired
    private DeliveryRiskService deliveryRiskService;

    @Autowired
    private ApprovalPreauditService approvalPreauditService;

    @Autowired
    private EightDService eightDService;

    @Autowired
    private DynamicSafetyStockService safetyStockService;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 业务表事件源（无 Outbox 的模块，按主键增量轮询）
     * sourceKey -> 查询SQL（:wm 为水位占位）
     */
    private static final Map<String, String> TABLE_SOURCES = new HashMap<>() {{
            put("EAM_FAULT_RECORD",
                    "SELECT id, equipment_name, type, description FROM eam_db.eam_fault_record "
                            + "WHERE id > :wm ORDER BY id ASC LIMIT " + BATCH_LIMIT);
            put("SRM_PURCHASE_ORDER",
                    "SELECT id, order_no, supplier_name FROM srm_db.srm_purchase_order "
                            + "WHERE id > :wm ORDER BY id ASC LIMIT " + BATCH_LIMIT);
            put("OA_APPROVAL_TASK",
                    "SELECT id, instance_id, node_name FROM oa_db.oa_approval_task "
                            + "WHERE id > :wm ORDER BY id ASC LIMIT " + BATCH_LIMIT);
            put("QMS_NC_REGISTRATION",
                    "SELECT id, registration_no FROM qms_db.qms_nc_registration "
                            + "WHERE id > :wm ORDER BY id ASC LIMIT " + BATCH_LIMIT);
        }};

    /** 业务表事件源对应的最大ID查询SQL（水位初始化用） */
    private static final Map<String, String> TABLE_MAX_ID_SQL = new HashMap<>() {{
            put("EAM_FAULT_RECORD", "SELECT COALESCE(MAX(id), 0) FROM eam_db.eam_fault_record");
            put("SRM_PURCHASE_ORDER", "SELECT COALESCE(MAX(id), 0) FROM srm_db.srm_purchase_order");
            put("OA_APPROVAL_TASK", "SELECT COALESCE(MAX(id), 0) FROM oa_db.oa_approval_task");
            put("QMS_NC_REGISTRATION", "SELECT COALESCE(MAX(id), 0) FROM qms_db.qms_nc_registration");
            put("WMS_OUTBOX", "SELECT COALESCE(MAX(id), 0) FROM wms_db.wms_integration_outbox");
            put("QMS_OUTBOX", "SELECT COALESCE(MAX(id), 0) FROM qms_db.qms_integration_outbox");
            put("LES_OUTBOX", "SELECT COALESCE(MAX(id), 0) FROM les_db.les_integration_outbox");
            put("HR_OUTBOX", "SELECT COALESCE(MAX(id), 0) FROM hr_db.hr_integration_outbox");
        }};

    /** Outbox 事件源（有 Outbox 的模块，消费 NEW/未消费事件按类型路由） */
    private static final Map<String, String> OUTBOX_SOURCES = new HashMap<>() {{
            put("WMS_OUTBOX",
                    "SELECT id, event_type, ref_no, entity_id FROM wms_db.wms_integration_outbox "
                            + "WHERE id > :wm ORDER BY id ASC LIMIT " + BATCH_LIMIT);
            put("QMS_OUTBOX",
                    "SELECT id, event_type, ref_no, entity_id FROM qms_db.qms_integration_outbox "
                            + "WHERE id > :wm ORDER BY id ASC LIMIT " + BATCH_LIMIT);
            put("LES_OUTBOX",
                    "SELECT id, event_type, ref_no, entity_id FROM les_db.les_integration_outbox "
                            + "WHERE id > :wm ORDER BY id ASC LIMIT " + BATCH_LIMIT);
            put("HR_OUTBOX",
                    "SELECT id, event_type, ref_no, entity_id FROM hr_db.hr_integration_outbox "
                            + "WHERE id > :wm ORDER BY id ASC LIMIT " + BATCH_LIMIT);
        }};

    /**
     * 全量事件轮询入口（由 DecisionJob 每 60s 调用，亦可手动触发）
     *
     * @return 本轮消费的事件总数（用于决策日志命中计数）
     */
    public int pollAll() {
        int total = 0;
        Map<String, Integer> summary = new HashMap<>();
        for (Map.Entry<String, String> src : TABLE_SOURCES.entrySet()) {
            int n = pollSource(src.getKey(), src.getValue(), false);
            total += n;
            if (n > 0) {
                summary.put(src.getKey(), n);
            }
        }
        for (Map.Entry<String, String> src : OUTBOX_SOURCES.entrySet()) {
            int n = pollSource(src.getKey(), src.getValue(), true);
            total += n;
            if (n > 0) {
                summary.put(src.getKey(), n);
            }
        }
        if (total > 0) {
            log.info("事件驱动触发: 本轮消费 {} 条新事件, 明细={}", total, summary);
        }
        return total;
    }

    /**
     * 消费单个事件源的增量数据
     *
     * @param sourceKey 事件源标识
     * @param querySql 增量查询SQL
     * @param outbox 是否 Outbox 事件源
     * @return 本轮消费条数
     */
    private int pollSource(String sourceKey, String querySql, boolean outbox) {
        try {
            long watermark = loadWatermark(sourceKey);
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                    querySql, new MapSqlParameterSource().addValue("wm", watermark));
            if (rows.isEmpty()) {
                return 0;
            }
            long maxId = watermark;
            int consumed = 0;
            for (Map<String, Object> row : rows) {
                long rowId = ((Number) row.get("id")).longValue();
                try {
                    if (outbox) {
                        routeOutboxEvent(sourceKey, row);
                    } else {
                        routeTableEvent(sourceKey, row);
                    }
                } catch (Exception e) {
                    // 单行失败不阻塞：记录日志并继续推进水位（毒消息隔离）
                    log.warn("事件路由失败 source={} id={}: {}", sourceKey, rowId, e.getMessage());
                }
                maxId = Math.max(maxId, rowId);
                consumed++;
            }
            saveWatermark(sourceKey, maxId);
            return consumed;
        } catch (Exception e) {
            // 源级失败（如目标表暂不可达）：跳过本轮，下轮重试，不影响其他源
            log.warn("事件源轮询失败 source={}: {}", sourceKey, e.getMessage());
            return 0;
        }
    }

    /**
     * 读取事件源水位：不存在则初始化为源表当前 MAX(id)（历史不回溯）
     */
    private long loadWatermark(String sourceKey) {
        return watermarkRepository.findById(sourceKey)
                .map(AiEventWatermarkEntity::getWatermarkId)
                .orElseGet(() -> {
                    String maxSql = TABLE_MAX_ID_SQL.get(sourceKey);
                    Long maxId = maxSql == null ? 0L
                            : jdbcTemplate.queryForObject(maxSql, new MapSqlParameterSource(), Long.class);
                    long init = maxId == null ? 0L : maxId;
                    saveWatermark(sourceKey, init);
                    log.info("事件源水位初始化 source={}, 起始水位={}", sourceKey, init);
                    return init;
                });
    }

    /**
     * 推进事件源水位（JpaRepository.save 自带事务，单行写入无需外层事务）
     */
    public void saveWatermark(String sourceKey, long watermarkId) {
        AiEventWatermarkEntity wm = watermarkRepository.findById(sourceKey)
                .orElseGet(AiEventWatermarkEntity::new);
        wm.setSourceKey(sourceKey);
        wm.setWatermarkId(watermarkId);
        wm.setUpdatedTime(LocalDateTime.now());
        watermarkRepository.save(wm);
    }

    /**
     * 业务表事件路由：按事件源触发对应技能（技能内部按 eventId 幂等去重）
     */
    private void routeTableEvent(String sourceKey, Map<String, Object> row) throws Exception {
        long id = ((Number) row.get("id")).longValue();
        switch (sourceKey) {
            case "EAM_FAULT_RECORD" -> {
                // S06 根因分析：跨域失效链≥3环节才生成建议（单点故障不打扰）
                Map<String, Object> rca = rootCauseService.traceByFault(id);
                if (Boolean.TRUE.equals(rca.get("success"))
                        && ((Number) rca.getOrDefault("chainLength", 0)).intValue() >= 3) {
                    publishRootCauseSuggestion(id, row, rca);
                }
                // S01 故障预测复评：新故障发生后立即重估漂移（幂等）
                faultPredictionService.predict();
                log.info("事件触发[EAM故障]: id={}, 设备={}, 根因链长度={}",
                        id, row.get("equipment_name"), rca.get("chainLength"));
            }
            case "SRM_PURCHASE_ORDER" -> {
                // S02 交期风险评估（新订单立即评估，eventId 按订单+日期幂等）
                deliveryRiskService.scan();
                log.info("事件触发[SRM订单]: id={}, 单号={}, 供应商={}", id, row.get("order_no"), row.get("supplier_name"));
            }
            case "OA_APPROVAL_TASK" -> {
                // S07 审批预审（新任务立即预审，eventId 按实例幂等）
                approvalPreauditService.scanPendingTasks();
                log.info("事件触发[OA任务]: id={}, 实例={}, 节点={}", id, row.get("instance_id"), row.get("node_name"));
            }
            case "QMS_NC_REGISTRATION" -> {
                // S09 8D 骨架预生成（CONFIRM 级，eventId 按NC单幂等）
                eightDService.generateAndPublish(id);
                log.info("事件触发[QMS不合格]: id={}, 登记号={}", id, row.get("registration_no"));
            }
            default -> log.debug("未路由的业务表事件源: {}", sourceKey);
        }
    }

    /**
     * Outbox 事件路由：按事件类型映射技能（未知类型仅推进水位，为后续模块事件接入预留）
     */
    private void routeOutboxEvent(String sourceKey, Map<String, Object> row) {
        String eventType = String.valueOf(row.get("event_type"));
        // WMS 库存低于安全水位事件 → S03 安全库存复评（技能内按备件+日期幂等）
        if (eventType != null && eventType.toUpperCase().contains("STOCK")
                && eventType.toUpperCase().contains("SAFETY")) {
            safetyStockService.scan();
            log.info("事件触发[Outbox库存]: source={}, type={}, refNo={}", sourceKey, eventType, row.get("ref_no"));
            return;
        }
        // 其他事件类型暂无技能映射：记录并推进水位（路由表配置化扩展点）
        log.debug("Outbox事件暂无技能映射: source={}, type={}, refNo={}", sourceKey, eventType, row.get("ref_no"));
    }

    /**
     * 发布 S06 根因分析建议（AUTO 级，跨域失效链预警）
     */
    private void publishRootCauseSuggestion(long faultId, Map<String, Object> fault,
                                            Map<String, Object> rca) throws Exception {
        AiSuggestionEntity s = new AiSuggestionEntity();
        s.setSuggestionType("ROOT_CAUSE");
        s.setModule("EAM");
        s.setSeverity("WARNING");
        s.setTitle(String.format("根因分析：%s【%s】发现跨域失效链（%d 环节）",
                fault.get("equipment_name"), fault.get("type"), rca.get("chainLength")));
        s.setAutomationLevel("AUTO");
        s.setEventId("ROOT-CAUSE-FAULT-" + faultId);
        Map<String, Object> analysis = new HashMap<>();
        analysis.put("algorithm", "跨域联合会诊（故障→工单→备件→采购→重复性失效）");
        analysis.put("conclusion", rca.get("conclusion"));
        analysis.put("chain", rca.get("chain"));
        analysis.put("evidence", rca.get("evidence"));
        s.setAnalysis(objectMapper.writeValueAsString(analysis));
        suggestionService.publish(s);
    }
}
