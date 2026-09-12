package com.hxcoe.erp.service;

import com.hxcoe.erp.entity.ProductionEntity;
import com.hxcoe.erp.dto.production.CapacityDataDto;
import com.hxcoe.erp.dto.production.CapacityPlanningResultDto;
import com.hxcoe.common.result.PageResult;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 生产服务接口
 */
public interface ProductionService {
    /**
     * 创建生产记录
     *
     * @param productionEntity 生产实体
     * @return 创建结果
     */
    ProductionEntity createProduction(ProductionEntity productionEntity);

    /**
     * 根据ID查询生产记录
     *
     * @param id 主键ID
     * @return 查询结果
     */
    ProductionEntity getProductionById(Long id);

    /**
     * 根据生产单号查询生产记录
     *
     * @param productionNo 生产单号
     * @return 查询结果
     */
    ProductionEntity getProductionByNo(String productionNo);

    /**
     * 更新生产记录
     *
     * @param productionEntity 生产实体
     * @return 更新结果
     */
    ProductionEntity updateProduction(ProductionEntity productionEntity);

    /**
     * 删除生产记录
     *
     * @param id 主键ID
     * @return 删除结果
     */
    boolean deleteProduction(Long id);

    /**
     * 分页查询生产记录
     *
     * @param page 页码
     * @param size 每页条数
     * @param productionNo 生产单号
     * @param productCode 产品编码
     * @param workshop 车间
     * @param productionStatus 生产状态
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 分页结果
     */
    PageResult<ProductionEntity> getProductionList(Integer page, Integer size, String productionNo, String productCode, String productName, String workshop, String productionLine, Integer productionStatus, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 根据生产状态查询生产记录
     *
     * @param productionStatus 生产状态
     * @return 生产记录列表
     */
    List<ProductionEntity> getProductionByStatus(Integer productionStatus);

    /**
     * 根据产品编码查询生产记录
     *
     * @param productCode 产品编码
     * @return 生产记录列表
     */
    List<ProductionEntity> getProductionByProductCode(String productCode);

    /**
     * 开始生产
     *
     * @param id 生产记录ID
     * @return 操作结果
     */
    boolean startProduction(Long id);

    /**
     * 暂停生产
     *
     * @param id 生产记录ID
     * @return 操作结果
     */
    boolean pauseProduction(Long id);

    /**
     * 完成生产
     *
     * @param id 生产记录ID
     * @return 操作结果
     */
    boolean completeProduction(Long id);

    /**
     * 取消生产
     *
     * @param id 生产记录ID
     * @return 操作结果
     */
    boolean cancelProduction(Long id);

    /**
     * 恢复生产
     *
     * @param id 生产记录ID
     * @return 操作结果
     */
    boolean resumeProduction(Long id);

    /**
     * 获取产能数据
     *
     * @param page 页码
     * @param size 每页条数
     * @param departmentName 部门名称
     * @param workCenterName 工作中心名称
     * @param resourceName 资源名称
     * @param period 时间范围
     * @param startDate 开始日期（yyyy-MM-dd 或 yyyy-MM-dd HH:mm:ss）
     * @param endDate 结束日期（yyyy-MM-dd 或 yyyy-MM-dd HH:mm:ss）
     * @return 产能数据分页
     */
    PageResult<CapacityDataDto> getCapacity(Integer page, Integer size, String departmentName, String workCenterName, String resourceName, String period, String startDate, String endDate);

    /**
     * 计算并缓存最近一次产能规划结果
     *
     * @param params 产能规划参数
     * @return 产能规划结果分页
     */
    PageResult<CapacityPlanningResultDto> runCapacityPlanning(Map<String, Object> params);

    /**
     * 获取最近一次产能规划结果（若无则返回空）
     *
     * @param page 页码
     * @param size 每页条数
     * @return 产能规划结果分页
     */
    PageResult<CapacityPlanningResultDto> getCapacityPlanningResults(Integer page, Integer size);

    /**
     * 更新生产状态 (外部集成用)
     */
    void updateStatus(Long id, String status, Double actualQuantity);

    /**
     * 按生产单号更新生产状态（B4 修复：MES 回写 ERP 使用业务键而非主键）。
     *
     * <p>MES 工单完工/开工时，仅持有 ERP 生产单号（业务键），无法获取 ERP 主键 id。
     * 此方法按 productionNo 查找生产单后回写状态与实际数量。
     *
     * @param productionNo    生产单号（业务键）
     * @param status          外部状态（STARTED/COMPLETED）
     * @param actualQuantity  实际完工数量（完工时回填，可空）
     * @return 是否找到并更新成功
     */
    boolean updateStatusByNo(String productionNo, String status, Double actualQuantity);
}
