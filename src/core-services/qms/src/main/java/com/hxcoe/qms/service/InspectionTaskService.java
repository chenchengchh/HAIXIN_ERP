package com.hxcoe.qms.service;

import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import com.hxcoe.qms.entity.InspectionTaskEntity;
import org.springframework.data.domain.Pageable;

import java.util.Map;

/**
 * 检验任务服务接口
 */
public interface InspectionTaskService {

    /**
     * 分页查询检验任务
     *
     * @param taskNo 任务编号（模糊）
     * @param status 状态
     * @param planId 计划ID
     * @param materialCode 物料编码
     * @param pageable 分页参数
     * @return 分页结果
     */
    Result<PageResult<InspectionTaskEntity>> page(String taskNo, String status, Long planId, String materialCode, Pageable pageable);

    /**
     * 获取检验任务详情
     *
     * @param id 任务ID
     * @return 任务详情
     */
    Result<InspectionTaskEntity> getById(Long id);

    /**
     * 创建检验任务
     *
     * @param entity 任务数据
     * @return 创建结果
     */
    Result<InspectionTaskEntity> create(InspectionTaskEntity entity);

    /**
     * 更新检验任务
     *
     * @param id 任务ID
     * @param entity 更新数据
     * @return 更新结果
     */
    Result<InspectionTaskEntity> update(Long id, InspectionTaskEntity entity);

    /**
     * 删除检验任务
     *
     * @param id 任务ID
     * @return 删除结果
     */
    Result<Void> delete(Long id);

    /**
     * 分配检验任务
     *
     * @param id 任务ID
     * @param assignee 分配人
     * @return 分配结果
     */
    Result<Void> assign(Long id, String assignee);

    /**
     * 取消检验任务
     *
     * @param id 任务ID
     * @return 取消结果
     */
    Result<Void> cancel(Long id);

    /**
     * 自动分配检验任务（按过滤条件批量创建/分配）
     *
     * @param params 参数
     * @return 处理结果
     */
    Result<Map<String, Object>> autoAssign(Map<String, Object> params);
}
