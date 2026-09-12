package com.hxcoe.aps.service;

import com.hxcoe.aps.entity.ProcessRouteEntity;

import java.util.List;

public interface ProcessRouteService {

    ProcessRouteEntity createProcessRoute(ProcessRouteEntity processRoute);

    ProcessRouteEntity getProcessRouteById(Long id);

    List<ProcessRouteEntity> getAllProcessRoutes();

    ProcessRouteEntity updateProcessRoute(Long id, ProcessRouteEntity processRoute);

    void deleteProcessRoute(Long id);
}
