package com.hxcoe.mes.client;

import com.hxcoe.common.result.Result;
import com.hxcoe.mes.client.dto.qms.QualityInspectionDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "qms-service")
public interface QmsClient {
    /**
     * 在QMS中创建质量检验单（路径与QMS QualityInspectionController一致）
     *
     * @param inspection 检验单数据
     * @return 创建结果
     */
    @PostMapping("/api/v1/qms/inspections")
    Result<QualityInspectionDTO> createInspection(@RequestBody QualityInspectionDTO inspection);
}
