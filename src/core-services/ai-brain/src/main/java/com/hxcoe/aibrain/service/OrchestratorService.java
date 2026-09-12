package com.hxcoe.aibrain.service;

import com.hxcoe.aibrain.service.expert.EquipmentExpert;
import com.hxcoe.aibrain.service.expert.ExpertFinding;
import com.hxcoe.aibrain.service.expert.InventoryExpert;
import com.hxcoe.aibrain.service.expert.ProcurementExpert;
import com.hxcoe.aibrain.service.expert.ProductionExpert;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 多专家编排器（P4-6 多 Agent 编排核心）
 * 参照 Palantir 编排 Agent 模式：接收问题 → 委派领域专家并行会诊 → 汇总交叉验证
 * - CompletableFuture 并行调用各 Specialist（替代 S06 单服务串行查询）
 * - 单专家异常隔离：降级为 UNKNOWN 结论，不阻塞整体会诊
 * - 交叉验证：汇总各领域状态，识别"单领域看合理、串起来见系统性失效"的组合风险
 */
@Slf4j
@Service
public class OrchestratorService {

    /** 专家会诊线程池（固定 4 线程对齐专家数，守护线程不阻塞 JVM 退出） */
    private final Executor consultPool = Executors.newFixedThreadPool(4, r -> {
        Thread t = new Thread(r, "expert-consult");
        t.setDaemon(true);
        return t;
    });

    @Autowired
    private EquipmentExpert equipmentExpert;

    @Autowired
    private InventoryExpert inventoryExpert;

    @Autowired
    private ProcurementExpert procurementExpert;

    @Autowired
    private ProductionExpert productionExpert;

    /**
     * 跨域联合会诊：以设备为中心，并行召集设备/库存/采购/生产四领域专家
     *
     * @param equipmentId   设备ID（可空）
     * @param equipmentName 设备名称
     * @return 各领域会诊结论（固定 4 份，异常领域为 UNKNOWN）
     */
    public List<ExpertFinding> consultCrossDomain(Object equipmentId, String equipmentName) {
        long start = System.currentTimeMillis();
        CompletableFuture<ExpertFinding> equip = async(() -> equipmentExpert.assessFaultHistory(equipmentId, equipmentName), "EQUIPMENT");
        CompletableFuture<ExpertFinding> inv = async(() -> inventoryExpert.assessLowSpares(), "INVENTORY");
        CompletableFuture<ExpertFinding> proc = async(() -> procurementExpert.assessOverdueOrders(), "PROCUREMENT");
        CompletableFuture<ExpertFinding> prod = async(() -> productionExpert.assessAffectedOrders(equipmentName), "PRODUCTION");

        List<ExpertFinding> findings = List.of(equip.join(), inv.join(), proc.join(), prod.join());
        log.info("多专家会诊完成: equipment={}, 耗时{}ms, 结论=[{}]",
                equipmentName, System.currentTimeMillis() - start,
                findings.stream().map(f -> f.getDomain() + ":" + f.getStatus()).reduce((a, b) -> a + ", " + b).orElse(""));
        return findings;
    }

    /**
     * 交叉验证：识别组合风险（单领域不严重但多领域叠加成系统性失效）
     *
     * @param findings 各领域结论
     * @return 组合风险摘要（无组合风险返回 null）
     */
    public String crossValidate(List<ExpertFinding> findings) {
        long warnOrWorse = findings.stream().filter(f -> !"OK".equals(f.getStatus()) && !"UNKNOWN".equals(f.getStatus())).count();
        boolean invRisk = findings.stream().anyMatch(f -> "INVENTORY".equals(f.getDomain()) && !"OK".equals(f.getStatus()) && !"UNKNOWN".equals(f.getStatus()));
        boolean procRisk = findings.stream().anyMatch(f -> "PROCUREMENT".equals(f.getDomain()) && !"OK".equals(f.getStatus()) && !"UNKNOWN".equals(f.getStatus()));
        if (invRisk && procRisk) {
            return "组合风险：备件库存不足与采购逾期叠加——设备维护面临'无件可换+补给受阻'双重保障失效";
        }
        if (warnOrWorse >= 3) {
            return "组合风险：设备/库存/生产多领域同时告警，存在系统性失效链";
        }
        return null;
    }

    /** 异步执行单专家评估，异常隔离为 UNKNOWN 结论 */
    private CompletableFuture<ExpertFinding> async(Supplier<ExpertFinding> task, String domain) {
        return CompletableFuture.supplyAsync(task, consultPool)
                .exceptionally(e -> {
                    log.warn("领域专家会诊异常（隔离不阻断）: {} - {}", domain, e.getMessage());
                    return ExpertFinding.unknown(domain, e.getMessage());
                });
    }
}
