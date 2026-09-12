package com.hxcoe.aps.service.impl;


import com.hxcoe.aps.entity.ProcessRouteEntity;
import com.hxcoe.aps.repository.ProcessRouteRepository;
import com.hxcoe.aps.service.ProcessRouteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProcessRouteServiceImpl implements ProcessRouteService {

    @Autowired
    private ProcessRouteRepository processRouteRepository;

    @Override
    public ProcessRouteEntity createProcessRoute(ProcessRouteEntity processRoute) {
        return processRouteRepository.save(processRoute);
    }

    @Override
    public ProcessRouteEntity getProcessRouteById(Long id) {
        return processRouteRepository.findById(id).orElse(null);
    }

    @Override
    public List<ProcessRouteEntity> getAllProcessRoutes() {
        return processRouteRepository.findAll();
    }

    @Override
    public ProcessRouteEntity updateProcessRoute(Long id, ProcessRouteEntity processRoute) {
        processRoute.setId(id);
        return processRouteRepository.save(processRoute);
    }

    @Override
    public void deleteProcessRoute(Long id) {
        processRouteRepository.deleteById(id);
    }
}
