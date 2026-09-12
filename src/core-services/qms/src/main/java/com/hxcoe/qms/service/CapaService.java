package com.hxcoe.qms.service;

import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import com.hxcoe.qms.entity.CapaEntity;
import org.springframework.data.domain.Pageable;

/**
 * CAPA服务接口
 */
public interface CapaService {

    /**
     * 分页查询CAPA
     *
     * @param reportNo 报告编号（模糊）
     * @param pageable 分页参数
     * @return 分页结果
     */
    Result<PageResult<CapaEntity>> page(String reportNo, Pageable pageable);

    /**
     * 获取CAPA详情
     *
     * @param id 措施ID
     * @return 详情
     */
    Result<CapaEntity> getById(Long id);

    /**
     * 创建CAPA
     *
     * @param entity 措施数据
     * @return 创建结果
     */
    Result<CapaEntity> create(CapaEntity entity);

    /**
     * 更新CAPA
     *
     * @param id 措施ID
     * @param entity 更新数据
     * @return 更新结果
     */
    Result<CapaEntity> update(Long id, CapaEntity entity);

    /**
     * 审核CAPA
     *
     * @param id 措施ID
     * @param reviewer 审核人
     * @param reviewResult 审核结果
     * @return 处理结果
     */
    Result<Void> review(Long id, String reviewer, String reviewResult);

    /**
     * 验证CAPA
     *
     * @param id 措施ID
     * @param verifyResult 验证结果描述
     * @param verifyStatus 验证状态
     * @return 处理结果
     */
    Result<Void> verify(Long id, String verifyResult, String verifyStatus);
}
