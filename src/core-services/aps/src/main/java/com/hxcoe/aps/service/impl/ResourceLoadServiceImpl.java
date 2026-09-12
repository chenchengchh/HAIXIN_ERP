package com.hxcoe.aps.service.impl;

import com.hxcoe.aps.entity.ResourceLoadEntity;
import com.hxcoe.aps.entity.ResourceEntity;
import com.hxcoe.aps.repository.ResourceLoadRepository;
import com.hxcoe.aps.service.ResourceLoadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;

import java.util.List;


@Service
public class ResourceLoadServiceImpl implements ResourceLoadService {

    @Autowired
    private ResourceLoadRepository resourceLoadRepository;

    @Override
    public List<ResourceLoadEntity> getResourceLoadData(Long planId, List<Long> resourceIds, String startTime, String endTime, String timeScale) {
        if (planId != null) {
            if (resourceIds != null && !resourceIds.isEmpty()) {
                return resourceLoadRepository.findByPlanIdAndResourceIdIn(planId, resourceIds);
            } else {
                return resourceLoadRepository.findByPlanId(planId);
            }
        } else {
            if (resourceIds != null && !resourceIds.isEmpty()) {
                return resourceLoadRepository.findByResourceIdIn(resourceIds);
            } else {
                return resourceLoadRepository.findAll();
            }
        }
    }

    @Override
    public List<ResourceEntity> getResources() {
        return new ArrayList<>();
    }

    @Override
    public ResourceLoadEntity getResourceLoadById(Long id) {
        return resourceLoadRepository.findById(id).orElse(null);
    }

    @Override
    public ResourceLoadEntity createResourceLoad(ResourceLoadEntity load) {
        return resourceLoadRepository.save(load);
    }

    @Override
    public ResourceLoadEntity updateResourceLoad(Long id, ResourceLoadEntity load) {
        load.setId(id);
        return resourceLoadRepository.save(load);
    }

    @Override
    public void deleteResourceLoad(Long id) {
        resourceLoadRepository.deleteById(id);
    }
}
