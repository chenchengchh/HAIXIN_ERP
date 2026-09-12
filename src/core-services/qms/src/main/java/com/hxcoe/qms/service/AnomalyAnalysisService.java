package com.hxcoe.qms.service;

import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import com.hxcoe.qms.entity.AnomalyAnalysisEntity;
import org.springframework.data.domain.Pageable;

/**
 * 质量异常分析服务接口
 */
public interface AnomalyAnalysisService {

    /**
     * 分页查询异常分析
     *
     * @param reportNo 报告编号（模糊）
     * @param analysisStatus 分析状态
     * @param pageable 分页参数
     * @return 分页结果
     */
    Result<PageResult<AnomalyAnalysisEntity>> page(String reportNo, String analysisStatus, Pageable pageable);

    /**
     * 获取异常分析详情
     *
     * @param id 分析ID
     * @return 分析详情
     */
    Result<AnomalyAnalysisEntity> getById(Long id);

    /**
     * 创建异常分析
     *
     * @param entity 分析数据
     * @return 创建结果
     */
    Result<AnomalyAnalysisEntity> create(AnomalyAnalysisEntity entity);

    /**
     * 更新异常分析
     *
     * @param id 分析ID
     * @param entity 更新数据
     * @return 更新结果
     */
    Result<AnomalyAnalysisEntity> update(Long id, AnomalyAnalysisEntity entity);

    /**
     * 完成异常分析
     *
     * @param id 分析ID
     * @return 处理结果
     */
    Result<Void> complete(Long id);
}
