package com.hxcoe.les.service;

import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import com.hxcoe.les.dto.LesUploadResultDto;
import com.hxcoe.les.entity.LesSignVoucherEntity;
import org.springframework.web.multipart.MultipartFile;

public interface LesSignService {

    /**
     * 分页查询签收凭证
     *
     * @param page   页码（从1开始）
     * @param size   每页条数
     * @param planId 计划ID（可选）
     * @param status 状态（可选）
     * @return 分页结果
     */
    Result<PageResult<LesSignVoucherEntity>> fetchSignVouchers(int page, int size, Long planId, String status);

    /**
     * 查询指定运输计划的最新签收凭证
     *
     * @param planId 计划ID
     * @return 签收凭证
     */
    Result<LesSignVoucherEntity> getPlanSignVoucher(Long planId);

    /**
     * 创建签收凭证
     *
     * @param voucher 凭证数据
     * @return 创建后的凭证
     */
    Result<LesSignVoucherEntity> createSignVoucher(LesSignVoucherEntity voucher);

    /**
     * 更新签收凭证
     *
     * @param id      凭证ID
     * @param voucher 更新数据
     * @return 更新后的凭证
     */
    Result<LesSignVoucherEntity> updateSignVoucher(Long id, LesSignVoucherEntity voucher);

    /**
     * 上传签收图片
     *
     * @param file 上传文件
     * @return 上传结果
     */
    Result<LesUploadResultDto> uploadSignImage(MultipartFile file);

    /**
     * 查询签收统计数据
     *
     * @param startDate 开始日期（可选）
     * @param endDate   结束日期（可选）
     * @return 统计数据（首版返回Map风格键值）
     */
    Result<Object> getSignStats(String startDate, String endDate);

    /**
     * 查询签收异常列表（首版返回空或从凭证聚合）
     *
     * @param page   页码（从1开始）
     * @param size   每页条数
     * @param status 状态（可选）
     * @return 分页结果
     */
    Result<PageResult<Object>> fetchSignAnomalies(int page, int size, String status);
}

