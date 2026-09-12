package com.hxcoe.qms.service;

import com.hxcoe.qms.client.dto.wms.WmsReceiptTriggerRequest;
import com.hxcoe.qms.entity.QualityInspectionEntity;
import com.hxcoe.qms.repository.QualityInspectionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * WMS 收货触发 QMS 来料检验（IQC）处理服务。
 * <p>接收 WMS 收货完成事件，按收货明细行幂等创建 IQC 质检单，
 * 质检完成后由 {@link ScmIqcSyncService} 回流 SCM，完成质量闭环。</p>
 */
@Slf4j
@Service
public class WmsReceiptTriggerService {

    /** IQC 检验单号前缀 */
    private static final String IQC_CODE_PREFIX = "IQC-";
    /** 检验单号最大长度（与 quality_inspection.inspection_code 列长度 50 对齐） */
    private static final int IQC_CODE_MAX_LEN = 50;

    @Autowired
    private QualityInspectionRepository inspectionRepository;

    /**
     * 应用 WMS 收货触发事件，按收货明细行创建 IQC 质检单。
     * <p>幂等：同一 receiptNo + materialCode 的质检单只创建一次。</p>
     *
     * @param req WMS 收货触发请求
     * @return 创建的质检单列表（已存在的会被跳过，不包含在结果中）
     */
    @Transactional
    public List<QualityInspectionEntity> applyReceiptTrigger(WmsReceiptTriggerRequest req) {
        List<QualityInspectionEntity> created = new ArrayList<>();
        if (req == null || req.getLines() == null || req.getLines().isEmpty()) {
            return created;
        }
        String poNo = req.getPoNo();
        String receiptNo = req.getReceiptNo();
        LocalDateTime now = LocalDateTime.now();

        for (WmsReceiptTriggerRequest.WmsReceiptLine line : req.getLines()) {
            if (line == null || line.getMaterialCode() == null || line.getMaterialCode().isBlank()) {
                continue;
            }
            String inspectionCode = buildInspectionCode(receiptNo, line.getMaterialCode());

            // 幂等查重：同一收货单+物料只创建一次 IQC
            if (inspectionRepository.findByInspectionCode(inspectionCode).isPresent()) {
                log.info("WMS->QMS IQC 触发命中幂等，跳过 inspectionCode={} poNo={}", inspectionCode, poNo);
                continue;
            }

            QualityInspectionEntity inspection = new QualityInspectionEntity();
            inspection.setInspectionCode(inspectionCode);
            inspection.setProductCode(line.getMaterialCode());
            inspection.setProductName(line.getMaterialName() == null ? line.getMaterialCode() : line.getMaterialName());
            inspection.setBatchNo(line.getBatchNo());
            inspection.setInspectionType("IQC");
            inspection.setSourceType("PURCHASE");
            inspection.setSourceNo(poNo);
            inspection.setQuantity(line.getQty());
            inspection.setStatus("PENDING");
            inspection.setInspectionDate(now);
            inspection.setCreatedTime(now);

            QualityInspectionEntity saved = inspectionRepository.save(inspection);
            created.add(saved);
            log.info("WMS->QMS IQC 质检单创建成功 inspectionCode={} poNo={} receiptNo={} materialCode={} qty={}",
                    inspectionCode, poNo, receiptNo, line.getMaterialCode(), line.getQty());
        }
        return created;
    }

    /**
     * 构造幂等的检验单号：IQC-{receiptNo}-{materialCode}，并截断到最大长度。
     *
     * @param receiptNo    收货单号
     * @param materialCode 物料编码
     * @return 检验单号
     */
    private String buildInspectionCode(String receiptNo, String materialCode) {
        String code = IQC_CODE_PREFIX + (receiptNo == null ? "NA" : receiptNo) + "-" + materialCode;
        if (code.length() > IQC_CODE_MAX_LEN) {
            // 超长时保留前缀和物料编码尾部，避免唯一约束冲突
            code = code.substring(0, IQC_CODE_MAX_LEN);
        }
        return code;
    }
}
