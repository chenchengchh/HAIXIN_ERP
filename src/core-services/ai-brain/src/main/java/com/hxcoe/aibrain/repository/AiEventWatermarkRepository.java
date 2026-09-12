package com.hxcoe.aibrain.repository;

import com.hxcoe.aibrain.entity.AiEventWatermarkEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 事件水位仓库（P4-3 事件驱动触发）
 */
@Repository
public interface AiEventWatermarkRepository extends JpaRepository<AiEventWatermarkEntity, String> {
}
