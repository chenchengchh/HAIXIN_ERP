package com.hxcoe.scm.repository;

import com.hxcoe.scm.entity.ScmProductionCompletionFactEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * SCM MES 完工事实 Repository（B6 闭环）。
 *
 * <p>继承 {@link JpaSpecificationExecutor} 以支持多条件动态分页查询，
 * 供前端 F2（SCM 完工事实页）按 erpProductionNo、workOrderNo、scmOrderStatus、完工时间范围筛选。
 */
@Repository
public interface ScmProductionCompletionFactRepository
        extends JpaRepository<ScmProductionCompletionFactEntity, Long>,
                JpaSpecificationExecutor<ScmProductionCompletionFactEntity> {

    /**
     * 按幂等键查询（用于去重，同一完工事件只落一次）。
     *
     * @param idempotencyKey 幂等键
     * @return 完工事实实体
     */
    Optional<ScmProductionCompletionFactEntity> findByIdempotencyKey(String idempotencyKey);
}
