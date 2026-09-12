package com.hxcoe.aps.repository;
import com.hxcoe.aps.entity.ProcessRouteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProcessRouteRepository extends JpaRepository<ProcessRouteEntity, Long> {

    /**
    * 根据产品代码查询工艺路线
    * @param productCode 产品代码
    * @return 工艺路线列表; 
    */
    List<ProcessRouteEntity> findByProductCode(String productCode);
}

