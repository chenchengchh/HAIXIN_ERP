package com.hxcoe.qms.service;

import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import com.hxcoe.qms.entity.NcTrackingEntity;
import org.springframework.data.domain.Pageable;

/**
 * 不合格品追踪服务接口
 */
public interface NcTrackingService {

    /**
     * 分页查询不合格品追踪
     *
     * @param registrationNo 登记编号（模糊）
     * @param trackingStatus 追踪状态
     * @param pageable 分页参数
     * @return 分页结果
     */
    Result<PageResult<NcTrackingEntity>> page(String registrationNo, String trackingStatus, Pageable pageable);

    /**
     * 获取追踪详情
     *
     * @param id 追踪ID
     * @return 追踪详情
     */
    Result<NcTrackingEntity> getById(Long id);

    /**
     * 创建追踪
     *
     * @param entity 追踪数据
     * @return 创建结果
     */
    Result<NcTrackingEntity> create(NcTrackingEntity entity);

    /**
     * 更新追踪
     *
     * @param id 追踪ID
     * @param entity 更新数据
     * @return 更新结果
     */
    Result<NcTrackingEntity> update(Long id, NcTrackingEntity entity);

    /**
     * 开始追踪
     *
     * @param id 追踪ID
     * @return 处理结果
     */
    Result<Void> start(Long id);

    /**
     * 完成追踪
     *
     * @param id 追踪ID
     * @param effectiveness 效果评估
     * @param improvementSuggestions 改进建议
     * @return 处理结果
     */
    Result<Void> complete(Long id, String effectiveness, String improvementSuggestions);
}
