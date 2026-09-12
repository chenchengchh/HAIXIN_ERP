package com.hxcoe.aps.service.impl;

import com.hxcoe.aps.entity.AlgorithmParamEntity;
import com.hxcoe.aps.repository.AlgorithmParamRepository;
import com.hxcoe.aps.service.AlgorithmParamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AlgorithmParamServiceImpl implements AlgorithmParamService {

    @Autowired
    private AlgorithmParamRepository algorithmParamRepository;

    @Override
    public List<AlgorithmParamEntity> getAlgorithmParams() {
        return algorithmParamRepository.findAll();
    }

    @Override
    public Page<AlgorithmParamEntity> getAlgorithmParamsByPage(Pageable pageable) {
        return algorithmParamRepository.findAll(pageable);
    }

    @Override
    public AlgorithmParamEntity getAlgorithmParamById(Long id) {
        return algorithmParamRepository.findById(id).orElse(null);
    }

    @Override
    public List<AlgorithmParamEntity> getAlgorithmParamsByAlgorithmName(String algorithmName) {
        return algorithmParamRepository.findByAlgorithmName(algorithmName);
    }

    @Override
    public AlgorithmParamEntity createAlgorithmParam(AlgorithmParamEntity algorithmParam) {
        return algorithmParamRepository.save(algorithmParam);
    }

    @Override
    public AlgorithmParamEntity updateAlgorithmParam(Long id, AlgorithmParamEntity algorithmParam) {
        algorithmParam.setId(id);
        return algorithmParamRepository.save(algorithmParam);
    }

    @Override
    public void deleteAlgorithmParam(Long id) {
        algorithmParamRepository.deleteById(id);
    }
}
