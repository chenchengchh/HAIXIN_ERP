package com.hxcoe.mes.repository;

import com.hxcoe.mes.entity.WipLocationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WipLocationRepository extends JpaRepository<WipLocationEntity, Long> {
    Optional<WipLocationEntity> findBySnCode(String snCode);
}
