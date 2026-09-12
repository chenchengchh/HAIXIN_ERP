package com.hxcoe.wms.service.impl;

import com.hxcoe.wms.entity.OutboundOrderEntity;
import com.hxcoe.wms.entity.OutboundOrderItemEntity;
import com.hxcoe.wms.entity.WaveEntity;
import com.hxcoe.wms.repository.OutboundOrderRepository;
import com.hxcoe.wms.repository.WaveRepository;
import com.hxcoe.wms.service.PickingTaskService;
import com.hxcoe.wms.service.WaveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class WaveServiceImpl implements WaveService {

    @Autowired
    private WaveRepository waveRepository;

    @Autowired
    private OutboundOrderRepository outboundOrderRepository;

    @Autowired
    private PickingTaskService pickingTaskService;

    @Transactional
    @Override
    public WaveEntity createWave(List<Long> outboundOrderIds, String remark, String createdBy) {
        if (outboundOrderIds == null || outboundOrderIds.isEmpty()) {
            throw new IllegalArgumentException("outboundOrderIds 不能为空");
        }
        WaveEntity wave = new WaveEntity();
        wave.setWaveNo("WV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        wave.setStatus("CREATED");
        wave.setRemark(remark);
        wave.setCreatedBy(createdBy);
        wave = waveRepository.save(wave);

        List<OutboundOrderEntity> orders = outboundOrderRepository.findAllById(outboundOrderIds);
        String type = null;
        boolean mixed = false;
        BigDecimal total = BigDecimal.ZERO;
        int assignedOrderCount = 0;
        for (OutboundOrderEntity order : orders) {
            if (order == null) {
                continue;
            }
            if (order.getWaveId() != null) {
                continue;
            }
            if (type == null) {
                type = order.getType();
            } else if (order.getType() != null && !order.getType().equals(type)) {
                mixed = true;
            }
            order.setWaveId(wave.getId());
            order.setStatus("WAVED"); // 更新出库单状态
            outboundOrderRepository.save(order);
            assignedOrderCount++;
            if (order.getItems() != null) {
                for (OutboundOrderItemEntity item : order.getItems()) {
                    if (item != null && item.getQuantity() != null) {
                        total = total.add(item.getQuantity());
                    }
                }
            }
        }
        wave.setOrderCount(assignedOrderCount);
        wave.setTotalQuantity(total);
        wave.setOrderType(mixed ? "MIXED" : (type == null ? "" : type));
        wave = waveRepository.save(wave);

        return wave;
    }

    @Override
    public WaveEntity autoCreate(String remark, String createdBy) {
        List<OutboundOrderEntity> approved = outboundOrderRepository.findByStatus("APPROVED");
        List<Long> ids = approved.stream()
                .filter(o -> o != null && o.getWaveId() == null)
                .map(OutboundOrderEntity::getId)
                .toList();
        if (ids.isEmpty()) {
            throw new IllegalStateException("没有可生成波次的出库单");
        }
        return createWave(ids, remark, createdBy);
    }

    @Override
    public Page<WaveEntity> getWaves(Pageable pageable, String waveNo, String status, String orderType, LocalDateTime startTime, LocalDateTime endTime) {
        Pageable p = pageable;
        if (p == null) {
            p = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdTime"));
        }
        Specification<WaveEntity> spec = (root, query, cb) -> {
            java.util.List<Predicate> predicates = new java.util.ArrayList<>();
            if (waveNo != null && !waveNo.isBlank()) {
                predicates.add(cb.like(root.get("waveNo"), "%" + waveNo.trim() + "%"));
            }
            if (status != null && !status.isBlank()) {
                predicates.add(cb.equal(root.get("status"), status.trim()));
            }
            if (orderType != null && !orderType.isBlank()) {
                predicates.add(cb.equal(root.get("orderType"), orderType.trim()));
            }
            if (startTime != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdTime"), startTime));
            }
            if (endTime != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdTime"), endTime));
            }
            if (predicates.isEmpty()) {
                return cb.conjunction();
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return waveRepository.findAll(spec, p);
    }

    @Override
    public Optional<WaveEntity> getWaveById(Long id) {
        return waveRepository.findById(id);
    }

    @Override
    public WaveEntity releaseWave(Long id) {
        Optional<WaveEntity> optional = waveRepository.findById(id);
        if (optional.isPresent()) {
            WaveEntity wave = optional.get();
            wave.setStatus("RELEASED");
            wave.setReleasedTime(LocalDateTime.now());
            pickingTaskService.ensureTasksForWave(wave.getId());
            return waveRepository.save(wave);
        }
        return null;
    }

    @Override
    public WaveEntity allocate(Long id, String assignedTo, String remark) {
        WaveEntity wave = waveRepository.findById(id).orElse(null);
        if (wave == null) {
            return null;
        }
        wave.setAssignedTo(assignedTo);
        if (remark != null && !remark.isBlank()) {
            wave.setRemark(remark);
        }
        wave.setAssignedTime(LocalDateTime.now());
        if ("CREATED".equalsIgnoreCase(wave.getStatus())) {
            wave.setStatus("ASSIGNED");
        }
        return waveRepository.save(wave);
    }

    @Override
    public WaveEntity startPicking(Long id) {
        WaveEntity wave = waveRepository.findById(id).orElse(null);
        if (wave == null) {
            return null;
        }
        if (!"RELEASED".equalsIgnoreCase(wave.getStatus()) && !"PICKING".equalsIgnoreCase(wave.getStatus())) {
            wave.setStatus("PICKING");
            return waveRepository.save(wave);
        }
        wave.setStatus("PICKING");
        return waveRepository.save(wave);
    }

    @Override
    public WaveEntity complete(Long id) {
        WaveEntity wave = waveRepository.findById(id).orElse(null);
        if (wave == null) {
            return null;
        }
        wave.setStatus("COMPLETED");
        wave.setCompletedTime(LocalDateTime.now());
        return waveRepository.save(wave);
    }

    @Override
    public WaveEntity cancel(Long id) {
        WaveEntity wave = waveRepository.findById(id).orElse(null);
        if (wave == null) {
            return null;
        }
        wave.setStatus("CANCELLED");
        List<OutboundOrderEntity> orders = outboundOrderRepository.findByWaveId(id);
        for (OutboundOrderEntity order : orders) {
            if (order == null) {
                continue;
            }
            order.setWaveId(null);
            if ("WAVED".equalsIgnoreCase(order.getStatus())) {
                order.setStatus("APPROVED");
            }
            outboundOrderRepository.save(order);
        }
        return waveRepository.save(wave);
    }
}
