package com.hxcoe.les.repository;

import com.hxcoe.les.entity.LesRouteEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LesRouteRepository extends JpaRepository<LesRouteEntity, Long> {

    Page<LesRouteEntity> findByRouteNameContainingIgnoreCase(String keyword, Pageable pageable);
}

