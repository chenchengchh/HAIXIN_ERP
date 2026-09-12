package com.hxcoe.aps.service;
import com.hxcoe.aps.entity.ResourceCapabilityEntity;
import java.util.List;

public interface ResourceCapabilityService {

/**
* 鍒涘缓璧勬簮鑳藉姏
* @param resourceCapability 璧勬簮鑳藉姏瀹炰綋
* @return 璧勬簮鑳藉姏瀹炰綋;
*/
ResourceCapabilityEntity createResourceCapability(ResourceCapabilityEntity resourceCapability);

/**
* 鏍规嵁ID鏌ヨ璧勬簮鑳藉姏
* @param id 璧勬簮鑳藉姏ID
* @return 璧勬簮鑳藉姏瀹炰綋;
*/
ResourceCapabilityEntity getResourceCapabilityById(Long id);

/**
* 鏍规嵁璧勬簮ID鏌ヨ璧勬簮鑳藉姏
* @param resourceId 璧勬簮ID
* @return 璧勬簮鑳藉姏鍒楄〃;
*/
List<ResourceCapabilityEntity> getResourceCapabilitiesByResourceId(Long resourceId);

/**
* 鏍规嵁鎿嶄綔绫诲瀷鏌ヨ璧勬簮鑳藉姏
* @param operationType 鎿嶄綔绫诲瀷
* @return 璧勬簮鑳藉姏鍒楄〃;
*/
List<ResourceCapabilityEntity> getResourceCapabilitiesByOperationType(String operationType);

/**
* 鏇存柊璧勬簮鑳藉姏
* @param id 璧勬簮鑳藉姏ID
* @param resourceCapability 璧勬簮鑳藉姏瀹炰綋
* @return 璧勬簮鑳藉姏瀹炰綋;
*/
ResourceCapabilityEntity updateResourceCapability(Long id, ResourceCapabilityEntity resourceCapability);

/**
* 鍒犻櫎璧勬簮鑳藉姏
* @param id 璧勬簮鑳藉姏ID
*/
void deleteResourceCapability(Long id);

/**
* 妫€鏌ヨ祫婧愭槸鍚﹀叿鏈夌壒瀹氳兘鍔?     * @param resourceId 璧勬簮ID
* @param operationType 鎿嶄綔绫诲瀷
* @return 鏄惁鍏锋湁璇ヨ兘鍔?     */;
boolean hasCapability(Long resourceId, String operationType);
}

