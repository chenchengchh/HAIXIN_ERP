package com.hxcoe.aps.service.impl;
import com.hxcoe.aps.entity.SchedulingConstraintEntity;
import com.hxcoe.aps.repository.SchedulingConstraintRepository;
import com.hxcoe.aps.service.SchedulingConstraintService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class SchedulingConstraintServiceImpl implements SchedulingConstraintService {

    @Autowired
    private SchedulingConstraintRepository schedulingConstraintRepository;

    // 构造方法
    public SchedulingConstraintServiceImpl() {
        // 默认构造方法
    }

    @Override
    public SchedulingConstraintEntity createConstraint(SchedulingConstraintEntity constraint) {
        return schedulingConstraintRepository.save(constraint);
    }

    @Override
    public SchedulingConstraintEntity getConstraintById(Long id) {
        return schedulingConstraintRepository.findById(id).orElse(null);
    }

    @Override
    public List<SchedulingConstraintEntity> getConstraintsByType(String constraintType) {
        return schedulingConstraintRepository.findByConstraintType(constraintType);
    }

    @Override
    public List<SchedulingConstraintEntity> getAllActiveConstraints() {
        return schedulingConstraintRepository.findByStatus("active");
    }

    @Override
    public List<SchedulingConstraintEntity> getAllConstraints() {
        return schedulingConstraintRepository.findAll();
    }

    @Override
    public SchedulingConstraintEntity updateConstraint(Long id, SchedulingConstraintEntity constraint) {
        return schedulingConstraintRepository.save(constraint);
    }

    @Override
    public void deleteConstraint(Long id) {
        schedulingConstraintRepository.deleteById(id);
    }

    @Override
    public SchedulingConstraintEntity activateConstraint(Long id) {
        SchedulingConstraintEntity constraint = schedulingConstraintRepository.findById(id).orElse(null);
        if (constraint != null) {
            constraint.setStatus("active");
            return schedulingConstraintRepository.save(constraint);
        }
        return null;
    }

    @Override
    public SchedulingConstraintEntity deactivateConstraint(Long id) {
        SchedulingConstraintEntity constraint = schedulingConstraintRepository.findById(id).orElse(null);
        if (constraint != null) {
            constraint.setStatus("inactive");
            return schedulingConstraintRepository.save(constraint);
        }
        return null;
    }
}
