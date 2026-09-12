package com.hxcoe.scm.controller;

import com.hxcoe.common.api.ApiResponse;
import com.hxcoe.common.result.PageResult;
import com.hxcoe.scm.entity.ShipmentEntity;
import com.hxcoe.scm.entity.TransportEventEntity;
import com.hxcoe.scm.service.LogisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/scm/logistics", "/api/scm/logistics"})
public class LogisticsController {

    @Autowired
    private LogisticsService logisticsService;

    @PostMapping("/shipments")
    public ApiResponse<ShipmentEntity> createShipment(@RequestBody ShipmentEntity shipment) {
        return success("创建成功", logisticsService.createShipment(shipment));
    }

    @PutMapping("/shipments/{id}")
    public ApiResponse<ShipmentEntity> updateShipment(@PathVariable Long id, @RequestBody ShipmentEntity shipment) {
        ShipmentEntity updated = logisticsService.updateShipment(id, shipment);
        if (updated == null) {
            return notFound("运单不存在");
        }
        return success("更新成功", updated);
    }

    @GetMapping("/shipments/{id}")
    public ApiResponse<ShipmentEntity> getShipment(@PathVariable Long id) {
        ShipmentEntity entity = logisticsService.getShipment(id);
        if (entity == null) {
            return notFound("运单不存在");
        }
        return success("查询成功", entity);
    }

    @GetMapping("/shipments")
    public ApiResponse<PageResult<ShipmentEntity>> getShipments(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "id"));
        return success("查询成功", logisticsService.getShipments(keyword, status, pageable));
    }

    @PostMapping("/shipments/{id}/events")
    public ApiResponse<TransportEventEntity> addEvent(@PathVariable Long id, @RequestBody TransportEventEntity event) {
        TransportEventEntity saved = logisticsService.addEvent(id, event);
        if (saved == null) {
            return notFound("运单不存在");
        }
        return success("新增事件成功", saved);
    }

    @GetMapping("/shipments/{id}/events")
    public ApiResponse<List<TransportEventEntity>> getEvents(@PathVariable Long id) {
        return success("查询成功", logisticsService.getEvents(id));
    }

    @PutMapping("/shipments/{id}/arrive")
    public ApiResponse<ShipmentEntity> markArrived(@PathVariable Long id) {
        ShipmentEntity updated = logisticsService.markArrived(id);
        if (updated == null) {
            return notFound("运单不存在");
        }
        return success("到货确认成功", updated);
    }

    private static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.success(message, data);
    }

    private static <T> ApiResponse<T> notFound(String message) {
        return ApiResponse.error(404, message);
    }
}
