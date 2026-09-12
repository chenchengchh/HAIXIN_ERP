package com.hxcoe.aps.service;

import com.hxcoe.aps.entity.ResourceLoadEntity;
import com.hxcoe.aps.entity.ResourceEntity;
import java.util.List;


public interface ResourceLoadService {

    List<ResourceLoadEntity> getResourceLoadData(Long planId, List<Long> resourceIds, String startTime, String endTime, String timeScale);

    List<ResourceEntity> getResources();

    ResourceLoadEntity getResourceLoadById(Long id);

    ResourceLoadEntity createResourceLoad(ResourceLoadEntity load);

    ResourceLoadEntity updateResourceLoad(Long id, ResourceLoadEntity load);

    void deleteResourceLoad(Long id);
}
