package com.hxcoe.qms.service;

import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import com.hxcoe.qms.entity.QualityReportEntity;
import org.springframework.data.domain.Pageable;

import java.util.Map;

/**
 * 质量报告服务接口
 */
public interface QualityReportService {

    /**
     * 分页查询质量报告
     *
     * @param reportNo 报告编号（模糊）
     * @param status 状态
     * @param pageable 分页参数
     * @return 分页结果
     */
    Result<PageResult<QualityReportEntity>> page(String reportNo, String status, Pageable pageable);

    /**
     * 获取质量报告详情
     *
     * @param id 报告ID
     * @return 报告详情
     */
    Result<QualityReportEntity> getById(Long id);

    /**
     * 创建质量报告
     *
     * @param entity 报告数据
     * @return 创建结果
     */
    Result<QualityReportEntity> create(QualityReportEntity entity);

    /**
     * 更新质量报告
     *
     * @param id 报告ID
     * @param entity 更新数据
     * @return 更新结果
     */
    Result<QualityReportEntity> update(Long id, QualityReportEntity entity);

    /**
     * 删除质量报告
     *
     * @param id 报告ID
     * @return 删除结果
     */
    Result<Void> delete(Long id);

    /**
     * 生成质量报告
     *
     * @param params 生成参数
     * @return 生成结果
     */
    Result<QualityReportEntity> generate(Map<String, Object> params);
}
