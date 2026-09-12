package com.hxcoe.aps.repository;

import com.hxcoe.aps.entity.ProcessEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProcessRepository extends JpaRepository<ProcessEntity, Long> {

    /**
     * 按工序序号升序查询全部工艺：排程引擎按工艺路线顺序串行排程
     */
    List<ProcessEntity> findAllByOrderBySequenceAsc();
}
