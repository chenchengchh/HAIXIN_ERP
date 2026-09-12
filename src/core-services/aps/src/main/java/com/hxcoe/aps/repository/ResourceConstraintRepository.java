package com.hxcoe.aps.repository;
import com.hxcoe.aps.entity.ResourceConstraintEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ResourceConstraintRepository extends JpaRepository<ResourceConstraintEntity, Long> {

    /**
    * 鏍规嵁璧勬簮ID鏌ヨ璧勬簮绾︽潫
    * @param resourceId 璧勬簮ID
    * @return 璧勬簮绾︽潫鍒楄〃; 
    */
    List<ResourceConstraintEntity> findByResourceId(Long resourceId);

    /**
    * 鏍规嵁璧勬簮绾︽潫绫诲瀷鏌ヨ璧勬簮绾︽潫
    * @param constraintType 绾︽潫绫诲瀷
    * @return 璧勬簮绾︽潫鍒楄〃; 
    */
    List<ResourceConstraintEntity> findByConstraintType(String constraintType);
}

