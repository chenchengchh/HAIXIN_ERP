package com.hxcoe.mes.client;

import com.hxcoe.common.dto.scm.MesCompletionFactDTO;
import com.hxcoe.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * MES 调用 SCM 集成接口的 Feign 客户端（HTTP 模式推送端，B6 闭环）。
 *
 * <p>当 hxcoe.integration.transport=HTTP 时，由 {@code MesCompletionOutboxService} 调用，
 * 将 MES 工单完工事实推送到 SCM。
 */
@FeignClient(name = "scm-service", path = "/api/v1/scm/integration", contextId = "mesScmIntegrationClient")
public interface ScmIntegrationClient {

    /**
     * 推送 MES 工单完工事实到 SCM。
     *
     * @param req 完工事实 DTO
     * @return 处理结果
     */
    @PostMapping("/mes/completion")
    Result<Map<String, Object>> receiveMesCompletion(@RequestBody MesCompletionFactDTO req);
}
