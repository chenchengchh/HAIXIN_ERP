package com.hxcoe.qms.repository;

import com.hxcoe.qms.entity.DataCollectionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

/**
 * 质量数据采集仓库
 */
public interface DataCollectionRepository extends JpaRepository<DataCollectionEntity, Long>, JpaSpecificationExecutor<DataCollectionEntity> {

    /**
     * 根据采集编号查询
     *
     * @param collectionNo 采集编号
     * @return 采集记录
     */
    Optional<DataCollectionEntity> findByCollectionNo(String collectionNo);
}
