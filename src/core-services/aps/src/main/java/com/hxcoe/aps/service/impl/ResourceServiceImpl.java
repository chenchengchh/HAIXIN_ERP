package com.hxcoe.aps.service.impl;
import com.hxcoe.aps.entity.ResourceCapabilityEntity;
import com.hxcoe.aps.entity.ResourceEntity;
import com.hxcoe.aps.repository.ResourceRepository;
import com.hxcoe.aps.service.ResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ResourceServiceImpl implements ResourceService {

    @Autowired
    private ResourceRepository resourceRepository;

    // 构造方法
    public ResourceServiceImpl() {
        // 默认构造方法
    }

    @Override
    public ResourceEntity createResource(ResourceEntity resource) {
        return resourceRepository.save(resource);
    }

    @Override
    public ResourceEntity getResourceById(Long id) {
        return resourceRepository.findById(id).orElse(null);
    }

    @Override
    public List<ResourceEntity> getResourcesByType(String resourceType) {
        return resourceRepository.findByType(resourceType);
    }

    @Override
    public List<ResourceEntity> getAllResources() {
        return resourceRepository.findAll();
    }

    @Override
    public ResourceEntity updateResource(Long id, ResourceEntity resource) {
        return resourceRepository.save(resource);
    }

    @Override
    public void deleteResource(Long id) {
        resourceRepository.deleteById(id);
    }

    @Override
    public List<ResourceCapabilityEntity> getResourceCapabilities(Long resourceId) {
        // 这里简化实现，实际应该调用ResourceCapabilityRepository
        return List.of();
    }

    @Override
    public boolean isResourceAvailable(Long resourceId) {
        // 这里简化实现，实际应该检查资源状态
        return true;
    }
}
