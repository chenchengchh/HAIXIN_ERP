package com.hxcoe.qms.service;

import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import com.hxcoe.qms.entity.AnomalyReportEntity;
import org.springframework.data.domain.Pageable;

/**
 * 质量异常报告服务接口
 */
public interface AnomalyReportService {

    /**
     * 分页查询异常报告
     *
     * @param reportNo 报告编号（模糊）
     * @param status 状态
     * @param pageable 分页参数
     * @return 分页结果
     */
    Result<PageResult<AnomalyReportEntity>> page(String reportNo, String status, Pageable pageable);

    /**
     * 获取异常报告详情
     *
     * @param id 报告ID
     * @return 报告详情
     */
    Result<AnomalyReportEntity> getById(Long id);

    /**
     * 创建异常报告
     *
     * @param entity 报告数据
     * @return 创建结果
     */
    Result<AnomalyReportEntity> create(AnomalyReportEntity entity);

    /**
     * 更新异常报告
     *
     * @param id 报告ID
     * @param entity 更新数据
     * @return 更新结果
     */
    Result<AnomalyReportEntity> update(Long id, AnomalyReportEntity entity);

    /**
     * 删除异常报告
     *
     * @param id 报告ID
     * @return 删除结果
     */
    Result<Void> delete(Long id);

    /**
     * 开始调查
     *
     * @param id 报告ID
     * @return 处理结果
     */
    Result<Void> startInvestigation(Long id);

    /**
     * 解决异常
     *
     * @param id 报告ID
     * @return 处理结果
     */
    Result<Void> resolve(Long id);

    /**
     * 关闭异常
     *
     * @param id 报告ID
     * @return 处理结果
     */
    Result<Void> close(Long id);
}
