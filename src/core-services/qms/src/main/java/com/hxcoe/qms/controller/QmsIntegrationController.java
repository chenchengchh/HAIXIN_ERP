package com.hxcoe.qms.controller;

import com.hxcoe.common.result.Result;
import com.hxcoe.qms.client.dto.wms.WmsReceiptTriggerRequest;
import com.hxcoe.qms.entity.QualityInspectionEntity;
import com.hxcoe.qms.service.WmsReceiptTriggerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * QMS 集成入口控制器。
 * <p>接收上游服务（WMS）的质量相关事件，补齐质量闭环入口段。</p>
 */
@RestController
@RequestMapping("/api/v1/qms/integration")
public class QmsIntegrationController {

    @Autowired
    private WmsReceiptTriggerService wmsReceiptTriggerService;

    /**
     * 接收 WMS 收货完成事件，触发 IQC 来料检验单创建。
     * <p>幂等：同一收货单+物料的质检单只创建一次。</p>
     *
     * @param req WMS 收货触发请求（poNo、receiptNo、lines）
     * @return 创建结果（创建数量、检验单号列表）
     */
    @PostMapping("/wms/receipt-triggered")
    public Result<Map<String, Object>> receiptTriggered(@RequestBody WmsReceiptTriggerRequest req) {
        if (req == null || req.getPoNo() == null || req.getPoNo().isBlank()) {
            return Result.fail("poNo不能为空");
        }
        if (req.getReceiptNo() == null || req.getReceiptNo().isBlank()) {
            return Result.fail("receiptNo不能为空");
        }
        if (req.getLines() == null || req.getLines().isEmpty()) {
            return Result.fail("收货明细lines不能为空");
        }

        List<QualityInspectionEntity> created = wmsReceiptTriggerService.applyReceiptTrigger(req);

        Map<String, Object> data = new HashMap<>();
        data.put("poNo", req.getPoNo());
        data.put("receiptNo", req.getReceiptNo());
        data.put("createdCount", created.size());
        data.put("inspectionCodes", created.stream()
                .map(QualityInspectionEntity::getInspectionCode)
                .collect(Collectors.toList()));
        return Result.success("IQC质检单创建成功", data);
    }
}
