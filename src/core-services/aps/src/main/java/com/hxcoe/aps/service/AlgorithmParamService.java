package com.hxcoe.aps.service;

import com.hxcoe.aps.entity.AlgorithmParamEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface AlgorithmParamService {

    List<AlgorithmParamEntity> getAlgorithmParams();

    Page<AlgorithmParamEntity> getAlgorithmParamsByPage(Pageable pageable);

    AlgorithmParamEntity getAlgorithmParamById(Long id);

    List<AlgorithmParamEntity> getAlgorithmParamsByAlgorithmName(String algorithmName);

    AlgorithmParamEntity createAlgorithmParam(AlgorithmParamEntity param);

    AlgorithmParamEntity updateAlgorithmParam(Long id, AlgorithmParamEntity param);

    void deleteAlgorithmParam(Long id);
}
