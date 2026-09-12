package com.hxcoe.erp.service;

import com.hxcoe.erp.entity.VoucherEntity;
import com.hxcoe.common.result.PageResult;

import java.time.LocalDateTime;

/**
 * 鍑瘉鏈嶅姟鎺ュ彛
 */
public interface VoucherService {

    /**
     * 鍒涘缓鍑瘉
     *
     * @param voucherEntity 鍑瘉瀹炰綋
     * @return 鍒涘缓缁撴灉
     */
    VoucherEntity createVoucher(VoucherEntity voucherEntity);

    /**
     * 鏍规嵁ID鏌ヨ鍑瘉
     *
     * @param id 涓婚敭ID
     * @return 鏌ヨ缁撴灉
     */
    VoucherEntity getVoucherById(Long id);

    /**
     * 鏍规嵁鍑瘉缂栧彿鏌ヨ鍑瘉
     *
     * @param voucherNo 鍑瘉缂栧彿
     * @return 鏌ヨ缁撴灉
     */
    VoucherEntity getVoucherByNo(String voucherNo);

    /**
     * 鏇存柊鍑瘉
     *
     * @param voucherEntity 鍑瘉瀹炰綋
     * @return 鏇存柊缁撴灉
     */
    VoucherEntity updateVoucher(VoucherEntity voucherEntity);

    /**
     * 鍒犻櫎鍑瘉
     *
     * @param id 涓婚敭ID
     * @return 鍒犻櫎缁撴灉
     */
    boolean deleteVoucher(Long id);

    /**
     * 鍒嗛〉鏌ヨ鍑瘉鍒楄〃
     *
     * @param page 褰撳墠椤电爜
     * @param size 姣忛〉鏉℃暟
     * @param voucherType 鍑瘉绫诲瀷
     * @param status 鐘讹拷?     * @param startDate 寮€濮嬫棩锟?     * @param endDate 缁撴潫鏃ユ湡
     * @return 鍒嗛〉缁撴灉
     */
    PageResult<VoucherEntity> getVoucherList(Integer page, Integer size, String voucherType, String status, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 瀹℃牳鍑瘉
     *
     * @param id 涓婚敭ID
     * @return 瀹℃牳缁撴灉
     */
    VoucherEntity approveVoucher(Long id);

    /**
     * 杩囪处鍑瘉
     *
     * @param id 涓婚敭ID
     * @return 杩囪处缁撴灉
     */
    VoucherEntity postVoucher(Long id);

    /**
     * 鎷掔粷鍑瘉
     *
     * @param id 涓婚敭ID
     * @return 鎷掔粷缁撴灉
     */
    VoucherEntity rejectVoucher(Long id);

    /**
     * 鎻愪氦鍑瘉
     *
     * @param id 涓婚敭ID
     * @return 鎻愪氦缁撴灉
     */
    VoucherEntity submitVoucher(Long id);

    /**
     * 提交凭证并发起OA统一审批。
     * <p>凭证状态置为submitted（审批中），同时向OA提交审批申请，
     * 审批完成后OA回调更新凭证状态为approved/rejected。</p>
     *
     * @param id            凭证ID
     * @param initiatorId   发起人ID
     * @param initiatorName 发起人名称
     * @return 提交结果
     */
    VoucherEntity submitVoucherForApproval(Long id, Long initiatorId, String initiatorName);

    /**
     * 鐢熸垚鍑瘉缂栧彿
     *
     * @param voucherType 鍑瘉绫诲瀷
     * @return 鍑瘉缂栧彿
     */
    String generateVoucherNo(String voucherType);
}

