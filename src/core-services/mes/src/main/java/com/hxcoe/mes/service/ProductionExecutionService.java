package com.hxcoe.mes.service;

import com.hxcoe.mes.entity.ProductionExecutionEntity;
import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 生产执行服务接口
 */
public interface ProductionExecutionService {

    /**
     * 创建生产执行记录
     *
     * @param productionExecutionEntity 生产执行实体
     * @return 保存后的生产执行实体
     */
    ProductionExecutionEntity createProductionExecution(ProductionExecutionEntity productionExecutionEntity);

    /**
     * 根据ID查询生产执行记录
     *
     * @param id 主键ID
     * @return 生产执行实体
     */
    ProductionExecutionEntity getProductionExecutionById(Long id);

    /**
     * 根据生产执行单号查询生产执行记录
     *
     * @param executionNo 生产执行单号
     * @return 生产执行实体
     */
    ProductionExecutionEntity getProductionExecutionByNo(String executionNo);

    /**
     * 更新生产执行记录
     *
     * @param productionExecutionEntity 生产执行实体
     * @return 更新后的生产执行实体
     */
    ProductionExecutionEntity updateProductionExecution(ProductionExecutionEntity productionExecutionEntity);

    /**
     * 删除生产执行记录
     *
     * @param id 主键ID
     * @return 删除结果
     */
    boolean deleteProductionExecution(Long id);

    /**
     * 分页查询生产执行记录
     *
     * @param page 当前页码
     * @param size 每页条数
     * @param executionNo 生产执行单号
     * @param productionOrderNo 生产订单号
     * @param productCode 产品编码
     * @param productionLine 生产线
     * @param executionStatus 生产状态
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 分页结果
     */
    PageResult<ProductionExecutionEntity> getProductionExecutionList(Integer page, Integer size, String executionNo, String productionOrderNo, String productCode, String productionLine, Integer executionStatus, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 根据生产状态查询生产执行记录
     *
     * @param executionStatus 生产状态
     * @return 生产执行实体列表
     */
    List<ProductionExecutionEntity> getProductionExecutionByStatus(Integer executionStatus);

    /**
     * 开始生产执行
     *
     * @param id 生产执行ID
     * @return 操作结果
     */
    boolean startProductionExecution(Long id);

    /**
     * 暂停生产执行
     *
     * @param id 生产执行ID
     * @return 操作结果
     */
    boolean pauseProductionExecution(Long id);

    /**
     * 恢复生产执行
     *
     * @param id 生产执行ID
     * @return 操作结果
     */
    boolean resumeProductionExecution(Long id);

    /**
     * 完成生产执行
     *
     * @param id 生产执行ID
     * @return 操作结果
     */
    boolean completeProductionExecution(Long id);

    /**
     * 取消生产执行
     *
     * @param id 生产执行ID
     * @return 操作结果
     */
    boolean cancelProductionExecution(Long id);

    /**
     * 更新生产数量
     *
     * @param id 生产执行ID
     * @param actualQuantity 实际数量
     * @param qualifiedQuantity 合格数量
     * @param unqualifiedQuantity 不合格数量
     * @return 更新结果
     */
    boolean updateProductionQuantity(Long id, Integer actualQuantity, Integer qualifiedQuantity, Integer unqualifiedQuantity);
}
