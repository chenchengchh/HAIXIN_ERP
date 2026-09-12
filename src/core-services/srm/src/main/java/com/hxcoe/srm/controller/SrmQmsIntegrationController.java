package com.hxcoe.srm.controller;

import com.hxcoe.common.result.Result;
import com.hxcoe.srm.entity.PurchaseOrderEntity;
import com.hxcoe.srm.entity.SrmQmsIqcMirrorEntity;
import com.hxcoe.srm.entity.SupplierPerformanceEntity;
import com.hxcoe.srm.repository.PurchaseOrderRepository;
import com.hxcoe.srm.repository.SrmQmsIqcMirrorRepository;
import com.hxcoe.srm.repository.SupplierPerformanceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * SRM接收QMS集成事件控制器
 *
 * <p>接收QMS推送的IQC检验结果，回写供应商质量绩效（累计检验批次、合格批次、合格率），
 * 形成 QMS→SRM 供应商质量绩效闭环。按检验单号幂等防重。
 */
@RestController
@RequestMapping({"/api/v1/srm/integration", "/srm/integration"})
public class SrmQmsIntegrationController {

    private static final Logger logger = LoggerFactory.getLogger(SrmQmsIntegrationController.class);

    @Autowired
    private SupplierPerformanceRepository supplierPerformanceRepository;

    @Autowired
    private SrmQmsIqcMirrorRepository qmsIqcMirrorRepository;

    @Autowired
    private PurchaseOrderRepository purchaseOrderRepository;

    /**
     * 接收QMS IQC检验结果并回写供应商质量绩效。
     *
     * <p>处理逻辑：
     * 1. 按 inspectionNo 在镜像表中判重，已处理则直接返回 duplicated；
     * 2. 确定供应商：优先取请求体 supplierId，缺失时按 poNo 查询采购订单兜底解析；
     * 3. 按 supplierId 查最新绩效记录，存在则累计更新质量指标，不存在则新建绩效记录；
     * 4. 写入幂等镜像并返回处理动作。
     *
     * @param body 检验结果事件体（eventId/supplierId/supplierName/materialCode/inspectionNo/
     *             result/inspectionDate/batchQuantity/rejectedQuantity，可选 poNo 兜底解析供应商）
     * @return 处理结果，data 携带 supplierId 与 action（created/updated/duplicated）
     */
    @PostMapping("/qms/inspection-result")
    public Result<Map<String, Object>> receiveInspectionResult(@RequestBody Map<String, Object> body) {
        Map<String, Object> data = new HashMap<>();
        if (body == null) {
            return Result.error("请求体不能为空");
        }
        String inspectionNo = asString(body.get("inspectionNo"));
        if (inspectionNo == null || inspectionNo.isBlank()) {
            return Result.error("inspectionNo 不能为空");
        }

        // 幂等判重：同一检验单号只累计一次绩效
        Optional<SrmQmsIqcMirrorEntity> mirrored = qmsIqcMirrorRepository.findByInspectionNo(inspectionNo);
        if (mirrored.isPresent()) {
            data.put("supplierId", mirrored.get().getSupplierId());
            data.put("action", "duplicated");
            return Result.success("检验结果已处理过，跳过重复累计", data);
        }

        // 确定供应商：优先 supplierId，缺失时按 poNo 查采购订单兜底
        Long supplierId = asLong(body.get("supplierId"));
        String supplierName = asString(body.get("supplierName"));
        if (supplierId == null) {
            String poNo = asString(body.get("poNo"));
            if (poNo != null && !poNo.isBlank()) {
                PurchaseOrderEntity po = purchaseOrderRepository.findByOrderNo(poNo);
                if (po != null) {
                    supplierId = po.getSupplierId();
                    if (supplierName == null) {
                        supplierName = po.getSupplierName();
                    }
                }
            }
        }
        if (supplierId == null) {
            logger.warn("QMS检验结果无法确定供应商，inspectionNo={}", inspectionNo);
            return Result.error("无法确定供应商（supplierId缺失且poNo未匹配到采购订单）");
        }

        String result = asString(body.get("result"));
        boolean passed = "pass".equalsIgnoreCase(result);

        // 查找最新绩效记录，存在则累计更新，不存在则新建
        Optional<SupplierPerformanceEntity> existing =
                supplierPerformanceRepository.findFirstBySupplierIdOrderByIdDesc(supplierId);
        SupplierPerformanceEntity performance;
        String action;
        if (existing.isPresent()) {
            performance = existing.get();
            action = "updated";
        } else {
            performance = new SupplierPerformanceEntity();
            performance.setSupplierId(supplierId);
            performance.setPeriod(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM")));
            performance.setEvaluator("QMS");
            performance.setTotalInspections(0);
            performance.setPassedInspections(0);
            action = "created";
        }

        // 累计检验批次与合格批次，重算合格率并同步质量得分
        int total = performance.getTotalInspections() == null ? 0 : performance.getTotalInspections();
        int pass = performance.getPassedInspections() == null ? 0 : performance.getPassedInspections();
        total += 1;
        if (passed) {
            pass += 1;
        }
        BigDecimal passRate = BigDecimal.valueOf(pass * 100.0 / total).setScale(2, RoundingMode.HALF_UP);
        performance.setTotalInspections(total);
        performance.setPassedInspections(pass);
        performance.setPassRate(passRate);
        performance.setQualityScore(passRate);
        performance.setEvaluateTime(LocalDateTime.now());
        supplierPerformanceRepository.save(performance);

        // 写入幂等镜像
        SrmQmsIqcMirrorEntity mirror = new SrmQmsIqcMirrorEntity();
        mirror.setInspectionNo(inspectionNo);
        mirror.setEventId(asString(body.get("eventId")));
        mirror.setSupplierId(supplierId);
        mirror.setResult(result);
        qmsIqcMirrorRepository.save(mirror);

        logger.info("QMS检验结果已回写供应商绩效: supplierId={}, supplierName={}, inspectionNo={}, result={}, action={}",
                supplierId, supplierName, inspectionNo, result, action);
        data.put("supplierId", supplierId);
        data.put("action", action);
        return Result.success("供应商质量绩效更新成功", data);
    }

    /**
     * 将请求体中的值安全转换为字符串
     *
     * @param value 原始值
     * @return 字符串形式，null 返回 null
     */
    private String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    /**
     * 将请求体中的值安全转换为 Long（兼容数字与字符串形式）
     *
     * @param value 原始值
     * @return Long 形式，无法转换时返回 null
     */
    private Long asLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
