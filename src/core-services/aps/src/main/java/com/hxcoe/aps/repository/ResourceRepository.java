package com.hxcoe.aps.repository;
import com.hxcoe.aps.entity.ResourceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ResourceRepository extends JpaRepository<ResourceEntity, Long> {

/**
* 根据资源类型查询资源
* @param type 资源类型
* @return 资源列表;
*/
List<ResourceEntity> findByType(String type);

/**
* 根据资源状态查询资源
* @param status 资源状态
* @return 资源列表;
*/
List<ResourceEntity> findByStatus(String status);
}

