package com.hxcoe.mes.service;

import com.hxcoe.common.dto.mes.MesReportingCreateDTO;
import com.hxcoe.common.dto.mes.MesReportingUpdateDTO;
import com.hxcoe.common.result.PageResult;
import com.hxcoe.mes.entity.ProductionReportEntity;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 生产报工服务接口
 */
public interface ProductionReportService {

    /**
     * 创建生产报工
     *
     * @param productionReportEntity 生产报工实体
     * @return 创建结果
     */
    ProductionReportEntity createProductionReport(ProductionReportEntity productionReportEntity);

    /**
     * 根据ID查询生产报工
     *
     * @param id 主键ID
     * @return 查询结果
     */
    ProductionReportEntity getProductionReportById(Long id);

    /**
     * 根据报工单号查询生产报工
     *
     * @param reportNo 报工单号
     * @return 查询结果
     */
    ProductionReportEntity getProductionReportByNo(String reportNo);

    /**
     * 更新生产报工
     *
     * @param productionReportEntity 生产报工实体
     * @return 更新结果
     */
    ProductionReportEntity updateProductionReport(ProductionReportEntity productionReportEntity);

    /**
     * 删除生产报工
     *
     * @param id 主键ID
     * @return 删除结果
     */
    boolean deleteProductionReport(Long id);

    /**
     * 分页查询生产报工
     *
     * @param page 当前页码
     * @param size 每页条数
     * @param reportNo 报工单号
     * @param workOrderNo 工单号
     * @param operatorName 操作员姓名
     * @param status 报工状态
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 分页结果
     */
    PageResult<ProductionReportEntity> getProductionReportList(
            Integer page,
            Integer size,
            String reportNo,
            String workOrderNo,
            String operatorName,
            String status,
            LocalDateTime startDate,
            LocalDateTime endDate);

    /**
     * 根据状态查询生产报工
     *
     * @param status 报工状态
     * @return 生产报工列表
     */
    List<ProductionReportEntity> getProductionReportByStatus(String status);

    /**
     * 更新生产报工状态
     *
     * @param id 报工ID
     * @param status 新状态
     * @return 更新结果
     */
    ProductionReportEntity updateProductionReportStatus(Long id, String status);

    /**
     * 验证生产报工
     *
     * @param id 报工ID
     * @return 操作结果
     */
    boolean verifyProductionReport(Long id);

    /**
     * 批准生产报工
     *
     * @param id 报工ID
     * @return 操作结果
     */
    boolean approveProductionReport(Long id);

    /**
     * 手动创建报工（B2 路径对齐：POST /api/v1/mes/reporting/manual）。
     *
     * <p>对应 ERP 侧 {@code MesReportingClient#manual} 契约，接收强类型 DTO。
     * reportNo 为空时由 MES 自动生成。
     *
     * @param dto 报工创建 DTO
     * @return 创建后的报工实体
     */
    ProductionReportEntity createManualReport(MesReportingCreateDTO dto);

    /**
     * 按 ID 更新报工（B2 路径对齐：PUT /api/v1/mes/reporting/{id}）。
     *
     * <p>对应 ERP 侧 {@code MesReportingClient#update} 契约，接收强类型 DTO。
     * 仅更新 DTO 中非空字段（增量更新语义）。
     *
     * @param id  报工ID
     * @param dto 报工更新 DTO
     * @return 更新后的报工实体，不存在返回 null
     */
    ProductionReportEntity updateReportById(Long id, MesReportingUpdateDTO dto);
}
