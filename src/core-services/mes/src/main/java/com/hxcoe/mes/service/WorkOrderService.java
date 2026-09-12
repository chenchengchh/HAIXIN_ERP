package com.hxcoe.mes.service;

import com.hxcoe.common.dto.mes.WorkOrderCreateRequestDTO;
import com.hxcoe.mes.entity.WorkOrderEntity;
import java.util.List;
import java.util.Optional;

import com.hxcoe.mes.client.dto.qms.InspectionResultDTO;

public interface WorkOrderService {
    WorkOrderEntity createWorkOrder(WorkOrderEntity workOrder);
    List<WorkOrderEntity> getAllWorkOrders();
    WorkOrderEntity updateStatus(Long id, String status);
    WorkOrderEntity updateStatusByWorkOrderNo(String workOrderNo, String status);
    Optional<WorkOrderEntity> getByWorkOrderNo(String workOrderNo);
    void handleInspectionResult(InspectionResultDTO resultDTO);

    /**
     * 基于 ERP 生产单创建事件创建 MES 工单（B1 消费端入口）。
     *
     * <p>由 HTTP Controller / Rabbit Listener / Redis Stream Job 三态入口调用。
     * 按 erpProductionNo 幂等：同一生产单重复事件不会重复创建工单。
     *
     * @param req ERP 推送的工单创建请求 DTO
     * @return 已创建或已存在的工单实体
     */
    WorkOrderEntity createWorkOrderFromErp(WorkOrderCreateRequestDTO req);
}
