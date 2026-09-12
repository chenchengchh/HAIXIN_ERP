package com.hxcoe.qms.service;

import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import com.hxcoe.qms.entity.NcReviewEntity;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 不合格品评审服务接口
 */
public interface NcReviewService {

    /**
     * 分页查询不合格品评审
     *
     * @param registrationNo 登记编号（模糊）
     * @param reviewStatus 评审状态
     * @param pageable 分页参数
     * @return 分页结果
     */
    Result<PageResult<NcReviewEntity>> page(String registrationNo, String reviewStatus, Pageable pageable);

    /**
     * 获取评审详情
     *
     * @param id 评审ID
     * @return 评审详情
     */
    Result<NcReviewEntity> getById(Long id);

    /**
     * 创建评审
     *
     * @param entity 评审数据
     * @return 创建结果
     */
    Result<NcReviewEntity> create(NcReviewEntity entity);

    /**
     * 更新评审
     *
     * @param id 评审ID
     * @param entity 更新数据
     * @return 更新结果
     */
    Result<NcReviewEntity> update(Long id, NcReviewEntity entity);

    /**
     * 审批评审
     *
     * @param id 评审ID
     * @param reviewStatus 审批状态
     * @param reviewOpinion 审批意见
     * @return 处理结果
     */
    Result<Void> approve(Long id, String reviewStatus, String reviewOpinion);

    /**
     * 批量审批评审
     *
     * @param ids 评审ID列表
     * @param reviewStatus 审批状态
     * @param reviewOpinion 审批意见
     * @return 处理结果
     */
    Result<Void> batchApprove(List<Long> ids, String reviewStatus, String reviewOpinion);
}
