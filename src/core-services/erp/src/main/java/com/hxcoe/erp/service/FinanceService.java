package com.hxcoe.erp.service;

import com.hxcoe.erp.entity.FinanceEntity;
import com.hxcoe.erp.entity.WriteOffEntity;
import com.hxcoe.erp.entity.CostCalculationEntity;
import com.hxcoe.erp.model.GeneralLedgerDTO;
import com.hxcoe.common.result.PageResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 财务服务接口
 */
public interface FinanceService {

    /**
     * 创建财务记录
     *
     * @param financeEntity 财务实体
     * @return 保存后的财务实体
     */
    FinanceEntity createFinance(FinanceEntity financeEntity);

    /**
     * 根据ID查询财务记录
     *
     * @param id 主键ID
     * @return 财务实体
     */
    FinanceEntity getFinanceById(Long id);

    /**
     * 根据财务单号查询财务记录
     *
     * @param financeNo 财务单号
     * @return 财务实体
     */
    FinanceEntity getFinanceByNo(String financeNo);

    /**
     * 更新财务记录
     *
     * @param financeEntity 财务实体
     * @return 更新后的财务实体
     */
    FinanceEntity updateFinance(FinanceEntity financeEntity);

    /**
     * 删除财务记录
     *
     * @param id 主键ID
     * @return 删除结果
     */
    boolean deleteFinance(Long id);

    /**
     * 分页查询财务记录
     *
     * @param page 当前页码
     * @param size 每页条数
     * @param transactionType 交易类型
     * @param status 状态
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 分页结果
     */
    PageResult<FinanceEntity> getFinanceList(Integer page, Integer size, Integer transactionType, Integer status, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 计算指定日期范围内的总收入
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 总收入
     */
    BigDecimal calculateTotalIncome(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 计算指定日期范围内的总支出
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 总支出
     */
    BigDecimal calculateTotalExpense(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 测试财务模块接口
     *
     * @return 测试结果
     */
    String testFinance();

    /**
     * 创建核销记录
     *
     * @param writeOffEntity 核销实体
     * @return 创建结果
     */
    WriteOffEntity createWriteOff(WriteOffEntity writeOffEntity);

    /**
     * 获取核销记录列表
     *
     * @param page 页码
     * @param size 每页条数
     * @param status 状态
     * @return 分页结果
     */
    PageResult<WriteOffEntity> getWriteOffList(Integer page, Integer size, String status);

    /**
     * 根据ID获取核销记录
     *
     * @param id 核销ID
     * @return 查询结果
     */
    WriteOffEntity getWriteOffById(Long id);

    /**
     * 执行成本核算
     *
     * @param costCalculationEntity 成本核算实体
     * @return 核算结果
     */
    CostCalculationEntity calculateCost(CostCalculationEntity costCalculationEntity);

    /**
     * 获取成本核算结果
     *
     * @param calculationId 核算ID
     * @return 核算结果
     */
    CostCalculationEntity getCostResults(Long calculationId);

    /**
     * 获取总账数据
     *
     * @param page 页码
     * @param size 每页条数
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 分页结果
     */
    PageResult<GeneralLedgerDTO> getGeneralLedger(Integer page, Integer size, LocalDateTime startDate, LocalDateTime endDate);
}


