package com.hxcoe.les.repository;

import com.hxcoe.les.entity.LesSignVoucherEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LesSignVoucherRepository extends JpaRepository<LesSignVoucherEntity, Long> {

    Optional<LesSignVoucherEntity> findTopByPlanIdOrderByCreateTimeDesc(Long planId);

    Page<LesSignVoucherEntity> findByPlanId(Long planId, Pageable pageable);

    Page<LesSignVoucherEntity> findByStatus(String status, Pageable pageable);

    Page<LesSignVoucherEntity> findByPlanIdAndStatus(Long planId, String status, Pageable pageable);

    long countByStatus(String status);
}
