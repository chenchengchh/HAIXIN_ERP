package com.hxcoe.aps.service.impl;
import com.hxcoe.aps.entity.ResourceConstraintEntity;
import com.hxcoe.aps.repository.ResourceConstraintRepository;
import com.hxcoe.aps.service.ResourceConstraintService;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ResourceConstraintServiceImpl implements ResourceConstraintService {

    @Autowired
    private ResourceConstraintRepository resourceConstraintRepository;

    // 构造方法
    public ResourceConstraintServiceImpl() {
        // 默认构造方法
    }

    @Override
    public ResourceConstraintEntity createResourceConstraint(ResourceConstraintEntity resourceConstraint) {
        return resourceConstraintRepository.save(resourceConstraint);
    }

    @Override
    public ResourceConstraintEntity updateResourceConstraint(Long id, ResourceConstraintEntity resourceConstraint) {
        return resourceConstraintRepository.save(resourceConstraint);
    }

    @Override
    public void deleteResourceConstraint(Long id) {
        resourceConstraintRepository.deleteById(id);
    }

    @Override
    public Optional<ResourceConstraintEntity> getResourceConstraintById(Long id) {
        return resourceConstraintRepository.findById(id);
    }

    @Override
    public List<ResourceConstraintEntity> getAllResourceConstraints() {
        return resourceConstraintRepository.findAll();
    }

    @Override
    public List<ResourceConstraintEntity> getResourceConstraintsByResourceId(Long resourceId) {
        return resourceConstraintRepository.findByResourceId(resourceId);
    }

    @Override
    public List<ResourceConstraintEntity> getResourceConstraintsByConstraintType(String constraintType) {
        return resourceConstraintRepository.findByConstraintType(constraintType);
    }
}
