package com.hxcoe.aps.service;
import com.hxcoe.aps.entity.ResourceEntity;
import com.hxcoe.aps.entity.ResourceCapabilityEntity;
import java.util.List;

public interface ResourceService {

/**
* 鍒涘缓璧勬簮
* @param resource 璧勬簮瀹炰綋
* @return 璧勬簮瀹炰綋;
*/
ResourceEntity createResource(ResourceEntity resource);

/**
* 鏍规嵁ID鏌ヨ璧勬簮
* @param id 璧勬簮ID
* @return 璧勬簮瀹炰綋;
*/
ResourceEntity getResourceById(Long id);

/**
* 鏍规嵁璧勬簮绫诲瀷鏌ヨ璧勬簮
* @param resourceType 璧勬簮绫诲瀷
* @return 璧勬簮鍒楄〃;
*/
List<ResourceEntity> getResourcesByType(String resourceType);

/**
* 鏌ヨ鎵€鏈夎祫婧?     * @return 璧勬簮鍒楄〃;
*/
List<ResourceEntity> getAllResources();

/**
* 鏇存柊璧勬簮
* @param id 璧勬簮ID
* @param resource 璧勬簮瀹炰綋
* @return 璧勬簮瀹炰綋;
*/
ResourceEntity updateResource(Long id, ResourceEntity resource);

/**
* 鍒犻櫎璧勬簮
* @param id 璧勬簮ID
*/
void deleteResource(Long id);

/**
* 鑾峰彇璧勬簮鐨勮兘鍔涘垪琛?     * @param resourceId 璧勬簮ID
* @return 璧勬簮鑳藉姏鍒楄〃;
*/
List<ResourceCapabilityEntity> getResourceCapabilities(Long resourceId);

/**
* 妫€鏌ヨ祫婧愭槸鍚﹀彲鐢?     * @param resourceId 璧勬簮ID
* @return 鏄惁鍙敤;
*/
boolean isResourceAvailable(Long resourceId);
}

