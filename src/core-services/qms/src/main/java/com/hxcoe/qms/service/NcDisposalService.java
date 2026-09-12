package com.hxcoe.qms.service;

import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import com.hxcoe.qms.entity.NcDisposalEntity;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 不合格品处理服务接口
 */
public interface NcDisposalService {

    /**
     * 分页查询不合格品处理
     *
     * @param registrationNo 登记编号（模糊）
     * @param disposalStatus 处理状态
     * @param pageable 分页参数
     * @return 分页结果
     */
    Result<PageResult<NcDisposalEntity>> page(String registrationNo, String disposalStatus, Pageable pageable);

    /**
     * 获取处理详情
     *
     * @param id 处理ID
     * @return 处理详情
     */
    Result<NcDisposalEntity> getById(Long id);

    /**
     * 创建处理
     *
     * @param entity 处理数据
     * @return 创建结果
     */
    Result<NcDisposalEntity> create(NcDisposalEntity entity);

    /**
     * 更新处理
     *
     * @param id 处理ID
     * @param entity 更新数据
     * @return 更新结果
     */
    Result<NcDisposalEntity> update(Long id, NcDisposalEntity entity);

    /**
     * 开始处理
     *
     * @param id 处理ID
     * @return 处理结果
     */
    Result<Void> start(Long id);

    /**
     * 完成处理
     *
     * @param id 处理ID
     * @param processResult 处理结果
     * @return 处理结果
     */
    Result<Void> complete(Long id, String processResult);

    /**
     * 批量开始处理
     *
     * @param ids 处理ID列表
     * @return 处理结果
     */
    Result<Void> batchStart(List<Long> ids);
}
