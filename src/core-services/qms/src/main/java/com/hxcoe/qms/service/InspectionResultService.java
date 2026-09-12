package com.hxcoe.qms.service;

import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import com.hxcoe.qms.entity.InspectionResultEntity;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 检验结果服务接口
 */
public interface InspectionResultService {

    /**
     * 分页查询检验结果
     *
     * @param resultNo 结果编号（模糊）
     * @param taskNo 任务编号（模糊）
     * @param auditStatus 审核状态
     * @param pageable 分页参数
     * @return 分页结果
     */
    Result<PageResult<InspectionResultEntity>> page(String resultNo, String taskNo, String auditStatus, Pageable pageable);

    /**
     * 获取检验结果详情
     *
     * @param id 结果ID
     * @return 结果详情
     */
    Result<InspectionResultEntity> getById(Long id);

    /**
     * 创建检验结果
     *
     * @param entity 结果数据
     * @return 创建结果
     */
    Result<InspectionResultEntity> create(InspectionResultEntity entity);

    /**
     * 更新检验结果
     *
     * @param id 结果ID
     * @param entity 更新数据
     * @return 更新结果
     */
    Result<InspectionResultEntity> update(Long id, InspectionResultEntity entity);

    /**
     * 审核检验结果
     *
     * @param id 结果ID
     * @param auditStatus 审核状态
     * @param auditRemark 审核备注
     * @param auditor 审核人
     * @return 审核结果
     */
    Result<Void> audit(Long id, String auditStatus, String auditRemark, String auditor);

    /**
     * 批量审核检验结果
     *
     * @param ids 结果ID列表
     * @param auditStatus 审核状态
     * @param auditRemark 审核备注
     * @return 处理结果
     */
    Result<Void> batchAudit(List<Long> ids, String auditStatus, String auditRemark);
}
