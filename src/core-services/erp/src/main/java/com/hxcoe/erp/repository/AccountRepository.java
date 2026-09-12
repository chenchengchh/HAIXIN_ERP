package com.hxcoe.erp.repository;

import com.hxcoe.erp.entity.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 会计科目Repository接口
 */
@Repository
public interface AccountRepository extends JpaRepository<AccountEntity, Long>, JpaSpecificationExecutor<AccountEntity> {

    /**
     * 根据科目编码查询
     */
    Optional<AccountEntity> findByAccountCode(String accountCode);

    /**
     * 根据状态查询启用中的科目
     */
    List<AccountEntity> findByStatusAndIsDeleted(Integer status, Integer isDeleted);

    /**
     * 根据父级科目ID查询子科目
     */
    List<AccountEntity> findByParentIdAndIsDeleted(Long parentId, Integer isDeleted);
}
