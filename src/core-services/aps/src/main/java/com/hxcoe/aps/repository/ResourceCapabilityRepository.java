package com.hxcoe.aps.repository;
import com.hxcoe.aps.entity.ResourceCapabilityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ResourceCapabilityRepository extends JpaRepository<ResourceCapabilityEntity, Long> {

    /**
    * 根据资源ID查询资源能力
    * @param resourceId 资源ID
    * @return 资源能力列表; 
    */
    List<ResourceCapabilityEntity> findByResourceId(Long resourceId);

    /**
    * 根据资源ID和能力类型查询资源能力
    * @param resourceId 资源ID
    * @param capabilityType 能力类型
    * @return 资源能力列表; 
    */
    List<ResourceCapabilityEntity> findByResourceIdAndCapabilityType(Long resourceId, String capabilityType);

    /**
    * 根据能力类型查询资源能力
    * @param capabilityType 能力类型
    * @return 资源能力列表; 
    */
    List<ResourceCapabilityEntity> findByCapabilityType(String capabilityType);
}

