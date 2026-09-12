package com.hxcoe.aps.repository;
import com.hxcoe.aps.entity.ProcessOperationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProcessOperationRepository extends JpaRepository<ProcessOperationEntity, Long> {

    /**
    * 鏍规嵁璺緞ID鏌ヨ宸ュ簭
    * @param routeId 璺緞ID
    * @return 宸ュ簭鍒楄〃; 
    */
    List<ProcessOperationEntity> findByRouteId(Long routeId);

    /**
    * 根据路线ID和工艺顺序查询工序
    * @param routeId 路线ID
    * @param sequence 工艺顺序
    * @return 工序; 
    */
    ProcessOperationEntity findByRouteIdAndSequence(Long routeId, Integer sequence);
}

