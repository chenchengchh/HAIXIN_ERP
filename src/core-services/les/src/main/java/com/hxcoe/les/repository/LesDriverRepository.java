package com.hxcoe.les.repository;

import com.hxcoe.les.entity.LesDriverEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LesDriverRepository extends JpaRepository<LesDriverEntity, Long> {

    Page<LesDriverEntity> findByStatus(String status, Pageable pageable);

    Page<LesDriverEntity> findByNameContainingIgnoreCaseOrPhoneContainingIgnoreCase(String nameKeyword, String phoneKeyword, Pageable pageable);

    Page<LesDriverEntity> findByStatusAndNameContainingIgnoreCaseOrStatusAndPhoneContainingIgnoreCase(
            String status1,
            String nameKeyword,
            String status2,
            String phoneKeyword,
            Pageable pageable
    );
}

