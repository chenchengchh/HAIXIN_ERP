package com.hxcoe.erp.repository;

import com.hxcoe.erp.entity.WriteOffEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 核销Repository接口
 */
@Repository
public interface WriteOffRepository extends JpaRepository<WriteOffEntity, Long>, JpaSpecificationExecutor<WriteOffEntity> {

    /**
     * 根据状态查询核销记录
     *
     * @param status 状态
     * @return 核销实体列表
     */
    List<WriteOffEntity> findByStatus(String status);

    /**
     * 根据核销日期范围查询
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 核销实体列表
     */
    List<WriteOffEntity> findByWriteOffDateBetween(LocalDateTime startDate, LocalDateTime endDate);
}
