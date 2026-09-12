package com.hxcoe.qms.service;

import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import com.hxcoe.qms.entity.InspectionPlanEntity;
import org.springframework.data.domain.Pageable;

/**
 * 检验计划服务接口
 */
public interface InspectionPlanService {

    /**
     * 分页查询检验计划
     *
     * @param planNo 计划编号（模糊）
     * @param planName 计划名称（模糊）
     * @param materialName 物料名称（模糊）
     * @param planType 计划类型
     * @param status 状态
     * @param pageable 分页参数
     * @return 分页结果
     */
    Result<PageResult<InspectionPlanEntity>> page(String planNo, String planName, String materialName, String planType, String status, Pageable pageable);

    /**
     * 获取检验计划详情
     *
     * @param id 计划ID
     * @return 计划详情
     */
    Result<InspectionPlanEntity> getById(Long id);

    /**
     * 创建检验计划
     *
     * @param entity 计划数据
     * @return 创建结果
     */
    Result<InspectionPlanEntity> create(InspectionPlanEntity entity);

    /**
     * 更新检验计划
     *
     * @param id 计划ID
     * @param entity 更新数据
     * @return 更新结果
     */
    Result<InspectionPlanEntity> update(Long id, InspectionPlanEntity entity);

    /**
     * 删除检验计划
     *
     * @param id 计划ID
     * @return 删除结果
     */
    Result<Void> delete(Long id);

    /**
     * 激活检验计划
     *
     * @param id 计划ID
     * @return 处理结果
     */
    Result<Void> activate(Long id);

    /**
     * 失效检验计划
     *
     * @param id 计划ID
     * @return 处理结果
     */
    Result<Void> invalidate(Long id);
}
