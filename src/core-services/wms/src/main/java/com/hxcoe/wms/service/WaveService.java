package com.hxcoe.wms.service;

import com.hxcoe.wms.entity.WaveEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

public interface WaveService {
    WaveEntity createWave(List<Long> outboundOrderIds, String remark, String createdBy);
    WaveEntity autoCreate(String remark, String createdBy);
    Page<WaveEntity> getWaves(Pageable pageable, String waveNo, String status, String orderType, java.time.LocalDateTime startTime, java.time.LocalDateTime endTime);
    Optional<WaveEntity> getWaveById(Long id);
    WaveEntity releaseWave(Long id);
    WaveEntity allocate(Long id, String assignedTo, String remark);
    WaveEntity startPicking(Long id);
    WaveEntity complete(Long id);
    WaveEntity cancel(Long id);
}
