package com.hxcoe.oa.controller;

import lombok.Data;

import java.util.Map;

/**
 * 统一审批申请请求DTO。
 * <p>各业务模块通过此DTO提交审批申请，OA根据businessType自动匹配审批流程模板。</p>
 */
@Data
public class UnifiedApprovalRequest {

    /**
     * 来源系统（crm/scm/erp/wms/mes/les/qms/srm/eam/hr等）
     */
    private String sourceSystem;

    /**
     * 业务类型（如：sales_order/purchase_order/production_order/warehouse_outbound等）
     */
    private String businessType;

    /**
     * 业务ID（各模块的业务单据ID）
     */
    private String businessId;

    /**
     * 业务单号（各模块的业务单据编号，用于展示）
     */
    private String businessNo;

    /**
     * 审批标题
     */
    private String title;

    /**
     * 审批描述
     */
    private String description;

    /**
     * 发起人ID
     */
    private Long initiatorId;

    /**
     * 发起人名称
     */
    private String initiatorName;

    /**
     * 表单数据（JSON格式，包含业务单据的详细信息）
     */
    private Map<String, Object> formData;

    /**
     * 回调URL（审批完成后OA回调此URL通知发起方模块）
     * 格式：http://{service-name}/api/v1/{module}/approval/callback
     */
    private String callbackUrl;

    /**
     * 关联ERP订单ID（可选）
     */
    private Long erpOrderId;

    /**
     * 关联SCM供应商ID（可选）
     */
    private Long scmSupplierId;

    /**
     * 关联MES车间ID（可选）
     */
    private Long mesWorkshopId;
}
