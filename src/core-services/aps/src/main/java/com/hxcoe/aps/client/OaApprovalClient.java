package com.hxcoe.aps.client;

import com.hxcoe.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * OA统一审批服务Feign客户端。
 * <p>APS生产计划通过此客户端向OA提交审批申请，
 * 审批完成后OA通过回调URL通知APS更新计划状态，实现审批闭环。</p>
 */
@FeignClient(name = "oa-service", path = "/api/v1/oa/unified-approval", contextId = "apsOaApprovalClient")
public interface OaApprovalClient {

    /**
     * 提交审批申请（统一入口）。
     *
     * @param request 审批申请请求（sourceSystem/businessType/businessId/callbackUrl等）
     * @return 审批实例
     */
    @PostMapping("/submit")
    Result<Object> submit(@RequestBody Map<String, Object> request);
}
