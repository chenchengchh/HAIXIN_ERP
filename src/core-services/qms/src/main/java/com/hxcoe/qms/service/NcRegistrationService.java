package com.hxcoe.qms.service;

import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import com.hxcoe.qms.entity.NcRegistrationEntity;
import org.springframework.data.domain.Pageable;

/**
 * 不合格品登记服务接口
 */
public interface NcRegistrationService {

    /**
     * 分页查询不合格品登记
     *
     * @param registrationNo 登记编号（模糊）
     * @param status 状态
     * @param pageable 分页参数
     * @return 分页结果
     */
    Result<PageResult<NcRegistrationEntity>> page(String registrationNo, String status, Pageable pageable);

    /**
     * 获取登记详情
     *
     * @param id 登记ID
     * @return 登记详情
     */
    Result<NcRegistrationEntity> getById(Long id);

    /**
     * 创建登记
     *
     * @param entity 登记数据
     * @return 创建结果
     */
    Result<NcRegistrationEntity> create(NcRegistrationEntity entity);

    /**
     * 更新登记
     *
     * @param id 登记ID
     * @param entity 更新数据
     * @return 更新结果
     */
    Result<NcRegistrationEntity> update(Long id, NcRegistrationEntity entity);

    /**
     * 删除登记
     *
     * @param id 登记ID
     * @return 删除结果
     */
    Result<Void> delete(Long id);

    /**
     * 提交评审
     *
     * @param id 登记ID
     * @return 提交结果
     */
    Result<Void> submitReview(Long id);
}
