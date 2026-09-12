package com.hxcoe.mes.service.impl;

import com.hxcoe.common.dto.mes.WorkOrderCreateRequestDTO;
import com.hxcoe.mes.entity.WorkOrderEntity;
import com.hxcoe.mes.repository.WorkOrderRepository;
import com.hxcoe.mes.service.MesCompletionOutboxService;
import com.hxcoe.mes.service.WorkOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;

import com.hxcoe.common.api.ApiResponse;
import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import com.hxcoe.mes.client.BomClient;
import com.hxcoe.mes.client.dto.bom.BomLineDTO;
import com.hxcoe.mes.client.dto.bom.BomStructureDTO;
import com.hxcoe.mes.client.dto.bom.MaterialDTO;
import com.hxcoe.mes.entity.WorkOrderMaterialEntity;
import com.hxcoe.mes.repository.WorkOrderMaterialRepository;
import java.math.BigDecimal;
import com.hxcoe.mes.client.QmsClient;
import com.hxcoe.mes.client.dto.qms.QualityInspectionDTO;
import com.hxcoe.mes.client.dto.qms.InspectionResultDTO;
import com.hxcoe.mes.client.ErpClient;
import com.hxcoe.mes.client.WmsClient;
import com.hxcoe.mes.client.dto.wms.WmsOutboundOrderDTO;
import com.hxcoe.mes.client.dto.wms.WmsAsnDTO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class WorkOrderServiceImpl implements WorkOrderService {

    private static final Logger logger = LoggerFactory.getLogger(WorkOrderServiceImpl.class);

    @Autowired
    private WorkOrderRepository workOrderRepository;

    @Autowired
    private WorkOrderMaterialRepository materialRepository;

    @Autowired
    private WmsClient wmsClient;

    @Autowired
    private BomClient bomClient;

    @Autowired
    private QmsClient qmsClient;

    @Autowired
    private ErpClient erpClient;

    @Autowired
    private MesCompletionOutboxService mesCompletionOutboxService;

    @Override
    public WorkOrderEntity createWorkOrder(WorkOrderEntity workOrder) {
        // 幂等处理：按工单号查询，已存在则直接返回已有工单。
        // APS排程下达以WO-{detailId}为工单号，网络重试/重复下达时避免唯一约束冲突导致状态不一致
        if (workOrder.getWorkOrderNo() != null) {
            Optional<WorkOrderEntity> existing = workOrderRepository.findByWorkOrderNo(workOrder.getWorkOrderNo());
            if (existing.isPresent()) {
                return existing.get();
            }
        }
        if (workOrder.getCreateTime() == null) {
            workOrder.setCreateTime(LocalDateTime.now());
        }
        if (workOrder.getStatus() == null) {
            workOrder.setStatus("CREATED");
        }
        WorkOrderEntity savedOrder = workOrderRepository.save(workOrder);
        
        // 创建工单后，异步或同步获取BOM并展开物料需求
        fetchBomAndExplode(savedOrder);
        
        return savedOrder;
    }
    
    private void fetchBomAndExplode(WorkOrderEntity order) {
        try {
            // 1. 获取物料ID
            ApiResponse<PageResult<MaterialDTO>> matRes = bomClient.getMaterials(order.getProductCode(), null, 1, 1);
            if (matRes != null && matRes.getCode() != null && matRes.getCode() == 200 && matRes.getData() != null && matRes.getData().getTotal() > 0) {
                MaterialDTO material = matRes.getData().getRecords().get(0);
                
                // 2. 获取BOM
                ApiResponse<BomStructureDTO> bomRes = bomClient.getBomTree(material.getId());
                if (bomRes != null && bomRes.getCode() != null && bomRes.getCode() == 200 && bomRes.getData() != null && bomRes.getData().getLines() != null) {
                    List<WorkOrderMaterialEntity> materials = new ArrayList<>();
                    for (BomLineDTO line : bomRes.getData().getLines()) {
                        WorkOrderMaterialEntity wm = new WorkOrderMaterialEntity();
                        wm.setWorkOrderId(order.getId());
                        
                        String childCode = line.getChildMaterialCode() != null ? line.getChildMaterialCode() : "ID-" + line.getChildMaterialId();
                        String childName = line.getChildMaterialName() != null ? line.getChildMaterialName() : "未知物料";
                        
                        wm.setMaterialCode(childCode);
                        wm.setMaterialName(childName);
                        
                        // 需求 = 计划数 * 单耗
                        BigDecimal req = order.getPlanQuantity().multiply(line.getQuantity());
                        wm.setRequiredQuantity(req);
                        wm.setConsumedQuantity(BigDecimal.ZERO);
                        // wm.setUnit(line.getUnit()); // 假设有单位
                        
                        materials.add(wm);
                    }
                    materialRepository.saveAll(materials);
                    logger.info("工单 {} 物料分解完成，共 {} 项", order.getWorkOrderNo(), materials.size());
                }
            }
        } catch (Exception e) {
            logger.error("获取BOM失败: {}", e.getMessage());
        }
    }

    @Override
    public List<WorkOrderEntity> getAllWorkOrders() {
        return workOrderRepository.findAll();
    }

    @Override
    public WorkOrderEntity updateStatus(Long id, String status) {
        WorkOrderEntity order = workOrderRepository.findById(id).orElse(null);
        if (order != null) {
            String oldStatus = order.getStatus();
            order.setStatus(status);
            WorkOrderEntity savedOrder = workOrderRepository.save(order);
            
            // 状态流转触发WMS逻辑
            if (!oldStatus.equals(status)) {
                handleWmsIntegration(savedOrder, status);
            }
            
            return savedOrder;
        }
        return null;
    }

    @Override
    public WorkOrderEntity updateStatusByWorkOrderNo(String workOrderNo, String status) {
        WorkOrderEntity order = workOrderRepository.findByWorkOrderNo(workOrderNo).orElse(null);
        if (order != null) {
            return updateStatus(order.getId(), status);
        }
        return null;
    }

    @Override
    public Optional<WorkOrderEntity> getByWorkOrderNo(String workOrderNo) {
        return workOrderRepository.findByWorkOrderNo(workOrderNo);
    }

    private void handleWmsIntegration(WorkOrderEntity order, String status) {
        try {
            if ("STARTED".equals(status)) {
                // 开始生产 -> 创建领料单
                // 查询该工单的物料需求
                List<WorkOrderMaterialEntity> materials = materialRepository.findByWorkOrderId(order.getId());
                
                WmsOutboundOrderDTO outbound = new WmsOutboundOrderDTO();
                outbound.setOutboundNo("MAT-" + order.getWorkOrderNo());
                outbound.setSourceNo(order.getWorkOrderNo());
                outbound.setOrderType("MATERIAL"); // 领料类型
                outbound.setStatus("CREATED");
                outbound.setCustomerName(order.getResourceName()); // 领料部门/资源
                
                // 计算总数 (简化：相加)
                BigDecimal total = materials.stream()
                    .map(WorkOrderMaterialEntity::getRequiredQuantity)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                
                // 如果没有物料清单，使用计划数兜底 (兼容旧逻辑)
                if (total.compareTo(BigDecimal.ZERO) == 0) {
                     total = order.getPlanQuantity();
                }
                
                outbound.setTotalQty(total); 
                
                // 实际项目中应传递 OutboundOrderItemDTO 列表包含明细
                
                // WMS返回ApiResponse成功码为200，需校验避免静默失败
                Result<Object> wmsOutRes = wmsClient.createOutboundOrder(outbound);
                if (wmsOutRes != null && (wmsOutRes.getCode() == 0 || wmsOutRes.getCode() == 200)) {
                    logger.info("已触发WMS领料单创建: {}", outbound.getOutboundNo());
                } else {
                    logger.error("WMS领料单创建失败: {} msg={}", outbound.getOutboundNo(), wmsOutRes == null ? "响应为空" : wmsOutRes.getMessage());
                }

                // B4 修复：使用 erpProductionNo 业务键回写 ERP 状态（原误用 MES 工单主键 id）
                syncErpStatusByProductionNo(order, "STARTED", null);
                
            } else if ("COMPLETED".equals(status)) {
                // 1. 生产完成 -> 创建入库单 (ASN)
                WmsAsnDTO asn = new WmsAsnDTO();
                asn.setAsnNo("ASN-" + order.getWorkOrderNo());
                asn.setRefOrderNo(order.getWorkOrderNo());
                asn.setSupplierName("车间-" + order.getResourceName());
                asn.setStatus(0);
                asn.setExpectedArrivalTime(LocalDateTime.now());
                asn.setTotalQty(order.getActualQuantity() != null ? order.getActualQuantity() : order.getPlanQuantity());
                
                // WMS返回ApiResponse成功码为200，需校验避免静默失败
                Result<Object> wmsAsnRes = wmsClient.createAsn(asn);
                if (wmsAsnRes != null && (wmsAsnRes.getCode() == 0 || wmsAsnRes.getCode() == 200)) {
                    logger.info("已触发WMS入库单创建: {}", asn.getAsnNo());
                } else {
                    logger.error("WMS入库单创建失败: {} msg={}", asn.getAsnNo(), wmsAsnRes == null ? "响应为空" : wmsAsnRes.getMessage());
                }
                
                // 2. 生产完成 -> 创建质检单
                QualityInspectionDTO inspection = new QualityInspectionDTO();
                inspection.setProductCode(order.getProductCode());
                inspection.setProductName(order.getProductName());
                inspection.setSourceType("PRODUCTION");
                inspection.setSourceNo(order.getWorkOrderNo());
                inspection.setQuantity(order.getActualQuantity() != null ? order.getActualQuantity() : order.getPlanQuantity());
                inspection.setStatus("PENDING");
                
                Result<QualityInspectionDTO> qmsRes = qmsClient.createInspection(inspection);
                // QMS统一返回Result，成功码为0；兼容历史200
                if (qmsRes != null && (qmsRes.getCode() == 0 || qmsRes.getCode() == 200)) {
                    logger.info("已触发QMS质检单创建");
                } else {
                    logger.error("QMS创建失败: {}", qmsRes == null ? "响应为空" : qmsRes.getMessage());
                }
                
                // 同步ERP状态
                Double qty = order.getActualQuantity() != null ? order.getActualQuantity().doubleValue() : order.getPlanQuantity().doubleValue();
                // B4 修复：使用 erpProductionNo 业务键回写 ERP 状态（原误用 MES 工单主键 id）
                syncErpStatusByProductionNo(order, "COMPLETED", qty);
                logger.info("已同步ERP生产状态: COMPLETED");

                // B6 修复：工单完工后通过 Outbox 异步推送 SCM 完工事实，闭环生产链
                mesCompletionOutboxService.enqueueWorkOrderCompleted(order);
            }
        } catch (Exception e) {
            logger.error("外部服务集成失败: {}", e.getMessage());
        }
    }

    /**
     * 通过 erpProductionNo 业务键回写 ERP 生产单状态（B4 修复）。
     *
     * <p>仅当工单关联了 ERP 生产单号时才回写；未关联（如 MES 独立创建的工单）则跳过并记日志。
     * 回写失败不阻断主流程（已包裹在外层 try-catch 中）。
     *
     * @param order           MES 工单
     * @param status          目标状态（STARTED/COMPLETED）
     * @param actualQuantity  实际完工数量（开工时为 null）
     */
    private void syncErpStatusByProductionNo(WorkOrderEntity order, String status, Double actualQuantity) {
        String productionNo = order.getErpProductionNo();
        if (productionNo == null || productionNo.isBlank()) {
            logger.warn("工单 {} 未关联 ERP 生产单号，跳过 ERP 状态回写 status={}", order.getWorkOrderNo(), status);
            return;
        }
        try {
            erpClient.updateProductionStatusByNo(productionNo, status, actualQuantity);
            logger.info("ERP 生产单状态回写成功 productionNo={} status={} qty={}", productionNo, status, actualQuantity);
        } catch (Exception ex) {
            logger.error("ERP 生产单状态回写失败 productionNo={} status={} error={}", productionNo, status, ex.getMessage());
        }
    }

    @Override
    public void handleInspectionResult(InspectionResultDTO resultDTO) {
        // 根据 sourceNo (工单号) 查找工单
        WorkOrderEntity order = workOrderRepository.findByWorkOrderNo(resultDTO.getSourceNo()).orElse(null);

        if (order != null) {
            order.setActualQuantity(resultDTO.getQualifiedQuantity());
            // 也可以更新不合格数等字段
            workOrderRepository.save(order);
            logger.info("工单 {} 质检结果已更新: 合格 {}", order.getWorkOrderNo(), order.getActualQuantity());
        } else {
            logger.warn("未找到关联工单: {}", resultDTO.getSourceNo());
        }
    }

    /**
     * 基于 ERP 生产单创建事件创建 MES 工单（B1 消费端入口）。
     *
     * <p>幂等策略：按 erpProductionNo 查询，若已存在则直接返回已有工单，不重复创建。
     * 工单号优先使用事件载荷中的 workOrderNo，为空时按 WO-{erpProductionNo} 生成。
     *
     * @param req ERP 推送的工单创建请求 DTO
     * @return 已创建或已存在的工单实体
     */
    @Override
    @Transactional
    public WorkOrderEntity createWorkOrderFromErp(WorkOrderCreateRequestDTO req) {
        if (req == null || req.getWorkOrder() == null) {
            throw new IllegalArgumentException("工单创建请求或载荷为空");
        }
        WorkOrderCreateRequestDTO.WorkOrderPayload payload = req.getWorkOrder();
        String erpProductionNo = payload.getErpProductionNo();
        if (erpProductionNo == null || erpProductionNo.isBlank()) {
            throw new IllegalArgumentException("erpProductionNo 不能为空");
        }

        // 幂等：同一 ERP 生产单号只创建一次
        Optional<WorkOrderEntity> existing = workOrderRepository.findByErpProductionNo(erpProductionNo);
        if (existing.isPresent()) {
            logger.info("ERP->MES 工单创建事件命中幂等，返回已有工单 erpProductionNo={} workOrderNo={}",
                    erpProductionNo, existing.get().getWorkOrderNo());
            return existing.get();
        }

        // 构造工单实体
        WorkOrderEntity order = new WorkOrderEntity();
        String workOrderNo = (payload.getWorkOrderNo() == null || payload.getWorkOrderNo().isBlank())
                ? "WO-" + erpProductionNo : payload.getWorkOrderNo();
        order.setWorkOrderNo(workOrderNo);
        order.setErpProductionNo(erpProductionNo);
        order.setProductCode(payload.getProductCode());
        order.setProductName(payload.getProductName());
        order.setPlanQuantity(payload.getPlanQuantity());
        // 车间/生产线映射到 resourceName（MES 工单用 resourceName 表示生产资源）
        String resource = payload.getProductionLine();
        if (resource == null || resource.isBlank()) {
            resource = payload.getWorkshop();
        }
        order.setResourceName(resource);
        order.setStartTime(payload.getPlanStartTime());
        order.setEndTime(payload.getPlanEndTime());
        order.setStatus("CREATED");
        order.setCreateTime(LocalDateTime.now());

        WorkOrderEntity saved = workOrderRepository.save(order);
        logger.info("ERP->MES 工单创建成功 erpProductionNo={} workOrderNo={} eventId={}",
                erpProductionNo, workOrderNo, req.getEventId());

        // 创建工单后异步展开 BOM 物料需求（复用既有逻辑，失败不影响工单创建）
        fetchBomAndExplode(saved);

        return saved;
    }
}
