package com.hxcoe.aps.service.impl;

import com.hxcoe.aps.entity.ResourceCapabilityEntity;
import com.hxcoe.aps.repository.ResourceCapabilityRepository;
import com.hxcoe.aps.service.ResourceCapabilityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.Nonnull;
import java.util.List;

@Service
public class ResourceCapabilityServiceImpl implements ResourceCapabilityService {

    private final ResourceCapabilityRepository resourceCapabilityRepository;

    @Autowired
    public ResourceCapabilityServiceImpl(ResourceCapabilityRepository resourceCapabilityRepository) {
        this.resourceCapabilityRepository = resourceCapabilityRepository;
    }

    @Override
    public @Nonnull ResourceCapabilityEntity createResourceCapability(@Nonnull ResourceCapabilityEntity resourceCapability) {
        // 实际应该使用repository.save()
        return resourceCapabilityRepository.save(resourceCapability);
    }

    @Override
    public ResourceCapabilityEntity getResourceCapabilityById(@Nonnull Long id) {
        // 实际应该使用repository.findById()
        return resourceCapabilityRepository.findById(id).orElse(null);
    }

    @Override
    public @Nonnull List<ResourceCapabilityEntity> getResourceCapabilitiesByResourceId(@Nonnull Long resourceId) {
        // 实际应该使用repository.findByResourceId()
        return resourceCapabilityRepository.findByResourceId(resourceId);
    }

    @Override
    public @Nonnull List<ResourceCapabilityEntity> getResourceCapabilitiesByOperationType(@Nonnull String operationType) {
        // 实际应该使用repository.findByCapabilityType()
        return resourceCapabilityRepository.findByCapabilityType(operationType);
    }

    @Override
    public @Nonnull ResourceCapabilityEntity updateResourceCapability(@Nonnull Long id, @Nonnull ResourceCapabilityEntity resourceCapability) {
        // 实际应该使用repository.save()
        return resourceCapabilityRepository.save(resourceCapability);
    }

    @Override
    public void deleteResourceCapability(@Nonnull Long id) {
        // 实际应该使用repository.deleteById()
        resourceCapabilityRepository.deleteById(id);
    }

    @Override
    public boolean hasCapability(@Nonnull Long resourceId, @Nonnull String operationType) {
        // 实际应该使用repository.findByResourceIdAndCapabilityType()
        List<ResourceCapabilityEntity> capabilities = resourceCapabilityRepository.findByResourceIdAndCapabilityType(resourceId, operationType);
        return !capabilities.isEmpty();
    }
}