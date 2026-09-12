package com.hxcoe.aps.service.impl;
import com.hxcoe.aps.entity.ProcessOperationEntity;
import com.hxcoe.aps.repository.ProcessOperationRepository;
import com.hxcoe.aps.service.ProcessOperationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ProcessOperationServiceImpl implements ProcessOperationService {

    @Autowired
    private ProcessOperationRepository processOperationRepository;

    // 构造方法
    public ProcessOperationServiceImpl() {
        // 默认构造方法
    }

    @Override
    public ProcessOperationEntity createProcessOperation(ProcessOperationEntity processOperation) {
        return processOperationRepository.save(processOperation);
    }

    @Override
    public ProcessOperationEntity getProcessOperationById(Long id) {
        return processOperationRepository.findById(id).orElse(null);
    }

    @Override
    public List<ProcessOperationEntity> getProcessOperationsByRouteId(Long routeId) {
        return processOperationRepository.findByRouteId(routeId);
    }

    @Override
    public ProcessOperationEntity updateProcessOperation(Long id, ProcessOperationEntity processOperation) {
        return processOperationRepository.save(processOperation);
    }

    @Override
    public void deleteProcessOperation(Long id) {
        processOperationRepository.deleteById(id);
    }

    @Override
    public ProcessOperationEntity updateOperationPredecessors(Long id, String predecessors) {
        // 由于ProcessOperationEntity中没有predecessors字段，暂时返回null
        return null;
    }
}
