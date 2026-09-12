package com.hxcoe.qms.service;

import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import com.hxcoe.qms.entity.AnomalyDisposalEntity;
import org.springframework.data.domain.Pageable;

/**
 * 质量异常处置服务接口
 */
public interface AnomalyDisposalService {

    /**
     * 分页查询异常处置
     *
     * @param reportNo 报告编号（模糊）
     * @param disposalStatus 处置状态
     * @param pageable 分页参数
     * @return 分页结果
     */
    Result<PageResult<AnomalyDisposalEntity>> page(String reportNo, String disposalStatus, Pageable pageable);

    /**
     * 获取异常处置详情
     *
     * @param id 处置ID
     * @return 处置详情
     */
    Result<AnomalyDisposalEntity> getById(Long id);

    /**
     * 创建异常处置
     *
     * @param entity 处置数据
     * @return 创建结果
     */
    Result<AnomalyDisposalEntity> create(AnomalyDisposalEntity entity);

    /**
     * 更新异常处置
     *
     * @param id 处置ID
     * @param entity 更新数据
     * @return 更新结果
     */
    Result<AnomalyDisposalEntity> update(Long id, AnomalyDisposalEntity entity);

    /**
     * 验证异常处置
     *
     * @param id 处置ID
     * @param verifier 验证人
     * @param verificationResult 验证结果
     * @return 处理结果
     */
    Result<Void> verify(Long id, String verifier, String verificationResult);
}
