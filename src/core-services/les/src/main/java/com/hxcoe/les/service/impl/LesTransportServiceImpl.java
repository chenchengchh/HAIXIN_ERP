package com.hxcoe.les.service.impl;

import com.hxcoe.common.api.ResponseStatusAdapter;
import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import com.hxcoe.les.client.AgvClient;
import com.hxcoe.les.client.CrmOrderClient;
import com.hxcoe.les.client.WmsOutboundClient;
import com.hxcoe.les.dto.LesSalesOrderDto;
import com.hxcoe.les.entity.LogisticsOrderEntity;
import com.hxcoe.les.entity.LesDriverEntity;
import com.hxcoe.les.entity.LesRouteEntity;
import com.hxcoe.les.entity.LesTransportPlanEntity;
import com.hxcoe.les.entity.LesTransportTaskEntity;
import com.hxcoe.les.entity.LesVehicleEntity;
import com.hxcoe.les.repository.LogisticsOrderRepository;
import com.hxcoe.les.repository.LesDriverRepository;
import com.hxcoe.les.repository.LesRouteRepository;
import com.hxcoe.les.repository.LesTransportPlanRepository;
import com.hxcoe.les.repository.LesTransportTaskRepository;
import com.hxcoe.les.repository.LesVehicleRepository;
import com.hxcoe.les.service.LesTransportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class LesTransportServiceImpl implements LesTransportService {

    private static final Logger log = LoggerFactory.getLogger(LesTransportServiceImpl.class);

    @Autowired
    private LesTransportPlanRepository transportPlanRepository;

    @Autowired
    private LesVehicleRepository vehicleRepository;

    @Autowired
    private LesDriverRepository driverRepository;

    @Autowired
    private LesRouteRepository routeRepository;

    @Autowired
    private LesTransportTaskRepository taskRepository;

    @Autowired
    private LogisticsOrderRepository logisticsOrderRepository;

    @Autowired(required = false)
    private WmsOutboundClient wmsOutboundClient;

    @Autowired(required = false)
    private CrmOrderClient crmOrderClient;

    @Autowired(required = false)
    private AgvClient agvClient;

    @Override
    public Result<PageResult<LesTransportPlanEntity>> fetchTransportPlans(int page, int size, String keyword, Integer status) {
        Pageable pageable = PageRequest.of(Math.max(page, 1) - 1, Math.max(size, 1), Sort.by(Sort.Direction.DESC, "createTime"));
        Page<LesTransportPlanEntity> result;
        boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();

        if (status != null && hasKeyword) {
            String k = keyword.trim();
            result = transportPlanRepository.findByStatusAndPlanNoContainingIgnoreCaseOrStatusAndSalesOrderNoContainingIgnoreCase(status, k, status, k, pageable);
        } else if (status != null) {
            result = transportPlanRepository.findByStatus(status, pageable);
        } else if (hasKeyword) {
            String k = keyword.trim();
            result = transportPlanRepository.findByPlanNoContainingIgnoreCaseOrSalesOrderNoContainingIgnoreCase(k, k, pageable);
        } else {
            result = transportPlanRepository.findAll(pageable);
        }

        PageResult<LesTransportPlanEntity> pageResult = PageResult.build(result.getTotalElements(), result.getSize(), result.getNumber() + 1, result.getContent());
        return Result.success("运输计划查询成功", pageResult);
    }

    @Override
    public Result<LesTransportPlanEntity> getTransportPlanDetail(Long id) {
        LesTransportPlanEntity entity = transportPlanRepository.findById(id).orElse(null);
        if (entity == null) {
            return Result.fail("运输计划不存在");
        }
        return Result.success(entity);
    }

    @Override
    public Result<LesTransportPlanEntity> createTransportPlan(LesTransportPlanEntity plan) {
        if (plan == null) {
            return Result.fail("运输计划数据不能为空");
        }
        if (plan.getPlanNo() == null || plan.getPlanNo().trim().isEmpty()) {
            plan.setPlanNo(generatePlanNo());
        }
        if (transportPlanRepository.existsByPlanNo(plan.getPlanNo())) {
            return Result.fail("计划编号已存在");
        }
        if (plan.getStatus() == null) {
            plan.setStatus(1);
        }
        if (plan.getSourceType() == null || plan.getSourceType().trim().isEmpty()) {
            plan.setSourceType((plan.getSalesOrderNo() != null && !plan.getSalesOrderNo().trim().isEmpty()) ? "WMS" : "LOCAL");
        }
        if (plan.getSourceNo() == null || plan.getSourceNo().trim().isEmpty()) {
            plan.setSourceNo(plan.getSalesOrderNo());
        }
        LocalDateTime now = LocalDateTime.now();
        plan.setCreateTime(now);
        plan.setUpdateTime(now);
        LesTransportPlanEntity saved = transportPlanRepository.save(plan);
        return Result.success("运输计划创建成功", saved);
    }

    @Override
    public Result<LesTransportPlanEntity> updateTransportPlan(Long id, LesTransportPlanEntity plan) {
        LesTransportPlanEntity existing = transportPlanRepository.findById(id).orElse(null);
        if (existing == null) {
            return Result.fail("运输计划不存在");
        }
        if (plan == null) {
            return Result.fail("更新数据不能为空");
        }
        if (plan.getPlanNo() != null && !Objects.equals(plan.getPlanNo(), existing.getPlanNo())) {
            if (transportPlanRepository.existsByPlanNo(plan.getPlanNo())) {
                return Result.fail("计划编号已存在");
            }
            existing.setPlanNo(plan.getPlanNo());
        }
        if (plan.getSalesOrderNo() != null) {
            existing.setSalesOrderNo(plan.getSalesOrderNo());
        }
        if (plan.getSourceType() != null) {
            existing.setSourceType(plan.getSourceType());
        }
        if (plan.getSourceNo() != null) {
            existing.setSourceNo(plan.getSourceNo());
        }
        if (plan.getVehicleId() != null) {
            existing.setVehicleId(plan.getVehicleId());
        }
        if (plan.getDriverId() != null) {
            existing.setDriverId(plan.getDriverId());
        }
        if (plan.getRouteId() != null) {
            existing.setRouteId(plan.getRouteId());
        }
        if (plan.getStatus() != null) {
            existing.setStatus(plan.getStatus());
        }
        if (plan.getCostEstimated() != null) {
            existing.setCostEstimated(plan.getCostEstimated());
        }
        existing.setUpdateTime(LocalDateTime.now());
        LesTransportPlanEntity saved = transportPlanRepository.save(existing);
        return Result.success("运输计划更新成功", saved);
    }

    @Override
    public Result<Void> deleteTransportPlan(Long id) {
        if (!transportPlanRepository.existsById(id)) {
            return Result.fail("运输计划不存在");
        }
        transportPlanRepository.deleteById(id);
        return Result.success("运输计划删除成功");
    }

    @Override
    public Result<PageResult<LesVehicleEntity>> fetchVehicles(int page, int size, String keyword, String status) {
        Pageable pageable = PageRequest.of(Math.max(page, 1) - 1, Math.max(size, 1), Sort.by(Sort.Direction.DESC, "createTime"));
        Page<LesVehicleEntity> result;
        boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();
        boolean hasStatus = status != null && !status.trim().isEmpty();
        if (hasStatus && hasKeyword) {
            result = vehicleRepository.findByStatusAndLicensePlateContainingIgnoreCase(status.trim(), keyword.trim(), pageable);
        } else if (hasStatus) {
            result = vehicleRepository.findByStatus(status.trim(), pageable);
        } else if (hasKeyword) {
            result = vehicleRepository.findByLicensePlateContainingIgnoreCase(keyword.trim(), pageable);
        } else {
            result = vehicleRepository.findAll(pageable);
        }
        PageResult<LesVehicleEntity> pageResult = PageResult.build(result.getTotalElements(), result.getSize(), result.getNumber() + 1, result.getContent());
        return Result.success("车辆查询成功", pageResult);
    }

    @Override
    public Result<PageResult<LesDriverEntity>> fetchDrivers(int page, int size, String keyword, String status) {
        Pageable pageable = PageRequest.of(Math.max(page, 1) - 1, Math.max(size, 1), Sort.by(Sort.Direction.DESC, "createTime"));
        Page<LesDriverEntity> result;
        boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();
        boolean hasStatus = status != null && !status.trim().isEmpty();
        if (hasStatus && hasKeyword) {
            String k = keyword.trim();
            String s = status.trim();
            result = driverRepository.findByStatusAndNameContainingIgnoreCaseOrStatusAndPhoneContainingIgnoreCase(s, k, s, k, pageable);
        } else if (hasStatus) {
            result = driverRepository.findByStatus(status.trim(), pageable);
        } else if (hasKeyword) {
            String k = keyword.trim();
            result = driverRepository.findByNameContainingIgnoreCaseOrPhoneContainingIgnoreCase(k, k, pageable);
        } else {
            result = driverRepository.findAll(pageable);
        }
        PageResult<LesDriverEntity> pageResult = PageResult.build(result.getTotalElements(), result.getSize(), result.getNumber() + 1, result.getContent());
        return Result.success("司机查询成功", pageResult);
    }

    @Override
    public Result<PageResult<LesRouteEntity>> fetchRoutes(int page, int size, String keyword) {
        Pageable pageable = PageRequest.of(Math.max(page, 1) - 1, Math.max(size, 1), Sort.by(Sort.Direction.DESC, "createTime"));
        Page<LesRouteEntity> result;
        boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();
        if (hasKeyword) {
            result = routeRepository.findByRouteNameContainingIgnoreCase(keyword.trim(), pageable);
        } else {
            result = routeRepository.findAll(pageable);
        }
        PageResult<LesRouteEntity> pageResult = PageResult.build(result.getTotalElements(), result.getSize(), result.getNumber() + 1, result.getContent());
        return Result.success("路线查询成功", pageResult);
    }

    @Override
    public Result<PageResult<LesSalesOrderDto>> fetchSalesOrders(int page, int size, String keyword) {
        String k = keyword == null ? null : keyword.trim();

        try {
            if (wmsOutboundClient != null) {
                Result<PageResult<Map<String, Object>>> wmsResult = wmsOutboundClient.listOutboundOrders(
                        Math.max(page, 1),
                        Math.max(size, 1),
                        null,
                        "SALES",
                        (k == null || k.isEmpty()) ? null : k,
                        null
                );
                if (isRemoteSuccess(wmsResult) && wmsResult.getData() != null) {
                    PageResult<Map<String, Object>> data = wmsResult.getData();
                    List<LesSalesOrderDto> rows = data.getRecords().stream().map(this::toSalesOrderDtoFromWms).toList();
                    PageResult<LesSalesOrderDto> pageResult = PageResult.build(data.getTotal(), data.getPageSize(), data.getCurrentPage(), rows);
                    return Result.success("销售订单查询成功", pageResult);
                }
            }
        } catch (Exception ignored) {
        }

        try {
            if (crmOrderClient != null) {
                Result<PageResult<Map<String, Object>>> crmResult = crmOrderClient.listMyOrders(
                        Math.max(page, 1),
                        Math.max(size, 1),
                        (k == null || k.isEmpty()) ? null : k,
                        null,
                        null,
                        null,
                        null
                );
                if (isRemoteSuccess(crmResult) && crmResult.getData() != null) {
                    PageResult<Map<String, Object>> data = crmResult.getData();
                    List<LesSalesOrderDto> rows = data.getRecords().stream().map(this::toSalesOrderDtoFromCrm).toList();
                    PageResult<LesSalesOrderDto> pageResult = PageResult.build(data.getTotal(), data.getPageSize(), data.getCurrentPage(), rows);
                    return Result.success("销售订单查询成功", pageResult);
                }
            }
        } catch (Exception ignored) {
        }

        Pageable pageable = PageRequest.of(Math.max(page, 1) - 1, Math.max(size, 1), Sort.by(Sort.Direction.DESC, "createdTime"));
        boolean hasKeyword = k != null && !k.isEmpty();
        Page<LogisticsOrderEntity> result;
        if (hasKeyword) {
            Specification<LogisticsOrderEntity> spec = (root, query, cb) -> {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(cb.or(
                        cb.like(root.get("orderCode"), "%" + k + "%"),
                        cb.like(root.get("destinationLocation"), "%" + k + "%"),
                        cb.like(root.get("sourceLocation"), "%" + k + "%")
                ));
                return cb.and(predicates.toArray(new Predicate[0]));
            };
            result = logisticsOrderRepository.findAll(spec, pageable);
        } else {
            result = logisticsOrderRepository.findAll(pageable);
        }
        List<LesSalesOrderDto> rows = result.getContent().stream().map(this::toSalesOrderDto).toList();
        PageResult<LesSalesOrderDto> pageResult = PageResult.build(result.getTotalElements(), result.getSize(), result.getNumber() + 1, rows);
        return Result.success("销售订单查询成功", pageResult);
    }

    @Override
    public Result<LesTransportTaskEntity> createTransportTask(LesTransportTaskEntity task) {
        if (task == null) {
            return Result.fail("运输任务数据不能为空");
        }
        if (task.getTaskNo() == null || task.getTaskNo().trim().isEmpty()) {
            task.setTaskNo(generateTaskNo());
        }
        if (task.getStatus() == null || task.getStatus().trim().isEmpty()) {
            task.setStatus("pending");
        }
        task.setCreateTime(LocalDateTime.now());
        LesTransportTaskEntity saved = taskRepository.save(task);
        dispatchAgvTask(saved);
        return Result.success("运输任务创建成功", saved);
    }

    /**
     * LES→AGV联动：运输任务创建成功后同步下发AGV搬运任务。
     * 说明：LesTransportTaskEntity 无任务类型字段可区分厂内搬运，故所有运输任务均默认下发；
     * 下发失败仅记录告警日志，不影响运输任务创建结果。
     *
     * @param lesTask 已保存的LES运输任务
     */
    private void dispatchAgvTask(LesTransportTaskEntity lesTask) {
        if (agvClient == null || lesTask == null) {
            return;
        }
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("taskId", lesTask.getTaskNo());
            body.put("taskType", "搬运");
            body.put("priority", "normal");
            body.put("sourceLocation", null);
            body.put("targetLocation", null);
            body.put("remark", "LES运输任务联动");
            Result<Map<String, Object>> result = agvClient.createTask(body);
            if (isRemoteSuccess(result)) {
                Object agvTaskId = result.getData() == null ? null : result.getData().get("id");
                log.info("AGV任务下发成功：lesTaskNo={}, agvTaskId={}, 标记=agvDispatched", lesTask.getTaskNo(), agvTaskId);
            } else {
                log.warn("AGV任务下发返回失败：lesTaskNo={}, msg={}", lesTask.getTaskNo(), result == null ? null : result.getMsg());
            }
        } catch (Exception e) {
            log.warn("AGV任务下发异常：lesTaskNo={}, error={}", lesTask.getTaskNo(), e.getMessage());
        }
    }

    @Override
    public Result<PageResult<LesTransportTaskEntity>> fetchTransportTasks(int page, int size, Long planId, String status) {
        Pageable pageable = PageRequest.of(Math.max(page, 1) - 1, Math.max(size, 1), Sort.by(Sort.Direction.DESC, "createTime"));
        boolean hasPlanId = planId != null;
        boolean hasStatus = status != null && !status.trim().isEmpty();
        Page<LesTransportTaskEntity> result;
        if (hasPlanId && hasStatus) {
            result = taskRepository.findByPlanIdAndStatus(planId, status.trim(), pageable);
        } else if (hasPlanId) {
            result = taskRepository.findByPlanId(planId, pageable);
        } else if (hasStatus) {
            result = taskRepository.findByStatus(status.trim(), pageable);
        } else {
            result = taskRepository.findAll(pageable);
        }
        PageResult<LesTransportTaskEntity> pageResult = PageResult.build(result.getTotalElements(), result.getSize(), result.getNumber() + 1, result.getContent());
        return Result.success("运输任务查询成功", pageResult);
    }

    @Override
    public Result<LesVehicleEntity> createVehicle(LesVehicleEntity vehicle) {
        if (vehicle == null) {
            return Result.fail("车辆数据不能为空");
        }
        if (vehicle.getStatus() == null || vehicle.getStatus().trim().isEmpty()) {
            vehicle.setStatus("available");
        }
        vehicle.setCreateTime(LocalDateTime.now());
        LesVehicleEntity saved = vehicleRepository.save(vehicle);
        return Result.success("车辆创建成功", saved);
    }

    @Override
    public Result<LesVehicleEntity> updateVehicle(Long id, LesVehicleEntity vehicle) {
        LesVehicleEntity existing = vehicleRepository.findById(id).orElse(null);
        if (existing == null) {
            return Result.fail("车辆不存在");
        }
        if (vehicle == null) {
            return Result.fail("更新数据不能为空");
        }
        if (vehicle.getLicensePlate() != null) {
            existing.setLicensePlate(vehicle.getLicensePlate());
        }
        if (vehicle.getVehicleType() != null) {
            existing.setVehicleType(vehicle.getVehicleType());
        }
        if (vehicle.getLoadCapacity() != null) {
            existing.setLoadCapacity(vehicle.getLoadCapacity());
        }
        if (vehicle.getStatus() != null) {
            existing.setStatus(vehicle.getStatus());
        }
        LesVehicleEntity saved = vehicleRepository.save(existing);
        return Result.success("车辆更新成功", saved);
    }

    @Override
    public Result<Void> deleteVehicle(Long id) {
        if (!vehicleRepository.existsById(id)) {
            return Result.fail("车辆不存在");
        }
        vehicleRepository.deleteById(id);
        return Result.success("车辆删除成功");
    }

    @Override
    public Result<LesDriverEntity> createDriver(LesDriverEntity driver) {
        if (driver == null) {
            return Result.fail("司机数据不能为空");
        }
        if (driver.getStatus() == null || driver.getStatus().trim().isEmpty()) {
            driver.setStatus("available");
        }
        driver.setCreateTime(LocalDateTime.now());
        LesDriverEntity saved = driverRepository.save(driver);
        return Result.success("司机创建成功", saved);
    }

    @Override
    public Result<LesDriverEntity> updateDriver(Long id, LesDriverEntity driver) {
        LesDriverEntity existing = driverRepository.findById(id).orElse(null);
        if (existing == null) {
            return Result.fail("司机不存在");
        }
        if (driver == null) {
            return Result.fail("更新数据不能为空");
        }
        if (driver.getName() != null) {
            existing.setName(driver.getName());
        }
        if (driver.getPhone() != null) {
            existing.setPhone(driver.getPhone());
        }
        if (driver.getLicenseNo() != null) {
            existing.setLicenseNo(driver.getLicenseNo());
        }
        if (driver.getStatus() != null) {
            existing.setStatus(driver.getStatus());
        }
        LesDriverEntity saved = driverRepository.save(existing);
        return Result.success("司机更新成功", saved);
    }

    @Override
    public Result<Void> deleteDriver(Long id) {
        if (!driverRepository.existsById(id)) {
            return Result.fail("司机不存在");
        }
        driverRepository.deleteById(id);
        return Result.success("司机删除成功");
    }

    @Override
    public Result<LesRouteEntity> createRoute(LesRouteEntity route) {
        if (route == null) {
            return Result.fail("路线数据不能为空");
        }
        route.setCreateTime(LocalDateTime.now());
        LesRouteEntity saved = routeRepository.save(route);
        return Result.success("路线创建成功", saved);
    }

    @Override
    public Result<LesRouteEntity> updateRoute(Long id, LesRouteEntity route) {
        LesRouteEntity existing = routeRepository.findById(id).orElse(null);
        if (existing == null) {
            return Result.fail("路线不存在");
        }
        if (route == null) {
            return Result.fail("更新数据不能为空");
        }
        if (route.getRouteName() != null) {
            existing.setRouteName(route.getRouteName());
        }
        if (route.getStartLocation() != null) {
            existing.setStartLocation(route.getStartLocation());
        }
        if (route.getEndLocation() != null) {
            existing.setEndLocation(route.getEndLocation());
        }
        if (route.getDistance() != null) {
            existing.setDistance(route.getDistance());
        }
        if (route.getEstimatedTime() != null) {
            existing.setEstimatedTime(route.getEstimatedTime());
        }
        LesRouteEntity saved = routeRepository.save(existing);
        return Result.success("路线更新成功", saved);
    }

    @Override
    public Result<Void> deleteRoute(Long id) {
        if (!routeRepository.existsById(id)) {
            return Result.fail("路线不存在");
        }
        routeRepository.deleteById(id);
        return Result.success("路线删除成功");
    }

    private LesSalesOrderDto toSalesOrderDto(LogisticsOrderEntity entity) {
        LesSalesOrderDto dto = new LesSalesOrderDto();
        dto.setId(entity.getId());
        dto.setOrderNo(entity.getOrderCode());
        dto.setCustomerName("");
        dto.setDeliveryAddress(entity.getDestinationLocation());
        dto.setOrderDate(formatDateTime(entity.getCreatedTime()));
        dto.setRequiredDate(formatDateTime(entity.getEstimatedArrivalTime()));
        dto.setTotalAmount(entity.getTotalAmount());
        return dto;
    }

    private LesSalesOrderDto toSalesOrderDtoFromWms(Map<String, Object> row) {
        LesSalesOrderDto dto = new LesSalesOrderDto();
        dto.setId(toLong(row.get("id")));
        dto.setOrderNo(toString(row.get("orderNo")));
        dto.setCustomerName(toString(row.get("customerName")));
        dto.setDeliveryAddress(toString(row.get("address")));
        dto.setOrderDate(toString(row.get("createdTime")));
        dto.setRequiredDate(toString(row.get("updatedTime")));
        dto.setTotalAmount(0.0);
        return dto;
    }

    private LesSalesOrderDto toSalesOrderDtoFromCrm(Map<String, Object> row) {
        LesSalesOrderDto dto = new LesSalesOrderDto();
        dto.setId(toLong(row.get("id")));
        dto.setOrderNo(toString(row.get("orderNo")));
        dto.setCustomerName(toString(row.get("customerName")));
        dto.setDeliveryAddress(toString(row.get("deliveryAddress")));
        dto.setOrderDate(toString(row.get("orderDate")));
        dto.setRequiredDate(toString(row.get("deliveryDate")));
        dto.setTotalAmount(toDouble(row.get("totalAmount")));
        return dto;
    }

    private static String generatePlanNo() {
        return "PLAN-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + "-" + (int) (Math.random() * 9000 + 1000);
    }

    private static String generateTaskNo() {
        return "TASK-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + "-" + (int) (Math.random() * 9000 + 1000);
    }

    private static String formatDateTime(LocalDateTime time) {
        if (time == null) {
            return "";
        }
        return time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    private static String toString(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private static Long toLong(Object value) {
        if (value == null) return null;
        if (value instanceof Number n) return n.longValue();
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (Exception ignored) {
            return null;
        }
    }

    private static Double toDouble(Object value) {
        if (value == null) return null;
        if (value instanceof Number n) return n.doubleValue();
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (Exception ignored) {
            return null;
        }
    }

    private boolean isRemoteSuccess(Result<?> result) {
        return result != null && ResponseStatusAdapter.isSuccess(result.getCode());
    }
}
