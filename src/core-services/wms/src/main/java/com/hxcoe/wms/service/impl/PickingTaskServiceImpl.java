package com.hxcoe.wms.service.impl;

import com.hxcoe.wms.entity.OutboundOrderEntity;
import com.hxcoe.wms.entity.OutboundOrderItemEntity;
import com.hxcoe.wms.entity.PickingTaskEntity;
import com.hxcoe.wms.entity.PickingTaskItemEntity;
import com.hxcoe.wms.entity.WaveEntity;
import com.hxcoe.wms.repository.OutboundOrderRepository;
import com.hxcoe.wms.repository.PickingTaskRepository;
import com.hxcoe.wms.repository.WaveRepository;
import com.hxcoe.wms.service.PickingTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PickingTaskServiceImpl implements PickingTaskService {

    @Autowired
    private WaveRepository waveRepository;

    @Autowired
    private OutboundOrderRepository outboundOrderRepository;

    @Autowired
    private PickingTaskRepository pickingTaskRepository;

    @Transactional
    @Override
    public void ensureTasksForWave(Long waveId) {
        if (waveId == null) {
            return;
        }
        Optional<WaveEntity> waveOpt = waveRepository.findById(waveId);
        if (waveOpt.isEmpty()) {
            return;
        }
        WaveEntity wave = waveOpt.get();
        List<OutboundOrderEntity> orders = outboundOrderRepository.findByWaveId(waveId);
        for (OutboundOrderEntity order : orders) {
            if (order == null || order.getId() == null) {
                continue;
            }
            if (pickingTaskRepository.findByOutboundOrderId(order.getId()).isPresent()) {
                continue;
            }

            PickingTaskEntity task = new PickingTaskEntity();
            task.setTaskNo(buildTaskNo(wave.getWaveNo(), order.getOrderNo()));
            task.setWaveId(wave.getId());
            task.setWaveNo(wave.getWaveNo());
            task.setOutboundOrderId(order.getId());
            task.setOrderNo(order.getOrderNo());
            task.setStatus("pending");

            List<PickingTaskItemEntity> items = new ArrayList<>();
            if (order.getItems() != null) {
                for (OutboundOrderItemEntity oi : order.getItems()) {
                    if (oi == null) {
                        continue;
                    }
                    PickingTaskItemEntity item = new PickingTaskItemEntity();
                    item.setTask(task);
                    item.setOutboundOrderItemId(oi.getId());
                    item.setMaterialCode(oi.getMaterialCode());
                    item.setMaterialName(oi.getMaterialName());
                    item.setUnit(oi.getUnit());
                    item.setQuantity(oi.getQuantity());
                    item.setLocationCode(oi.getLocationCode());
                    item.setBatchNo(oi.getBatchNo());
                    item.setStatus("pending");
                    items.add(item);
                }
            }
            task.setItems(items);
            pickingTaskRepository.save(task);
        }
    }

    private static String buildTaskNo(String waveNo, String orderNo) {
        String w = waveNo == null ? "WV" : waveNo.replaceAll("[^A-Za-z0-9\\-]", "");
        String o = orderNo == null ? "ORD" : orderNo.replaceAll("[^A-Za-z0-9\\-]", "");
        return ("PICK-" + w + "-" + o);
    }
}

