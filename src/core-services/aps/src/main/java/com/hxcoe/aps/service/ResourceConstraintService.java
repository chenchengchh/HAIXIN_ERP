package com.hxcoe.aps.service;
import com.hxcoe.aps.entity.ResourceConstraintEntity;

import java.util.List;
import java.util.Optional;

public interface ResourceConstraintService {

/**
* 鍒涘缓璧勬簮绾︽潫
* @param resourceConstraint 璧勬簮绾︽潫瀹炰綋
* @return 鍒涘缓鍚庣殑璧勬簮绾︽潫瀹炰綋;
*/
ResourceConstraintEntity createResourceConstraint(ResourceConstraintEntity resourceConstraint);

/**
* 鏇存柊璧勬簮绾︽潫
* @param id 绾︽潫ID
* @param resourceConstraint 鏇存柊鐨勮祫婧愮害鏉熷疄浣?     * @return 鏇存柊鍚庣殑璧勬簮绾︽潫瀹炰綋;
*/
ResourceConstraintEntity updateResourceConstraint(Long id, ResourceConstraintEntity resourceConstraint);

/**
* 鍒犻櫎璧勬簮绾︽潫
* @param id 绾︽潫ID
*/
void deleteResourceConstraint(Long id);

/**
* 鏍规嵁ID鏌ヨ璧勬簮绾︽潫
* @param id 绾︽潫ID
* @return 璧勬簮绾︽潫瀹炰綋;
*/
Optional<ResourceConstraintEntity> getResourceConstraintById(Long id);

/**
    * 鏌ヨ鎵€鏈夎祫婧愮害鏉?     * @return 璧勬簮绾︽潫鍒楄〃;
    */
    List<ResourceConstraintEntity> getAllResourceConstraints();

    /**
    * 鏍规嵁璧勬簮ID鏌ヨ璧勬簮绾︽潫
    * @param resourceId 璧勬簮ID
    * @return 璧勬簮绾︽潫鍒楄〃;
    */
    List<ResourceConstraintEntity> getResourceConstraintsByResourceId(Long resourceId);

/**
* 鏍规嵁绾︽潫绫诲瀷鏌ヨ璧勬簮绾︽潫
* @param constraintType 绾︽潫绫诲瀷
* @return 璧勬簮绾︽潫鍒楄〃;
*/
List<ResourceConstraintEntity> getResourceConstraintsByConstraintType(String constraintType);
}

