package com.hxcoe.erp.service;


import com.hxcoe.common.result.PageResult;
import com.hxcoe.erp.entity.AccountEntity;


/**
 * 会计科目服务接口
 */
public interface AccountService {

    /**
     * 分页查询会计科目列表
     *
     * @param page 页码
     * @param size 每页条数
     * @param accountName 科目名称
     * @param accountCode 科目编码
     * @param accountType 科目类型
     * @return 分页结果
     */
    PageResult<AccountEntity> getAccountList(Integer page, Integer size, String accountName, String accountCode, String accountType);

    /**
     * 创建会计科目
     *
     * @param accountEntity 科目实体
     * @return 创建结果
     */
    AccountEntity createAccount(AccountEntity accountEntity);

    /**
     * 更新会计科目
     *
     * @param accountEntity 科目实体
     * @return 更新结果
     */
    AccountEntity updateAccount(AccountEntity accountEntity);

    /**
     * 删除会计科目
     *
     * @param id 科目ID
     * @return 是否成功
     */
    boolean deleteAccount(Long id);

    /**
     * 根据ID查询会计科目
     *
     * @param id 科目ID
     * @return 科目实体
     */
    AccountEntity getAccountById(Long id);

    /**
     * 根据编码查询会计科目
     *
     * @param accountCode 科目编码
     * @return 科目实体
     */
    AccountEntity getAccountByCode(String accountCode);
}
